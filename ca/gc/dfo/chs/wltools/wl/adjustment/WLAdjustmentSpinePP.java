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
//import java.util.stream.Stream;
import java.nio.file.DirectoryStream;
//import java.nio.file.PathMatcher;
//import java.nio.file.FileSystems;

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

  // --- Info about the TGs that define the range of the ship channel
  //     point locations that are in-between those TGs
  //protected List<WLLocation> tgNeighLocations= null;

  // --- To store the considered region bounding box
  //     EPSG:4326 coordinates (South-West corner at index 0
  //     and North-East corber at index 1). The
  //     region bounding box is built with the
  //     smallest (lon,lat) coordinates for the SW
  //     corner and the largest (lon,lat) coordinates
  //     for the North-East corner. This is normally
  //     used only by the SpineIPP and SpineFPP classes.
  protected List<HBCoords> regionBBox= new ArrayList<HBCoords>(2); //null;
    
  protected String nonAdjFMFInputDataInfo= null;

    //private Map<String, Double> twoNearestTGInfo= new HashMap<String, Double>(2);

  // --- The List<MeasurementCustom> where to store the adjusted long-term
  //     "forecasts" at the Spine ship channel point locations being processed.
  protected Map<String, List<MeasurementCustom>> spineLocationAdjForecast= null;
  //private MeasurementCustomBundle spineLocationAdjForecast= null;

  // --- To store The initial NS_TIDE WL predictions at the Spine target location.
  // INPUT ONLY, not used if the spineLocationNonAdjForecast= is used
  //private List<MeasurementCustom> spineLocationNSTPred= null;

  // --- To store the non-adjusted WL NS Tide WL pred (or FMF) data at the Spine
  //     ship channel point locations (INPUT ONLY)
  //private List<MeasurementCustom> spineLocationNonAdjData= null;
  protected Map<String, MeasurementCustomBundle> spineLocationNonAdjData= null;

  // --- To store the NS_TIDE WL non-ajusted predictions at the Spine locations that are
  //     the nearest to the nearest tide gauges locations
  //     INPUT ONLY
  //private Map<String, List<MeasurementCustom>> tgsNearestSpineLocationsNonAdjData= null;
  protected Map<String, MeasurementCustomBundle> tgsNearestSpineLocationsNonAdjData= null;

  // --- To store the model adjusted forecast at the spine locations that are the
  //     nearest to the tide gauges locations used for the adjustments.
  //     INPUT ONLY
  //private Map<String, List<MeasurementCustom>> tgsNearestSpineLocationsAdjForecast= null;
  protected Map<String, MeasurementCustomBundle> tgsNearestSpineLocationsAdjForecast= null;

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

    final String mmi=
      "WLAdjustmentSpinePP(final WLAdjustment.Type spinePPType , final Map<String,String> argsMap) constructor ";

    try {
      this.locations.size();
    } catch (NullPointerException npe){
      throw new RuntimeException(mmi+"this.locations cannot be null here !");
    }

    if (this.locations.size() != 2) {
      throw new RuntimeException(mmi+"this.location.size() != 2 !!");
    }
    
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
    final JsonObject mainJsonTGInfoMapObj= Json.
      createReader(jsonFileInputStream).readObject();

    // --- We can close the tide gauges info Json file now
    try {
      jsonFileInputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(mmi+e);
    }

    // --- Set the configurations of the two tide gauges that
    //     defines the ship channel points locations range where
    //     we need to linearly interpolate the residuals between
    //     the adjusted FMF data and the non-adjusted WL data (Which
    //     could either be predictions OR non-adjusted FMF)
    //     TODO: Need to possibly consider more than two TGs
    //     at some point so we would then have to iterate on
    //     the number of TGs wanted and not assume that we
    //     have only two TGs to use here.
    this.locations.get(0).
      setConfig(mainJsonTGInfoMapObj.getJsonObject(this.locations.get(0).getIdentity()));

    this.locations.get(1).
      setConfig(mainJsonTGInfoMapObj.getJsonObject(this.locations.get(1).getIdentity()));

    if (!argsMap.keySet().contains("--regionTGsBBoxInfo")) {
      throw new RuntimeException(mmi+
         "Must have the --regionTGsBBoxInfo=<TG0Id:TG1Id> defined in argsMap");
    }

    // --- Now deal with the region bounding box which includes all the tide gauges
    //     and all the ship channel points locations needed for the Spine WL adjustments. 
    final String regionTGsBBoxInfo= argsMap.get("--regionTGsBBoxInfo");

    slog.info(mmi+"regionTGsBBoxInfo="+regionTGsBBoxInfo);

    final String [] regionTGsBBoxInfoSplit=
      regionTGsBBoxInfo.split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

    if (regionTGsBBoxInfoSplit.length != 2) {
      throw new RuntimeException(mmi+"regionTGsBBoxInfoSplit must have length of 2 here!");
    }

    // ---
    final TideGaugeConfig bBoxTideGauge0= new TideGaugeConfig(regionTGsBBoxInfoSplit[0]);
    final TideGaugeConfig bBoxTideGauge1= new TideGaugeConfig(regionTGsBBoxInfoSplit[1]);

    bBoxTideGauge0.setConfig(mainJsonTGInfoMapObj.getJsonObject(bBoxTideGauge0.getIdentity()));
    bBoxTideGauge1.setConfig(mainJsonTGInfoMapObj.getJsonObject(bBoxTideGauge1.getIdentity()));

    final double bBoxTideGauge0Lon= bBoxTideGauge0.getLongitude();
    final double bBoxTideGauge1Lon= bBoxTideGauge1.getLongitude();
    final double bBoxTideGauge0Lat= bBoxTideGauge0.getLatitude();
    final double bBoxTideGauge1Lat= bBoxTideGauge1.getLatitude();    
    
    final double minLon= (bBoxTideGauge0Lon < bBoxTideGauge1Lon) ? bBoxTideGauge0Lon: bBoxTideGauge1Lon;
    final double maxLon= (bBoxTideGauge0Lon > bBoxTideGauge1Lon) ? bBoxTideGauge0Lon: bBoxTideGauge1Lon;
    final double minLat= (bBoxTideGauge0Lat < bBoxTideGauge1Lat) ? bBoxTideGauge0Lat: bBoxTideGauge1Lat;
    final double maxLat= (bBoxTideGauge0Lat > bBoxTideGauge1Lat) ? bBoxTideGauge0Lat: bBoxTideGauge1Lat;  

    //slog.info(mmi+"minLon="+minLon);
    //slog.info(mmi+"maxLon="+maxLon);
    //slog.info(mmi+"minLat="+minLat);
    //slog.info(mmi+"maxLat="+maxLat);

    // --- Set the this.regionBBox HBCoords objects
    this.regionBBox.add(0, new HBCoords(minLon,minLat));
    this.regionBBox.add(1, new HBCoords(maxLon,maxLat));
    
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
    
    if (!argsMap.keySet().contains("--tidalConstsInputInfo")) {
      throw new RuntimeException(mmi+
         "Must have the --tidalConstsInputInfo=<tidal consts. type:model name from which the tidal consts where produced with the NS_TIDE analysis> defined in argsMap");
    }

    final String tidalConstsInputInfo= argsMap.get("--tidalConstsInputInfo");

    final String [] tidalConstsInputInfoStrSplit=
      tidalConstsInputInfo.split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

    if (tidalConstsInputInfoStrSplit.length != 3 ) {
      throw new RuntimeException(mmi+"ERROR: tidalConstsInputInfoStrSplit.length != 3 !!!");
    }

    final String checkTidalConstInputFileFmt= tidalConstsInputInfoStrSplit[0];
    //tidalConstsInputInfo.split(IWLLocation.ID_SPLIT_CHAR)[0];

    if (!checkTidalConstInputFileFmt.
            equals(ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON.name())) {

      throw new RuntimeException(mmi+"Only the:"+
                                 ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON.name()+
                                 " tidal prediction input file format allowed for now!!");
    }

    // --- Extract the relevant substrings that will be used to find the tidal consts.
    //     files for the ship channel point locations on disk from the tidalConstsInputInfoStrSplit array
    final String tidalConstsTypeId= tidalConstsInputInfoStrSplit[1];
    final String tidalConstsTypeModelId= tidalConstsInputInfoStrSplit[2];

    slog.info(mmi+"tidalConstsTypeId="+tidalConstsTypeId);
    slog.info(mmi+"tidalConstsTypeModelId="+tidalConstsTypeModelId);
     
    // --- Build the path of the main folder where we can find all tidal consts. files
    //       for all the ship channel point locations on disk.
    final String spineLocationsTCInputFolder= WLToolsIO.
      getLocationNSTFHAFilePath(tidalConstsTypeId, tidalConstsTypeModelId, null); // this.locationIdInfo);

    slog.info(mmi+"spineLocationsTCInputFolder="+spineLocationsTCInputFolder);

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);        

    //final HBCoords spineLocationHBCoord= HBCoords.
    //  getFromCHSJSONTCFile(spineLocationTCInputFile);

    //slog.info(mmi+"spineLocationHBCoord lon="+spineLocationHBCoord.getLongitude());
    //slog.info(mmi+"spineLocationHBCoord lat="+spineLocationHBCoord.getLatitude());

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    //// --- Now find the two nearest CHS tide gauges from this WDS grid point location
    //final String spineTideGaugesInfoFile= WLToolsIO.
    //  getTideGaugeInfoFilePath(tideGaugeLocationsDefFileName);

      //WLToolsIO.getMainCfgDir() + File.separator +
      //ITideGaugeConfig.INFO_FOLDER_NAME + File.separator + tideGaugeDefFileName ;

    //slog.info(mmi+"spineTideGaugesInfoFile="+spineTideGaugesInfoFile);
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
