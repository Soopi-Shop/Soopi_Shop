package vn.soopishop.seed;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

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

        System.out.println("Seed completed successfully (Clerk auth mapping enabled).");
    }

    private Map<String, Long> seedUsers() {
        record UserDef(String clerkUserId, String name, String email, String phone, String role, String status) {}
        List<UserDef> users = List.of(
                new UserDef("user_seed_admin_01", "Nguyen Hoang Admin", "admin@soopishop.vn", "0901000001", "ADMIN", "ACTIVE"),
                new UserDef("user_seed_staff_01", "Tran Minh Staff", "staff@soopishop.vn", "0901000002", "STAFF", "ACTIVE"),
                new UserDef("user_seed_khach1_01", "Le Van Khach Mot", "khach1@gmail.com", "0912345001", "CUSTOMER", "ACTIVE"),
                new UserDef("user_seed_khach2_01", "Pham Thi Khach Hai", "khach2@gmail.com", "0912345002", "CUSTOMER", "ACTIVE"),
                new UserDef("user_seed_khach3_01", "Hoang Anh Khach Ba", "khach3@gmail.com", "0912345003", "CUSTOMER", "ACTIVE"),
                new UserDef("user_seed_locked_01", "Tai Khoan Bi Khoa", "locked@gmail.com", "0912345999", "CUSTOMER", "LOCKED")
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
        record AddrDef(String email, String name, String phone, String province, String district, String ward, String street, boolean isDefault) {}
        List<AddrDef> addrs = List.of(
                new AddrDef("khach1@gmail.com", "Le Van Khach Mot", "0912345001", "TP. Ho Chi Minh", "Quan 10", "Phuong 12", "268 Ly Thuong Kiet", true),
                new AddrDef("khach1@gmail.com", "Le Van Khach Mot (Cong ty)", "0912345001", "TP. Ho Chi Minh", "Quan 1", "Phuong Ben Nghe", "123 Le Loi", false),
                new AddrDef("khach2@gmail.com", "Pham Thi Khach Hai", "0912345002", "Ha Noi", "Quan Cau Giay", "Phuong Dich Vong", "144 Xuan Thuy", true),
                new AddrDef("khach3@gmail.com", "Hoang Anh Khach Ba", "0912345003", "Ha Noi", "Quan Hai Ba Trung", "Phuong Bach Khoa", "1 Dai Co Viet", true)
        );
        for (AddrDef a : addrs) {
            Long uid = userMap.get(a.email());
            Integer exists = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM user_addresses WHERE user_id = ? AND street_address = ?",
                    Integer.class, uid, a.street()
            );
            if (exists == null || exists == 0) {
                jdbc.update(
                        """
                        INSERT INTO user_addresses
                          (user_id, recipient_name, recipient_phone, province, district, ward, street_address, is_default)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                        uid, a.name(), a.phone(), a.province(), a.district(), a.ward(), a.street(), a.isDefault()
                );
            }
        }
    }

    private Map<String, Long> seedCategories() {
        record CatDef(String slug, String name, String desc) {}
        List<CatDef> cats = List.of(
                new CatDef("cpu", "CPU", "Vi xu ly Intel & AMD chinh hang"),
                new CatDef("gpu", "GPU (Card man hinh)", "Card do hoa NVIDIA GeForce & AMD Radeon"),
                new CatDef("mainboard", "Mainboard", "Bo mach chu Intel & AMD"),
                new CatDef("ram", "RAM", "Bo nho trong DDR4 / DDR5"),
                new CatDef("ssd-hdd", "SSD/HDD", "O cung SSD NVMe & HDD"),
                new CatDef("psu", "PSU (Nguon)", "Nguon may tinh chuan 80 Plus"),
                new CatDef("case", "Case (Vo may)", "Vo case PC gaming & workstation"),
                new CatDef("tan-nhiet", "Tan nhiet", "Tan nhiet khi & tan nhiet nuoc AIO")
        );
        Map<String, Long> map = new HashMap<>();
        for (CatDef c : cats) {
            Long id = jdbc.queryForObject(
                    """
                    INSERT INTO categories (name, slug, description, is_active)
                    VALUES (?, ?, ?, TRUE)
                    ON CONFLICT (slug) DO UPDATE SET name = EXCLUDED.name, description = EXCLUDED.description
                    RETURNING id
                    """,
                    Long.class, c.name(), c.slug(), c.desc()
            );
            map.put(c.slug(), id);
        }
        return map;
    }

    private Map<String, Long> seedBrands() {
        record BrandDef(String slug, String name, String logo) {}
        List<BrandDef> brands = List.of(
                new BrandDef("intel", "Intel", "https://placehold.co/200x80?text=Intel"),
                new BrandDef("amd", "AMD", "https://placehold.co/200x80?text=AMD"),
                new BrandDef("asus", "ASUS", "https://placehold.co/200x80?text=ASUS"),
                new BrandDef("msi", "MSI", "https://placehold.co/200x80?text=MSI"),
                new BrandDef("gigabyte", "Gigabyte", "https://placehold.co/200x80?text=Gigabyte"),
                new BrandDef("kingston", "Kingston", "https://placehold.co/200x80?text=Kingston"),
                new BrandDef("corsair", "Corsair", "https://placehold.co/200x80?text=Corsair"),
                new BrandDef("samsung", "Samsung", "https://placehold.co/200x80?text=Samsung")
        );
        Map<String, Long> map = new HashMap<>();
        for (BrandDef b : brands) {
            Long id = jdbc.queryForObject(
                    """
                    INSERT INTO brands (name, slug, logo_url, is_active)
                    VALUES (?, ?, ?, TRUE)
                    ON CONFLICT (slug) DO UPDATE SET name = EXCLUDED.name, logo_url = EXCLUDED.logo_url
                    RETURNING id
                    """,
                    Long.class, b.name(), b.slug(), b.logo()
            );
            map.put(b.slug(), id);
        }
        return map;
    }

    private Map<String, Long> seedCategorySpecAttributes(Map<String, Long> categoryMap) {
        record SpecAttrDef(String cat, String code, String name, String unit, String type, int order) {}
        List<SpecAttrDef> attrs = List.of(
                new SpecAttrDef("cpu", "socket", "Socket", null, "TEXT", 1),
                new SpecAttrDef("cpu", "cores", "So nhan", "nhan", "NUMBER", 2),
                new SpecAttrDef("cpu", "threads", "So luong", "luong", "NUMBER", 3),
                new SpecAttrDef("cpu", "base_clock", "Xung nhip co ban", "GHz", "NUMBER", 4),
                new SpecAttrDef("cpu", "tdp", "TDP", "W", "NUMBER", 5),
                new SpecAttrDef("gpu", "vram", "VRAM", "GB", "NUMBER", 1),
                new SpecAttrDef("gpu", "interface", "Chuan giao tiep", null, "TEXT", 2),
                new SpecAttrDef("gpu", "recommended_psu", "Nguon khuyen nghi", "W", "NUMBER", 3),
                new SpecAttrDef("mainboard", "socket", "Socket", null, "TEXT", 1),
                new SpecAttrDef("mainboard", "chipset", "Chipset", null, "TEXT", 2),
                new SpecAttrDef("mainboard", "form_factor", "Kich thuoc", null, "TEXT", 3),
                new SpecAttrDef("mainboard", "ram_type", "Ho tro RAM", null, "TEXT", 4),
                new SpecAttrDef("ram", "ram_type", "Chuan RAM", null, "TEXT", 1),
                new SpecAttrDef("ram", "capacity", "Dung luong", "GB", "NUMBER", 2),
                new SpecAttrDef("ram", "bus_speed", "Bus", "MHz", "NUMBER", 3),
                new SpecAttrDef("ssd-hdd", "capacity", "Dung luong", "GB", "NUMBER", 1),
                new SpecAttrDef("ssd-hdd", "interface", "Chuan giao tiep", null, "TEXT", 2),
                new SpecAttrDef("ssd-hdd", "read_speed", "Toc do doc", "MB/s", "NUMBER", 3),
                new SpecAttrDef("psu", "wattage", "Cong suat", "W", "NUMBER", 1),
                new SpecAttrDef("psu", "efficiency", "Chuan hieu suat", null, "TEXT", 2),
                new SpecAttrDef("psu", "modular", "Loai day", null, "TEXT", 3),
                new SpecAttrDef("case", "form_factor", "Ho tro Mainboard", null, "TEXT", 1),
                new SpecAttrDef("case", "max_gpu_length", "Chieu dai GPU toi da", "mm", "NUMBER", 2),
                new SpecAttrDef("case", "color", "Mau sac", null, "TEXT", 3),
                new SpecAttrDef("tan-nhiet", "cooler_type", "Loai tan nhiet", null, "TEXT", 1),
                new SpecAttrDef("tan-nhiet", "socket_support", "Ho tro Socket", null, "TEXT", 2),
                new SpecAttrDef("tan-nhiet", "tdp_rating", "Cong suat giai nhiet", "W", "NUMBER", 3)
        );
        Map<String, Long> map = new HashMap<>();
        for (SpecAttrDef sa : attrs) {
            Long id = jdbc.queryForObject(
                    """
                    INSERT INTO category_spec_attributes
                      (category_id, name, code, unit, data_type, is_filterable, display_order)
                    VALUES (?, ?, ?, ?, ?, TRUE, ?)
                    ON CONFLICT (category_id, code) DO UPDATE SET
                      name = EXCLUDED.name, unit = EXCLUDED.unit, data_type = EXCLUDED.data_type, display_order = EXCLUDED.display_order
                    RETURNING id
                    """,
                    Long.class, categoryMap.get(sa.cat()), sa.name(), sa.code(), sa.unit(), sa.type(), sa.order()
            );
            map.put(sa.cat() + ":" + sa.code(), id);
        }
        return map;
    }

    private record SpecVal(String code, String text, Double num) {}
    private record VariantDef(String sku, String name, long listPrice, long salePrice, int initialStock, List<SpecVal> specs) {}
    private record ProductDef(String slug, String cat, String brand, String name, String desc, int warranty, String condition, String status, List<SpecVal> specs, List<VariantDef> variants) {}
    public record VariantInfo(long id, long productId, String productName, String variantName, long listPrice, long salePrice) {}

    private void seedCatalog(
            Map<String, Long> categoryMap,
            Map<String, Long> brandMap,
            Map<String, Long> specAttrMap,
            Map<String, Long> productMap,
            Map<String, VariantInfo> variantMap,
            Map<String, Integer> initialStockMap
    ) {
        List<ProductDef> products = List.of(
                new ProductDef("intel-core-i5-14600k", "cpu", "intel", "CPU Intel Core i5-14600K",
                        "14 nhan 20 luong, xung boost toi da 5.3GHz, ho tro DDR4/DDR5.", 36, "NEW_FULLBOX", "ACTIVE",
                        List.of(new SpecVal("socket", "LGA1700", null), new SpecVal("cores", "14 nhan", 14.0), new SpecVal("threads", "20 luong", 20.0), new SpecVal("base_clock", "3.5 GHz", 3.5), new SpecVal("tdp", "125 W", 125.0)),
                        List.of(new VariantDef("CPU-I5-14600K", "Box Chinh Hang", 8490000, 7990000, 25, List.of()))),
                new ProductDef("amd-ryzen-7-7800x3d", "cpu", "amd", "CPU AMD Ryzen 7 7800X3D",
                        "8 nhan 16 luong, cong nghe 3D V-Cache toi uu cho gaming.", 36, "NEW_FULLBOX", "ACTIVE",
                        List.of(new SpecVal("socket", "AM5", null), new SpecVal("cores", "8 nhan", 8.0), new SpecVal("threads", "16 luong", 16.0), new SpecVal("base_clock", "4.2 GHz", 4.2), new SpecVal("tdp", "120 W", 120.0)),
                        List.of(new VariantDef("CPU-R7-7800X3D", "Box Chinh Hang", 10990000, 9990000, 15, List.of()))),
                new ProductDef("asus-dual-rtx-4070-super-12g", "gpu", "asus", "Card man hinh ASUS Dual GeForce RTX 4070 SUPER 12GB",
                        "Kien truc Ada Lovelace, DLSS 3.5, 2 quat Axial-tech.", 36, "NEW_FULLBOX", "ACTIVE",
                        List.of(new SpecVal("vram", "12 GB GDDR6X", 12.0), new SpecVal("interface", "PCIe 4.0 x16", null), new SpecVal("recommended_psu", "650 W", 650.0)),
                        List.of(new VariantDef("GPU-ASUS-4070S-12G", "OC Edition 12GB", 18490000, 17490000, 5, List.of()))),
                new ProductDef("msi-rtx-4060-ventus-2x-8g", "gpu", "msi", "Card man hinh MSI GeForce RTX 4060 Ventus 2X Black 8G OC",
                        "Card do hoa tam trung hieu nang cao, tiet kiem dien.", 36, "NEW_FULLBOX", "ACTIVE",
                        List.of(new SpecVal("vram", "8 GB GDDR6", 8.0), new SpecVal("interface", "PCIe 4.0 x8", null), new SpecVal("recommended_psu", "550 W", 550.0)),
                        List.of(new VariantDef("GPU-MSI-4060-8G", "Ventus 2X 8GB", 8990000, 8490000, 20, List.of()))),
                new ProductDef("gigabyte-b760m-ds3h-ddr4", "mainboard", "gigabyte", "Mainboard Gigabyte B760M DS3H DDR4",
                        "Ho tro CPU Intel Gen 12/13/14, 2 khe M.2 PCIe 4.0.", 36, "NEW_FULLBOX", "ACTIVE",
                        List.of(new SpecVal("socket", "LGA1700", null), new SpecVal("chipset", "B760", null), new SpecVal("form_factor", "Micro-ATX", null), new SpecVal("ram_type", "DDR4", null)),
                        List.of(new VariantDef("MB-GIG-B760M-DS3H", "B760M DS3H DDR4", 3290000, 2990000, 18, List.of()))),
                new ProductDef("msi-mag-b650m-mortar-wifi", "mainboard", "msi", "Mainboard MSI MAG B650M MORTAR WIFI",
                        "Bo mach chu AM5 cao cap, tich hop Wi-Fi 6E va Bluetooth 5.3.", 36, "NEW_FULLBOX", "ACTIVE",
                        List.of(new SpecVal("socket", "AM5", null), new SpecVal("chipset", "B650", null), new SpecVal("form_factor", "Micro-ATX", null), new SpecVal("ram_type", "DDR5", null)),
                        List.of(new VariantDef("MB-MSI-B650M-MORTAR", "B650M Mortar WiFi", 5690000, 5290000, 16, List.of()))),
                new ProductDef("kingston-fury-beast-ddr5-6000", "ram", "kingston", "RAM Kingston Fury Beast DDR5 6000MHz",
                        "Ho tro Intel XMP 3.0 va AMD EXPO, tan nhiet nhom.", 36, "NEW_FULLBOX", "ACTIVE",
                        List.of(new SpecVal("ram_type", "DDR5", null), new SpecVal("bus_speed", "6000 MHz", 6000.0)),
                        List.of(
                                new VariantDef("RAM-KST-FURY-16G", "16GB (1x16GB) DDR5 6000MHz", 1690000, 1490000, 30, List.of(new SpecVal("capacity", "16 GB", 16.0))),
                                new VariantDef("RAM-KST-FURY-32G", "32GB (2x16GB) DDR5 6000MHz", 3290000, 2950000, 2, List.of(new SpecVal("capacity", "32 GB", 32.0)))
                        )),
                new ProductDef("corsair-vengeance-rgb-ddr5-5600", "ram", "corsair", "RAM Corsair Vengeance RGB DDR5 5600MHz",
                        "Den LED RGB 10 vung doc lap qua phan mem iCUE.", 36, "NEW_FULLBOX", "ACTIVE",
                        List.of(new SpecVal("ram_type", "DDR5", null), new SpecVal("capacity", "32 GB", 32.0), new SpecVal("bus_speed", "5600 MHz", 5600.0)),
                        List.of(new VariantDef("RAM-CRS-VENG-32G", "32GB (2x16GB) Black", 3490000, 3190000, 18, List.of()))),
                new ProductDef("samsung-990-pro-nvme-m2", "ssd-hdd", "samsung", "SSD Samsung 990 PRO PCIe 4.0 NVMe M.2",
                        "SSD PCIe 4.0 hieu nang dinh cao, toc do doc len den 7450MB/s.", 60, "NEW_FULLBOX", "ACTIVE",
                        List.of(new SpecVal("interface", "M.2 NVMe PCIe 4.0", null), new SpecVal("read_speed", "7450 MB/s", 7450.0)),
                        List.of(
                                new VariantDef("SSD-SAM-990PRO-512", "512GB NVMe", 2190000, 1890000, 22, List.of(new SpecVal("capacity", "512 GB", 512.0))),
                                new VariantDef("SSD-SAM-990PRO-1TB", "1TB NVMe", 3490000, 3150000, 1, List.of(new SpecVal("capacity", "1000 GB", 1000.0)))
                        )),
                new ProductDef("kingston-nv2-pcie-4-nvme", "ssd-hdd", "kingston", "SSD Kingston NV2 PCIe 4.0 NVMe M.2 1TB",
                        "O cung SSD NVMe gia tot cho PC va laptop.", 36, "NEW_FULLBOX", "ACTIVE",
                        List.of(new SpecVal("capacity", "1000 GB", 1000.0), new SpecVal("interface", "M.2 NVMe PCIe 4.0", null), new SpecVal("read_speed", "3500 MB/s", 3500.0)),
                        List.of(new VariantDef("SSD-KST-NV2-1TB", "1TB NVMe", 1790000, 1590000, 25, List.of()))),
                new ProductDef("corsair-rm850e-80-plus-gold", "psu", "corsair", "Nguon Corsair RM850e 850W 80 Plus Gold Full Modular",
                        "Chuan ATX 3.0 & PCIe 5.0 12VHPWR cho RTX 4000 series.", 84, "NEW_FULLBOX", "OUT_OF_STOCK",
                        List.of(new SpecVal("wattage", "850 W", 850.0), new SpecVal("efficiency", "80 Plus Gold", null), new SpecVal("modular", "Full Modular", null)),
                        List.of(new VariantDef("PSU-CRS-RM850E", "850W ATX 3.0", 3590000, 3290000, 2, List.of()))),
                new ProductDef("msi-mag-a650bn-650w", "psu", "msi", "Nguon MSI MAG A650BN 650W 80 Plus Bronze",
                        "Nguon may tinh ben bi, day boc luoi gon gang.", 60, "NEW_FULLBOX", "ACTIVE",
                        List.of(new SpecVal("wattage", "650 W", 650.0), new SpecVal("efficiency", "80 Plus Bronze", null), new SpecVal("modular", "Non-Modular", null)),
                        List.of(new VariantDef("PSU-MSI-A650BN", "650W Bronze", 1490000, 1290000, 20, List.of()))),
                new ProductDef("corsair-4000d-airflow", "case", "corsair", "Vo Case Corsair 4000D Airflow Tempered Glass",
                        "Mat luoi thoang khi toi uu luong gio, kinh cuong luc.", 24, "NEW_FULLBOX", "ACTIVE",
                        List.of(new SpecVal("form_factor", "ATX / Micro-ATX / Mini-ITX", null), new SpecVal("max_gpu_length", "360 mm", 360.0)),
                        List.of(
                                new VariantDef("CASE-CRS-4000D-BLK", "Den (Black)", 2390000, 2190000, 16, List.of(new SpecVal("color", "Den", null))),
                                new VariantDef("CASE-CRS-4000D-WHT", "Trang (White)", 2490000, 2290000, 4, List.of(new SpecVal("color", "Trang", null)))
                        )),
                new ProductDef("msi-mag-forge-112r", "case", "msi", "Vo Case MSI MAG FORGE 112R ARGB",
                        "San 4 quat ARGB, ho tro rad 240mm.", 24, "NEW_FULLBOX", "ACTIVE",
                        List.of(new SpecVal("form_factor", "ATX / Micro-ATX", null), new SpecVal("max_gpu_length", "330 mm", 330.0), new SpecVal("color", "Den", null)),
                        List.of(new VariantDef("CASE-MSI-112R", "Den 4 Fan ARGB", 1650000, 1450000, 19, List.of()))),
                new ProductDef("corsair-icue-h100i-rgb-elite", "tan-nhiet", "corsair", "Tan nhiet nuoc AIO Corsair iCUE H100i RGB ELITE 240mm",
                        "AIO 240mm hieu nang cao, bom em ai.", 60, "NEW_FULLBOX", "ACTIVE",
                        List.of(new SpecVal("cooler_type", "AIO Liquid 240mm", null), new SpecVal("socket_support", "LGA1700 / AM5 / AM4", null), new SpecVal("tdp_rating", "250 W", 250.0)),
                        List.of(new VariantDef("COOL-CRS-H100I", "240mm Black", 3690000, 3390000, 15, List.of()))),
                new ProductDef("asus-rog-ryuo-iii-360-argb", "tan-nhiet", "asus", "Tan nhiet nuoc AIO ASUS ROG RYUO III 360 ARGB",
                        "Man hinh Anime Matrix doc dao, rad 360mm cao cap.", 72, "NEW_FULLBOX", "ACTIVE",
                        List.of(new SpecVal("cooler_type", "AIO Liquid 360mm", null), new SpecVal("socket_support", "LGA1700 / AM5", null), new SpecVal("tdp_rating", "320 W", 320.0)),
                        List.of(new VariantDef("COOL-ASUS-RYUO360", "360mm Black", 7490000, 6990000, 3, List.of())))
        );

        for (ProductDef p : products) {
            Long productId = jdbc.queryForObject(
                    """
                    INSERT INTO products (category_id, brand_id, name, slug, description, warranty_months, condition_type, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT (slug) DO UPDATE SET
                      name = EXCLUDED.name, description = EXCLUDED.description, status = EXCLUDED.status
                    RETURNING id
                    """,
                    Long.class,
                    categoryMap.get(p.cat()), brandMap.get(p.brand()), p.name(), p.slug(), p.desc(), p.warranty(), p.condition(), p.status()
            );
            productMap.put(p.slug(), productId);

            Integer imgExists = jdbc.queryForObject(
                    "SELECT COUNT(*) FROM product_images WHERE product_id = ? AND variant_id IS NULL AND is_primary = TRUE",
                    Integer.class, productId
            );
            if (imgExists == null || imgExists == 0) {
                jdbc.update(
                        "INSERT INTO product_images (product_id, variant_id, image_url, is_primary, sort_order) VALUES (?, NULL, ?, TRUE, 0)",
                        productId, "https://placehold.co/600x600?text=" + p.slug()
                );
            }

            for (SpecVal s : p.specs()) {
                Long attrId = specAttrMap.get(p.cat() + ":" + s.code());
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
                          variant_name = EXCLUDED.variant_name, list_price = EXCLUDED.list_price, sale_price = EXCLUDED.sale_price
                        RETURNING id
                        """,
                        Long.class, productId, v.sku(), v.name(), v.listPrice(), v.salePrice(), v.initialStock()
                );
                variantMap.put(v.sku(), new VariantInfo(variantId, productId, p.name(), v.name(), v.listPrice(), v.salePrice()));
                initialStockMap.put(v.sku(), v.initialStock());

                for (SpecVal vs : v.specs()) {
                    Long attrId = specAttrMap.get(p.cat() + ":" + vs.code());
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

    private Map<String, Long> seedVouchers(Map<String, Long> categoryMap) {
        Instant now = Instant.now();
        record VoucherDef(String code, String name, String type, long val, Long maxDiscount, long minOrder, int limit, int used, Long catId, Timestamp start, Timestamp end) {}
        List<VoucherDef> vouchers = List.of(
                new VoucherDef("SOOPI10", "Giam 10% toi da 500K cho don tu 2 trieu", "PERCENT", 10, 500000L, 2000000, 100, 1, null, Timestamp.from(now.minus(30, ChronoUnit.DAYS)), Timestamp.from(now.plus(60, ChronoUnit.DAYS))),
                new VoucherDef("GIAM200K", "Giam truc tiep 200K cho don tu 1.5 trieu", "FIXED", 200000, null, 1500000, 50, 1, null, Timestamp.from(now.minus(15, ChronoUnit.DAYS)), Timestamp.from(now.plus(45, ChronoUnit.DAYS))),
                new VoucherDef("RAMONLY", "Giam 150K chi ap dung danh muc RAM", "FIXED", 150000, null, 1000000, 30, 0, categoryMap.get("ram"), Timestamp.from(now.minus(10, ChronoUnit.DAYS)), Timestamp.from(now.plus(30, ChronoUnit.DAYS))),
                new VoucherDef("EXPIRED50", "Voucher da het han su dung", "FIXED", 50000, null, 500000, 50, 0, null, Timestamp.from(now.minus(60, ChronoUnit.DAYS)), Timestamp.from(now.minus(10, ChronoUnit.DAYS))),
                new VoucherDef("SOLDOUT99", "Voucher da het luot su dung", "PERCENT", 15, 300000L, 1000000, 10, 10, null, Timestamp.from(now.minus(20, ChronoUnit.DAYS)), Timestamp.from(now.plus(20, ChronoUnit.DAYS)))
        );

        Map<String, Long> map = new HashMap<>();
        for (VoucherDef v : vouchers) {
            Long id = jdbc.queryForObject(
                    """
                    INSERT INTO vouchers
                      (code, name, discount_type, discount_value, max_discount_amount, min_order_value,
                       usage_limit, used_count, per_user_limit, applicable_category_id, start_at, end_at, is_active)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, 1, ?, ?, ?, TRUE)
                    ON CONFLICT (code) DO UPDATE SET name = EXCLUDED.name, used_count = EXCLUDED.used_count
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
                        "Le Van Khach Mot", "0912345001", "268 Ly Thuong Kiet, Phuong 12, Quan 10, TP. Ho Chi Minh", "Giao gio hanh chinh",
                        500000, 0, null,
                        List.of(new OrderItemDef("CPU-I5-14600K", 1, 7590000L), new OrderItemDef("MB-GIG-B760M-DS3H", 1, null)),
                        List.of(
                                new HistoryDef(null, "PENDING", "khach1@gmail.com", "Khach hang dat don"),
                                new HistoryDef("PENDING", "CONFIRMED", "staff@soopishop.vn", "Xac nhan don hang"),
                                new HistoryDef("CONFIRMED", "PROCESSING", "staff@soopishop.vn", "Dang dong goi linh kien"),
                                new HistoryDef("PROCESSING", "SHIPPING", "staff@soopishop.vn", "Da ban giao don vi van chuyen"),
                                new HistoryDef("SHIPPING", "COMPLETED", "staff@soopishop.vn", "Khach da nhan hang thanh cong")
                        ),
                        new PaymentDef("VNPAY_SANDBOX", "VNPAY-SB-202611-0001", "SUCCESS", true)),
                new OrderDef("ORD-202611-0002", "khach2@gmail.com", null, "COMPLETED", "COD", "PAID",
                        "Pham Thi Khach Hai", "0912345002", "144 Xuan Thuy, Phuong Dich Vong, Quan Cau Giay, Ha Noi", "Goi truoc khi giao",
                        0, 0, null,
                        List.of(new OrderItemDef("CPU-R7-7800X3D", 1, null), new OrderItemDef("MB-MSI-B650M-MORTAR", 1, null), new OrderItemDef("PSU-CRS-RM850E", 2, null)),
                        List.of(
                                new HistoryDef(null, "PENDING", "khach2@gmail.com", "Khach dat don COD"),
                                new HistoryDef("PENDING", "CONFIRMED", "staff@soopishop.vn", "Da xac nhan qua dien thoai"),
                                new HistoryDef("CONFIRMED", "PROCESSING", "staff@soopishop.vn", "Xuat kho & dong goi"),
                                new HistoryDef("PROCESSING", "SHIPPING", "staff@soopishop.vn", "Dang giao hang"),
                                new HistoryDef("SHIPPING", "COMPLETED", "staff@soopishop.vn", "Hoan tat thu ho COD")
                        ),
                        new PaymentDef("COD", "COD-202611-0002", "SUCCESS", true)),
                new OrderDef("ORD-202611-0003", "khach1@gmail.com", "GIAM200K", "SHIPPING", "MOCK", "PAID",
                        "Le Van Khach Mot", "0912345001", "123 Le Loi, Phuong Ben Nghe, Quan 1, TP. Ho Chi Minh", null,
                        200000, 30000, null,
                        List.of(new OrderItemDef("GPU-ASUS-4070S-12G", 1, null)),
                        List.of(
                                new HistoryDef(null, "PENDING", "khach1@gmail.com", "Dat don thanh toan Mock Gateway"),
                                new HistoryDef("PENDING", "CONFIRMED", "staff@soopishop.vn", "Xac nhan don"),
                                new HistoryDef("CONFIRMED", "PROCESSING", "staff@soopishop.vn", "Dong goi chong soc GPU"),
                                new HistoryDef("PROCESSING", "SHIPPING", "staff@soopishop.vn", "Shipper dang giao")
                        ),
                        new PaymentDef("MOCK_GATEWAY", "MOCK-GW-202611-0003", "SUCCESS", true)),
                new OrderDef("ORD-202611-0004", "khach3@gmail.com", null, "PROCESSING", "COD", "UNPAID",
                        "Hoang Anh Khach Ba", "0912345003", "1 Dai Co Viet, Phuong Bach Khoa, Quan Hai Ba Trung, Ha Noi", null,
                        0, 30000, null,
                        List.of(new OrderItemDef("GPU-MSI-4060-8G", 1, null)),
                        List.of(
                                new HistoryDef(null, "PENDING", "khach3@gmail.com", "Khach dat don"),
                                new HistoryDef("PENDING", "CONFIRMED", "staff@soopishop.vn", "Xac nhan don"),
                                new HistoryDef("CONFIRMED", "PROCESSING", "staff@soopishop.vn", "Dang kiem tra seal")
                        ),
                        new PaymentDef("COD", "COD-202611-0004", "PENDING", false)),
                new OrderDef("ORD-202611-0005", "khach2@gmail.com", null, "CONFIRMED", "SANDBOX", "PAID",
                        "Pham Thi Khach Hai", "0912345002", "144 Xuan Thuy, Phuong Dich Vong, Quan Cau Giay, Ha Noi", null,
                        0, 30000, null,
                        List.of(new OrderItemDef("RAM-KST-FURY-16G", 2, null)),
                        List.of(
                                new HistoryDef(null, "PENDING", "khach2@gmail.com", "Dat mua 2 thanh RAM"),
                                new HistoryDef("PENDING", "CONFIRMED", "staff@soopishop.vn", "Da nhan thanh toan VNPAY Sandbox")
                        ),
                        new PaymentDef("VNPAY_SANDBOX", "VNPAY-SB-202611-0005", "SUCCESS", true)),
                new OrderDef("ORD-202611-0006", "khach3@gmail.com", null, "PENDING", "COD", "UNPAID",
                        "Hoang Anh Khach Ba", "0912345003", "1 Dai Co Viet, Phuong Bach Khoa, Quan Hai Ba Trung, Ha Noi", "Cho xac nhan",
                        0, 30000, null,
                        List.of(new OrderItemDef("CASE-CRS-4000D-BLK", 1, null)),
                        List.of(new HistoryDef(null, "PENDING", "khach3@gmail.com", "Moi tao don hang")),
                        new PaymentDef("COD", "COD-202611-0006", "PENDING", false)),
                new OrderDef("ORD-202611-0007", "khach1@gmail.com", null, "CANCELLED", "COD", "UNPAID",
                        "Le Van Khach Mot", "0912345001", "268 Ly Thuong Kiet, Phuong 12, Quan 10, TP. Ho Chi Minh", null,
                        0, 0, "Khach hang doi y muon nang cap len i7-14700K",
                        List.of(new OrderItemDef("CPU-I5-14600K", 1, null)),
                        List.of(
                                new HistoryDef(null, "PENDING", "khach1@gmail.com", "Khach dat don"),
                                new HistoryDef("PENDING", "CANCELLED", "khach1@gmail.com", "Khach huy don truoc khi xac nhan")
                        ),
                        new PaymentDef("COD", "COD-202611-0007", "FAILED", false))
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
                    ON CONFLICT (order_code) DO UPDATE SET status = EXCLUDED.status, payment_status = EXCLUDED.payment_status
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

    private void seedInventoryLedger(
            Map<String, Long> userMap,
            Map<String, VariantInfo> variantMap,
            Map<String, Integer> initialStockMap,
            Map<String, Long> orderMap,
            List<OrderDef> orders
    ) {
        Integer existingCount = jdbc.queryForObject("SELECT COUNT(*) FROM inventory_transactions", Integer.class);
        if (existingCount != null && existingCount > 0) {
            return;
        }

        Long adminId = userMap.get("admin@soopishop.vn");
        Long staffId = userMap.get("staff@soopishop.vn");
        Map<String, Integer> running = new HashMap<>();

        for (Map.Entry<String, Integer> entry : initialStockMap.entrySet()) {
            if (entry.getValue() > 0) {
                recordTx(running, variantMap, orderMap, entry.getKey(), null, "STOCK_IN", entry.getValue(),
                        "Nhap kho ban dau SKU " + entry.getKey(), adminId);
            }
        }

        for (OrderDef o : orders) {
            for (OrderItemDef item : o.items()) {
                recordTx(running, variantMap, orderMap, item.sku(), o.code(), "ORDER_DEDUCT", -item.qty(),
                        "Tru kho cho don hang " + o.code(), staffId);
                if ("CANCELLED".equals(o.status())) {
                    recordTx(running, variantMap, orderMap, item.sku(), o.code(), "ORDER_CANCEL_RESTOCK", item.qty(),
                            "Hoan kho do huy don hang " + o.code(), staffId);
                }
            }
        }

        recordTx(running, variantMap, orderMap, "GPU-ASUS-4070S-12G", null, "ADJUSTMENT", -1,
                "Dieu chinh kiem ke: 1 san pham loi hop chuyen bao hanh", adminId);

        for (Map.Entry<String, Integer> entry : running.entrySet()) {
            jdbc.update(
                    "UPDATE product_variants SET stock_quantity = ?, updated_at = NOW() WHERE id = ?",
                    entry.getValue(), variantMap.get(entry.getKey()).id()
            );
        }
    }

    private void recordTx(
            Map<String, Integer> running,
            Map<String, VariantInfo> variantMap,
            Map<String, Long> orderMap,
            String sku,
            String orderCode,
            String type,
            int change,
            String note,
            Long byId
    ) {
        int before = running.getOrDefault(sku, 0);
        int after = before + change;
        if (after < 0) {
            throw new IllegalStateException("Negative stock calculation for " + sku);
        }
        running.put(sku, after);
        jdbc.update(
                """
                INSERT INTO inventory_transactions
                  (variant_id, order_id, transaction_type, quantity_before, quantity_change, quantity_after, note, performed_by)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                variantMap.get(sku).id(),
                orderCode != null ? orderMap.get(orderCode) : null,
                type, before, change, after, note, byId
        );
    }

    private void seedReviews(Map<String, Long> userMap, Map<String, Long> productMap, Map<String, Long> orderMap) {
        record ReviewDef(String email, String productSlug, String orderCode, int rating, String comment, boolean visible) {}
        List<ReviewDef> reviews = List.of(
                new ReviewDef("khach1@gmail.com", "intel-core-i5-14600k", "ORD-202611-0001", 5, "CPU chay cuc mat va manh, giao hang nhanh chong trong ngay.", true),
                new ReviewDef("khach1@gmail.com", "gigabyte-b760m-ds3h-ddr4", "ORD-202611-0001", 4, "Mainboard gia tot trong tam gia, du cong ket noi.", true),
                new ReviewDef("khach2@gmail.com", "amd-ryzen-7-7800x3d", "ORD-202611-0002", 5, "FPS game tang ro ret nho 3D V-Cache, rat dang tien.", true),
                new ReviewDef("khach2@gmail.com", "msi-mag-b650m-mortar-wifi", "ORD-202611-0002", 1, "Spam link quang cao khong hop le - da an boi admin.", false)
        );
        for (ReviewDef r : reviews) {
            jdbc.update(
                    """
                    INSERT INTO reviews (user_id, product_id, order_id, rating, comment, is_verified_purchase, is_visible)
                    VALUES (?, ?, ?, ?, ?, TRUE, ?)
                    ON CONFLICT (user_id, product_id, order_id) DO UPDATE SET
                      rating = EXCLUDED.rating, comment = EXCLUDED.comment, is_visible = EXCLUDED.is_visible
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
                  SELECT product_id, COUNT(*)::int AS cnt, ROUND(AVG(rating)::numeric, 2) AS avg_r
                  FROM reviews
                  WHERE is_visible = TRUE
                  GROUP BY product_id
                ) agg
                WHERE p.id = agg.product_id
                """
        );
    }
}
