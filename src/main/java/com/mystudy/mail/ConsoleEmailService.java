package com.mystudy.mail;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile("local")
@Component
public class ConsoleEmailService implements EmailService {

	@Override
	public void sendEmail(EmailMessage eMailMessage) {
		log.info("sent email:{}", eMailMessage.getMessage());
		System.out.println("ConsoleEmailService sent email:" + eMailMessage.getMessage());
	}

}
