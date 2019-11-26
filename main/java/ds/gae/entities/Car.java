package ds.gae.entities;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class Car {

	private static Logger logger = Logger.getLogger(Car.class.getName());
	
	
	private int id;


	/***************
	 * CONSTRUCTOR *
	 ***************/

	public Car(int uid) {
		this.id = uid;
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

		/*for (Reservation reservation : getReservations()) {
			if (reservation.getEndDate().before(start) || reservation.getStartDate().after(end)) {
				continue;
			}
			return false;
		}*/
		//TODO
		return true;
	}

	public void addReservation(Reservation res) {
		//reservations.add(res);
		//TODO
	}

	public void removeReservation(Reservation reservation) {
		//reservations.remove(reservation);
		//TODO
	}
	
	
	
	/*public Quote createQuote(ReservationConstraints constraints, String client) throws ReservationException {
	logger.log(Level.INFO, "<{0}> Creating tentative reservation for {1} with constraints {2}",
			new Object[] { name, client, constraints.toString() });

	CarType type = getCarType(constraints.getCarType());

	if (!isAvailable(constraints.getCarType(), constraints.getStartDate(), constraints.getEndDate())) {
		throw new ReservationException("<" + name + "> No cars available to satisfy the given constraints.");
	}

	double price = calculateRentalPrice(
			type.getRentalPricePerDay(), 
			constraints.getStartDate(),
			constraints.getEndDate()
	);

	return new Quote(
			client,
			constraints.getStartDate(),
			constraints.getEndDate(),
			getName(),
			constraints.getCarType(),
			price
	);
}*/

// Implementation can be subject to different pricing strategies
private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
	return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24D));
}

	public Reservation confirmQuote(Quote quote)  {
	//logger.log(Level.INFO, "<{0}> Reservation of {1}", new Object[] { name, quote.toString() });
	/*List<Car> availableCars = getAvailableCars(quote.getCarType(), quote.getStartDate(), quote.getEndDate());
	if (availableCars.isEmpty()) {
		throw new ReservationException("Reservation failed, all cars of type " + quote.getCarType()
				+ " are unavailable from " + quote.getStartDate() + " to " + quote.getEndDate());
	}
	Car car = availableCars.get((int) (Math.random() * availableCars.size()));

	Reservation res = new Reservation(quote, car.getId());
	car.addReservation(res);
	return res;*/
	return null;
}

public void cancelReservation(Reservation res) {
	//logger.log(Level.INFO, "<{0}> Cancelling reservation {1}", new Object[] { name, res.toString() });
	//getCar(res.getCarId()).removeReservation(res);
}
	
	
	
	
	
	
	
}
