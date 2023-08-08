//import javax.json;
import java.io.File;
import java.util.Map;
import java.util.Set;
//import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
//import java.util.Calendar;
import java.net.URISyntaxException;
//import java.util.GregorianCalendar;

// ---
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStage;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStageIO;
import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPred;
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

      //--- tmp File object to be used to automagically determine the directory
      //    where the main program class is located.
      File binDir= null;

      try {

         binDir= new File(WLTools.class.getProtectionDomain().getCodeSource().getLocation().toURI());

      } catch (URISyntaxException e) {
         throw new RuntimeException(e);
      }

      System.out.println("WLTools main: binDir="+binDir);

      WLToolsIO.setMainCfgDir(binDir+ "/../"+IWLToolsIO.PKG_CFG_MAIN_DIR);

      System.out.println("WLTools main: mainCfgDir= "+WLToolsIO.getMainCfgDir());

      // --- Now get the --<option name>=<option value> from the args
      HashMap<String, String> argsMap = new HashMap<>();

      for (String arg: args) {
        String[] parts = arg.split("=");
        argsMap.put(parts[0], parts[1]);
      }

      final WLStationPred wlStationPred= new WLStationPred(argsMap); //.parseArgsOptions(argsMap);

      //System.out.println("WLTools main: debug System.exit(0)");
      //System.exit(0);

      wlStationPred.getAllPredictions();

      System.out.println("WLTools main end");
  }
}
