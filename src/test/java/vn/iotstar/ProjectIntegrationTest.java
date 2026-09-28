package vn.iotstar;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.OtpTokenRepository;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.security.CustomUserDetailsService;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.ProductService;
import vn.iotstar.service.UserService;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProjectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OtpTokenRepository otpTokenRepository;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Kiểm tra khởi tạo dữ liệu mẫu (Admin, Demo User, Roles, Products)")
    void testInitialData() {
        assertTrue(roleRepository.findByNameIgnoreCase("ROLE_ADMIN").isPresent(), "Role ADMIN phải tồn tại");
        assertTrue(roleRepository.findByNameIgnoreCase("ROLE_USER").isPresent(), "Role USER phải tồn tại");

        Optional<User> admin = userRepository.findByEmailIgnoreCase("trungnh@hcmute.edu.vn");
        assertTrue(admin.isPresent(), "Admin trungnh@hcmute.edu.vn phải tồn tại");
        assertEquals("admin", admin.get().getUsername());

        Optional<User> user01 = userRepository.findByUsernameIgnoreCase("user01");
        assertTrue(user01.isPresent(), "User user01 phải tồn tại");
        assertEquals("Nguyễn Hữu Trung", user01.get().getFullName());

        assertTrue(productRepository.count() > 0, "Phải có sản phẩm mẫu");
    }

    @Test
    @DisplayName("Ví dụ 1 & 2: Đăng nhập bằng Email và Username")
    void testLoginWithEmailAndUsername() throws Exception {
        // Đăng nhập bằng Email admin
        mockMvc.perform(formLogin("/login").user("trungnh@hcmute.edu.vn").password("123456"))
                .andExpect(authenticated().withUsername("admin"));

        // Đăng nhập bằng Username admin
        mockMvc.perform(formLogin("/login").user("admin").password("123456"))
                .andExpect(authenticated().withUsername("admin"));

        // Đăng nhập bằng Username user01
        mockMvc.perform(formLogin("/login").user("user01").password("123456"))
                .andExpect(authenticated().withUsername("user01"));

        // Đăng nhập bằng Email user01
        mockMvc.perform(formLogin("/login").user("user01@gmail.com").password("123456"))
                .andExpect(authenticated().withUsername("user01"));

        // Đăng nhập sai mật khẩu
        mockMvc.perform(formLogin("/login").user("admin").password("wrongpass"))
                .andExpect(unauthenticated());
    }

    @Test
    @DisplayName("Ví dụ 2: CustomUserDetails nạp đúng Fullname, Images và Role")
    void testCustomUserDetails() {
        CustomUserDetails details = (CustomUserDetails) userDetailsService.loadUserByUsername("user01");
        assertNotNull(details);
        assertEquals("Nguyễn Hữu Trung", details.getFullName());
        assertEquals("/images/user.png", details.getImages());
        assertEquals("ROLE_USER", details.getRole());
    }

    @Test
    @DisplayName("MapStruct: Kiểm tra mapping UserMapper và ProductMapper")
    void testMapStruct() {
        Role role = roleRepository.findByNameIgnoreCase("ROLE_USER").orElseThrow();
        User testUser = User.builder()
                .id(999L)
                .username("testmap")
                .email("testmap@gmail.com")
                .fullName("Test Mapper")
                .images("/images/test.png")
                .role(role)
                .enabled(true)
                .build();

        UserDTO userDto = userMapper.toDTO(testUser);
        assertEquals("testmap", userDto.getUsername());
        assertEquals("ROLE_USER", userDto.getRoleName());
        assertEquals(role.getId(), userDto.getRoleId());

        Product product = Product.builder()
                .id(888L)
                .name("Tai nghe Bluetooth")
                .price(new BigDecimal("500000.00"))
                .description("Chống ồn chủ động")
                .user(testUser)
                .build();

        ProductDTO productDto = productMapper.toDTO(product);
        assertEquals("Tai nghe Bluetooth", productDto.getName());
        assertEquals(999L, productDto.getUserId());
        assertEquals("testmap", productDto.getUsername());
    }

    @Test
    @DisplayName("Ví dụ 3: Đăng ký tài khoản + Xác thực OTP")
    void testRegisterAndOtpVerification() {
        String testEmail = "newuser_test@example.com";
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser_test");
        dto.setEmail(testEmail);
        dto.setFullName("New User Test");
        dto.setPassword("123456");
        dto.setConfirmPassword("123456");

        authService.register(dto);

        User saved = userRepository.findByEmailIgnoreCase(testEmail).orElseThrow();
        assertFalse(saved.isEnabled(), "Tài khoản vừa đăng ký phải chưa được kích hoạt (enabled=false)");

        // Set known OTP hash for testing
        OtpToken otpToken = otpTokenRepository.findTopByEmailAndTypeOrderByCreatedAtDesc(testEmail, "REGISTER").orElseThrow();
        otpToken.setOtpHash(passwordEncoder.encode("654321"));
        otpTokenRepository.save(otpToken);

        boolean verified = authService.verifyRegister(testEmail, "654321");
        assertTrue(verified, "Xác thực OTP phải thành công");

        User activeUser = userRepository.findByEmailIgnoreCase(testEmail).orElseThrow();
        assertTrue(activeUser.isEnabled(), "Tài khoản sau khi verify OTP phải có enabled=true");
    }

    @Test
    @DisplayName("Ví dụ 3: Quên mật khẩu và Reset mật khẩu qua OTP")
    void testForgotPasswordAndReset() {
        String testEmail = "forgot_test@example.com";
        Role role = roleRepository.findByNameIgnoreCase("ROLE_USER").orElseThrow();
        User user = User.builder()
                .username("forgot_user")
                .email(testEmail)
                .fullName("Forgot User")
                .password(passwordEncoder.encode("oldPassword"))
                .role(role)
                .enabled(true)
                .build();
        userRepository.save(user);

        authService.forgotPassword(testEmail);

        // Set known OTP hash for testing
        OtpToken otpToken = otpTokenRepository.findTopByEmailAndTypeOrderByCreatedAtDesc(testEmail, "RESET_PASSWORD").orElseThrow();
        otpToken.setOtpHash(passwordEncoder.encode("987654"));
        otpTokenRepository.save(otpToken);

        boolean otpValid = authService.verifyResetOtp(testEmail, "987654");
        assertTrue(otpValid, "Mã OTP reset password phải hợp lệ");

        authService.resetPassword(testEmail, "newPassword123");

        User updatedUser = userRepository.findByEmailIgnoreCase(testEmail).orElseThrow();
        assertTrue(passwordEncoder.matches("newPassword123", updatedUser.getPassword()), "Mật khẩu mới phải khớp");
    }

    @Test
    @DisplayName("Ví dụ 3: Quản lý User (CRUD, Search, Pagination, Thống kê)")
    void testUserCrudAndCount() {
        long initialCount = userService.countUsers();
        assertTrue(initialCount >= 2, "Số lượng user ban đầu tối thiểu là 2 (Admin và user01)");

        var page = userService.findAll("admin", 0, 10);
        assertTrue(page.getTotalElements() >= 1, "Tìm kiếm theo từ khóa 'admin' phải có kết quả");
    }

    @Test
    @DisplayName("Ví dụ 3: Quản lý Product (CRUD, Search, Pagination, Thống kê)")
    void testProductCrudAndCount() {
        long productCount = productService.countProducts();
        assertTrue(productCount >= 2, "Số lượng sản phẩm ban đầu tối thiểu là 2");

        var page = productService.findAll("Oppo", 0, 10);
        assertTrue(page.getTotalElements() >= 1, "Tìm kiếm theo từ khóa 'Oppo' phải có kết quả");
    }

    @Test
    @DisplayName("Public endpoints accessible without login")
    void testPublicEndpoints() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/login")).andExpect(status().isOk());
        mockMvc.perform(get("/register")).andExpect(status().isOk());
        mockMvc.perform(get("/forgot-password")).andExpect(status().isOk());
    }
}
