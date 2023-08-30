package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.time.Instant;
import java.util.HashMap;
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
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.nontidal.stage.StageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;

/**
 * Comments please!
 */
final public class WLAdjustmentWDS extends WLAdjustmentType { // implements IWLAdjustment {

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentWDS";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);


  //private IWLAdjustment.Type adjType= null;

  /**
   * Comments please!
   */
  public WLAdjustmentWDS() {

    super();

    //this.wlOriginalData=
    //  this.wlAdjustedData= null;
  }

  public WLAdjustmentWDS(/*@NotNull*/ final HashMap<String,String> argsMap) { // final String wdsLocationIdInfoFile) {

    super(IWLAdjustment.Type.WDS,argsMap);

    final String mmi=
      "WLAdjustmentWDS(final WLAdjustment.Type adjType, final Map<String,String> argsMap) constructor ";

    slog.info(mmi+"start: this.locationIdInfo="+this.locationIdInfo); //wdsLocationIdInfoFile="+wdsLocationIdInfoFile);

    final String wdsLocationIdInfoFile=
      WLToolsIO.getMainCfgDir() + File.separator + this.locationIdInfo;

    final JsonObject wdsLocationInfoJsonObj=
      this.getWDSJsonLocationIdInfo( wdsLocationIdInfoFile );

    this.adjLocationZCVsVDatum= wdsLocationInfoJsonObj.
      getJsonNumber(StageIO.LOCATION_INFO_JSON_ZCIGLD_CONV_KEY).doubleValue();

    this.adjLocationLatitude= wdsLocationInfoJsonObj.
      getJsonNumber(StageIO.LOCATION_INFO_JSON_LATCOORD_KEY).doubleValue();

    this.adjLocationLongitude= wdsLocationInfoJsonObj.
      getJsonNumber(StageIO.LOCATION_INFO_JSON_LONCOORD_KEY).doubleValue();

    slog.info(mmi+"WDS adjustment location IGLD to ZC conversion value="+this.adjLocationZCVsVDatum);
    slog.info(mmi+"WDS adjustment location coordinates=("+this.adjLocationLatitude+","+this.adjLocationLongitude+")");

    // --- Now find the two nearest CHS tide gauges from this WDS grid point location
    final String wdsTideGaugesInfoFile= WLToolsIO.getMainCfgDir() + File.separator +
      IWLAdjustmentIO.TIDE_GAUGES_INFO_FOLDER_NAME + File.separator + IWLAdjustmentIO.WDS_TIDE_GAUGES_INFO_FNAME;

    slog.info(mmi+"wdsTideGaugesInfoFile="+wdsTideGaugesInfoFile);

    FileInputStream jsonFileInputStream= null;

    try {
      jsonFileInputStream= new FileInputStream(wdsTideGaugesInfoFile);

    } catch (FileNotFoundException e) {
      throw new RuntimeException(mmi+"e");
    }

    final JsonObject mainJsonMapObj= Json.
      createReader(jsonFileInputStream).readObject();

    //double minDistRad= Double.MAX_VALUE;

    // String [] twoNearestTideGaugesIds= {null, null};
    Map<Double,String> tmpDistCheck= new HashMap<Double,String>();

    final Set<String> tgStrNumIdKeysSet= mainJsonMapObj.keySet();

    // --- Loop on the tide gauges json info objects.
    for (final String tgStrNumId: tgStrNumIdKeysSet) {

      //slog.info(mmi+"Checkin with tgStrNumId="+tgStrNumId);

      final JsonObject tgInfoJsonObj=
        mainJsonMapObj.getJsonObject(tgStrNumId);

      final double tgLatitude= tgInfoJsonObj.
        getJsonNumber(StageIO.LOCATION_INFO_JSON_LATCOORD_KEY).doubleValue();

      final double tgLongitude= tgInfoJsonObj.
        getJsonNumber(StageIO.LOCATION_INFO_JSON_LONCOORD_KEY).doubleValue();

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
    this.nearestObsData= new HashMap<String, ArrayList<MeasurementCustom>>();

    //--- And initialize it with the 3 nearest TG location and null
    //    ArrayList<WLMeasurement> object for now (the ArrayList<WLMeasurement>
    //    objects will be populated later
    this.nearestObsData.put(firstNearestTGStrId, null);
    this.nearestObsData.put(secondNearestTGStrId, null);
    this.nearestObsData.put(thirdNearestTGStrId, null);

    // --- Now store the nearest tide gauges coordinates
    //     in the local nearestsTGCoords map for subsequent
    //     usage.
    final Map<String, HBCoords> nearestsTGCoords= new HashMap<String, HBCoords>();

    // --- Also need to get the ECCC TG location string id. from the Json file
    final Map<String, String> nearestsTGEcccIds= new HashMap<String, String>();

    for (final String chsTGId: this.nearestObsData.keySet()) {

      final JsonObject chsTGJsonObj= mainJsonMapObj.getJsonObject(chsTGId);

      final double tgLon= chsTGJsonObj.
        getJsonNumber(StageIO.LOCATION_INFO_JSON_LONCOORD_KEY).doubleValue();

      final double tgLat= chsTGJsonObj.
        getJsonNumber(StageIO.LOCATION_INFO_JSON_LATCOORD_KEY).doubleValue();

      nearestsTGCoords.put(chsTGId, new HBCoords(tgLon,tgLat) );

      //--- Get the TG ECCC string id.
      nearestsTGEcccIds.put(chsTGId, chsTGJsonObj.getString(TIDE_GAUGES_INFO_ECCC_IDS_KEY));

    }

    slog.info(mmi+"nearestsTGCoords keys="+nearestsTGCoords.keySet().toString());
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- We can close the Json file now
    try {
      jsonFileInputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(mmi+e);
    }

    //// --- Now get the coordinates of:
    ////     1). The nearest model input data grid point from the WDS location
    ////     2). The nearest model input data grid point from the three nearest TG locations.
    //final String firstInputDataFile= this.modelInputDataFiles.get(0);
    //slog.info(mmi+"firstInputDataFile="+firstInputDataFile);

    ////Map<Integer,HBCoords> mdlGrdPtsCoordinates= null;
    //ArrayList<HBCoords> mdlGrdPtsCoordinates= null;

    slog.info(mmi+" Getting inputDataType -> "+this.inputDataType.name()+
              " using the "+this.inputDataFormat.name()+" file format");

    // --- TODO: replace this if-else block by a switch-case block ??
    if (this.inputDataType == IWLAdjustmentIO.InputDataType.ECCC_H2D2) {

      if (this.inputDataFormat == IWLAdjustmentIO.InputDataTypesFormatsDef.ASCII) {

        this.getH2D2ASCIIWLFProbesData(nearestsTGCoords, nearestsTGEcccIds);

      } else {
        throw new RuntimeException(mmi+"Invalid inputDataFormat -> "+this.inputDataFormat.name()+" !!");
      }

      //if (this.inputDataFormat == IWLAdjustmentIO.InputDataTypesFormatsDef.NETCDF) {
      //  this.getH2D2NearestGPNCDFWLData(nearestsTGCoords);
      //
      //} else {
      //  throw new RuntimeException(mmi+"Invalid inputDataFormat -> "+this.inputDataFormat.name()+" !!");
      //}

    } else {
      throw new RuntimeException(mmi+"Invalid inputDataType -> "+this.inputDataType.name());
    }

    // --- Now find the 3 nearest WDS grid points from the 3 neareast TG locations
    //     to apply the IWLS-FMS algo to their NS_TIDE predictions and merge the results with the
    //     model WLF signal before interpolating their predictions errors (past: WLO, future: WLF)
    //     to the WDS location to add it to its NS_TIDE prediction

    // --- Split the this.locationIdInfo to use the resulting String array to build
    //     the path to the main discharges cluster directory
    final String [] locationIdInfoSplit= this.locationIdInfo.split(File.separator);

    // --- Build the path to the main discharges cluster directory where to find all the
    //     WDS grid points definition.
    final String mainDischargeClustersDir= WLToolsIO.getMainCfgDir() +
      File.separator + String.join(File.separator,Arrays.copyOfRange(locationIdInfoSplit,0,4));

    slog.info(mmi+"mainDischargeClustersDir="+mainDischargeClustersDir);

    // --- Now we need to read all WDS grid points location json info files.
    //     that are located in the subdirectories under the mainDischargeClustersDir
    DirectoryStream<Path> mainDischargeClustersSubDirs= null;

    try {
      mainDischargeClustersSubDirs=
        Files.newDirectoryStream(Paths.get(mainDischargeClustersDir));
    } catch (IOException e) {
      throw new RuntimeException(mmi+e);
    }

    // --- Iterate on all the disharges clusters subdirectories to find
    //     all the WDS grid points json info files
    for(final Path clusterSubDir: mainDischargeClustersSubDirs) {

      final String tfhaSubDir= clusterSubDir + File.separator + "dischargeClimatoTFHA";

      DirectoryStream<Path> clusterDirGpInfoFiles= null;

      try {
        clusterDirGpInfoFiles=
          Files.newDirectoryStream(Paths.get(tfhaSubDir));
      } catch (IOException e) {
        throw new RuntimeException(mmi+e);
      }

      for (final Path jsonInfoFile: clusterDirGpInfoFiles) {
        slog.info(mmi+"jsonInfoFile="+jsonInfoFile.toString());

        slog.info(mmi+"Debug System.exit(0)");
        System.exit(0);
      }
    }

    slog.info(mmi+"end");

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);
  }

  ///**
  // * Comments please.
  // */
  final public List<MeasurementCustom> getAdjustment() {

    final String mmi= "getAdjustment: ";

    slog.info(mmi+"start");

    slog.info(mmi+"end");

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

    return this.locationAdjustedData; //adjustmentRet;
  }
}

