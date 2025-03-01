package greenart.trade.mebmer.service;

import org.springframework.stereotype.Service;

import javax.mail.*;
import java.util.Properties;

@Service
public class MailService {

    private static final String SENDER_EMAIL = "dieneid13@gmail.com";
    private static final String SENDER_PASSWORD = "";
    private static int verificationCode;

    /**
     * 인증 번호 생성
     */
    private static void createVerificationCode() {
        verificationCode = (int) (Math.random() * 90000) + 100000; // 6자리 숫자 생성
    }

    /**
     * 이메일 전송 및 인증 번호 반환
     */
    public int sendMail(String recipientEmail) {
        createVerificationCode(); // 인증 번호 생성

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            javax.mail.internet.MimeMessage message = new javax.mail.internet.MimeMessage(session);
            message.setFrom(new javax.mail.internet.InternetAddress(SENDER_EMAIL));
            message.setRecipient(Message.RecipientType.TO, new javax.mail.internet.InternetAddress(recipientEmail));
            message.setSubject("이메일 인증입니다.");
            message.setText("<h1>인증 번호: " + verificationCode + "</h1>", "UTF-8", "html");

            Transport.send(message); // 이메일 전송
            System.out.println("메일 전송 성공! 인증 번호: " + verificationCode);
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new RuntimeException("메일 전송 실패: " + e.getMessage(), e);
        }

        return verificationCode; // 생성된 인증 번호 반환
    }
}
