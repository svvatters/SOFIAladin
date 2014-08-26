package sofia.mops.aladin;

// Sources of Astronomical Objects
public enum Catalog {
    OTHER("", ""),
    VIZIER_HIP2("hip2", "Hpmag"),
    VIZIER_UCAC4("ucac4", "f.mag"),
    VIZIER_TYCHO2("Tycho-2", "VTmag"),
    SIMBAD("simbad", "V");

    private String name;
    private String visMag;
    
    private Catalog(String name, String visMag) {
        this.name = name;
        this.visMag = visMag;
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getVisMagCol() {
        return this.visMag;       
    }
}
