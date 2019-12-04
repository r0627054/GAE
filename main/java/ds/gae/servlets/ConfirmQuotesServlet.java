package ds.gae.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ds.gae.CarRentalModel;
import ds.gae.ReservationException;
import ds.gae.entities.Quote;
import ds.gae.view.JSPSite;
import ds.gae.view.Tools;

@SuppressWarnings("serial")
public class ConfirmQuotesServlet extends HttpServlet {

	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		HttpSession session = req.getSession();
		HashMap<String, ArrayList<Quote>> allQuotes = (HashMap<String, ArrayList<Quote>>) session
				.getAttribute("quotes");

		String renter = (String) session.getAttribute("renter");
		String mail = renter.replace(" ", "").replace(".", "") + "@gmail.com";

		try {
			ArrayList<Quote> qs = new ArrayList<Quote>();

			for (String crcName : allQuotes.keySet()) {
				qs.addAll(allQuotes.get(crcName));
			}
			UUID orderId = CarRentalModel.get().confirmQuotes(qs, renter, mail);

			session.setAttribute("quotes", new HashMap<String, ArrayList<Quote>>());
			session.setAttribute("renterEmail", mail);
			session.setAttribute("orderId", orderId.toString());

			// resp.sendRedirect(JSPSite.CREATE_QUOTES.url());
			resp.sendRedirect(JSPSite.CONFIRM_QUOTES_RESPONSE.url());
		} catch (ReservationException e) {
			session.setAttribute("errorMsg", Tools.encodeHTML(e.getMessage()));
			resp.sendRedirect(JSPSite.RESERVATION_ERROR.url());
		}
	}
}
