package com.edunest.backend.nhacno;

import com.edunest.backend.util.CheDuLieu;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Gửi Email bằng JavaMailSender. Bean JavaMailSender chỉ tồn tại khi có
 * spring.mail.host;
 * nếu không, kênh chạy giả lập (ghi log) để môi trường dev/CI không cần SMTP.
 */
@Component
public class EmailKenhNhacNo implements KenhNhacNo {

    private static final Logger log = LoggerFactory.getLogger(EmailKenhNhacNo.class);

    private final ObjectProvider<JavaMailSender> mailSender;
    private final NhacNoProperties props;

    public EmailKenhNhacNo(ObjectProvider<JavaMailSender> mailSender, NhacNoProperties props) {
        this.mailSender = mailSender;
        this.props = props;
    }

    @Override
    public KenhGui kenh() {
        return KenhGui.EMAIL;
    }

    @Override
    public Optional<String> nguoiNhanDaChe(ThongTinNhac t) {
        if (t.email() == null || t.email().isBlank())
            return Optional.empty();
        return Optional.of(CheDuLieu.email(t.email().trim()));
    }

    @Override
    public void gui(ThongTinNhac t) {
        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender == null) {
            log.info("[GIẢ LẬP EMAIL] hoaDon={} -> {} | {}", t.hoaDonId(), CheDuLieu.email(t.email()), t.tieuDe());
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(props.email().tuDiaChi());
        msg.setTo(t.email().trim());
        msg.setSubject(t.tieuDe());
        msg.setText(t.noiDung());
        sender.send(msg); // MailException (RuntimeException) nếu lỗi -> NhacNoService ghi trạng thái LOI
    }
}