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
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.wl.IWLLocation;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.wl.ITideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
//import ca.gc.dfo.chs.wltools.nontidal.stage.StageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
import ca.gc.dfo.chs.wltools.tidal.nonstationary.INonStationaryIO;

/**
 * Comments please!
 */
final public class WLAdjustmentSpineIPP extends WLAdjustmentType {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentSpineIPP: ";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // --- To store the HBCoords objects of all the spine cluster locations that
  //    are relevant for the WL adjustment at a given spine location.
  //private Map<String, Map<String,HBCoords>> relevantSpineClustersInfo= null;

  // --- To store The initial NS_TIDE WL predictions at the Spine target location.
  private List<MeasurementCustom> spineLocationNSTPred= null;

  // --- To store The adjusted NS_TIDE WL predictions at the Spine target location.
  private List<MeasurementCustom> spineLocationNSTPredAdj= null;

  // --- To store The NS_TIDE WL predictions at the Spine locations that are
  //     the nearest to the tide gauges locations used for the adjustments.
  private Map<String, List<MeasurementCustom>> tgsNearestSpineLocationsPred= null;

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

    //slog.info(mmi+"Not ready yet!");
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

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
      tidalConstsInputInfo.split(IWLLocation.ID_SPLIT_CHAR);

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
    //    file on disk from the tidalConstsInputInfoStrSplit array
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

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

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
    final JsonObject mainJsonMapObj= Json.
      createReader(jsonFileInputStream).readObject();

    // --- We can close the tide gauges info Json file now
    try {
      jsonFileInputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(mmi+e);
    }

    // --- Define the Set of the CHS tide gauges string ids.
    final Set<String> tgStrNumIdKeysSet= mainJsonMapObj.keySet();

    slog.info(mmi+"tgStrNumIdKeysSet="+tgStrNumIdKeysSet.toString());

    //--- Keep only the tide gauges that are at 80km or less from the
    //     locations where we want to adjust the water levels.
    slog.info(mmi+"Debug exit 0");
    System.exit(0);

    if (!argsMap.keySet().contains("--nsTidePredInputDataInfo")) {
      throw new RuntimeException(mmi+
        "Must have the --nsTidePredInputDataDir=<NS_TIDE pred. data input directory> defined in argsMap");
    }

    final String nsTidePredInputDataDir= argsMap.get("--nsTidePredInputDataDir");

    slog.info(mmi+"nsTidePredInputDataDir="+nsTidePredInputDataDir);

    final String spineLocationIdInfoFile=
      WLToolsIO.getMainCfgDir() + File.separator + this.locationIdInfo;

    final JsonObject spineLocationInfoJsonObj=
      this.getSpineJsonLocationIdInfo( spineLocationIdInfoFile );

    this.adjLocationZCVsVDatum= spineLocationInfoJsonObj.
      getJsonNumber(IWLLocation.INFO_JSON_ZCIGLD_CONV_KEY).doubleValue();

    this.adjLocationLatitude= spineLocationInfoJsonObj.
      getJsonNumber(IWLLocation.INFO_JSON_LATCOORD_KEY).doubleValue();

    this.adjLocationLongitude= spineLocationInfoJsonObj.
      getJsonNumber(IWLLocation.INFO_JSON_LONCOORD_KEY).doubleValue();

    //this.relevantSpineClustersInfo= new HashMap<String, Map<String,HBCoords>>();

    slog.info(mmi+"Spine adjustment location IGLD to ZC conversion value="+this.adjLocationZCVsVDatum);
    slog.info(mmi+"Spine adjustment location coordinates=("+this.adjLocationLatitude+","+this.adjLocationLongitude+")");
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    //FileInputStream jsonFileInputStream= null;
    //try {
    //  jsonFileInputStream= new FileInputStream(spineTideGaugesInfoFile);
    //} catch (FileNotFoundException e) {
    //  throw new RuntimeException(mmi+"e");
    //}

    //final JsonObject mainJsonMapObj= Json.
    //  createReader(jsonFileInputStream).readObject();

    //double minDistRad= Double.MAX_VALUE;

    // String [] twoNearestTideGaugesIds= {null, null};
    Map<Double,String> tmpDistCheck= new HashMap<Double,String>();
    //final Set<String> tgStrNumIdKeysSet= mainJsonMapObj.keySet();

    // --- Loop on the tide gauges json info objects.
    for (final String tgStrNumId: tgStrNumIdKeysSet) {

      //slog.info(mmi+"Checkin with tgStrNumId="+tgStrNumId);

      final JsonObject tgInfoJsonObj=
        mainJsonMapObj.getJsonObject(tgStrNumId);

      final double tgLatitude= tgInfoJsonObj.
        getJsonNumber(IWLLocation.INFO_JSON_LATCOORD_KEY).doubleValue();

      final double tgLongitude= tgInfoJsonObj.
        getJsonNumber(IWLLocation.INFO_JSON_LONCOORD_KEY).doubleValue();

      final double tgDistRad= Trigonometry.
        getDistanceInRadians(tgLongitude, tgLatitude, this.adjLocationLongitude, this.adjLocationLatitude);

      tmpDistCheck.put(tgDistRad,tgStrNumId); //(tgStrNumId,tgDistRad);

      //slog.info(mmi+"tgStrNumId="+tgStrNumId+", tgDistRad="+tgDistRad);
    }

