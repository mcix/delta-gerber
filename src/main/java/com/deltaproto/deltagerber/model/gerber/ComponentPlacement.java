package com.deltaproto.deltagerber.model.gerber;

public class ComponentPlacement {

    private final String refdes;
    private final String value;
    private final String footprint;
    private final String mountType;
    private final double x;
    private final double y;
    private final double rotation;
    private final String side;

    public ComponentPlacement(String refdes, String value, String footprint, String mountType,
                              double x, double y, double rotation, String side) {
        this.refdes = refdes;
        this.value = value != null ? value : "";
        this.footprint = footprint != null ? footprint : "";
        this.mountType = mountType != null ? mountType : "SMD";
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.side = side != null ? side : "";
    }

    public String getRefdes()    { return refdes; }
    public String getValue()     { return value; }
    public String getFootprint() { return footprint; }
    public String getMountType() { return mountType; }
    public double getX()         { return x; }
    public double getY()         { return y; }
    public double getRotation()  { return rotation; }
    public String getSide()      { return side; }
}
