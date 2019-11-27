package ds.gae.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;

import ds.gae.CarRentalModel;

public class CarRentalServletContextListener implements ServletContextListener {

	private Datastore datastore = CarRentalModel.getDatastore();

	private int carCounter = 0;

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// This will be invoked as part of a warming request,
		// or the first user request if no warming request was invoked.

		// check if dummy data is available, and add if necessary
		if (!isDummyDataAvailable()) {
			System.out.println("Dummy data not available.");
			addDummyData();
		} else {
			System.out.println("Dummy data available.");
		}

		// check that correct launch configuration is used
		if ("distributed-systems-gae".equals(System.getenv("DATASTORE_DATASET"))) {
			Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.INFO,
					"Launch configuration correctly loaded");
		} else {
			Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.SEVERE,
					"Launch configuration did not load! Restart using the correct launch configuration.");
			throw new RuntimeException("Launch configuration did not load!");
		}
	}

	private boolean isDummyDataAvailable() {
		// If the Hertz car rental company is in the datastore, we assume the dummy data
		// is available
		return CarRentalModel.get().getAllRentalCompanyNames().contains("Hertz");

	}

	private void addDummyData() {
		loadRental("Hertz", "hertz.csv");
		loadRental("Dockx", "dockx.csv");
	}

	private void loadRental(String name, String datafile) {
		Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.INFO, "loading {0} from file {1}",
				new Object[] { name, datafile });
		try {

			// THIS HAS TO BE IMPLEMENTED
			// Set<Car> cars = loadData(name, datafile);
			// CarRentalCompany company = new CarRentalCompany(name, cars);
			// Storing a car rental company

			System.out.println("Loading " + name + "...");
			Key crcKey = this.getDatastore().newKeyFactory().setKind("CarRentalCompany").newKey(name);

			Entity crcEntity = Entity.newBuilder(crcKey).set("name", name).build();
			this.getDatastore().put(crcEntity);
			loadData(name, datafile);

			// CarRentalModel.get().CRCS.put(name, company);
		} catch (NumberFormatException ex) {
			Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.SEVERE, "bad file", ex);
		} catch (IOException ex) {
			Logger.getLogger(CarRentalServletContextListener.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public void loadData(String name, String datafile) throws NumberFormatException, IOException {
		int carId = 1;
		// open file from jar
		BufferedReader in = new BufferedReader(new InputStreamReader(
				CarRentalServletContextListener.class.getClassLoader().getResourceAsStream(datafile)));
		// while next line exists
		while (in.ready()) {
			// read line
			String line = in.readLine();
			// if comment: skip
			if (line.startsWith("#")) {
				continue;
			}
			// tokenize on ,
			StringTokenizer csvReader = new StringTokenizer(line, ",");
			// create new car type from first 5 fields
			// storing a car type
			String carTypeName = csvReader.nextToken();
			Key carTypeKey = this.getDatastore().newKeyFactory().addAncestors(PathElement.of("CarRentalCompany", name))
					.setKind("CarType").newKey(carTypeName);

			Entity carTypeEntity = Entity.newBuilder(carTypeKey)
					.set("nbOfSeats", Integer.parseInt(csvReader.nextToken()))
					.set("trunkSpace", Float.parseFloat(csvReader.nextToken()))
					.set("rentalPricePerDay", Double.parseDouble(csvReader.nextToken()))
					.set("smokingAllowed", Boolean.parseBoolean(csvReader.nextToken())).build();
			this.getDatastore().put(carTypeEntity);

			for (int i = Integer.parseInt(csvReader.nextToken()); i > 0; i--) {
				Key carKey = this.getDatastore().newKeyFactory()
						.addAncestors(PathElement.of("CarType", carTypeName), PathElement.of("CarRentalCompany", name))
						.setKind("Car").newKey(++carCounter);

				Entity carEntity = Entity.newBuilder(carKey).set("id", carCounter).set("carRentalCompanyName", name)
						.build();

				this.getDatastore().put(carEntity);
			}
		}

	}

	/*
	 * public static Set<Car> loadData(String name, String datafile) throws
	 * NumberFormatException, IOException { Set<Car> cars = new HashSet<Car>(); int
	 * carId = 1;
	 * 
	 * // open file from jar BufferedReader in = new BufferedReader(new
	 * InputStreamReader(
	 * CarRentalServletContextListener.class.getClassLoader().getResourceAsStream(
	 * datafile))); // while next line exists while (in.ready()) { // read line
	 * String line = in.readLine(); // if comment: skip if (line.startsWith("#")) {
	 * continue; } // tokenize on , StringTokenizer csvReader = new
	 * StringTokenizer(line, ","); // create new car type from first 5 fields
	 * CarType type = new CarType(csvReader.nextToken(),
	 * Integer.parseInt(csvReader.nextToken()),
	 * Float.parseFloat(csvReader.nextToken()),
	 * Double.parseDouble(csvReader.nextToken()),
	 * Boolean.parseBoolean(csvReader.nextToken())); // create N new cars with given
	 * type, where N is the 5th field for (int i =
	 * Integer.parseInt(csvReader.nextToken()); i > 0; i--) { cars.add(new
	 * Car(carId++, type)); } }
	 * 
	 * return cars; }
	 */

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// Please leave this method empty.
	}

	public Datastore getDatastore() {
		return datastore;
	}

}
