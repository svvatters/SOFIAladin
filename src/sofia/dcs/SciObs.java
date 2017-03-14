package sofia.dcs;

import java.text.ParseException;
import java.util.HashMap;

import org.jsoup.nodes.Element;

import cds.astro.Astrocoo;

/**
 * @author shannon.watters@gmail.com
 * @SOFIAladin-extension class
 */
@SuppressWarnings("serial")
public class SciObs extends Astrocoo {

	private String astroObjectType;
	private String astroObjectName;
	private String aorID;
	private boolean sidereal;
	/*
	 * TODO: The ObsPlan file has a self-closing element under Sidereal "<J2000 /> so 
	 * this is just hardwired now
	 */
	private final String equinox = "J2000";
	private String instrumentName;
	private String instrumentConfig;
	private String instrumentMode;
	private String instrumentSlit;
	private String spectralElement;
	private String spectralElement2;
	private String mapArea;
	private String propMotnRA;
	private String propMotnDec;

	// TODO: If not sidereal...
	/*
	 *  TODO:  Duplicate SciObs will have equal:
	 *  	sidereal
	 *  	equinox
	 *  	lon
	 *  	lat
	 *  	instrumentName
	 *  	instrumentConfig
	 *  	instrumentMode
	 *  	instrumentSlit
	 *  	spectralElement1?
	 *  	spectralElement2?
	 *  	mapArea?
	 */
	 // TODO: Have enums or their xml for all instruments?

	public SciObs(Element proposedObs) throws ParseException {

        //
		Element astroObj = proposedObs.select("AstroObject").first();
		// TODO: verify only 1 occurrence?

		//
		this.astroObjectName = astroObj.select("AstroObjectName").first().text();

		//
		Element objType = astroObj.select("AstroObjectType").first();
		// TODO: verify only 1 occurrence?

		//
		this.astroObjectType = objType.text();
        
        //
		Element aorID = proposedObs.select("AORID").first();
		// TODO: verify only 1 occurrence?

		//
		this.aorID = aorID.text();
		
		if ( (this.astroObjectType.equalsIgnoreCase("TargetFixedSingle")) ||
				(this.astroObjectType.equalsIgnoreCase("Sidereal")) ) {

			//
			Element sidereal =
					astroObj.select("ObservationCenter").select("Sidereal").first();
			this.sidereal = sidereal.equals(null) ? false : true;
			if (!this.sidereal ) {
				// TODO:  warn the user and skip out of creating this instance
			}

			// The right ascension in the ObsPlan files are in units of "Hours" so convert to deg
			double ra_degrees = Double.parseDouble(sidereal.select("RA").first().text()) * 15.0;
			double dec_degrees = Double.parseDouble(sidereal.select("Dec").first().text());
			
//			// Check for coords of 0.0 and 0.0
//			// TODO: better method?
//			if ( (ra_degrees < 0.001) && (dec_degrees < 0.001) ) {
//				System.out.println("Will not create a report for " 
//						+ this.aorID 
//						+ " because it's coords are 0 0");				
//			}
			
			//Format the coords so they work as inputs for parent Astrocoo.set()
	        String ra = this.formatCoord(Double.toString(ra_degrees));
	        String dec = this.formatCoord(Double.toString(dec_degrees));

			/*
	         *  Use the parent class constructor to set the coordinates.  They are
	         *  accessed from the getLon() and getLat() methods
	         *  Throws: ParseException
	         */
	        this.set(ra + " " + dec);
	        this.setPrecision(7, 7);

	        /*
	         *  TODO: This doesn't get the equinox with current ObsPlan.xml
	         */
//				        this.equinox = sidereal.select("Equinox").text();

			//
			Element propMotion = astroObj.select("ProperMotion").first();
			// TODO: verify only 1 occurrence?

			//
			Element instrument = proposedObs.select("Instrument").first();
			// TODO: verify only 1 occurrence?
			// TODO: verify they correspond with an enum or ext xml?

			//
			this.instrumentName = instrument.attr("name").trim();

			//
			this.instrumentConfig = instrument.select("Config").first().text();
			// TODO: verify only 1 occurrence?

			//
			this.instrumentMode = instrument.select("Mode").first().text();
			// TODO: verify only 1 occurrence?

			//
			Element spec = instrument.select("SpectralElement").first();
			// TODO: verify only 0-1 occurrences?
			this.spectralElement = (spec == null) ? null : spec.text();

			//
			Element spec2 = instrument.select("SpectralElement2").first();
			// TODO: verify only 0-1 occurrences?
			this.spectralElement2 = (spec2 == null) ? null : spec2.text();

			//
			Element slit = instrument.select("Slit").first();
			// TODO: verify only 0-1 occurrences?
			this.instrumentSlit = (slit == null) ? null : slit.text();

		} else if ( (objType.text().equals("NonSidereal")) || 
				(objType.text().equals("TargetMovingSingle")) ) {
			
			// TODO: Set properties accordingly

		} else {
			System.out.println("Unknown object type:  " + objType.text());
			
			// TODO: Set properties accordingly

		}
	}

