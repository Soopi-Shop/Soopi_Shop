package vn.soopishop.seed;

import java.util.ArrayList;
import java.util.List;

/**
 * Rich, realistic PC hardware catalog seed data covering all 9 Soopi Shop categories:
 * CPU, GPU, Mainboard, RAM, SSD/HDD, PSU, Case, Tan nhiet, and Phu kien.
 *
 * Designed to satisfy Catalog browsing, Server-side Pagination (> 20 items/page),
 * Multi-criteria Faceted Search, Direct Product Comparison (2-4 items/category),
 * Low-stock / Out-of-stock / Inactive states, and Inventory Audit Ledger verification.
 */
public final class CatalogSeedData {

    private CatalogSeedData() {}

    public record CatDef(String slug, String name, String desc) {}
    public record BrandDef(String slug, String name, String logo) {}
    public record SpecAttrDef(String cat, String code, String name, String unit, String type, int order) {}
    public record SpecVal(String code, String text, Double num) {}
    public record VariantDef(String sku, String name, long listPrice, long salePrice, int initialStock, List<SpecVal> specs) {}
    public record ProductDef(
            String slug,
            String cat,
            String brand,
            String name,
            String desc,
            int warranty,
            String condition,
            String status,
            List<SpecVal> specs,
            List<VariantDef> variants
    ) {}

    public static List<CatDef> categories() {
        return List.of(
                new CatDef("cpu", "CPU (Vi xử lý)", "Bộ vi xử lý Intel Core & AMD Ryzen chính hãng phục vụ gaming và đồ họa"),
                new CatDef("gpu", "GPU (Card màn hình)", "Card đồ họa NVIDIA GeForce RTX & AMD Radeon RX hiệu năng cao"),
                new CatDef("mainboard", "Mainboard (Bo mạch chủ)", "Bo mạch chủ chuẩn Intel & AMD đa dạng chipset và kích thước"),
                new CatDef("ram", "RAM (Bộ nhớ trong)", "Bộ nhớ trong DDR4 & DDR5 dung lượng lớn, xung nhịp cao"),
                new CatDef("ssd-hdd", "SSD/HDD (Ổ cứng)", "Ổ cứng SSD NVMe PCIe, SSD SATA 2.5 inch & HDD lưu trữ dung lượng lớn"),
                new CatDef("psu", "PSU (Nguồn máy tính)", "Nguồn máy tính chuẩn 80 Plus Bronze, Gold & Platinum ổn định điện áp"),
                new CatDef("case", "Case (Vỏ máy tính)", "Vỏ case PC gaming, mặt lưới thoáng khí, kính cường lực & workstation"),
                new CatDef("tan-nhiet", "Tản nhiệt", "Tản nhiệt khí tháp đôi hiệu năng cao & tản nhiệt nước AIO 240mm/360mm"),
                new CatDef("phu-kien", "Phụ kiện", "Bàn phím cơ, chuột gaming, tai nghe, quạt case, keo tản nhiệt & phụ kiện PC")
        );
    }

    public static List<BrandDef> brands() {
        return List.of(
                new BrandDef("intel", "Intel", "https://placehold.co/200x80?text=Intel"),
                new BrandDef("amd", "AMD", "https://placehold.co/200x80?text=AMD"),
                new BrandDef("asus", "ASUS", "https://placehold.co/200x80?text=ASUS"),
                new BrandDef("msi", "MSI", "https://placehold.co/200x80?text=MSI"),
                new BrandDef("gigabyte", "Gigabyte", "https://placehold.co/200x80?text=Gigabyte"),
                new BrandDef("asrock", "ASRock", "https://placehold.co/200x80?text=ASRock"),
                new BrandDef("kingston", "Kingston", "https://placehold.co/200x80?text=Kingston"),
                new BrandDef("corsair", "Corsair", "https://placehold.co/200x80?text=Corsair"),
                new BrandDef("samsung", "Samsung", "https://placehold.co/200x80?text=Samsung"),
                new BrandDef("gskill", "G.Skill", "https://placehold.co/200x80?text=G.Skill"),
                new BrandDef("crucial", "Crucial", "https://placehold.co/200x80?text=Crucial"),
                new BrandDef("western-digital", "Western Digital", "https://placehold.co/200x80?text=WD"),
                new BrandDef("seagate", "Seagate", "https://placehold.co/200x80?text=Seagate"),
                new BrandDef("seasonic", "Seasonic", "https://placehold.co/200x80?text=Seasonic"),
                new BrandDef("cooler-master", "Cooler Master", "https://placehold.co/200x80?text=Cooler+Master"),
                new BrandDef("nzxt", "NZXT", "https://placehold.co/200x80?text=NZXT"),
                new BrandDef("lian-li", "Lian Li", "https://placehold.co/200x80?text=Lian+Li"),
                new BrandDef("deepcool", "DeepCool", "https://placehold.co/200x80?text=DeepCool"),
                new BrandDef("thermalright", "Thermalright", "https://placehold.co/200x80?text=Thermalright"),
                new BrandDef("logitech", "Logitech", "https://placehold.co/200x80?text=Logitech"),
                new BrandDef("keychron", "Keychron", "https://placehold.co/200x80?text=Keychron"),
                new BrandDef("noctua", "Noctua", "https://placehold.co/200x80?text=Noctua")
        );
    }

    public static List<SpecAttrDef> specAttributes() {
        return List.of(
                // CPU (7)
                new SpecAttrDef("cpu", "socket", "Socket", null, "TEXT", 1),
                new SpecAttrDef("cpu", "cores", "Số nhân", "nhân", "NUMBER", 2),
                new SpecAttrDef("cpu", "threads", "Số luồng", "luồng", "NUMBER", 3),
                new SpecAttrDef("cpu", "base_clock", "Xung nhịp cơ bản", "GHz", "NUMBER", 4),
                new SpecAttrDef("cpu", "boost_clock", "Xung nhịp tăng tốc", "GHz", "NUMBER", 5),
                new SpecAttrDef("cpu", "tdp", "Công suất TDP", "W", "NUMBER", 6),
                new SpecAttrDef("cpu", "integrated_gpu", "Đồ họa tích hợp", null, "TEXT", 7),

                // GPU (6)
                new SpecAttrDef("gpu", "gpu_chipset", "Chip đồ họa", null, "TEXT", 1),
                new SpecAttrDef("gpu", "vram", "VRAM", "GB", "NUMBER", 2),
                new SpecAttrDef("gpu", "memory_type", "Chuẩn bộ nhớ", null, "TEXT", 3),
                new SpecAttrDef("gpu", "interface", "Chuẩn giao tiếp", null, "TEXT", 4),
                new SpecAttrDef("gpu", "recommended_psu", "Nguồn khuyến nghị", "W", "NUMBER", 5),
                new SpecAttrDef("gpu", "length_mm", "Chiều dài card", "mm", "NUMBER", 6),

                // Mainboard (6)
                new SpecAttrDef("mainboard", "socket", "Socket", null, "TEXT", 1),
                new SpecAttrDef("mainboard", "chipset", "Chipset", null, "TEXT", 2),
                new SpecAttrDef("mainboard", "form_factor", "Kích thước", null, "TEXT", 3),
                new SpecAttrDef("mainboard", "ram_type", "Hỗ trợ RAM", null, "TEXT", 4),
                new SpecAttrDef("mainboard", "m2_slots", "Số khe M.2", "khe", "NUMBER", 5),
                new SpecAttrDef("mainboard", "wifi", "Kết nối Wi-Fi", null, "TEXT", 6),

                // RAM (5)
                new SpecAttrDef("ram", "ram_type", "Chuẩn RAM", null, "TEXT", 1),
                new SpecAttrDef("ram", "capacity", "Dung lượng", "GB", "NUMBER", 2),
                new SpecAttrDef("ram", "bus_speed", "Tốc độ Bus", "MHz", "NUMBER", 3),
                new SpecAttrDef("ram", "cas_latency", "Độ trễ CAS", null, "TEXT", 4),
                new SpecAttrDef("ram", "led_rgb", "Đèn LED RGB", null, "TEXT", 5),

                // SSD/HDD (5)
                new SpecAttrDef("ssd-hdd", "storage_type", "Loại ổ cứng", null, "TEXT", 1),
                new SpecAttrDef("ssd-hdd", "capacity", "Dung lượng", "GB", "NUMBER", 2),
                new SpecAttrDef("ssd-hdd", "interface", "Chuẩn giao tiếp", null, "TEXT", 3),
                new SpecAttrDef("ssd-hdd", "read_speed", "Tốc độ đọc", "MB/s", "NUMBER", 4),
                new SpecAttrDef("ssd-hdd", "write_speed", "Tốc độ ghi", "MB/s", "NUMBER", 5),

                // PSU (4)
                new SpecAttrDef("psu", "wattage", "Công suất", "W", "NUMBER", 1),
                new SpecAttrDef("psu", "efficiency", "Chuẩn hiệu suất", null, "TEXT", 2),
                new SpecAttrDef("psu", "modular", "Loại dây", null, "TEXT", 3),
                new SpecAttrDef("psu", "atx_standard", "Chuẩn ATX", null, "TEXT", 4),

                // Case (5)
                new SpecAttrDef("case", "case_size", "Phân loại vỏ", null, "TEXT", 1),
                new SpecAttrDef("case", "form_factor", "Hỗ trợ Mainboard", null, "TEXT", 2),
                new SpecAttrDef("case", "max_gpu_length", "Chiều dài GPU tối đa", "mm", "NUMBER", 3),
                new SpecAttrDef("case", "max_cooler_height", "Chiều cao tản khí tối đa", "mm", "NUMBER", 4),
                new SpecAttrDef("case", "color", "Màu sắc", null, "TEXT", 5),

                // Tan nhiet (5)
                new SpecAttrDef("tan-nhiet", "cooler_type", "Loại tản nhiệt", null, "TEXT", 1),
                new SpecAttrDef("tan-nhiet", "socket_support", "Hỗ trợ Socket", null, "TEXT", 2),
                new SpecAttrDef("tan-nhiet", "tdp_rating", "Công suất giải nhiệt", "W", "NUMBER", 3),
                new SpecAttrDef("tan-nhiet", "color", "Màu sắc", null, "TEXT", 4),
                new SpecAttrDef("tan-nhiet", "led_rgb", "Đèn LED RGB", null, "TEXT", 5),

                // Phu kien (5)
                new SpecAttrDef("phu-kien", "accessory_type", "Loại phụ kiện", null, "TEXT", 1),
                new SpecAttrDef("phu-kien", "connection_type", "Chuẩn kết nối", null, "TEXT", 2),
                new SpecAttrDef("phu-kien", "compatibility", "Tương thích", null, "TEXT", 3),
                new SpecAttrDef("phu-kien", "color", "Màu sắc", null, "TEXT", 4),
                new SpecAttrDef("phu-kien", "led_rgb", "Đèn LED RGB", null, "TEXT", 5)
        );
    }

    public static List<ProductDef> allProducts() {
        List<ProductDef> all = new ArrayList<>();
        all.addAll(cpuProducts());
        all.addAll(gpuProducts());
        all.addAll(mainboardProducts());
        all.addAll(ramProducts());
        all.addAll(storageProducts());
        all.addAll(psuProducts());
        all.addAll(caseProducts());
        all.addAll(coolerProducts());
        all.addAll(accessoryProducts());
        return List.copyOf(all);
    }

