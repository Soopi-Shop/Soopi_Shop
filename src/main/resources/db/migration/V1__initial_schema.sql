-- ============================================================================
-- Soopi Shop - Flyway V1 Initial PostgreSQL Schema (19 tables across 6 domains)
-- ============================================================================

-- ----------------------------------------------------------------------------
-- 1. NGUOI DUNG & BAO MAT (RBAC)
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS users (
    id                      BIGSERIAL PRIMARY KEY,
    full_name               VARCHAR(150) NOT NULL,
    email                   VARCHAR(255) NOT NULL UNIQUE,
    phone                   VARCHAR(20),
    password_hash           TEXT NOT NULL,
    role                    VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER', 'STAFF', 'ADMIN')),
    status                  VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'LOCKED')),
    failed_login_attempts   INT NOT NULL DEFAULT 0 CHECK (failed_login_attempts >= 0),
    locked_until            TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash  TEXT NOT NULL UNIQUE,
    expires_at  TIMESTAMPTZ NOT NULL,
    revoked_at  TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens (user_id);

CREATE TABLE IF NOT EXISTS user_addresses (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    recipient_name  VARCHAR(150) NOT NULL,
    recipient_phone VARCHAR(20) NOT NULL,
    province        VARCHAR(100) NOT NULL,
    district        VARCHAR(100) NOT NULL,
    ward            VARCHAR(100) NOT NULL,
    street_address  VARCHAR(255) NOT NULL,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_addresses_user_id ON user_addresses (user_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_user_addresses_default_per_user
    ON user_addresses (user_id)
    WHERE is_default = TRUE;

-- ----------------------------------------------------------------------------
-- 2. DANH MUC, SAN PHAM, BIEN THE & THONG SO KY THUAT
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS categories (
    id          BIGSERIAL PRIMARY KEY,
    parent_id   BIGINT REFERENCES categories(id) ON DELETE RESTRICT,
    name        VARCHAR(150) NOT NULL,
    slug        VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS brands (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150) NOT NULL UNIQUE,
    slug        VARCHAR(150) NOT NULL UNIQUE,
    logo_url    TEXT,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS category_spec_attributes (
    id              BIGSERIAL PRIMARY KEY,
    category_id     BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name            VARCHAR(150) NOT NULL,
    code            VARCHAR(100) NOT NULL,
    unit            VARCHAR(50),
    data_type       VARCHAR(20) NOT NULL CHECK (data_type IN ('TEXT', 'NUMBER', 'BOOLEAN')),
    is_filterable   BOOLEAN NOT NULL DEFAULT TRUE,
    display_order   INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_category_spec_attr_code UNIQUE (category_id, code)
);

CREATE TABLE IF NOT EXISTS products (
    id              BIGSERIAL PRIMARY KEY,
    category_id     BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    brand_id        BIGINT NOT NULL REFERENCES brands(id) ON DELETE RESTRICT,
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(255) NOT NULL UNIQUE,
    description     TEXT,
    warranty_months INT NOT NULL DEFAULT 12 CHECK (warranty_months >= 0),
    condition_type  VARCHAR(20) NOT NULL DEFAULT 'NEW_FULLBOX' CHECK (condition_type IN ('NEW_FULLBOX', 'TRAY')),
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'OUT_OF_STOCK', 'INACTIVE')),
    avg_rating      NUMERIC(3, 2) NOT NULL DEFAULT 0 CHECK (avg_rating >= 0 AND avg_rating <= 5),
    review_count    INT NOT NULL DEFAULT 0 CHECK (review_count >= 0),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_products_cat_brand_status
    ON products (category_id, brand_id, status);

CREATE TABLE IF NOT EXISTS product_variants (
    id                  BIGSERIAL PRIMARY KEY,
    product_id          BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    sku                 VARCHAR(100) NOT NULL UNIQUE,
    variant_name        VARCHAR(255) NOT NULL,
    list_price          NUMERIC(14, 2) NOT NULL CHECK (list_price >= 0),
    sale_price          NUMERIC(14, 2) NOT NULL CHECK (sale_price >= 0 AND sale_price <= list_price),
    stock_quantity      INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    low_stock_threshold INT NOT NULL DEFAULT 5 CHECK (low_stock_threshold >= 0),
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_product_variants_prod_price_stock
    ON product_variants (product_id, sale_price, stock_quantity);
CREATE INDEX IF NOT EXISTS idx_product_variants_stock_quantity
    ON product_variants (stock_quantity);

CREATE TABLE IF NOT EXISTS product_images (
    id          BIGSERIAL PRIMARY KEY,
    product_id  BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_id  BIGINT REFERENCES product_variants(id) ON DELETE CASCADE,
    image_url   TEXT NOT NULL,
    is_primary  BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order  INT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_product_images_product_id ON product_images (product_id);

CREATE TABLE IF NOT EXISTS product_specifications (
    id                  BIGSERIAL PRIMARY KEY,
    product_id          BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_id          BIGINT REFERENCES product_variants(id) ON DELETE CASCADE,
    spec_attribute_id   BIGINT NOT NULL REFERENCES category_spec_attributes(id) ON DELETE RESTRICT,
    value_text          TEXT NOT NULL,
    value_number        NUMERIC(18, 4)
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_product_specs_product_level
    ON product_specifications (product_id, spec_attribute_id)
    WHERE variant_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_product_specs_variant_level
    ON product_specifications (product_id, variant_id, spec_attribute_id)
    WHERE variant_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_product_specs_attr_text
    ON product_specifications (spec_attribute_id, value_text);
CREATE INDEX IF NOT EXISTS idx_product_specs_attr_number
    ON product_specifications (spec_attribute_id, value_number);

-- ----------------------------------------------------------------------------
-- 3. GIO HANG & VOUCHER
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS carts (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS cart_items (
    id          BIGSERIAL PRIMARY KEY,
    cart_id     BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    variant_id  BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    quantity    INT NOT NULL CHECK (quantity > 0),
    added_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_cart_items_cart_variant UNIQUE (cart_id, variant_id)
);

CREATE TABLE IF NOT EXISTS vouchers (
    id                      BIGSERIAL PRIMARY KEY,
    code                    VARCHAR(50) NOT NULL UNIQUE,
    name                    VARCHAR(255) NOT NULL,
    discount_type           VARCHAR(20) NOT NULL CHECK (discount_type IN ('PERCENT', 'FIXED')),
    discount_value          NUMERIC(14, 2) NOT NULL CHECK (discount_value > 0),
    max_discount_amount     NUMERIC(14, 2) CHECK (max_discount_amount IS NULL OR max_discount_amount >= 0),
    min_order_value         NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (min_order_value >= 0),
    usage_limit             INT NOT NULL CHECK (usage_limit > 0),
    used_count              INT NOT NULL DEFAULT 0 CHECK (used_count >= 0 AND used_count <= usage_limit),
    per_user_limit          INT NOT NULL DEFAULT 1 CHECK (per_user_limit > 0),
    applicable_category_id  BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    start_at                TIMESTAMPTZ NOT NULL,
    end_at                  TIMESTAMPTZ NOT NULL,
    is_active               BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_vouchers_date_range CHECK (end_at > start_at)
);

-- ----------------------------------------------------------------------------
-- 4. DON HANG & THANH TOAN
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS orders (
    id                  BIGSERIAL PRIMARY KEY,
    order_code          VARCHAR(50) NOT NULL UNIQUE,
    user_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    voucher_id          BIGINT REFERENCES vouchers(id) ON DELETE RESTRICT,
    status              VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPING', 'COMPLETED', 'CANCELLED')),
    payment_method      VARCHAR(20) NOT NULL CHECK (payment_method IN ('COD', 'SANDBOX', 'MOCK')),
    payment_status      VARCHAR(20) NOT NULL CHECK (payment_status IN ('UNPAID', 'PAID', 'FAILED', 'REFUNDED')),
    recipient_name      VARCHAR(150) NOT NULL,
    recipient_phone     VARCHAR(20) NOT NULL,
    shipping_address    TEXT NOT NULL,
    note                TEXT,
    subtotal_amount     NUMERIC(14, 2) NOT NULL CHECK (subtotal_amount >= 0),
    voucher_discount    NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (voucher_discount >= 0),
    shipping_fee        NUMERIC(14, 2) NOT NULL DEFAULT 0 CHECK (shipping_fee >= 0),
    final_total         NUMERIC(14, 2) NOT NULL CHECK (final_total >= 0),
    cancel_reason       TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_orders_user_status_created
    ON orders (user_id, status, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_orders_status_created
    ON orders (status, created_at DESC);

CREATE TABLE IF NOT EXISTS order_items (
    id                      BIGSERIAL PRIMARY KEY,
    order_id                BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id              BIGINT NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    variant_id              BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE RESTRICT,
    product_name_snapshot   VARCHAR(255) NOT NULL,
    variant_name_snapshot   VARCHAR(255) NOT NULL,
    sku_snapshot            VARCHAR(100) NOT NULL,
    list_price_snapshot     NUMERIC(14, 2) NOT NULL CHECK (list_price_snapshot >= 0),
    unit_price_snapshot     NUMERIC(14, 2) NOT NULL CHECK (unit_price_snapshot >= 0),
    quantity                INT NOT NULL CHECK (quantity > 0),
    line_subtotal           NUMERIC(14, 2) NOT NULL CHECK (line_subtotal >= 0)
);

CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items (order_id);

CREATE TABLE IF NOT EXISTS order_status_history (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    from_status VARCHAR(20) CHECK (from_status IS NULL OR from_status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPING', 'COMPLETED', 'CANCELLED')),
    to_status   VARCHAR(20) NOT NULL CHECK (to_status IN ('PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPING', 'COMPLETED', 'CANCELLED')),
    changed_by  BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    note        TEXT,
    changed_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_order_status_history_order_id ON order_status_history (order_id);

CREATE TABLE IF NOT EXISTS payments (
    id                      BIGSERIAL PRIMARY KEY,
    order_id                BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    provider                VARCHAR(30) NOT NULL CHECK (provider IN ('COD', 'VNPAY_SANDBOX', 'MOCK_GATEWAY')),
    transaction_ref         VARCHAR(150) UNIQUE,
    amount                  NUMERIC(14, 2) NOT NULL CHECK (amount >= 0),
    status                  VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    raw_callback_payload    JSONB,
    paid_at                 TIMESTAMPTZ,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments (order_id);

-- ----------------------------------------------------------------------------
-- 5. TRUY VET TON KHO
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS inventory_transactions (
    id                  BIGSERIAL PRIMARY KEY,
    variant_id          BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE RESTRICT,
    order_id            BIGINT REFERENCES orders(id) ON DELETE SET NULL,
    transaction_type    VARCHAR(30) NOT NULL CHECK (transaction_type IN ('STOCK_IN', 'ADJUSTMENT', 'ORDER_DEDUCT', 'ORDER_CANCEL_RESTOCK')),
    quantity_before     INT NOT NULL CHECK (quantity_before >= 0),
    quantity_change     INT NOT NULL CHECK (quantity_change <> 0),
    quantity_after      INT NOT NULL CHECK (quantity_after >= 0),
    note                TEXT,
    performed_by        BIGINT NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_inventory_math CHECK (quantity_before + quantity_change = quantity_after)
);

CREATE INDEX IF NOT EXISTS idx_inventory_tx_variant_created
    ON inventory_transactions (variant_id, created_at DESC);

-- ----------------------------------------------------------------------------
-- 6. DANH GIA SAN PHAM
-- ----------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS reviews (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id              BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    order_id                BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    rating                  INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment                 TEXT,
    is_verified_purchase    BOOLEAN NOT NULL DEFAULT TRUE,
    is_visible              BOOLEAN NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_reviews_user_product_order UNIQUE (user_id, product_id, order_id)
);

CREATE INDEX IF NOT EXISTS idx_reviews_product_visible_created
    ON reviews (product_id, created_at DESC)
    WHERE is_visible = TRUE;
