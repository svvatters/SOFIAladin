package sofia;

public enum Imager {
    WFI (3.0),
    FFI (0.50),
    FPI (0.075);
    
    private double fovRadius;
    
    private Imager(double radius) {
        this.fovRadius = radius;
    }
    
    public double getFOVRadius() {
        return this.fovRadius;       
    }
}
