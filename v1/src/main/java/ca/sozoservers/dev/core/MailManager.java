package ca.sozoservers.dev.core;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.codec.binary.Base64;

import ca.sozoservers.dev.config.Config.EmailConfig;
import ca.sozoservers.dev.config.Config.EmailInfo;

public class MailManager {

    private static Session getSession(Properties props){
       return  Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(props.getProperty("mail.smtp.user"), new String(Base64.decodeBase64(EmailInfo.PASSWORD.getBytes())));
            }
        });
    }
    
    public static boolean sendEmail(EmailConfig config){
        Properties props = EmailInfo.getProperties();
        Session session = getSession(props);
        System.out.println("sending");
        try {

            MimeMessage email = new MimeMessage(session);
            email.setSubject(config.formatText(config.getSubjectLine().getContent()));
            email.setFrom(new InternetAddress(props.getProperty("mail.smtp.user")));
            email.addRecipient(Message.RecipientType.TO, config.getTo());

            BodyPart body = new MimeBodyPart();
            body.setText(config.formatText(config.getBody().getContent()));
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(body);

            // System.out.println(config.getBody().getContent());
            // System.out.println(config.formatText(config.getBody().getContent()));
            BodyPart attachment = new MimeBodyPart();
            DataSource source = new FileDataSource(config.getAttachment().getContent());
            attachment.setDataHandler(new DataHandler(source));
            attachment.setFileName(config.getAttachment().getName());
            multipart.addBodyPart(attachment);

            email.setContent(multipart);
            Transport transport = session.getTransport("smtps");
            transport.connect(props.getProperty("mail.smtp.host"), Integer.parseInt(props.getProperty("mail.smtp.port")), props.getProperty("mail.smtp.user"), new String(Base64.decodeBase64(EmailInfo.PASSWORD.getBytes())));
            transport.sendMessage(email, email.getAllRecipients());
            transport.close(); 
            System.out.println(config);
            return true;
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
        return false;
    }
    
}
