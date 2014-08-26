package sofia.mops;

import cds.aladin.Aladin;
import cds.aladin.AladinData;
import cds.aladin.AladinException;

public class SOFIA_Aladin{

    private static Aladin aladin;
    private static AladinData ad;
    
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        // Instantiate Aladin and set its level of verbosity
        aladin = cds.aladin.Aladin.launch("");
        try {
            ad = new cds.aladin.AladinData(aladin, -1);
//            ad.addSource("new", 0.5, 0.5, ("1", "2"));
        } catch (AladinException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
