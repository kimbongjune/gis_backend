package kr.co.saeron.gis.domain.layers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceInfo {
    private String href;

    public String getHref() { return href; }
    public void setHref(String href) { this.href = href; }
}
