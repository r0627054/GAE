package ds.gae;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;

public class DataStoreManager {

	private static Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public static Datastore getDataStore() {
		return datastore;
	}
}
