//import javax.json;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStage;
//import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPred;
import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPredFactory;

final public class WLTools {

   //static {
   //   System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
   //}

   private static String mainExecDir= "";

   // ---
   final static String getMainExecDir() {
      return mainExecDir;
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

      mainExecDir = System.getProperty("user.dir");
      System.out.println("WLTools main: mainExecDir= "+mainExecDir);
      System.out.println("WLTools main: debug exit(0)");
      System.exit(0);

      final String nsTCInputFile=
         "/home/gme042/slinks/fs7_isi_dfo_chs_enav/NSTide/output/TFHA/onedGridPointsClusters/Deschaillons/tmp/gridPoint-540-TFHA.json";

      ///final startSse= new Date().getTime()/1000L;
      final long unixTimeNow = System.currentTimeMillis() / 1000L;

      Calendar gcld= new GregorianCalendar().getInstance(TimeZone.getTimeZone("GMT"));
      gcld.set(GregorianCalendar.YEAR,2014);
      gcld.set(GregorianCalendar.MONTH,1);
      gcld.set(GregorianCalendar.DAY_OF_MONTH,1);
      gcld.set(GregorianCalendar.HOUR_OF_DAY,0);

      final long testStartTime= gcld.getTimeInMillis()/1000L;

      final long endPredTime= testStartTime + 40L*24L*3600L; //  unixTimeNow + 40L*24L*3600L

      final WLStationPredFactory wlStnPrdFct= new WLStationPredFactory("Deschaillons:gridPoint-540",
                                                                       testStartTime, //unixTimeNow,
                                                                       endPredTime,
                                                                       900L,
                                                                       46.55,
                                                                       ITidal.Method.NON_STATIONARY_FOREMAN,
                                                                       nsTCInputFile,
                                                                       ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON,
                                                                       IStage.Type.DISCHARGE_CFG_STATIC,
                                                                       null, // --- IStage.Type.DISCHARGE_CFG_STATIC: Stage data taken from inner config DB
                                                                       null // --- IStage.Type.DISCHARGE_CFG_STATIC IStageIO.FileFormat is JSON by default.
                                                                       );

      System.out.println("WLTools main end");
  }
}
