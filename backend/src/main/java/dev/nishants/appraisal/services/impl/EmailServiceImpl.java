package dev.nishants.appraisal.services.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import dev.nishants.appraisal.services.EmailService;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
  private final JavaMailSender mailSender;

  @Value("${spring.mail.username}")
  private String fromEmail;

  @Override
  public void sendEmail(String to, String subject, String body) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(body, false); // false = plain text

      mailSender.send(message);
      log.info("Email sent to: {}", to);

    } catch (MessagingException e) {
      log.error("Failed to send email to {}: {}", to, e.getMessage());
      throw new IllegalStateException("Failed to send email", e);
    }
  }

  @Override
  public void sendHtmlEmail(String to, String subject, String htmlBody) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(fromEmail);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlBody, true); // true = HTML

      mailSender.send(message);
      log.info("HTML email sent to: {}", to);

    } catch (MessagingException e) {
      log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
      throw new IllegalStateException("Failed to send HTML email", e);
    }
  }
}
