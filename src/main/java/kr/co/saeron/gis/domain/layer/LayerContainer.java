package kr.co.saeron.gis.domain.layer;

import java.util.Collections;
import java.util.List;

public class LayerContainer {
    private List<LayerItem> layer;

    public LayerContainer() {}

    public LayerContainer(String value) {
        this.layer = Collections.emptyList();
    }

    public List<LayerItem> getLayer() { return layer; }
    public void setLayer(List<LayerItem> layer) { this.layer = layer; }
}
