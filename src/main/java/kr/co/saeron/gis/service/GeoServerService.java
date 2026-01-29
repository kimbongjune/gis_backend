package kr.co.saeron.gis.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import kr.co.saeron.gis.domain.bbox.BBox;

public interface GeoServerService {
    
    // 워크스페이스 목록 조회
    List<String> getWorkspaces();

    // 특정 워크스페이스의 레이어 목록 조회
    List<String> getLayers(String workspace);

    // 특정 레이어의 영역(Extent) 정보 조회
    BBox getLayerExtent(String workspace, String layerName);
    
    // 특정 워크스페이스의 데이터스토어 목록 조회
    List<String> getDatastores(String workspace);
    
    // 레이어 발행 (with CRS)
    boolean publishShapefile(String workspace, String datastoreName, String crs, MultipartFile file);
}
