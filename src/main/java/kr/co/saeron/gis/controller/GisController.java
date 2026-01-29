package kr.co.saeron.gis.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.co.saeron.gis.domain.bbox.BBox;
import kr.co.saeron.gis.service.GeoServerService;

@RestController
@RequestMapping("/api/geo")
@io.swagger.v3.oas.annotations.tags.Tag(name = "GIS Controller", description = "GeoServer 연동 및 GIS 관련 API")
public class GisController {

    private final GeoServerService geoServerService;

    public GisController(GeoServerService geoServerService) {
        this.geoServerService = geoServerService;
    }

    // 1. 워크스페이스 목록 API
    @io.swagger.v3.oas.annotations.Operation(summary = "워크스페이스 목록 조회", description = "GeoServer의 모든 워크스페이스 목록을 반환합니다.")
    @GetMapping("/workspaces")
    public ResponseEntity<List<String>> getWorkspaces() {
        return ResponseEntity.ok(geoServerService.getWorkspaces());
    }

    // 2. 레이어 목록 API
    @io.swagger.v3.oas.annotations.Operation(summary = "레이어 목록 조회", description = "특정 워크스페이스의 모든 레이어 목록을 반환합니다.")
    @GetMapping("/workspaces/{workspaceName}/layers")
    public ResponseEntity<List<String>> getLayers(@PathVariable String workspaceName) {
        return ResponseEntity.ok(geoServerService.getLayers(workspaceName));
    }

    // 3. 레이어 Extent API (Optional, for auto-zoom)
    @io.swagger.v3.oas.annotations.Operation(summary = "레이어 Extent 조회", description = "지정된 레이어의 BBox(Extent) 및 좌표계(CRS) 정보를 반환합니다.")
    @GetMapping("/workspaces/{workspaceName}/layers/{layerName}/extent")
    public ResponseEntity<BBox> getLayerExtent(
            @PathVariable String workspaceName, 
            @PathVariable String layerName) {
        return ResponseEntity.ok(geoServerService.getLayerExtent(workspaceName, layerName));
    }

    // 4. 데이터스토어 목록 API
    @io.swagger.v3.oas.annotations.Operation(summary = "데이터스토어 목록 조회", description = "특정 워크스페이스의 데이터스토어 목록을 반환합니다.")
    @GetMapping("/workspaces/{workspaceName}/datastores")
    public ResponseEntity<List<String>> getDatastores(@PathVariable String workspaceName) {
        return ResponseEntity.ok(geoServerService.getDatastores(workspaceName));
    }

    // 4. Shapefile Upload API
    @io.swagger.v3.oas.annotations.Operation(summary = "Shapefile 발행", description = "압축된 Shapefile(zip)을 GeoServer에 발행합니다.")
    @org.springframework.web.bind.annotation.PostMapping(value = "/publish", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> publishShapefile(
            @org.springframework.web.bind.annotation.RequestParam("workspace") String workspace,
            @org.springframework.web.bind.annotation.RequestParam("storeName") String storeName,
            @org.springframework.web.bind.annotation.RequestParam(value = "crs", defaultValue = "EPSG:5186") String crs,
            @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        
        boolean success = geoServerService.publishShapefile(workspace, storeName, crs, file);
        if (success) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Shapefile published successfully.");
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to publish shapefile.");
        }
    }
}
