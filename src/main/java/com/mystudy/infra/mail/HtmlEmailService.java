package com.mystudy.infra.mail;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.mystudy.infra.config.AppProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("dev1")
@Component
@RequiredArgsConstructor
public class HtmlEmailService implements EmailService {

	private final JavaMailSender javaMailSender;
	private final AppProperties appProperties;

	@Override
	public void sendEmail(EmailMessage eMailMessage) {
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		try {
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
			mimeMessageHelper.setFrom(appProperties.getMailFrom());
			mimeMessageHelper.setTo(eMailMessage.getTo());
			mimeMessageHelper.setSubject(eMailMessage.getSubject());
			mimeMessageHelper.setText(eMailMessage.getMessage(), true);
			javaMailSender.send(mimeMessage);
			log.info("sent email: {}", eMailMessage.getMessage());
		} catch (MessagingException e) {
			log.error("fail to send email", e);
			throw new RuntimeException(e);
		}

	}

}
