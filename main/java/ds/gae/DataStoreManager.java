package ds.gae;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

public class DataStoreManager {

	private static Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	// All client-side classes use this getter to use the same
	// datastore service.
	public static Datastore getDataStore() {
		return datastore;
	}
}
