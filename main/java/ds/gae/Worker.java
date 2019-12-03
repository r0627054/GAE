package ds.gae;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.cloud.datastore.Datastore;
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
				confirmSingleQuote(wrapper.getQuotes().get(0));
			} else {
				confirmQuotes(wrapper.getQuotes());
			}

		} catch (ClassNotFoundException | ReservationException e) {
			MailSender.sendFailedMail(wrapper.getName(), wrapper.getEmail(), wrapper.getQuotes());
			e.printStackTrace();

		} finally {

			try {
				if (inputStream != null)
					inputStream.close();

				MailSender.sendDoneMail(wrapper.getName(), wrapper.getEmail());

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
	}

	private void confirmSingleQuote(Quote quote) {

		try {
			
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

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

	// Getters & Setters
	private Datastore getDatastore() {
		return DataStoreManager.getDataStore();
	}
}
