package ds.gae.entities;

import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.Transaction;

import ds.gae.DataStoreManager;

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

	public boolean isAvailable(Date start, Date end, String carTypeName) {
		if (!start.before(end)) {
			throw new IllegalArgumentException("Illegal given period");
		}
		Datastore ds = DataStoreManager.getDataStore();

		Key carKey = ds.newKeyFactory().addAncestors(PathElement.of("CarRentalCompany", carRentalCompanyName),
				PathElement.of("CarType", carTypeName)).setKind("Car").newKey(getId());

		Query<Entity> query = Query.newEntityQueryBuilder().setKind("Reservation")
				.setFilter(PropertyFilter.hasAncestor(carKey)).build();

		QueryResults<Entity> queryResults = ds.run(query);

		while (queryResults.hasNext()) {
			Reservation reservation = Reservation.parse(queryResults.next());

			// No two comparing filters can be used with GAE Queries, so we need to filter
			// in Java.
			if (!(reservation.getEndDate().before(start) || reservation.getStartDate().after(end))) {
				return false;
			}
		}
		return true;

	}

	public void addReservation(Quote quote, int carId, Transaction tx) {
		Datastore ds = DataStoreManager.getDataStore();

		Key resKey = ds.allocateId(ds.newKeyFactory()
				.addAncestors(PathElement.of("CarRentalCompany", quote.getRentalCompany()),
						PathElement.of("CarType", quote.getCarType()), PathElement.of("Car", carId))
				.setKind("Reservation").newKey());

		Entity reservationEntity = Entity.newBuilder(resKey).set("rentalCompany", quote.getRentalCompany())
				.set("startDate", Timestamp.of(quote.getStartDate())).set("endDate", Timestamp.of(quote.getEndDate()))
				.set("renter", quote.getRenter()).set("carType", quote.getCarType())
				.set("rentalPrice", quote.getRentalPrice()).build();

		if (tx != null) {
			tx.put(reservationEntity);
		} else {
			ds.put(reservationEntity);
		}
	}

	public void removeReservation(Reservation res) {
		Datastore ds = DataStoreManager.getDataStore();

		Key key = ds.newKeyFactory().setKind("Reservation").newKey(res.getReservationId());
		ds.delete(key);
	}

	@Override
	public String toString() {
		return this.getId() + " OF " + carRentalCompanyName;
	}

	public void cancelReservation(Reservation res) {
		this.removeReservation(res);
	}

	public static Car parse(Entity car) {
		return new Car(car.getKey().getId().intValue(), car.getString("carRentalCompanyName"));
	}

}
