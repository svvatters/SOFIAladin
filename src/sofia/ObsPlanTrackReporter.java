package sofia;

import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.ArrayList;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import cds.aladin.Aladin;
import cds.aladin.AladinException;
import sofia.SciObsTrackReport.ResourceFile;
import sofia.dcs.SciObs;

/**
 * @author shannon.watters@gmail.com
 * @SOFIA_Aladin-extension class
 */
public final class ObsPlanTrackReporter {

    // Parts of the GUI log console
    final private JFileChooser fc;
    private JFrame frame;
    private JTextArea textArea;

    ObsPlanTrackReporter() {
        super();
        this.fc = new JFileChooser();
        this.frame = new JFrame();
        this.textArea = new JTextArea();
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
		            "Choose an ObsPlan xml file or a directory " +
		            "containing the ObsPlan xml files to include in the " +
		            "report...");
		    inFile = openInputDialog();
	
		    }
	
		// TODO: Check the validity of the infile or the xml files in the directory
	
		File[] out = {inFile, outDir};
		return out;
	}

	/**
	 * @return
	 */
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
	 * @param obsCoords
	 * @param outDir
	 * @throws IOException
	 * @throws AladinException
	 */
	private void createTrackReports(ArrayList<SciObs> scienceObservations,
			File outDir) throws IOException, AladinException {

		/*
	     *  Using Aladin to save initial data collection for MOPS flight prep.
	     *  Also using Aladin for it's built in query and filter commands.
	     */
		Aladin aladin;

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
	    aladin = cds.aladin.Aladin.launch("-nogui");
	    aladin.execCommand("trace 0");

	    // Shutdown the ScheduledExecutorService (indeterminate progress bar)
	    ses.shutdown();
	    
	    // TODO: MAKE A LOOP THAT ITERATES OVER DIRECTORIES NEEDS TO CREATE
	    // Create obsplan directory
        File obsPlanDir = new File(outDir + "/obsPlans");
        if (!obsPlanDir.exists()) {
            Files.createDirectory(obsPlanDir.toPath());
        }
        // TODO: FileAlreadyExistsException	    
        
        // Create obsplan/sidereal
        File siderealDir = new File(obsPlanDir + "/sidereal");
        if (!siderealDir.exists()) {
            Files.createDirectory(siderealDir.toPath());
        }
        // TODO: FileAlreadyExistsException	    
        
        // Create obsplan/nonsidereal
        File nonsiderealDir = new File(obsPlanDir + "/nonsidereal");
        if (!nonsiderealDir.exists()) {
            Files.createDirectory(nonsiderealDir.toPath());
        }
        // TODO: FileAlreadyExistsException	    

        // Create obsplan/zero_zero
        File zero_zeroDir = new File(obsPlanDir + "/zero_zero");
        if (!zero_zeroDir.exists()) {
            Files.createDirectory(zero_zeroDir.toPath());
        }
        // TODO: FileAlreadyExistsException	    
	
	    // Create html directory
        File htmlDir = new File(outDir + "/html");
        if (!htmlDir.exists()) {
            Files.createDirectory(htmlDir.toPath());
        }
        // TODO: FileAlreadyExistsException
        
		// Create the css directory under the html directory
        File cssDir = new File(outDir + "/html/css");
        if (!cssDir.exists()) {
            Files.createDirectory(cssDir.toPath());
        }
        // TODO: FileAlreadyExistsException

        // Copy the css files into cssDir
		InputStream css_trackingReport = this.getClass().getResourceAsStream(
				ResourceFile.$CSS_MAIN.getValue());
		File out = new File(cssDir.toPath()  +  ResourceFile.$CSS_MAIN.getValue()); 
		Files.copy(css_trackingReport, out.toPath());

		InputStream css_dataTables_jquery = this.getClass().getResourceAsStream(
				ResourceFile.$CSS_DATATABLE.getValue());
		out = new File(cssDir.toPath() + ResourceFile.$CSS_DATATABLE.getValue());
		Files.copy(css_dataTables_jquery, out.toPath());
		
		InputStream css_boot = this.getClass().getResourceAsStream(
				ResourceFile.$CSS_BOOT.getValue());
		out = new File(cssDir.toPath() + ResourceFile.$CSS_BOOT.getValue());
		Files.copy(css_boot, out.toPath());
		
		InputStream css_boot_data = this.getClass().getResourceAsStream(
				ResourceFile.$CSS_BOOT_DATA.getValue());
		out = new File(cssDir.toPath() + ResourceFile.$CSS_BOOT_DATA.getValue());
		Files.copy(css_boot_data, out.toPath());
		
		// Create the javascript directory under the html directory
		File jsDir = new File(outDir + "/html/js");
        if (!jsDir.exists()) {
            Files.createDirectory(jsDir.toPath());
        }
        // TODO: FileAlreadyExistsException

        // Copy the javascript files into jsDir
        InputStream js_trackingReport = this.getClass().getResourceAsStream(
				ResourceFile.$JS_MAIN.getValue());
		File out_js = new File(jsDir.toPath() + ResourceFile.$JS_MAIN.getValue());
		Files.copy(js_trackingReport, out_js.toPath());
		
		InputStream js_jquery = this.getClass().getResourceAsStream(
				ResourceFile.$JS_JQUERY.getValue());
		out_js = new File(jsDir.toPath() + ResourceFile.$JS_JQUERY.getValue());
		Files.copy(js_jquery, out_js.toPath());
		
		InputStream js_dataTables_jquery = this.getClass().getResourceAsStream(
				ResourceFile.$JS_DATATABLE.getValue());
		out_js = new File(jsDir.toPath() + ResourceFile.$JS_DATATABLE.getValue());
		Files.copy(js_dataTables_jquery, out_js.toPath());
		
//		InputStream js_boot = this.getClass().getResourceAsStream(
//				ResourceFile.$JS_BOOT.getValue());
//		out_js = new File(jsDir.toPath() + 
//				"/" +  ResourceFile.$JS_BOOT.getFileName));
//		Files.copy(js_boot, out_js.toPath());
//		InputStream js_boot_data = this.getClass().getResourceAsStream(
//				ResourceFile.$JS_BOOT_DATA.getValue());
//		out_js = new File(jsDir.toPath() + 
//				"/" +  ResourceFile.$JS_BOOT_DATA.getFileName());
//		Files.copy(js_boot_data, out_js.toPath());
		
	    /*
	     *  Iterate over each sciObs and create a pointing report of
	     *  potential tracking stars.
	     *  TODO:  Verify that scienceObservations isn't null or empty
	     */
	    for (SciObs sciObs : scienceObservations) {

	        /*
	         *  Create the pointing report (selection criteria found in
	         *  the enum sofia.Imager 
	         *  TODO: replace)
	         */
	        SciObsTrackReport report = new SciObsTrackReport(sciObs, aladin);

	        // The common filename prefix for report files
	        String filename = sciObs.getAorID() + "-" + sciObs.getAstroObjectName();

	        // Filter out spaces from filename
	        filename = filename.replaceAll("\\s","");
	        // Filter out back slashes from filename
	        filename = filename.replaceAll("\\\\", "_");
	        // Filter out forward slashes from filename
	        filename = filename.replaceAll("/", "_");

	        //
	        String absPath = outDir.getCanonicalPath().toString();
	        String absFile = absPath + "/" + filename;

	        // Write the report to an Aladin (.aj) file
	        report.writeToAladin(new File(absFile + ".aj"));

	        // Write the report to a html file
	        report.writeToHtml(new File(absFile + ".html"));
		}
        // Quit Aladin
	    System.out.print("\nShutting down");
        aladin.execCommand("quit");
	}

    /**
     * This class extends from OutputStream to redirect output to a JTextArrea
     * @author www.codejava.net
     * @author shannon.watters@gmail.com - just changed the name to be specific
     */
    public final class TextAreaOutputStream extends OutputStream {

        private JTextArea textArea;

        public TextAreaOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) throws IOException {
            // redirects data to the text area
            textArea.append(String.valueOf((char)b));
            // scrolls the text area to the end of data
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }

	/**
    	 * @param arg
    	 * @throws FileNotFoundException
    	 * @throws IOException
    	 * @throws ParseException
    	 * @throws AladinException
    	 */
    	public static void main(String[] arg) throws FileNotFoundException, IOException,
    			ParseException,	AladinException {

    	    // TODO: HANDLE EXCEPTIONS

    	    //
        ObsPlanTrackReporter reporter = new ObsPlanTrackReporter();

        //
        File[] files = reporter.parseArg(arg);
        File inFile = files[0];
        File outDir = files[1];
        File[] fileList = {inFile};

        System.out.println("Obs plan files: " + inFile.getCanonicalPath());

        // Filter to select xml files
        // TODO:  Add some verification (schema?)
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
            // TODO
        } else {
            throw new FileNotFoundException();
        }

    	    if (outDir == null) {
    	        // Prompt the User for the output file or directory
            System.out.println("Choose a root directory to save the report "
                    + "files in...");
           outDir = reporter.openOutputDialog();
    	    }

        //
        if (!outDir.exists()) {

            // Attempt to create the output directory
            try {
                Files.createDirectory(outDir.toPath());
            } catch (IOException | SecurityException e) {
                e.printStackTrace();
                // TODO Inform the User and prompt for an
                outDir = reporter.openOutputDialog();
            }

        }

        System.out.println("The report will be saved in: " +
                outDir.getCanonicalPath());

		/*
	     *  Gather all the unique pointings in this group of aors into a
	     *  LinkedHashSet of ScienceObs.
	     *  The equals() and hashcode() methods for ScienceObs determine
	     *  what makes a pointing unique.
	     */
	    ArrayList<SciObs> scienceObservations = new ArrayList<SciObs>();

	    // Iterate through the ObsPlan xml files
	    for (File f : fileList) {
	        System.out.println("Processing: " + f.getCanonicalPath() + "...");

	        // Open and parse the file
	        InputStream xml = new FileInputStream(f);
	        Document xmlDoc= Jsoup.parse(xml, "utf-8", "./", Parser.xmlParser());
	        xml.close();

	        //
	        Elements obsPlans = xmlDoc.select("ObservingPlan");

	        // TODO: Should only be 1 item in obsPlans (?) so validate?

	        //
	        for (Element obsPlan : obsPlans) {
	        	// TODO: Should only be 1 obsPlan?

		        //
		        Elements propObservations =
		        			obsPlan.select("ProposedObservation");

		        //
		        for (Element propObs : propObservations) {
		        		SciObs obs = new SciObs(propObs);
		        		if ( (obs.getAstroObjectType().equalsIgnoreCase("TargetFixedSingle")) || 
		        				(obs.getAstroObjectType().equalsIgnoreCase("Sidereal")) ) {

		        			// Check for coords of 0.0 and 0.0
	        				// TODO: better method?
		        			if ( (Math.abs(obs.getLon()) < 0.001) && (Math.abs(obs.getLat()) < 0.001) ) {
	        					System.out.println("Will not create a report for " 
	        							+ obs.getAorID()
	        							+ " because its coords are 0 0");		
		        			} else {
	        					scienceObservations.add(new SciObs(propObs));		
		        			}
		        		} else {
		        			System.out.println("Will not create a report for " 
							+ obs.getAorID() 
							+ " because it's of type " + obs.getAstroObjectType());		
		        		}
		        }
	        }
	    }

	    // TESTING
	    System.out.println("scienceObservations.size(): " + scienceObservations.size());

//	    //TODO: scienceObservations have to have hashcode() and equals() set for this to work?
//		// Remove the duplicates
//	    Set<ScienceObs> uniquePropObservations =
//	    			new LinkedHashSet<ScienceObs>(scienceObservations);
//		System.out.println(uniquePropObservations.size() +
//				" unique proposed science observations:");
//		for (ScienceObs s: uniquePropObservations) {
//			System.out.println(s);
//		}

        /*
         *  Create a collection of SciObs reports with potential tracking
         *  objects for each SciObs
         */
        reporter.createTrackReports(scienceObservations, outDir);
    }
}
