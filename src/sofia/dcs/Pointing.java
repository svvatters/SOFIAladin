package sofia.dcs;

import java.text.ParseException;
import java.util.HashMap;

import cds.aladin.SOFIA_Aladin;
import cds.astro.Astrocoo;
import cds.astro.Astroformat;
import sofia.dcs.obsplan.FixedTarget;
import sofia.dcs.obsplan.Observation;

/**
 * @author shannon.watters@gmail.com
 *
 */
@SuppressWarnings("serial")
public final class Pointing extends Astrocoo {
    
    private final String obsID;
    private final String objName;
    private final String instrumentID;
    private final String configuration;
    private final String equinox;
    
    /**
     * @param obs
     * @param ft
     * @throws ParseException 
     */
    public Pointing(Observation obs, FixedTarget ft) throws ParseException {
        this.obsID = obs.getId();
        this.objName = ft.getObjectName();
        this.instrumentID = obs.getInstrumentId();
        this.configuration = obs.getInstrumentConfigurations();
        this.equinox = ft.getEquinox();
        
        //Format the coords so they work as inputs for parent Astrocoo.set()
        String ra = cds.aladin.SOFIA_Aladin.formatCoord(ft.getRightAscension());
        String dec = cds.aladin.SOFIA_Aladin.formatCoord(ft.getDeclination());
        /*
         *  Use the parent class constructor to set the coordinates. Throws
         *  a ParseException
         */
        this.set(ra + " " + dec);        
        this.setPrecision(7, 7);
        // TODO: Handle RA and Decs that end up out of bounds
    }
    
    /**
     * @return the obsID
     */
    public String getObsID() {
        return obsID;
    }

    /**
     * @return the objName
     */
    public String getObjName() {
        return objName;
    }

    /**
     * @return the instrumentID
     */
    public String getInstrumentID() {
        return instrumentID;
    }

    /**
     * @return the configuration
     */
    public String getConfiguration() {
        return configuration;
    }

    /**
     * @return the equinox
     */
    public String getEquinox() {
        return equinox;
    }

    /**
     * @return
     */
    public HashMap<String, String[]> getObjColInfo() {
        // 
        String [] names = {"NAME",
                            "_RAJ2000", 
                            "_DEJ2000",
                            "OBSID",
                            "INSTRUMENT",
                            "CONFIG",
        };
        String [] datatypes = {"char",
                                "double", 
                                "double",
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
        };
        String [] ucds = {"meta.id;meta.main",
                            "pos.eq.ra;meta.main", 
                            "pos.eq.dec;meta.main",
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
        };
        String [] arraysizes = {"13",
                                "", 
                                "",
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
        String[] values = {this.getObjName(), 
                            Double.toString(this.getLon()),
                            Double.toString(this.getLat()),
                            this.getObsID(),
                            this.getInstrumentID(),
                            this.getConfiguration()};
        return values;                        
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        
        //
        String[] c = super.toString(Astroformat.SEXA3h).split("s", 2);
        // Replace the 's' stripped from the ra coord
        c[0] = c[0] + 's';      
                
        return (this.obsID + "\t" + 
                this.objName + "\t" + 
                c[0] + "\t" + 
                c[1] + "\t" + 
                this.equinox + "\t" +
                this.instrumentID + "\t" +
                this.configuration);  
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((configuration == null) ? 0 : configuration.hashCode());
        result = 
                prime * result + ((equinox == null) ? 0 : equinox.hashCode());
        result = prime * result
                + ((instrumentID == null) ? 0 : instrumentID.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {

        /*
         *  This block was not generated by Eclipse
         */
        if (obj == null) 
            return false;
       
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof Pointing))
            return false;
        Pointing other = (Pointing) obj;
        if (configuration == null) {
            if (other.configuration != null)
                return false;
        } else if (!configuration.equals(other.configuration))
            return false;
        if (equinox == null) {
            if (other.equinox != null)
                return false;
        } else if (!equinox.equals(other.equinox))
            return false;
        if (instrumentID == null) {
            if (other.instrumentID != null)
                return false;
        } else if (!instrumentID.equals(other.instrumentID))
            return false;
        return true;
    }
    
    // TESTING & DEBUGGING
    public static void main(String[] args) {
        System.out.println("sofia.dcs.Pointing.java");
    }
}
