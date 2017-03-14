# SOFIAladin

## Aladin Sky Atlas extended to help observation preparation at SOFIA

* Centre de Données astronomiques de Strasbourg's [Aladin Sky Atlas Home](http://aladin.u-strasbg.fr/)

* SOFIAladin Executables [here](https://github.com/svvatters/SOFIAladin/releases)

* Build SOFIAladin from Source:
  * Requires JDK 1.8 or greater
  * Download and uncompress the [source files](https://github.com/svvatters/SOFIAladin.git)
  * Build with [Apache Ant](http://ant.apache.org/) 
    * `cd` to the new directory `SOFIAladin-master/`
    * Run `build.xml` using Ant:

      `$ ant`
    * Ant will do a clean compile and create the executable `SOFIAladin.jar` in `dist/`

* Run executable SOFIAladin.jar
  * Clicking on the jar file's icon may start it (if the system is configured to do so)
  * From the commmand-line:
  
    `java -jar SOFIAladin.jar`