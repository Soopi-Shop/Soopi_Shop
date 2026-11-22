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
                jdbc.update("UPDATE product_variants SET stock_quantity = -1 WHERE id = 1");
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
    }
}
