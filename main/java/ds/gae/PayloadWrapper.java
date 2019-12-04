package ds.gae;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import ds.gae.entities.Quote;

@SuppressWarnings("serial")
public class PayloadWrapper implements Serializable {

	//128 bit ID
	private UUID orderId;
	
	private List<Quote> quotes;
	private String name;
	private String email;

	public PayloadWrapper(List<Quote> quotes, String name, String email) {
		setQuotes(quotes);
		setName(name);
		setEmail(email);
		setOrderId(UUID.randomUUID());
	}

	public List<Quote> getQuotes() {
		return quotes;
	}

	private void setQuotes(List<Quote> quotes) {
		this.quotes = quotes;
	}

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	private void setEmail(String email) {
		this.email = email;
	}

	public UUID getOrderId() {
		return orderId;
	}

	private void setOrderId(UUID orderId) {
		this.orderId = orderId;
	}
	
	

}
