package sofia.dcs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import cds.aladin.Aladin;
import cds.aladin.AladinData;
import cds.aladin.AladinException;
import cds.aladin.Obj;
import cds.aladin.SOFIA_Aladin;
import sofia.Imager;
import sofia.Vizier;
import swatters.SwattersUtils;

/**
 * @author shannon.watters@gmail.com
 * @SOFIA_Aladin-extension class
 */
public final class TrackStarReport {
    
    // TODO: Replace the enums (don't think they're needed)
    enum ResourceFile {
        $HTML_TEMPLATE_FILE("TrackStarReport_body.html");
//        $JQUERY_FILE("jquery-1.11.1.min.js"),
//        $CSS_FILE("css/styles.css");
//        $MAINSCRIPT_FILE("pointingReport.js"),
//        $TABLE_SORTER_FILE("jquery.tablesorter.min.js");
        
        private String filepath;        
        private ResourceFile(String filepath) {
            this.filepath = filepath;
        }      
        public String getValue() {
            return this.filepath;
        }
    }
    
    private final Pointing pointing;
    private final AladinData[] hip2ImagerPlanes;
    private final AladinData[] ucac4ImagerPlanes;
    private Document htmlReport;
    private int fpi_hip2ObjCount;
    private int fpi_ucac4ObjCount;
    private int ffi_hip2ObjCount;
    private int ffi_ucac4ObjCount;
    private int wfi_hip2ObjCount;
    private int wfi_ucac4ObjCount;
    
    /**
     * @param p
     * @throws IOException
     * @throws AladinException 
     */
    public TrackStarReport(Pointing p) throws IOException, AladinException {       
        this(p, cds.aladin.Aladin.launch("-nogui"));
        
    }    
    /**
     * @param p
     * @param a
     * @throws IOException
     * @throws AladinException 
     */
    public TrackStarReport(Pointing p, Aladin a) 
            throws IOException, AladinException {
        
        System.out.println("Initializing report for:\n " + p.toString());
        
        //
        this.pointing = p;
        
        //
        String fpiPlane = Imager.FPI.toString();
        String ffiPlane = Imager.FFI.toString();
        String wfiPlane = Imager.WFI.toString();

        // Clear any previous planes from Aladin
        a.execCommand("rm all");           
        
        // Make stack directories for each imager
        a.execCommand("md " + fpiPlane);
        a.execCommand("md " + ffiPlane);
        a.execCommand("md " + wfiPlane);
        
        // Add the pointing to an Aladin plane (or create the plane)
        SOFIA_Aladin.addObjToStack(a, pointing);
        
        /*
         *  Create Aladin planes with objects from HIP2 and UCAC4 
         *  within the radii & max magnitude for each imager.  
         */        
        String hip2Plane = SOFIA_Aladin.queryVizier(a, pointing, 
                                    Vizier.HIP2, 
                                    Imager.WFI.getMaxFOVDiagonal(), 
                                    Imager.FPI.getMaxOptMag());  
        String ucac4Plane = SOFIA_Aladin.queryVizier(a, pointing, 
                                    Vizier.UCAC4, 
                                    Imager.FFI.getMaxFOVDiagonal(), 
                                    Imager.FPI.getMaxOptMag());  
 
        /*
         * Filter out potential tracking stars to new planes and delete the
         * larger catalog planes
         */
        hip2ImagerPlanes = 
                SOFIA_Aladin.selectTrackStars(a, hip2Plane, pointing);
        ucac4ImagerPlanes = 
                SOFIA_Aladin.selectTrackStars(a, ucac4Plane, pointing);            
        a.execCommand("rm " + hip2Plane);
        a.execCommand("rm " + ucac4Plane);
        
        // Summarize the number of potential track stars in each plane
        fpi_hip2ObjCount = hip2ImagerPlanes[0].getNbObj();
        fpi_ucac4ObjCount = ucac4ImagerPlanes[0].getNbObj();
        ffi_hip2ObjCount = hip2ImagerPlanes[1].getNbObj();
        ffi_ucac4ObjCount = ucac4ImagerPlanes[1].getNbObj();
        wfi_hip2ObjCount = hip2ImagerPlanes[2].getNbObj();
        wfi_ucac4ObjCount = ucac4ImagerPlanes[2].getNbObj();

        //
        this.setHtmlReport();
        
        //
        a.execCommand("mv " + hip2ImagerPlanes[0].getLabel() + " " + fpiPlane);
        a.execCommand("mv " + hip2ImagerPlanes[1].getLabel() + " " + ffiPlane);
        a.execCommand("mv " + hip2ImagerPlanes[2].getLabel() + " " + wfiPlane); 
        a.execCommand("mv " + ucac4ImagerPlanes[0].getLabel() + " " + fpiPlane);
        a.execCommand("mv " + ucac4ImagerPlanes[1].getLabel() + " " + ffiPlane);
        a.execCommand("mv " + ucac4ImagerPlanes[2].getLabel() + " " + wfiPlane);
    }
    /**
     * @param p
     * @param a
     * @param f
     * @throws IOException
     * @throws AladinException
     */
    public TrackStarReport(Pointing p, Aladin a, String f) 
            throws IOException, AladinException {        
        this(p, a);
        // Save the report in a .aj (aladin) file
        a.execCommand("backup " + f);
    }

