package ClassModel;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * 
 * @author Callum
 */
public class Mail {

    private String userName = "rfidprojectaut";  // GMail user name (just the part before "@gmail.com")
    private String password = "rfidArduino"; // GMail password
    
    /**
     * This constructor is called when a trigger event has happened in the application, prompting an email
     * to be sent to either a parent, teacher or both.
     * 
     * @param recipient the recipient of the email
     * @param subject the subject of the email
     * @param body the main text of the email
     */
    public Mail(String recipient, String subject, String body) {
        String from = userName;
        String pass = password;
        String[] to = {recipient};

        sendFromGMail(from, pass, to, subject, body);
    }

    /**
     * Sends an email from the sender to the recipient(s) with the specified subject and body.
     * 
     * @param from the account sending the email
     * @param pass the password of the account sending the email
     * @param to the proposed recipients of the email
     * @param subject the subject of the email
     * @param body the main text within the email
     */
    public void sendFromGMail(String from, String pass, String[] to, String subject, String body) {
        Properties props = System.getProperties();
        String host = "smtp.gmail.com";
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", from);
        props.put("mail.smtp.password", pass);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);

        try {
            message.setFrom(new InternetAddress(from));
            InternetAddress[] toAddress = new InternetAddress[to.length];

            // To get the array of addresses
            for( int i = 0; i < to.length; i++ ) {
                toAddress[i] = new InternetAddress(to[i]);
            }

            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }

            message.setSubject(subject);
            message.setText(body);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, from, pass);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
    }
}