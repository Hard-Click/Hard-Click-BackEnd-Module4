package com.wanted.backend.domain.identity.infrastructure.persistence;

import com.wanted.backend.domain.identity.application.port.EmailSendPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSendAdapter implements EmailSendPort {

    private final JavaMailSender mailSender; // (Spring Mail 스타터가 제공하는 객체)

    @Override
    @Async("notificationExecutor")
    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[Hard-Click] 이메일 인증 번호 안내");
        message.setText("안녕하세요. Hard-Click 서비스입니다.\n\n" +
                "인증 번호는 [" + code + "] 입니다.\n" +
                "3분 이내에 입력해 주세요.");

        try {
            mailSender.send(message);
            log.info("인증 메일 발송 성공: to={}", to);
        } catch (MailException e) {
            log.error("인증 메일 발송 실패: to={}", to, e);
        }
    }
}