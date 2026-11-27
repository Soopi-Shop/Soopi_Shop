package vn.soopishop.verify;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

@Component
public class DatabaseVerifier {

    private static final List<String> TABLES = List.of(
            "users",
            "user_addresses",
            "categories",
            "brands",
            "category_spec_attributes",
            "products",
            "product_variants",
            "product_images",
            "product_specifications",
            "carts",
            "cart_items",
            "vouchers",
            "orders",
            "order_items",
            "order_status_history",
            "payments",
            "inventory_transactions",
            "reviews"
    );

    private static final List<String> REQUIRED_CATEGORY_SLUGS = List.of(
            "cpu",
            "gpu",
            "mainboard",
            "ram",
            "ssd-hdd",
            "psu",
            "case",
            "tan-nhiet",
            "phu-kien"
    );

    private final JdbcTemplate jdbc;
    private final TransactionTemplate txTemplate;

    public DatabaseVerifier(JdbcTemplate jdbc, TransactionTemplate txTemplate) {
        this.jdbc = jdbc;
        this.txTemplate = txTemplate;
    }

    public void verify() {
        System.out.println("=== 1. ROW COUNTS FOR ALL 18 TABLES ===");
        for (String table : TABLES) {
            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
            System.out.printf("  %-28s : %d%n", table, count);
        }

        System.out.println("\n=== 2. TEST NEGATIVE STOCK CONSTRAINT (ROLLBACK) ===");
        txTemplate.executeWithoutResult(status -> {
            status.setRollbackOnly();
            try {
                jdbc.update("UPDATE product_variants SET stock_quantity = -1 WHERE id = (SELECT MIN(id) FROM product_variants)");
                throw new IllegalStateException("FAIL: Negative stock was allowed!");
            } catch (DataIntegrityViolationException ex) {
                System.out.println("  PASS: Constraint rejected negative stock -> " + ex.getMostSpecificCause().getMessage());
            }
        });

        System.out.println("\n=== 3. VERIFY CLERK USER MAPPING (NO LOCAL PASSWORD STORAGE) ===");
        Integer passwordColCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'password_hash'",
                Integer.class
        );
        if (passwordColCount != null && passwordColCount > 0) {
            throw new IllegalStateException("FAIL: users.password_hash column should not exist when using Clerk.");
        }
        List<Map<String, Object>> badClerkIds = jdbc.queryForList(
                "SELECT id, email, clerk_user_id FROM users WHERE clerk_user_id IS NULL OR clerk_user_id NOT LIKE 'user_%'"
        );
        if (!badClerkIds.isEmpty()) {
            throw new IllegalStateException("FAIL: Found invalid clerk_user_id values: " + badClerkIds);
        }
        System.out.println("  PASS: All users have valid Clerk user IDs (user_*) and no password_hash column exists.");

        System.out.println("\n=== 4. VERIFY INVENTORY LEDGER CONSISTENCY ===");
        List<Map<String, Object>> mismatches = jdbc.queryForList(
                """
                SELECT pv.sku, pv.stock_quantity, COALESCE(SUM(it.quantity_change), 0)::int AS ledger_sum
                FROM product_variants pv
                LEFT JOIN inventory_transactions it ON it.variant_id = pv.id
                GROUP BY pv.id, pv.sku, pv.stock_quantity
                HAVING pv.stock_quantity <> COALESCE(SUM(it.quantity_change), 0)
                """
        );
        if (!mismatches.isEmpty()) {
            throw new IllegalStateException("FAIL: Inventory ledger mismatch: " + mismatches);
        }
        System.out.println("  PASS: Every variant stock_quantity matches SUM(inventory_transactions.quantity_change).");

