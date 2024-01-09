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

  // --- Info about the TGs that define the range of the ship channel
  //     point locations that are in-between those TGs
  //protected List<WLLocation> tgNeighLocations= null;

  protected int scLoopEndIndex= -1;
  protected int scLoopStartIndex= -1;
    
  protected int tg0NearestSCLocIndex= -1;
  protected int tg1NearestSCLocIndex= -1;

  protected String lowerSideScLocStrId= null;
  protected String upperSideScLocStrId= null;

  protected String lowerSideScLocTGId= null;
  protected String upperSideScLocTGId= null;

  protected String scLocFNameCommonPrefix= null;

  protected double tgsNearestsLocsDistRad= -1.0; 
    
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

  protected Map<String, Double> scLocsDistances= null;
    
  // --- The List<MeasurementCustom> where to store the adjusted long-term
  //     "foreDictions" or "predCasts" at the ship channel point locations being processed (-> OUTPUT)
  //
  //     NOTES:
  //        1. Map<String,> size is determined by the number of in-between ship channel points locations
  //           that we have for a given pair of surrounding tide gauges.
  //        2. List<MeasurementCustom> size is determined by the duration in the future and the time increment used.
  protected Map<String, List<MeasurementCustom>> scLocsAdjLTFP= null;

  // --- To store the model adjusted forecast (medium term 48H) at the two ship channel points
  //     locations that are the nearest to the tide gauges locations used for the adjustments (INPUT ONLY)
  protected Map<String, MeasurementCustomBundle>
    tgsNearestSCLocsAdjFMF= new HashMap<String, MeasurementCustomBundle>(2); //null;

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

    // --- Calculate the polar stereographic great circle distance in radians between the
    //     two ship channel point locations that are the nearests to the two TGs considered.
    this.tgsNearestsLocsDistRad= Trigonometry.
      getDistanceInRadians(tg0NearestSCLocLon,tg0NearestSCLocLat,tg1NearestSCLocLon,tg1NearestSCLocLat);
    
    // --- Need the polar stereographic great circle half-distance in radians between the
    //     two ship channel point locations that are the nearests to the two TGs considered.
    final double tgsNearestsLocsHalfDistRad= 0.5 * this.tgsNearestsLocsDistRad;

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
    //     MUST be smaller that the radius of this circle.
    
    this.tg0NearestSCLocIndex= Integer.
      parseInt(tg0NearestSCLocId.split(IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR)[1]);
    
    this.tg1NearestSCLocIndex= Integer.
      parseInt(tg1NearestSCLocId.split(IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR)[1]);

    if (this.tg1NearestSCLocIndex == 0) {
      throw new RuntimeException(mmi+"this.tg1NearestSCLocIndex cannot be 0 here!!");
    }
    
    if (this.tg1NearestSCLocIndex == this.tg0NearestSCLocIndex) {
      throw new RuntimeException(mmi+"this.tg1NearestSCLocIndex and this.tg0NearestSCLocIndex cannot be the same here!!");
    }
   
    slog.info(mmi+"this.tg0NearestSCLocIndex="+this.tg0NearestSCLocIndex);
    slog.info(mmi+"this.tg1NearestSCLocIndex="+this.tg1NearestSCLocIndex);

    // --- Need to have a HBCoords object reference for the
    //     spatial linear interpolation of FMF residuals.
    HBCoords tgNearestSCLocCoordsRef= tg0NearestSCLocHBCoords;

    // --- Case where this.tg0NearestSCLocIndex < this.tg1NearestSCLocIndex
    this.scLoopStartIndex= this.tg0NearestSCLocIndex + 1;
    this.scLoopEndIndex=   this.tg1NearestSCLocIndex - 1;

    this.lowerSideScLocTGId= this.locations.get(0).getIdentity();
    this.upperSideScLocTGId= this.locations.get(1).getIdentity();

    // --- DO NOT ASSUME HERE that this.tg0NearestSCLocIndex is always smaller than this.tg1NearestSCLocIndex!!
    if (this.tg0NearestSCLocIndex > this.tg1NearestSCLocIndex) {
	
      this.scLoopStartIndex= this.tg1NearestSCLocIndex + 1;
      this.scLoopEndIndex=   this.tg0NearestSCLocIndex - 1;

      this.lowerSideScLocTGId= this.locations.get(1).getIdentity();
      this.upperSideScLocTGId= this.locations.get(0).getIdentity();      

      // --- Also need to use the HBCoord object of the tg1 here
      //     for the tgNearestSCLocCoordsRef (convention of using
      //     the smallest ship channel point location index for it)
      tgNearestSCLocCoordsRef= tg1NearestSCLocHBCoords;
    }

    final double tgNearestSCLocLonRef= tgNearestSCLocCoordsRef.getLongitude();
    final double tgNearestSCLocLatRef= tgNearestSCLocCoordsRef.getLatitude();
    
    slog.info(mmi+"this.scLoopStartIndex="+this.scLoopStartIndex);
    slog.info(mmi+"this.scLoopEndIndex="+this.scLoopEndIndex);
    
    slog.info(mmi+"this.lowerSideScLocTGId="+this.lowerSideScLocTGId);
    slog.info(mmi+"this.upperSideScLocTGId="+this.upperSideScLocTGId);
    
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
    
    // --- Get the common file name prefix string to use for the ship channel points locations
    this.scLocFNameCommonPrefix= tg0NearestSCLocId.split(IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR)[0];

    this.scLocsDistances= new HashMap<String,Double>();

    // --- Get the HBCoords info of all the in-between ship channel points locations and also
    //     read the related prediction data for them.
    for (int idx= this.scLoopStartIndex; idx <= this.scLoopEndIndex; idx++) {

      // --- Specific file name prefix string to use for the ship channel point location
      //     being processed
      final String scLocFNameSpecPrefix= this.scLocFNameCommonPrefix +
	IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + Integer.toString(idx);	

      final String scLocTCFile= shipChannelPointLocsTCInputDir + File.separator +
	scLocFNameSpecPrefix + INonStationaryIO.LOCATION_TIDAL_CONSTS_FNAME_SUFFIX + IWLToolsIO.JSON_FEXT;	

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
	throw new RuntimeException(mmi+"distanceFromMidPoint > tgsNearestsLocsHalfDistRad for ship channell point location -> "+scLocFNameSpecPrefix);
      }

      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);

      // --- Now calculate the distance from the HBCoords reference.
      final double distanceFromRef= Trigonometry.
	getDistanceInRadians(scLocHBLon, scLocHBLat, tgNearestSCLocLonRef, tgNearestSCLocLatRef);

      // --- Store this distanceFromRef for later usage.
      this.scLocsDistances.put(scLocFNameSpecPrefix, distanceFromRef);

      //// --- Now read the prediction data for this in-between ship channel point location.
      //final String fileGlobExpr=  
      //final PathMatcher fileMatch= FileSystems.getDefault().getPathMatcher( "glob:"+)
    }

    // --- Lower side ship channel location: subtract 1 from scLoopStartIndex to build its proper str id
    this.lowerSideScLocStrId= this.scLocFNameCommonPrefix +
      IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + Integer.toString(this.scLoopStartIndex-1);    

    // --- upper side ship channel location: add 1 from scLoopEndIndex to build its proper str id
    this.upperSideScLocStrId= this.scLocFNameCommonPrefix +
      IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + Integer.toString(this.scLoopEndIndex+1);    

    // --- Now read the adjusted FMF data at the two ship channel locations
    //     that are the nearest to the two tide gauges being processed.
    if (!argsMap.keySet().contains("--adjFMFAtTGSInputDataInfo")) {
      throw new RuntimeException(mmi+
         "Must have the --adjFMFAtTGSInputDataInfo=<Folder where to find all the adj. FMF at tide gauges> defined in argsMap");
    }

    // --- Now read the adjusted FMF WL data at the two ship channel points locations
    //     that are the nearest to the two tide gauges that are now processed.
    final String adjFMFAtTGSInputDataInfo= argsMap.get("--adjFMFAtTGSInputDataInfo");

    final String [] adjFMFAtTGSInputDataInfoStrSplit=
      adjFMFAtTGSInputDataInfo.split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

    if (adjFMFAtTGSInputDataInfoStrSplit.length != 2 ) {
      throw new RuntimeException(mmi+"ERROR: adjFMFAtTGSInputDataInfoStrSplit.length != 2 !!!");
    }

    // --- Get the input file(s) format from the adjFMFAtTGSInputDataInfoStrSplit two Strings array
    final String adjFMFAtTGSInputDataInfoFileFmt= adjFMFAtTGSInputDataInfoStrSplit[0];
    
    // --- Input file(s) format is not the same depending on the Spine adjustment type.
    //     TODO: We will eventually only use the S-104 DCF8 HDF5 IO format at some point.
    if (spinePPType == IWLAdjustment.Type.SpineIPP) {

      // --- CHS_JSON for now for the SpineIP type
      if (!adjFMFAtTGSInputDataInfoFileFmt.equals(IWLToolsIO.Format.CHS_JSON.name())) {
	
        throw new RuntimeException(mmi+"Only the:"+IWLToolsIO.Format.CHS_JSON.name()+
                                 " adj. FMF WL data input file format allowed for now!!");
      }
      
      // --- Get the paths of all the ship channel points locations adjusted FMF WL
      //     data input files in a List<Path> object 
      final List<Path> adjFMFAtTGSInputDataDirFilesList= WLToolsIO.
	getRelevantFilesList(adjFMFAtTGSInputDataInfoStrSplit[1], "*"+IWLAdjustmentIO.ADJ_HFP_ATTG_FNAME_PRFX+"*"+IWLToolsIO.JSON_FEXT);

      // --- Get the path of the ship channel point location adjusted FMF WL data
      //     that is the nearest to the lower side (in terms of ship channel locations indices)
      //     tide gauge.
      final String fmfAdjAtLowerSideTGFile= WLToolsIO.
	getSCLocFilePath(adjFMFAtTGSInputDataDirFilesList,this.lowerSideScLocTGId);

      slog.info(mmi+"fmfAdjAtLowerSideTGFile="+fmfAdjAtLowerSideTGFile);

      // --- Read the adjusted FMF WL data for the ship channel point location
      //     that is the nearest to the lower side tide gauge.
      this.tgsNearestSCLocsAdjFMF.put(this.lowerSideScLocTGId,
				      new MeasurementCustomBundle( WLAdjustmentIO.getWLDataInJsonFmt(fmfAdjAtLowerSideTGFile, -1L, 0.0)));

      // --- Get the path of the ship channel point location adjusted FMF WL data
      //     that is the nearest to the upper side (in terms of ship channel locations indices)
      //     tide gauge.
      final String fmfAdjAtUpperSideTGFile= WLToolsIO.
	getSCLocFilePath(adjFMFAtTGSInputDataDirFilesList,this.upperSideScLocTGId);

      slog.info(mmi+"fmfAdjAtUpperSideTGFile="+fmfAdjAtUpperSideTGFile);

      // --- Read the adjusted FMF WL data for the ship channel point location
      //     that is the nearest to the upper side tide gauge.
      this.tgsNearestSCLocsAdjFMF.put(this.upperSideScLocTGId,
      				      new MeasurementCustomBundle( WLAdjustmentIO.getWLDataInJsonFmt(fmfAdjAtUpperSideTGFile, -1L, 0.0)));      
      
    } else if (spinePPType == IWLAdjustment.Type.SpineFPP) {
       throw new RuntimeException(mmi+"SpineFPP type not ready yet!");
       
    } else {
       throw new RuntimeException(mmi+"Invalid spinePPType -> "+spinePPType.name());
    }

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
