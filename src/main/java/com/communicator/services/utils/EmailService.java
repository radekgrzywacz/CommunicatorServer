package com.communicator.services.utils;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class EmailService {

    @Value("${application.mail.service.from}")
    private String from;
    @Value("${application.mail.service.password}")
    private String password;
    @Value("${application.mail.service.host}")
    private String host;
    @Value("${application.mail.service.port}")
    private int port;

    /**
     * Method to send an email via gmail smpt server.
     *
     * @param to      From client. User's email adress
     * @param subject From higher method. ex. "Account verification", "Password reset"
     * @param content Email content.
     * @param title Email title.
     * @param code Verification code.
     * @return returns boolean that indicates if sending an email succeeded.
     */
    public boolean sendEmail(final String to, final String subject, final String title,
                             final String content, final String code) {
        String messageTemplate = """
                <head>
                    <style>
                        body {
                            background-color: #D9D9D9;
                            display: flex;
                            flex-direction: column;
                            margin: 0;
                            padding: 0;
                        }
                        #title, h1 {
                            text-align: center;
                            font-size: xx-large;
                            font-weight: 900;
                            padding: 10px;
                            margin-left: auto;
                            margin-right: auto;
                            color: #222831;
                        }
                        #content {
                            text-align: center;
                            margin: 20px;
                            justify-content: center;
                            font-size: medium;
                            color: #222831;
                        }
                        #verification-code {
                            text-align: center;
                            font-size: xx-large;
                            font-weight: 900;
                            padding: 20px;
                            margin: 20px auto;
                            color: rgb(133, 148, 228);
                            background-color: #D9D9D9;
                            border-radius: 20px;
                            width: 40%%;
                        }
                    </style>
                </head>
                <body>
                    <div id="title">
                        <h1>%s</h1>
                    </div>
                    <div id="content">
                        %s
                        <div id="verification-code">
                            %s
                        </div>
                    </div>
                    <div id="footer"></div>
                </body>
                </html>
                """;
        String msg = String.format(messageTemplate, title, content, code);

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);


        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        };

        Session session = Session.getInstance(props, auth);
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(msg, "text/html");
            Transport.send(message);
            return true;
        } catch (MessagingException e) {
            return false;
        }
    }

}
