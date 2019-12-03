package ds.gae;

public class MailSender {

	public static void sendMail(String receiverName, String receiverMail, String subject, String content) {
		System.out.println(receiverName + " " + subject);
	}

}
