-- ============================================================================
-- Soopi Shop - Flyway V2 Integrity & Performance Hardening
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. BO SUNG GIA TRI MAC DINH (DEFAULT) DONG BO VOI SOOPI-DB-ERD-01
-- ----------------------------------------------------------------------------

ALTER TABLE orders
    ALTER COLUMN status SET DEFAULT 'PENDING',
    ALTER COLUMN payment_status SET DEFAULT 'UNPAID';

ALTER TABLE payments
    ALTER COLUMN status SET DEFAULT 'PENDING';

ALTER TABLE category_spec_attributes
    ALTER COLUMN data_type SET DEFAULT 'TEXT';

ALTER TABLE products
    ALTER COLUMN warranty_months SET DEFAULT 36;

-- ----------------------------------------------------------------------------
-- 2. GIA CO KHOA NGOAI BANG REVIEWS (CASCADE -> RESTRICT)
--    Ngan chan xoa day chuyen lam mat lich su danh gia hop le (BR-REV-01)
-- ----------------------------------------------------------------------------

ALTER TABLE reviews
    DROP CONSTRAINT IF EXISTS reviews_user_id_fkey,
    DROP CONSTRAINT IF EXISTS reviews_product_id_fkey,
    DROP CONSTRAINT IF EXISTS reviews_order_id_fkey;

ALTER TABLE reviews
    ADD CONSTRAINT reviews_user_id_fkey
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    ADD CONSTRAINT reviews_product_id_fkey
        FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
    ADD CONSTRAINT reviews_order_id_fkey
        FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT;

-- ----------------------------------------------------------------------------
-- 3. TOI UU KIEM TRA PER_USER_LIMIT KHI AP DUNG VOUCHER LUC CHECKOUT (BF-04)
-- ----------------------------------------------------------------------------

CREATE INDEX IF NOT EXISTS idx_orders_user_voucher_active
    ON orders (user_id, voucher_id)
    WHERE voucher_id IS NOT NULL AND status <> 'CANCELLED';

-- ----------------------------------------------------------------------------
-- 4. TOI UU TIM KIEM TU KHOA ILIKE '%keyword%' DAT QM-PERF-01 (<= 500ms)
-- ----------------------------------------------------------------------------

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_products_name_trgm
    ON products USING GIN (name gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_product_variants_sku_trgm
    ON product_variants USING GIN (sku gin_trgm_ops);
