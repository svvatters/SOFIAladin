package sofia;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;

import cds.aladin.Aladin;
import cds.aladin.AladinData;
import cds.aladin.AladinException;
import cds.aladin.Obj;
import cds.astro.Astrocoo;
import sofia.Imager;
import sofia.Vizier;
import sofia.dcs.SciObs;

/**
 * @author shannon.watters@gmail.com
 */
public class SciObsTrackReport {

    //
    enum ResourceFile {
    	
    		// Resource filepaths relative to src/sofia/html/
        $HTML_TEMPLATE("/sofia/html/SciObsTrackReport.html"),
        $CSS_DATATABLE("/sofia/html/css/jquery.dataTables.css"),
        $CSS_BOOT("/sofia/html/css/bootstrap.min.css"),
        $CSS_BOOT_DATA("/sofia/html/css/dataTables.bootstrap.css"),
        $CSS_MAIN("/sofia/html/css/styles.css"),
        $JS_JQUERY("/sofia/html/js/jquery-1.11.1.min.js"),
        $JS_DATATABLE("/sofia/html/js/jquery.dataTables.min.js"),
        $JS_BOOT("/sofia/html/js/bootstrap.min.js"),
        $JS_BOOT_DATA("/sofia/html/js/dataTables.bootstrap.js"),
        $JS_MAIN("/sofia/html/js/sciObsTrackReport.js");
        
        private String filepath;
        
        private ResourceFile(String filepath) {
            this.filepath = filepath;
        }
        
        public String getValue() {
            return this.filepath;
        }
        
        public String getFileName() {
        		Path p = Paths.get(this.getValue());
        		return p.getFileName().toString();
        }
    }

    private final SciObs scienceObs;
    private final Aladin aladin;
    private ArrayList<Document> catalogTables;

    SciObsTrackReport(SciObs scienceObs, Aladin aladin) throws AladinException,
    			IOException {
		this.scienceObs = scienceObs;
		this.aladin = aladin;

        //
        this.catalogTables = new ArrayList<Document>();

        // Clear any previous planes from Aladin
        aladin.execCommand("rm all");

        // TODO: Add the pointing to an Aladin plane (or create the plane)
        this.addObjToStack(scienceObs);

        /*
         *  Create Aladin planes with potential tracking objects from
		 * Vizier catalogs
         */
        classifyTrackStars(this.scienceObs.getLon(), this.scienceObs.getLat());

	}

