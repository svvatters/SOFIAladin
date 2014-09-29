package sofia;

public enum SciInstrument {

    FORCAST_modeA (0.0, 0.0, "abc"),
    FORCAST_modeB (0.0, 0.0, "abc"),
    FORCAST_modeC (0.0, 0.0, "abc"),
    GREAT_modeA (0.0, 0.0, "abc"),
    GREAT_modeB (0.0, 0.0, "abc"),
    GREAT_modeC (0.0, 0.0, "abc"),
    FLITECAM_modeA (0.0, 0.0, "abc"),
    FLITECAM_modeB (0.0, 0.0, "abc"),
    FLITECAM_modeC (0.0, 0.0, "abc"),
    HIPO_modeA (0.0, 0.0, "abc"),
    HIPO_modeB (0.0, 0.0, "abc"),
    HIPO_modeC (0.0, 0.0, "abc"),
    FLIPO_modeA (0.0, 0.0, "abc"),
    FLIPO_modeB (0.0, 0.0, "abc"),
    FLIPO_modeC (0.0, 0.0, "abc"),
    EXES (0.0, 0.0, "abc"),
    HAWK (0.0, 0.0, "abc");
    
    private double offsetAngle;
    private double second;
    private String third;
    
    private SciInstrument(double offsetAngle, double second, String third) {
        this.offsetAngle = offsetAngle;
        this.second = second;
        this.third = third;
    }

    public double getOffsetAngle() {
        return offsetAngle;
    }

    public double getSecond() {
        return second;
    }

    public String getThird() {
        return third;
    }
}