    // --- Use the SortedSet class to automagically sort the distances used
    //     as kays in the tmpDistCheck Map
    final SortedSet<Double> sortedTGDistRad= new TreeSet<Double>(tmpDistCheck.keySet());

    // --- Convert the SortedSet to an array of Double objects.
    final Object [] sortedTGDistRadArray= sortedTGDistRad.toArray();

    final Double firstNearestDistRadForTG= (Double) sortedTGDistRadArray[0]; //sortedTGDistRad.first();
    final Double secondNearestDistRadForTG= (Double) sortedTGDistRadArray[1];
    final Double thirdNearestDistRadForTG= (Double) sortedTGDistRadArray[2];
      //sortedTGDistRad.tailSet(firstNearestDistRadForTG).first();

    //slog.info(mmi+"sortedTGDistRad="+sortedTGDistRad.toString());
    slog.info(mmi+"firstNearestDistRadForTG="+firstNearestDistRadForTG);
    slog.info(mmi+"secondNearestDistRadForTG="+secondNearestDistRadForTG);
    slog.info(mmi+"thirdNearestDistRadForTG="+thirdNearestDistRadForTG);

    final String firstNearestTGStrId= tmpDistCheck.get(firstNearestDistRadForTG);
    final String secondNearestTGStrId= tmpDistCheck.get(secondNearestDistRadForTG);
    final String thirdNearestTGStrId= tmpDistCheck.get(thirdNearestDistRadForTG);

    slog.info(mmi+"firstNearestTGStrId="+firstNearestTGStrId);
    slog.info(mmi+"secondNearestTGStrId="+secondNearestTGStrId);
    slog.info(mmi+"thirdNearestTGStrId="+thirdNearestTGStrId);

    // --- Instantiate the this.nearestObsData Map for subsequent usage
    //this.nearestObsData= new HashMap<String, ArrayList<WLMeasurement>>();
    //this.nearestObsData= new HashMap<String, List<MeasurementCustom>>();

    //--- And initialize it with the 3 nearest TG location and null
    //    ArrayList<WLMeasurement> object for now (the ArrayList<WLMeasurement>
    //    objects will be populated later
    //this.nearestObsData.put(firstNearestTGStrId, null);
    //this.nearestObsData.put(secondNearestTGStrId, null);
   // this.nearestObsData.put(thirdNearestTGStrId, null);

    // --- Now store the nearest tide gauges coordinates
    //     in the local nearestsTGCoords map for subsequent
    //     usage.
    final Map<String, HBCoords> nearestsTGCoords= new HashMap<String, HBCoords>();

    // --- Also need to get the ECCC TG location string id. from the Json file
    //final Map<String, String> nearestsTGEcccIds= new HashMap<String, String>();

    //for (final String chsTGId: this.nearestObsData.keySet()) {
    //  final JsonObject chsTGJsonObj= mainJsonMapObj.getJsonObject(chsTGId);
    //  final double tgLon= chsTGJsonObj.
    //    getJsonNumber(IWLLocation.INFO_JSON_LONCOORD_KEY).doubleValue();
    //  final double tgLat= chsTGJsonObj.
    //    getJsonNumber(IWLLocation.INFO_JSON_LATCOORD_KEY).doubleValue();
    //  nearestsTGCoords.put(chsTGId, new HBCoords(tgLon,tgLat) );
    //  //--- Get the TG ECCC string id.
    //  //nearestsTGEcccIds.put(chsTGId, chsTGJsonObj.getString(TIDE_GAUGES_INFO_ECCC_IDS_KEY));
    //}

    slog.info(mmi+"nearestsTGCoords keys="+nearestsTGCoords.keySet().toString());
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

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
    final String [] locationIdInfoSplit= this.locationIdInfo.split(File.separator);

    // --- Extract the name of the spine domain (e.g. STLawrence
    final String spineDomainName= locationIdInfoSplit[2];

    slog.info(mmi+"spineDomainName="+spineDomainName);

    // --- Extract the name of the discharge cluster where the spine location is located
    final String spineLocationClusterName= locationIdInfoSplit[4];

    slog.info(mmi+"spineLocationClusterName="+spineLocationClusterName);

    // --- Add the spineLocationClusterName to the 2 other nearestDischargeClusters Set
    //     in order to get a complete collection for finding the nearest spine locations
    //     for each nearest CHS TG
    //nearestDischargeClusters.add(spineLocationClusterName);

    // --- Redefine this.locationId to include its spineDomainName and
    //     its cluster name and also remove the unwanted file name suffix.
    final String locIdStrWithoutSuffix= this.location.getIdentity().
      replace(INonStationaryIO.LOCATION_TIDAL_CONSTS_FNAME_SUFFIX,"");

    final String spinelocationId= spineDomainName +
      IWLAdjustmentIO.OUTPUT_DATA_FMT_SPLIT_CHAR + spineLocationClusterName +
              IWLAdjustmentIO.OUTPUT_DATA_FMT_SPLIT_CHAR + locIdStrWithoutSuffix;

