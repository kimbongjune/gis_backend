package kr.co.saeron.gis.domain.bbox;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureTypeInfo {
    private BBox nativeBoundingBox;
    private String srs;

    public BBox getNativeBoundingBox() { return nativeBoundingBox; }
    public void setNativeBoundingBox(BBox nativeBoundingBox) {
        this.nativeBoundingBox = nativeBoundingBox;
    }

    public String getSrs() { return srs; }
    public void setSrs(String srs) { this.srs = srs; }
}