    private static List<ProductDef> cpuProducts() {
        return List.of(
                new ProductDef("intel-core-i5-14600k", "cpu", "intel", "CPU Intel Core i5-14600K",
                        "14 nhân 20 luồng, xung boost tối đa 5.3GHz, hỗ trợ DDR4/DDR5, lý tưởng cho gaming và sáng tạo nội dung.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "LGA1700", null),
                                new SpecVal("cores", "14 nhân", 14.0),
                                new SpecVal("threads", "20 luồng", 20.0),
                                new SpecVal("base_clock", "3.5 GHz", 3.5),
                                new SpecVal("boost_clock", "5.3 GHz", 5.3),
                                new SpecVal("tdp", "125 W", 125.0),
                                new SpecVal("integrated_gpu", "Có (Intel UHD 770)", null)
                        ),
                        List.of(
                                new VariantDef("CPU-I5-14600K", "Box Chính Hãng", 8490000, 7990000, 25, List.of()),
                                new VariantDef("CPU-I5-14600K-TRAY", "Bản Tray Bảo Hành 36T", 7690000, 7190000, 12, List.of())
                        )),
                new ProductDef("amd-ryzen-7-7800x3d", "cpu", "amd", "CPU AMD Ryzen 7 7800X3D",
                        "8 nhân 16 luồng, công nghệ 3D V-Cache 96MB L3 tối ưu hiệu năng gaming hàng đầu.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "AM5", null),
                                new SpecVal("cores", "8 nhân", 8.0),
                                new SpecVal("threads", "16 luồng", 16.0),
                                new SpecVal("base_clock", "4.2 GHz", 4.2),
                                new SpecVal("boost_clock", "5.0 GHz", 5.0),
                                new SpecVal("tdp", "120 W", 120.0),
                                new SpecVal("integrated_gpu", "Có (AMD Radeon Graphics)", null)
                        ),
                        List.of(new VariantDef("CPU-R7-7800X3D", "Box Chính Hãng", 10990000, 9990000, 15, List.of()))),
                new ProductDef("intel-core-i3-12100f", "cpu", "intel", "CPU Intel Core i3-12100F",
                        "4 nhân 8 luồng tiết kiệm điện, lựa chọn quốc dân cho cấu hình PC sinh viên và gaming phổ thông.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "LGA1700", null),
                                new SpecVal("cores", "4 nhân", 4.0),
                                new SpecVal("threads", "8 luồng", 8.0),
                                new SpecVal("base_clock", "3.3 GHz", 3.3),
                                new SpecVal("boost_clock", "4.3 GHz", 4.3),
                                new SpecVal("tdp", "58 W", 58.0),
                                new SpecVal("integrated_gpu", "Không", null)
                        ),
                        List.of(new VariantDef("CPU-I3-12100F", "Box Kèm Quạt Zin", 2390000, 2090000, 35, List.of()))),
                new ProductDef("intel-core-i5-12400f", "cpu", "intel", "CPU Intel Core i5-12400F",
                        "6 nhân 12 luồng cân bằng hoàn hảo hiệu năng và chi phí cho PC gaming tầm trung.",
                        36, "TRAY", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "LGA1700", null),
                                new SpecVal("cores", "6 nhân", 6.0),
                                new SpecVal("threads", "12 luồng", 12.0),
                                new SpecVal("base_clock", "2.5 GHz", 2.5),
                                new SpecVal("boost_clock", "4.4 GHz", 4.4),
                                new SpecVal("tdp", "65 W", 65.0),
                                new SpecVal("integrated_gpu", "Không", null)
                        ),
                        List.of(
                                new VariantDef("CPU-I5-12400F-TRAY", "Bản Tray Không Quạt", 2990000, 2690000, 40, List.of()),
                                new VariantDef("CPU-I5-12400F-BOX", "Box Chính Hãng", 3490000, 3190000, 22, List.of())
                        )),
                new ProductDef("intel-core-i7-14700k", "cpu", "intel", "CPU Intel Core i7-14700K",
                        "20 nhân 28 luồng mạnh mẽ cho dựng phim 4K, render 3D và chiến game AAA tốc độ khung hình cao.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "LGA1700", null),
                                new SpecVal("cores", "20 nhân", 20.0),
                                new SpecVal("threads", "28 luồng", 28.0),
                                new SpecVal("base_clock", "3.4 GHz", 3.4),
                                new SpecVal("boost_clock", "5.6 GHz", 5.6),
                                new SpecVal("tdp", "125 W", 125.0),
                                new SpecVal("integrated_gpu", "Có (Intel UHD 770)", null)
                        ),
                        List.of(new VariantDef("CPU-I7-14700K", "Box Chính Hãng", 11490000, 10690000, 10, List.of()))),
                new ProductDef("intel-core-ultra-7-265k", "cpu", "intel", "CPU Intel Core Ultra 7 265K",
                        "Kiến trúc Arrow Lake 20 nhân 20 luồng tích hợp NPU AI, mát mẻ và tiết kiệm điện trên nền tảng LGA1851.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "LGA1851", null),
                                new SpecVal("cores", "20 nhân", 20.0),
                                new SpecVal("threads", "20 luồng", 20.0),
                                new SpecVal("base_clock", "3.9 GHz", 3.9),
                                new SpecVal("boost_clock", "5.5 GHz", 5.5),
                                new SpecVal("tdp", "125 W", 125.0),
                                new SpecVal("integrated_gpu", "Có (Intel Graphics)", null)
                        ),
                        List.of(new VariantDef("CPU-U7-265K", "Box Chính Hãng", 10990000, 10290000, 8, List.of()))),
                new ProductDef("amd-ryzen-5-5600x", "cpu", "amd", "CPU AMD Ryzen 5 5600X",
                        "6 nhân 12 luồng kiến trúc Zen 3 trên nền tảng AM4 bền bỉ, tối ưu ngân sách nâng cấp máy cũ.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "AM4", null),
                                new SpecVal("cores", "6 nhân", 6.0),
                                new SpecVal("threads", "12 luồng", 12.0),
                                new SpecVal("base_clock", "3.7 GHz", 3.7),
                                new SpecVal("boost_clock", "4.6 GHz", 4.6),
                                new SpecVal("tdp", "65 W", 65.0),
                                new SpecVal("integrated_gpu", "Không", null)
                        ),
                        List.of(new VariantDef("CPU-R5-5600X", "Box Kèm Wraith Stealth", 3690000, 3290000, 19, List.of()))),
                new ProductDef("amd-ryzen-5-7500f", "cpu", "amd", "CPU AMD Ryzen 5 7500F",
                        "6 nhân 12 luồng AM5 DDR5 giá cực tốt, hiệu năng đơn nhân cao cho PC gaming hiện đại.",
                        36, "TRAY", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "AM5", null),
                                new SpecVal("cores", "6 nhân", 6.0),
                                new SpecVal("threads", "12 luồng", 12.0),
                                new SpecVal("base_clock", "3.7 GHz", 3.7),
                                new SpecVal("boost_clock", "5.0 GHz", 5.0),
                                new SpecVal("tdp", "65 W", 65.0),
                                new SpecVal("integrated_gpu", "Không", null)
                        ),
                        List.of(new VariantDef("CPU-R5-7500F-TRAY", "Bản Tray Chính Hãng", 4290000, 3890000, 26, List.of()))),
                new ProductDef("amd-ryzen-7-9800x3d", "cpu", "amd", "CPU AMD Ryzen 7 9800X3D",
                        "Thế hệ 3D V-Cache mới nhất kiến trúc Zen 5, xung nhịp boost 5.2GHz, đỉnh cao hiệu năng gaming.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "AM5", null),
                                new SpecVal("cores", "8 nhân", 8.0),
                                new SpecVal("threads", "16 luồng", 16.0),
                                new SpecVal("base_clock", "4.7 GHz", 4.7),
                                new SpecVal("boost_clock", "5.2 GHz", 5.2),
                                new SpecVal("tdp", "120 W", 120.0),
                                new SpecVal("integrated_gpu", "Có (AMD Radeon Graphics)", null)
                        ),
                        List.of(new VariantDef("CPU-R7-9800X3D", "Box Chính Hãng", 13990000, 13490000, 4, List.of()))),
                new ProductDef("intel-core-i9-12900k-legacy", "cpu", "intel", "CPU Intel Core i9-12900K (Ngưng kinh doanh)",
                        "16 nhân 24 luồng thế hệ 12 — sản phẩm lưu kho ngưng kinh doanh phục vụ kiểm thử trạng thái INACTIVE.",
                        36, "NEW_FULLBOX", "INACTIVE",
                        List.of(
                                new SpecVal("socket", "LGA1700", null),
                                new SpecVal("cores", "16 nhân", 16.0),
                                new SpecVal("threads", "24 luồng", 24.0),
                                new SpecVal("base_clock", "3.2 GHz", 3.2),
                                new SpecVal("boost_clock", "5.2 GHz", 5.2),
                                new SpecVal("tdp", "125 W", 125.0),
                                new SpecVal("integrated_gpu", "Có (Intel UHD 770)", null)
                        ),
                        List.of(new VariantDef("CPU-I9-12900K-OLD", "Box Chính Hãng", 12990000, 9990000, 2, List.of())))
        );
    }

    private static List<ProductDef> gpuProducts() {
        return List.of(
                new ProductDef("asus-dual-rtx-4070-super-12g", "gpu", "asus", "Card màn hình ASUS Dual GeForce RTX 4070 SUPER 12GB",
                        "Kiến trúc Ada Lovelace, DLSS 3.5, 2 quạt Axial-tech gọn gàng cho độ phân giải 2K 144Hz.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("gpu_chipset", "NVIDIA GeForce", null),
                                new SpecVal("vram", "12 GB", 12.0),
                                new SpecVal("memory_type", "GDDR6X", null),
                                new SpecVal("interface", "PCIe 4.0 x16", null),
                                new SpecVal("recommended_psu", "650 W", 650.0),
                                new SpecVal("length_mm", "267 mm", 267.0)
                        ),
                        List.of(new VariantDef("GPU-ASUS-4070S-12G", "OC Edition 12GB Đen", 18490000, 17490000, 5, List.of()))),
                new ProductDef("msi-rtx-4060-ventus-2x-8g", "gpu", "msi", "Card màn hình MSI GeForce RTX 4060 Ventus 2X Black 8G OC",
                        "Card đồ họa tầm trung hiệu năng cao, tiết kiệm điện năng chỉ 115W, hỗ trợ DLSS 3 Frame Generation.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("gpu_chipset", "NVIDIA GeForce", null),
                                new SpecVal("vram", "8 GB", 8.0),
                                new SpecVal("memory_type", "GDDR6", null),
                                new SpecVal("interface", "PCIe 4.0 x8", null),
                                new SpecVal("recommended_psu", "550 W", 550.0),
                                new SpecVal("length_mm", "199 mm", 199.0)
                        ),
                        List.of(
                                new VariantDef("GPU-MSI-4060-8G", "Ventus 2X 8GB Đen", 8990000, 8490000, 20, List.of()),
                                new VariantDef("GPU-MSI-4060-8G-WHT", "Ventus 2X 8GB Trắng", 9290000, 8790000, 11, List.of())
                        )),
                new ProductDef("gigabyte-rtx-3060-windforce-12g", "gpu", "gigabyte", "Card màn hình Gigabyte GeForce RTX 3060 WINDFORCE OC 12GB",
                        "Dung lượng 12GB VRAM rộng rãi cho làm đồ họa 3D, Stable Diffusion cơ bản và gaming Full HD.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("gpu_chipset", "NVIDIA GeForce", null),
                                new SpecVal("vram", "12 GB", 12.0),
                                new SpecVal("memory_type", "GDDR6", null),
                                new SpecVal("interface", "PCIe 4.0 x16", null),
                                new SpecVal("recommended_psu", "550 W", 550.0),
                                new SpecVal("length_mm", "198 mm", 198.0)
                        ),
                        List.of(new VariantDef("GPU-GIG-3060-12G", "Windforce OC 12GB", 7690000, 7190000, 18, List.of()))),
                new ProductDef("gigabyte-rtx-4060-ti-eagle-8g", "gpu", "gigabyte", "Card màn hình Gigabyte GeForce RTX 4060 Ti EAGLE OC 8GB",
                        "Hệ thống tản nhiệt 3 quạt WINDFORCE quay đảo chiều mát mẻ, hiệu năng xuất sắc cho màn hình 1080p/2K.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("gpu_chipset", "NVIDIA GeForce", null),
                                new SpecVal("vram", "8 GB", 8.0),
                                new SpecVal("memory_type", "GDDR6", null),
                                new SpecVal("interface", "PCIe 4.0 x8", null),
                                new SpecVal("recommended_psu", "550 W", 550.0),
                                new SpecVal("length_mm", "272 mm", 272.0)
                        ),
                        List.of(new VariantDef("GPU-GIG-4060TI-8G", "Eagle OC 8GB", 11890000, 11290000, 14, List.of()))),
                new ProductDef("msi-rtx-4070-ti-super-gaming-x-slim", "gpu", "msi", "Card màn hình MSI GeForce RTX 4070 Ti SUPER 16G GAMING X SLIM",
                        "16GB GDDR6X băng thông 256-bit, thiết kế Slim 3 quạt TRI FROZR 3 êm ái cho gaming 2K/4K và AI.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("gpu_chipset", "NVIDIA GeForce", null),
                                new SpecVal("vram", "16 GB", 16.0),
                                new SpecVal("memory_type", "GDDR6X", null),
                                new SpecVal("interface", "PCIe 4.0 x16", null),
                                new SpecVal("recommended_psu", "750 W", 750.0),
                                new SpecVal("length_mm", "307 mm", 307.0)
                        ),
                        List.of(new VariantDef("GPU-MSI-4070TIS-16G", "Gaming X Slim 16GB", 24990000, 23890000, 7, List.of()))),
                new ProductDef("asus-tuf-rtx-4080-super-16g", "gpu", "asus", "Card màn hình ASUS TUF Gaming GeForce RTX 4080 SUPER 16GB OC",
                        "Độ bền chuẩn quân sự, khung kim loại cứng cáp, chiến mượt mọi tựa game 4K Ultra Ray Tracing.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("gpu_chipset", "NVIDIA GeForce", null),
                                new SpecVal("vram", "16 GB", 16.0),
                                new SpecVal("memory_type", "GDDR6X", null),
                                new SpecVal("interface", "PCIe 4.0 x16", null),
                                new SpecVal("recommended_psu", "850 W", 850.0),
                                new SpecVal("length_mm", "348 mm", 348.0)
                        ),
                        List.of(new VariantDef("GPU-ASUS-4080S-16G", "TUF OC 16GB", 32990000, 31490000, 3, List.of()))),
                new ProductDef("asus-rog-strix-rtx-4090-24g", "gpu", "asus", "Card màn hình ASUS ROG Strix GeForce RTX 4090 24GB OC",
                        "Flagship 24GB GDDR6X dành cho chuyên gia AI, dựng phim 8K và game thủ đỉnh cao.",
                        36, "NEW_FULLBOX", "OUT_OF_STOCK",
                        List.of(
                                new SpecVal("gpu_chipset", "NVIDIA GeForce", null),
                                new SpecVal("vram", "24 GB", 24.0),
                                new SpecVal("memory_type", "GDDR6X", null),
                                new SpecVal("interface", "PCIe 4.0 x16", null),
                                new SpecVal("recommended_psu", "1000 W", 1000.0),
                                new SpecVal("length_mm", "358 mm", 358.0)
                        ),
                        List.of(new VariantDef("GPU-ASUS-4090-24G", "ROG Strix OC 24GB", 56990000, 54990000, 0, List.of()))),
                new ProductDef("asrock-rx-6600-challenger-d-8g", "gpu", "asrock", "Card màn hình ASRock Radeon RX 6600 Challenger D 8GB",
                        "Hiệu năng trên giá thành xuất sắc nhất phân khúc phổ thông, chơi mượt eSports và AAA 1080p.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("gpu_chipset", "AMD Radeon", null),
                                new SpecVal("vram", "8 GB", 8.0),
                                new SpecVal("memory_type", "GDDR6", null),
                                new SpecVal("interface", "PCIe 4.0 x8", null),
                                new SpecVal("recommended_psu", "500 W", 500.0),
                                new SpecVal("length_mm", "269 mm", 269.0)
                        ),
                        List.of(new VariantDef("GPU-ASR-RX6600-8G", "Challenger D 8GB", 5690000, 5190000, 24, List.of()))),
                new ProductDef("gigabyte-rx-7800-xt-gaming-oc-16g", "gpu", "gigabyte", "Card màn hình Gigabyte Radeon RX 7800 XT GAMING OC 16GB",
                        "16GB GDDR6 hiệu năng rasterization mạnh mẽ ở độ phân giải 1440p, hỗ trợ FSR 3.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("gpu_chipset", "AMD Radeon", null),
                                new SpecVal("vram", "16 GB", 16.0),
                                new SpecVal("memory_type", "GDDR6", null),
                                new SpecVal("interface", "PCIe 4.0 x16", null),
                                new SpecVal("recommended_psu", "700 W", 700.0),
                                new SpecVal("length_mm", "302 mm", 302.0)
                        ),
                        List.of(new VariantDef("GPU-GIG-RX7800XT-16G", "Gaming OC 16GB", 14990000, 13990000, 9, List.of()))),
                new ProductDef("asrock-rx-7900-xtx-phantom-24g", "gpu", "asrock", "Card màn hình ASRock Radeon RX 7900 XTX Phantom Gaming 24GB OC",
                        "24GB VRAM băng thông 384-bit, thiết kế 3 quạt ARGB mạnh mẽ cho gaming 4K thuần túy.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("gpu_chipset", "AMD Radeon", null),
                                new SpecVal("vram", "24 GB", 24.0),
                                new SpecVal("memory_type", "GDDR6", null),
                                new SpecVal("interface", "PCIe 4.0 x16", null),
                                new SpecVal("recommended_psu", "850 W", 850.0),
                                new SpecVal("length_mm", "330 mm", 330.0)
                        ),
                        List.of(new VariantDef("GPU-ASR-RX7900XTX-24G", "Phantom Gaming 24GB", 27490000, 25990000, 4, List.of())))
        );
    }

    private static List<ProductDef> mainboardProducts() {
        return List.of(
                new ProductDef("gigabyte-b760m-ds3h-ddr4", "mainboard", "gigabyte", "Mainboard Gigabyte B760M DS3H DDR4",
                        "Hỗ trợ CPU Intel Gen 12/13/14, 4 khe RAM DDR4, 2 khe M.2 PCIe 4.0 và cổng LAN 2.5GbE.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "LGA1700", null),
                                new SpecVal("chipset", "B760", null),
                                new SpecVal("form_factor", "Micro-ATX", null),
                                new SpecVal("ram_type", "DDR4", null),
                                new SpecVal("m2_slots", "2 khe", 2.0),
                                new SpecVal("wifi", "Không", null)
                        ),
                        List.of(new VariantDef("MB-GIG-B760M-DS3H", "B760M DS3H DDR4", 3290000, 2990000, 18, List.of()))),
                new ProductDef("msi-mag-b650m-mortar-wifi", "mainboard", "msi", "Mainboard MSI MAG B650M MORTAR WIFI",
                        "Bo mạch chủ AM5 cao cấp với VRM 12+2+1 phase 80A, tích hợp Wi-Fi 6E và Bluetooth 5.3.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "AM5", null),
                                new SpecVal("chipset", "B650", null),
                                new SpecVal("form_factor", "Micro-ATX", null),
                                new SpecVal("ram_type", "DDR5", null),
                                new SpecVal("m2_slots", "2 khe", 2.0),
                                new SpecVal("wifi", "Wi-Fi 6E", null)
                        ),
                        List.of(new VariantDef("MB-MSI-B650M-MORTAR", "B650M Mortar WiFi", 5690000, 5290000, 16, List.of()))),
                new ProductDef("asus-prime-h610m-k-d4", "mainboard", "asus", "Mainboard ASUS PRIME H610M-K D4",
                        "Bo mạch chủ LGA1700 tiết kiệm chi phí, bền bỉ với công nghệ bảo vệ 5X Protection III.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "LGA1700", null),
                                new SpecVal("chipset", "H610", null),
                                new SpecVal("form_factor", "Micro-ATX", null),
                                new SpecVal("ram_type", "DDR4", null),
                                new SpecVal("m2_slots", "1 khe", 1.0),
                                new SpecVal("wifi", "Không", null)
                        ),
                        List.of(new VariantDef("MB-ASUS-H610M-K", "H610M-K DDR4", 1990000, 1790000, 30, List.of()))),
                new ProductDef("msi-pro-b760m-a-wifi-ddr5", "mainboard", "msi", "Mainboard MSI PRO B760M-A WIFI DDR5",
                        "Hỗ trợ RAM DDR5 tốc độ cao đến 6800+ MHz, trang bị sẵn Wi-Fi 6E và tản nhiệt M.2 Shield Frozr.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "LGA1700", null),
                                new SpecVal("chipset", "B760", null),
                                new SpecVal("form_factor", "Micro-ATX", null),
                                new SpecVal("ram_type", "DDR5", null),
                                new SpecVal("m2_slots", "2 khe", 2.0),
                                new SpecVal("wifi", "Wi-Fi 6E", null)
                        ),
                        List.of(new VariantDef("MB-MSI-B760M-A-D5", "PRO B760M-A WiFi DDR5", 4190000, 3890000, 21, List.of()))),
                new ProductDef("asus-tuf-gaming-z790-plus-wifi", "mainboard", "asus", "Mainboard ASUS TUF GAMING Z790-PLUS WIFI",
                        "Bo mạch chủ ATX ép xung Intel K-series với 16+1 DrMOS, 4 khe M.2 PCIe 4.0 và PCIe 5.0 x16.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "LGA1700", null),
                                new SpecVal("chipset", "Z790", null),
                                new SpecVal("form_factor", "ATX", null),
                                new SpecVal("ram_type", "DDR5", null),
                                new SpecVal("m2_slots", "4 khe", 4.0),
                                new SpecVal("wifi", "Wi-Fi 6E", null)
                        ),
                        List.of(new VariantDef("MB-ASUS-Z790-TUF", "TUF Z790-PLUS WiFi", 6990000, 6490000, 12, List.of()))),
                new ProductDef("gigabyte-z890-aorus-elite-wifi7", "mainboard", "gigabyte", "Mainboard Gigabyte Z890 AORUS ELITE WIFI7",
                        "Nền tảng LGA1851 mới nhất cho Intel Core Ultra, tích hợp Wi-Fi 7, Thunderbolt 4 và 4 khe M.2.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "LGA1851", null),
                                new SpecVal("chipset", "Z890", null),
                                new SpecVal("form_factor", "ATX", null),
                                new SpecVal("ram_type", "DDR5", null),
                                new SpecVal("m2_slots", "4 khe", 4.0),
                                new SpecVal("wifi", "Wi-Fi 7", null)
                        ),
                        List.of(new VariantDef("MB-GIG-Z890-ELITE", "AORUS Elite WiFi 7", 8290000, 7790000, 6, List.of()))),
                new ProductDef("msi-b550m-pro-vdh-wifi", "mainboard", "msi", "Mainboard MSI B550M PRO-VDH WIFI",
                        "Bo mạch chủ AM4 phổ biến với 4 khe DDR4, khe PCIe 4.0 bọc thép và Wi-Fi tích hợp.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "AM4", null),
                                new SpecVal("chipset", "B550", null),
                                new SpecVal("form_factor", "Micro-ATX", null),
                                new SpecVal("ram_type", "DDR4", null),
                                new SpecVal("m2_slots", "2 khe", 2.0),
                                new SpecVal("wifi", "Wi-Fi 6", null)
                        ),
                        List.of(new VariantDef("MB-MSI-B550M-VDH", "B550M PRO-VDH WiFi", 2890000, 2590000, 17, List.of()))),
                new ProductDef("asrock-a620m-pro-rs-wifi", "mainboard", "asrock", "Mainboard ASRock A620M Pro RS WiFi",
                        "Lựa chọn AM5 tiết kiệm nhất có 3 khe M.2, hỗ trợ DDR5 và kết nối Wi-Fi 6E.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "AM5", null),
                                new SpecVal("chipset", "A620", null),
                                new SpecVal("form_factor", "Micro-ATX", null),
                                new SpecVal("ram_type", "DDR5", null),
                                new SpecVal("m2_slots", "3 khe", 3.0),
                                new SpecVal("wifi", "Wi-Fi 6E", null)
                        ),
                        List.of(new VariantDef("MB-ASR-A620M-PRORS", "A620M Pro RS WiFi", 3490000, 3190000, 14, List.of()))),
                new ProductDef("gigabyte-b650-aorus-elite-ax", "mainboard", "gigabyte", "Mainboard Gigabyte B650 AORUS ELITE AX",
                        "Kích thước chuẩn ATX, VRM mạnh mẽ hỗ trợ Ryzen 7000/9000 series cùng 3 khe M.2 PCIe 5.0/4.0.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "AM5", null),
                                new SpecVal("chipset", "B650", null),
                                new SpecVal("form_factor", "ATX", null),
                                new SpecVal("ram_type", "DDR5", null),
                                new SpecVal("m2_slots", "3 khe", 3.0),
                                new SpecVal("wifi", "Wi-Fi 6E", null)
                        ),
                        List.of(new VariantDef("MB-GIG-B650-AORUS", "B650 AORUS Elite AX", 5990000, 5590000, 11, List.of()))),
                new ProductDef("asus-rog-strix-x870e-e-gaming-wifi", "mainboard", "asus", "Mainboard ASUS ROG STRIX X870E-E GAMING WIFI",
                        "Bo mạch chủ AM5 cao cấp nhất hỗ trợ Wi-Fi 7, USB4 40Gbps, 5 khe M.2 và AI Overclocking.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("socket", "AM5", null),
                                new SpecVal("chipset", "X870E", null),
                                new SpecVal("form_factor", "ATX", null),
                                new SpecVal("ram_type", "DDR5", null),
                                new SpecVal("m2_slots", "5 khe", 5.0),
                                new SpecVal("wifi", "Wi-Fi 7", null)
                        ),
                        List.of(new VariantDef("MB-ASUS-X870E-STRIX", "ROG Strix X870E-E WiFi", 13990000, 13290000, 3, List.of())))
        );
    }

    private static List<ProductDef> ramProducts() {
        return List.of(
                new ProductDef("kingston-fury-beast-ddr5-6000", "ram", "kingston", "RAM Kingston Fury Beast DDR5 6000MHz",
                        "Hỗ trợ Intel XMP 3.0 và AMD EXPO, tản nhiệt nhôm gọn gàng tương thích mọi tản nhiệt khí.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("ram_type", "DDR5", null),
                                new SpecVal("bus_speed", "6000 MHz", 6000.0),
                                new SpecVal("cas_latency", "CL36", null),
                                new SpecVal("led_rgb", "Không", null)
                        ),
                        List.of(
                                new VariantDef("RAM-KST-FURY-16G", "16GB (1x16GB) DDR5 6000MHz", 1690000, 1490000, 30, List.of(new SpecVal("capacity", "16 GB", 16.0))),
                                new VariantDef("RAM-KST-FURY-32G", "32GB (2x16GB) DDR5 6000MHz", 3290000, 2950000, 2, List.of(new SpecVal("capacity", "32 GB", 32.0))),
                                new VariantDef("RAM-KST-FURY-64G", "64GB (2x32GB) DDR5 6000MHz", 6190000, 5690000, 8, List.of(new SpecVal("capacity", "64 GB", 64.0)))
                        )),
                new ProductDef("corsair-vengeance-rgb-ddr5-5600", "ram", "corsair", "RAM Corsair Vengeance RGB DDR5 5600MHz",
                        "Đèn LED RGB 10 vùng độc lập tùy biến qua phần mềm iCUE, tối ưu ổn định cho dàn PC gaming.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("ram_type", "DDR5", null),
                                new SpecVal("bus_speed", "5600 MHz", 5600.0),
                                new SpecVal("cas_latency", "CL36", null),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(
                                new VariantDef("RAM-CRS-VENG-32G", "32GB (2x16GB) Đen", 3490000, 3190000, 18, List.of(new SpecVal("capacity", "32 GB", 32.0))),
                                new VariantDef("RAM-CRS-VENG-32G-WHT", "32GB (2x16GB) Trắng", 3590000, 3290000, 10, List.of(new SpecVal("capacity", "32 GB", 32.0)))
                        )),
                new ProductDef("kingston-fury-beast-ddr4-3200", "ram", "kingston", "RAM Kingston Fury Beast DDR4 3200MHz",
                        "Dòng RAM DDR4 bán chạy nhất phân khúc phổ thông, cắm là chạy với Plug N Play 3200MHz.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("ram_type", "DDR4", null),
                                new SpecVal("bus_speed", "3200 MHz", 3200.0),
                                new SpecVal("cas_latency", "CL16", null),
                                new SpecVal("led_rgb", "Không", null)
                        ),
                        List.of(
                                new VariantDef("RAM-KST-D4-8G", "8GB (1x8GB) DDR4 3200MHz", 590000, 490000, 45, List.of(new SpecVal("capacity", "8 GB", 8.0))),
                                new VariantDef("RAM-KST-D4-16G", "16GB (2x8GB) DDR4 3200MHz", 1150000, 990000, 34, List.of(new SpecVal("capacity", "16 GB", 16.0))),
                                new VariantDef("RAM-KST-D4-32G", "32GB (2x16GB) DDR4 3200MHz", 2090000, 1890000, 20, List.of(new SpecVal("capacity", "32 GB", 32.0)))
                        )),
                new ProductDef("corsair-vengeance-lpx-ddr4-3200", "ram", "corsair", "RAM Corsair Vengeance LPX DDR4 3200MHz",
                        "Thiết kế low-profile siêu thấp bằng nhôm nguyên chất, độ tương thích tuyệt đối với tản khí lớn.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("ram_type", "DDR4", null),
                                new SpecVal("bus_speed", "3200 MHz", 3200.0),
                                new SpecVal("cas_latency", "CL16", null),
                                new SpecVal("led_rgb", "Không", null)
                        ),
                        List.of(
                                new VariantDef("RAM-CRS-LPX-16G", "16GB (2x8GB) Đen", 1190000, 1050000, 28, List.of(new SpecVal("capacity", "16 GB", 16.0))),
                                new VariantDef("RAM-CRS-LPX-32G", "32GB (2x16GB) Đen", 2190000, 1950000, 16, List.of(new SpecVal("capacity", "32 GB", 32.0)))
                        )),
                new ProductDef("gskill-trident-z-rgb-ddr4-3600", "ram", "gskill", "RAM G.Skill Trident Z RGB DDR4 3600MHz",
                        "Thanh dẫn sáng RGB sang trọng, bus 3600MHz tối ưu hiệu năng cho CPU AMD Ryzen AM4.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("ram_type", "DDR4", null),
                                new SpecVal("bus_speed", "3600 MHz", 3600.0),
                                new SpecVal("cas_latency", "CL18", null),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(
                                new VariantDef("RAM-GSK-TZD4-16G", "16GB (2x8GB) 3600MHz", 1590000, 1390000, 15, List.of(new SpecVal("capacity", "16 GB", 16.0))),
                                new VariantDef("RAM-GSK-TZD4-32G", "32GB (2x16GB) 3600MHz", 2690000, 2450000, 12, List.of(new SpecVal("capacity", "32 GB", 32.0)))
                        )),
                new ProductDef("crucial-pro-ddr4-3200", "ram", "crucial", "RAM Crucial Pro DDR4 3200MHz 32GB",
                        "Độ ổn định chuẩn Micron dành cho máy trạm văn phòng, lập trình và chạy nhiều máy ảo.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("ram_type", "DDR4", null),
                                new SpecVal("capacity", "32 GB", 32.0),
                                new SpecVal("bus_speed", "3200 MHz", 3200.0),
                                new SpecVal("cas_latency", "CL22", null),
                                new SpecVal("led_rgb", "Không", null)
                        ),
                        List.of(new VariantDef("RAM-CRU-PROD4-32G", "32GB (2x16GB) Đen", 1990000, 1790000, 22, List.of()))),
                new ProductDef("gskill-trident-z5-neo-rgb-ddr5-6000", "ram", "gskill", "RAM G.Skill Trident Z5 Neo RGB DDR5 6000MHz CL30",
                        "Chuẩn AMD EXPO với độ trễ siêu thấp CL30 — điểm ngọt hiệu năng tốt nhất cho Ryzen 7000/9000.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("ram_type", "DDR5", null),
                                new SpecVal("bus_speed", "6000 MHz", 6000.0),
                                new SpecVal("cas_latency", "CL30", null),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(
                                new VariantDef("RAM-GSK-TZ5NEO-32G", "32GB (2x16GB) Đen CL30", 3890000, 3590000, 14, List.of(new SpecVal("capacity", "32 GB", 32.0))),
                                new VariantDef("RAM-GSK-TZ5NEO-64G", "64GB (2x32GB) Đen CL30", 7290000, 6890000, 5, List.of(new SpecVal("capacity", "64 GB", 64.0)))
                        )),
                new ProductDef("corsair-dominator-titanium-ddr5-6400", "ram", "corsair", "RAM Corsair Dominator Titanium RGB DDR5 6400MHz",
                        "Dòng RAM cao cấp nhất của Corsair với công nghệ tản nhiệt DHX và thanh LED trên có thể tháo rời.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("ram_type", "DDR5", null),
                                new SpecVal("bus_speed", "6400 MHz", 6400.0),
                                new SpecVal("cas_latency", "CL32", null),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(
                                new VariantDef("RAM-CRS-DOM-32G", "32GB (2x16GB) Đen", 4990000, 4690000, 6, List.of(new SpecVal("capacity", "32 GB", 32.0))),
                                new VariantDef("RAM-CRS-DOM-64G", "64GB (2x32GB) Trắng", 9290000, 8790000, 3, List.of(new SpecVal("capacity", "64 GB", 64.0)))
                        )),
                new ProductDef("crucial-pro-ddr5-5600", "ram", "crucial", "RAM Crucial Pro DDR5 5600MHz",
                        "Giá thành hợp lý, hỗ trợ cả XMP 3.0 và EXPO cho hệ thống làm việc cần dung lượng RAM lớn.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("ram_type", "DDR5", null),
                                new SpecVal("bus_speed", "5600 MHz", 5600.0),
                                new SpecVal("cas_latency", "CL46", null),
                                new SpecVal("led_rgb", "Không", null)
                        ),
                        List.of(
                                new VariantDef("RAM-CRU-PROD5-32G", "32GB (2x16GB) 5600MHz", 2690000, 2490000, 25, List.of(new SpecVal("capacity", "32 GB", 32.0)))
                        )),
                new ProductDef("gskill-ripjaws-s5-ddr5-5200", "ram", "gskill", "RAM G.Skill Ripjaws S5 DDR5 5200MHz",
                        "Chiều cao chỉ 33mm gọn gàng, phù hợp build PC mini-ITX hoặc lắp dưới tản khí tháp đôi.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("ram_type", "DDR5", null),
                                new SpecVal("capacity", "32 GB", 32.0),
                                new SpecVal("bus_speed", "5200 MHz", 5200.0),
                                new SpecVal("cas_latency", "CL40", null),
                                new SpecVal("led_rgb", "Không", null)
                        ),
                        List.of(new VariantDef("RAM-GSK-RJS5-32G", "32GB (2x16GB) Trắng", 2590000, 2350000, 17, List.of())))
        );
    }

    private static List<ProductDef> storageProducts() {
        return List.of(
                new ProductDef("samsung-990-pro-nvme-m2", "ssd-hdd", "samsung", "SSD Samsung 990 PRO PCIe 4.0 NVMe M.2",
                        "SSD PCIe 4.0 hiệu năng đỉnh cao của Samsung, tốc độ đọc 7450MB/s, lý tưởng cho hệ điều hành và đồ họa nặng.",
                        60, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("storage_type", "SSD NVMe", null),
                                new SpecVal("interface", "M.2 NVMe PCIe 4.0", null),
                                new SpecVal("read_speed", "7450 MB/s", 7450.0),
                                new SpecVal("write_speed", "6900 MB/s", 6900.0)
                        ),
                        List.of(
                                new VariantDef("SSD-SAM-990PRO-512", "512GB NVMe", 2190000, 1890000, 22, List.of(new SpecVal("capacity", "512 GB", 512.0))),
                                new VariantDef("SSD-SAM-990PRO-1TB", "1TB NVMe", 3490000, 3150000, 1, List.of(new SpecVal("capacity", "1000 GB", 1000.0))),
                                new VariantDef("SSD-SAM-990PRO-2TB", "2TB NVMe", 5890000, 5390000, 11, List.of(new SpecVal("capacity", "2000 GB", 2000.0)))
                        )),
                new ProductDef("kingston-nv2-pcie-4-nvme", "ssd-hdd", "kingston", "SSD Kingston NV2 PCIe 4.0 NVMe M.2",
                        "Ổ cứng SSD NVMe PCIe 4.0 giá tốt, tiết kiệm điện năng cho cả PC và laptop văn phòng.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("storage_type", "SSD NVMe", null),
                                new SpecVal("interface", "M.2 NVMe PCIe 4.0", null),
                                new SpecVal("read_speed", "3500 MB/s", 3500.0),
                                new SpecVal("write_speed", "2100 MB/s", 2100.0)
                        ),
                        List.of(
                                new VariantDef("SSD-KST-NV2-500G", "500GB NVMe", 1090000, 950000, 32, List.of(new SpecVal("capacity", "500 GB", 500.0))),
                                new VariantDef("SSD-KST-NV2-1TB", "1TB NVMe", 1790000, 1590000, 25, List.of(new SpecVal("capacity", "1000 GB", 1000.0)))
                        )),
                new ProductDef("wd-black-sn850x-nvme", "ssd-hdd", "western-digital", "SSD WD Black SN850X PCIe 4.0 NVMe M.2",
                        "Tối ưu chuyên biệt cho game thủ với chế độ Game Mode 2.0, tốc độ đọc tuần tự lên tới 7300MB/s.",
                        60, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("storage_type", "SSD NVMe", null),
                                new SpecVal("interface", "M.2 NVMe PCIe 4.0", null),
                                new SpecVal("read_speed", "7300 MB/s", 7300.0),
                                new SpecVal("write_speed", "6600 MB/s", 6600.0)
                        ),
                        List.of(
                                new VariantDef("SSD-WD-SN850X-1TB", "1TB NVMe", 2890000, 2590000, 18, List.of(new SpecVal("capacity", "1000 GB", 1000.0))),
                                new VariantDef("SSD-WD-SN850X-2TB", "2TB NVMe", 4990000, 4590000, 9, List.of(new SpecVal("capacity", "2000 GB", 2000.0)))
                        )),
                new ProductDef("kingston-kc3000-nvme", "ssd-hdd", "kingston", "SSD Kingston KC3000 PCIe 4.0 NVMe M.2",
                        "Trang bị DRAM Cache và tản nhiệt nhôm graphene mỏng, chịu tải ghi bền bỉ cho dựng phim.",
                        60, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("storage_type", "SSD NVMe", null),
                                new SpecVal("interface", "M.2 NVMe PCIe 4.0", null),
                                new SpecVal("read_speed", "7000 MB/s", 7000.0),
                                new SpecVal("write_speed", "6000 MB/s", 6000.0)
                        ),
                        List.of(
                                new VariantDef("SSD-KST-KC3000-1TB", "1TB NVMe", 2590000, 2350000, 16, List.of(new SpecVal("capacity", "1000 GB", 1000.0))),
                                new VariantDef("SSD-KST-KC3000-2TB", "2TB NVMe", 4690000, 4290000, 7, List.of(new SpecVal("capacity", "2000 GB", 2000.0)))
                        )),
                new ProductDef("crucial-t700-pcie5-nvme", "ssd-hdd", "crucial", "SSD Crucial T700 PCIe 5.0 NVMe M.2",
                        "Thế hệ SSD PCIe Gen 5 siêu tốc độ lên đến 12400MB/s dành cho nền tảng Z890/X870E.",
                        60, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("storage_type", "SSD NVMe", null),
                                new SpecVal("interface", "M.2 NVMe PCIe 5.0", null),
                                new SpecVal("read_speed", "12400 MB/s", 12400.0),
                                new SpecVal("write_speed", "11800 MB/s", 11800.0)
                        ),
                        List.of(
                                new VariantDef("SSD-CRU-T700-2TB", "2TB PCIe 5.0 Kèm Heatsink", 8490000, 7990000, 4, List.of(new SpecVal("capacity", "2000 GB", 2000.0)))
                        )),
                new ProductDef("samsung-980-pcie3-nvme", "ssd-hdd", "samsung", "SSD Samsung 980 PCIe 3.0 NVMe M.2",
                        "Giải pháp nâng cấp tiết kiệm, mát mẻ với công nghệ Intelligent TurboWrite 2.0.",
                        60, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("storage_type", "SSD NVMe", null),
                                new SpecVal("interface", "M.2 NVMe PCIe 3.0", null),
                                new SpecVal("read_speed", "3500 MB/s", 3500.0),
                                new SpecVal("write_speed", "3000 MB/s", 3000.0)
                        ),
                        List.of(
                                new VariantDef("SSD-SAM-980-500G", "500GB NVMe", 1490000, 1290000, 20, List.of(new SpecVal("capacity", "500 GB", 500.0))),
                                new VariantDef("SSD-SAM-980-1TB", "1TB NVMe", 2390000, 2150000, 14, List.of(new SpecVal("capacity", "1000 GB", 1000.0)))
                        )),
                new ProductDef("samsung-870-evo-sata", "ssd-hdd", "samsung", "SSD Samsung 870 EVO 2.5 inch SATA III",
                        "Ổ cứng SSD SATA III bền bỉ nhất thị trường, tương thích mọi PC và laptop đời cũ.",
                        60, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("storage_type", "SSD SATA", null),
                                new SpecVal("interface", "SATA III 2.5\"", null),
                                new SpecVal("read_speed", "560 MB/s", 560.0),
                                new SpecVal("write_speed", "530 MB/s", 530.0)
                        ),
                        List.of(
                                new VariantDef("SSD-SAM-870EVO-500G", "500GB SATA 2.5\"", 1590000, 1390000, 24, List.of(new SpecVal("capacity", "500 GB", 500.0))),
                                new VariantDef("SSD-SAM-870EVO-1TB", "1TB SATA 2.5\"", 2690000, 2450000, 15, List.of(new SpecVal("capacity", "1000 GB", 1000.0)))
                        )),
                new ProductDef("crucial-mx500-sata", "ssd-hdd", "crucial", "SSD Crucial MX500 2.5 inch SATA III",
                        "Tích hợp DRAM Cache và mã hóa phần cứng AES 256-bit, tốc độ ổn định lâu dài.",
                        60, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("storage_type", "SSD SATA", null),
                                new SpecVal("interface", "SATA III 2.5\"", null),
                                new SpecVal("read_speed", "560 MB/s", 560.0),
                                new SpecVal("write_speed", "510 MB/s", 510.0)
                        ),
                        List.of(
                                new VariantDef("SSD-CRU-MX500-1TB", "1TB SATA 2.5\"", 2190000, 1990000, 19, List.of(new SpecVal("capacity", "1000 GB", 1000.0)))
                        )),
                new ProductDef("wd-blue-hdd-35-sata", "ssd-hdd", "western-digital", "Ổ cứng HDD Western Digital Blue 3.5 inch SATA III",
                        "Lưu trữ dữ liệu dung lượng lớn an toàn, tiết kiệm chi phí trên mỗi GB cho PC để bàn.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("storage_type", "HDD", null),
                                new SpecVal("interface", "SATA III 3.5\"", null),
                                new SpecVal("read_speed", "215 MB/s", 215.0),
                                new SpecVal("write_speed", "200 MB/s", 200.0)
                        ),
                        List.of(
                                new VariantDef("HDD-WD-BLUE-2TB", "2TB 7200RPM", 1790000, 1590000, 22, List.of(new SpecVal("capacity", "2000 GB", 2000.0))),
                                new VariantDef("HDD-WD-BLUE-4TB", "4TB 5400RPM", 2890000, 2590000, 12, List.of(new SpecVal("capacity", "4000 GB", 4000.0)))
                        )),
                new ProductDef("seagate-barracuda-hdd-35", "ssd-hdd", "seagate", "Ổ cứng HDD Seagate Barracuda 3.5 inch SATA III",
                        "Bộ đệm 256MB Multi-Tier Caching giúp truy xuất kho phim, ảnh và dữ liệu sao lưu nhanh chóng.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("storage_type", "HDD", null),
                                new SpecVal("interface", "SATA III 3.5\"", null),
                                new SpecVal("read_speed", "220 MB/s", 220.0),
                                new SpecVal("write_speed", "210 MB/s", 210.0)
                        ),
                        List.of(
                                new VariantDef("HDD-SGT-BARA-2TB", "2TB 7200RPM", 1750000, 1550000, 26, List.of(new SpecVal("capacity", "2000 GB", 2000.0))),
                                new VariantDef("HDD-SGT-BARA-4TB", "4TB 5400RPM", 2790000, 2490000, 9, List.of(new SpecVal("capacity", "4000 GB", 4000.0)))
                        ))
        );
    }

    private static List<ProductDef> psuProducts() {
        return List.of(
                new ProductDef("corsair-rm850e-80-plus-gold", "psu", "corsair", "Nguồn Corsair RM850e 850W 80 Plus Gold Full Modular",
                        "Chuẩn ATX 3.0 & PCIe 5.0 12VHPWR dành cho RTX 4000 series, quạt 120mm chế độ Zero RPM yên tĩnh.",
                        84, "NEW_FULLBOX", "OUT_OF_STOCK",
                        List.of(
                                new SpecVal("wattage", "850 W", 850.0),
                                new SpecVal("efficiency", "80 Plus Gold", null),
                                new SpecVal("modular", "Full Modular", null),
                                new SpecVal("atx_standard", "ATX 3.0", null)
                        ),
                        List.of(new VariantDef("PSU-CRS-RM850E", "850W ATX 3.0 Đen", 3590000, 3290000, 2, List.of()))),
                new ProductDef("msi-mag-a650bn-650w", "psu", "msi", "Nguồn MSI MAG A650BN 650W 80 Plus Bronze",
                        "Nguồn máy tính bền bỉ thiết kế mạch DC-to-DC, dây bọc lưới gọn gàng cho cấu hình RTX 4060.",
                        60, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("wattage", "650 W", 650.0),
                                new SpecVal("efficiency", "80 Plus Bronze", null),
                                new SpecVal("modular", "Non-Modular", null),
                                new SpecVal("atx_standard", "ATX 2.52", null)
                        ),
                        List.of(new VariantDef("PSU-MSI-A650BN", "650W Bronze", 1490000, 1290000, 20, List.of()))),
                new ProductDef("cooler-master-mwe-550-bronze-v2", "psu", "cooler-master", "Nguồn Cooler Master MWE 550 Bronze V2 550W",
                        "Phù hợp PC văn phòng và gaming phổ thông, quạt HDB vận hành êm ái và bền bỉ.",
                        60, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("wattage", "550 W", 550.0),
                                new SpecVal("efficiency", "80 Plus Bronze", null),
                                new SpecVal("modular", "Non-Modular", null),
                                new SpecVal("atx_standard", "ATX 2.52", null)
                        ),
                        List.of(new VariantDef("PSU-CM-MWE550V2", "550W Bronze V2", 1250000, 1090000, 28, List.of()))),
                new ProductDef("corsair-cx650-80-plus-bronze", "psu", "corsair", "Nguồn Corsair CX650 650W 80 Plus Bronze",
                        "Kích thước nhỏ gọn dài 125mm dễ lắp đặt trong các vỏ case mATX, dây cáp dẹt màu đen.",
                        60, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("wattage", "650 W", 650.0),
                                new SpecVal("efficiency", "80 Plus Bronze", null),
                                new SpecVal("modular", "Non-Modular", null),
                                new SpecVal("atx_standard", "ATX 2.52", null)
                        ),
                        List.of(new VariantDef("PSU-CRS-CX650", "650W Bronze", 1590000, 1450000, 24, List.of()))),
                new ProductDef("msi-mag-a750gl-pcie5", "psu", "msi", "Nguồn MSI MAG A750GL PCIE5 750W 80 Plus Gold",
                        "Cáp 12VHPWR hai tông màu chống cắm lỏng, thiết kế Full Modular chuẩn ATX 3.0 gọn nhẹ.",
                        84, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("wattage", "750 W", 750.0),
                                new SpecVal("efficiency", "80 Plus Gold", null),
                                new SpecVal("modular", "Full Modular", null),
                                new SpecVal("atx_standard", "ATX 3.0", null)
                        ),
                        List.of(
                                new VariantDef("PSU-MSI-A750GL-BLK", "750W Gold Đen", 2790000, 2590000, 18, List.of()),
                                new VariantDef("PSU-MSI-A750GL-WHT", "750W Gold Trắng", 2990000, 2790000, 9, List.of())
                        )),
                new ProductDef("seasonic-focus-gx-750-atx3", "psu", "seasonic", "Nguồn Seasonic FOCUS GX-750 ATX 3.0 750W 80 Plus Gold",
                        "Thương hiệu nguồn hàng đầu thế giới, tụ Nhật 100%, bảo hành chính hãng 10 năm.",
                        120, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("wattage", "750 W", 750.0),
                                new SpecVal("efficiency", "80 Plus Gold", null),
                                new SpecVal("modular", "Full Modular", null),
                                new SpecVal("atx_standard", "ATX 3.0", null)
                        ),
                        List.of(new VariantDef("PSU-SSN-GX750", "750W Gold ATX 3.0", 3490000, 3290000, 14, List.of()))),
                new ProductDef("deepcool-pk750d-semi-modular", "psu", "deepcool", "Nguồn DeepCool PM750D / PK750M 750W 80 Plus Gold Semi-Modular",
                        "Thiết kế Semi-Modular tiện quản lý dây, hiệu suất 80 Plus Gold tối ưu điện năng.",
                        60, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("wattage", "750 W", 750.0),
                                new SpecVal("efficiency", "80 Plus Gold", null),
                                new SpecVal("modular", "Semi-Modular", null),
                                new SpecVal("atx_standard", "ATX 2.52", null)
                        ),
                        List.of(new VariantDef("PSU-DC-PM750", "750W Gold Semi-Modular", 2290000, 2090000, 16, List.of()))),
                new ProductDef("asus-tuf-gaming-850g-atx3", "psu", "asus", "Nguồn ASUS TUF Gaming 850W Gold ATX 3.0",
                        "Linh kiện chuẩn quân sự phủ lớp chống ẩm PCB, bảo hành 10 năm cho cấu hình RTX 4080.",
                        120, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("wattage", "850 W", 850.0),
                                new SpecVal("efficiency", "80 Plus Gold", null),
                                new SpecVal("modular", "Full Modular", null),
                                new SpecVal("atx_standard", "ATX 3.0", null)
                        ),
                        List.of(new VariantDef("PSU-ASUS-TUF850G", "850W Gold ATX 3.0", 3890000, 3590000, 11, List.of()))),
                new ProductDef("corsair-rm1000x-shift-atx3", "psu", "corsair", "Nguồn Corsair RM1000x SHIFT 1000W 80 Plus Gold",
                        "Cổng cắm dây modular xoay ngang hông độc đáo, chuẩn ATX 3.1 giúp đi dây cực kỳ dễ dàng.",
                        120, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("wattage", "1000 W", 1000.0),
                                new SpecVal("efficiency", "80 Plus Gold", null),
                                new SpecVal("modular", "Full Modular", null),
                                new SpecVal("atx_standard", "ATX 3.1", null)
                        ),
                        List.of(new VariantDef("PSU-CRS-RM1000X-S", "1000W Gold SHIFT", 5490000, 5190000, 5, List.of()))),
                new ProductDef("seasonic-vertex-px-1200-platinum", "psu", "seasonic", "Nguồn Seasonic VERTEX PX-1200 1200W 80 Plus Platinum",
                        "Công suất 1200W hiệu suất Platinum chuẩn ATX 3.1 dành cho workstation đa GPU hoặc RTX 4090.",
                        120, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("wattage", "1200 W", 1200.0),
                                new SpecVal("efficiency", "80 Plus Platinum", null),
                                new SpecVal("modular", "Full Modular", null),
                                new SpecVal("atx_standard", "ATX 3.1", null)
                        ),
                        List.of(new VariantDef("PSU-SSN-PX1200", "1200W Platinum ATX 3.1", 7990000, 7490000, 3, List.of())))
        );
    }

    private static List<ProductDef> caseProducts() {
        return List.of(
                new ProductDef("corsair-4000d-airflow", "case", "corsair", "Vỏ Case Corsair 4000D Airflow Tempered Glass",
                        "Mặt lưới thép thoáng khí tối ưu luồng gió, kính cường lực dễ tháo lắp cùng hệ thống đi dây RapidRoute.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("case_size", "Mid Tower", null),
                                new SpecVal("form_factor", "ATX / Micro-ATX / Mini-ITX", null),
                                new SpecVal("max_gpu_length", "360 mm", 360.0),
                                new SpecVal("max_cooler_height", "170 mm", 170.0)
                        ),
                        List.of(
                                new VariantDef("CASE-CRS-4000D-BLK", "Đen (Black)", 2390000, 2190000, 16, List.of(new SpecVal("color", "Đen", null))),
                                new VariantDef("CASE-CRS-4000D-WHT", "Trắng (White)", 2490000, 2290000, 4, List.of(new SpecVal("color", "Trắng", null)))
                        )),
                new ProductDef("msi-mag-forge-112r", "case", "msi", "Vỏ Case MSI MAG FORGE 112R ARGB",
                        "Trang bị sẵn 4 quạt ARGB 120mm, nút điều khiển LED tích hợp và hỗ trợ radiator 240mm.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("case_size", "Mid Tower", null),
                                new SpecVal("form_factor", "ATX / Micro-ATX / Mini-ITX", null),
                                new SpecVal("max_gpu_length", "330 mm", 330.0),
                                new SpecVal("max_cooler_height", "160 mm", 160.0),
                                new SpecVal("color", "Đen", null)
                        ),
                        List.of(new VariantDef("CASE-MSI-112R", "Đen Kèm 4 Fan ARGB", 1650000, 1450000, 19, List.of()))),
                new ProductDef("nzxt-h5-flow-rgb", "case", "nzxt", "Vỏ Case NZXT H5 Flow RGB",
                        "Thiết kế tối giản hiện đại, quạt đáy nghiêng chuyên thổi mát trực tiếp cho card đồ họa.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("case_size", "Mid Tower", null),
                                new SpecVal("form_factor", "ATX / Micro-ATX / Mini-ITX", null),
                                new SpecVal("max_gpu_length", "365 mm", 365.0),
                                new SpecVal("max_cooler_height", "165 mm", 165.0)
                        ),
                        List.of(
                                new VariantDef("CASE-NZXT-H5F-BLK", "Đen (Matte Black)", 2690000, 2490000, 14, List.of(new SpecVal("color", "Đen", null))),
                                new VariantDef("CASE-NZXT-H5F-WHT", "Trắng (Matte White)", 2790000, 2590000, 12, List.of(new SpecVal("color", "Trắng", null)))
                        )),
                new ProductDef("nzxt-h9-flow-dual-chamber", "case", "nzxt", "Vỏ Case NZXT H9 Flow Dual-Chamber",
                        "Thiết kế bể cá kính cường lực tràn viền hai mặt, khoang nguồn tách riêng hỗ trợ tới 10 quạt.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("case_size", "Dual-Chamber", null),
                                new SpecVal("form_factor", "ATX / Micro-ATX / Mini-ITX", null),
                                new SpecVal("max_gpu_length", "435 mm", 435.0),
                                new SpecVal("max_cooler_height", "165 mm", 165.0)
                        ),
                        List.of(
                                new VariantDef("CASE-NZXT-H9F-BLK", "Đen (Black)", 4390000, 4090000, 7, List.of(new SpecVal("color", "Đen", null))),
                                new VariantDef("CASE-NZXT-H9F-WHT", "Trắng (White)", 4490000, 4190000, 5, List.of(new SpecVal("color", "Trắng", null)))
                        )),
                new ProductDef("lian-li-o11-dynamic-evo-rgb", "case", "lian-li", "Vỏ Case Lian Li O11 Dynamic EVO RGB",
                        "Biểu tượng dòng case bể cá cao cấp với dải LED chữ L tinh tế, lắp được GPU dài đến 455mm.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("case_size", "Dual-Chamber", null),
                                new SpecVal("form_factor", "E-ATX / ATX / Micro-ATX", null),
                                new SpecVal("max_gpu_length", "455 mm", 455.0),
                                new SpecVal("max_cooler_height", "167 mm", 167.0)
                        ),
                        List.of(
                                new VariantDef("CASE-LL-O11EVO-BLK", "Đen (Black)", 4690000, 4390000, 8, List.of(new SpecVal("color", "Đen", null))),
                                new VariantDef("CASE-LL-O11EVO-WHT", "Trắng (White)", 4890000, 4590000, 4, List.of(new SpecVal("color", "Trắng", null)))
                        )),
                new ProductDef("lian-li-lancool-216-rgb", "case", "lian-li", "Vỏ Case Lian Li Lancool 216 RGB",
                        "Trang bị 2 quạt mặt trước siêu lớn 160mm ARGB tạo lưu lượng gió hàng đầu phân khúc Mid Tower.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("case_size", "Mid Tower", null),
                                new SpecVal("form_factor", "E-ATX / ATX / Micro-ATX", null),
                                new SpecVal("max_gpu_length", "392 mm", 392.0),
                                new SpecVal("max_cooler_height", "180 mm", 180.0),
                                new SpecVal("color", "Đen", null)
                        ),
                        List.of(new VariantDef("CASE-LL-LC216-BLK", "Đen 2 Fan 160mm ARGB", 2690000, 2490000, 13, List.of()))),
                new ProductDef("deepcool-ch370-matx", "case", "deepcool", "Vỏ Case DeepCool CH370 Micro-ATX",
                        "Nhỏ gọn chuẩn Micro-ATX nhưng vẫn lắp vừa tản AIO 360mm mặt trước và có móc treo tai nghe ẩn.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("case_size", "Mini Tower", null),
                                new SpecVal("form_factor", "Micro-ATX / Mini-ITX", null),
                                new SpecVal("max_gpu_length", "320 mm", 320.0),
                                new SpecVal("max_cooler_height", "165 mm", 165.0)
                        ),
                        List.of(
                                new VariantDef("CASE-DC-CH370-BLK", "Đen (Black)", 1390000, 1250000, 20, List.of(new SpecVal("color", "Đen", null))),
                                new VariantDef("CASE-DC-CH370-WHT", "Trắng (White)", 1450000, 1290000, 15, List.of(new SpecVal("color", "Trắng", null)))
                        )),
                new ProductDef("asus-prime-ap201-mesh", "case", "asus", "Vỏ Case ASUS Prime AP201 Micro-ATX Mesh",
                        "Toàn bộ vỏ đục lỗ lưới 360 độ siêu thoáng khí, thể tích chỉ 33L nhưng lắp được GPU 338mm.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("case_size", "Mini Tower", null),
                                new SpecVal("form_factor", "Micro-ATX / Mini-ITX", null),
                                new SpecVal("max_gpu_length", "338 mm", 338.0),
                                new SpecVal("max_cooler_height", "170 mm", 170.0),
                                new SpecVal("color", "Trắng", null)
                        ),
                        List.of(new VariantDef("CASE-ASUS-AP201-WHT", "Trắng Lưới Mesh", 1990000, 1790000, 11, List.of()))),
                new ProductDef("cooler-master-nr200p-itx", "case", "cooler-master", "Vỏ Case Cooler Master MasterBox NR200P Mini-ITX",
                        "Case SFF Mini-ITX chỉ 18.25L kèm dây riser PCIe dựng dọc VGA, phù hợp góc bàn làm việc tối giản.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("case_size", "Mini Tower", null),
                                new SpecVal("form_factor", "Mini-ITX", null),
                                new SpecVal("max_gpu_length", "330 mm", 330.0),
                                new SpecVal("max_cooler_height", "155 mm", 155.0),
                                new SpecVal("color", "Đen", null)
                        ),
                        List.of(new VariantDef("CASE-CM-NR200P-BLK", "Đen Kèm Riser", 2290000, 2050000, 9, List.of()))),
                new ProductDef("corsair-7000d-airflow-full-tower", "case", "corsair", "Vỏ Case Corsair 7000D Airflow Full Tower",
                        "Không gian khổng lồ hỗ trợ mainboard E-ATX và tới 3 radiator 360mm cho workstation chuyên nghiệp.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("case_size", "Full Tower", null),
                                new SpecVal("form_factor", "E-ATX / ATX / Micro-ATX", null),
                                new SpecVal("max_gpu_length", "450 mm", 450.0),
                                new SpecVal("max_cooler_height", "190 mm", 190.0),
                                new SpecVal("color", "Đen", null)
                        ),
                        List.of(new VariantDef("CASE-CRS-7000D-BLK", "Full Tower Đen", 6490000, 5990000, 3, List.of())))
        );
    }

    private static List<ProductDef> coolerProducts() {
        return List.of(
                new ProductDef("corsair-icue-h100i-rgb-elite", "tan-nhiet", "corsair", "Tản nhiệt nước AIO Corsair iCUE H100i RGB ELITE 240mm",
                        "AIO 240mm hiệu năng cao với 16 đèn LED RGB trên block bơm, vận hành êm ái với quạt AF ELITE.",
                        60, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("cooler_type", "Tản nhiệt nước AIO 240mm", null),
                                new SpecVal("socket_support", "LGA1700 / LGA1851 / AM5 / AM4", null),
                                new SpecVal("tdp_rating", "250 W", 250.0),
                                new SpecVal("color", "Đen", null),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(new VariantDef("COOL-CRS-H100I", "240mm Đen", 3690000, 3390000, 15, List.of()))),
                new ProductDef("asus-rog-ryuo-iii-360-argb", "tan-nhiet", "asus", "Tản nhiệt nước AIO ASUS ROG RYUO III 360 ARGB",
                        "Màn hình Anime Matrix LED độc đáo, bơm Asetek thế hệ 8 và radiator 360mm giải nhiệt đỉnh cao.",
                        72, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("cooler_type", "Tản nhiệt nước AIO 360mm", null),
                                new SpecVal("socket_support", "LGA1700 / LGA1851 / AM5", null),
                                new SpecVal("tdp_rating", "320 W", 320.0),
                                new SpecVal("color", "Đen", null),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(new VariantDef("COOL-ASUS-RYUO360", "360mm Đen", 7490000, 6990000, 3, List.of()))),
                new ProductDef("thermalright-peerless-assassin-120-se", "tan-nhiet", "thermalright", "Tản nhiệt khí Thermalright Peerless Assassin 120 SE",
                        "Tản khí tháp đôi 6 ống đồng hiệu năng/giá thành tốt nhất thị trường, cân mượt Core i7 & Ryzen 7.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("cooler_type", "Tản nhiệt khí", null),
                                new SpecVal("socket_support", "LGA1700 / LGA1851 / AM5 / AM4", null),
                                new SpecVal("tdp_rating", "245 W", 245.0),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(
                                new VariantDef("COOL-TR-PA120SE-BLK", "ARGB Đen", 950000, 850000, 32, List.of(new SpecVal("color", "Đen", null))),
                                new VariantDef("COOL-TR-PA120SE-WHT", "ARGB Trắng", 990000, 890000, 20, List.of(new SpecVal("color", "Trắng", null)))
                        )),
                new ProductDef("deepcool-ak400-digital", "tan-nhiet", "deepcool", "Tản nhiệt khí DeepCool AK400 Digital",
                        "Tích hợp màn hình LCD hiển thị nhiệt độ CPU thời gian thực trên nắp tản, 4 ống đồng gọn gàng.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("cooler_type", "Tản nhiệt khí", null),
                                new SpecVal("socket_support", "LGA1700 / AM5 / AM4", null),
                                new SpecVal("tdp_rating", "220 W", 220.0),
                                new SpecVal("color", "Đen", null),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(new VariantDef("COOL-DC-AK400D", "AK400 Digital Đen", 1150000, 990000, 26, List.of()))),
                new ProductDef("deepcool-ak620-zero-dark", "tan-nhiet", "deepcool", "Tản nhiệt khí DeepCool AK620 Zero Dark",
                        "Tháp đôi phủ đen toàn phần cực ngầu, 6 ống đồng và 2 quạt FDB 120mm độ ồn thấp.",
                        36, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("cooler_type", "Tản nhiệt khí", null),
                                new SpecVal("socket_support", "LGA1700 / LGA1851 / AM5 / AM4", null),
                                new SpecVal("tdp_rating", "260 W", 260.0),
                                new SpecVal("color", "Đen", null),
                                new SpecVal("led_rgb", "Không", null)
                        ),
                        List.of(new VariantDef("COOL-DC-AK620-ZD", "Zero Dark Đen", 1790000, 1590000, 18, List.of()))),
                new ProductDef("noctua-nh-d15-chromax-black", "tan-nhiet", "noctua", "Tản nhiệt khí Noctua NH-D15 chromax.black",
                        "Huyền thoại tản nhiệt khí siêu êm của Áo với 2 quạt NF-A15 140mm và bảo hành 6 năm.",
                        72, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("cooler_type", "Tản nhiệt khí", null),
                                new SpecVal("socket_support", "LGA1700 / LGA1851 / AM5 / AM4", null),
                                new SpecVal("tdp_rating", "250 W", 250.0),
                                new SpecVal("color", "Đen", null),
                                new SpecVal("led_rgb", "Không", null)
                        ),
                        List.of(new VariantDef("COOL-NOC-NHD15-BLK", "chromax.black", 3190000, 2950000, 8, List.of()))),
                new ProductDef("cooler-master-hyper-212-halo", "tan-nhiet", "cooler-master", "Tản nhiệt khí Cooler Master Hyper 212 Halo",
                        "Quạt MF120 Halo2 ARGB hai vòng sáng rực rỡ, dễ lắp đặt cho CPU Intel i3/i5 và Ryzen 5.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("cooler_type", "Tản nhiệt khí", null),
                                new SpecVal("socket_support", "LGA1700 / AM5 / AM4", null),
                                new SpecVal("tdp_rating", "180 W", 180.0),
                                new SpecVal("color", "Đen", null),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(new VariantDef("COOL-CM-H212-HALO", "Halo Black ARGB", 890000, 790000, 29, List.of()))),
                new ProductDef("nzxt-kraken-240-rgb", "tan-nhiet", "nzxt", "Tản nhiệt nước AIO NZXT Kraken 240 RGB",
                        "Màn hình LCD 1.54 inch tùy biến GIF/nhiệt độ qua phần mềm NZXT CAM, hiệu năng ổn định.",
                        72, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("cooler_type", "Tản nhiệt nước AIO 240mm", null),
                                new SpecVal("socket_support", "LGA1700 / LGA1851 / AM5 / AM4", null),
                                new SpecVal("tdp_rating", "260 W", 260.0),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(
                                new VariantDef("COOL-NZXT-K240-BLK", "240mm Đen", 4390000, 4090000, 10, List.of(new SpecVal("color", "Đen", null))),
                                new VariantDef("COOL-NZXT-K240-WHT", "240mm Trắng", 4490000, 4190000, 7, List.of(new SpecVal("color", "Trắng", null)))
                        )),
                new ProductDef("deepcool-ls720-se-360", "tan-nhiet", "deepcool", "Tản nhiệt nước AIO DeepCool LS720 SE 360mm",
                        "Bơm thế hệ 4 tốc độ 3100 RPM kèm công nghệ chống rò rỉ Anti-Leak độc quyền của DeepCool.",
                        60, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("cooler_type", "Tản nhiệt nước AIO 360mm", null),
                                new SpecVal("socket_support", "LGA1700 / LGA1851 / AM5 / AM4", null),
                                new SpecVal("tdp_rating", "300 W", 300.0),
                                new SpecVal("color", "Đen", null),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(new VariantDef("COOL-DC-LS720SE", "360mm ARGB Đen", 2990000, 2690000, 14, List.of()))),
                new ProductDef("lian-li-galahad-ii-trinity-360", "tan-nhiet", "lian-li", "Tản nhiệt nước AIO Lian Li Galahad II Trinity SL-INF 360",
                        "Đi kèm 3 quạt gương vô cực UNI FAN SL-INF 120 tuyệt đẹp, 3 nắp bơm thay đổi linh hoạt.",
                        60, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("cooler_type", "Tản nhiệt nước AIO 360mm", null),
                                new SpecVal("socket_support", "LGA1700 / LGA1851 / AM5 / AM4", null),
                                new SpecVal("tdp_rating", "310 W", 310.0),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(
                                new VariantDef("COOL-LL-GA2-360-WHT", "360mm Trắng SL-INF", 4990000, 4690000, 5, List.of(new SpecVal("color", "Trắng", null)))
                        ))
        );
    }

    private static List<ProductDef> accessoryProducts() {
        return List.of(
                new ProductDef("keychron-k2-pro-qmk-via", "phu-kien", "keychron", "Bàn phím cơ Keychron K2 Pro QMK/VIA Wireless",
                        "Layout 75% nhỏ gọn, hot-swap 5 pin, keycap PBT Double-shot và kết nối Bluetooth 5.1 / USB-C.",
                        12, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("accessory_type", "Bàn phím cơ", null),
                                new SpecVal("connection_type", "Không dây 2.4GHz / Bluetooth", null),
                                new SpecVal("compatibility", "Windows / macOS", null),
                                new SpecVal("color", "Đen", null),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(
                                new VariantDef("ACC-KEY-K2PRO-RED", "K Pro Red Switch RGB", 2690000, 2450000, 18, List.of()),
                                new VariantDef("ACC-KEY-K2PRO-BRN", "K Pro Brown Switch RGB", 2690000, 2450000, 14, List.of())
                        )),
                new ProductDef("logitech-g-pro-x-tkl-lightspeed", "phu-kien", "logitech", "Bàn phím cơ Logitech G PRO X TKL LIGHTSPEED",
                        "Chuẩn thi đấu eSports không dây độ trễ siêu thấp 1ms, keycap PBT và hộp đựng chống sốc đi kèm.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("accessory_type", "Bàn phím cơ", null),
                                new SpecVal("connection_type", "Không dây 2.4GHz / Bluetooth", null),
                                new SpecVal("compatibility", "PC / Mac", null),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(
                                new VariantDef("ACC-LOG-GPROXTKL-BLK", "Đen Tactile Switch", 4490000, 4190000, 9, List.of(new SpecVal("color", "Đen", null))),
                                new VariantDef("ACC-LOG-GPROXTKL-WHT", "Trắng Linear Switch", 4490000, 4190000, 6, List.of(new SpecVal("color", "Trắng", null)))
                        )),
                new ProductDef("logitech-g-pro-x-superlight-2", "phu-kien", "logitech", "Chuột gaming Logitech G PRO X SUPERLIGHT 2",
                        "Trọng lượng siêu nhẹ 60g, cảm biến HERO 2 32.000 DPI, switch quang cơ LIGHTFORCE và pin 95 giờ.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("accessory_type", "Chuột gaming", null),
                                new SpecVal("connection_type", "Không dây 2.4GHz", null),
                                new SpecVal("compatibility", "PC / Mac", null),
                                new SpecVal("led_rgb", "Không", null)
                        ),
                        List.of(
                                new VariantDef("ACC-LOG-GPX2-BLK", "Đen (Black)", 3690000, 3390000, 15, List.of(new SpecVal("color", "Đen", null))),
                                new VariantDef("ACC-LOG-GPX2-WHT", "Trắng (White)", 3690000, 3390000, 12, List.of(new SpecVal("color", "Trắng", null)))
                        )),
                new ProductDef("logitech-g304-lightspeed", "phu-kien", "logitech", "Chuột không dây Logitech G304 LIGHTSPEED",
                        "Chuột không dây quốc dân cảm biến HERO 12.000 DPI, dùng 1 pin AA lên đến 250 giờ chơi game.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("accessory_type", "Chuột gaming", null),
                                new SpecVal("connection_type", "Không dây 2.4GHz", null),
                                new SpecVal("compatibility", "PC / Mac", null),
                                new SpecVal("led_rgb", "Không", null)
                        ),
                        List.of(
                                new VariantDef("ACC-LOG-G304-BLK", "Đen (Black)", 990000, 790000, 40, List.of(new SpecVal("color", "Đen", null))),
                                new VariantDef("ACC-LOG-G304-WHT", "Trắng (White)", 990000, 820000, 28, List.of(new SpecVal("color", "Trắng", null)))
                        )),
                new ProductDef("corsair-hs65-surround", "phu-kien", "corsair", "Tai nghe gaming Corsair HS65 SURROUND",
                        "Âm thanh vòm Dolby Audio 7.1 qua adapter USB, công nghệ cá nhân hóa âm thanh SoundID nhẹ 282g.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("accessory_type", "Tai nghe gaming", null),
                                new SpecVal("connection_type", "Có dây USB", null),
                                new SpecVal("compatibility", "PC / Mac", null),
                                new SpecVal("color", "Đen", null),
                                new SpecVal("led_rgb", "Không", null)
                        ),
                        List.of(new VariantDef("ACC-CRS-HS65-BLK", "Carbon Đen", 1990000, 1690000, 19, List.of()))),
                new ProductDef("lian-li-uni-fan-sl-inf-120-pack3", "phu-kien", "lian-li", "Bộ 3 quạt Case Lian Li UNI FAN SL-INF 120 ARGB (Kèm Hub)",
                        "Quạt khớp nối không dây giảm tối đa dây cáp, hiệu ứng gương vô cực hai mặt và hub điều khiển L-Connect 3.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("accessory_type", "Quạt tản nhiệt", null),
                                new SpecVal("connection_type", "PWM 4-Pin / ARGB 5V", null),
                                new SpecVal("compatibility", "Case hỗ trợ fan 120mm", null),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(
                                new VariantDef("ACC-LL-SLINF120-BLK", "Pack 3 Đen + Hub", 2490000, 2290000, 12, List.of(new SpecVal("color", "Đen", null))),
                                new VariantDef("ACC-LL-SLINF120-WHT", "Pack 3 Trắng + Hub", 2590000, 2390000, 4, List.of(new SpecVal("color", "Trắng", null)))
                        )),
                new ProductDef("noctua-nt-h2-thermal-paste", "phu-kien", "noctua", "Keo tản nhiệt Noctua NT-H2 (Tuýp 3.5g)",
                        "Hiệu suất dẫn nhiệt cao, không dẫn điện an toàn tuyệt đối cho CPU & GPU, kèm 3 khăn lau NA-CW1.",
                        12, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("accessory_type", "Keo tản nhiệt", null),
                                new SpecVal("connection_type", "Không dùng kết nối", null),
                                new SpecVal("compatibility", "Mọi CPU / GPU", null),
                                new SpecVal("color", "Xám", null),
                                new SpecVal("led_rgb", "Không", null)
                        ),
                        List.of(new VariantDef("ACC-NOC-NTH2-35G", "Tuýp 3.5g", 350000, 290000, 38, List.of()))),
                new ProductDef("lian-li-strimer-plus-v2-24pin", "phu-kien", "lian-li", "Cáp nguồn nối dài Lian Li Strimer Plus V2 24-Pin ARGB",
                        "120 bóng LED ARGB siêu sáng đồng bộ Mainboard, nâng tầm thẩm mỹ cho dàn PC kính cường lực.",
                        24, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("accessory_type", "Cáp nguồn nối dài", null),
                                new SpecVal("connection_type", "PWM 4-Pin / ARGB 5V", null),
                                new SpecVal("compatibility", "Mọi nguồn ATX 24-Pin", null),
                                new SpecVal("color", "Trắng", null),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(new VariantDef("ACC-LL-STRIMER-24P", "24-Pin + Controller", 1690000, 1490000, 10, List.of()))),
                new ProductDef("asus-rog-herculx-gpu-holder", "phu-kien", "asus", "Giá đỡ card màn hình ASUS ROG Herculx ARGB",
                        "Chống xệ card màn hình RTX 4080/4090 nặng, điều chỉnh chiều cao 72–128mm kèm thước thủy cân bằng.",
                        12, "NEW_FULLBOX", "ACTIVE",
                        List.of(
                                new SpecVal("accessory_type", "Giá đỡ GPU", null),
                                new SpecVal("connection_type", "PWM 4-Pin / ARGB 5V", null),
                                new SpecVal("compatibility", "Mọi Case ATX", null),
                                new SpecVal("color", "Đen", null),
                                new SpecVal("led_rgb", "Có", null)
                        ),
                        List.of(new VariantDef("ACC-ASUS-HERCULX", "ROG Herculx ARGB", 1090000, 950000, 15, List.of()))),
                new ProductDef("logitech-g640-mousepad-legacy", "phu-kien", "logitech", "Lót chuột Logitech G640 Large Cloth (Bản cũ ngưng bán)",
                        "Bề mặt vải ma sát trung bình kích thước 460x400mm — bản lưu kho ngưng bán phục vụ kiểm thử trạng thái INACTIVE.",
                        12, "NEW_FULLBOX", "INACTIVE",
                        List.of(
                                new SpecVal("accessory_type", "Lót chuột", null),
                                new SpecVal("connection_type", "Không dùng kết nối", null),
                                new SpecVal("compatibility", "PC / Mac", null),
                                new SpecVal("color", "Đen", null),
                                new SpecVal("led_rgb", "Không", null)
                        ),
                        List.of(new VariantDef("ACC-LOG-G640-OLD", "Size L 460x400mm", 790000, 590000, 5, List.of())))
        );
    }
}
