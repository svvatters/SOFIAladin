package sofia.dcs;

import java.util.HashMap;

/**
 * @author shannon.watters@gmail.com
 * @SOFIA_Aladin-extension interface
 */
public interface AladinObj {

    /**
     * @return
     */
    public HashMap<String, String[]> getObjColInfo();

    /**
     * @return
     */
    public String[] getObjValues();
}
