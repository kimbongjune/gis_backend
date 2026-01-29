package kr.co.saeron.gis.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.data.SimpleFeatureStore;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import kr.co.saeron.gis.domain.bbox.BBox;
import kr.co.saeron.gis.domain.bbox.FeatureTypeResponse;
import kr.co.saeron.gis.domain.layer.*;
import kr.co.saeron.gis.domain.layers.LayerResponse;
import kr.co.saeron.gis.domain.workspace.*;
import kr.co.saeron.gis.service.GeoServerService;
import kr.co.saeron.gis.domain.datastore.DatastoreListResponse;
import kr.co.saeron.gis.domain.datastore.DatastoreItem;

@Service
public class GeoServerServiceImpl implements GeoServerService {

    private final RestClient restClient;

    @Value("${spring.datasource.url}") private String dbUrl; 
    @Value("${spring.datasource.username}") private String dbUser;
    @Value("${spring.datasource.password}") private String dbPass;
    @Value("${postgis.schema:public}") private String dbSchema;

    private String dbHost = "localhost";
    private int dbPort = 5432;
    private String dbName = "gis";

    public GeoServerServiceImpl(
            RestClient.Builder builder,
            @Value("${geoserver.url}") String gsUrl,
            @Value("${geoserver.user}") String gsUser,
            @Value("${geoserver.password}") String gsPass) {
        
        String auth = gsUser + ":" + gsPass;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        this.restClient = builder
                .baseUrl(gsUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    
    private void parseDbConnection() {
        if (dbUrl != null && dbUrl.startsWith("jdbc:postgresql://")) {
            String clean = dbUrl.replace("jdbc:postgresql://", "");
            String[] parts = clean.split("/");
            if (parts.length >= 2) {
                String[] hostPort = parts[0].split(":");
                this.dbHost = hostPort[0];
                if (hostPort.length > 1) {
                    this.dbPort = Integer.parseInt(hostPort[1]);
                }
                this.dbName = parts[1].split("\\?")[0];
            }
        }
    }

    @Override
    public BBox getLayerExtent(String workspace, String layerName) {
        try {
            LayerResponse layerRes = restClient.get()
                    .uri("/rest/workspaces/{workspace}/layers/{layerName}.json", workspace, layerName)
                    .retrieve()
                    .body(LayerResponse.class);
            
            if (layerRes == null || layerRes.getLayer() == null) return new BBox();

            String resourceUrl = layerRes.getLayer().getResource().getHref();

            FeatureTypeResponse featureRes = restClient.get()
                    .uri(resourceUrl)
                    .retrieve()
                    .body(FeatureTypeResponse.class);

            if (featureRes != null && featureRes.getFeatureType() != null) {
                return featureRes.getFeatureType().getNativeBoundingBox();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BBox();
    }

    @Override
    public List<String> getWorkspaces() {
        try {
            WorkspaceListResponse response = restClient.get()
                    .uri("/rest/workspaces.json")
                    .retrieve()
                    .body(WorkspaceListResponse.class);
            
            if (response != null && response.getWorkspaces() != null) {
                return response.getWorkspaces().getWorkspace().stream()
                        .map(WorkspaceItem::getName).collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getLayers(String workspace) {
        try {
            LayerListResponse response = restClient.get()
                    .uri("/rest/workspaces/{workspace}/layers.json", workspace)
                    .retrieve()
                    .body(LayerListResponse.class);
            
            if (response != null && response.getLayers() != null) {
                return response.getLayers().getLayer().stream()
                        .map(LayerItem::getName).collect(Collectors.toList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getDatastores(String workspace) {
        try {
            DatastoreListResponse response = restClient.get()
                    .uri("/rest/workspaces/{workspace}/datastores.json", workspace)
                    .retrieve()
                    .body(DatastoreListResponse.class);
            
            if (response != null && response.getDataStores() != null) {
                return response.getDataStores().getDataStore().stream()
                        .map(DatastoreItem::getName).collect(Collectors.toList());
            }
        } catch (Exception e) {
             e.printStackTrace();
        }
        return Collections.emptyList();
    }

    @Override
    public boolean publishShapefile(String workspace, String datastoreName, String crs, MultipartFile zipFile) {
        parseDbConnection();

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("geo_upload_");
            File shpFile = extractZip(zipFile, tempDir);
            
            if (shpFile == null) throw new RuntimeException("ZIP 파일 내에 .shp 파일이 없습니다.");
            
            String tableName = shpFile.getName().replace(".shp", "").toLowerCase();

            // 1. PostGIS Import
            importToPostgis(shpFile, tableName);

            // 2. DataStore Create
            createPostgisDataStore(workspace, datastoreName);

            // 3. Layer Publish (Using Hardcoded CRS same as Web-Test)
            publishPostgisLayer(workspace, datastoreName, tableName);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (tempDir != null) {
                try (Stream<Path> walk = Files.walk(tempDir)) {
                    walk.map(Path::toFile).forEach(File::delete);
                } catch (IOException ignored) {}
            }
        }
    }

    private File extractZip(MultipartFile zipFile, Path destDir) throws IOException {
        File shpFile = null;
        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = destDir.resolve(entry.getName()).toFile();
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    Files.copy(zis, newFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    if (entry.getName().toLowerCase().endsWith(".shp")) {
                        shpFile = newFile;
                    }
                }
            }
        }
        return shpFile;
    }

    private void importToPostgis(File shpFile, String tableName) throws Exception {
        Map<String, Object> shpParams = new HashMap<>();
        shpParams.put("url", shpFile.toURI().toURL());
        shpParams.put("charset", Charset.forName("EUC-KR"));
        
        DataStore shpStore = DataStoreFinder.getDataStore(shpParams);
        String typeName = shpStore.getTypeNames()[0];
        SimpleFeatureSource featureSource = shpStore.getFeatureSource(typeName);

        Map<String, Object> pgParams = new HashMap<>();
        pgParams.put("dbtype", "postgis");
        pgParams.put("host", dbHost);
        pgParams.put("port", dbPort);
        pgParams.put("database", dbName);
        pgParams.put("schema", dbSchema);
        pgParams.put("user", dbUser);
        pgParams.put("passwd", dbPass);
        // NO "expose primary keys" here - matches Web-Test

        DataStore pgStore = DataStoreFinder.getDataStore(pgParams);

        try {
            SimpleFeatureType originalSchema = featureSource.getSchema();
            SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
            builder.init(originalSchema);
            builder.setName(tableName);
            
            SimpleFeatureType newSchema = builder.buildFeatureType();

            // Web-Test only does createSchema without drop logic
            pgStore.createSchema(newSchema); 
            
            SimpleFeatureStore featureStore = (SimpleFeatureStore) pgStore.getFeatureSource(tableName);
            featureStore.addFeatures(featureSource.getFeatures());
            
        } catch (Exception e) {
            System.err.println("PostGIS 입력 중 오류: " + e.getMessage());
            throw e;
        } finally {
            shpStore.dispose();
            pgStore.dispose();
        }
    }

    private void createPostgisDataStore(String workspace, String dsName) {
        Map<String, Object> connParams = new HashMap<>();
        connParams.put("host", dbHost);
        connParams.put("port", dbPort);
        connParams.put("database", dbName);
        connParams.put("user", dbUser);
        connParams.put("passwd", dbPass);
        connParams.put("dbtype", "postgis");
        connParams.put("schema", dbSchema);
        connParams.put("validate connections", true); // Web-Test had this TRUE

        Map<String, Object> dataStore = new HashMap<>();
        dataStore.put("name", dsName);
        dataStore.put("connectionParameters", connParams);

        Map<String, Object> root = new HashMap<>();
        root.put("dataStore", dataStore);

        try {
            restClient.post()
                    .uri("/rest/workspaces/{workspace}/datastores", workspace)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(root)
                    .retrieve()
                    .toBodilessEntity();
            
        } catch (HttpClientErrorException.Conflict | HttpServerErrorException e) {
            try {
                restClient.put()
                        .uri("/rest/workspaces/{workspace}/datastores/{dsName}", workspace, dsName)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(root)
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception updateEx) {
                updateEx.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create DataStore");
        }
    }

    // Hardcoded CRS as per Web-Test
    private void publishPostgisLayer(String workspace, String dsName, String tableName) {
        Map<String, Object> featureType = new HashMap<>();
        featureType.put("name", tableName);       
        featureType.put("nativeName", tableName); 
        featureType.put("srs", "EPSG:5186"); // Matches Web-Test hardcode
        featureType.put("projectionPolicy", "FORCE_DECLARED");
        featureType.put("enabled", true);

        Map<String, Object> root = new HashMap<>();
        root.put("featureType", featureType);

        try {
             restClient.post()
                .uri(uriBuilder -> uriBuilder
                    .path("/rest/workspaces/{workspace}/datastores/{dsName}/featuretypes")
                    .queryParam("recalculate", "nativebbox,latlonbbox")
                    .build(workspace, dsName))
                .contentType(MediaType.APPLICATION_JSON)
                .body(root)
                .retrieve()
                .toBodilessEntity();
        } catch (HttpClientErrorException.Conflict e) {
             try {
                restClient.put() 
                    .uri(uriBuilder -> uriBuilder
                        .path("/rest/workspaces/{workspace}/datastores/{dsName}/featuretypes/{featureTypeName}")
                        .queryParam("recalculate", "nativebbox,latlonbbox")
                        .build(workspace, dsName, tableName))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(root)
                    .retrieve()
                    .toBodilessEntity();
             } catch (Exception updateEx) {
                 updateEx.printStackTrace();
             }
        }
    }
}
