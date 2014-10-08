package sofia.dcs;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.nio.file.Files;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.bind.JAXBException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import cds.aladin.Aladin;
import cds.aladin.AladinException;
import sofia.Imager;
import swatters.SwattersUtils;
import swatters.swing.TextAreaOutputStream;

/**
 * @author shannon.watters@gmail.com
 *
 */
public final class ObsPlanReporter {

    // HTML summary of all the track star reports
    private Document batchReport;

    // Initialize the JFileChooser to use the same instance throughout
    private final JFileChooser fc = new JFileChooser();
    
    // Parts of the GUI log console
    private JFrame frame = new JFrame();
    private JTextArea textArea = new JTextArea();

    ObsPlanReporter() {
        super();
    }
    
    	private void createTrackReports(Set<Pointing> pointings, File outDir) 
    	        throws IOException, AladinException {

        /*
         *  Using Aladin to save initial data collection for MOPS flight prep.
         *  Also using Aladin for it's built in query and filter commands.
         */
    	    Aladin a;

    	    //
    	    File tempFile;
  	    
        //Check for pointings dirs and create it if needed.
        if (!outDir.exists()) {
            Files.createDirectory(outDir.toPath());
        }
       
        // Create a simple stdout indeterminate progress bar
        System.out.print("Starting reporter...");
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);       
        @SuppressWarnings("unused")
        ScheduledFuture<?> sf = ses.scheduleWithFixedDelay(new Runnable() {
                public void run() {
                    System.out.print(".");
                }
            }, 
            0, 500, TimeUnit.MILLISECONDS);
        
        // Instantiate Aladin and set its level of verbosity
        a = cds.aladin.Aladin.launch("-nogui");
        a.execCommand("trace 0");

        // Shutdown the ScheduledExecutorService (indeterminate progress bar)
        ses.shutdown();
    	        	    
        /*
         *  Set the filename and create a temp pointingReportCollection 
         *  html file which will be deleted when the program exits normally
         */
        Timestamp time = new Timestamp(new java.util.Date().getTime());
        String prefix = new SimpleDateFormat("yyyyMMddHHmm").format(time);
        tempFile = File.createTempFile(prefix, ".html");
        tempFile.deleteOnExit();   
        
        // Load the html template
        InputStream head = this.getClass().getResourceAsStream(
                "head.html");
        batchReport = Jsoup.parse(head, "utf-8", "./");
        head.close();
        
        //
        batchReport.select("title").html("Obs Plan Tracking Report");
        
        // TODO Move the template name to something like an enum or user setting
        InputStream template = this.getClass().getResourceAsStream(
                                "BatchReport_body.html");
        String body = SwattersUtils.streamToString(template);
        batchReport.select("body").html(body);
        
        // TODO: Set the reports-table header
        
