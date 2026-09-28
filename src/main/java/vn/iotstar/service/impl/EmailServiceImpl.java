package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.iotstar.service.EmailService;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtp(String email, String otp, String subject) {
        log.info("==================================================");
        log.info("[OTP NOTIFICATION] Sending OTP to: {}", email);
        log.info("[OTP CODE]: {}", otp);
        log.info("==================================================");

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject(subject);
            message.setText("""
                Xin chào,

                Mã OTP của bạn là: %s

                OTP có hiệu lực trong 5 phút và chỉ sử dụng một lần.
                Không chia sẻ mã này cho người khác.

                Trân trọng,
                IOTSTAR SHOP Team
                """.formatted(otp));

            mailSender.send(message);
            log.info("Đã gửi email OTP thành công tới {}", email);
        } catch (Exception e) {
            log.warn("Không thể gửi email qua SMTP: {}. Mã OTP để kiểm thử là: {}", e.getMessage(), otp);
        }
    }
}
