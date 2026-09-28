package vn.iotstar.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;

import java.math.BigDecimal;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder,
            @Value("${ADMIN_EMAIL:trungnh@hcmute.edu.vn}") String adminEmail,
            @Value("${ADMIN_PASSWORD:123456}") String adminPassword) {

        return args -> {
            // 1. Initialize Roles
            Role userRole = roleRepository.findByNameIgnoreCase("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

            Role adminRole = roleRepository.findByNameIgnoreCase("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

            // 2. Initialize Admin User
            if (!userRepository.existsByEmailIgnoreCase(adminEmail)) {
                User admin = User.builder()
                        .username("admin")
                        .email(adminEmail.toLowerCase())
                        .fullName("System Administrator")
                        .password(passwordEncoder.encode(adminPassword))
                        .images("/images/avatar-default.png")
                        .role(adminRole)
                        .enabled(true)
                        .build();
                userRepository.save(admin);
                log.info("Initialized Admin account: {} / {}", adminEmail, adminPassword);
            }

            // 3. Initialize Demo User (user01) from Example 2
            if (!userRepository.existsByUsernameIgnoreCase("user01")) {
                User user01 = User.builder()
                        .username("user01")
                        .email("user01@gmail.com")
                        .fullName("Nguyễn Hữu Trung")
                        .password(passwordEncoder.encode("123456"))
                        .images("/images/user.png")
                        .role(userRole)
                        .enabled(true)
                        .build();
                userRepository.save(user01);
                log.info("Initialized Demo User: user01 / 123456");

                // 4. Initialize sample products for user01
                if (productRepository.count() == 0) {
                    productRepository.save(Product.builder()
                            .name("Điện thoại Oppo A95")
                            .description("Màn hình AMOLED 6.43 inch, RAM 8GB, Pin 5000mAh sạc nhanh 33W")
                            .price(new BigDecimal("6500000.00"))
                            .imageUrl(null)
                            .user(user01)
                            .build());

                    productRepository.save(Product.builder()
                            .name("Điện thoại Oppo A6")
                            .description("Thiết kế thời thượng, camera AI sắc nét, hiệu năng ổn định")
                            .price(new BigDecimal("4890000.00"))
                            .imageUrl(null)
                            .user(user01)
                            .build());

                    log.info("Initialized sample products");
                }
            }
        };
    }
}
