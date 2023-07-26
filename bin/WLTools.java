//import javax.json;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;

import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
//import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPred;
import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPredFactory;

final public class WLTools {

   static {
      System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
   }

   static public void main (String[] args) {

      System.out.println("WLTools main start");

      Map<String, String> argsMap = new HashMap<>();

      for (String arg: args) {
         String[] parts = arg.split("=");
         argsMap.put(parts[0], parts[1]);
      } 

      for (String key: argsMap.keySet()){
         System.out.println("WLTools main argsMap key="+key);
         System.out.println("WLTools main argsMap value="+argsMap.get(key));
      }

      final String nsTCInputFile=
         "/home/gme042/slinks/fs7_isi_dfo_chs_enav/NSTide/output/TFHA/onedGridPointsClusters/Deschaillons/tmp/gridPoint-540-TFHA.json";

      ///final startSse= new Date().getTime()/1000L;
      final long unixTimeNow = System.currentTimeMillis() / 1000L;

      final long endPredTime= unixTimeNow + 40L*24L*3600L;

      final WLStationPredFactory wlStnPrdFct= new WLStationPredFactory(ITidal.Method.NON_STATIONARY_FOREMAN,
                                                                       "Deschaillons-gridPoint-540",
                                                                       nsTCInputFile,
                                                                       ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON,
                                                                       unixTimeNow,
                                                                       endPredTime,
                                                                       900L,
                                                                       47.5);

      System.out.println("WLTools main end");
  }
}
