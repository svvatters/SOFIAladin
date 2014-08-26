package sofia.mops.aladin;

import java.io.InputStream;

import cds.aladin.Aladin;
import sofia.dcs.aor.Pointing;

public class AladinUtils {

    public static InputStream queryVizier(Aladin a, Pointing p, 
            sofia.mops.aladin.Catalog catalog, double radius, double maxMag) {
 
        // TODO RENAME THIS METHOD

        String catName = catalog.getName();
        String filterName = "mag_" + catName;
                
        a.execCommand("temp = get vizier(" + catName + ") " +
                       p.getLon() + " " + p.getLat() + " " + 
                       radius + "deg");
        
        a.execCommand("filter " + filterName +
                        " { ${" + catalog.getVisMagCol() + "} <" + maxMag + 
                        " {draw white square}}");
        a.execCommand("filter " + filterName + " on");
        a.execCommand("select temp");
        a.execCommand("cplane " + catName);
        a.execCommand("rm temp");
        a.execCommand("rm " + filterName);

        // Return a votable 
        return(a.getVOTable(catName));
    }
}
