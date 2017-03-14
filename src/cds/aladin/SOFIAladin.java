package cds.aladin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import cds.astro.Astrocoo;
import sofia.Imager;

/**
 * @author shannon.watters@gmail.com
 * @SOFIA_Aladin-extension class
 */
public abstract class SOFIAladin extends Aladin {
    
    //
    enum Regex {    
        RAHMS("[0-9]{1,2}[h]" + 
                "[0-9]{1,2}[m]" + 
                "[0-9]{1,2}[\\.][0-9]{1,}[s]"), 
        DECDMS( "[+\\-]?" +
                "[0-9]{1,2}[d]" +
                "[0-9]{1,2}[m]" +
                "[0-9]{1,2}[\\.][0-9]{1,}[s]");

        private String value;

        private Regex(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }
    }

    //
    enum Shapes {CIRCLE, SQUARE}

    /**
     * @param a
     * @param colInfo
     * @param vals
     * @return
     * @throws AladinException
     */
    public static Obj addObjToStack(Aladin a, 
            HashMap<String, String[]> colInfo, String[] vals) 
            throws AladinException {
        //
        AladinData plane = null;
        
        // If the plane exists get it's data otherwise create it
        try {
            plane = a.getAladinData(vals[0]);
        } catch (AladinException e1) {                
            // 
            plane = a.createAladinCatalog(vals[0]);     
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
        Obj obj = plane.addSource(vals[0], Double.parseDouble(vals[1]), 
                Double.parseDouble(vals[2]), vals);       
        plane.objModified();
        
        return obj;
    }

    /**
     * 
     * @param params Argument String (ra, dec, object name, 
     *              catalog/plane name)
     *              Examples of valid "ra dec":
     *                  335.852333   -9.982667
     *                  22h23m24.56s -9d58m57.6s
     *                  22:23:24.56  -9:58:57.6
     *                  22 23 24.56  -9 58 57.6
     *              (Note: leading zeros and "+" are also 
     *              allowed for dec degs)
     */
    public static void addPos(String params) {  
        // TODO add try statement(s)
        
        String[] args = params.split(",");  
        Astrocoo ac = createAstrocoo(args[0], args[1]);         
        addPosToPlane(ac, args[2], args[3]);
    }  
    
    /**
     * @param params
     */
    public static void addPosOffset(String params) {    
    	
    	//TODO: Document params
    	
        String[] args = params.split(",");  
        
        //TODO: Surround with try to check args
        Astrocoo ac = createAstrocoo(args[0], args[1]);
        
        //TODO: Check that args[2] and args[3] are valid offsets in arcsec
        
        // Calculate the RA Declination dependence term
        double raDecDep = 3600.0 * 
                            Math.cos((Math.PI/180.0) * ac.getLat());
        
        // Calculate the offsets in RA and Dec
        double raOffset = (Double.valueOf(args[2]) / raDecDep);
        double decOffset = (Double.valueOf(args[3]) / 3600.0);
        
        // Apply the offsets to the RA and Dec 
        double newRA = ac.getLon() + raOffset;
        double newDec = ac.getLat() + decOffset;
        
        // TODO Deal with the celestial poles?
        
        // Create a new Astrocoo with the new offset coordinates
        Astrocoo newAC = createAstrocoo(Double.toString(newRA), 
                                        Double.toString(newDec));

        addPosToPlane(newAC, args[4], args[5]);
    }

    /**
     * Adds a new position to the Aladin stack Plane plane.
     * @param ac
     * @param name
     * @param plane
     */
    static private void addPosToPlane(Astrocoo ac, String name, String plane) {
        // Clean up the name and plane Strings
        name = SOFIAladin.formatName(name.trim());
        plane = SOFIAladin.formatName(plane.trim());

        AladinData cat = null;

        double lon = ac.getLon();
        double lat = ac.getLat();
        String[] values = { name.trim(), Double.toString(lon),
                Double.toString(lat) };

        /*
         * If catalog plan already exists get it otherwise create it first
         * (after catching the error thrown by trying to get a null plan).
         */
        try {
            cat = Aladin.aladin.getAladinData(plane);
        } catch (AladinException e1) {
            if (e1.getMessage().equals(cds.aladin.AladinData.ERR001)) {
                try {
                    String[] names = { "NAME", "_RAJ2000", "_DEJ2000" };
                    String[] datatypes = { "char", "double", "double" };
                    String[] units = { "", "deg", "deg" };
                    String[] ucds = { "meta.id;meta.main",
                            "pos.eq.ra;meta.main", "pos.eq.dec;meta.main" };
                    String[] widths = { "22", "13", "13" };
                    String[] arraysizes = { "*", "13", "13" };
                    String[] precisions = { "", "8", "8" };
                    cat = Aladin.aladin.createAladinCatalog(plane);
                    cat.setDatatype(datatypes);
                    cat.setName(names);
                    cat.setUnit(units);
                    cat.setUCD(ucds);
                    cat.setWidth(widths);
                    cat.setArraysize(arraysizes);
                    cat.setPrecision(precisions);
                } catch (AladinException e) {
                    // TODO Auto-generated catch block

                    e.printStackTrace();
                }
            } else {
                // TODO Auto-generated catch block

                e1.printStackTrace();
            }
        }
        try {
            cat.addSource(name, lon, lat, values);
            cat.objModified();
        } catch (AladinException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        }
    }

    /**
     * Returns a String with only alpha-numeric, underscore, dot (.), or
     * dash (-) chars
     * 
     */
    public static String formatName(String name) {
        return(name.replaceAll("[^a-zA-Z0-9\\._-]", "_"));          
    }

    /**
     * Converts the coord string to 00:00:00.0 format if it is in the 
     * format of Regex RAHMS or DECDMS. 
     * @param coord
     * @return 
     */
    public static String formatCoord(String coord) {
        String[] params = coord.split(" ");
        String str = "";
        for (int j=0; j<params.length; j++) {
            
            if (params[j] == " ") continue;         
            params[j] = params[j].trim();           
            if ( (params[j].matches(Regex.RAHMS.getValue())) || 
                    (params[j].matches(Regex.DECDMS.getValue())) ) {
                params[j] = params[j].replaceAll("[a-rt-z]", ":");
                params[j] = params[j].replaceAll("[s]", "");
            }           
            str = str + params[j] + " ";
        }   
        return str;
    }

    public static AladinData[] concatPlanes(AladinData[] A, AladinData[] B) {
        int aLen = A.length;
        int bLen = B.length;
        AladinData[] C= new AladinData[aLen+bLen];
        System.arraycopy(A, 0, C, 0, aLen);
        System.arraycopy(B, 0, C, aLen, bLen);
        return C;
     }

    /**
     * @param coords
     * @return
     */
    public static Astrocoo createAstrocoo(String[] coords) {
        return (createAstrocoo(coords[0], coords[1]));
    }
    /**
     * Creates a cds.astro.Astrocoo instance to make use it's 
     * coordinate checks and conversions
     * @param ra
     * @param dec
     * @return
     */
    public static Astrocoo createAstrocoo(String ra, String dec) {
        Astrocoo ac = null;
        
        /* 
         * Check that the coord strings are in a format Astrocoo can
         * take for initialization
         */
        ra = SOFIAladin.formatCoord(ra);
        dec = SOFIAladin.formatCoord(dec);
        
        /* Use an Astrocoo to handle different "ra dec" input formats.
         * Examples:
         * 335.852333  -9.982667   (degs)
         * 22 23 24.56 -9 58 57.6  (hrs min sec degs arcmin arcsec)
         * 22:23:24.56 -9:58:57.6
         * (Note: leading zeros and "+" are also allowed for dec degs)
         */
        try {                           
            ac = new Astrocoo("(ICRS) " + ra + " " + dec);
        } catch (ParseException pe) {
            // TODO 
        } catch (Exception e) {
            // TODO
        }       
        // TODO Handle RA and Decs that end up out of bounds
    
        ac.setPrecision(7, 6);      
        return ac;
    }

    /**
     * Returns the SOFIA-Rotation-of-Flight for aladin's currently
     * selected ViewSimple
     * @param aladin
     * @return
     */
    static double getViewSimpleSOFIAROF(Aladin aladin) {
        return getViewSimpleSOFIAROF(aladin.view.getCurrentView());
    }   
    /**
     * Returns the SOFIA-Rotation-of-Flight for the given ViewSimple
     * @param viewSimple
     * @return
     */
    static double getViewSimpleSOFIAROF(ViewSimple viewSimple) {
        double sofiaROF = 0.0;
        double astroPA = viewSimple.getProj().rot;
        
        		// Convert 'astronomical position angle' to 'SOFIA position angle'
            if (astroPA != 0.0) {sofiaROF = 360.0 - (astroPA);}
        
            return sofiaROF;        
    }

    /**
     * Sets SOFIA-Rotation-of-Field for Aladin's selected ViewSimple
     * @param aladin
     */
    public static void setViewSimpleSOFIAROF(Aladin aladin) {
        setViewSimpleSOFIAROF(aladin.view.getCurrentView());
    }        
    /**
     * Sets SOFIA-Rotation-of-Field for the param viewSimple
     * @param viewSimple
     */
    static void setViewSimpleSOFIAROF(ViewSimple viewSimple) {
        
        // TODO RENAME THIS METHOD!!  IT'S NOT A SETTER (PROMPTS USER)
        
        // Get currently selected SimpleView's SOFIA-Rotation-of-Field
        double sofiaROF = SOFIAladin.getViewSimpleSOFIAROF(viewSimple);

        // Formatting for display of current sofiaROF
        DecimalFormat df = new DecimalFormat("##0.0#");

        // Prompt the user for a new SOFIA ROF value
        double userInput;
        try {
            userInput = SOFIAladin.showNumInputDialog(
                                            viewSimple.aladin.f,
                                            0.0, 
                                            360.0, 
                                            "SOFIA Rotation of Field:", 
                                            "Current View SOFIA ROF",  
                                            df.format(sofiaROF));
        } catch (UserCanceledException e) {
            // "Cancel" pressed so return without changing SOFIAROF
            return;
        }
        // Convert SOFIA-ROF to Astro-PA
        double newAstroPA = 0.0;
        if (userInput != 0.0) {newAstroPA = 360.0 - userInput;}

        /*
         *  TODO The informational overlay rotates but the 
         *  images plane overlays don't rotate until the
         *  view is selected again.
         */
        viewSimple.projLocal.setProjRot(newAstroPA);    
        viewSimple.projLocal.toNorth(newAstroPA);   
    }

    /**
     * Draws the current Rotation of Field value as seen by SOFIA if the
     * SOFIA Imagers overlay is active. It is at the top of the 
     * SimpleView & centered horizontally.
     * @param vs
     * @param g
     */
    public static void drawSOFIAROFValue(ViewSimple vs, Graphics g) {
        if (!vs.calque.hasSOFIAImagers()) {
            
            double sofiaPA = getViewSimpleSOFIAROF(vs);

            /*
             *  Format the display string and determine the starting position
             *  needed to center it horizontally  
             */
            g.setColor(Color.orange);
            g.setFont(g.getFont().deriveFont(14.0f));    
            DecimalFormat df = new DecimalFormat("###.##");
            String rof = "SOFIA Rotation of Field: " + df.format(sofiaPA);      
            int stringWidth = g.getFontMetrics().stringWidth(rof);       
            int stringPosition = (int)((vs.rv.getWidth() - stringWidth) / 2.0);    
            // Draw the display string
            g.drawString(rof, stringPosition, (15));
        }
    }
    
    /**
     * Draws outlines of the SOFIA Imager Field-of-views if the
     * SOFIA Imagers overlay is active.
     * @param vs
     * @param g
     */
    public static void drawSOFIAImagers(ViewSimple vs, Graphics g){
        // TODO Get more accurate dimensions for FPI
        if (!vs.calque.hasSOFIAImagers()) {
              g.setColor(Color.orange);
              
              // TODO Use sofia.Imager enum here for radius (d / 2)
              AltAzFov fpiFov = new AltAzFov(vs, g, 
                      (Imager.FPI.getFOVRadius() * 2.0), Shapes.CIRCLE);              
              fpiFov.draw();                  
              
              // TODO Use sofia.Imager enum here for radius (d / 2)
              AltAzFov ffiFov = new AltAzFov(vs, g, 
                      (Imager.FFI.getFOVRadius() * 2.0), Shapes.SQUARE);
              ffiFov.draw();
 
              // TODO Use sofia.Imager enum here for radius (d / 2)
              AltAzFov wfiFov = new AltAzFov(vs, g, 
                      (Imager.WFI.getFOVRadius() * 2.0), Shapes.SQUARE);
              wfiFov.draw();
         }  
    }

    /**
    * Opens a new SOFIAExportPosDialog for use with Aladin.
    * 
    */
    public static void openExportPosDialog(Aladin aladin) {
    
        ExportPosDialog dialog = null;
    
        try {
            dialog = new ExportPosDialog(aladin);
        } catch (IOException io) {
            JOptionPane.showMessageDialog(aladin.f,
                    "There aren't any available catalog planes to export",
                    "No Data Warning",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }       
        Point pos = aladin.getLocation();
        pos.translate(20, 40);
        dialog.setSize(new Dimension(710, 600));
        dialog.setLocation(pos);
        dialog.setVisible(true);
    }
    
    /**
     * Removes duplicates from a Collection and preserves it's order
     * @param c
     * @return
     */
    public static <E> Set<E> removeDups(Collection<E> c) {
        return new LinkedHashSet<E>(c);
    }

    /**
     * Turns the contents of a File into a single String
     * @param f
     * @return
     * @throws FileNotFoundException
     */
    public static String streamToString(InputStream s) throws FileNotFoundException {
        
        // TODO: Use some kind of buffer for larger files
        
        Scanner scan = new Scanner(s);
        String string = scan.useDelimiter("\\Z").next();
        scan.close();
        return string;
    }

    /**
     * @param s
     * @param f
     * @throws IOException
     */
    public static void stringToFile(String s, File f) throws IOException {
        f.createNewFile();
        BufferedWriter outWriter = new BufferedWriter(new FileWriter(f));
        outWriter.write(s);
        outWriter.flush();
        outWriter.close();     
    }

    /**
     * @param string
     * @param listB
     * @return
     */
    public static String[] concatStringLists(String string, String[] listB) {
        String[] list = {string};
        return(concatStringLists(list, listB));
    }   
    /**
     * @param listA
     * @param listB
     * @return
     */
    public static String[] concatStringLists(String[] listA, String[] listB) {
        int aLen = listA.length;
        int bLen = listB.length;
        String[] C= new String[aLen+bLen];
        System.arraycopy(listA, 0, C, 0, aLen);
        System.arraycopy(listB, 0, C, aLen, bLen);
        return C;
     }

    /**
     * @param parent
     * @param minimum
     * @param maximum
     * @param promptString
     * @param title
     * @param seedValue
     * @return
     * @throws UserCanceledException 
     */
    public static double showNumInputDialog(JFrame parent, 
                                            double minimum, 
                                            double maximum, 
                                            String promptString, 
                                            String title,
                                            String seed) 
                                        throws UserCanceledException {
        // Construct the prompt message using the promptString,
        // minimum, and maximum values
        String promptMessage = (promptString + 
                                "  (" + minimum + " - " + maximum + "):");
        //
        while (true) {

            //
            Object userInput = JOptionPane.showInputDialog(
                                            parent, 
                                            promptMessage, 
                                            title, 
                                            JOptionPane.QUESTION_MESSAGE, 
                                            null, 
                                            null, 
                                            seed);      
            // If "Cancel" was clicked throw an Exception
            if (userInput == null) {throw new UserCanceledException();}
                
            // Check the validity of the user input
            try {
                // If the string isn't a valid double parseDouble()
                // will throw a NumberFormatException
                double userValue = Double.parseDouble(
                                            userInput.toString());
                
                // If the double isn't in the range minimum - maximum
                // throw a NumberFormatException
                if ((userValue < minimum) || (userValue > maximum)) {
                    throw new NumberFormatException();                      
                }
                // Return the valid user input value
                return userValue;           
                
            } catch (NumberFormatException e) {
                //
                String infoMessage = ("Input value ('" + 
                                    userInput.toString() + 
                                    "') is invalid.\n" +
                                    "Valid range:  " +
                                    minimum + 
                                    " - " + 
                                    maximum + "\n" + 
                                    "Please try again");
                //
                JOptionPane.showMessageDialog(null, infoMessage);
            }
        }
    }

    public static void main(String[] args) {
        for( int i=0; i<args.length; i++ ) {
        		if ( args[i].equals("-mopsreport") ){
        			try {
						sofia.ObsPlanTrackReporter.main(new String[0]);
					} catch (IOException | ParseException | AladinException e) {
						// TODO Return the error to the user 
						e.printStackTrace();
					}
        		}
        }
		launch("");
    }
}
