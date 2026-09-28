package com.sfms.iam.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Gửi email giao dịch — BR-003 (đặt lại MK), BR-010/BR-011 (kích hoạt), UC-003 (thông báo
 * vô hiệu hóa).
 *
 * <p>Phạm vi MF01 dừng ở <i>"đã gửi 1 email tới đúng địa chỉ"</i> (TestSpec §3 TDS-01);
 * nội dung/template thuộc Email Service. Trong test, {@code JavaMailSender} trỏ GreenMail
 * in-memory ({@code FX-GREENMAIL}), không bao giờ gửi ra ngoài.
 *
 * <p>SEC-012: <b>không log token thô</b>. Chuỗi token chỉ nằm trong thân email.
 *
 * <p>⚠️ {@code MAIL_FROM} — Gmail SMTP thật BẮT BUỘC "From" khớp (hoặc là alias đã verify)
 * với chính tài khoản Gmail dùng để login SMTP ({@code SMTP_USER}), không thì Gmail từ chối
 * gửi hoặc tự ghi đè From. Mặc định dưới CHỈ đúng khi có Google Workspace domain riêng.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String from;

    public EmailService(JavaMailSender mailSender, @Value("${sfms.mail.from:no-reply@sfsm.io.vn}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    /** BR-011 — link kích hoạt, hiệu lực 48h. */
    public void sendActivationEmail(String to, String recipientName, String rawToken) {
        send(to, "[SFMS] Kích hoạt tài khoản",
                """
                Xin chào %s,

                Tài khoản SFMS của bạn đã được tạo. Vui lòng truy cập liên kết bên dưới để đặt mật khẩu và kích hoạt tài khoản:
                https://sfsm.io.vn/activate-account?token=%s

                (Mã token kích hoạt: %s)

                Liên kết có hiệu lực trong 48 giờ (BR-011).
                """.formatted(recipientName, rawToken, rawToken));
    }

    /** BR-003 — link đặt lại mật khẩu, hiệu lực 1 giờ. */
    public void sendPasswordResetEmail(String to, String recipientName, String rawToken) {
        send(to, "[SFMS] Đặt lại mật khẩu",
                """
                Xin chào %s,

                Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản của bạn.
                Vui lòng truy cập liên kết bên dưới để đặt lại mật khẩu:
                https://sfsm.io.vn/reset-password?token=%s

                (Mã token đặt lại: %s)

                Liên kết có hiệu lực trong 1 giờ (BR-003). Nếu bạn không yêu cầu, hãy bỏ qua email này.
                """.formatted(recipientName, rawToken, rawToken));
    }

    /** UC-003 Postconditions — <i>"An email notification is sent to the user"</i>. */
    public void sendAccountDeactivatedEmail(String to, String recipientName) {
        send(to, "[SFMS] Tài khoản đã bị vô hiệu hóa",
                """
                Xin chào %s,

                Tài khoản SFMS của bạn vừa bị vô hiệu hóa bởi quản trị viên.
                Vui lòng liên hệ quản trị viên của trường nếu cần hỗ trợ.
                """.formatted(recipientName));
    }

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
        } catch (MailException e) {
            // Email hỏng KHÔNG được làm rollback nghiệp vụ (tài khoản đã tạo vẫn phải tồn tại,
            // người dùng có thể yêu cầu gửi lại). Chỉ log — và log KHÔNG chứa thân email.
            log.error("Không gửi được email tới {} (subject={})", to, subject, e);
        }
    }
}