	/**
     * @param outFile
     * @throws IOException
     */
    void writeToHtml(File outfile) throws IOException {

    		// Read the TrackReport template html
    		InputStream bodyTemplate = this.getClass().getResourceAsStream(
    				ResourceFile.$HTML_TEMPLATE.getValue());
    		Document htmlDoc = Jsoup.parse(bodyTemplate, "utf-8", "./", Parser.xmlParser());
	    bodyTemplate.close();

	    // Set the title of the report
        htmlDoc.select("title").html(scienceObs.getAorID() + " - " + "Tracking Report" );

        //
		Element criteria = htmlDoc.select("#selection-criteria").first();
		criteria.select("#maxVMag-fpi").html(Double.toString(Imager.FPI.getMaxOptMag()));
		criteria.select("#maxVMag-ffi").html(Double.toString(Imager.FFI.getMaxOptMag()));
		criteria.select("#maxVMag-wfi").html(Double.toString(Imager.WFI.getMaxOptMag()));	
		
		// Convert the maximum sky separations from degrees to arcminutes and add to table
        DecimalFormat skySepFormatter = new DecimalFormat("###.##");
		String fpiMaxRadius = 
				skySepFormatter.format(Imager.FPI.getFOVRadius() * 60.0);
		String ffiMaxRadius = 
				skySepFormatter.format(Imager.FFI.getMaxFOVDiagonal() * 60.0);
		String wfiMaxRadius = 
				skySepFormatter.format(Imager.WFI.getMaxFOVDiagonal() * 60.0);
		criteria.select("#maxSkySep-fpi").html(fpiMaxRadius);
		criteria.select("#maxSkySep-ffi").html(ffiMaxRadius);
		criteria.select("#maxSkySep-wfi").html(wfiMaxRadius);

		//
		Element obsPlan = htmlDoc.select("#obsPlan-table").first();

		// TODO: Populate the obsPlanTable

		// Format the ra and dec
        DecimalFormat myFormatter = new DecimalFormat("###.######");
        String ra = myFormatter.format(scienceObs.getLon());
        String dec = myFormatter.format(scienceObs.getLat());
		obsPlan.select("#rightAscension").html(ra);
		obsPlan.select("#declination").html(dec);

		//
		obsPlan.select("#equinox").html(scienceObs.getEquinox());

		//
		obsPlan.select("#aorID").html(scienceObs.getAorID());

		//
		String objName = scienceObs.getAstroObjectName();
		obsPlan.select("#objName").html( (objName == null) ? "" : objName);

		//
		String instName = scienceObs.getInstrumentName();
		obsPlan.select("#instrumentName").html( (instName == null) ? "" : instName);

		//
		String instConfig = scienceObs.getInstrumentConfig();
		obsPlan.select("#instrumentConfig").html( (instConfig == null) ? "" : instConfig);

		//
		String instMode = scienceObs.getInstrumentMode();
		obsPlan.select("#instrumentMode").html( (instMode == null) ? "" : instMode);

		//
		String instSlit = scienceObs.getInstrumentSlit();
		obsPlan.select("#instrumentSlit").html( (instSlit == null) ? "" : instSlit);

		//
		String specElement = scienceObs.getSpectralElement();
		obsPlan.select("#spectralElement").html ( (specElement == null) ? "" : specElement);

		//
		String specElement2 = scienceObs.getSpectralElement2();
		obsPlan.select("#spectralElement2").html ( (specElement2 == null) ? "" : specElement2);

		//
		String mapArea = scienceObs.getMapArea();
		obsPlan.select("#mapArea").html( (mapArea == null) ? "" : mapArea);

		//
		String pmRA = scienceObs.getPropMotnRA();
		obsPlan.select("#propMotnRA").html( (pmRA == null) ? "" : pmRA);

		//
		String pmDec = scienceObs.getPropMotnDec();
		obsPlan.select("#propMotnDec").html( (pmDec == null) ? "" : pmDec);

		for (int i = 0; i < catalogTables.size(); i++) {
        		htmlDoc.getElementById("potential-track-stars").append(
        				catalogTables.get(i).toString());
    		}

        // TODO: Add a comment to the html file to document these modifications
//        Calendar cal = Calendar.getInstance();
//        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssz");
//        Comment note =
//        		new Comment("Potential track objects generated by ObsPlanReporter "
//        				+ df.format(cal.getTime()), "");
//        htmlDoc.select("Pointing").before(note.toString());

        //
        outfile.createNewFile();
        BufferedWriter outWriter = new BufferedWriter(new FileWriter(outfile));
        outWriter.write(htmlDoc.toString());
        outWriter.flush();
        outWriter.close();

    }

    /**
     * @param aladin
     * @param f
     * @throws IOException
     */
    void writeToAladin(File outFile) throws IOException {
        aladin.execCommand("backup " + outFile);
    }

    /**
     * @param a
     * @param ra
     * @param dec
     * @param catalog
     * @param radius
     * @param maxMag
     * @return
     */
    private String queryVizier(Aladin a, double ra, double dec, Vizier catalog,
            double radius, double maxMag) {

        String catName = catalog.getName();
        String tempName = "vizier_" + catName;
        String filterName = "MaxVMag_" + maxMag;

        System.out.println("Querying vizier for " + catName
                + " objects with (optical magnitude < " + maxMag
                + "; sky separation < " + radius + " degs)");

        // Create a simple stdout indeterminate progress bar
        ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
        @SuppressWarnings("unused")
        ScheduledFuture<?> sf = ses.scheduleWithFixedDelay(new Runnable() {
                public void run() {
                    System.out.print(".");
                }
            },
            0, 500, TimeUnit.MILLISECONDS);

        //
        a.execCommand(tempName + " = get vizier(" + catName + ") " +
                       ra + " " + dec + " " + radius + "deg");

        //
        a.execCommand("filter " + filterName +
                        " { ${" + catalog.getVisMagCol() + "} <" + maxMag +
                        " {draw white square}}");
        a.execCommand("filter " + filterName + " on");
        a.execCommand("select " + tempName);
        a.execCommand("cplane " + catName);
        a.execCommand("rm " + tempName);
        a.execCommand("rm " + filterName);

        // Shutdown the ScheduledExecutorService (indeterminate progress bar)
        ses.shutdown();
        System.out.print("\r"); // Clear the progress markings

        /*
         *  TODO:  This isn't guaranteed to be the correct name for the Aladin
         *  plane!! If it clashed with an already existing plan Aladin appends
         *  '~1' to the new plane ==> catName would be a label ref to the OLD plane.
         */
        return catName;
    }

