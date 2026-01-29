package kr.co.saeron.gis.domain.datastore;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DatastoreListResponse {
    private Datastores dataStores;

    public Datastores getDataStores() {
        return dataStores;
    }

    public void setDataStores(Datastores dataStores) {
        this.dataStores = dataStores;
    }

    public static class Datastores {
        @JsonProperty("dataStore")
        private List<DatastoreItem> dataStore;

        public List<DatastoreItem> getDataStore() {
            return dataStore;
        }

        public void setDataStore(List<DatastoreItem> dataStore) {
            this.dataStore = dataStore;
        }
    }
}
