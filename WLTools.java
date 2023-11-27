//import javax.json;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.TimeZone;
//import java.util.Calendar;
import java.net.URISyntaxException;
//import java.util.GregorianCalendar;

// ---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.util.spine.S104Dcf8ToAscii;
// ---
import ca.gc.dfo.chs.wltools.IWLTools;
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStage;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
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

    //System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "error");
    //System.setProperty(org.slf4j.impl.SimpleLogger.defaultLogLevel,"ERROR");

    final String mmi= "WLTools main: ";

    final Logger mlog= LoggerFactory.getLogger(mmi);

    mlog.info(mmi+"start");

    //--- tmp File object to be used to automagically determine the directory
    //    where the main program class is located.
    File binDir= null;

    try {

      binDir= new File(WLTools.class.
        getProtectionDomain().getCodeSource().getLocation().toURI());

    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }

    mlog.info(mmi+"binDir="+binDir);

    // --- TODO: add an option arg. to define the mainCfgDir
    WLToolsIO.setMainCfgDir(binDir+ "/../"+IWLToolsIO.PKG_CFG_MAIN_DIR);

    mlog.info(mmi+"mainCfgDir= "+WLToolsIO.getMainCfgDir());

    // --- Now get the --<option name>=<option value> from the args
    HashMap<String, String> argsMap= new HashMap<String,String>();

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

    mlog.info(mmi+"Will use tool -> "+tool);

    //WLStationPred wlStationPred= null

    if (!argsMap.keySet().contains("--outputDirectory")) {
      throw new RuntimeException(mmi+"Must have the --outputDirectory=<path to the output dir.> defined in the args.!");
    }

    WLToolsIO.setOutputDirectory(argsMap.get("--outputDirectory"));

    mlog.info(mmi+"WLToolsIO.getOutputDirectory()="+WLToolsIO.getOutputDirectory());

    boolean writeAllData= false;

    if (argsMap.keySet().contains("--writeAllData")) {
      writeAllData= argsMap.get("--writeAllData").equals("true") ? true : false;
    }

    mlog.info(mmi+"writeAllData="+writeAllData);
    //System.out.println(mmi+"Debug exit 0");
    //System.exit(0);

    //if (tool.equals("prediction")) {
    if (tool.equals(IWLTools.Box.prediction.name())) {

      mlog.info(mmi+"Doing WL "+
                IWLTools.Box.prediction.name()+" for a station or grid point.");

      final WLStationPred wlStationPred= new WLStationPred(argsMap); //.parseArgsOptions(argsMap);

      //System.out.println("WLTools main: debug System.exit(0)");
        //System.exit(0);

      wlStationPred.getAllPredictions();

      wlStationPred.writeIfNeeded(IWLStationPredIO.Format.CHS_JSON); //.writeResults(IWLStationPred.OutputFormat.JSON)

      //wlStationPred.writeResults(IWLStationPred.OutputFormat.JSON)

      mlog.info(mmi+"Done with WL  "+
                IWLTools.Box.prediction.name()+" at a station or grid point.");
    }

    //if (tool.equals("adjustment")) {
    if (tool.equals(IWLTools.Box.adjustment.name())) {

      mlog.info(mmi+"Doing WL forecast or prediction "+
                IWLTools.Box.adjustment.name()+" using the more recently validated CHS WLO TG data");

      final WLAdjustment wlAdjust= new WLAdjustment(argsMap);

      //List<MeasurementCustom> adjustedWLForecast= null;

      // --- Check if we need to write all WL data (input and results) on disk
      final String outputDirArg= writeAllData ? WLToolsIO.getOutputDirectory() : null;

      final List<MeasurementCustom> adjustedWLForecast= wlAdjust.getAdjustment(outputDirArg); //.writeResult(finak string outFile); //

       // --- Only write the adjusted WL forecast data on disk
       //WLToolsIO.write(adjustedWLForecast,WLToolsIO.getOutputFormat,WLToolsIO.getOutputDirectory())

       //System.out.println(mmi+"Debug System.exit(0)");
       //System.exit(0);

     }

        // S104Dcf8ToAscii
    if (tool.equals(IWLTools.Box.S104Dcf8ToAscii.name())) {

      mlog.info(mmi+"Generating SPINE Ascii files from S-104 Dcf8");

      if (!argsMap.keySet().contains("--outputDirectory")) {
        throw new RuntimeException(mmi+"Must have the --outputDirectory=<path to the output dir.> defined in the args.!");
      }
        final String outputDirArg= argsMap.get("--outputDirectory");

      if (!argsMap.keySet().contains("--h5Path")) {
        throw new RuntimeException(mmi+"Must have the --h5Path=<path to S-104 file.> defined in the args.!");
      }
      final String h5PathArg= argsMap.get("--h5Path");


      if (!argsMap.keySet().contains("--time")) {
        throw new RuntimeException(mmi+"Must have the --time=<ISO time> defined in the args.!");
      }
      final String timeArg= argsMap.get("--time");


      S104Dcf8ToAscii.runConversion(timeArg, outputDirArg, h5PathArg);

     }

    mlog.info(mmi+"end");
  }
}
