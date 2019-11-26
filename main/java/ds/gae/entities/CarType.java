package ds.gae.entities;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.cloud.datastore.Entity;

public class CarType {

	private String name;
	private int nbOfSeats;
	private boolean smokingAllowed;
	private double rentalPricePerDay;
	// trunk space in liters
	private float trunkSpace;

	/***************
	 * CONSTRUCTOR *
	 ***************/

	public CarType(String name, int nbOfSeats, float trunkSpace, double rentalPricePerDay, boolean smokingAllowed) {
		this.name = name;
		this.nbOfSeats = nbOfSeats;
		this.trunkSpace = trunkSpace;
		this.rentalPricePerDay = rentalPricePerDay;
		this.smokingAllowed = smokingAllowed;
	}

	public String getName() {
		return name;
	}

	public int getNbOfSeats() {
		return nbOfSeats;
	}

	public boolean isSmokingAllowed() {
		return smokingAllowed;
	}

	public double getRentalPricePerDay() {
		return rentalPricePerDay;
	}

	public float getTrunkSpace() {
		return trunkSpace;
	}

	/*************
	 * TO STRING *
	 *************/

	@Override
	public String toString() {
		return String.format("Car type: %s \t[seats: %d, price: %.2f, smoking: %b, trunk: %.0fl]", getName(),
				getNbOfSeats(), getRentalPricePerDay(), isSmokingAllowed(), getTrunkSpace());
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CarType other = (CarType) obj;
		if (!Objects.equals(name, other.name)) {
			return false;
		}
		return true;
	}

	private Car getCar(int uid) {
		/*
		 * for (Car car : cars) { if (car.getId() == uid) { return car; } } throw new
		 * IllegalArgumentException("<" + name + "> No car with uid " + uid);
		 */
		return null;
	}

	public Set<Car> getCars() {
		// return cars;
		return null;
	}

	private List<Car> getAvailableCars(String carType, Date start, Date end) {
		/*
		 * List<Car> availableCars = new LinkedList<Car>(); for (Car car : cars) { if
		 * (car.getType().getName().equals(carType) && car.isAvailable(start, end)) {
		 * availableCars.add(car); } } return availableCars;
		 */
		return null;
	}

	public static CarType parse(Entity e) {
		return new CarType(e.getKey().getName(), (int) Math.round(e.getLong("nbOfSeats")),
				(float) e.getDouble("trunkSpace"), e.getDouble("rentalPricePerDay"), e.getBoolean("smokingAllowed"));
	}

}