        /*
         *  Iterate over each pointing and create a pointing report of 
         *  potential tracking stars.
         */
        for (Pointing p : pointings) {

            /*
             *  Create the pointing report (selection criteria found in 
             *  the enum sofia.Imager)
             *  TODO: Place criteria into xml instead
             */
            TrackStarReport report = new TrackStarReport(p, a);
            
            //
            int fpi_ucac4Count = report.getFpi_ucac4ObjCount();
            int fpi_hip2Count = report.getFpi_hip2ObjCount();
            int ffi_ucac4Count = report.getFfi_ucac4ObjCount();
            int ffi_hip2Count = report.getFfi_hip2ObjCount();
            int wfi_ucac4Count = report.getWfi_ucac4ObjCount();
            int wfi_hip2Count = report.getWfi_hip2ObjCount();
            
            // The common filename prefix for report files
            String filename = p.getObsID() + "-" + p.getObjName();
            
            // TODO: Filter out spaces (and other ugly chars?)

            //
            String absPath = outDir.getCanonicalPath().toString();
            String absFile = absPath + "/" + filename;
            
            // Write the report to an Aladin (.aj) file
            report.writeToAladin(a, new File(absFile + ".aj"));

            // Quit Aladin
            a.execCommand("quit");
            
            // Write the report to a html file
            report.writeToHTML(new File(absFile + ".html"));
                        
            /*
             *  Create a new html row that represents the pointing report 
             *  to add to the collection 
             *  TODO: add a link to the obsplan xml file
             */
            String row = "<tr>" 
                    + "<td><a href=\"" + "\">" + p.getObsID() 
                    + "<td><a href=\"" + outDir.getName() + "/" 
                            + filename + ".html\">" 
                            + p.getObjName()
                    + "<td>" + p.getLon() 
                    + "<td>" + p.getLat()
                    + "<td>" + p.getInstrumentID()
                    + "<td>" + p.getConfiguration()
                    + "<td>" + fpi_ucac4Count
                    + "<td>" + fpi_hip2Count
                    + "<td>" + ffi_ucac4Count
                    + "<td>" + ffi_hip2Count
                    + "<td>" + wfi_ucac4Count
                    + "<td>" + wfi_hip2Count;
            
            // Append the table row to the table of pointing reports
            batchReport.select("#pointing-reports tbody").append(row);
            
            // Update the temporary html file
            SwattersUtils.stringToFile(batchReport.toString(), tempFile);
             
            // stdout summary of pointing reports
            System.out.println("Potential FPI track objects from Hip2:\t" +
                    fpi_ucac4Count);
            System.out.println("Potential FPI track objects from UCAC4:\t" +
                    fpi_hip2Count);
            System.out.println("Potential FFI track objects from Hip2:\t" +
                    ffi_ucac4Count);
            System.out.println("Potential FFI track objects from UCAC4:\t" +
                    ffi_hip2Count);    
            System.out.println("Potential WFI track objects from Hip2:\t" +
                    wfi_ucac4Count);
            System.out.println("Potential WFI track objects from UCAC4:\t" +
                    wfi_hip2Count);    
        }
    }

        /**
    	 * @param arg
    	 * @return
    	 * @throws IOException
    	 */
    	private File[] parseArg(String[] arg) throws IOException {

    	    //
        File inFile = null;
        File outDir = null;

         //
        if (arg.length > 1) {
            
            //
            inFile = new File(arg[0]);
            if (!inFile.exists()) {
                // TODO: Inform the User and prompt for an input file or directory 
                inFile = this.openInputDialog();
            }            
            
            //
            outDir = new File(arg[1]);             

        } else if (arg.length == 1) {            
            /*
             *  If their is only 1 command line argument use it as the input
             *  file or directory and set outDir to the same directory
             */
            
            inFile = new File(arg[0]);
            if (!inFile.exists()) {
                // TODO: Inform the User and prompt for an input file or directory 
                inFile = openInputDialog();
            }            
    
            //
            File dir = new File(arg[0]);    
            
            // If the command line argument is a file use its parent dir
            if (dir.isDirectory()) {
                outDir = dir;
            }
            
        } else if (arg.length == 0) {
    
            //
            frame = new JFrame("Obs Plan Report Log");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setSize(new Dimension(1000, 600));
            textArea = new JTextArea(60, 80);
            textArea.setEditable(false);
            PrintStream printStream = 
                    new PrintStream(new TextAreaOutputStream(textArea));
             
            // re-assigns standard output stream and error output stream
            System.setOut(printStream);
            System.setErr(printStream);
            
            //
            frame.add(new JScrollPane(textArea));
            frame.setVisible(true); 
            
            // Prompt the User for the input file or directory
            System.out.println("Create a report of the potential SOFIA " +
                    "tracking objects for Obsplan xml file(s).\n" +
                    "Choose an Obsblock xml file or a directory " + 
                    "containing the Obsblock xml files to include in the " + 
                    "report...");
            inFile = openInputDialog();
                
            }
        
        // TODO: Check the validity of the infile or the xml files in the directory  
    	
        File[] out = {inFile, outDir};
        return out;
    	}
    	
    	private File openInputDialog() {
    	    
        FileNameExtensionFilter filter = 
                new FileNameExtensionFilter("xml", "xml");
        fc.setFileFilter(filter);
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);        
        fc.setDialogTitle("Choose an Obsblock file or directory");
        
        // Open the dialog window
        int opt = fc.showOpenDialog(null);
        if (opt != JFileChooser.APPROVE_OPTION) {                   
            // User clicked "Cancel", dismissed it, or there was an error
            System.exit(0);
        }  
        
        // User made a selection and clicked "OK"
        return(fc.getSelectedFile());      
    	}
    	
    /**
     * @param dir
     * @return
     */
    private File openOutputDialog() {

        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);     
        fc.setDialogTitle(
                "Select the report output directory");
        int opt = fc.showDialog(null, "Select");
        if (opt != JFileChooser.APPROVE_OPTION) {

            // User clicked "Cancel", dismissed it, or there was an error
            System.exit(0);
        }
        // User made a selection and clicked "OK"
        return(fc.getSelectedFile());             
    }
    /**
    	 * @param arg
    	 * @throws FileNotFoundException
    	 * @throws IOException
    	 * @throws JAXBException
    	 * @throws ParseException
    	 * @throws AladinException
    	 */
    	public static void main(String[] arg) throws FileNotFoundException,
                                                	IOException,
                                                	JAXBException,
                                                	ParseException,											 
                                                	AladinException {    	    
    	    // TODO: HANDLE EXCEPTIONS 

    	    //
        ObsPlanReporter r = new ObsPlanReporter();
        
        //
        File[] files = r.parseArg(arg);
        File inFile = files[0];
        File outDir = files[1];
        File[] fileList = {inFile};
        
        System.out.println("Obs plan files: " + inFile.getCanonicalPath());

        // Filter to select xml files
        FilenameFilter textFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                String lowercaseName = name.toLowerCase();
                if (lowercaseName.endsWith(".xml")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        
        // Determine the list of files to gather data from
        if (inFile.isDirectory()) {
            fileList = inFile.listFiles(textFilter);
        } else if ((inFile.isFile())) {
            // TODO: Check to make sure it is an xml file
        } else {
            throw new FileNotFoundException();
        } 
        
    	    /*
    	     *  Gather all the unique pointings in this group of aors into a
    	     *  LinkedHashSet of sofia.dcs.aor.Pointings.  
    	     *  The equals() and hashcode() methods for Pointings determine 
    	     *  what makes a pointing unique.
    	     */
    	    Set<Pointing> pointings = 
    	            sofia.dcs.DCSUtils.getUniqueSiderealPointings(fileList);
    	    
    	    if (outDir == null) {
    	        // Prompt the User for the output file or directory
            System.out.println("Choose a root directory to save the report "
                    + "files in...");
           outDir = r.openOutputDialog();            	        
    	    }
    	    
        //
        if (!outDir.exists()) {
            
            // Attempt to create the output directory            
            try {
                Files.createDirectory(outDir.toPath());
            } catch (IOException | SecurityException e) {
                e.printStackTrace();
                
                // TODO Inform the User and prompt for an 
                outDir = r.openOutputDialog();
            }
            
        }

        System.out.println("The report will be saved in: " + 
                outDir.getCanonicalPath());  
        
        //
        File cssDir = new File(outDir + "/css");
        File obsplanDir = new File(outDir + "/obsplans");
        File trackingDir = new File(outDir + "/tracking");

        //
        if (!cssDir.exists()) {
            Files.createDirectory(cssDir.toPath());
        }
        if (!obsplanDir.exists()) {
            Files.createDirectory(obsplanDir.toPath());
        }

        // TODO: Move css and obsplans files to dirs 

        /*
         *  Create a collection of pointing reports with potential tracking
         *  objects for each pointing
         *  TODO: Move selection criteria from the enum sofia.Imagers
         */       
        r.createTrackReports(pointings, trackingDir);
        
        String inputList = "";
        
        for (int i=0; i<fileList.length; i++) {
            String name = fileList[i].getName();
            inputList = inputList.concat("<li><a href=\"obsplans/" + name 
                        + "\">" + name);
        }
        r.batchReport.select("#input-files ul").append(inputList);
        
        //
        String criteriaTable = Imager.getLimitsTable();
        r.batchReport.select("#criteria-info .table").append(criteriaTable);

        //
        SwattersUtils.stringToFile(r.batchReport.toString(), 
              new File(outDir.getCanonicalPath() + "/index.html"));

        
        // TODO: Testing selection algorithm
        double V = 5.8;
        double d = 0.12;
        
        if ( (Imager.FFI.getMaxOptMag() < V) 
                && (V <= Imager.FPI.getMaxOptMag())) {
            
            // Bright enough for the FPI
            if (inFPI(d)) {
                // FPI
                System.out.println("FPI");
            } else {                
                // TODO:
                System.out.println("none");
            }
        } else if ((Imager.WFI.getMaxOptMag() < V) 
                && (V <= Imager.FFI.getMaxOptMag())) {
            
            // Bright enough for the FPI and FFI
            if (inFFIDonut(d)) {
                // FFI
                System.out.println("FFI");
            } else if (inFFICorner(d)) {
                // FFI-corner
                System.out.println("FFI-corner");
            } else if (inFPI(d)) {
                // FFI and FPI
                System.out.println("FFI and FPI");
            } else {                
                // TODO:
                System.out.println("none");
            }
        } else if (V <= Imager.WFI.getMaxOptMag()) {
            
            // Bright enough for the FPI, FFI, and WFI
            if (inWFIDonut(d)) {
                // WFI
                System.out.println("WFI");
            } else if (inWFICorner(d)) {
                // WFI and WFI-corner
                System.out.println("WFI and WFI-corner");
            } else if (inFFIDonut(d)) {
                // WFI and FFI
                System.out.println("WFI and FFI");
            } else if (inFFICorner(d)) {
                // WFI and FFI-corner
                System.out.println("WFI and FFI-corner");
            } else if (inFPI(d)) {
                // WFI, FFI, and FPI
                System.out.println("WFI, FFI, and FPI");
            } else {                
                // TODO:
                System.out.println("none");
            }           
        } else {            
            // TODO:
            System.out.println("none");
        }                
   	}  

    private static boolean inWFIDonut(double degrees) {
        return ((Imager.FFI.getMaxFOVDiagonal() < degrees)
                && (degrees <= Imager.WFI.getFOVRadius()));
    }
            
    private static boolean inWFICorner(double degrees) {
        return ((Imager.WFI.getFOVRadius() < degrees) 
                && (degrees <= Imager.WFI.getMaxFOVDiagonal()));   
    }
    
    private static boolean inFFIDonut(double degrees) {
        return ((Imager.FPI.getFOVRadius() < degrees) 
            && (degrees <= Imager.FFI.getFOVRadius()));   
    }
    
    private static boolean inFFICorner(double degrees) { 
        return ((Imager.FFI.getFOVRadius() < degrees) 
            && (degrees <= Imager.FFI.getMaxFOVDiagonal()));  
    }
    
    private static boolean inFPI(double degrees) {
        return (degrees <= Imager.FPI.getFOVRadius());        
    }
}
