package ds.gae.entities;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.IncompleteKey;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;

import ds.gae.CarRentalModel;

public class Car {

	private static Logger logger = Logger.getLogger(Car.class.getName());

	private int id;
	private String carRentalCompanyName;

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public Car(int uid, String carRentalCompanyName) {
		this.id = uid;
		this.carRentalCompanyName = carRentalCompanyName;
	}

	/******
	 * ID *
	 ******/

	public int getId() {
		return id;
	}

	/****************
	 * RESERVATIONS *
	 ****************/

	public Set<Reservation> getReservations() {
		return null;
	}

	public boolean isAvailable(Date start, Date end) {
		if (!start.before(end)) {
			throw new IllegalArgumentException("Illegal given period");
		}
		Datastore ds = CarRentalModel.getDatastore();

		Key carKey = ds.newKeyFactory().setKind("Car").newKey(getId());
		Query<Entity> query = Query.newEntityQueryBuilder().setKind("Reservation")
				.setFilter(PropertyFilter.hasAncestor(carKey)).build();

		QueryResults<Entity> queryResults = ds.run(query);

		if (queryResults.hasNext()) {
			Reservation reservation = Reservation.parse(queryResults.next());

			if (reservation.getEndDate().before(start) && reservation.getStartDate().after(end)) {
				return false;
			}
		}
		return true;

	}

	public void addReservation(Reservation res) {
		Datastore ds = CarRentalModel.getDatastore();

		Key resKey = ds.allocateId(ds.newKeyFactory().addAncestors(PathElement.of("CarType", res.getCarType()),
				PathElement.of("CarRentalCompany", res.getRentalCompany()), PathElement.of("Car", res.getCarId()))
				.setKind("Reservation").newKey());

		Entity carEntity = Entity.newBuilder(resKey).set("rentalCompany", res.getRentalCompany())
				.set("startDate", Timestamp.of(res.getStartDate())).set("endDate", Timestamp.of(res.getEndDate()))
				.set("renter", res.getRenter()).set("carType", res.getCarType())
				.set("rentalPrice", res.getRentalPrice()).build();

		ds.put(carEntity);
	}

	public void removeReservation(Reservation reservation) {
		// reservations.remove(reservation);
		// TODO
	}

	/*
	 * public Quote createQuote(ReservationConstraints constraints, String client)
	 * throws ReservationException { logger.log(Level.INFO,
	 * "<{0}> Creating tentative reservation for {1} with constraints {2}", new
	 * Object[] { name, client, constraints.toString() });
	 * 
	 * CarType type = getCarType(constraints.getCarType());
	 * 
	 * if (!isAvailable(constraints.getCarType(), constraints.getStartDate(),
	 * constraints.getEndDate())) { throw new ReservationException("<" + name +
	 * "> No cars available to satisfy the given constraints."); }
	 * 
	 * double price = calculateRentalPrice( type.getRentalPricePerDay(),
	 * constraints.getStartDate(), constraints.getEndDate() );
	 * 
	 * return new Quote( client, constraints.getStartDate(),
	 * constraints.getEndDate(), getName(), constraints.getCarType(), price ); }
	 */

// Implementation can be subject to different pricing strategies
	private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
		return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24D));
	}

	public void cancelReservation(Reservation res) {
		// logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[] {
		// name, res.toString() });
		// getCar(res.getCarId()).removeReservation(res);
	}

	public static Car parse(Entity car) {
		return new Car(car.getKey().getId().intValue(), car.getString("carRentalCompanyName"));
	}

}
