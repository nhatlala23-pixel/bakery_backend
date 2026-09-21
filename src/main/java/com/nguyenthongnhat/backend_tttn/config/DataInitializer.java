package com.nguyenthongnhat.backend_tttn.config;

import com.nguyenthongnhat.backend_tttn.entity.*;
import com.nguyenthongnhat.backend_tttn.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ContactSettingRepository contactSettingRepository;
    private final ProductRepository productRepository;
    private final BannerRepository bannerRepository;
    private final GalleryRepository galleryRepository;
    private final BlogRepository blogRepository;
    private final BrandRepository brandRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        seedRoles();
        seedAdminUser();
        seedBrands();
        seedCategories();
        seedContactSetting();
        seedBanners();
        seedGalleries();
        seedProductsAndBlogs();
    }

    private void seedRoles() {
        List<String> roleNames = Arrays.asList("ADMIN", "USER", "STAFF");
        List<String> descriptions = Arrays.asList(
                "Quản trị viên hệ thống",
                "Khách hàng mua sắm",
                "Nhân viên quản lý kho và xử lý đơn hàng"
        );

        for (int i = 0; i < roleNames.size(); i++) {
            String roleName = roleNames.get(i);
            String description = descriptions.get(i);

            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                Role role = Role.builder()
                        .roleName(roleName)
                        .description(description)
                        .build();
                roleRepository.save(role);
                log.info("Successfully seeded role: {}", roleName);
            }
        }
    }

    private void seedBrands() {
        if (brandRepository.findByBrandName("GẤU").isEmpty()) {
            Brand brand = Brand.builder()
                    .brandName("GẤU")
                    .slug("gau")
                    .country("Vietnam")
                    .description("Gấu Bakery")
                    .status(1)
                    .build();
            brandRepository.save(brand);
            log.info("Successfully seeded brand GẤU");
        }
    }

    private void seedAdminUser() {
        String adminEmail = "admin@gaubakery.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            Role adminRole = roleRepository.findByRoleName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN role not found"));

            User admin = User.builder()
                    .fullName("Gấu Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin123"))
                    .phone("0987654321")
                    .address("123 Đường Ba Tháng Hai, Quận 10, TP. Hồ Chí Minh")
                    .role(adminRole)
                    .status(1)
                    .build();

            userRepository.save(admin);
            log.info("Successfully seeded admin user: {}", adminEmail);
        }
    }

    private void seedCategories() {
        List<String> names = Arrays.asList("Bánh kem", "Bánh ngọt", "Bánh mặn", "Combo");
        List<String> slugs = Arrays.asList("banh-kem", "banh-ngot", "banh-man", "combo");
        List<String> descriptions = Arrays.asList(
                "Những chiếc bánh dành cho sinh nhật, kỷ niệm và những ngày đặc biệt.",
                "Thơm ngon ngọt ngào, phù hợp cho bữa trà chiều thư giãn.",
                "Đậm đà hương vị, sự kết hợp hoàn hảo từ bột và thịt, phô mai.",
                "Sự kết hợp tuyệt vời giữa các loại bánh bán chạy nhất với giá ưu đãi."
        );

        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            String slug = slugs.get(i);
            String description = descriptions.get(i);

            if (categoryRepository.findBySlug(slug).isEmpty()) {
                Category category = Category.builder()
                        .categoryName(name)
                        .slug(slug)
                        .description(description)
                        .status(1)
                        .build();
                categoryRepository.save(category);
                log.info("Successfully seeded category: {}", name);
            }
        }
    }

    private void seedContactSetting() {
        if (contactSettingRepository.count() == 0) {
            ContactSetting setting = ContactSetting.builder()
                    .zaloUrl("https://zalo.me/0987654321")
                    .facebookUrl("https://facebook.com/gaubakery")
                    .instagramUrl("https://instagram.com/gaubakery")
                    .tiktokUrl("https://tiktok.com/@gaubakery")
                    .hotline("0987654321")
                    .email("contact@gaubakery.com")
                    .address("123 Đường Ba Tháng Hai, Quận 10, TP. Hồ Chí Minh")
                    .googleMaps("<iframe src=\"https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d3919.460228996504!2d106.66531391483562!3d10.776019492321855!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x3919.460228996504!2zMTIzIMSQxrDhu51uZyBCYSBUaMOhbmcgSGFpLCBRdeG6rW4gMTAsIEjhu5MgQ2jDrSBNaW5o!5e0!3m2!1svi!2svn!4v1629000000000!5m2!1svi!2svn\" width=\"100%\" height=\"450\" style=\"border:0;\" allowfullscreen=\"\" loading=\"lazy\"></iframe>")
                    .openingHours("08:00 - 22:00 (Mỗi ngày)")
                    .logoUrl("https://images.unsplash.com/photo-1509440159596-0249088772ff?w=150&auto=format&fit=crop&q=60")
                    .build();
            contactSettingRepository.save(setting);
            log.info("Successfully seeded ContactSetting");
        }
    }

    private void seedBanners() {
        if (bannerRepository.count() == 0) {
            Banner banner1 = Banner.builder()
                    .title("Ngọt ngào cho những khoảnh khắc đáng nhớ.")
                    .subtitle("GẤU BAKERY")
                    .description("Những chiếc bánh được làm bằng sự tận tâm, nguyên liệu được chọn lọc và dành riêng cho những khoảnh khắc đặc biệt của bạn.")
                    .imageUrl("https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=1200&auto=format&fit=crop&q=80")
                    .buttonText("Xem bộ sưu tập")
                    .buttonLink("/products")
                    .sortOrder(1)
                    .active(true)
                    .build();

            Banner banner2 = Banner.builder()
                    .title("Hương vị tự nhiên, tình yêu đong đầy.")
                    .subtitle("SẢN PHẨM MỚI")
                    .description("Mỗi ngày tại Gấu là một câu chuyện ngọt ngào được kể bằng hương vị của các loại bánh tươi nóng hổi.")
                    .imageUrl("https://images.unsplash.com/photo-1517433456452-f9633a875f6f?w=1200&auto=format&fit=crop&q=80")
                    .buttonText("Khám phá ngay")
                    .buttonLink("/products")
                    .sortOrder(2)
                    .active(true)
                    .build();

            bannerRepository.saveAll(Arrays.asList(banner1, banner2));
            log.info("Successfully seeded Banners");
        }
    }

    private void seedGalleries() {
        if (galleryRepository.count() == 0) {
            List<Gallery> items = Arrays.asList(
                    Gallery.builder().title("Bếp lò Gấu").imageUrl("https://images.unsplash.com/photo-1556910103-1c02745aae4d?w=800&auto=format&fit=crop&q=80").category("KITCHEN").displayOrder(1).active(true).build(),
                    Gallery.builder().title("Không gian ấm cúng").imageUrl("https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800&auto=format&fit=crop&q=80").category("STORE").displayOrder(2).active(true).build(),
                    Gallery.builder().title("Bánh kem dâu tươi").imageUrl("https://images.unsplash.com/photo-1464349172961-10442a8b643d?w=800&auto=format&fit=crop&q=80").category("PRODUCT").displayOrder(3).active(true).build(),
                    Gallery.builder().title("Sự kiện khai trương").imageUrl("https://images.unsplash.com/photo-1511795409834-ef04bbd61622?w=800&auto=format&fit=crop&q=80").category("EVENT").displayOrder(4).active(true).build(),
                    Gallery.builder().title("Khách hàng thân thiết").imageUrl("https://images.unsplash.com/photo-1543007630-9710e4a00a20?w=800&auto=format&fit=crop&q=80").category("CUSTOMER").displayOrder(5).active(true).build()
            );
            galleryRepository.saveAll(items);
            log.info("Successfully seeded Galleries");
        }
    }

    private void seedProductsAndBlogs() {
        if (productRepository.count() == 0) {
            Category banhKem = categoryRepository.findBySlug("banh-kem").orElse(null);
            Category banhNgot = categoryRepository.findBySlug("banh-ngot").orElse(null);
            Category banhMan = categoryRepository.findBySlug("banh-man").orElse(null);
            Category combo = categoryRepository.findBySlug("combo").orElse(null);

            User admin = userRepository.findByEmail("admin@gaubakery.com").orElse(null);
            Brand gauBrand = brandRepository.findByBrandName("GẤU").orElse(null);

            if (banhKem != null) {
                Product p1 = Product.builder()
                        .sku("KEM-001")
                        .productName("Strawberry Fresh Cake")
                        .slug("strawberry-fresh-cake")
                        .originalPrice(BigDecimal.valueOf(350000))
                        .salePrice(BigDecimal.valueOf(320000))
                        .stock(50)
                        .thumbnail("https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600&auto=format&fit=crop&q=80")
                        .shortDescription("Bánh kem dâu tây tươi mát lạnh với lớp kem whipping cao cấp và dâu tây Đà Lạt tươi rói ngọt lịm.")
                        .description("Chiếc bánh sinh nhật dâu tây là một trong những sản phẩm đặc trưng bán chạy nhất tại Gấu Bakery. Lớp cốt bánh chiffon mềm xốp kết hợp với mứt dâu tây chua nhẹ và lớp kem whipping cheese béo ngậy tạo nên hương vị khó quên.")
                        .ingredients("Bột mỳ hữu cơ, Dâu tây tươi Đà Lạt, Whipping cream Pháp, Đường organic, Trứng gà ta.")
                        .size("Đường kính 18cm, Cao 8cm")
                        .preservation("Bảo quản tủ mát nhiệt độ từ 2 - 6 độ C trong vòng 48 giờ.")
                        .isBestseller(true)
                        .isFeatured(true)
                        .rating(BigDecimal.valueOf(4.9))
                        .category(banhKem)
                        .brand(gauBrand)
                        .status(1)
                        .build();

                Product p2 = Product.builder()
                        .sku("KEM-002")
                        .productName("Premium Chocolate Cake")
                        .slug("premium-chocolate-cake")
                        .originalPrice(BigDecimal.valueOf(380000))
                        .salePrice(BigDecimal.valueOf(350000))
                        .stock(30)
                        .thumbnail("https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=600&auto=format&fit=crop&q=80")
                        .shortDescription("Bánh kem socola nguyên chất đắng nhẹ, sang trọng và lôi cuốn.")
                        .description("Sự kết hợp hoàn hảo từ cốt bánh gato cacao và lớp ganache socola đen béo ngậy, mịn màng làm hài lòng cả những thực khách sành ăn nhất.")
                        .ingredients("Socola đen Bỉ 70%, Bột cacao nguyên chất, Kem sữa tươi tươi, Trứng gà, Bơ nhạt.")
                        .size("Đường kính 16cm, Cao 9cm")
                        .preservation("Giữ trong ngăn mát tủ lạnh, sử dụng tốt nhất trong 3 ngày.")
                        .isBestseller(true)
                        .isFeatured(false)
                        .rating(BigDecimal.valueOf(4.8))
                        .category(banhKem)
                        .brand(gauBrand)
                        .status(1)
                        .build();

                Product p3 = Product.builder()
                        .sku("KEM-003")
                        .productName("Matcha Tiramisu Cake")
                        .slug("matcha-tiramisu-cake")
                        .originalPrice(BigDecimal.valueOf(340000))
                        .salePrice(BigDecimal.valueOf(340000))
                        .stock(40)
                        .thumbnail("https://images.unsplash.com/photo-1536680465769-2365207b035e?w=600&auto=format&fit=crop&q=80")
                        .shortDescription("Sự giao thoa tinh tế giữa Tiramisu truyền thống Ý và trà xanh Uji Matcha Nhật Bản.")
                        .description("Vị béo ngậy của kem phô mai mascarpone quyện cùng hương thơm đặc trưng, chát nhẹ tinh tế của bột trà xanh matcha cao cấp.")
                        .ingredients("Phô mai Mascarpone Ý, Bột trà xanh Matcha Uji Nhật Bản, Cốt bánh ladyfinger, Trứng.")
                        .size("Hộp vuông 12cm x 12cm")
                        .preservation("Bảo quản đông mát từ 0 - 4 độ C. Dùng trong 24 giờ.")
                        .isBestseller(false)
                        .isFeatured(true)
                        .rating(BigDecimal.valueOf(4.7))
                        .category(banhKem)
                        .brand(gauBrand)
                        .status(1)
                        .build();

                productRepository.saveAll(Arrays.asList(p1, p2, p3));
            }

            if (banhNgot != null) {
                Product p4 = Product.builder()
                        .sku("NGOT-001")
                        .productName("French Butter Croissant")
                        .slug("french-butter-croissant")
                        .originalPrice(BigDecimal.valueOf(45000))
                        .salePrice(BigDecimal.valueOf(38000))
                        .stock(100)
                        .thumbnail("https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=600&auto=format&fit=crop&q=80")
                        .shortDescription("Bánh sừng bò ngàn lớp thơm lừng bơ Pháp cao cấp, giòn tan bên ngoài, mềm xốp bên trong.")
                        .description("Chiếc bánh sừng bò được làm theo công thức truyền thống Pháp. Từng lớp bột được cán mỏng cuộn tròn khéo léo với bơ lạt nguyên chất tạo nên độ nở hoàn hảo.")
                        .ingredients("Bột mỳ Pháp, Bơ lạt Normandy, Men tự nhiên, Sữa tươi, Đường.")
                        .size("Tiêu chuẩn 80g / cái")
                        .preservation("Ngon nhất khi dùng trong ngày. Có thể nướng lại bằng nồi chiên không dầu 160 độ trong 3 phút.")
                        .isBestseller(true)
                        .isFeatured(true)
                        .rating(BigDecimal.valueOf(4.9))
                        .category(banhNgot)
                        .brand(gauBrand)
                        .status(1)
                        .build();

                Product p5 = Product.builder()
                        .sku("NGOT-002")
                        .productName("Salted Egg Yolks Pastry")
                        .slug("salted-egg-yolks-pastry")
                        .originalPrice(BigDecimal.valueOf(35000))
                        .salePrice(BigDecimal.valueOf(30000))
                        .stock(120)
                        .thumbnail("https://images.unsplash.com/photo-1509440159596-0249088772ff?w=600&auto=format&fit=crop&q=80")
                        .shortDescription("Bánh pía trứng muối tan chảy béo ngậy thơm ngon ngọt dịu.")
                        .description("Vỏ bánh nhiều lớp mỏng ôm trọn nhân đậu xanh nhuyễn mịn kết hợp cùng lòng đỏ trứng muối chín mềm tan chảy kích thích vị giác.")
                        .ingredients("Bột mỳ, Trứng muối ta, Nhân đậu xanh, Đường, Dầu ăn.")
                        .size("Cái tròn 70g")
                        .preservation("Bảo quản nhiệt độ thường từ 3 - 5 ngày.")
                        .isBestseller(false)
                        .isFeatured(false)
                        .rating(BigDecimal.valueOf(4.6))
                        .category(banhNgot)
                        .brand(gauBrand)
                        .status(1)
                        .build();

                productRepository.saveAll(Arrays.asList(p4, p5));
            }

            if (banhMan != null) {
                Product p6 = Product.builder()
                        .sku("MAN-001")
                        .productName("Sausage and Cheese Bread")
                        .slug("sausage-and-cheese-bread")
                        .originalPrice(BigDecimal.valueOf(48000))
                        .salePrice(BigDecimal.valueOf(42000))
                        .stock(80)
                        .thumbnail("https://images.unsplash.com/photo-1549931319-a545dcf3bc73?w=600&auto=format&fit=crop&q=80")
                        .shortDescription("Bánh mỳ mặn nhân xúc xích Đức thượng hạng phủ phô mai Mozzarella chảy dai ngon.")
                        .description("Sự lựa chọn tuyệt vời cho bữa ăn sáng hoặc bữa xế năng lượng. Cốt bánh mỳ mềm mại cuộn tròn xúc xích và phủ đầy xốt mayo, tương cà cùng phô mai nướng thơm nức.")
                        .ingredients("Bột mỳ, Xúc xích Đức, Phô mai Mozzarella, Sốt mayonnaise, Hành lá.")
                        .size("Dài 15cm")
                        .preservation("Ăn ngay hoặc bảo quản ngăn mát tủ lạnh 2 ngày. Hâm nóng trước khi ăn.")
                        .isBestseller(true)
                        .isFeatured(true)
                        .rating(BigDecimal.valueOf(4.8))
                        .category(banhMan)
                        .brand(gauBrand)
                        .status(1)
                        .build();

                productRepository.saveAll(Arrays.asList(p6));
            }

            // Seed 3 blog posts
            if (admin != null && blogRepository.count() == 0) {
                Blog b1 = Blog.builder()
                        .title("5 mẫu bánh sinh nhật được yêu thích nhất mùa cưới 2026")
                        .slug("5-mau-banh-sinh-nhat-yeu-thich-2026")
                        .thumbnail("https://images.unsplash.com/photo-1535141192574-5d4897c13636?w=600&auto=format&fit=crop&q=80")
                        .excerpt("Tổng hợp những xu hướng thiết kế bánh kem sang trọng, tối giản nhưng vô cùng cuốn hút của năm nay tại Gấu Bakery.")
                        .content("<p>Mùa cưới và mùa lễ hội năm 2026 chứng kiến sự lên ngôi của các mẫu bánh kem phong cách tối giản (minimalist) và sang trọng kiểu Pháp. Không còn quá nhiều chi tiết sặc sỡ, thay vào đó là sự tinh tế trong cách phối màu Pastel và sử dụng hoa tươi trang trí...</p><p>Tại Gấu Bakery, chúng tôi luôn cập nhật những xu hướng thiết kế mới nhất để tạo ra những tác phẩm nghệ thuật ngọt ngào cho ngày vui của bạn.</p>")
                        .author(admin)
                        .status("PUBLISHED")
                        .publishedDate(LocalDateTime.now())
                        .seoTitle("5 mẫu bánh sinh nhật đẹp xu hướng năm 2026 | Gấu Bakery")
                        .seoDescription("Tổng hợp các mẫu bánh cưới, bánh sinh nhật phong cách minimalist và sang trọng tại Gấu Bakery.")
                        .build();

                Blog b2 = Blog.builder()
                        .title("Mẹo bảo quản bánh kem tươi lâu tại nhà không bị khô cốt bánh")
                        .slug("meo-bao-quan-banh-kem-tuoi-lau-khong-bi-kho")
                        .thumbnail("https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600&auto=format&fit=crop&q=80")
                        .excerpt("Tìm hiểu cách giữ cho chiếc bánh kem của bạn luôn mát lạnh, lớp kem mịn màng và cốt bánh ẩm xốp suốt nhiều ngày.")
                        .content("<p>Bánh kem tươi ngon nhất là khi thưởng thức ngay sau khi làm xong. Tuy nhiên, nếu bạn chưa dùng hết chiếc bánh, việc bảo quản đúng cách là vô cùng quan trọng để bánh không bị khô cốt hoặc kem bị đông cứng...</p><p>1. Luôn để bánh trong hộp kín để ngăn mùi tủ lạnh.<br/>2. Nhiệt độ bảo quản lý tưởng là 2 - 6 độ C.<br/>3. Không để bánh kem gần các thực phẩm tươi sống có mùi nồng.</p>")
                        .author(admin)
                        .status("PUBLISHED")
                        .publishedDate(LocalDateTime.now().minusDays(2))
                        .seoTitle("Cách bảo quản bánh kem tươi lâu tại nhà | Gấu Bakery")
                        .seoDescription("Hướng dẫn cách giữ bánh kem tươi ngon, kem không bị chảy hay cốt bánh bị khô khi cất giữ tủ lạnh.")
                        .build();

                Blog b3 = Blog.builder()
                        .title("Câu chuyện thương hiệu Gấu Bakery - Tinh túy từ đôi bàn tay Việt")
                        .slug("cau-chuyen-thuong-hieu-gau-bakery")
                        .thumbnail("https://images.unsplash.com/photo-1556910103-1c02745aae4d?w=600&auto=format&fit=crop&q=80")
                        .excerpt("Hành trình từ một tiệm bánh thủ công nhỏ đến biểu tượng ngọt ngào trọn vị hạnh phúc của hàng ngàn khách hàng.")
                        .content("<p>Gấu Bakery ra đời từ tình yêu vô hạn với những chiếc bánh nướng thơm lừng trong căn bếp nhỏ. Chúng tôi tin rằng, mỗi chiếc bánh trao đi không chỉ chứa đựng hương vị ngọt ngào, mà còn gửi gắm cả sự quan tâm, chân thành và lời chúc hạnh phúc...</p><p>Mỗi nguyên liệu tại Gấu đều được tuyển lựa nghiêm ngặt từ các trang trại organic và nhà cung cấp bơ sữa hàng đầu châu Âu.</p>")
                        .author(admin)
                        .status("PUBLISHED")
                        .publishedDate(LocalDateTime.now().minusDays(5))
                        .seoTitle("Hành trình câu chuyện thương hiệu Gấu Bakery")
                        .seoDescription("Khám phá hành trình xây dựng thương hiệu bánh kem, bánh ngọt Gấu Bakery - sự tận tâm trong từng chiếc bánh.")
                        .build();

                blogRepository.saveAll(Arrays.asList(b1, b2, b3));
                log.info("Successfully seeded Products and Blogs");
            }
        }
    }
}
