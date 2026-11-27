package vn.soopishop.seed;

import org.junit.jupiter.api.Test;
import vn.soopishop.seed.CatalogSeedData.BrandDef;
import vn.soopishop.seed.CatalogSeedData.CatDef;
import vn.soopishop.seed.CatalogSeedData.ProductDef;
import vn.soopishop.seed.CatalogSeedData.SpecAttrDef;
import vn.soopishop.seed.CatalogSeedData.SpecVal;
import vn.soopishop.seed.CatalogSeedData.VariantDef;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogSeedDataTest {

    private static final Set<String> REQUIRED_CATEGORIES = Set.of(
            "cpu", "gpu", "mainboard", "ram", "ssd-hdd", "psu", "case", "tan-nhiet", "phu-kien"
    );

    @Test
    void coversAllNineHardwareCategoriesWithRichProductsAndValidSpecs() {
        List<CatDef> categories = CatalogSeedData.categories();
        Set<String> catSlugs = categories.stream().map(CatDef::slug).collect(Collectors.toSet());
        assertEquals(REQUIRED_CATEGORIES, catSlugs, "Must cover all 9 required PC hardware categories");

        List<BrandDef> brands = CatalogSeedData.brands();
        Set<String> brandSlugs = brands.stream().map(BrandDef::slug).collect(Collectors.toSet());
        assertEquals(brands.size(), brandSlugs.size(), "Brand slugs must be unique");
        assertTrue(brands.size() >= 18, "Should have diverse real brands");

        Map<String, SpecAttrDef> specAttrByKey = new HashMap<>();
        Map<String, Integer> attrCountPerCat = new HashMap<>();
        for (SpecAttrDef sa : CatalogSeedData.specAttributes()) {
            assertTrue(catSlugs.contains(sa.cat()), "Unknown category on spec attr: " + sa.cat());
            String key = sa.cat() + ":" + sa.code();
            assertFalse(specAttrByKey.containsKey(key), "Duplicate spec attribute: " + key);
            specAttrByKey.put(key, sa);
            attrCountPerCat.merge(sa.cat(), 1, Integer::sum);
        }
        for (String slug : REQUIRED_CATEGORIES) {
            assertTrue(attrCountPerCat.getOrDefault(slug, 0) >= 4,
                    "Category '" + slug + "' must have at least 4 filterable spec attributes");
        }

        List<ProductDef> products = CatalogSeedData.allProducts();
        assertEquals(90, products.size(), "Expected 90 products (10 per category)");

        Set<String> productSlugs = new HashSet<>();
        Set<String> variantSkus = new HashSet<>();
        Map<String, Integer> productCountPerCat = new HashMap<>();
        int totalVariants = 0;

        for (ProductDef p : products) {
            assertTrue(productSlugs.add(p.slug()), "Duplicate product slug: " + p.slug());
            assertTrue(catSlugs.contains(p.cat()), "Unknown category on product: " + p.slug());
            assertTrue(brandSlugs.contains(p.brand()), "Unknown brand on product: " + p.slug());
            assertTrue(p.warranty() >= 0, "Negative warranty on product: " + p.slug());
            assertTrue(Set.of("NEW_FULLBOX", "TRAY").contains(p.condition()), "Invalid condition: " + p.condition());
            assertTrue(Set.of("ACTIVE", "OUT_OF_STOCK", "INACTIVE").contains(p.status()), "Invalid status: " + p.status());
            assertFalse(p.variants().isEmpty(), "Product must have at least 1 variant: " + p.slug());
            productCountPerCat.merge(p.cat(), 1, Integer::sum);

            Set<String> prodSpecCodes = new HashSet<>();
            for (SpecVal s : p.specs()) {
                assertTrue(prodSpecCodes.add(s.code()), "Duplicate product-level spec " + s.code() + " on " + p.slug());
                SpecAttrDef attr = specAttrByKey.get(p.cat() + ":" + s.code());
                assertNotNull(attr, "Unmapped spec code " + s.code() + " on product " + p.slug());
                if ("NUMBER".equals(attr.type())) {
                    assertNotNull(s.num(), "NUMBER spec must provide numeric value: " + p.slug() + ":" + s.code());
                }
            }

            for (VariantDef v : p.variants()) {
                totalVariants++;
                assertTrue(variantSkus.add(v.sku()), "Duplicate SKU: " + v.sku());
                assertTrue(v.listPrice() >= 0 && v.salePrice() >= 0 && v.salePrice() <= v.listPrice(),
                        "Invalid prices on SKU: " + v.sku());
                assertTrue(v.initialStock() >= 0, "Negative initial stock on SKU: " + v.sku());

                Set<String> varSpecCodes = new HashSet<>();
                for (SpecVal vs : v.specs()) {
                    assertTrue(varSpecCodes.add(vs.code()), "Duplicate variant-level spec " + vs.code() + " on " + v.sku());
                    SpecAttrDef attr = specAttrByKey.get(p.cat() + ":" + vs.code());
                    assertNotNull(attr, "Unmapped variant spec code " + vs.code() + " on " + v.sku());
                    if ("NUMBER".equals(attr.type())) {
                        assertNotNull(vs.num(), "NUMBER variant spec must provide numeric value: " + v.sku() + ":" + vs.code());
                    }
                }
                int effectiveSpecCount = prodSpecCodes.size() + varSpecCodes.size();
                assertTrue(effectiveSpecCount >= 4, "SKU " + v.sku() + " should have at least 4 specifications");
            }
        }

        for (String slug : REQUIRED_CATEGORIES) {
            assertEquals(10, productCountPerCat.getOrDefault(slug, 0),
                    "Category '" + slug + "' must have 10 products");
        }
        assertTrue(totalVariants >= 120, "Expected at least 120 SKU variants, got " + totalVariants);
    }

    @Test
    void coversKeyFacetedSearchAndStatusScenarios() {
        List<ProductDef> products = CatalogSeedData.allProducts();

        // RAM has both DDR4 and DDR5
        Set<String> ramTypes = products.stream()
                .filter(p -> "ram".equals(p.cat()))
                .flatMap(p -> p.specs().stream())
                .filter(s -> "ram_type".equals(s.code()))
                .map(SpecVal::text)
                .collect(Collectors.toSet());
        assertTrue(ramTypes.containsAll(Set.of("DDR4", "DDR5")));

        // Storage has SSD NVMe, SSD SATA, and HDD
        Set<String> storageTypes = products.stream()
                .filter(p -> "ssd-hdd".equals(p.cat()))
                .flatMap(p -> p.specs().stream())
                .filter(s -> "storage_type".equals(s.code()))
                .map(SpecVal::text)
                .collect(Collectors.toSet());
        assertTrue(storageTypes.containsAll(Set.of("SSD NVMe", "SSD SATA", "HDD")));

        // GPU has both NVIDIA and AMD
        Set<String> gpuChipsets = products.stream()
                .filter(p -> "gpu".equals(p.cat()))
                .flatMap(p -> p.specs().stream())
                .filter(s -> "gpu_chipset".equals(s.code()))
                .map(SpecVal::text)
                .collect(Collectors.toSet());
        assertTrue(gpuChipsets.containsAll(Set.of("NVIDIA GeForce", "AMD Radeon")));

        // Cooler has Air Cooler and AIO
        Set<String> coolerTypes = products.stream()
                .filter(p -> "tan-nhiet".equals(p.cat()))
                .flatMap(p -> p.specs().stream())
                .filter(s -> "cooler_type".equals(s.code()))
                .map(SpecVal::text)
                .collect(Collectors.toSet());
        assertTrue(coolerTypes.contains("Tản nhiệt khí"));
        assertTrue(coolerTypes.stream().anyMatch(t -> t.contains("AIO")));

        // Statuses include ACTIVE, OUT_OF_STOCK, and INACTIVE
        Set<String> statuses = products.stream().map(ProductDef::status).collect(Collectors.toSet());
        assertEquals(Set.of("ACTIVE", "OUT_OF_STOCK", "INACTIVE"), statuses);
    }
}
