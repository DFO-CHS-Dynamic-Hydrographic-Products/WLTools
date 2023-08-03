//import javax.json;
import java.io.File;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Calendar;
import java.net.URISyntaxException;
import java.util.GregorianCalendar;

import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStage;
//import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPred;
import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPredFactory;

final public class WLTools extends WLToolsIO {

   //static {
   //   System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
   //}

   //private static String mainCfgDir= "";

   // ---
   //final static String getMainCfgDir() {
   //   return mainCfgDir;
   //}

   static public void main (String[] args) {

      System.out.println("WLTools main start");

      Map<String, String> argsMap = new HashMap<>();

      for (String arg: args) {
        String[] parts = arg.split("=");
        argsMap.put(parts[0], parts[1]);
      }

      //for (String key: argsMap.keySet()){
      //   System.out.println("WLTools main argsMap key="+key);
      //   System.out.println("WLTools main argsMap value="+argsMap.get(key));
      //}

      //if ("--mainExecDir" not
      //final String binDir= argsMap.get("--binDir");

      //final String mainExecDir= System.getProperty("user.dir");
      //System.out.println("WLTools main: binDir= "+binDir);

      File binDir= null;

      try {
         binDir= new File(WLTools.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      } catch (URISyntaxException e) {
         throw new RuntimeException(e);
      }

      System.out.println("WLTools main: binDir="+binDir);
      //System.out.println("WLTools main: debug exit(0)");
      //System.exit(0);

      //this.super(mainExecDir + "/../"+IWLToolsIO.PKG_CFG_MAIN_DIR);
     // mainCfgDir= mainExecDir + "/../"+IWLToolsIO.PKG_CFG_MAIN_DIR;

      //final WLToolsIO io= new WLToolsIO(mainExecDir + "/../"+IWLToolsIO.PKG_CFG_MAIN_DIR);
      WLToolsIO.setMainCfgDir(binDir+ "/../"+IWLToolsIO.PKG_CFG_MAIN_DIR);

      System.out.println("WLTools main: mainCfgDir= "+WLToolsIO.getMainCfgDir());

      //System.out.println("WLTools main: debug exit(0)");
      //System.exit(0);

      final String nsTCInputFile=
         "/home/gme042/slinks/fs7_isi_dfo_chs_enav/NSTide/output/TFHA/onedGridPointsClusters/Deschaillons/tmp/gridPoint-540-TFHA.json";

      ///final startSse= new Date().getTime()/1000L;
      final long unixTimeNow = System.currentTimeMillis() / 1000L;

      Calendar gcld= new GregorianCalendar().getInstance(TimeZone.getTimeZone("GMT"));
      gcld.set(GregorianCalendar.YEAR, 2014); //2023); //2014);
      gcld.set(GregorianCalendar.MONTH, 0); //7); //0); // --- January is month 0 in Java.
      gcld.set(GregorianCalendar.DAY_OF_MONTH,1);
      gcld.set(GregorianCalendar.HOUR_OF_DAY,0);
      gcld.set(GregorianCalendar.MINUTE,0);
      gcld.set(GregorianCalendar.SECOND,0);

      final Long testStartTime= gcld.getTimeInMillis()/1000L;

      final Long endPredTime= testStartTime + 40L*24L*3600L; //  unixTimeNow + 40L*24L*3600L

      final WLStationPredFactory wlStnPrdFct= new WLStationPredFactory("StLawrence:Deschaillons:gridPoint-540-SCD",
                                                                       testStartTime, //unixTimeNow,
                                                                       endPredTime,
                                                                       900L,//180L, //900L, //3600L,//900L,
                                                                       46.55,
                                                                       ITidal.Method.NON_STATIONARY_FOREMAN,
                                                                       nsTCInputFile,
                                                                       ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON,
                                                                       IStage.Type.DISCHARGE_CFG_STATIC,
                                                                       null, // --- IStage.Type.DISCHARGE_CFG_STATIC: Stage data taken from inner config DB
                                                                       null // --- IStage.Type.DISCHARGE_CFG_STATIC IStageIO.FileFormat is JSON by default.
                                                                       );

      wlStnPrdFct.getTidalPredictionsForStation();

      System.out.println("WLTools main end");
  }
}
