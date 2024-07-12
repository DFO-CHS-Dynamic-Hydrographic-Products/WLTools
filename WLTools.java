//import javax.json;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.HashMap;
import java.util.TimeZone;
import java.net.URISyntaxException;

// ---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ---
import ca.gc.dfo.chs.wltools.IWLTools;
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
//Not used on the gpsc clusters: import ca.gc.dfo.chs.util.spine.S104Dcf8ToAscii;
import ca.gc.dfo.chs.modeldata.ModelDataExtraction;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStage;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustment;
import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPred;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
//import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPredFactory;

/**
 * Comments please!
 */
final public class WLTools extends WLToolsIO {

  //static { System.loadLibrary("HDFql"); }

  // ---
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
      throw new RuntimeException(mmi+e);
    }

    mlog.info(mmi+"binDir="+binDir);

    // --- Now get the --<option name>=<option value> from the args
    HashMap<String, String> argsMap= new HashMap<String,String>();

    // ---
    for (final String arg: args) {

      if (!arg.contains("=")) {
	throw new RuntimeException(mmi+"Incorrect option arg -> "+arg+" we should have --<option name>=<option value> !!");
      }

      final String [] parts= arg.split("=");

      if (parts.length != 2 ) {
        throw new RuntimeException(mmi+"Incorrect option arg -> "+arg+" we should have --<option name>=<option value> !!");
      }

      if (!parts[0].startsWith("--")) {
	throw new RuntimeException(mmi+"Incorrect option arg -> "+arg+" All option args must start with the \"--\" prefix !");
      }

      mlog.debug(mmi+"parts[0]="+parts[0]+", parts[1]="+parts[1]);

      argsMap.put(parts[0], parts[1]);
    }

    // ---
    if (argsMap.containsKey("--mainCfgDirectory")) {

      WLToolsIO.setMainCfgDir(argsMap.get("--mainCfgDirectory"));

    } else {

      mlog.info(mmi+"Trying to define the path to the main config directory using the path of the main program -> "+binDir);

      WLToolsIO.setMainCfgDir(binDir+ "/../"+IWLToolsIO.PKG_CFG_MAIN_DIR);
    }

    // ---  Check if the main config directory exists before going further.
    if (!WLToolsIO.checkForFileExistence(WLToolsIO.getMainCfgDir())) {
      throw new RuntimeException(mmi+"main config directory -> "+WLToolsIO.getMainCfgDir()+" not found !!");
    }

    mlog.info(mmi+"mainCfgDir="+WLToolsIO.getMainCfgDir());

    //System.out.println(mmi+"Debug exit 0");
    //System.exit(0);

    // --- Check the mandatory --tool option
    if (!argsMap.keySet().contains("--tool")) {

      throw new RuntimeException(mmi+"Must have one of the --tool="+
                                 IWLTools.Box.prediction.name()+" OR --tool="+
                                 IWLTools.Box.adjustment.name()+" OR --tool="+
                                 IWLTools.Box.analysis.name()+" OR --tool="+
				 IWLTools.Box.IPPAdjToS104DCF8+" OR --tool "+
                                 IWLTools.Box.S104Dcf8ToAscii.name()+" OR --tool "+
				 IWLTools.Box.modelDataExtraction.name()+" option defined !!");

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

    if (!tool.equals(IWLTools.Box.IPPAdjToS104DCF8)) {

      if (!argsMap.keySet().contains("--outputDataFormat")) {
        throw new RuntimeException(mmi+"--outputDataFormat option must be used for this tool -> "+tool);
      }

      WLToolsIO.setOutputDataFormat(argsMap.get("--outputDataFormat"));

      mlog.info(mmi+"WLToolsIO.getOutputDataFormat()="+WLToolsIO.getOutputDataFormat());	  
    }

    //mlog.info(mmi+"debug System.exit 0");
    //System.exit(0);

    // ---
    if (tool.equals(IWLTools.Box.prediction.name())) {

      mlog.info(mmi+"Doing WL "+
                IWLTools.Box.prediction.name()+" for a station or grid point.");

      final WLStationPred wlStationPred= new WLStationPred(argsMap); //.parseArgsOptions(argsMap);

      //mlog.info((mmi+"debug System.exit(0)");
      //System.exit(0);

      wlStationPred.getAllPredictions();

      wlStationPred.writeIfNeeded(IWLToolsIO.Format.CHS_JSON); //.writeResults(IWLStationPred.OutputFormat.JSON)

      //wlStationPred.writeResults(IWLStationPred.OutputFormat.JSON)

      mlog.info(mmi+"Done with WL  "+
                IWLTools.Box.prediction.name()+" at a station or grid point.");
    }

    // --- WL adjustments tool
    if (tool.equals(IWLTools.Box.adjustment.name())) {

      mlog.info(mmi+"Doing WL forecast or prediction "+
                IWLTools.Box.adjustment.name()+" using the more recently validated CHS WLO TG data");

      final WLAdjustment wlAdjustAtLocation= new WLAdjustment(argsMap);

      // --- Check if we need to write all WL adj. data (input and results) on disk
      final String allAdjDataOutDir= writeAllData ? WLToolsIO.getOutputDirectory() : null;

      final List<MeasurementCustom> adjustedHWLPS=
        wlAdjustAtLocation.getAdjustment(allAdjDataOutDir); //.writeResult(finak string outFile); //

      // --- Write the WL adjustments results if adjustedHWLPS is not null
      if (adjustedHWLPS != null ) {
	  
        try {
          WLToolsIO.getOutputDataFormat();
        } catch (NullPointerException npe) {
          throw new RuntimeException(mmi+npe);
        }

        if (!WLToolsIO.getOutputDataFormat().equals(IWLToolsIO.Format.CHS_JSON.name())) {
          throw new RuntimeException(mmi+"Invalid output data format -> "+WLToolsIO.getOutputDataFormat()+" for the adjustment tool!");
        }

        mlog.info(mmi+"Writing this.locationAdjustedData results in folder -> "+WLToolsIO.getOutputDirectory());

        final String hfpLeadTimeStr=
          wlAdjustAtLocation.getFMFLeadTimeECCCOperStr() + IWLToolsIO.ISO8601_YYYYMMDD_SEP_CHAR;

        //mlog.info(mmi+"hfpLeadTimeStr="+hfpLeadTimeStr);
        //mlog.info(mmi+"Debug System.exit(0)");
        //System.exit(0);

        final String adjustedHWLPSOutFName= hfpLeadTimeStr +
          IWLAdjustmentIO.ADJ_HFP_ATTG_FNAME_PRFX + wlAdjustAtLocation.getLocationIdentity();

        mlog.info(mmi+"adjustedHWLPSOutFName="+adjustedHWLPSOutFName);

        // -- Write the adjusted WL forecast results data on disk using the WLToolsIO.getOutputDataFormat()
        //    output format.
        WLToolsIO.writeToOutputDir(adjustedHWLPS,
                                   IWLToolsIO.Format.valueOf(WLToolsIO.getOutputDataFormat()),
                                   adjustedHWLPSOutFName, null );
      } else {
	mlog.info(mmi+"Assuming here that all the results have already been written by the WLAdjustment wlAdjustAtLocation object"); 
      }
      
      //mlog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
    }

    // --- Conversion of adj. SpineIPP results to S104DCF8 file tool.
    if (tool.equals(IWLTools.Box.IPPAdjToS104DCF8.name())) {

      mlog.info(mmi+"Using the conversion of SpineIPP results to S104DCF8 tool");

      WLToolsIO.ippAdjToS104DCF8(argsMap);
      
      //mlog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
    }
    
    // --- S104Dcf8ToAscii: not used on the gpsc clusters.
    // if (tool.equals(IWLTools.Box.S104Dcf8ToAscii.name())) {
    //   mlog.info(mmi+"Generating SPINE Ascii files from S-104 Dcf8");
    //   if (!argsMap.keySet().contains("--outputDirectory")) {
    //     throw new RuntimeException(mmi+"Must have the --outputDirectory=<path to the output dir.> defined in the args.!");
    //   }
    //     final String outputDirArg= argsMap.get("--outputDirectory");
    //   if (!argsMap.keySet().contains("--h5Path")) {
    //     throw new RuntimeException(mmi+"Must have the --h5Path=<path to S-104 file.> defined in the args.!");
    //   }
    //   final String h5PathArg= argsMap.get("--h5Path");
    //   if (!argsMap.keySet().contains("--time")) {
    //     throw new RuntimeException(mmi+"Must have the --time=<ISO time> defined in the args.!");
    //   }
    //   final String timeArg= argsMap.get("--time");
    //   if (!argsMap.keySet().contains("--type")) {
    //     throw new RuntimeException(mmi+"Must have file type --type=UU,30,Q2,Q3,Q4 defined in the args.!");
    //   }
    //   final String typeArg= argsMap.get("--type");
    //   S104Dcf8ToAscii.runConversion(timeArg, outputDirArg, h5PathArg, typeArg);
    //  }

     if (tool.equals(IWLTools.Box.modelDataExtraction.name())) {

       mlog.info(mmi+"Starting tool "+IWLTools.Box.modelDataExtraction.name());

       final ModelDataExtraction modelDataExtraction= new ModelDataExtraction(argsMap);

       //modelDataExtraction.run();
       
       mlog.info(mmi+"Debug System.exit(0)");
       System.exit(0);
     }
    
     mlog.info(mmi+"end");
  }
}
