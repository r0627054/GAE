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
import com.google.cloud.datastore.Transaction;
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

	public Reservation addReservation(Quote quote, int carId, Transaction tx) {
		Datastore ds = CarRentalModel.getDatastore();

		Key resKey = ds.allocateId(ds.newKeyFactory()
				.addAncestors(PathElement.of("CarRentalCompany", quote.getRentalCompany()),
						PathElement.of("CarType", quote.getCarType()), PathElement.of("Car", carId))
				.setKind("Reservation").newKey());

		Entity carEntity = Entity.newBuilder(resKey).set("rentalCompany", quote.getRentalCompany())
				.set("startDate", Timestamp.of(quote.getStartDate())).set("endDate", Timestamp.of(quote.getEndDate()))
				.set("renter", quote.getRenter()).set("carType", quote.getCarType())
				.set("rentalPrice", quote.getRentalPrice()).build();

		tx.put(carEntity);
		tx.commit();
		return new Reservation(resKey.getId(), quote, carId);
	}

	public void removeReservation(Reservation res) {
		Datastore ds = CarRentalModel.getDatastore();

		Key key = ds.newKeyFactory().setKind("Reservation").newKey(res.getReservationId());
		ds.delete(key);
	}

	public void cancelReservation(Reservation res) {
		this.removeReservation(res);
	}

	public static Car parse(Entity car) {
		return new Car(car.getKey().getId().intValue(), car.getString("carRentalCompanyName"));
	}

}
