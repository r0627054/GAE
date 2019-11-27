package ds.gae.entities;

import java.util.Date;
import java.util.Objects;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Entity;

public class Reservation extends Quote {

	private int carId;
	private long reservationId;

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public Reservation(long reservationId, Quote quote, int carId) {
		this(quote.getRenter(), quote.getStartDate(), quote.getEndDate(), quote.getRentalCompany(), quote.getCarType(),
				quote.getRentalPrice());
		this.carId = carId;
		this.reservationId = reservationId;
	}

	private Reservation(String renter, Date start, Date end, String rentalCompany, String carType,
			double rentalPrice) {
		super(renter, start, end, rentalCompany, carType, rentalPrice);
	}

	/******
	 * ID *
	 ******/

	public int getCarId() {
		return carId;
	}
	
	public long getReservationId() {
		return reservationId;
	}

	/*************
	 * TO STRING *
	 *************/

	@Override
	public String toString() {
		return String.format("Reservation for %s from %s to %s at %s\nCar type: %s\tCar: %s\nTotal price: %.2f",
				getRenter(), getStartDate(), getEndDate(), getRentalCompany(), getCarType(), getCarId(),
				getRentalPrice());
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), getCarId());
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		Reservation other = (Reservation) obj;
		if (getCarId() != other.getCarId()) {
			return false;
		}
		return true;
	}

	public static Reservation parse(Entity res) {
		return new Reservation(res.getKey().getId(), new Quote(res.getString("renter"), res.getTimestamp("endDate").toDate(),
				res.getTimestamp("startDate").toDate(), res.getString("rentalCompany"), res.getString("carType"),
				res.getDouble("rentalPrice")), res.getKey().getId().intValue());
	}

}