    slog.info(mmi+"spinelocationId= "+spinelocationId);
    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

    // --- Build the path to the main discharges cluster directory where to find all the
    //     WDS grid points definition.
    //final String mainDischargeClustersDir= WLToolsIO.getMainCfgDir() +
    //  File.separator + String.join(File.separator,Arrays.copyOfRange(locationIdInfoSplit,0,4));
    //slog.info(mmi+"mainDischargeClustersDir="+mainDischargeClustersDir);

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    Map<String, HBCoords> tmpSpineLocationsHBCoords= new HashMap<String, HBCoords>();

    slog.info(mmi+"tmpSpineLocationsHBCoords.size()="+tmpSpineLocationsHBCoords.size());
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    Map<String,String> tgVsSpineLocInfo= new HashMap<String,String>();
    Map<String,Double> tgVsSpineLocMinDists= new HashMap<String,Double>();

    for (final String tgNumStrId: nearestsTGCoords.keySet()) {

      tgVsSpineLocInfo.put(tgNumStrId,null);
      tgVsSpineLocMinDists.put(tgNumStrId,Double.MAX_VALUE);
    }

    for (final String spineLocationFID: tmpSpineLocationsHBCoords.keySet()) {

      //slog.info(mmi+"spineLocationFID="+spineLocationFID);

      final HBCoords spineLocHBCoordsObj=
        tmpSpineLocationsHBCoords.get(spineLocationFID);

      for (final String tgNumStrId: nearestsTGCoords.keySet()) {

        final HBCoords tgHBCoordsObj= nearestsTGCoords.get(tgNumStrId);

        final double tmpDist= Trigonometry.
          getDistanceInRadians(tgHBCoordsObj.getLongitude(), tgHBCoordsObj.getLatitude(),
                               spineLocHBCoordsObj.getLongitude(), spineLocHBCoordsObj.getLatitude());

        if (tmpDist < tgVsSpineLocMinDists.get(tgNumStrId) ) {

          //slog.info(mmi+"spineLocationFID="+spineLocationFID);
          //slog.info(mmi+"tgNumStrId="+tgNumStrId);
          //slog.info(mmi+"tmpDist="+tmpDist);

          //slog.info(mmi+"tgHBCoordsObj lon="+tgHBCoordsObj.getLongitude());
          //slog.info(mmi+"tgHBCoordsObj lat="+tgHBCoordsObj.getLatitude());
          //slog.info(mmi+"spineLocObj lon="+ spineLocHBCoordsObj.getLongitude());
          //slog.info(mmi+"spineLocObj lat="+ spineLocHBCoordsObj.getLatitude()+"\n");

          tgVsSpineLocMinDists.put(tgNumStrId,tmpDist);
          tgVsSpineLocInfo.put(tgNumStrId,spineLocationFID);
        }
      }

      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
    }

    for (final String tgNumStrId: nearestsTGCoords.keySet()) {

      // --- Allocate the this.tgsNearestSpineLocationsPred Map properly
      //     to store the NS_TIDE WL prediction for the nearest spine locations.
      this.tgsNearestSpineLocationsPred= new HashMap<String, List<MeasurementCustom> >();

      final String nearestSpineLocationId= tgVsSpineLocInfo.get(tgNumStrId);

      slog.info(mmi+"CHS TG id -> "+tgNumStrId+
                ": Reading the NS_TIDE prediction for the nearest spine location -> "+nearestSpineLocationId);

      final String nsTidePredDataJsonFile= nsTidePredInputDataDir +
        File.separator + nearestSpineLocationId + IWLToolsIO.JSON_FEXT;

      slog.info(mmi+"nsTidePredDataJsonFile="+nsTidePredDataJsonFile);

      this.tgsNearestSpineLocationsPred.put( tgNumStrId,
                                             WLAdjustmentIO.getWLDataInJsonFmt(nsTidePredDataJsonFile, -1L, 0.0) );

      if (nearestSpineLocationId.equals(this.location.getIdentity())) {

         slog.info(mmi+"The Spine target location is also the nearest spine location of the CHS TG -> "+tgNumStrId);

         this.spineLocationNSTPred= this.
           tgsNearestSpineLocationsPred.get(tgNumStrId);
      }
    }

    //slog.info(mmi+"spinelocationId="+spinelocationId);

    if (this.spineLocationNSTPred == null) {

      slog.info(mmi+"Filling-up this.spineLocationNSTPred with its NS_TIDE prediction data");

      final String nsTidePredDataJsonFile= nsTidePredInputDataDir +
        File.separator + spinelocationId + IWLToolsIO.JSON_FEXT;

      slog.info(mmi+"NS_TIDE prediction data file for the spine target location -> "+nsTidePredDataJsonFile);

      // --- fill up the this.spineLocationNSTPred with its NS_TIDE prediction
      this.spineLocationNSTPred= WLAdjustmentIO.
        getWLDataInJsonFmt(nsTidePredDataJsonFile, -1L, 0.0);

      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
    }

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
