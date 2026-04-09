package dev.nishants.appraisal.services;

public interface EmailService {


    void sendEmail(String to, String subject, String body);

    void sendHtmlEmail(String to, String subject, String htmlBody);
}
