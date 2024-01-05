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
import ca.gc.dfo.chs.wltools.wl.ITideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
//import ca.gc.dfo.chs.wltools.nontidal.stage.StageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentSpinePP;
import ca.gc.dfo.chs.wltools.tidal.nonstationary.INonStationaryIO;

/**
 * Comments please!
 */
final public class WLAdjustmentSpineIPP extends WLAdjustmentSpinePP {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentSpineIPP: ";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // private String nonAdjFMFInputDataInfo= null;
  // private Map<String, Double> twoNearestTGInfo= new HashMap<String, Double>(2);
  // // --- The List<MeasurementCustom> where to save the adjusted forecast
  // //     at the Spine location being processed.
  // //private List<MeasurementCustom> spineLocationAdjForecast= null;
  // private MeasurementCustomBundle spineLocationAdjForecast= null;
  // // --- To store The initial NS_TIDE WL predictions at the Spine target location.
  // // INPUT ONLY, not used if the spineLocationNonAdjForecast= is used
  // //private List<MeasurementCustom> spineLocationNSTPred= null;
  // // --- To store the non-adjusted WL NS Tide WL pred data at the Spine location
  // //     possibly merged with the non-adjusted full model forecast WL data extracted
  // //     at this same Spine locatiom.
  // //     INPUT ONLY
  // //private List<MeasurementCustom> spineLocationNonAdjData= null;
  //  private MeasurementCustomBundle spineLocationNonAdjData= null;
  // // --- To store the NS_TIDE WL non-ajusted predictions at the Spine locations that are
  // //     the nearest to the tide gauges locations and that could possibly be merged with
  // //      the non-adjusted full model forecast WL data extracted at this same Spine locatiom.
  // //     INPUT ONLY
  // //private Map<String, List<MeasurementCustom>> tgsNearestSpineLocationsNonAdjData= null;
  // private Map<String, MeasurementCustomBundle> tgsNearestSpineLocationsNonAdjData= null;

  // // --- To store the model adjusted forecast at the spine locations that are the
  // //     nearest to the tide gauges locations used for the adjustments.
  // //     INPUT ONLY
  // //private Map<String, List<MeasurementCustom>> tgsNearestSpineLocationsAdjForecast= null;
  // private Map<String, MeasurementCustomBundle> tgsNearestSpineLocationsAdjForecast= null;

  //private IWLAdjustment.Type adjType= null;

  /**
   * Comments please!
   */
  public WLAdjustmentSpineIPP() {
    super();

    //this.wlOriginalData=
    //  this.wlAdjustedData= null;
  }

