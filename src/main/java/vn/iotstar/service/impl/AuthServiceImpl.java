package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.AuthService;
import vn.iotstar.service.OtpService;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        if (userRepository.existsByUsernameIgnoreCase(dto.getUsername())) {
            throw new IllegalArgumentException("Username đã tồn tại");
        }
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không đúng");
        }

        Role role = roleRepository.findByNameIgnoreCase("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail().toLowerCase())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .images("/images/avatar-default.png")
                .enabled(false)
                .role(role)
                .build();

        userRepository.save(user);
        otpService.sendRegisterOtp(dto.getEmail());
    }

    @Override
    @Transactional
    public boolean verifyRegister(String email, String otp) {
        boolean ok = otpService.verifyRegisterOtp(email, otp);
        if (!ok) {
            return false;
        }

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email: " + email));
        user.setEnabled(true);
        userRepository.save(user);
        return true;
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        if (!userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("Email không tồn tại trong hệ thống");
        }
        otpService.sendResetPasswordOtp(email);
    }

    @Override
    public boolean verifyResetOtp(String email, String otp) {
        return otpService.verifyResetPasswordOtp(email, otp);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String password) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại trong hệ thống"));
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }
}
