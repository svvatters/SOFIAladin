package sofia;

import java.text.DecimalFormat;

/**
 * @author shannon.watters@gmail.com
 * @SOFIA_Aladin-extension class
 */
public enum Imager {

    WFI (3.0, 7.0),
    FFI (0.50, 9.0),
    FPI (0.075, 14.0);
    
    private double fovRadius;
    private double maxOptMag;
    
    private Imager(double radius, double mag) {
        this.fovRadius = radius;
        this.maxOptMag = mag;
    }
    
    public double getFOVRadius() {
        return this.fovRadius;       
    }

    public double getMaxOptMag() {
        return this.maxOptMag;       
    }  
    
    /**
     * The distance from the Imagers' center to the corners of a square of
     * length 2 x Imagers' radius.
     * @return
     */
    public double getMaxFOVDiagonal() {
        return(Math.sqrt(
            (this.getFOVRadius() * this.getFOVRadius()) * 2));
    }
    
    /**
     * @return 2 html table rows representing the visual magnitude and 
     * distance (arcminutes) from the pointing coordinate of the Imagers
     */
    public static String getLimitsTable() {
        
        //
        String fpiMag = Double.toString(Imager.FPI.getMaxOptMag());
        String ffiMag = Double.toString(Imager.FFI.getMaxOptMag());
        String wfiMag = Double.toString(Imager.WFI.getMaxOptMag());
        
        //
        DecimalFormat myFormatter = new DecimalFormat("###.#");

        //
        String fpiArcmin = 
                myFormatter.format(Imager.FPI.getFOVRadius() * 60.0);
        String ffiArcmin = 
                myFormatter.format(Imager.FFI.getMaxFOVDiagonal() * 60.0);
        String wfiArcmin = 
                myFormatter.format(Imager.WFI.getMaxFOVDiagonal() * 60.0);  

        //
        String limits =  "<thead><tr>"
                + "<th></th>"
                + "<th>FPI</th>"
                + "<th>FFI</th>"
                + "<th>WFI</th>"  
                + "</tr></thead><tbody><tr>"
                + "<td>Max Visual Magnitude</td>" 
                + "<td>" + fpiMag + "</td>" 
                + "<td>" + ffiMag + "</td>" 
                + "<td>" + wfiMag + "</td>" 
                + "</tr><tr>"
                + "<td>Max Sky Separation from Pointing</td>" 
                + "<td>" + fpiArcmin + "\'</td>" 
                + "<td>" + ffiArcmin + "\'</td>" 
                + "<td>" + wfiArcmin + "\'</td>"
                + "</tr></tbody>";
        return limits;
    }
}