  // ---
  public WLAdjustmentSpineIPP(/*@NotNull*/ final HashMap<String,String> argsMap) {

    super(IWLAdjustment.Type.SpineIPP,argsMap);

    final String mmi=
      "WLAdjustmentSpineIPP(final WLAdjustment.Type adjType, final Map<String,String> argsMap) constructor ";

    slog.info(mmi+"start: this.locationIdInfo="+this.locationIdInfo);

    // --- Check if the non-adjusted full model forecast is available for this run.
    //     (If yes then it will be used instead of the NS Tide prediction at the
    //      Spine location being processed)
    if (argsMap.keySet().contains("--nonAdjFMFInputDataInfo")) {

      this.nonAdjFMFInputDataInfo= argsMap.get("--nonAdjFMFInputDataInfo");

      throw new RuntimeException(mmi+"Usage of the non-adjusted full model forecast not ready yet!!");
       //slog.info(mmi+"Will use this.nonAdjFMFInputDataInfo="+this.nonAdjFMFInputDataInfo);
    }

    //slog.info(mmi+"Not ready yet!");
    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

    if (!argsMap.keySet().contains("--tideGaugeLocationsDefFileName")) {
      throw new RuntimeException(mmi+
         "Must have the --tideGaugeLocationsDefFileName=<tide gauges definition file name> defined in argsMap");
    }

    final String tideGaugeLocationsDefFileName= argsMap.get("--tideGaugeLocationsDefFileName");

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

    // --- Extract the relevant substrings that will be used to find the location tidal consts.
    //     file on disk from the tidalConstsInputInfoStrSplit array
    final String tidalConstsTypeId= tidalConstsInputInfoStrSplit[1];
    final String tidalConstsTypeModelId= tidalConstsInputInfoStrSplit[2];

    // --- Build the path of the location tidal consts. file on disk.
    final String spineLocationTCInputFile= WLToolsIO.
      getLocationNSTFHAFilePath(tidalConstsTypeId, tidalConstsTypeModelId, this.locationIdInfo);

    slog.info(mmi+"spineLocationTCInputFile="+spineLocationTCInputFile);

    final HBCoords spineLocationHBCoord= HBCoords.
      getFromCHSJSONTCFile(spineLocationTCInputFile);

    slog.info(mmi+"spineLocationHBCoord lon="+spineLocationHBCoord.getLongitude());
    slog.info(mmi+"spineLocationHBCoord lat="+spineLocationHBCoord.getLatitude());

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Now find the two nearest CHS tide gauges from this WDS grid point location
    final String spineTideGaugesInfoFile= WLToolsIO.
      getTideGaugeInfoFilePath(tideGaugeLocationsDefFileName);

      //WLToolsIO.getMainCfgDir() + File.separator +
      //ITideGaugeConfig.INFO_FOLDER_NAME + File.separator + tideGaugeDefFileName ;

    slog.info(mmi+"spineTideGaugesInfoFile="+spineTideGaugesInfoFile);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Object for reading the CHS tide gauges info file.
    FileInputStream jsonFileInputStream= null;

    try {
      jsonFileInputStream= new FileInputStream(spineTideGaugesInfoFile);

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

    // --- Define the Set of the CHS tide gauges string ids.
    final Set<String> tgStrNumIdKeysSet= mainJsonTGInfoMapObj.keySet();

    slog.info(mmi+"tgStrNumIdKeysSet="+tgStrNumIdKeysSet.toString());

    //--- Keep only the tide gauges that are at 80km or less from the
    //     locations where we want to adjust the water levels.
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    Map<IHBGeom.BBoxCornersId,HBCoords>
      tideGaugesRectBBox= new HashMap<IHBGeom.BBoxCornersId,HBCoords>(2);

    tideGaugesRectBBox.put(IHBGeom.BBoxCornersId.SOUTH_WEST,
                           new HBCoords(Double.MAX_VALUE,Double.MAX_VALUE));

    tideGaugesRectBBox.put(IHBGeom.BBoxCornersId.NORTH_EAST,
                           new HBCoords(-Double.MAX_VALUE,Double.MIN_VALUE));

    // String [] twoNearestTideGaugesIds= {null, null};
    Map<Double,String> tmpDistCheck= new HashMap<Double,String>();

    Map<String, HBCoords> tmpTGHBCoords= new HashMap<String, HBCoords>();

    // --- Loop on the tide gauges json info objects.
    for (final String chsTGStrNumId: tgStrNumIdKeysSet) {

      //slog.info(mmi+"Checkin with tgStrNumId="+tgStrNumId);

      final JsonObject tgInfoJsonObj=
        mainJsonTGInfoMapObj.getJsonObject(chsTGStrNumId);

      final double tgLatitude= tgInfoJsonObj.
        getJsonNumber(IWLLocation.INFO_JSON_LATCOORD_KEY).doubleValue();

      final double tgLongitude= tgInfoJsonObj.
        getJsonNumber(IWLLocation.INFO_JSON_LONCOORD_KEY).doubleValue();

      // --- calculate the distance (in radians) between the location and this CHS tide gauge
      //final double tgDistRad= Trigonometry.getDistanceInRadians(tgLongitude, tgLatitude,
      //                                                          spineLocationHBCoord.getLongitude(),
      //                                                          spineLocationHBCoord.getLatitude()) ; //this.adjLocationLongitude, this.adjLocationLatitude);

      final double tgDistKm= Trigonometry.getDistanceInKms(tgLongitude, tgLatitude,
                                                           spineLocationHBCoord.getLongitude(),
                                                           spineLocationHBCoord.getLatitude()) ;

      // --- Store this distance in the temporary Map
      //tmpDistCheck.put((Double)tgDistRad, chsTGStrNumId); //(tgStrNumId,tgDistRad);
      tmpDistCheck.put((Double)tgDistKm, chsTGStrNumId);

      tmpTGHBCoords.put(chsTGStrNumId, new HBCoords(tgLongitude, tgLatitude) );

      //slog.info(mmi+"chsTGStrNumId="+chsTGStrNumId+", tgLongitude="+tgLongitude+", tgLatitude="+tgLatitude);

      if (tgLongitude < tideGaugesRectBBox.get(IHBGeom.BBoxCornersId.SOUTH_WEST).getLongitude()) {
        tideGaugesRectBBox.get(IHBGeom.BBoxCornersId.SOUTH_WEST).setLongitude(tgLongitude);
      }

      if (tgLatitude < tideGaugesRectBBox.get(IHBGeom.BBoxCornersId.SOUTH_WEST).getLatitude()) {
        tideGaugesRectBBox.get(IHBGeom.BBoxCornersId.SOUTH_WEST).setLatitude(tgLatitude);
      }

      if (tgLongitude > tideGaugesRectBBox.get(IHBGeom.BBoxCornersId.NORTH_EAST).getLongitude()) {
        tideGaugesRectBBox.get(IHBGeom.BBoxCornersId.NORTH_EAST).setLongitude(tgLongitude);
      }

      if (tgLatitude > tideGaugesRectBBox.get(IHBGeom.BBoxCornersId.NORTH_EAST).getLatitude()) {
        tideGaugesRectBBox.get(IHBGeom.BBoxCornersId.NORTH_EAST).setLatitude(tgLatitude);
      }

      //slog.info(mmi+"tgStrNumId="+tgStrNumId+", tgDistRad="+tgDistRad);
    }

    slog.info(mmi+"tideGaugesRectBBox.get(IHBGeom.BBoxCornersId.SOUTH_WEST).getLongitude()="+
              tideGaugesRectBBox.get(IHBGeom.BBoxCornersId.SOUTH_WEST).getLongitude());

    slog.info(mmi+"tideGaugesRectBBox.get(IHBGeom.BBoxCornersId.SOUTH_WEST).getLatitude()="+
              tideGaugesRectBBox.get(IHBGeom.BBoxCornersId.SOUTH_WEST).getLatitude());

    slog.info(mmi+"tideGaugesRectBBox.get(IHBGeom.BBoxCornersId.NORTH_EAST).getLongitude()="+
              tideGaugesRectBBox.get(IHBGeom.BBoxCornersId.NORTH_EAST).getLongitude());

    slog.info(mmi+"tideGaugesRectBBox.get(IHBGeom.BBoxCornersId.NORTH_EAST).getLatitude()="+
              tideGaugesRectBBox.get(IHBGeom.BBoxCornersId.NORTH_EAST).getLatitude());

    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    // --- Use the SortedSet class to automagically sort the distances used
    //     as kays in the tmpDistCheck Map
    final SortedSet<Double> sortedTGDistRad= new TreeSet<Double>(tmpDistCheck.keySet());

    // --- Convert the SortedSet to an array of Double objects.
    final Object [] sortedTGDistRadArray= sortedTGDistRad.toArray();

    final Double firstNearestDistKmForTG= (Double) sortedTGDistRadArray[0]; //sortedTGDistRad.first();
    final Double secondNearestDistKmForTG= (Double) sortedTGDistRadArray[1];
    final Double thirdNearestDistKmForTG= (Double) sortedTGDistRadArray[2];
    final Double fourthNearestDistKmForTG= (Double) sortedTGDistRadArray[3];
      //sortedTGDistRad.tailSet(firstNearestDistRadForTG).first();

    //slog.info(mmi+"sortedTGDistRad="+sortedTGDistRad.toString());
    slog.info(mmi+"firstNearestDistKmForTG="+firstNearestDistKmForTG);
    slog.info(mmi+"secondNearestDistKmForTG="+secondNearestDistKmForTG);
    slog.info(mmi+"thirdNearestDistKmForTG="+thirdNearestDistKmForTG);
    slog.info(mmi+"fourthNearestDistKmForTG="+fourthNearestDistKmForTG);

    final String firstNearestTGStrId= tmpDistCheck.get(firstNearestDistKmForTG);
    final String secondNearestTGStrId= tmpDistCheck.get(secondNearestDistKmForTG);
    final String thirdNearestTGStrId= tmpDistCheck.get(thirdNearestDistKmForTG);
    final String fourthNearestTGStrId= tmpDistCheck.get(+fourthNearestDistKmForTG);

    slog.info(mmi+"firstNearestTGStrId="+firstNearestTGStrId);
    slog.info(mmi+"secondNearestTGStrId="+secondNearestTGStrId);
    slog.info(mmi+"thirdNearestTGStrId="+thirdNearestTGStrId);
    slog.info(mmi+"fourthNearestTGStrId="+fourthNearestTGStrId);

    // --- Now store the nearest tide gauges coordinates
    //     in the local nearestsTGCoords map for subsequent
    //     usage.
    final Map<String, HBCoords> nearestsTGCoords= new HashMap<String, HBCoords>();

    nearestsTGCoords.put(firstNearestTGStrId, tmpTGHBCoords.get(firstNearestTGStrId));
    nearestsTGCoords.put(secondNearestTGStrId, tmpTGHBCoords.get(secondNearestTGStrId));
    nearestsTGCoords.put(thirdNearestTGStrId, tmpTGHBCoords.get(thirdNearestTGStrId));
    nearestsTGCoords.put(fourthNearestTGStrId, tmpTGHBCoords.get(fourthNearestTGStrId));

    slog.info(mmi+"nearestsTGCoords keys="+nearestsTGCoords.keySet().toString());
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    if (!argsMap.keySet().contains("--adjForecastAtTGSInputDataInfo")) {
     throw new RuntimeException(mmi+
        "Must have the --adjForecastAtTGSInputDataInfo=<input file(s) format>:<FMF adjustment data input directory> defined in argsMap");
    }

    final String [] adjForecastAtTGSInputDataInfoStrSplit=
      argsMap.get("--adjForecastAtTGSInputDataInfo").split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

    if (adjForecastAtTGSInputDataInfoStrSplit.length != 2) {
      throw new RuntimeException(mmi+"adjForecastAtTGSInputDataInfoStrSplit.length != 2 !!");
    }

    if ( !adjForecastAtTGSInputDataInfoStrSplit[0].equals(IWLToolsIO.Format.CHS_JSON.name()) ) {
      throw new RuntimeException(mmi+"Invalid FMF adjustment input data file format -> "+adjForecastAtTGSInputDataInfoStrSplit[0]);
    }

    final String adjForecastAtTGSInputDataDir= adjForecastAtTGSInputDataInfoStrSplit[1];

    final File fmfAdjInfoDirFileObj= new File(adjForecastAtTGSInputDataDir);

    if (!fmfAdjInfoDirFileObj.exists()) {
      throw new RuntimeException(mmi+"FMF adjustment data input directory -> "+adjForecastAtTGSInputDataDir+" not found!!");
    }

    slog.info(mmi+"adjForecastAtTGSInputDataDir="+adjForecastAtTGSInputDataDir);

    slog.info(mmi+"Debug exit 0");
    System.exit(0);

    if (!argsMap.keySet().contains("--nsTidePredInputDataInfo")) {
      throw new RuntimeException(mmi+
        "Must have the --nsTidePredInputDataInfo=<input file(s) format>:<NS_TIDE pred. data input directory> defined in argsMap");
    }

    final String [] nsTidePredInputDataInfoStrSplit=
      argsMap.get("--nsTidePredInputDataInfo").split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

    if (nsTidePredInputDataInfoStrSplit.length != 2) {
      throw new RuntimeException(mmi+"nsTidePredInputDataInfoStrSplit.length != 2 !!");
    }

    //slog.info(mmi+"nsTidePredInputDataInfoStrSplit[0]="+nsTidePredInputDataInfoStrSplit[0]+".");
    //slog.info(mmi+"IWLToolsIO.Format.CHS_JSON.name()="+IWLToolsIO.Format.CHS_JSON.name()+".");

    if ( !nsTidePredInputDataInfoStrSplit[0].equals(IWLToolsIO.Format.CHS_JSON.name()) ) {
      throw new RuntimeException(mmi+"Invalid NS Tide pred input file format -> "+nsTidePredInputDataInfoStrSplit[0]);
    }

    final String nsTidePredInputDataDir= nsTidePredInputDataInfoStrSplit[1];

    final File nsTidePredInputDataDirFileObj= new File(nsTidePredInputDataDir);

    if (!nsTidePredInputDataDirFileObj.exists()) {
      throw new RuntimeException(mmi+"NS Tide pred data input directory -> "+nsTidePredInputDataDir+" not found!!");
    }

    slog.info(mmi+"nsTidePredInputDataDir="+nsTidePredInputDataDir);

    final String spineLocNSTidePredFilePrfx= this.locationIdInfo.
      replace(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR, IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR);

    //--- First get the NS Tide WL pred data at the Spine location being processed:
    final String spineLocationNSTidePredFile= nsTidePredInputDataDir +
      File.separator + spineLocNSTidePredFilePrfx + IWLToolsIO.JSON_FEXT;

    //final File spineLocationNSTidePredFileObj= new File(spineLocationNSTidePredFile);

    //slog.info(mmi+"this.locationIdInfo="+this.locationIdInfo);
    slog.info(mmi+"spineLocationNSTidePredFile="+spineLocationNSTidePredFile);

    // --- Put the Spine location WL pred data in a MeasurementCustomBundle object
    //     to avoid the (very) annoying wrong array indexing syndrome
    this.spineLocationNonAdjData= new
      MeasurementCustomBundle(WLAdjustmentIO.
        getWLDataInJsonFmt(spineLocationNSTidePredFile, -1L, 0.0));

    // --- Inatantiate the HashMap for the other Spine locations WL Pred data
    this.tgsNearestSpineLocationsNonAdjData= new HashMap<String, MeasurementCustomBundle>();

    //--- Now read the NS Tide prediction data for the spine locations that
    ///   the nearests to the 3 nearest TGs
    for (final String chsTGStrNumId: nearestsTGCoords.keySet() ) {

      final JsonObject tgInfoJsonObj=
        mainJsonTGInfoMapObj.getJsonObject(chsTGStrNumId);

      final String nearTGSpineLocId= tgInfoJsonObj.
        getString(ITideGaugeConfig.INFO_NEAREST_SPINE_POINT_ID_JSON_KEY);

      slog.info(mmi+"nearTGSpineLocId="+nearTGSpineLocId);

      final String nearTGSpineLocTidePredFile= nsTidePredInputDataDir +
      File.separator + nearTGSpineLocId + IWLToolsIO.JSON_FEXT;

      slog.info(mmi+"Reading NS Tide prediction for CHS TG -> "+chsTGStrNumId+
                " from the Json file -> "+nearTGSpineLocTidePredFile+"\n");

      final List<MeasurementCustom> tmpMCList= WLAdjustmentIO.
        getWLDataInJsonFmt(spineLocationNSTidePredFile, -1L, 0.0);

      this.tgsNearestSpineLocationsNonAdjData.
        put(chsTGStrNumId, new MeasurementCustomBundle(tmpMCList));
    }

    if (this.nonAdjFMFInputDataInfo != null) {

      //--- Merge the non-adjusted NS Tide pred data with the non-adjusted full model forecast
      //    data for the Spine location being processed and also for the spine locations that are the
      //    nearest to the nearest 3 tide gauges locations

      throw new RuntimeException(mmi+
        "The usage of the non-adjusted full model forecast for the Spine location is not ready yet!");
    }

    slog.info(mmi+"Debug exit 0");
    System.exit(0);

    //// --- Now get the coordinates of:
    ////     1). The nearest model input data grid point from the WDS location
    ////     2). The nearest model input data grid point from the three nearest TG locations.
    //final String firstInputDataFile= this.modelInputDataFiles.get(0);
    //slog.info(mmi+"firstInputDataFile="+firstInputDataFile);

    ////Map<Integer,HBCoords> mdlGrdPtsCoordinates= null;
    //ArrayList<HBCoords> mdlGrdPtsCoordinates= null;

    //slog.info(mmi+" Getting inputDataType -> "+this.inputDataType.name()); //+
        //  " using the "+this.inputDataFormat.name()+" file format");

    // --- TODO: replace this if-else block by a switch-case block ??
    //if (this.inputDataType == IWLAdjustmentIO.DataType.CHS_SPINE) {

      //if (this.inputDataFormat == IWLAdjustmentIO.InputDataTypesFormatsDef.ASCII) {
        //final Map<String, String> nearestsTGEcccIds= new HashMap<String, String>();
      //  this.getH2D2ASCIIWLFProbesData(nearestsTGCoords, mainJsonMapObj); //nearestsTGEcccIds);

      //} else {
      //  throw new RuntimeException(mmi+"Invalid inputDataFormat -> "+this.inputDataFormat.name()+
      //                              " for inputDataType ->"+this.inputDataType.name()+" !!");
      //}

      //if (this.inputDataFormat == IWLAdjustmentIO.InputDataTypesFormatsDef.NETCDF) {
      //  this.getH2D2NearestGPNCDFWLData(nearestsTGCoords);
      //
      //} else {
      //  throw new RuntimeException(mmi+"Invalid inputDataFormat -> "+this.inputDataFormat.name()+" !!");
      //}

    //} else if  (this.inputDataType == IWLAdjustmentIO.DataType.CHS_DHP_S104) {
    //
    //  throw new RuntimeException(mmi+" inputDataType -> "+
    //                             IWLAdjustmentIO.InputDataType.CHS_DHP_S104.name()+" not ready to be used yet!!");

    //} else {
    //  throw new RuntimeException(mmi+"Invalid inputDataType -> "+this.inputDataType.name());
    //}

    // --- We can close the Json file now
    //try {
    //  jsonFileInputStream.close();
    //} catch (IOException e) {
    //  throw new RuntimeException(mmi+e);
    //}

    // --- Now find the 3 nearest Spine grid points location from the 3 neareast TG locations
    //     to apply the IWLS-FMS algo to their NS_TIDE predictions and merge the results with the
    //     model WLF signal before interpolating their predictions errors (past: WLO, future: WLF)
    //     to the Spine location to add it to its NS_TIDE prediction

    // --- Split the this.locationIdInfo to use the resulting String array to build
    //     the path to the main discharges cluster directory
    //final String [] locationIdInfoSplit= this.locationIdInfo.split(File.separator);
    // --- Extract the name of the spine domain (e.g. STLawrence
    //final String spineDomainName= locationIdInfoSplit[2];
    //slog.info(mmi+"spineDomainName="+spineDomainName);
    // --- Extract the name of the discharge cluster where the spine location is located
    //final String spineLocationClusterName= locationIdInfoSplit[4];
    //slog.info(mmi+"spineLocationClusterName="+spineLocationClusterName);
    // --- Add the spineLocationClusterName to the 2 other nearestDischargeClusters Set
    //     in order to get a complete collection for finding the nearest spine locations
    //     for each nearest CHS TG
    //nearestDischargeClusters.add(spineLocationClusterName);

    // --- Redefine this.locationId to include its spineDomainName and
    //     its cluster name and also remove the unwanted file name suffix.
    //final String locIdStrWithoutSuffix= this.location.getIdentity().
    //  replace(INonStationaryIO.LOCATION_TIDAL_CONSTS_FNAME_SUFFIX,"");

   // final String spinelocationId= spineDomainName +
   //   IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + spineLocationClusterName +
   //           IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR + locIdStrWithoutSuffix;

    //slog.info(mmi+"spinelocationId= "+spinelocationId);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Build the path to the main discharges cluster directory where to find all the
    //     WDS grid points definition.
    //final String mainDischargeClustersDir= WLToolsIO.getMainCfgDir() +
    //  File.separator + String.join(File.separator,Arrays.copyOfRange(locationIdInfoSplit,0,4));
    //slog.info(mmi+"mainDischargeClustersDir="+mainDischargeClustersDir);

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    slog.info(mmi+"end");

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);
  }

  ///**
  // * Comments please.
  // */
  final public List<MeasurementCustom> getAdjustment(final String outputDirectory) {

    final String mmi= "getAdjustment: ";

    slog.info(mmi+"start");

    slog.info(mmi+"end");

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

    return this.locationAdjustedData; //adjustmentRet;
  }
}
