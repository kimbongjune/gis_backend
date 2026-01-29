package kr.co.saeron.gis.domain.bbox;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BBox {
    private double minx;
    private double miny;
    private double maxx;
    private double maxy;
    private Object crs;

    public double getMinx() { return minx; }
    public void setMinx(double minx) { this.minx = minx; }

    public double getMiny() { return miny; }
    public void setMiny(double miny) { this.miny = miny; }

    public double getMaxx() { return maxx; }
    public void setMaxx(double maxx) { this.maxx = maxx; }

    public double getMaxy() { return maxy; }
    public void setMaxy(double maxy) { this.maxy = maxy; }

    public Object getCrs() { return crs; }
    public void setCrs(Object crs) { this.crs = crs; }

    // Helper to get safe String code if needed
    public String getCrsCode() {
        if (crs instanceof String) return (String) crs;
        if (crs instanceof java.util.Map) {
            java.util.Map<?, ?> map = (java.util.Map<?, ?>) crs;
            Object val = map.get("$"); // Common convention in some JSON mappers for XML-like
            if (val != null) return val.toString();
            // Fallback or specific parsing for GeoServer JSON
        }
        return String.valueOf(crs);
    }
}
