package ds.gae;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.Quote;

@SuppressWarnings("serial")
public class Worker extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PayloadWrapper wrapper = null;
		ObjectInputStream inputStream = new ObjectInputStream(req.getInputStream());

		try {
			wrapper = (PayloadWrapper) inputStream.readObject();

			if (wrapper.getQuotes().size() == 1) {
				confirmSingleQuoteWithNoTransaction(wrapper.getQuotes().get(0));
			} else {
				confirmQuotes(wrapper.getQuotes());
			}
			
			sendDoneMail(wrapper.getName(), wrapper.getEmail(), wrapper.getQuotes());
		} catch (ClassNotFoundException | ReservationException e) {
			sendFailedMail(wrapper.getName(), wrapper.getEmail(), wrapper.getQuotes());

			e.printStackTrace();

		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	}
	
	/**************************************************
	/*              RESERVATIONS                      *
	/**************************************************/

	private void confirmQuotes(List<Quote> quotes) throws ReservationException {
		Transaction tx = getDatastore().newTransaction();

		try {
			for (Quote q : quotes) {
				confirmQuoteWithTransaction(q, tx);
			}
			tx.commit();

		} finally {
			if (tx.isActive()) {
				tx.rollback();

				throw new ReservationException("Error confirming quotes. All reservations are rolled back.");
			}

		}

	}

	/**
	 * Confirm the given quote.
	 *
	 * @param quote Quote to confirm
	 * @param tx    Transaction to use
	 * @return the Reservation that is created
	 * 
	 * @throws ReservationException Confirmation of given quote failed.
	 */
	public void confirmQuoteWithTransaction(Quote quote, Transaction tx) throws ReservationException {
		if (tx == null) {
			tx = getDatastore().newTransaction();
		}
		Datastore ds = getDatastore();
		Key crcKey = ds.newKeyFactory().setKind("CarRentalCompany").newKey(quote.getRentalCompany());
		Entity crcEntity = ds.get(crcKey);

		CarRentalCompany crc = CarRentalCompany.parse(crcEntity);
		crc.confirmQuote(quote, tx);
	}

	private void confirmSingleQuoteWithNoTransaction(Quote quote) throws ReservationException {
		Datastore ds = getDatastore();
		Key crcKey = ds.newKeyFactory().setKind("CarRentalCompany").newKey(quote.getRentalCompany());
		Entity crcEntity = ds.get(crcKey);

		CarRentalCompany crc = CarRentalCompany.parse(crcEntity);
		crc.confirmQuote(quote, null);
	}
	
	/**************************************************
	/*              MAILS                             *
	/**************************************************/
	
	public static void sendMail(String receiverName, String receiverMail, String subject, String content) {
		Session session = Session.getDefaultInstance(new Properties(), null);

		try {
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress("no-reply@gae.com", "GAE app"));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(receiverMail, ""));
			msg.setSubject(subject);
			msg.setText(content);

			Transport.send(msg);
			System.out.println("MailSender send mail: " + msg.getContent().toString()); //TODO Remove this
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void sendDoneMail(String receiverName, String receiverMail, List<Quote> list) {
		String content = "Dear " + receiverName
				+ ",\nThe quotes you have created are now confirmed.\nThe quotes are:\n";
		for (Quote q : list) {
			content += q.toString() + "\n";
		}
		sendMail(receiverName, receiverMail, "The reservations have been confirmed.", content);
	}

	public static void sendFailedMail(String receiverName, String receiverMail, List<Quote> quotes) {
		String content = "Dear " + receiverName
				+ ",\nThe quotes you have created cannot be confirmed.\nThese quotes are:\n";
		for (Quote q : quotes) {
			content += q.toString() + "\n";
		}
		sendMail(receiverName, receiverMail, "The reservations have been cancelled!", content);
	}
	

	/**************************************************
	/*              DATASTORE                         *
	/**************************************************/
	
	// Since this worker will execute on a back-end server
	// a new DataStore service has to be created, the one in 
	// DataStoreManager cannot be reused. This service needs
	// to use the same datastore as the front-end.
	// There is no shared memory front & back end
	private Datastore getDatastore() {
		return DatastoreOptions.getDefaultInstance().getService();
	}
}
