package ds.gae;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.images.Composite;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import ds.gae.entities.Car;
import ds.gae.entities.CarRentalCompany;
import ds.gae.entities.CarType;
import ds.gae.entities.Quote;
import ds.gae.entities.Reservation;
import ds.gae.entities.ReservationConstraints;

public class CarRentalModel {

	// FIXME use persistence instead
//	public Map<String, CarRentalCompany> CRCS = new HashMap<String, CarRentalCompany>();

	private static Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private static CarRentalModel instance;

	public static CarRentalModel get() {
		if (instance == null) {
			instance = new CarRentalModel();
		}
		return instance;
	}

	private CarRentalModel() {
		// Private constructor so we cannot create multiple
	}

	/**
	 * Get the car types available in the given car rental company.
	 *
	 * @param companyName the car rental company
	 * @return The list of car types (i.e. name of car type), available in the given
	 *         car rental company.
	 */
	public Set<String> getCarTypesNames(String companyName) {
		// FIXME add implementation
		return null;
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
		System.out.println("CREATE QUOTE " + companyName + " " + renterName + " " + constraints);

//		Key resKey = getDatastore().newKeyFactory().addAncestors(PathElement.of("Car", 80),
//				PathElement.of("CarType", constraints.getCarType()), PathElement.of("CarRentalCompany", companyName))
//				.setKind("Reservation").newKey(1);
//
//		Entity e = Entity.newBuilder(resKey)
//				.set("id", 1)
//				.set("name", renterName)
//				.set("rentalCompany", companyName)
//				.set("startDate", Timestamp.now())
//				.set("endDate", Timestamp.now())
//				.set("renter", renterName)
//				.set("carType", constraints.getCarType())
//				.set("rentalPrice", 50)
//				.build();
//
//		getDatastore().put(e);
//
//		return null;

		// NOT DONE

		Query<Entity> query = Query.newEntityQueryBuilder().setKind("CarRentalCompany")
				.setFilter(PropertyFilter.eq("name", companyName)).build();
		QueryResults<Entity> results = getDatastore().run(query);

		if (results.hasNext()) {
			System.out.println("hasNext OK");
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
	 * 
	 * @throws ReservationException Confirmation of given quote failed.
	 */
	public void confirmQuote(Quote quote) throws ReservationException {
		// FIXME: use persistence instead

		// hier ga de crc opvragen
		// en bouwen
		// crc object aangemaakt

		// CarRentalCompany crc = CRCS.get(quote.getRentalCompany());
		// crc.confirmQuote(quote);
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
	public List<Reservation> confirmQuotes(List<Quote> quotes) throws ReservationException {
		List<Reservation> result = new ArrayList<Reservation>();
		for (Quote q : quotes) {
			
		}
		return result;
	}

	/**
	 * Get all reservations made by the given car renter.
	 *
	 * @param renter name of the car renter
	 * @return the list of reservations of the given car renter
	 */
	public List<Reservation> getReservations(String renter) {
		// FIXME: use persistence instead
		/*
		 * List<Reservation> out = new ArrayList<Reservation>(); for (CarRentalCompany
		 * crc : CRCS.values()) { for (Car c : crc.getCars()) { for (Reservation r :
		 * c.getReservations()) { if (r.getRenter().equals(renter)) { out.add(r); } } }
		 * } return out;
		 */
		List<Reservation> result = new ArrayList<Reservation>();

		Query<Entity> query = Query.newEntityQueryBuilder().setKind("Reservation")
				.setFilter(PropertyFilter.eq("name", renter)).build();
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

//		Query<Entity> query = Query.newEntityQueryBuilder().setKind("Car")
//				.setFilter(CompositeFilter.and(
//						PropertyFilter.hasAncestor(
//								getDatastore().newKeyFactory().setKind("CarRentalCompany").newKey(companyName)),
//						PropertyFilter.hasAncestor(
//								getDatastore().newKeyFactory().setKind("CarType").newKey(carType.getName()))))
//				.setFilter(PropertyFilter.hasAncestor(getDatastore().newKeyFactory().setKind("CarRentalCompany").newKey(companyName)))
//				.setFilter(PropertyFilter
//						.hasAncestor(getDatastore().newKeyFactory().setKind("CarType").newKey(carType.getName())))
//				.setFilter(PropertyFilter.eq("name", companyName))
//				.build();
//
//		Key key = getDatastore().newKeyFactory().setKind("CarType")
//				.addAncestor(PathElement.of("CarRentalCompany", companyName)).newKey(carType.getName());
//		Entity en = getDatastore().get(key);
//		Query<Entity> q = Query.newEntityQueryBuilder().setKind("Car").setFilter(PropertyFilter.hasAncestor(key))
//				.build();
//		Key key2 = getDatastore().newKeyFactory().setKind("CarType").newKey(en.getKey().getName());

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

	public static Datastore getDatastore() {
		return datastore;
	}
}
