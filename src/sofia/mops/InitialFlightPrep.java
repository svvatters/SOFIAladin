package sofia.mops;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import cds.aladin.Aladin;
import cds.savot.model.SavotResource;
import cds.savot.pull.SavotPullEngine;
import cds.savot.pull.SavotPullParser;

import java.io.File;

import javax.xml.bind.JAXBException;

import sofia.Imager;
import sofia.dcs.aor.Pointing;
import sofia.mops.aladin.Catalog;

/**
 * @author shannon.watters@gmail.com
 *
 */
public abstract class InitialFlightPrep {

	/**
    	 * @param args
    	 */
    	public static void main(String[] arg) throws JAXBException, 
    												IOException, 
    												FileNotFoundException {    
    	    /*
    	     *  Using Aladin to save initial data collection for MOPS flight prep.
    	     *  Also using Aladin for it's built in query and filter commands.
    	     */
    	    final Aladin a;

    	    // Takes a single file or directory path
    	    File file = new File(arg[0]);

    	    /*
    	     *  Gather all the unique pointings in this group of aors into a
    	     *  LinkedHashSet of sofia.dcs.aor.Pointings.  
    	     *  The equals() and hashcode() methods for Pointings determine 
    	     *  what makes a pointing unique.
    	     */
	    Set<Pointing> pointings = 
	                    sofia.dcs.DCSUtils.getUniqueSiderealPointings(file);
	    
	    // TODO Save a pos file of these pointings
	    
	    // TODO Start logging/reporting
	    
	    // Instantiate Aladin and set its level of verbosity
        a = cds.aladin.Aladin.launch("");
        a.execCommand("trace 2");

        for (Pointing p : pointings) {
            // Clear any previous planes from Aladin
            a.execCommand("rm all");
            
            //
            a.execCommand("md Catalogs");
            
            //
            a.execCommand("addPos " + 
                    p.getLon() + "," + 
                    p.getLat() + "," +  
                    p.getObjName() + "," + "AOR");
            /*
             *  Calculate the distance from the center of the Imagers 
             *  (WFI & FFI) to the corners of the FFI imager for use as 
             *  the search radius.
             */
            double radiusWFI = Math.sqrt(
                    (Imager.WFI.getFOVRadius()*Imager.WFI.getFOVRadius()) * 2);
            double radiusFFI = Math.sqrt(
                    (Imager.FFI.getFOVRadius()*Imager.FFI.getFOVRadius()) * 2);
            
            // Gather objects within the radii for each imager & max magnitude
            InputStream hip2VOTStream = 
                    sofia.mops.aladin.AladinUtils.queryVizier(a, p, 
                            Catalog.VIZIER_HIP2, radiusWFI, 14.0);  
            a.execCommand("mv " + 
                    Catalog.VIZIER_HIP2.getName() + " Catalogs");
            
            //
            InputStream ucac4VOTStream = 
                    sofia.mops.aladin.AladinUtils.queryVizier(a, p, 
                            Catalog.VIZIER_UCAC4, radiusFFI, 14.0);  
            a.execCommand("mv " + 
                    Catalog.VIZIER_UCAC4.getName() + " Catalogs");

            // Save the gathered objects in a .aj (aladin) file
            a.execCommand("backup ./" + 
                                p.getObsID() + "-" + p.getObjName() + ".aj");
           
            // Parse the VO Tables
            SavotPullParser sbHip2 = new SavotPullParser(hip2VOTStream,
                    SavotPullEngine.SEQUENTIAL, "UTF-8");
            SavotPullParser sbUcac4 = new SavotPullParser(ucac4VOTStream,
                    SavotPullEngine.SEQUENTIAL, "UTF-8");
            
                // TODO parse down to coord info and increment through each
                
                    // TODO pass coords to new Coo object
                
                    // TODO get distance for p to Coo
                
                    // TODO sort the catalog objects for trackability
                
                        // TODO add the object to the appropriate aladin pane
            
            // TODO backup the .aj file      
            
            // TODO write the results to a report and/or log
        }       
        // Exit this program and Aladin
        System.exit(0);
    	}
}
