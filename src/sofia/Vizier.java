package sofia;

// Sources of Astronomical Objects
public enum Vizier {
    OTHER("", ""),
    HIP2("hip2", "Hpmag"),
    UCAC4("ucac4", "Vmag"),
    TYCHO2("Tycho-2", "VTmag"),
    SIMBAD("simbad", "V");

    private String name;
    private String visMagCol;
    
    private Vizier(String name, String visMag) {
        this.name = name;
        this.visMagCol = visMag;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getVisMagCol() {
        return this.visMagCol;       
    }

}
