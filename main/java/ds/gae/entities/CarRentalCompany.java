package ds.gae.entities;

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
		return null;
	}

	public boolean isAvailable(String carTypeName, Date start, Date end) {
		logger.log(Level.INFO, "<{0}> Checking availability for car type {1}", new Object[] { name, carTypeName });
		//return getAvailableCarTypes(start, end).contains(getCarType(carTypeName));
		return true;
	}

	public Set<CarType> getAvailableCarTypes(Date start, Date end) {
		/*Set<CarType> availableCarTypes = new HashSet<CarType>();
		for (Car car : getCars()) {
			if (car.isAvailable(start, end)) {
				availableCarTypes.add(car.getType());
			}
		}*/
		//return availableCarTypes;
		return null;
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
	}
	
	
	// Implementation can be subject to different pricing strategies
	private double calculateRentalPrice(double rentalPricePerDay, Date start, Date end) {
		return rentalPricePerDay * Math.ceil((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24D));
	}

}