    private String aladinDataToTable(AladinData ad) throws AladinException {
        StringBuilder sb = new StringBuilder();
    
        //
        if (ad.getNbObj() == 0) return sb.toString();
        
        Obj[] objects = ad.seeObj();
        
        sb.append("<table id=\"" + ad.getLabel().toLowerCase() 
                + "\" class=\"data-table\">");
        sb.append("<thead>");
        
        
        
        sb.append("<tr>");  
        String[] names = objects[0].getNames();
        for (int i=0; i < names.length; i++) {
            sb.append("<th>" + names[i]+ "</th>");
        }
        sb.append("</tr>");  
        
        sb.append("</thead>");        
        sb.append("<tbody>");
    
        //
        for (int row=0; row < ad.getNbObj(); row++) {
            sb.append("<tr>");   
            objects[row].getNames();
            String[] vals = objects[row].getValues();
            for (int col=0; col < vals.length; col++) {              
                sb.append("<td>");
                sb.append(vals[col]);
                sb.append("</td>");
            }
            sb.append("</tr>\n");
        }        
        
        sb.append("</tbody>");
        sb.append("</table>");
    
        return sb.toString();
    }
    /**
     * @throws AladinException 
     * @throws IOException 
     * 
     */
    private void setHtmlReport() throws AladinException, IOException {
            
        // Load the html template
        InputStream head = this.getClass().getResourceAsStream("head.html");
        htmlReport = Jsoup.parse(head, "utf-8", "./");
        head.close();
        htmlReport.select("title").html(
                                "Tracking Report - " + pointing.getObsID());
        /*
         *  TODO: This wouldn't always be a good for <base> if this isn't a
         *  subreport (if it's not initiated by a batch reporter then the 
         *  css files etc might not be relative to '../' 
         */
        htmlReport.select("head").first().prepend("<base href=\"../\">");
        InputStream template = this.getClass().getResourceAsStream(
                                ResourceFile.$HTML_TEMPLATE_FILE.getValue());
        String htmlString = SwattersUtils.streamToString(template);
        template.close();
        htmlReport.select("body").html(htmlString);
        
        // 
        DecimalFormat myFormatter = new DecimalFormat("###.######");
        String ra = myFormatter.format(pointing.getLon());
        String dec = myFormatter.format(pointing.getLat());
    
        //
        htmlReport.select("#object-name").html(pointing.getObjName());
        htmlReport.select("#object-ra").html(ra);
        htmlReport.select("#object-dec").html(dec);
        htmlReport.select("#object-obsID").html(pointing.getObsID());
        htmlReport.select("#object-instrument").html(
                pointing.getInstrumentID());
        htmlReport.select("#object-configuration").html(
                pointing.getConfiguration());
    
        //        
        htmlReport.select("#criteria.table").append(Imager.getLimitsTable());
    
        //
        htmlReport.select("#summary.table").append(this.getCountTable());
            
        // TODO: move the tables information somewhere with a larger scope
        String[] tables = {"#fpi-tables", "#ffi-tables", "#wfi-tables"};
        
        //
        String panel =  "<div class=\"panel data-panel panel-primary\">"
                        + "<div class=\"panel-body\">";
        
        //
        for (int i=0; i<3; i++) {
            
            //
            if (this.ucac4ImagerPlanes[i].getNbObj() > 0) {
                String ucac4 = aladinDataToTable(this.ucac4ImagerPlanes[i]);                
                htmlReport.select(tables[i]).append(panel);
                htmlReport.select(tables[i]).select(
                        ".data-panel .panel-body").last().append(ucac4);  
            }        
            if (this.hip2ImagerPlanes[i].getNbObj() > 0) {
                String hip2 = aladinDataToTable(this.hip2ImagerPlanes[i]);
                htmlReport.select(tables[i]).append(panel);
                htmlReport.select(tables[i]).select(
                        ".data-panel .panel-body").last().append(hip2);
            } 
            
            // Add classes for bootstrap formatting of the track star tables
            htmlReport.getElementsByClass("data-table").addClass(
                    "table display table-bordered table-nowrap");
        }      
    }
    
