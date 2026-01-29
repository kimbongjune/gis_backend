package kr.co.saeron.gis.domain.layers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LayerResponse {
    private LayerInfo layer;

    public LayerInfo getLayer() { return layer; }
    public void setLayer(LayerInfo layer) { this.layer = layer; }
}
