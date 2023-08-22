package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.time.Instant;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NavigableSet;
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

    final String mmi= "WLAdjustmentWDS(final WLAdjustment.Type adjType, final Map<String,String> argsMap) constructor ";

    slog.info(mmi+"start: this.locationIdInfo="+this.locationIdInfo); //wdsLocationIdInfoFile="+wdsLocationIdInfoFile);

    final String wdsLocationIdInfoFile=
        WLToolsIO.getMainCfgDir() + "/" + this.locationIdInfo;

    final JsonObject wdsLocationInfoJsonObj=
      this.getWDSLocationIdInfo( wdsLocationIdInfoFile );

    this.adjLocationZCVsVDatum= wdsLocationInfoJsonObj.
      getJsonNumber(StageIO.LOCATION_INFO_JSON_ZCIGLD_CONV_KEY).doubleValue();

    this.adjLocationLatitude= wdsLocationInfoJsonObj.
      getJsonNumber(StageIO.LOCATION_INFO_JSON_LATCOORD_KEY).doubleValue();

    this.adjLocationLongitude= wdsLocationInfoJsonObj.
      getJsonNumber(StageIO.LOCATION_INFO_JSON_LONCOORD_KEY).doubleValue();

    slog.info(mmi+"WDS adjustment location IGLD to ZC conversion value="+this.adjLocationZCVsVDatum);
    slog.info(mmi+"WDS adjustment location coordinates=("+this.adjLocationLatitude+","+this.adjLocationLongitude+")");

    // --- Now find the two nearest CHS tide gauges from this WDS grid point location
    final String wdsTideGaugesInfoFile= WLToolsIO.getMainCfgDir() +
      "/" + IWLAdjustmentIO.TIDE_GAUGES_INFO_FOLDER_NAME + "/" + IWLAdjustmentIO.WDS_TIDE_GAUGES_INFO_FNAME;

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
    SortedSet<Double> sortedTGDistRad= new TreeSet<Double>(tmpDistCheck.keySet());

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

    // --- This is not needed anymore, we will use the WLO data of the
    //     three nearest TGs by default with interpolation weigths calculated
    //     with the respective distances in radians.
    //// --- We must have one TG location that is upstream from the WDS location
    ////     and the other TG location that is downstream from the WDS location.
    ////     This means that we keep the first nearest TG and select the other
    ////     TG from the remaining two that has its longitude difference from the
    ////     WDS location being of the opposite sign compared to the longitude
    ////     difference of the first nearest TG from the WDS location.
    ////     NOTE: If the first nearest TG does not have enough valid WLO data
    ////     to use for the adjustments then we will use the other two
    ////     as the for the WLF adjustements (assuming that those other
    ////     two TGs have valid WLO data??)
    // final JsonObject firstNearestTGJsonObj=
    //   mainJsonMapObj.getJsonObject(firstNearestTGStrId);
    //final double firstNearestTGLongDiff= this.adjLocationLongitude -
    //  firstNearestTGJsonObj.getJsonNumber(StageIO.LOCATION_INFO_JSON_LONCOORD_KEY).doubleValue();
    //slog.info(mmi+"firstNearestTGLongDiff="+firstNearestTGLongDiff);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Now get the coordinates of:
    //     1). The nearest model input data grid point from the WDS location
    //     2). The nearest model input data grid point from the three nearest TG locations.

    final String firstInputDataFile= this.inputDataFilesPaths.get(0);

    slog.info(mmi+"firstInputDataFile="+firstInputDataFile);

    Map<Integer,HBCoords> mdlGrdPtsCoordinates= null;

    slog.info(mmi+"this.inputDataFormat="+this.inputDataFormat.name());

    // --- TODO: replace this if-else block by a switch-case block ??
    if (this.inputDataType == IWLAdjustmentIO.InputDataType.ECCC_H2D2 &&
        this.inputDataFormat == IWLAdjustmentIO.InputDataTypesFormatsDef.NETCDF) {

      slog.info(mmi+" Getting inputDataType -> "+this.inputDataType.name()+
                    " grid point coordinates in "+this.inputDataFormat.name()+" file format");

      mdlGrdPtsCoordinates= this.
        getH2D2NCDFGridPointsCoords(firstInputDataFile);

    } else {
       throw new RuntimeException(mmi+"Invalid inputDataType -> "+this.inputDataType.name()+
                                  " versus inputDataFormat -> "+this.inputDataFormat.name()+" combination!");
    }

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

    try {
      jsonFileInputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(mmi+e);
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

