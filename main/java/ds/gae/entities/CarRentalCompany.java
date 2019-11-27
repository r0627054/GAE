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
import com.google.cloud.datastore.Transaction;

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
		Datastore ds = CarRentalModel.getDatastore();
		Timestamp startTime = Timestamp.of(start);
		Timestamp endTime = Timestamp.of(end);
		Set<CarType> result = new HashSet<>();

		// Get all CarTypes of company
		Key crcKey = ds.newKeyFactory().setKind("CarRentalCompany").newKey(getName());
		Query<Entity> qTypes = Query.newEntityQueryBuilder().setKind("CarType")
				.setFilter(PropertyFilter.hasAncestor(crcKey)).build();
		QueryResults<Entity> queryCarTypeResults = ds.run(qTypes);

		// For each carType
		queryCarTypeResults.forEachRemaining(res -> {

			// Get the cars of this CarType
			Key carTypeKey = ds.newKeyFactory().setKind("CarType").newKey(res.getKey().getName());

			Query<Entity> q = Query.newEntityQueryBuilder().setKind("Car").setFilter(CompositeFilter
					.and(PropertyFilter.hasAncestor(carTypeKey), PropertyFilter.eq("carRentalCompanyName", getName())))
					.build();
			QueryResults<Entity> queryCarResults = ds.run(q);

			// If the car is available, add the TYPE to the result.
			queryCarResults.forEachRemaining(carRes -> {
				if (Car.parse(carRes).isAvailable(start, end)) {
					result.add(CarType.parse(res));
				} // Result is set, so no duplicates
			});

		});
		return result;
	}

	/****************
	 * RESERVATIONS *
	 ****************/

	public Quote createQuote(ReservationConstraints constraints, String client) throws ReservationException {
		logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}",
				new Object[] { name, client, constraints.toString() });

		if (!isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate())) {
			throw new ReservationException("<" + name + "> No cars available to satisfy the given constraints.");
		}
		
		CarType type = getCarType(constraints.getCarType());

		double price = calculateRentalPrice(type.getRentalPricePerDay(), constraints.getStartDate(),
				constraints.getEndDate());
		return new Quote(client, constraints.getStartDate(), constraints.getEndDate(), getName(),
				constraints.getCarType(), price);
	}

	public Reservation confirmQuote(Quote quote, Transaction tx) throws ReservationException {
		logger.log(Level.INFO, "<{0}> Reservation of {1}", new Object[] { name, quote.toString() });

		List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());

		if (availableCars.size() == 0) {
			throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
					+ " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
		}

		Car car = availableCars.get((int) (Math.random() * availableCars.size()));

		Reservation res = car.addReservation(quote, car.getId(), tx);
		return res;
	}

	/*********
	 * CARS *
	 *********/
	
	private List<Car> getAvailableCars(String carType, Date start, Date end) {
		List<Car> result = new ArrayList<Car>();
		Datastore ds = CarRentalModel.getDatastore();

		Key carTypeKey = ds.newKeyFactory().setKind("CarType").newKey(carType);

		Query<Entity> query = Query.newEntityQueryBuilder().setKind("Car").setFilter(CompositeFilter
				.and(PropertyFilter.hasAncestor(carTypeKey), PropertyFilter.eq("carRentalCompanyName", getName())))
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
