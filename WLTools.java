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

    final String mmi= "WLTools main: ";

    System.out.println(mmi+"start");

    //--- tmp File object to be used to automagically determine the directory
    //    where the main program class is located.
    File binDir= null;

    try {

      binDir= new File(WLTools.class.getProtectionDomain().getCodeSource().getLocation().toURI());

    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    System.out.println(mmi+"binDir="+binDir);

    // --- TODO: add an option arg. to define the mainCfgDir
    WLToolsIO.setMainCfgDir(binDir+ "/../"+IWLToolsIO.PKG_CFG_MAIN_DIR);

    System.out.println(mmi+"mainCfgDir= "+WLToolsIO.getMainCfgDir());

    // --- Now get the --<option name>=<option value> from the args
    HashMap<String, String> argsMap = new HashMap<>();

    for (String arg: args) {
      String[] parts = arg.split("=");
      argsMap.put(parts[0], parts[1]);
    }

    if (!argsMap.keySet().contains("--tool")) {
      throw new RuntimeException(mmi+"Must have the --tool=<prediction OR analysis> option defined !!");
    }

    final String tool= argsMap.get("--tool");

    if ( !tool.equals("prediction") && !tool.equals("analysis") ) {
      throw new RuntimeException(mmi+"Invalid tool -> "+tool+" !!, must be prediction OR analysis");
    }

    if (tool.equals("analysis")) {
      throw new RuntimeException(mmi+"Sorry! the analysis part is not ready to be used !!");
    }

    System.out.println(mmi+"Will use tool -> "+tool);

    //WLStationPred wlStationPred= null;

    if (tool.equals("prediction")) {

      System.out.println(mmi+"Doing WL predictions for a station or grid point.");

      final WLStationPred wlStationPred= new WLStationPred(argsMap); //.parseArgsOptions(argsMap);

      //System.out.println("WLTools main: debug System.exit(0)");
        //System.exit(0);

      wlStationPred.getAllPredictions();

      //wlStationPred.writeResults(IWLStationPred.OutputFormat.JSON)

      System.out.println(mmi+"Done with WL predictions at a station or grid point.");
    }

    System.out.println(mmi+"end");
  }
}
