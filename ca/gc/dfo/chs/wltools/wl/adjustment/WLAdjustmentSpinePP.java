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
  //protected List<HBCoords> regionBBox= new ArrayList<HBCoords>(2); //null;
    
  protected String nonAdjFMFInputDataInfo= null;

    //private Map<String, Double> twoNearestTGInfo= new HashMap<String, Double>(2);

  protected Map<String, Double> shipChannelLocsDistances= null;
    
  // --- The List<MeasurementCustom> where to store the adjusted long-term
  //     "forecasts" at the ship channel point locations being processed.
  //      -> OUTPUT
  protected Map<String, List<MeasurementCustom>> shipChannelLocsAdjForecast= null;
  //private MeasurementCustomBundle spineLocationAdjForecast= null;

  // --- To store The initial NS_TIDE WL predictions at the Spine target location.
  // INPUT ONLY, not used if the spineLocationNonAdjForecast= is used
  //private List<MeasurementCustom> spineLocationNSTPred= null;

  // --- To store the non-adjusted WL NS Tide WL pred (or FMF) data at the Spine
  //     ship channel point locations (INPUT ONLY)
  //private List<MeasurementCustom> spineLocationNonAdjData= null;
  protected Map<String, MeasurementCustomBundle> shipChannelLocsNonAdjData= null;

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

    // --- Now get the corresponding nearest ship channel points locations for those two TGs
    //     from their json config:
    final String tg0NearestSCLocId= this.locations.get(0).
      getNearestSpinePointId().split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR)[2]; //+ INonStationaryIO.LOCATION_TIDAL_CONSTS_FNAME_SUFFIX;
    
    final String tg1NearestSCLocId= this.locations.get(1).
      getNearestSpinePointId().split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR)[2]; // + INonStationaryIO.LOCATION_TIDAL_CONSTS_FNAME_SUFFIX;

    slog.info(mmi+"tg0NearestSCLocId="+tg0NearestSCLocId);
    slog.info(mmi+"tg1NearestSCLocId="+tg1NearestSCLocId);
    
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
     
    // --- Build the path of the main folder where we can find all tidal consts. files
    //     for all the ship channel point locations on disk.
    final String mainTCInputDir= tidalConstsInputInfoStrSplit[1];
	       
    slog.info(mmi+"mainTCInputDir="+mainTCInputDir);

    final String shipChannelPointLocsTCInputDir=
      WLToolsIO.getMainCfgDir() + mainTCInputDir + IWLToolsIO.SHIP_CHANNEL_POINTS_DEF_DIRNAME;

    slog.info(mmi+"shipChannelPointLocsTCInputDir="+shipChannelPointLocsTCInputDir);

    // --- Build the paths to find the tidal consts. files of the two ship channel point locations
    //     that are the nearests to the two TGs considered.
    final String tg0NearestSCLocTCFile= shipChannelPointLocsTCInputDir + File.separator +
      tg0NearestSCLocId + INonStationaryIO.LOCATION_TIDAL_CONSTS_FNAME_SUFFIX + IWLToolsIO.JSON_FEXT;
    
    final String tg1NearestSCLocTCFile= shipChannelPointLocsTCInputDir + File.separator +
      tg1NearestSCLocId + INonStationaryIO.LOCATION_TIDAL_CONSTS_FNAME_SUFFIX + IWLToolsIO.JSON_FEXT;

    slog.info(mmi+"tg0NearestSCLocTCFile="+tg0NearestSCLocTCFile);
    slog.info(mmi+"tg1NearestSCLocTCFile="+tg1NearestSCLocTCFile);

    // --- Get the HBCoords objects from the tidal consts. files of the two ship channel point locations
    final HBCoords tg0NearestSCLocHBCoords= this.getHBCoordsFromNSTCJsonFile(tg0NearestSCLocTCFile);
    final HBCoords tg1NearestSCLocHBCoords= this.getHBCoordsFromNSTCJsonFile(tg1NearestSCLocTCFile);

    final double tg0NearestSCLocLon= tg0NearestSCLocHBCoords.getLongitude();
    final double tg0NearestSCLocLat= tg0NearestSCLocHBCoords.getLatitude();
    final double tg1NearestSCLocLon= tg1NearestSCLocHBCoords.getLongitude();
    final double tg1NearestSCLocLat= tg1NearestSCLocHBCoords.getLatitude();    

    // --- Calculate the polar stereographic great circle half-distance in radians between the two ship channel point locations
    final double tgsNearestsLocsHalfDistRad= 0.5* Trigonometry.
      getDistanceInRadians(tg0NearestSCLocLon,tg0NearestSCLocLat,tg1NearestSCLocLon,tg1NearestSCLocLat);

    // --- Calulate the square of the half distance between the two ship channel point locations
    //     to use it later (it is in fact the radius of the circle that has its center at
    //     the mid-point between that are the nearests to the two TGs considered
    //final double tgsNearestsLocsHalfDistRadSquared= Math.pow(0.5 * tgsNearestsLocsDistRad, 2);
    
    // --- longitude of the mid-point between the two ship channel point locations
    final double tgsNearestsLocsCenterLon= 0.5 * (tg0NearestSCLocLon + tg1NearestSCLocLon);

    // --- latitude of the mid-point between the two ship channel point locations
    final double tgsNearestsLocsCenterLat= 0.5 * (tg0NearestSCLocLat + tg1NearestSCLocLat);

    slog.info(mmi+"tgsNearestsLocsCenterLon="+tgsNearestsLocsCenterLon);
    slog.info(mmi+"tgsNearestsLocsCenterLat="+tgsNearestsLocsCenterLat);

    // --- Now get the HBCoords of all the ship channel points locations that are in-between
    //     the two ship channel points locations that are the nearests to the two TGs considered.
    //     Verify at the same time if they are all inside the circle which center is located at the mid-point
    //     between the two ship channel points locations that are the nearests to the two TGs considered.
    //     The distance in radians between the mid-point and the in-between ship channel points locations
    //     should be smaller that the radius of this circle.
    
    final int tg0NearestSCLocIndex= Integer.
      parseInt(tg0NearestSCLocId.split(IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR)[1]);
    
    final int tg1NearestSCLocIndex= Integer.
      parseInt(tg1NearestSCLocId.split(IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR)[1]);

    slog.info(mmi+"tg0NearestSCLocIndex="+tg0NearestSCLocIndex);
    slog.info(mmi+"tg1NearestSCLocIndex="+tg1NearestSCLocIndex);

    // --- Need to have a HBCoords object reference for the
    //     spatial linear interpolation of FMF residuals.
    HBCoords tgNearestSCLocCoordsRef= tg0NearestSCLocHBCoords;
    
    int loopStartIndex= tg0NearestSCLocIndex + 1;
    int loopEndIndex=   tg1NearestSCLocIndex - 1;

    // --- DO NOT ASSUME that tg0NearestSCLocIndex is smaller than tg1NearestSCLocIndex!!
    if (tg0NearestSCLocIndex > tg1NearestSCLocIndex) {
	
      loopStartIndex= tg1NearestSCLocIndex + 1;
      loopEndIndex=  tg0NearestSCLocIndex - 1;

      // --- Also need to use the HBCoord object of the tg1 here
      //     for the tgNearestSCLocCoordsRef (convention of using
      //     the smallest ship channel point location index for it)
      tgNearestSCLocCoordsRef= tg1NearestSCLocHBCoords;
    }

    final double tgNearestSCLocLonRef= tgNearestSCLocCoordsRef.getLongitude();
    final double tgNearestSCLocLatRef= tgNearestSCLocCoordsRef.getLatitude();
    
    slog.info(mmi+"loopStartIndex="+loopStartIndex);
    slog.info(mmi+"loopEndIndex="+loopEndIndex);

    // --- Get the file name prefix string to use.
    final String scLocFNameShortPrefix= tg0NearestSCLocId.split(IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR)[0];

    this.shipChannelLocsDistances= new HashMap<String,Double>();
    
    // --- Get the HBCoords info of all the in-between ship channel points locations and also
    //     read the related prediction data for them.
    for (int idx= loopStartIndex; idx <= loopEndIndex; idx++) {

      final String scLocFNameLongPrefix= scLocFNameShortPrefix +
	IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + Integer.toString(idx);	

      final String scLocTCFile= shipChannelPointLocsTCInputDir + File.separator +
	scLocFNameLongPrefix + INonStationaryIO.LOCATION_TIDAL_CONSTS_FNAME_SUFFIX + IWLToolsIO.JSON_FEXT;	

      //slog.info(mmi+"scLocTCFile="+scLocTCFile);
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
      
      final HBCoords scLocHBCoords= this.getHBCoordsFromNSTCJsonFile(scLocTCFile);

      final double scLocHBLon= scLocHBCoords.getLongitude();
      final double scLocHBLat= scLocHBCoords.getLatitude();

      final double distanceFromMidPoint= Trigonometry.
	getDistanceInRadians(scLocHBLon, scLocHBLat, tgsNearestsLocsCenterLon, tgsNearestsLocsCenterLat);

      //slog.info(mmi+"distanceFromMidPoint="+distanceFromMidPoint+", tgsNearestsLocsHalfDistRad="+tgsNearestsLocsHalfDistRad);

      if (distanceFromMidPoint > tgsNearestsLocsHalfDistRad) {
	throw new RuntimeException(mmi+"distanceFromMidPoint > tgsNearestsLocsHalfDistRad for ship channell point location -> "+scLocFNameLongPrefix);
      }

      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);

      // --- Now calculate the distance from the HBCoords reference.
      final double distanceFromRef= Trigonometry.
	getDistanceInRadians(scLocHBLon, scLocHBLat, tgNearestSCLocLonRef, tgNearestSCLocLatRef);

      // --- Store this distanceFromRef for later usage.
      this.shipChannelLocsDistances.put(scLocFNameLongPrefix, distanceFromRef);

      // --- Now read the prediction data for this in-between ship channel point location.
      // final String 
      
    }
    
    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);        
 
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
