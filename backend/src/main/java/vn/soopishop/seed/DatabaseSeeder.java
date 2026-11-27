package vn.soopishop.seed;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import vn.soopishop.seed.CatalogSeedData.BrandDef;
import vn.soopishop.seed.CatalogSeedData.CatDef;
import vn.soopishop.seed.CatalogSeedData.ProductDef;
import vn.soopishop.seed.CatalogSeedData.SpecAttrDef;
import vn.soopishop.seed.CatalogSeedData.SpecVal;
import vn.soopishop.seed.CatalogSeedData.VariantDef;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseSeeder {

    private final JdbcTemplate jdbc;
    private final TransactionTemplate txTemplate;

    public DatabaseSeeder(JdbcTemplate jdbc, TransactionTemplate txTemplate) {
        this.jdbc = jdbc;
        this.txTemplate = txTemplate;
    }

    public void seed() {
        txTemplate.executeWithoutResult(status -> {
            Map<String, Long> userMap = seedUsers();
            seedUserAddresses(userMap);
            Map<String, Long> categoryMap = seedCategories();
            Map<String, Long> brandMap = seedBrands();
            Map<String, Long> specAttrMap = seedCategorySpecAttributes(categoryMap);

            Map<String, Long> productMap = new HashMap<>();
            Map<String, VariantInfo> variantMap = new HashMap<>();
            Map<String, Integer> initialStockMap = new LinkedHashMap<>();
            seedCatalog(categoryMap, brandMap, specAttrMap, productMap, variantMap, initialStockMap);

            Map<String, Long> voucherMap = seedVouchers(categoryMap);
            seedCart(userMap, variantMap);
            List<OrderDef> orders = buildOrders();
            Map<String, Long> orderMap = seedOrders(userMap, voucherMap, variantMap, orders);
            seedInventoryLedger(userMap, variantMap, initialStockMap, orderMap, orders);
            seedReviews(userMap, productMap, orderMap);
        });

        System.out.println("Seed completed successfully (9 categories, 90 products, Clerk auth mapping enabled).");
    }

    private Map<String, Long> seedUsers() {
        record UserDef(String clerkUserId, String name, String email, String phone, String role, String status) {}
        List<UserDef> users = List.of(
                new UserDef("user_seed_admin_01", "Nguyễn Hoàng Admin", "admin@soopishop.vn", "0901000001", "ADMIN", "ACTIVE"),
                new UserDef("user_seed_staff_01", "Trần Minh Staff", "staff@soopishop.vn", "0901000002", "STAFF", "ACTIVE"),
                new UserDef("user_seed_khach1_01", "Lê Văn Khách Một", "khach1@gmail.com", "0912345001", "CUSTOMER", "ACTIVE"),
                new UserDef("user_seed_khach2_01", "Phạm Thị Khách Hai", "khach2@gmail.com", "0912345002", "CUSTOMER", "ACTIVE"),
                new UserDef("user_seed_khach3_01", "Hoàng Anh Khách Ba", "khach3@gmail.com", "0912345003", "CUSTOMER", "ACTIVE"),
                new UserDef("user_seed_locked_01", "Tài Khoản Bị Khóa", "locked@gmail.com", "0912345999", "CUSTOMER", "LOCKED")
        );

        Map<String, Long> map = new HashMap<>();
        for (UserDef u : users) {
            Long id = jdbc.queryForObject(
                    """
                    INSERT INTO users (clerk_user_id, full_name, email, phone, role, status)
                    VALUES (?, ?, ?, ?, ?, ?)
                    ON CONFLICT (clerk_user_id) DO UPDATE SET
                      full_name = EXCLUDED.full_name,
                      email = EXCLUDED.email,
                      phone = EXCLUDED.phone,
                      role = EXCLUDED.role,
                      status = EXCLUDED.status,
                      updated_at = NOW()
                    RETURNING id
                    """,
                    Long.class,
                    u.clerkUserId(), u.name(), u.email(), u.phone(), u.role(), u.status()
            );
            map.put(u.email(), id);
        }
        return map;
    }

    private void seedUserAddresses(Map<String, Long> userMap) {
        record AddrDef(String email, String name, String phone, String province, String district, String ward, String street, String legacyStreet, boolean isDefault) {}
        List<AddrDef> addrs = List.of(
                new AddrDef("khach1@gmail.com", "Lê Văn Khách Một", "0912345001", "TP. Hồ Chí Minh", "Quận 10", "Phường 12", "268 Lý Thường Kiệt", "268 Ly Thuong Kiet", true),
                new AddrDef("khach1@gmail.com", "Lê Văn Khách Một (Công ty)", "0912345001", "TP. Hồ Chí Minh", "Quận 1", "Phường Bến Nghé", "123 Lê Lợi", "123 Le Loi", false),
                new AddrDef("khach2@gmail.com", "Phạm Thị Khách Hai", "0912345002", "Hà Nội", "Quận Cầu Giấy", "Phường Dịch Vọng", "144 Xuân Thủy", "144 Xuan Thuy", true),
                new AddrDef("khach3@gmail.com", "Hoàng Anh Khách Ba", "0912345003", "Hà Nội", "Quận Hai Bà Trưng", "Phường Bách Khoa", "1 Đại Cồ Việt", "1 Dai Co Viet", true)
        );
        for (AddrDef a : addrs) {
            Long uid = userMap.get(a.email());
            List<Long> existingIds = jdbc.queryForList(
                    "SELECT id FROM user_addresses WHERE user_id = ? AND (street_address = ? OR street_address = ?) ORDER BY id ASC LIMIT 1",
                    Long.class, uid, a.street(), a.legacyStreet()
            );
            if (existingIds.isEmpty()) {
                jdbc.update(
                        """
                        INSERT INTO user_addresses
                          (user_id, recipient_name, recipient_phone, province, district, ward, street_address, is_default)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                        uid, a.name(), a.phone(), a.province(), a.district(), a.ward(), a.street(), a.isDefault()
                );
            } else {
                jdbc.update(
                        """
                        UPDATE user_addresses
                        SET recipient_name = ?, recipient_phone = ?, province = ?, district = ?, ward = ?, street_address = ?, updated_at = NOW()
                        WHERE id = ?
                        """,
                        a.name(), a.phone(), a.province(), a.district(), a.ward(), a.street(), existingIds.get(0)
                );
            }
        }
    }

    private Map<String, Long> seedCategories() {
        Map<String, Long> map = new HashMap<>();
        for (CatDef c : CatalogSeedData.categories()) {
            Long id = jdbc.queryForObject(
                    """
                    INSERT INTO categories (name, slug, description, is_active)
                    VALUES (?, ?, ?, TRUE)
                    ON CONFLICT (slug) DO UPDATE SET
                      name = EXCLUDED.name,
                      description = EXCLUDED.description,
                      is_active = TRUE,
                      updated_at = NOW()
                    RETURNING id
                    """,
                    Long.class, c.name(), c.slug(), c.desc()
            );
            map.put(c.slug(), id);
        }
        return map;
    }

    private Map<String, Long> seedBrands() {
        Map<String, Long> map = new HashMap<>();
        for (BrandDef b : CatalogSeedData.brands()) {
            Long id = jdbc.queryForObject(
                    """
                    INSERT INTO brands (name, slug, logo_url, is_active)
                    VALUES (?, ?, ?, TRUE)
                    ON CONFLICT (slug) DO UPDATE SET
                      name = EXCLUDED.name,
                      logo_url = EXCLUDED.logo_url,
                      is_active = TRUE
                    RETURNING id
                    """,
                    Long.class, b.name(), b.slug(), b.logo()
            );
            map.put(b.slug(), id);
        }
        return map;
    }

    private Map<String, Long> seedCategorySpecAttributes(Map<String, Long> categoryMap) {
        Map<String, Long> map = new HashMap<>();
        for (SpecAttrDef sa : CatalogSeedData.specAttributes()) {
            Long categoryId = categoryMap.get(sa.cat());
            if (categoryId == null) {
                throw new IllegalStateException("Unknown category slug for spec attribute: " + sa.cat());
            }
            Long id = jdbc.queryForObject(
                    """
                    INSERT INTO category_spec_attributes
                      (category_id, name, code, unit, data_type, is_filterable, display_order)
                    VALUES (?, ?, ?, ?, ?, TRUE, ?)
                    ON CONFLICT (category_id, code) DO UPDATE SET
                      name = EXCLUDED.name,
                      unit = EXCLUDED.unit,
                      data_type = EXCLUDED.data_type,
                      is_filterable = TRUE,
                      display_order = EXCLUDED.display_order
                    RETURNING id
                    """,
                    Long.class, categoryId, sa.name(), sa.code(), sa.unit(), sa.type(), sa.order()
            );
            map.put(sa.cat() + ":" + sa.code(), id);
        }
        return map;
    }

    public record VariantInfo(long id, long productId, String productName, String variantName, long listPrice, long salePrice) {}

    private void seedCatalog(
            Map<String, Long> categoryMap,
            Map<String, Long> brandMap,
            Map<String, Long> specAttrMap,
            Map<String, Long> productMap,
            Map<String, VariantInfo> variantMap,
            Map<String, Integer> initialStockMap
    ) {
        for (ProductDef p : CatalogSeedData.allProducts()) {
            Long categoryId = categoryMap.get(p.cat());
            Long brandId = brandMap.get(p.brand());
            if (categoryId == null || brandId == null) {
                throw new IllegalStateException("Missing category or brand for product: " + p.slug());
            }

            Long productId = jdbc.queryForObject(
                    """
                    INSERT INTO products (category_id, brand_id, name, slug, description, warranty_months, condition_type, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT (slug) DO UPDATE SET
                      category_id = EXCLUDED.category_id,
                      brand_id = EXCLUDED.brand_id,
                      name = EXCLUDED.name,
                      description = EXCLUDED.description,
                      warranty_months = EXCLUDED.warranty_months,
                      condition_type = EXCLUDED.condition_type,
                      status = EXCLUDED.status,
                      updated_at = NOW()
                    RETURNING id
                    """,
                    Long.class,
                    categoryId, brandId, p.name(), p.slug(), p.desc(), p.warranty(), p.condition(), p.status()
            );
            productMap.put(p.slug(), productId);

            upsertProductImage(productId, null, "https://placehold.co/600x600?text=" + p.slug(), true, 0);
            upsertProductImage(productId, null, "https://placehold.co/600x600?text=" + p.slug() + "-detail", false, 1);

            for (SpecVal s : p.specs()) {
                Long attrId = resolveSpecAttrId(specAttrMap, p.cat(), s.code());
                jdbc.update(
                        """
                        INSERT INTO product_specifications (product_id, variant_id, spec_attribute_id, value_text, value_number)
                        VALUES (?, NULL, ?, ?, ?)
                        ON CONFLICT (product_id, spec_attribute_id) WHERE variant_id IS NULL
                        DO UPDATE SET value_text = EXCLUDED.value_text, value_number = EXCLUDED.value_number
                        """,
                        productId, attrId, s.text(), s.num() == null ? null : BigDecimal.valueOf(s.num())
                );
            }

            for (VariantDef v : p.variants()) {
                Long variantId = jdbc.queryForObject(
                        """
                        INSERT INTO product_variants (product_id, sku, variant_name, list_price, sale_price, stock_quantity, low_stock_threshold, is_active)
                        VALUES (?, ?, ?, ?, ?, ?, 5, TRUE)
                        ON CONFLICT (sku) DO UPDATE SET
                          product_id = EXCLUDED.product_id,
                          variant_name = EXCLUDED.variant_name,
                          list_price = EXCLUDED.list_price,
                          sale_price = EXCLUDED.sale_price,
                          updated_at = NOW()
                        RETURNING id
                        """,
                        Long.class, productId, v.sku(), v.name(), v.listPrice(), v.salePrice(), v.initialStock()
                );
                variantMap.put(v.sku(), new VariantInfo(variantId, productId, p.name(), v.name(), v.listPrice(), v.salePrice()));
                initialStockMap.put(v.sku(), v.initialStock());

                for (SpecVal vs : v.specs()) {
                    Long attrId = resolveSpecAttrId(specAttrMap, p.cat(), vs.code());
                    jdbc.update(
                            """
                            INSERT INTO product_specifications (product_id, variant_id, spec_attribute_id, value_text, value_number)
                            VALUES (?, ?, ?, ?, ?)
                            ON CONFLICT (product_id, variant_id, spec_attribute_id) WHERE variant_id IS NOT NULL
                            DO UPDATE SET value_text = EXCLUDED.value_text, value_number = EXCLUDED.value_number
                            """,
                            productId, variantId, attrId, vs.text(), vs.num() == null ? null : BigDecimal.valueOf(vs.num())
                    );
                }
            }
        }
    }

    private void upsertProductImage(Long productId, Long variantId, String imageUrl, boolean isPrimary, int sortOrder) {
        Integer exists = variantId == null
                ? jdbc.queryForObject(
                        "SELECT COUNT(*) FROM product_images WHERE product_id = ? AND variant_id IS NULL AND sort_order = ?",
                        Integer.class, productId, sortOrder
                )
                : jdbc.queryForObject(
                        "SELECT COUNT(*) FROM product_images WHERE product_id = ? AND variant_id = ? AND sort_order = ?",
                        Integer.class, productId, variantId, sortOrder
                );
        if (exists == null || exists == 0) {
            jdbc.update(
                    "INSERT INTO product_images (product_id, variant_id, image_url, is_primary, sort_order) VALUES (?, ?, ?, ?, ?)",
                    productId, variantId, imageUrl, isPrimary, sortOrder
            );
        }
    }

    private static Long resolveSpecAttrId(Map<String, Long> specAttrMap, String categorySlug, String specCode) {
        Long attrId = specAttrMap.get(categorySlug + ":" + specCode);
        if (attrId == null) {
            throw new IllegalStateException("Unmapped spec attribute code '" + specCode + "' for category '" + categorySlug + "'");
        }
        return attrId;
    }

    private Map<String, Long> seedVouchers(Map<String, Long> categoryMap) {
        Instant now = Instant.now();
        record VoucherDef(String code, String name, String type, long val, Long maxDiscount, long minOrder, int limit, int used, Long catId, Timestamp start, Timestamp end) {}
        List<VoucherDef> vouchers = List.of(
                new VoucherDef("SOOPI10", "Giảm 10% tối đa 500K cho đơn từ 2 triệu", "PERCENT", 10, 500000L, 2000000, 100, 1, null, Timestamp.from(now.minus(30, ChronoUnit.DAYS)), Timestamp.from(now.plus(60, ChronoUnit.DAYS))),
                new VoucherDef("GIAM200K", "Giảm trực tiếp 200K cho đơn từ 1.5 triệu", "FIXED", 200000, null, 1500000, 50, 1, null, Timestamp.from(now.minus(15, ChronoUnit.DAYS)), Timestamp.from(now.plus(45, ChronoUnit.DAYS))),
                new VoucherDef("RAMONLY", "Giảm 150K chỉ áp dụng danh mục RAM", "FIXED", 150000, null, 1000000, 30, 0, categoryMap.get("ram"), Timestamp.from(now.minus(10, ChronoUnit.DAYS)), Timestamp.from(now.plus(30, ChronoUnit.DAYS))),
                new VoucherDef("PHUKIEN15", "Giảm 15% tối đa 300K cho danh mục Phụ kiện", "PERCENT", 15, 300000L, 500000, 60, 1, categoryMap.get("phu-kien"), Timestamp.from(now.minus(10, ChronoUnit.DAYS)), Timestamp.from(now.plus(45, ChronoUnit.DAYS))),
                new VoucherDef("GPU500K", "Giảm 500K cho card màn hình từ 8 triệu", "FIXED", 500000, null, 8000000, 25, 1, categoryMap.get("gpu"), Timestamp.from(now.minus(10, ChronoUnit.DAYS)), Timestamp.from(now.plus(45, ChronoUnit.DAYS))),
                new VoucherDef("EXPIRED50", "Voucher đã hết hạn sử dụng", "FIXED", 50000, null, 500000, 50, 0, null, Timestamp.from(now.minus(60, ChronoUnit.DAYS)), Timestamp.from(now.minus(10, ChronoUnit.DAYS))),
                new VoucherDef("SOLDOUT99", "Voucher đã hết lượt sử dụng", "PERCENT", 15, 300000L, 1000000, 10, 10, null, Timestamp.from(now.minus(20, ChronoUnit.DAYS)), Timestamp.from(now.plus(20, ChronoUnit.DAYS)))
        );

        Map<String, Long> map = new HashMap<>();
        for (VoucherDef v : vouchers) {
            Long id = jdbc.queryForObject(
                    """
                    INSERT INTO vouchers
                      (code, name, discount_type, discount_value, max_discount_amount, min_order_value,
                       usage_limit, used_count, per_user_limit, applicable_category_id, start_at, end_at, is_active)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ?, TRUE)
                    ON CONFLICT (code) DO UPDATE SET
                      name = EXCLUDED.name,
                      used_count = EXCLUDED.used_count,
                      applicable_category_id = EXCLUDED.applicable_category_id
                    RETURNING id
                    """,
                    Long.class, v.code(), v.name(), v.type(), v.val(), v.maxDiscount(), v.minOrder(), v.limit(), v.used(), v.catId(), v.start(), v.end()
            );
            map.put(v.code(), id);
        }
        return map;
    }

    private void seedCart(Map<String, Long> userMap, Map<String, VariantInfo> variantMap) {
        Long cartId = jdbc.queryForObject(
                """
                INSERT INTO carts (user_id) VALUES (?)
                ON CONFLICT (user_id) DO UPDATE SET updated_at = NOW()
                RETURNING id
                """,
                Long.class, userMap.get("khach1@gmail.com")
        );
        jdbc.update(
                "INSERT INTO cart_items (cart_id, variant_id, quantity) VALUES (?, ?, ?) ON CONFLICT (cart_id, variant_id) DO UPDATE SET quantity = EXCLUDED.quantity",
                cartId, variantMap.get("RAM-KST-FURY-16G").id(), 2
        );
        jdbc.update(
                "INSERT INTO cart_items (cart_id, variant_id, quantity) VALUES (?, ?, ?) ON CONFLICT (cart_id, variant_id) DO UPDATE SET quantity = EXCLUDED.quantity",
                cartId, variantMap.get("SSD-SAM-990PRO-512").id(), 1
        );
    }

    private record OrderItemDef(String sku, int qty, Long unitPriceOverride) {}
    private record HistoryDef(String from, String to, String byEmail, String note) {}
    private record PaymentDef(String provider, String ref, String status, boolean paid) {}
    private record OrderDef(String code, String userEmail, String voucherCode, String status, String payMethod, String payStatus,
                            String recipientName, String recipientPhone, String address, String note,
                            long voucherDiscount, long shippingFee, String cancelReason,
                            List<OrderItemDef> items, List<HistoryDef> history, PaymentDef payment) {}

    private List<OrderDef> buildOrders() {
        return List.of(
                new OrderDef("ORD-202611-0001", "khach1@gmail.com", "SOOPI10", "COMPLETED", "SANDBOX", "PAID",
                        "Lê Văn Khách Một", "0912345001", "268 Lý Thường Kiệt, Phường 12, Quận 10, TP. Hồ Chí Minh", "Giao giờ hành chính",
                        500000, 0, null,
                        List.of(new OrderItemDef("CPU-I5-14600K", 1, 7590000L), new OrderItemDef("MB-GIG-B760M-DS3H", 1, null)),
                        List.of(
                                new HistoryDef(null, "PENDING", "khach1@gmail.com", "Khách hàng đặt đơn"),
                                new HistoryDef("PENDING", "CONFIRMED", "staff@soopishop.vn", "Xác nhận đơn hàng"),
                                new HistoryDef("CONFIRMED", "PROCESSING", "staff@soopishop.vn", "Đang đóng gói linh kiện"),
                                new HistoryDef("PROCESSING", "SHIPPING", "staff@soopishop.vn", "Đã bàn giao đơn vị vận chuyển"),
                                new HistoryDef("SHIPPING", "COMPLETED", "staff@soopishop.vn", "Khách đã nhận hàng thành công")
                        ),
                        new PaymentDef("VNPAY_SANDBOX", "VNPAY-SB-202611-0001", "SUCCESS", true)),
                new OrderDef("ORD-202611-0002", "khach2@gmail.com", null, "COMPLETED", "COD", "PAID",
                        "Phạm Thị Khách Hai", "0912345002", "144 Xuân Thủy, Phường Dịch Vọng, Quận Cầu Giấy, Hà Nội", "Gọi trước khi giao",
                        0, 0, null,
                        List.of(new OrderItemDef("CPU-R7-7800X3D", 1, null), new OrderItemDef("MB-MSI-B650M-MORTAR", 1, null), new OrderItemDef("PSU-CRS-RM850E", 2, null)),
                        List.of(
                                new HistoryDef(null, "PENDING", "khach2@gmail.com", "Khách đặt đơn COD"),
                                new HistoryDef("PENDING", "CONFIRMED", "staff@soopishop.vn", "Đã xác nhận qua điện thoại"),
                                new HistoryDef("CONFIRMED", "PROCESSING", "staff@soopishop.vn", "Xuất kho & đóng gói"),
                                new HistoryDef("PROCESSING", "SHIPPING", "staff@soopishop.vn", "Đang giao hàng"),
                                new HistoryDef("SHIPPING", "COMPLETED", "staff@soopishop.vn", "Hoàn tất thu hộ COD")
                        ),
                        new PaymentDef("COD", "COD-202611-0002", "SUCCESS", true)),
                new OrderDef("ORD-202611-0003", "khach1@gmail.com", "GIAM200K", "SHIPPING", "MOCK", "PAID",
                        "Lê Văn Khách Một", "0912345001", "123 Lê Lợi, Phường Bến Nghé, Quận 1, TP. Hồ Chí Minh", null,
                        200000, 30000, null,
                        List.of(new OrderItemDef("GPU-ASUS-4070S-12G", 1, null)),
                        List.of(
                                new HistoryDef(null, "PENDING", "khach1@gmail.com", "Đặt đơn thanh toán Mock Gateway"),
                                new HistoryDef("PENDING", "CONFIRMED", "staff@soopishop.vn", "Xác nhận đơn"),
                                new HistoryDef("CONFIRMED", "PROCESSING", "staff@soopishop.vn", "Đóng gói chống sốc GPU"),
                                new HistoryDef("PROCESSING", "SHIPPING", "staff@soopishop.vn", "Shipper đang giao")
                        ),
                        new PaymentDef("MOCK_GATEWAY", "MOCK-GW-202611-0003", "SUCCESS", true)),
                new OrderDef("ORD-202611-0004", "khach3@gmail.com", null, "PROCESSING", "COD", "UNPAID",
                        "Hoàng Anh Khách Ba", "0912345003", "1 Đại Cồ Việt, Phường Bách Khoa, Quận Hai Bà Trưng, Hà Nội", null,
                        0, 30000, null,
                        List.of(new OrderItemDef("GPU-MSI-4060-8G", 1, null)),
                        List.of(
                                new HistoryDef(null, "PENDING", "khach3@gmail.com", "Khách đặt đơn"),
                                new HistoryDef("PENDING", "CONFIRMED", "staff@soopishop.vn", "Xác nhận đơn"),
                                new HistoryDef("CONFIRMED", "PROCESSING", "staff@soopishop.vn", "Đang kiểm tra seal")
                        ),
                        new PaymentDef("COD", "COD-202611-0004", "PENDING", false)),
                new OrderDef("ORD-202611-0005", "khach2@gmail.com", null, "CONFIRMED", "SANDBOX", "PAID",
                        "Phạm Thị Khách Hai", "0912345002", "144 Xuân Thủy, Phường Dịch Vọng, Quận Cầu Giấy, Hà Nội", null,
                        0, 30000, null,
                        List.of(new OrderItemDef("RAM-KST-FURY-16G", 2, null)),
                        List.of(
                                new HistoryDef(null, "PENDING", "khach2@gmail.com", "Đặt mua 2 thanh RAM"),
                                new HistoryDef("PENDING", "CONFIRMED", "staff@soopishop.vn", "Đã nhận thanh toán VNPAY Sandbox")
                        ),
                        new PaymentDef("VNPAY_SANDBOX", "VNPAY-SB-202611-0005", "SUCCESS", true)),
                new OrderDef("ORD-202611-0006", "khach3@gmail.com", null, "PENDING", "COD", "UNPAID",
                        "Hoàng Anh Khách Ba", "0912345003", "1 Đại Cồ Việt, Phường Bách Khoa, Quận Hai Bà Trưng, Hà Nội", "Chờ xác nhận",
                        0, 30000, null,
                        List.of(new OrderItemDef("CASE-CRS-4000D-BLK", 1, null)),
                        List.of(
                                new HistoryDef(null, "PENDING", "khach3@gmail.com", "Mới tạo đơn hàng")
                        ),
                        new PaymentDef("COD", "COD-202611-0006", "PENDING", false)),
                new OrderDef("ORD-202611-0007", "khach1@gmail.com", null, "CANCELLED", "COD", "FAILED",
                        "Lê Văn Khách Một", "0912345001", "268 Lý Thường Kiệt, Phường 12, Quận 10, TP. Hồ Chí Minh", null,
                        0, 0, "Khách hàng đổi ý muốn nâng cấp lên i7-14700K",
                        List.of(new OrderItemDef("CPU-I5-14600K", 1, null)),
                        List.of(
                                new HistoryDef(null, "PENDING", "khach1@gmail.com", "Khách đặt đơn"),
                                new HistoryDef("PENDING", "CANCELLED", "khach1@gmail.com", "Khách hủy đơn trước khi xác nhận")
                        ),
                        new PaymentDef("COD", "COD-202611-0007", "FAILED", false)),
                new OrderDef("ORD-202611-0008", "khach1@gmail.com", "PHUKIEN15", "COMPLETED", "SANDBOX", "PAID",
                        "Lê Văn Khách Một", "0912345001", "268 Lý Thường Kiệt, Phường 12, Quận 10, TP. Hồ Chí Minh", "Đóng gói kỹ bàn phím & chuột",
                        300000, 0, null,
                        List.of(
                                new OrderItemDef("ACC-KEY-K2PRO-RED", 1, null),
                                new OrderItemDef("ACC-LOG-GPX2-BLK", 1, null),
                                new OrderItemDef("ACC-NOC-NTH2-35G", 1, null)
                        ),
                        List.of(
                                new HistoryDef(null, "PENDING", "khach1@gmail.com", "Khách đặt combo phụ kiện gaming"),
                                new HistoryDef("PENDING", "CONFIRMED", "staff@soopishop.vn", "Xác nhận đơn phụ kiện"),
                                new HistoryDef("CONFIRMED", "PROCESSING", "staff@soopishop.vn", "Đóng gói chống sốc"),
                                new HistoryDef("PROCESSING", "SHIPPING", "staff@soopishop.vn", "Đang vận chuyển"),
                                new HistoryDef("SHIPPING", "COMPLETED", "staff@soopishop.vn", "Giao hàng thành công")
                        ),
                        new PaymentDef("VNPAY_SANDBOX", "VNPAY-SB-202611-0008", "SUCCESS", true)),
                new OrderDef("ORD-202611-0009", "khach2@gmail.com", "GPU500K", "COMPLETED", "MOCK", "PAID",
                        "Phạm Thị Khách Hai", "0912345002", "144 Xuân Thủy, Phường Dịch Vọng, Quận Cầu Giấy, Hà Nội", "Kiểm tra nguyên seal trước khi gửi",
                        500000, 0, null,
                        List.of(
                                new OrderItemDef("GPU-GIG-RX7800XT-16G", 1, null),
                                new OrderItemDef("SSD-WD-SN850X-1TB", 1, null),
                                new OrderItemDef("COOL-TR-PA120SE-BLK", 1, null)
                        ),
                        List.of(
                                new HistoryDef(null, "PENDING", "khach2@gmail.com", "Khách đặt bộ nâng cấp GPU + SSD + Tản"),
                                new HistoryDef("PENDING", "CONFIRMED", "staff@soopishop.vn", "Đã thanh toán Mock Gateway"),
                                new HistoryDef("CONFIRMED", "PROCESSING", "staff@soopishop.vn", "Xuất kho & dán tem bảo hành"),
                                new HistoryDef("PROCESSING", "SHIPPING", "staff@soopishop.vn", "Đang giao hàng nhanh"),
                                new HistoryDef("SHIPPING", "COMPLETED", "staff@soopishop.vn", "Khách xác nhận nhận đủ hàng")
                        ),
                        new PaymentDef("MOCK_GATEWAY", "MOCK-GW-202611-0009", "SUCCESS", true)),
                new OrderDef("ORD-202611-0010", "khach3@gmail.com", null, "COMPLETED", "COD", "PAID",
                        "Hoàng Anh Khách Ba", "0912345003", "1 Đại Cồ Việt, Phường Bách Khoa, Quận Hai Bà Trưng, Hà Nội", null,
                        0, 0, null,
                        List.of(
                                new OrderItemDef("RAM-GSK-TZ5NEO-32G", 1, null),
                                new OrderItemDef("SSD-SAM-990PRO-2TB", 1, null),
                                new OrderItemDef("CASE-NZXT-H5F-WHT", 1, null)
                        ),
                        List.of(
                                new HistoryDef(null, "PENDING", "khach3@gmail.com", "Khách đặt bộ RAM + SSD + Case"),
                                new HistoryDef("PENDING", "CONFIRMED", "staff@soopishop.vn", "Xác nhận đơn COD"),
                                new HistoryDef("CONFIRMED", "PROCESSING", "staff@soopishop.vn", "Đóng thùng chống sốc"),
                                new HistoryDef("PROCESSING", "SHIPPING", "staff@soopishop.vn", "Đã giao cho đơn vị vận chuyển"),
                                new HistoryDef("SHIPPING", "COMPLETED", "staff@soopishop.vn", "Thu hộ COD thành công")
                        ),
                        new PaymentDef("COD", "COD-202611-0010", "SUCCESS", true))
        );
    }

    private Map<String, Long> seedOrders(
            Map<String, Long> userMap,
            Map<String, Long> voucherMap,
            Map<String, VariantInfo> variantMap,
            List<OrderDef> orders
    ) {
        Map<String, Long> orderMap = new HashMap<>();
        for (OrderDef o : orders) {
            long subtotal = 0;
            for (OrderItemDef item : o.items()) {
                VariantInfo v = variantMap.get(item.sku());
                if (v == null) {
                    throw new IllegalStateException("Unknown SKU in order " + o.code() + ": " + item.sku());
                }
                long unitPrice = item.unitPriceOverride() != null ? item.unitPriceOverride() : v.salePrice();
                subtotal += unitPrice * item.qty();
            }
            long finalTotal = Math.max(0, subtotal - o.voucherDiscount() + o.shippingFee());

            Long orderId = jdbc.queryForObject(
                    """
                    INSERT INTO orders
                      (order_code, user_id, voucher_id, status, payment_method, payment_status,
                       recipient_name, recipient_phone, shipping_address, note,
                       subtotal_amount, voucher_discount, shipping_fee, final_total, cancel_reason)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT (order_code) DO UPDATE SET
                      status = EXCLUDED.status,
                      payment_status = EXCLUDED.payment_status,
                      updated_at = NOW()
                    RETURNING id
                    """,
                    Long.class,
                    o.code(), userMap.get(o.userEmail()), o.voucherCode() != null ? voucherMap.get(o.voucherCode()) : null,
                    o.status(), o.payMethod(), o.payStatus(), o.recipientName(), o.recipientPhone(), o.address(), o.note(),
                    subtotal, o.voucherDiscount(), o.shippingFee(), finalTotal, o.cancelReason()
            );
            orderMap.put(o.code(), orderId);

            for (OrderItemDef item : o.items()) {
                VariantInfo v = variantMap.get(item.sku());
                long unitPrice = item.unitPriceOverride() != null ? item.unitPriceOverride() : v.salePrice();
                long lineSubtotal = unitPrice * item.qty();
                Integer exists = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM order_items WHERE order_id = ? AND sku_snapshot = ?",
                        Integer.class, orderId, item.sku()
                );
                if (exists == null || exists == 0) {
                    jdbc.update(
                            """
                            INSERT INTO order_items
                              (order_id, product_id, variant_id, product_name_snapshot, variant_name_snapshot,
                               sku_snapshot, list_price_snapshot, unit_price_snapshot, quantity, line_subtotal)
                            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                            """,
                            orderId, v.productId(), v.id(), v.productName(), v.variantName(),
                            item.sku(), v.listPrice(), unitPrice, item.qty(), lineSubtotal
                    );
                }
            }

            Integer histCount = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM order_status_history WHERE order_id = ?",
                    Integer.class, orderId
            );
            if (histCount == null || histCount == 0) {
                for (HistoryDef h : o.history()) {
                    jdbc.update(
                            "INSERT INTO order_status_history (order_id, from_status, to_status, changed_by, note) VALUES (?, ?, ?, ?, ?)",
                            orderId, h.from(), h.to(), userMap.get(h.byEmail()), h.note()
                    );
                }
            }

            String payload = "COD".equals(o.payment().provider())
                    ? null
                    : String.format("{\"orderCode\":\"%s\",\"ref\":\"%s\",\"status\":\"%s\"}", o.code(), o.payment().ref(), o.payment().status());
            jdbc.update(
                    """
                    INSERT INTO payments (order_id, provider, transaction_ref, amount, status, raw_callback_payload, paid_at)
                    VALUES (?, ?, ?, ?, ?, ?::jsonb, ?)
                    ON CONFLICT (transaction_ref) DO NOTHING
                    """,
                    orderId, o.payment().provider(), o.payment().ref(), finalTotal, o.payment().status(),
                    payload, o.payment().paid() ? Timestamp.from(Instant.now()) : null
            );
        }
        return orderMap;
    }

    /**
     * Per-SKU and per-order idempotent ledger seeding.
     * Safe to run on an empty DB, repeatedly (--seed x2), or on top of an older seed snapshot.
     */
    private void seedInventoryLedger(
            Map<String, Long> userMap,
            Map<String, VariantInfo> variantMap,
            Map<String, Integer> initialStockMap,
            Map<String, Long> orderMap,
            List<OrderDef> orders
    ) {
        Long adminId = userMap.get("admin@soopishop.vn");
        Long staffId = userMap.get("staff@soopishop.vn");

        Map<Long, Integer> runningByVariantId = new HashMap<>();
        jdbc.query(
                "SELECT variant_id, COALESCE(SUM(quantity_change), 0)::int AS total FROM inventory_transactions GROUP BY variant_id",
                rs -> {
                    runningByVariantId.put(rs.getLong("variant_id"), rs.getInt("total"));
                }
        );

        for (Map.Entry<String, Integer> entry : initialStockMap.entrySet()) {
            String sku = entry.getKey();
            int initialStock = entry.getValue();
            long variantId = variantMap.get(sku).id();

            if (initialStock > 0) {
                Integer stockInExists = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM inventory_transactions WHERE variant_id = ? AND transaction_type = 'STOCK_IN' AND order_id IS NULL",
                        Integer.class, variantId
                );
                if (stockInExists == null || stockInExists == 0) {
                    recordTx(runningByVariantId, variantId, null, "STOCK_IN", initialStock,
                            "Nhập kho ban đầu SKU " + sku, adminId);
                }
            } else {
                runningByVariantId.putIfAbsent(variantId, 0);
            }
        }

        for (OrderDef o : orders) {
            Long orderId = orderMap.get(o.code());
            for (OrderItemDef item : o.items()) {
                long variantId = variantMap.get(item.sku()).id();
                Integer deductExists = jdbc.queryForObject(
                        "SELECT COUNT(*) FROM inventory_transactions WHERE variant_id = ? AND order_id = ? AND transaction_type = 'ORDER_DEDUCT'",
                        Integer.class, variantId, orderId
                );
                if (deductExists == null || deductExists == 0) {
                    recordTx(runningByVariantId, variantId, orderId, "ORDER_DEDUCT", -item.qty(),
                            "Trừ kho cho đơn hàng " + o.code(), staffId);
                }

                if ("CANCELLED".equals(o.status())) {
                    Integer restockExists = jdbc.queryForObject(
                            "SELECT COUNT(*) FROM inventory_transactions WHERE variant_id = ? AND order_id = ? AND transaction_type = 'ORDER_CANCEL_RESTOCK'",
                            Integer.class, variantId, orderId
                    );
                    if (restockExists == null || restockExists == 0) {
                        recordTx(runningByVariantId, variantId, orderId, "ORDER_CANCEL_RESTOCK", item.qty(),
                                "Hoàn kho do hủy đơn hàng " + o.code(), staffId);
                    }
                }
            }
        }

        long adjustVariantId = variantMap.get("GPU-ASUS-4070S-12G").id();
        Integer adjustExists = jdbc.queryForObject(
                "SELECT COUNT(*) FROM inventory_transactions WHERE variant_id = ? AND transaction_type = 'ADJUSTMENT' AND order_id IS NULL",
                Integer.class, adjustVariantId
        );
        if (adjustExists == null || adjustExists == 0) {
            recordTx(runningByVariantId, adjustVariantId, null, "ADJUSTMENT", -1,
                    "Điều chỉnh kiểm kê: 1 sản phẩm lỗi hộp chuyển bảo hành", adminId);
        }

        for (VariantInfo vi : variantMap.values()) {
            int exactLedgerBalance = runningByVariantId.getOrDefault(vi.id(), 0);
            jdbc.update(
                    "UPDATE product_variants SET stock_quantity = ?, updated_at = NOW() WHERE id = ?",
                    exactLedgerBalance, vi.id()
            );
        }
    }

    private void recordTx(
            Map<Long, Integer> runningByVariantId,
            long variantId,
            Long orderId,
            String type,
            int change,
            String note,
            Long byId
    ) {
        int before = runningByVariantId.getOrDefault(variantId, 0);
        int after = before + change;
        if (after < 0) {
            throw new IllegalStateException("Negative stock calculation for variant_id=" + variantId);
        }
        runningByVariantId.put(variantId, after);
        jdbc.update(
                """
                INSERT INTO inventory_transactions
                  (variant_id, order_id, transaction_type, quantity_before, quantity_change, quantity_after, note, performed_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                variantId, orderId, type, before, change, after, note, byId
        );
    }

    private void seedReviews(Map<String, Long> userMap, Map<String, Long> productMap, Map<String, Long> orderMap) {
        record ReviewDef(String email, String productSlug, String orderCode, int rating, String comment, boolean visible) {}
        List<ReviewDef> reviews = List.of(
                new ReviewDef("khach1@gmail.com", "intel-core-i5-14600k", "ORD-202611-0001", 5, "CPU chạy cực mát và mạnh, giao hàng nhanh chóng trong ngày.", true),
                new ReviewDef("khach1@gmail.com", "gigabyte-b760m-ds3h-ddr4", "ORD-202611-0001", 4, "Mainboard giá tốt trong tầm giá, đủ cổng kết nối M.2 và LAN 2.5G.", true),
                new ReviewDef("khach2@gmail.com", "amd-ryzen-7-7800x3d", "ORD-202611-0002", 5, "FPS game tăng rõ rệt nhờ 3D V-Cache, rất đáng tiền cho dàn PC gaming.", true),
                new ReviewDef("khach2@gmail.com", "msi-mag-b650m-mortar-wifi", "ORD-202611-0002", 1, "Spam link quảng cáo không hợp lệ - đã ẩn bởi admin.", false),
                new ReviewDef("khach2@gmail.com", "corsair-rm850e-80-plus-gold", "ORD-202611-0002", 5, "Nguồn chạy êm ru, có sẵn dây 12VHPWR gọn gàng.", true),
                new ReviewDef("khach1@gmail.com", "keychron-k2-pro-qmk-via", "ORD-202611-0008", 5, "Gõ đầm tay, kết nối Bluetooth chuyển qua lại giữa Mac và PC mượt.", true),
                new ReviewDef("khach1@gmail.com", "logitech-g-pro-x-superlight-2", "ORD-202611-0008", 5, "Chuột siêu nhẹ 60g cầm rất sướng, pin trâu dùng cả tuần.", true),
                new ReviewDef("khach2@gmail.com", "gigabyte-rx-7800-xt-gaming-oc-16g", "ORD-202611-0009", 5, "Chiến game 2K Ultra rất mượt, 16GB VRAM dư dả và nhiệt độ dưới 65 độ.", true),
                new ReviewDef("khach2@gmail.com", "wd-black-sn850x-nvme", "ORD-202611-0009", 5, "Tốc độ load game và Windows cực nhanh, đúng thông số hãng.", true),
                new ReviewDef("khach2@gmail.com", "thermalright-peerless-assassin-120-se", "ORD-202611-0009", 4, "Hiệu năng tản nhiệt vượt xa tầm giá dưới 1 triệu, lắp đặt dễ.", true),
                new ReviewDef("khach3@gmail.com", "gskill-trident-z5-neo-rgb-ddr5-6000", "ORD-202611-0010", 5, "Bật EXPO 6000MHz CL30 ăn ngay lần đầu, đèn RGB rất đẹp.", true),
                new ReviewDef("khach3@gmail.com", "nzxt-h5-flow-rgb", "ORD-202611-0010", 5, "Case màu trắng hoàn thiện sắc nét, thoáng khí tốt và dễ đi dây.", true)
        );
        for (ReviewDef r : reviews) {
            jdbc.update(
                    """
                    INSERT INTO reviews (user_id, product_id, order_id, rating, comment, is_verified_purchase, is_visible)
                    VALUES (?, ?, ?, ?, ?, TRUE, ?)
                    ON CONFLICT (user_id, product_id, order_id) DO UPDATE SET
                      rating = EXCLUDED.rating,
                      comment = EXCLUDED.comment,
                      is_visible = EXCLUDED.is_visible,
                      updated_at = NOW()
                    """,
                    userMap.get(r.email()), productMap.get(r.productSlug()), orderMap.get(r.orderCode()), r.rating(), r.comment(), r.visible()
            );
        }

        jdbc.update(
                """
                UPDATE products p
                SET
                  review_count = COALESCE(agg.cnt, 0),
                  avg_rating = COALESCE(agg.avg_r, 0),
                  updated_at = NOW()
                FROM (
                  SELECT p2.id AS product_id,
                         COUNT(r.id)::int AS cnt,
                         COALESCE(ROUND(AVG(r.rating)::numeric, 2), 0) AS avg_r
                  FROM products p2
                  LEFT JOIN reviews r ON r.product_id = p2.id AND r.is_visible = TRUE
                  GROUP BY p2.id
                ) agg
                WHERE p.id = agg.product_id
                """
        );
    }
}