    /**
     * @param outFile
     * @throws IOException
     */
    void writeToHTML(File outFile) throws IOException {
        SwattersUtils.stringToFile(htmlReport.toString(), outFile);                                        
    }
    
    /**
     * @param a
     * @param f
     * @throws IOException
     */
    void writeToAladin(Aladin a, File outFile) throws IOException {       
        a.execCommand("backup " + outFile);
    }

    /**
     * @return
     */
    String getCountTable() {        
        return ("<thead><tr>"
                + "<th></th>"
                + "<th>FPI</th>"
                + "<th>FFI</th>"
                + "<th>WFI</th>"
                + "</tr></thead><tbody><tr>" 
                + "<td>UCAC4</td>" 
                + "<td>" + this.fpi_ucac4ObjCount + "</td>" 
                + "<td>" + this.ffi_ucac4ObjCount + "</td>"
                + "<td>" + this.wfi_ucac4ObjCount + "</td>"
                + "</tr><tr>" 
                + "<td>Hip2</td>"
                + "<td>" + this.fpi_hip2ObjCount + "</td>"
                + "<td>" + this.ffi_hip2ObjCount + "</td>"
                + "<td>" + this.wfi_hip2ObjCount + "</td>"
                + "</tr></tbody>");        
    }
    /**
     * @return
     */
    Pointing getPointing() {
        return pointing;
    }
    
    /**
     * @return
     */
    AladinData[] getHip2ImagerPlanes() {
        return hip2ImagerPlanes;
    }
    
    /**
     * @return
     */
    AladinData[] getUcac4ImagerPlanes() {
        return ucac4ImagerPlanes;
    }
    
    int getFpi_hip2ObjCount() {
        return fpi_hip2ObjCount;
    }
    
    int getFpi_ucac4ObjCount() {
        return fpi_ucac4ObjCount;
    }
    
    int getFfi_hip2ObjCount() {
        return ffi_hip2ObjCount;
    }
    
    int getFfi_ucac4ObjCount() {
        return ffi_ucac4ObjCount;
    }
    
    int getWfi_hip2ObjCount() {
        return wfi_hip2ObjCount;
    }
    
    int getWfi_ucac4ObjCount() {
        return wfi_ucac4ObjCount;
    }
 
    // DEBUGGING & TESTING
    public static void main (String[] args) throws IOException {
        System.out.println("cds.aladin.TrackStarReport");
    }
}
