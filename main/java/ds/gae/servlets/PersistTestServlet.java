package ds.gae.servlets;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ds.gae.CarRentalModel;
import ds.gae.ReservationException;
import ds.gae.entities.Quote;
import ds.gae.entities.ReservationConstraints;
import ds.gae.view.JSPSite;
import ds.gae.view.Tools;

@SuppressWarnings("serial")
public class PersistTestServlet extends HttpServlet {

	private static Logger logger = Logger.getLogger(PersistTestServlet.class.getName());

	private Map<String, List<Quote>> sessionQuotes = new HashMap<>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String companyName = "Hertz";
		String userName = "Pieter A.";
		String email = userName.replace(" ", "").replace(".", "") + "@gmail.com";
		req.getSession().setAttribute("renter", userName);

		try {

			if (CarRentalModel.get().getReservations(userName).size() == 0) {

				ReservationConstraints c = new ReservationConstraints(Tools.DATE_FORMAT.parse("08.12.2019"),
						Tools.DATE_FORMAT.parse("14.12.2019"), "Compact");

				final Quote q = CarRentalModel.get().createQuote(companyName, userName, c);
				addToMap(userName, q);
				CarRentalModel.get().confirmQuote(q, userName, email);
			}

			resp.sendRedirect(JSPSite.PERSIST_TEST.url());
		} catch (ReservationException | ParseException e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void addToMap(String userName, Quote newQuote) {
		List<Quote> quotesOfUser = sessionQuotes.get(userName);

		if (quotesOfUser == null) {
			quotesOfUser = new ArrayList<Quote>();
			quotesOfUser.add(newQuote);
			sessionQuotes.put(userName, quotesOfUser);
		} else {
			if (!quotesOfUser.contains(newQuote)) {
				quotesOfUser.add(newQuote);
			}
		}
	}
}
