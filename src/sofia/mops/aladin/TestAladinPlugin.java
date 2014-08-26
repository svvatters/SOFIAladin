package sofia.mops.aladin;
import cds.aladin.*;

public class TestAladinPlugin extends AladinPlugin {

   public String menu() { return "TestAladinPlugin"; }
   public String description() { return "Testing an Aladin Plugin"; } 

   public void exec() {
      try {
          AladinData ad = aladin.getAladinData();

         AladinData sd = Aladin.aladin.getAladinImage();
         double [][] pix = sd.getPixels();
         int w = sd.getWidth();
         int h = sd.getHeight();
         double [][] rotpix = new double[h][w];
         for( int y=0; y<h; y++) {
            for( int x=0; x<w; x++ ) rotpix[y][w-x-1] = pix[x][y];
         }
         sd.setPixels(rotpix);
      } catch( AladinException e ) { e.printStackTrace(); }
   } 
}