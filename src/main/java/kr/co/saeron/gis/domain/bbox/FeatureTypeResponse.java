package kr.co.saeron.gis.domain.bbox;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FeatureTypeResponse {
    private FeatureTypeInfo featureType;

    public FeatureTypeInfo getFeatureType() { return featureType; }
    public void setFeatureType(FeatureTypeInfo featureType) { this.featureType = featureType; }
}
