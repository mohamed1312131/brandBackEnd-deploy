package com.example.hamzabackend.service;

import com.example.hamzabackend.entity.NewsletterSubscription;
import com.example.hamzabackend.repository.NewsletterSubscriptionRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;

@Service
public class NewsletterService {

    private final NewsletterSubscriptionRepository repository;
    private final JavaMailSender mailSender;

    public NewsletterService(NewsletterSubscriptionRepository repository, JavaMailSender mailSender) {
        this.repository = repository;
        this.mailSender = mailSender;
    }

    public void subscribe(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be empty");
        }

        boolean alreadyExists = repository.existsByEmail(email.trim().toLowerCase());

        if (!alreadyExists) {
            try {
                repository.save(new NewsletterSubscription(email.trim().toLowerCase()));
            } catch (Exception e) {
                if (e.getMessage().contains("duplicate key")) {
                    // Already exists, safe to ignore
                } else {
                    throw e;
                }
            }
        }
    }

    public List<NewsletterSubscription> getAllSubscribers() {
        return repository.findAll();
    }

    public void unsubscribe(String email) {
        repository.deleteById(email);
    }

    public void sendEmailToSubscribers(List<String> emails, String subject, String title, String subtitle, String body, String imageUrl, String productLink) {
        for (String email : emails) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(email);
                helper.setSubject(subject);
                helper.setFrom("your-email@gmail.com"); // ✅ use a configured SMTP email

                String html = """
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <style>
    body {
      margin: 0; padding: 0; background-color: #f5f5f5;
      font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
    }
    .container {
      max-width: 600px; margin: auto; background: #fff; border-radius: 8px;
      overflow: hidden; box-shadow: 0 5px 15px rgba(0, 0, 0, 0.05);
    }
    .header {
      padding: 40px 30px 10px; text-align: center;
    }
    .header img {
      max-width: 100%%; border-radius: 6px;
    }
    .subtitle {
      text-transform: uppercase; font-size: 12px; color: #999; margin-top: 20px;
    }
    .title {
      font-size: 32px; font-weight: bold; color: #111; margin: 10px 0;
    }
    .content {
      padding: 0 30px 30px; color: #333; font-size: 15px; line-height: 1.6;
    }
    .cta-button {
      display: inline-block; background-color: #111; color: #fff !important;
      padding: 14px 26px; text-decoration: none; border-radius: 6px; margin-top: 30px;
    }
    .footer {
      text-align: center; font-size: 12px; color: #aaa; padding: 20px;
    }
  </style>
</head>
<body>
  <div class="container">
    <div class="header">
      %s
      <div class="subtitle">%s</div>
      <div class="title">%s</div>
    </div>
    <div class="content">
      <p>%s</p>
      %s
    </div>
    <div class="footer">
      &copy; %d Your Brand. All rights reserved.
    </div>
  </div>
</body>
</html>
""".formatted(
                        (imageUrl != null && !imageUrl.isBlank()) ? "<img src='" + imageUrl + "' alt='Product'>" : "",
                        subtitle != null ? subtitle : "",
                        title != null ? title : "",
                        body != null ? body : "",
                        (productLink != null && !productLink.isBlank()) ? "<a href='" + productLink + "' class='cta-button'>View Product</a>" : "",
                        java.time.Year.now().getValue()
                );

                helper.setText(html, true);
                mailSender.send(message);

            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

}