        System.out.println("\n=== 5. VERIFY V2 INTEGRITY & PERFORMANCE HARDENING ===");
        Integer nonRestrictReviewFks = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM pg_constraint
                WHERE conrelid = 'reviews'::regclass
                  AND contype = 'f'
                  AND confdeltype <> 'r'
                """,
                Integer.class
        );
        if (nonRestrictReviewFks != null && nonRestrictReviewFks > 0) {
            throw new IllegalStateException("FAIL: Expected all reviews foreign keys to use ON DELETE RESTRICT.");
        }

        List<String> requiredV2Indexes = List.of(
                "idx_orders_user_voucher_active",
                "idx_products_name_trgm",
                "idx_product_variants_sku_trgm"
        );
        for (String idx : requiredV2Indexes) {
            Integer exists = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM pg_indexes WHERE schemaname = 'public' AND indexname = ?",
                    Integer.class,
                    idx
            );
            if (exists == null || exists == 0) {
                throw new IllegalStateException("FAIL: Missing V2 index: " + idx);
            }
        }
        System.out.println("  PASS: V2 hardening verified (reviews FKs = RESTRICT, voucher & pg_trgm indexes present).");

        System.out.println("\n=== 6. VERIFY CATALOG & SEARCH READINESS (ALL 9 CATEGORIES & SPECS) ===");
        for (String slug : REQUIRED_CATEGORY_SLUGS) {
            Map<String, Object> stats = jdbc.queryForMap(
                    """
                    SELECT c.name,
                           (SELECT COUNT(*) FROM category_spec_attributes csa WHERE csa.category_id = c.id AND csa.is_filterable = TRUE) AS attr_cnt,
                           (SELECT COUNT(*) FROM products p WHERE p.category_id = c.id) AS prod_cnt,
                           (SELECT COUNT(*) FROM product_variants pv JOIN products p ON p.id = pv.product_id WHERE p.category_id = c.id) AS var_cnt
                    FROM categories c
                    WHERE c.slug = ? AND c.is_active = TRUE
                    """,
                    slug
            );
            long attrCnt = ((Number) stats.get("attr_cnt")).longValue();
            long prodCnt = ((Number) stats.get("prod_cnt")).longValue();
            long varCnt = ((Number) stats.get("var_cnt")).longValue();
            if (attrCnt < 4 || prodCnt < 10 || varCnt < 10) {
                throw new IllegalStateException(
                        "FAIL: Category '" + slug + "' has insufficient seed data: attrs=" + attrCnt + ", products=" + prodCnt + ", variants=" + varCnt
                );
            }
            System.out.printf("  %-12s : %2d attrs | %2d products | %2d variants (%s)%n",
                    slug, attrCnt, prodCnt, varCnt, stats.get("name"));
        }

        Integer productsWithoutVariants = jdbc.queryForObject(
                "SELECT COUNT(*) FROM products p WHERE NOT EXISTS (SELECT 1 FROM product_variants pv WHERE pv.product_id = p.id)",
                Integer.class
        );
        Integer productsWithoutPrimaryImg = jdbc.queryForObject(
                "SELECT COUNT(*) FROM products p WHERE NOT EXISTS (SELECT 1 FROM product_images pi WHERE pi.product_id = p.id AND pi.is_primary = TRUE)",
                Integer.class
        );
        Integer productsWithoutSpecs = jdbc.queryForObject(
                "SELECT COUNT(*) FROM products p WHERE (SELECT COUNT(*) FROM product_specifications ps WHERE ps.product_id = p.id) < 3",
                Integer.class
        );
        Integer crossCategorySpecs = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM product_specifications ps
                JOIN products p ON p.id = ps.product_id
                JOIN category_spec_attributes csa ON csa.id = ps.spec_attribute_id
                WHERE p.category_id <> csa.category_id
                """,
                Integer.class
        );
        if ((productsWithoutVariants != null && productsWithoutVariants > 0)
                || (productsWithoutPrimaryImg != null && productsWithoutPrimaryImg > 0)
                || (productsWithoutSpecs != null && productsWithoutSpecs > 0)
                || (crossCategorySpecs != null && crossCategorySpecs > 0)) {
            throw new IllegalStateException(
                    "FAIL: Catalog integrity errors -> missingVariants=" + productsWithoutVariants
                            + ", missingPrimaryImg=" + productsWithoutPrimaryImg
                            + ", sparseSpecs=" + productsWithoutSpecs
                            + ", crossCategorySpecs=" + crossCategorySpecs
            );
        }
        System.out.println("  PASS: All 9 categories, 90+ products, variants, primary images & specs verified for Catalog & Search.");
    }
}