    /**
     * @param a
     * @param colInfo
     * @param vals
     * @return
     * @throws AladinException
     */
    public Obj addObjToStack(SciObs scienceObs) {

        HashMap<String, String[]> colInfo = scienceObs.getObjColInfo();
        String[] vals = scienceObs.getObjValues();
        //
        AladinData plane = null;

        // If the plane exists get it's data otherwise create it
        try {
            plane = this.aladin.getAladinData(vals[0]);
        } catch (AladinException e) {
            try {
				plane = this.aladin.createAladinCatalog(vals[0]);
			} catch (AladinException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }

        // TODO: Check validity of data (same length String[] lists,...)

        plane.setDatatype(colInfo.get("datatypes"));
        plane.setName(colInfo.get("names"));
        plane.setUnit(colInfo.get("units"));
        plane.setUCD(colInfo.get("ucds"));
        plane.setWidth(colInfo.get("widths"));
        plane.setArraysize(colInfo.get("arraysizes"));
        plane.setPrecision(colInfo.get("precisions"));

        //
        Obj obj = null;
		try {
			obj = plane.addSource(vals[0], Double.parseDouble(vals[1]),
			        Double.parseDouble(vals[2]), vals);
			plane.objModified();
		} catch (NumberFormatException e) {
			// TODO Warn the user, log, and remove this obj from the report
			e.printStackTrace();
		} catch (AladinException e) {
			// TODO Warn the user, log, and remove this obj from the report
			e.printStackTrace();
		}

        return obj;
    }

    /**
     * @param ra
     * @param dec
     * @throws IOException
     * @throws AladinException
     */
    private void classifyTrackStars(double ra, double dec)  throws IOException,
				AladinException {

		// TODO: check imager fov shape and set min and max radius for queryVizier()

//		//
//		for (Imager img : Imager.values()) {
//			aladin.execCommand("md " + img.name());
//		}

		// Iterate over Vizer catalogs
		for (Vizier catalog : Vizier.values()) {

			//
			HashMap<String, AladinData> imagerPlanes =
					new HashMap<String, AladinData>();

			//
			Document table = new Document("");
			Element tableRoot = table.appendElement("table");
			tableRoot.attr("id", catalog.getName());
			tableRoot.attr("class", "catalog");
			Element headers = tableRoot.appendElement("thead");
			Element tableData = tableRoot.appendElement("tbody");

    			// 
			String catPlaneLabel;
			if (catalog == Vizier.HIP2) {
        			catPlaneLabel =  queryVizier(this.aladin, ra, dec, catalog,
        				Imager.WFI.getMaxFOVDiagonal(), Imager.FPI.getMaxOptMag());
        			tableRoot.attr("max-skysep-arcmin", 
        					Double.toString(Imager.WFI.getMaxFOVDiagonal()));
        			// TODO:  Add selection criteria to output
//        			tableRoot.attr("max-app-vmag", Double.toString(Imager.FPI.getMaxOptMag()));

			} else if (catalog == Vizier.UCAC4) {
        			catPlaneLabel =  queryVizier(this.aladin, ra, dec, catalog,
        				Imager.FFI.getMaxFOVDiagonal(), Imager.FPI.getMaxOptMag());
        			tableRoot.attr("max-skysep-arcmin", 
        					Double.toString(Imager.FFI.getMaxFOVDiagonal()));
        			// TODO:  Add selection criteria to output
//        			tableRoot.attr("max-app-vmag", Double.toString(Imager.FPI.getMaxOptMag()));

			} else {
				// TODO:  Handle error
				catPlaneLabel = null;
			}
    			System.out.println("Searching " + catPlaneLabel +
                    " for potential SOFIA tracking objects...");

    			// The catalog data
    			AladinData catPlaneData = aladin.getAladinData(catPlaneLabel);

	        //
	        Obj[] objects = catPlaneData.seeObj();


	        /*
	         *  Insert a column into the catalog plane data for the sky separation
	         *  between each catalog Obj and the Pointing
	         */
	        String[] dataTypes = SciObsTrackReport.prependStringToList("D",
	                objects[0].getDataTypes());
	        String[] names = SciObsTrackReport.prependStringToList("_SkySep",
	                objects[0].getNames());
	        String[] units = SciObsTrackReport.prependStringToList("arcmin",
	                objects[0].getUnits());
	        String[] ucds = SciObsTrackReport.prependStringToList(
	                "pos.angDistance;pos.ang.separation", objects[0].getUCDs());
	        String[] widths = SciObsTrackReport.prependStringToList("10",
	                objects[0].getWidths());
	        String[] arraySizes = SciObsTrackReport.prependStringToList("null",
	                objects[0].getArraysizes());
	        String[] precisions = SciObsTrackReport.prependStringToList("6",
	                objects[0].getPrecisions());

	    		for (Imager img : Imager.values()) {

	            // TODO: Check if catalog already exists

	            /*
	             *  Create the new catalog planes for each imager and set the column
	             *  info
	             */
	    			String imagerPlaneLabel = img.name() + "_"  + catPlaneLabel;
	            AladinData plane =
	            		Aladin.aladin.createAladinCatalog(imagerPlaneLabel);
	            plane.setDatatype(dataTypes);
	            plane.setName(names);
	            plane.setUnit(units);
	            plane.setUCD(ucds);
	            plane.setWidth(widths);
	            plane.setArraysize(arraySizes);
	            plane.setPrecision(precisions);

	            imagerPlanes.put(imagerPlaneLabel, plane);
	            // TODO: Need to access all the "plane" objects outside this scope
	    		}

	        // Add header info to the table
	    		// Column names
			Element hrow = headers.appendElement("tr");
			hrow.attr("class", "names");
	        for (String name : names) {
	        		hrow.appendElement("th").text(name);
	        }
			// Column units
	        Element hrow_units = headers.appendElement("tr");
			hrow_units.attr("class", "units");
			for (String unit : units) {
				if (unit == null) {
					hrow_units.appendElement("th").text("");
				} else {
					hrow_units.appendElement("th").text(unit);
				}
			}
			// Column precisions
			Element hrow_precs = headers.appendElement("tr");
			hrow_precs.attr("class", "precisions");
			for (String precision : precisions) {
				if (precision == null) {
					hrow_precs.appendElement("th").text("");
				} else {
					hrow_precs.appendElement("th").text(precision);
				}
			}
			// Column uniform content descriptors
			Element hrow_ucds = headers.appendElement("tr");
			hrow_ucds.attr("class", "ucds");
			for (String ucd : ucds) {
				if (ucd == null) {
					hrow_ucds.appendElement("th").text("");
				} else {
					hrow_ucds.appendElement("th").text(ucd);
				}
			}

	    		// Iterate through each object
	    		for (int k = 0; k < catPlaneData.getNbObj(); k++) {
	    			Obj obj = objects[k];

	            // Determine the column in the catalog for the optical Magnitude
	            int objMagCol = -1;
	            for (int i = 0; i < ucds.length; i++) {
	                if (ucds[i].toLowerCase().equals("phot.mag;em.opt.v")) {
	                    int oldObjMagCol = i;
	                    /*
	                     *  A Column was inserted at col 0 of ucds[] earlier in
	                     *  this method but the number of columns in the objects
	                     *  values list hasn't increased so the column number
	                     *  needs to be adjusted by -1
	                     */
	                    objMagCol = oldObjMagCol - 1;
	                }
	            }
	            if (objMagCol < 0) {
	                // TODO: Error finding the optical magnitude column!
	            		System.out.println(obj.id + ":  Can't find object 's visible magnitude!");
	            };

	            // Get the data from the catalog Obj
	            String objID = obj.id;
	            double objRA = obj.getRa();
	            double objDec = obj.getDec();

	            // Get the visual magnitude from the catalog Obj values
	            String[] catObjValues = obj.getValues();
	            double objMag = Double.parseDouble(catObjValues[objMagCol]);

	            // The distance to the target Pointing in degrees
	            double distance = Astrocoo.distance(ra, dec, objRA, objDec);

	            // Format the distance number for printing in ARCMINUTES
	            DecimalFormat myFormatter = new DecimalFormat("###.####");
	            String skySep = myFormatter.format(distance* 60.0);

	            // Add the distance to column 0 of the Obj values
	            String[] objValues =
	            		SciObsTrackReport.prependStringToList(skySep, catObjValues);

	            // TODO: Test selection algorithm more
	            /*
	             *  Categorize objects as possible SOFIA track objects based on
	             *  visual magnitude and proximity to the target Coo
	             */
	            if ( (Imager.FFI.getMaxOptMag() < objMag)
	                    && (objMag <= Imager.FPI.getMaxOptMag())) {

	                // Bright enough for the FPI only

	                if (inFpi(distance)) {
	                    // FPI

	                		/*
	                		 *  TODO: add to
	                		 *  <imager>
	                		 *  	<id></id>
	                		 *  	<table catalog="">
	                		 *  		<tbody>
	                		 *  			<tr class="fpi">
	                		 */

	    					AladinData plane =
	    							imagerPlanes.get(Imager.FPI.name() + "_" + catPlaneLabel);
	    					plane.addSource(objID, objRA, objDec, objValues);
	                		plane.objModified();

	                		Element row = toTableRow(objValues);
	                		row.attr("class", "fpi");
	                		tableData.append(row.toString());

	                } else {
	                    // TODO:
	                }

	            } else if ((Imager.WFI.getMaxOptMag() < objMag)
	                    && (objMag <= Imager.FFI.getMaxOptMag())) {

	                // Bright enough for the FPI and FFI

	                if (inFfiDonut(distance)) {
	                    // FFI
	    					AladinData plane =
	    							imagerPlanes.get(Imager.FFI.name() + "_" + catPlaneLabel);
	                		plane.addSource(objID, objRA, objDec, objValues);
	                		plane.objModified();

	                		Element row = toTableRow(objValues);
	                		row.attr("class", "ffi");
	                		tableData.append(row.toString());

	                } else if (inFfiCorner(distance)) {
						AladinData plane =
									imagerPlanes.get(Imager.FFI.name() + "_" + catPlaneLabel);
	                		plane.addSource(objID, objRA, objDec, objValues);
	                		plane.objModified();

	                		Element row = toTableRow(objValues);
	                		row.attr("class", "ffi-corner");
	                		tableData.append(row.toString());

	                } else if (inFpi(distance)) {
	                		// TODO:  Is this really how we want this classified in Aladin?
							AladinData plane =
									imagerPlanes.get(Imager.FPI.name() + "_" + catPlaneLabel);
	                		plane.addSource(objID, objRA, objDec, objValues);
	                		plane.objModified();

	                		Element row = toTableRow(objValues);
	                		row.attr("class", "fpi ffi");
	                		tableData.append(row.toString());

	                } else {
	                    // TODO:

	                }

	            } else if (objMag <= Imager.WFI.getMaxOptMag()) {

	                // Bright enough for the FPI, FFI, and WFI

	                if (inWfiDonut(distance)) {
	                    // WFI
	                		AladinData plane =
	                				imagerPlanes.get(Imager.WFI.name() + "_" + catPlaneLabel);
	                		plane.addSource(objID, objRA, objDec, objValues);
	                		plane.objModified();

	                		Element row = toTableRow(objValues);
	                		row.attr("class", "wfi");
	                		tableData.append(row.toString());

	                } else if (inWfiCorner(distance)) {
	            			AladinData plane =
	            					imagerPlanes.get(Imager.WFI.name() + "_" + catPlaneLabel);
	            			plane.addSource(objID, objRA, objDec, objValues);
	                		plane.objModified();

	                		Element row = toTableRow(objValues);
	                		row.attr("class", "wfi-corner");
	                		tableData.append(row.toString());

	                } else if (inFfiDonut(distance)) {
	                		// TODO:  Is this really how we want to classify this in Aladin?
	            			AladinData plane =
	            					imagerPlanes.get(Imager.FFI.name() + "_" + catPlaneLabel);
	            			plane.addSource(objID, objRA, objDec, objValues);
	                		plane.objModified();

	                		Element row = toTableRow(objValues);
	                		row.attr("class", "ffi wfi");
	                		tableData.append(row.toString());

	                } else if (inFfiCorner(distance)) {
	            			// TODO:  Is this really how we want to classify this in Aladin?
	        				AladinData plane =
	        						imagerPlanes.get(Imager.FFI.name() + "_" + catPlaneLabel);
	        				plane.addSource(objID, objRA, objDec, objValues);
	                		plane.objModified();

	                		Element row = toTableRow(objValues);
	                		row.attr("class", "ffi-corner wfi");
	                		tableData.append(row.toString());

	                } else if (inFpi(distance)) {
	        				// TODO:  Is this really how we want to classify this in Aladin?
	    					AladinData plane =
	    							imagerPlanes.get(Imager.FPI.name() + "_" + catPlaneLabel);
	    					plane.addSource(objID, objRA, objDec, objValues);
	                		plane.objModified();

	                		Element row = toTableRow(objValues);
	                		row.attr("class", "fpi ffi wfi");
	                		tableData.append(row.toString());

	                } else {
	                    // TODO:
	                }
	            } else {
	                // TODO:
	            }
	    		}

//	    		//
//	    		for (Imager img : Imager.values()) {
//	    			String name =  img.name();
//	    			String cmd = "mv " + name + "_" + catPlaneLabel + " " + img.name();
//	    			aladin.execCommand(cmd);
//			}

	    		//
	    		aladin.execCommand("rm " + catPlaneLabel);

	    		// Add table to a set of catalogTables
	    		this.catalogTables.add(table);
		}
	    // TODO: Remove the empty planes
    }

    /**
     * @param skySep_degs
     * @return
     */
    private boolean inWfiDonut(double skySep_degs) {
        return ( (Imager.FFI.getMaxFOVDiagonal() < skySep_degs)
                && (skySep_degs <= Imager.WFI.getFOVRadius()) );
    }

    /**
     * @param skySep_degs
     * @return
     */
    private boolean inWfiCorner(double skySep_degs) {
        return ( (Imager.WFI.getFOVRadius() < skySep_degs)
                && (skySep_degs <= Imager.WFI.getMaxFOVDiagonal()) );
    }

    /**
     * @param skySep_degs
     * @return
     */
    private boolean inFfiDonut(double skySep_degs) {
        return ( (Imager.FPI.getFOVRadius() < skySep_degs)
            && (skySep_degs <= Imager.FFI.getFOVRadius()) );
    }

    /**
     * @param skySep_degs
     * @return
     */
    private boolean inFfiCorner(double skySep_degs) {
        return ( (Imager.FFI.getFOVRadius() < skySep_degs)
            && (skySep_degs <= Imager.FFI.getMaxFOVDiagonal()) );
    }

    /**
     * @param skySep_degs
     * @return
     */
    private boolean inFpi(double skySep_degs) {
        return (skySep_degs <= Imager.FPI.getFOVRadius());
    }

    /**
     * @param objValues
     * @return
     */
    private Element toTableRow(String[] objValues) {
    		Document ml = new Document("");
    		Element row = ml.appendElement("tr");
    		for (String val : objValues) {
    			row.appendElement("td").text(val);
    		}
    		return row;
    }

    /**
     * @param string
     * @param listB
     * @return
     */
    public static String[] prependStringToList(String string, String[] listB) {
        String[] listA = {string};
        int aLen = listA.length;
        int bLen = listB.length;
        String[] C= new String[aLen+bLen];
        System.arraycopy(listA, 0, C, 0, aLen);
        System.arraycopy(listB, 0, C, aLen, bLen);
        return C;
    }

    // DEBUGGING & TESTING
    public static void main (String[] args) throws IOException {
        System.out.println("sofia.aladin.SciObsTrackReport");
    }
}
