package kr.co.saeron.gis.domain.layers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LayerInfo {
    private ResourceInfo resource;

    public ResourceInfo getResource() { return resource; }
    public void setResource(ResourceInfo resource) { this.resource = resource; }
}
