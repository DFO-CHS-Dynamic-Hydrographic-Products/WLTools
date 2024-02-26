package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;
import java.util.ArrayList;
import java.nio.file.Files;
import java.util.Collection;
import java.util.NavigableSet;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
//import java.util.stream.Stream;
//import java.nio.file.DirectoryStream;

//import java.awt.geom.Point2D; //.Double;
//import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

// ---
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.util.IHBGeom;
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.wl.IWLLocation;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.wl.TideGaugeConfig;
import ca.gc.dfo.chs.wltools.wl.ITideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
//import ca.gc.dfo.chs.wltools.nontidal.stage.StageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
import ca.gc.dfo.chs.wltools.tidal.nonstationary.INonStationaryIO;

/**
 * Comments please!
 */
abstract public class WLAdjustmentSpinePP extends WLAdjustmentType {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentSpinePP: ";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // --- Moved to WLSCReachIntrpUnit class  
  // protected int scLoopEndIndex= -1;
  // protected int scLoopStartIndex= -1;
  // protected int tg0NearestSCLocIndex= -1;
  // protected int tg1NearestSCLocIndex= -1;
  // protected String lowerSideScLocStrId= null;
  // protected String upperSideScLocStrId= null;
  // protected String lowerSideScLocTGId= null;
  // protected String upperSideScLocTGId= null;
  // protected String scLocFNameCommonPrefix= null;
  // protected double tgsNearestsLocsDistRad= -1.0; 
  // protected Map<String, Double> scLocsDistances= null;

  protected JsonObject mainJsonTGInfoMapObj= null;

  protected String shipChannelPointLocsTCInputDir= null;
    
  // -- Not used for now
  //protected String nonAdjFMFInputDataInfo= null;
    
  // --- The List<MeasurementCustom> where to store the adjusted long-term
  //     "foreDictions" or "predCasts" at the ship channel point locations being processed (-> OUTPUT)
  //
  //     NOTES:
  //        1. Map<String,> size is determined by the number of in-between ship channel points locations
  //           that we have for a given pair of surrounding tide gauges.
  //        2. List<MeasurementCustom> size is determined by the duration in the future and the time increment used.
  protected Map<String, List<MeasurementCustom>> scLocsAdjLTFP= null;

  // --- To store the model adjusted forecast (medium term 48H) at the ship channel points
  //     locations that are the nearest to the tide gauges locations used for the adjustments (INPUT ONLY)
  protected Map<String, MeasurementCustomBundle> tgsNearestSCLocsAdjFMF= null;
    //  tgsNearestSCLocsAdjFMF= new HashMap<String, MeasurementCustomBundle>(2); //null;
    
  //private IWLAdjustment.Type adjType= null;

  /**
   * Comments please!
   */
  public WLAdjustmentSpinePP() {
    super();

    //this.wlOriginalData=
    //  this.wlAdjustedData= null;
  }

  // ---
  public WLAdjustmentSpinePP(final IWLAdjustment.Type spinePPType, final HashMap<String,String> argsMap) {
      
    super(spinePPType,argsMap);

    final String mmi= "WLAdjustmentSpinePP main constructor ";

    slog.info(mmi+"start");
    
    if (!argsMap.keySet().contains("--tideGaugeLocationsDefFileName")) {
      throw new RuntimeException(mmi+
         "Must have the --tideGaugeLocationsDefFileName=<tide gauges definition file name> defined in argsMap");
    }

    final String tideGaugeLocationsDefFileName= argsMap.get("--tideGaugeLocationsDefFileName");

    // --- Get the path to the tide gauges info file on disk.
    final String tideGaugesInfoFile= WLToolsIO.
      getTideGaugeInfoFilePath(tideGaugeLocationsDefFileName);

    slog.info(mmi+"tideGaugesInfoFile="+tideGaugesInfoFile); 	
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);    
	
    // --- FileInputStream Object for reading the CHS tide gauges info file.
    FileInputStream jsonFileInputStream= null;

    try {
      jsonFileInputStream= new FileInputStream(tideGaugesInfoFile);

    } catch (FileNotFoundException e) {
      throw new RuntimeException(mmi+e);
    }

    // --- JSON reader for the CHS tide gauges info file.
    //final JsonObject mainJsonTGInfoMapObj= Json.
    this.mainJsonTGInfoMapObj= Json.
      createReader(jsonFileInputStream).readObject();

    // --- We can close the tide gauges info Json file now
    try {
      jsonFileInputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(mmi+e);
    }
    
    //slog.info(mmi+"debug exit 0");
    //System.exit(0);
    
    // --- Now need to consider where to find the ship channel points locations
    //     tidal consts. files in order to get their EPSG:4326 coordinates.
    if (!argsMap.keySet().contains("--tidalConstsInputInfo")) {
      throw new RuntimeException(mmi+
         "Must have the --tidalConstsInputInfo=<> defined in argsMap");
    }

    final String tidalConstsInputInfo= argsMap.get("--tidalConstsInputInfo");

    final String [] tidalConstsInputInfoStrSplit=
      tidalConstsInputInfo.split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

    if (tidalConstsInputInfoStrSplit.length != 2 ) {
      throw new RuntimeException(mmi+"ERROR: tidalConstsInputInfoStrSplit.length != 2 !!!");
    }

    final String checkTidalConstInputFileFmt= tidalConstsInputInfoStrSplit[0];
    //tidalConstsInputInfo.split(IWLLocation.ID_SPLIT_CHAR)[0];

    if (!checkTidalConstInputFileFmt.
            equals(ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON.name())) {

      throw new RuntimeException(mmi+"Only the:"+
                                 ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON.name()+
                                 " tidal prediction input file format allowed for now!!");
    }
     
    // --- Build the path of the main folder where we can find all tidal conststituents files
    //     for all the ship channel point locations on disk.
    final String mainTCInputDir= tidalConstsInputInfoStrSplit[1];
	       
    slog.info(mmi+"mainTCInputDir="+mainTCInputDir);

    // --- Define the path (as a String) of the main folder where we can find all tidal conststituents files
    //     for all the ship channel point locations on disk.
    this.shipChannelPointLocsTCInputDir= WLToolsIO.getMainCfgDir() + 	
      File.separator + mainTCInputDir + IWLToolsIO.SHIP_CHANNEL_POINTS_DEF_DIRNAME;

    slog.info(mmi+"this.shipChannelPointLocsTCInputDir="+this.shipChannelPointLocsTCInputDir);

    slog.info(mmi+"end");
    
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);        
 
  } // --- main constructor
    
  // ///**
  // // * Comments please.
  // // */
  // final public List<MeasurementCustom> getAdjustment(final String outputDirectory) {

  //   final String mmi= "getAdjustment: ";

  //   slog.info(mmi+"start");

  //   slog.info(mmi+"end");

  //   slog.info(mmi+"Debug System.exit(0)");
  //   System.exit(0);

  //   return this.locationAdjustedData; //adjustmentRet;
  // }
    
} // --- class WLAdjustmentSpinePP
