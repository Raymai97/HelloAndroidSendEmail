package com.raymai97.helloandroidsendemail;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * It is quite difficult to send email using Gmail. Google is too skepticism to non-Google apps
 * trying to login Google account without using OAuth 2.0.
 * To use Gmail to send email, you must enable Less Secure App Access in Account Settings.
 * If you hit error "Please log in via your web browser..." then visit below URL to allow access
 * https://accounts.google.com/b/0/DisplayUnlockCaptcha
 */

public class SimpleSmtpMailSender {
    private static final String TAG = SimpleSmtpMailSender.class.getSimpleName();

    public static class Cfg {
        @NonNull
        public String smtpHost = "";
        public int smtpPort;
        @NonNull
        public String smtpUserName = "";
        @NonNull
        public String smtpPassword = "";

        /**
         * Custom value for the "From" field in the email.
         * If null, it will use {@code smtpUserName}.
         */
        @Nullable
        public String emailFrom;

        /**
         * If true, attempt to authenticate the user using the AUTH command.
         */
        public boolean wantAuth;

        /**
         * If true, enable SSL. May not compatible with StartTLS.
         */
        public boolean wantSslEnable;

        /**
         * If true, enable StartTLS. May not compatible with SSL.
         */
        public boolean wantStartTlsEnable;
    }

    @NonNull
    public Cfg getGmailTlsCfg() {
        final Cfg cfg = new Cfg();
        cfg.wantAuth = true;
        cfg.wantStartTlsEnable = true;
        cfg.smtpHost = "smtp.gmail.com";
        cfg.smtpPort = 587;
        return cfg;
    }

    public void send(
            @NonNull final Cfg cfg,
            @NonNull final String emailTo,
            @NonNull final String emailSubject,
            @Nullable final String emailBodyText,
            @Nullable final File[] attachmentFiles
    ) throws MessagingException, IOException {
        Log.d(TAG, "send: cfg.smtpHost = " + cfg.smtpHost + ":" + cfg.smtpPort);
        Log.d(TAG, "send: cfg.smtpUserName = " + cfg.smtpUserName);
        Log.d(TAG, "send: emailTo = " + emailTo);
        Log.d(TAG, "send: emailSubject = " + emailSubject);
        final Properties properties = new Properties();
        properties.put("mail.smtp.host", cfg.smtpHost);
        properties.put("mail.smtp.port", cfg.smtpPort);
        if (cfg.wantAuth) {
            properties.put("mail.smtp.auth", "true");
        }
        if (cfg.wantSslEnable) {
            properties.put("mail.smtp.ssl.enable", "true");
        }
        if (cfg.wantStartTlsEnable) {
            properties.put("mail.smtp.starttls.enable", "true");
        }
        final MimeMessage mimeMessage = new MimeMessage(Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(cfg.smtpUserName, cfg.smtpPassword);
            }
        }));
        if (cfg.emailFrom != null) {
            mimeMessage.setFrom(cfg.emailFrom);
        } else {
            mimeMessage.setFrom(cfg.smtpUserName);
        }
        mimeMessage.setRecipients(Message.RecipientType.TO, emailTo);
        mimeMessage.setSubject(emailSubject);
        final MimeMultipart mimeMultipart = new MimeMultipart();
        if (emailBodyText != null) {
            final MimeBodyPart bodyPart = new MimeBodyPart();
            bodyPart.setText(emailBodyText);
            mimeMultipart.addBodyPart(bodyPart);
        }
        if (attachmentFiles != null) {
            for (File file : attachmentFiles) {
                final MimeBodyPart bodyPart = new MimeBodyPart();
                bodyPart.attachFile(file);
                mimeMultipart.addBodyPart(bodyPart);
            }
        }
        mimeMessage.setContent(mimeMultipart);
        mimeMessage.setSentDate(new Date());
        Transport.send(mimeMessage);
    }
}
