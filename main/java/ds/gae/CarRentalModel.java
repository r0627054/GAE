package ds.gae;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import ds.gae.entities.Car;
import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.CarType;
import ds.gae.entities.Quote;
import ds.gae.entities.Reservation;
import ds.gae.entities.ReservationConstraints;

public class CarRentalModel {

	private Queue queue = QueueFactory.getQueue("distri-queue");

	private static CarRentalModel instance;

	public static CarRentalModel get() {
		if (instance == null) {
			instance = new CarRentalModel();
		}
		return instance;
	}

	/**
	 * Get the car types available in the given car rental company.
	 *
	 * @param companyName the car rental company
	 * @return The list of car types (i.e. name of car type), available in the given
	 *         car rental company.
	 */
	public Set<String> getCarTypesNames(String companyName) {
		Key crcKey = getDatastore().newKeyFactory().setKind("CarRentalCompany").newKey(companyName);
		Query<Entity> q = Query.newEntityQueryBuilder().setKind("CarType").setFilter(PropertyFilter.hasAncestor(crcKey))
				.build();

		QueryResults<Entity> queryResults = getDatastore().run(q);

		Set<String> results = new HashSet<>();

		queryResults.forEachRemaining(res -> {
			results.add(CarType.parse(res).getName());
		});

		return results;
	}

	/**
	 * Get the names of all registered car rental companies
	 *
	 * @return the list of car rental companies
	 */
	public Collection<String> getAllRentalCompanyNames() {
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("CarRentalCompany").build();
		QueryResults<Entity> results = getDatastore().run(query);

		Set<String> result = new HashSet<>();
		while (results.hasNext()) {
			Entity e = results.next();
			result.add(e.getString("name"));
		}
		return result;
	}

	/**
	 * Create a quote according to the given reservation constraints (tentative
	 * reservation).
	 * 
	 * @param companyName name of the car renter company
	 * @param renterName  name of the car renter
	 * @param constraints reservation constraints for the quote
	 * @return The newly created quote.
	 * 
	 * @throws ReservationException No car available that fits the given
	 *                              constraints.
	 */
	public Quote createQuote(String companyName, String renterName, ReservationConstraints constraints)
			throws ReservationException {
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("CarRentalCompany")
				.setFilter(PropertyFilter.eq("name", companyName)).build();
		QueryResults<Entity> results = getDatastore().run(query);

		if (results.hasNext()) {
			CarRentalCompany crc = CarRentalCompany.parse(results.next());
			return crc.createQuote(constraints, renterName);
		} else {
			throw new ReservationException("No car rental company found for : " + companyName);
		}

	}

	/**
	 * Confirm the given quote.
	 *
	 * @param quote Quote to confirm
	 * @return the Reservation that is created
	 * 
	 * @throws ReservationException Confirmation of given quote failed.
	 */
	public UUID confirmQuote(Quote quote, String name, String emailAdress) throws ReservationException {
		List<Quote> singleQuoteList = new ArrayList<>();
		singleQuoteList.add(quote);

		return createWorkerWithPayload(new PayloadWrapper(singleQuoteList, name, emailAdress));
	}

	/**
	 * Confirm the given list of quotes
	 * 
	 * @param quotes the quotes to confirm
	 * @return The list of reservations, resulting from confirming all given quotes.
	 * 
	 * @throws ReservationException One of the quotes cannot be confirmed. Therefore
	 *                              none of the given quotes is confirmed.
	 */
	public UUID confirmQuotes(List<Quote> quotes, String name, String emailAdress) throws ReservationException {
		return createWorkerWithPayload(new PayloadWrapper(quotes, name, emailAdress));
	}

	private UUID createWorkerWithPayload(PayloadWrapper wrapper) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutput objectOut = null;
		byte[] bytes = {};
		try {
			objectOut = new ObjectOutputStream(outputStream);
			objectOut.writeObject(wrapper);
			objectOut.flush();
			bytes = outputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		getQueue().add(TaskOptions.Builder.withUrl("/worker").payload(bytes));
		return wrapper.getOrderId();
	}

	/**
	 * Get all reservations made by the given car renter.
	 *
	 * @param renter name of the car renter
	 * @return the list of reservations of the given car renter
	 */
	public List<Reservation> getReservations(String renter) {
		List<Reservation> result = new ArrayList<Reservation>();

		Query<Entity> query = Query.newEntityQueryBuilder().setKind("Reservation")
				.setFilter(PropertyFilter.eq("renter", renter)).build();
		QueryResults<Entity> queryResults = getDatastore().run(query);

		queryResults.forEachRemaining(res -> {
			result.add(Reservation.parse(res));
		});

		return result;
	}

	/**
	 * Get the car types available in the given car rental company.
	 *
	 * @param companyName the given car rental company
	 * @return The list of car types in the given car rental company.
	 */
	public Collection<CarType> getCarTypesOfCarRentalCompany(String companyName) {
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("CarType")
				.setFilter(PropertyFilter
						.hasAncestor(getDatastore().newKeyFactory().setKind("CarRentalCompany").newKey(companyName)))
				.build();
		QueryResults<Entity> results = getDatastore().run(query);

		Set<CarType> result = new HashSet<CarType>();

		results.forEachRemaining(res -> {
			result.add(CarType.parse(res));
		});
		return result;
	}

	/**
	 * Get the list of cars of the given car type in the given car rental company.
	 *
	 * @param companyName name of the car rental company
	 * @param carType     the given car type
	 * @return A list of car IDs of cars with the given car type.
	 */
	public Collection<Integer> getCarIdsByCarType(String companyName, CarType carType) {
		Collection<Integer> out = new ArrayList<Integer>();
		for (Car c : getCarsByCarType(companyName, carType)) {
			out.add(c.getId());
		}
		return out;
	}

	/**
	 * Get the amount of cars of the given car type in the given car rental company.
	 *
	 * @param companyName name of the car rental company
	 * @param carType     the given car type
	 * @return A number, representing the amount of cars of the given car type.
	 */
	public int getAmountOfCarsByCarType(String companyName, CarType carType) {
		return this.getCarsByCarType(companyName, carType).size();
	}

	/**
	 * Get the list of cars of the given car type in the given car rental company.
	 *
	 * @param companyName name of the car rental company
	 * @param carType     the given car type
	 * @return List of cars of the given car type
	 */
	private List<Car> getCarsByCarType(String companyName, CarType carType) {
		List<Car> result = new ArrayList<>();

		Query<Entity> q = Query.newEntityQueryBuilder().setKind("Car")
				.setFilter(
						CompositeFilter.and(PropertyFilter.eq("carRentalCompanyName", companyName),
								PropertyFilter.hasAncestor(
										getDatastore().newKeyFactory().setKind("CarType").newKey(carType.getName()))))
				.build();

		QueryResults<Entity> carResults = getDatastore().run(q);

		carResults.forEachRemaining(res -> {
			result.add(Car.parse(res));
		});

		return result;

	}

	/**
	 * Check whether the given car renter has reservations.
	 *
	 * @param renter the car renter
	 * @return True if the number of reservations of the given car renter is higher
	 *         than 0. False otherwise.
	 */
	public boolean hasReservations(String renter) {
		return this.getReservations(renter).size() > 0;
	}

	private Datastore getDatastore() {
		return DataStoreManager.getDataStore();
	}

	public Queue getQueue() {
		return queue;
	}

}
