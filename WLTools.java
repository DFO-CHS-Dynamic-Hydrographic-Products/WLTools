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
import ca.gc.dfo.chs.wltools.IWLTools;
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStage;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustment;
import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPred;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
//import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPredFactory;

/**
 * Comments please!
 */
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

      binDir= new File(WLTools.class.
        getProtectionDomain().getCodeSource().getLocation().toURI());

    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    System.out.println(mmi+"binDir="+binDir);

    // --- TODO: add an option arg. to define the mainCfgDir
    WLToolsIO.setMainCfgDir(binDir+ "/../"+IWLToolsIO.PKG_CFG_MAIN_DIR);

    System.out.println(mmi+"mainCfgDir= "+WLToolsIO.getMainCfgDir());

    // --- Now get the --<option name>=<option value> from the args
    HashMap<String, String> argsMap = new HashMap<String,String>();

    for (final String arg: args) {
      String[] parts = arg.split("=");
      argsMap.put(parts[0], parts[1]);
    }

    //final [] String toolsIds= {  };

    if (!argsMap.keySet().contains("--tool")) {

      throw new RuntimeException(mmi+"Must have one of the --tool="+
                                 IWLTools.Box.prediction.name()+" OR --tool="+
                                 IWLTools.Box.adjustment.name()+" OR --tool="+
                                 IWLTools.Box.analysis.name()+" OR --tool="+
                                 IWLTools.Box.merge.name()+" option defined !!");
    }

    final String tool= argsMap.get("--tool");

    // --- Validate the tool (Check if we have it in our IWLTools.Box enum)
    if (!IWLTools.BoxContent.contains(tool)) {

      throw new RuntimeException(mmi+"Invalid tool -> "+tool+
                                 " !!, must be one of "+IWLTools.BoxContent.toString());
    }

    //if (tool.equals("analysis")) {
    if (tool.equals(IWLTools.Box.analysis.name())) {

      throw new RuntimeException(mmi+"Sorry! the "+
                                 IWLTools.Box.analysis.name()+" part is not ready to be used !!");
    }

    System.out.println(mmi+"Will use tool -> "+tool);

    //WLStationPred wlStationPred= null;

    //if (tool.equals("prediction")) {
    if (tool.equals(IWLTools.Box.prediction.name())) {

      System.out.println(mmi+"Doing WL "+
                         IWLTools.Box.prediction.name()+" for a station or grid point.");

      final WLStationPred wlStationPred= new WLStationPred(argsMap); //.parseArgsOptions(argsMap);

      //System.out.println("WLTools main: debug System.exit(0)");
        //System.exit(0);

      wlStationPred.getAllPredictions();

      wlStationPred.writeIfNeeded(IWLStationPredIO.OutputFormats.JSON); //.writeResults(IWLStationPred.OutputFormat.JSON)

      //wlStationPred.writeResults(IWLStationPred.OutputFormat.JSON)

      System.out.println(mmi+"Done with WL  "+
                         IWLTools.Box.prediction.name()+" at a station or grid point.");
    }

    //if (tool.equals("adjustment")) {
    if (tool.equals(IWLTools.Box.adjustment.name())) {

       System.out.println(mmi+"Doing WL forecast or prediction"+
                          IWLTools.Box.adjustment.name()+" using the more recently validated CHS WLO TG data");

       final WLAdjustment wlAdjust= new WLAdjustment(argsMap);

       wlAdjust.getAdjustment(); //.writeResult(finak string outFile); //

       //System.out.println(mmi+"Debug System.exit(0)");
       //System.exit(0);

     }

    System.out.println(mmi+"end");
  }
}
