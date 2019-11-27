package ds.gae.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.Property.PropertyType;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.CompositeFilter;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;

import ds.gae.CarRentalModel;
import ds.gae.ReservationException;

public class CarRentalCompany {

	private static Logger logger = Logger.getLogger(CarRentalCompany.class.getName());

	private String name;

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public CarRentalCompany(String name) {
		setName(name);

	}

	/********
	 * NAME *
	 ********/

	public String getName() {
		return name;
	}

	private void setName(String name) {
		this.name = name;
	}

	/*************
	 * CAR TYPES *
	 *************/

	public Collection<CarType> getAllCarTypes() {
		return null;
	}

	public CarType getCarType(String carTypeName) {
		Datastore ds = CarRentalModel.getDatastore();
//TODO check ancestor filter
//CompositeFilter.and(
//		PropertyFilter.hasAncestor(ds.newKeyFactory().setKind("CarRentalCompany").newKey(getName())),
		Key carTypeKey = ds.newKeyFactory().addAncestors(PathElement.of("CarRentalCompany", getName()))
				.setKind("CarType").newKey(carTypeName);

		Query<Entity> q = Query.newEntityQueryBuilder().setKind("CarType")
				.setFilter(PropertyFilter.eq("__key__", carTypeKey)).build();
		QueryResults<Entity> results = ds.run(q);

		if (results.hasNext()) {
			return CarType.parse(results.next());
		} else {
			return null;
		}
	}

	public boolean isAvailable(String carTypeName, Date start, Date end) {
		logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[] { name, carTypeName });
		return getAvailableCarTypes(start, end).contains(getCarType(carTypeName));
	}

	public Set<CarType> getAvailableCarTypes(Date start, Date end) {

		/*
		 * Set<CarType> availableCarTypes = new HashSet<CarType>(); for (Car car :
		 * getCars()) { if (car.isAvailable(start, end)) {
		 * availableCarTypes.add(car.getType()); } }
		 */
		// return availableCarTypes;

		Datastore ds = CarRentalModel.getDatastore();
		Timestamp startTime = Timestamp.of(start);
		Timestamp endTime = Timestamp.of(end);
		Set<CarType> result = new HashSet<>();

		// TODO DatastoreException: Only one inequality filter per query is supported.
		Query<Entity> qTypes = Query.newEntityQueryBuilder().setKind("CarType").build();
		QueryResults<Entity> queryCarTypeResults = ds.run(qTypes);

		queryCarTypeResults.forEachRemaining(res -> {
			result.add(CarType.parse(res));

//			Query<Entity> q = Query.newEntityQueryBuilder().setKind("Reservation").setFilter(filter).build();
//			QueryResults<Entity> queryResults = ds.run(q);

		});

		return result;

		/*
		 * queryResults.forEachRemaining(res -> { Key carTypeKey =
		 * ds.newKeyFactory().addAncestors(PathElement.of("CarRentalCompany",
		 * getName())) .setKind("CarType").newKey(res.getKey().getName());
		 * 
		 * 
		 * // Key resKey =
		 * getDatastore().newKeyFactory().addAncestors(PathElement.of("Car", 80), //
		 * PathElement.of("CarType", constraints.getCarType()),
		 * PathElement.of("CarRentalCompany", companyName)) //
		 * .setKind("Reservation").newKey(1);
		 * 
		 * Query<Entity> carQuery =
		 * Query.newEntityQueryBuilder().setKind("Reservation").setFilter(PropertyFilter
		 * .hasAncestor(carTypeKey)).build(); QueryResults<Entity> carQueryResults =
		 * ds.run(carQuery);
		 * 
		 * carQueryResults.forEachRemaining(carE -> {
		 * 
		 * Car car = Car.parse(carE);
		 * 
		 * if(car)
		 * 
		 * result.add(CarType.parse(res));
		 * 
		 * });
		 * 
		 * 
		 * });
		 */
	}

	/*********
	 * CARS *
	 *********/

	/****************
	 * RESERVATIONS *
	 ****************/

	public Quote createQuote(ReservationConstraints constraints, String client) throws ReservationException {
		logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}",
				new Object[] { name, client, constraints.toString() });

		CarType type = getCarType(constraints.getCarType());

//		if (!isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate())) {
//			System.out.println("not available");
//			throw new ReservationException("<" + name + "> No cars available to satisfy the given constraints.");
//		}
//		System.out.println("is available"); TODO

		double price = calculateRentalPrice(type.getRentalPricePerDay(), constraints.getStartDate(),
				constraints.getEndDate());
		return new Quote(client, constraints.getStartDate(), constraints.getEndDate(), getName(),
				constraints.getCarType(), price);
	}

	public Reservation confirmQuote(Quote quote) throws ReservationException {
		logger.log(Level.INFO, "<{0}> Reservation of {1}", new Object[] { name, quote.toString() });

		List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());

		if (availableCars.size() == 0) {
			throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
					+ " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
		}

		Car car = availableCars.get((int) (Math.random() * availableCars.size()));

		Reservation res = new Reservation(quote, car.getId());
		car.addReservation(res);
		return res;
	}

	private List<Car> getAvailableCars(String carType, Date start, Date end) {
		List<Car> result = new ArrayList<Car>();
		Datastore ds = CarRentalModel.getDatastore();

		Key carTypeKey = ds.newKeyFactory().setKind("CarType").newKey(carType);

		Query<Entity> query = Query.newEntityQueryBuilder().setKind("Car")
				.setFilter(
						CompositeFilter.and(PropertyFilter.hasAncestor(carTypeKey), PropertyFilter.eq("carRentalCompanyName", getName())))
				.build();

		QueryResults<Entity> queryResults = ds.run(query);

		queryResults.forEachRemaining(res -> {
			Car car = Car.parse(res);
			if (car.isAvailable(start, end)) {
				result.add(car);
			}
		});
		return result;

	}

	// Implementation can be subject to different pricing strategies
	private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
		return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24D));
	}

	public static CarRentalCompany parse(Entity entityToParse) {
		return new CarRentalCompany(entityToParse.getString("name"));
	}

}