	/**
	 * @return
	 */
	public HashMap<String, String[]> getObjColInfo() {
	    //
	    String [] names = {"NAME",
	                        "_RAJ2000",
	                        "_DEJ2000",
	                        "AORID",
	                        "INSTRUMENT",
	                        "CONFIG",
	                        "MODE",
	    };
	    String [] datatypes = {"char",
	                            "double",
	                            "double",
	                            "char",
	                            "char",
	                            "char",
	                            "char",
	    };
	    String [] units = {"",
	                        "deg",
	                        "deg",
	                        "",
	                        "",
	                        "",
	                        "",
	    };
	    String [] ucds = {"meta.id;meta.main",
	                        "pos.eq.ra;meta.main",
	                        "pos.eq.dec;meta.main",
	                        "meta.note",
	                        "meta.note",
	                        "meta.note",
	                        "meta.note",
	    };
	    String [] widths = {"13",
	                        "13",
	                        "13",
	                        "",
	                        "",
	                        "",
	                        "",
	    };
	    String [] arraysizes = {"13",
	                            "",
	                            "",
	                            "13",
	                            "13",
	                            "13",
	                            "13",
	    };
	    String [] precisions = {"",
	                            Byte.toString(this.getLonPrec()),
	                            Byte.toString(this.getLatPrec()),
	                            "",
	                            "",
	                            "",
	                            "",
	    };
	    //
	    HashMap<String, String[]> colInfo = new HashMap<String, String[]>();

	    //
	    colInfo.put("names", names);
	    colInfo.put("datatypes", datatypes);
	    colInfo.put("units", units);
	    colInfo.put("ucds", ucds);
	    colInfo.put("widths", widths);
	    colInfo.put("arraysizes", arraysizes);
	    colInfo.put("precisions", precisions);

	    return colInfo;
	}

	/**
	 * @return
	 */
	public String[] getObjValues() {
	    //
	    String[] values = {this.getAstroObjectName(),
			                        Double.toString(this.getLon()),
			                        Double.toString(this.getLat()),
			                        this.getAorID(),
			                        this.getInstrumentName(),
			                        this.getInstrumentConfig(),
			                        this.getInstrumentMode()};
	    return values;
	}

    public String getAstroObjectType() {
		return astroObjectType;
	}

	public String getAstroObjectName() {
		return astroObjectName;
	}

	public String getAorID() {
		return aorID;
	}

	public boolean isSidereal() {
		return sidereal;
	}

	public String getEquinox() {
		return equinox;
	}

	public String getInstrumentName() {
		return instrumentName;
	}

	public String getInstrumentConfig() {
		return instrumentConfig;
	}

	public String getInstrumentMode() {
		return instrumentMode;
	}

	public String getInstrumentSlit() {
		return instrumentSlit;
	}

	public String getSpectralElement() {
		return spectralElement;
	}

	public String getSpectralElement2() {
		return spectralElement2;
	}

	public String getMapArea() {
		return mapArea;
	}

	public String getPropMotnRA() {
		return propMotnRA;
	}

	public String getPropMotnDec() {
		return propMotnDec;
	}
	
	/**
     * Converts the coord string to 00:00:00.0 format if it is in the
     * format of the Regular expression RAHMS and DECDMS.
     * @param coord
     * @return
     */
    private String formatCoord(String coord) {
        String[] params = coord.split(" ");
        String str = "";

        /* Regular expressions for the coordinates in HMS +/-DMS format:
         *
         * e.g. 00h00m00.0s -00d00m00.0s
         *
         */
        String RAHMS = ("[0-9]{1,2}[h]" +
					            "[0-9]{1,2}[m]" +
					            "[0-9]{1,2}[\\.][0-9]{1,}[s]");
        String DECDMS = ( "[+\\-]?" +
						            "[0-9]{1,2}[d]" +
						            "[0-9]{1,2}[m]" +
						            "[0-9]{1,2}[\\.][0-9]{1,}[s]");

        for (int j=0; j<params.length; j++) {

            if (params[j] == " ") continue;
            params[j] = params[j].trim();
            if ( (params[j].matches(RAHMS)) ||  (params[j].matches(DECDMS)) ) {
                params[j] = params[j].replaceAll("[a-rt-z]", ":");
                params[j] = params[j].replaceAll("[s]", "");
            }
            str = str + params[j] + " ";
        }
        return str;
    }
}
