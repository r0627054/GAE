package ds.gae;

import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import ds.gae.entities.Quote;

public class MailSender {

	public static void sendMail(String receiverName, String receiverMail, String subject, String content) {

		Session session = Session.getDefaultInstance(new Properties(), null);

		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("no-reply@gae.com", "GAE app"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(receiverMail, ""));
			msg.setSubject(subject);
			msg.setText(content);

			Transport.send(msg);
			System.out.println("MailSender send mail: " + msg.getContent().toString());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void sendDoneMail(String receiverName, String receiverMail) {
		String content = "Dear " + receiverName + ",\nThe quotes you have created are now confirmed.";
		sendMail(receiverName, receiverMail, "The reservations have been confirmed", content);
	}

	public static void sendFailedMail(String receiverName, String receiverMail, List<Quote> quotes) {
		String content = "Dear " + receiverName
				+ ",\nThe quotes you have created cannot be confirmed.\nThese quotes are:";
		for (Quote q : quotes) {
			content += q.toString() + "\n";
		}
		sendMail(receiverName, receiverMail, "The reservations are cancelled.", content);
	}

	public static void sendInQueueMail(String name, String emailAdress) {
		String content = "Dear " + name
				+ ",\nThe quotes have been received on the server. They will be processed as soon as possible.";
		sendMail(name, emailAdress, "The quotes are received. They will be processed as soon as possible.", content);

	}

}
