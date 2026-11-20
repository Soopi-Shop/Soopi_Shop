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
            "refresh_tokens",
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
        System.out.println("=== 1. ROW COUNTS FOR ALL 19 TABLES ===");
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

        System.out.println("\n=== 3. VERIFY PASSWORD HASHES (NO PLAINTEXT) ===");
        List<Map<String, Object>> badPasswords = jdbc.queryForList(
                "SELECT id, email FROM users WHERE password_hash NOT LIKE '$2a$%' AND password_hash NOT LIKE '$2b$%' AND password_hash NOT LIKE '$argon2%'"
        );
        if (!badPasswords.isEmpty()) {
            throw new IllegalStateException("FAIL: Found non-hashed passwords: " + badPasswords);
        }
        System.out.println("  PASS: All users have valid bcrypt/argon2 password hashes.");

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
    }
}
