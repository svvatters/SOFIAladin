# SOFIAladin
##### Aladin Sky Atlas extended to help observation preparation at SOFIA

* Aladin is developed by [Centre de Données astronomiques de Strasbourg](http://cdsweb.u-strasbg.fr/):
  * [Aladin Sky Atlas Home](http://aladin.u-strasbg.fr/)
  * [Aladin Download Page](http://aladin.u-strasbg.fr/java/nph-aladin.pl?frame=downloading)
  * [Download Aladin v8.040](https://github.com/svvatters/SOFIAladin/blob/master/AladinSrc.jar) from this repository
  
* SOFIAladin Executables [here](https://github.com/svvatters/SOFIAladin/releases)

* Build SOFIAladin from Source:
  * Requires JDK 1.8 or greater
  * Download and uncompress the [source files](https://github.com/svvatters/SOFIAladin.git)
  * Move to the `SOFIAladin-master/` directory
  * Run build.xml with [Apache Ant](http://ant.apache.org/) 
  
    `$ ant`
  * Ant will do a clean, compile and create the executable `SOFIAladin.jar` in `dist/`

* Run executable SOFIAladin.jar
  * Clicking on the jar file's icon may start it (if the system is configured to do so)
  * From the commmand-line:
  
    `$ java -jar SOFIAladin.jar`
