package sofia.dcs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import sofia.dcs.aor.FixedTarget;
import sofia.dcs.aor.Observation;
import sofia.dcs.aor.ObservingPlan;
import sofia.dcs.aor.Pointing;

/**
 * @author shannon.watters@gmail.com
 * 
 */
public abstract class DCSUtils {

    /**
     * @param file
     * @return
     * @throws JAXBException
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static ArrayList<Pointing> getSiderealPointings(File file) 
            throws JAXBException, IOException, FileNotFoundException {    
        
        File[] fileList = {file};
        ArrayList<Pointing> output = new ArrayList<Pointing>();
        
        // Initialize JAXB, Unmarshaller, and Marshaller
        JAXBContext context = JAXBContext.newInstance(ObservingPlan.class);
        Unmarshaller um = context.createUnmarshaller();
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

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
        if (file.isDirectory()) {
            fileList = file.listFiles(textFilter);
        } else if ((file.isFile())) {
            // TODO Check to make sure it is an xml file
        } else {
            throw new FileNotFoundException();
        }       
        
        // Iterate through the files
        for (File f : fileList) {
            System.out.println("Processing: " + f.toString() + "...");

            // Get the observing plan from the File
            ObservingPlan plan = 
                            (ObservingPlan) um.unmarshal(new FileReader(f));
            
            // Append all the pointings from this File to the output
            appendSiderealPointings(plan, output);
        }     
        // Return the pointings from all the files together
        return output;
    }    

    /**
     * @param file
     * @return
     * @throws JAXBException
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static Set<Pointing> getUniqueSiderealPointings(File file) 
            throws JAXBException, IOException, FileNotFoundException {
    
        ArrayList<Pointing> pointings = 
                                getSiderealPointings(file);            
        // Remove the duplicates
        Set<Pointing> uniquePointings = removeDups(pointings);
        System.out.println(uniquePointings.size() + " unique pointings:");
        for (Pointing p : uniquePointings) {
            System.out.println(p);
        }
        return(uniquePointings);
    }
    

    /**
     * Appends the sidereal pointings (FixedTarget Objects in aor 
     * nomenclature) from a aor xml file to an ArrayList<String>
     * @param file
     * @return
     * @throws JAXBException
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static ArrayList<Pointing> appendSiderealPointings(
            ObservingPlan plan, ArrayList<Pointing> output) 
            throws JAXBException, IOException, FileNotFoundException {
                
        // Iterate through the observations in the plan
        ArrayList<Observation> observations = plan.getObservations();
        for (Observation obs : observations) {
                        
            /* 
             * Iterate through the sidereal pointings (FixedTarget objects)
             * and add them to the output
             */ 
            ArrayList<FixedTarget> targets = obs.getFixedTargets();
            for (FixedTarget t : targets) {
                
                Pointing point;
                try {
                    point = new Pointing(obs, t);
                    output.add(point);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }           
        }
        return output;
    }

    /**
     * Removes duplicates from a Collection while preserving it's order
     * @param c
     * @return
     */
    private static <E> Set<E> removeDups(Collection<E> c) {
        return new LinkedHashSet<E>(c);
    }   

    /**
     * FOR DEBUGGING
     * @param args
     * @throws JAXBException
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void main(String[] args) 
            throws JAXBException, IOException, FileNotFoundException {

        for (String arg : args) {
            for (Pointing p : getUniqueSiderealPointings(new File(arg))) {    
                System.out.println(p);                
            }
        }
    }
}
