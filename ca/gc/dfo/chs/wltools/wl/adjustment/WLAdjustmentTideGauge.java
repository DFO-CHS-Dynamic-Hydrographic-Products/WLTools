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
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.nontidal.stage.StageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
import ca.gc.dfo.chs.wltools.tidal.nonstationary.INonStationaryIO;

/**
 * Comments please!
 */
final public class WLAdjustmentTideGauge extends WLAdjustmentType {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentTideGauge: ";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // --- To store the HBCoords objects of all the spine cluster locations that
  //    are relevant for the WL adjustment at a given spine location.
  //private Map<String, Map<String,HBCoords>> relevantSpineClustersInfo= null;

  // --- To store The NS_TIDE WL predictions at the Spine locations that are
  //     the nearest to the tide gauges locations used for the adjustments.
  //private Map<String, List<MeasurementCustom>> tgsNearestSpineLocationsPred= null;

  //private IWLAdjustment.Type adjType= null;

  private List<MeasurementCustom> tgLocationWLData= null;

  /**
   * Comments please!
   */
  public WLAdjustmentTideGauge() {
    super();

    //this.wlOriginalData=
    //  this.wlAdjustedData= null;
  }


  /**
   * Comments please!
   */
  public WLAdjustmentTideGauge(/*@NotNull*/ final HashMap<String,String> argsMap) {

    super(IWLAdjustment.Type.TideGauge, argsMap);

    final String mmi=
      "WLAdjustmentTideGauge(final WLAdjustment.Type adjType, final Map<String,String> argsMap) constructor ";

    slog.info(mmi+"start: this.locationIdInfo="+this.locationIdInfo); //wdsLocationIdInfoFile="+wdsLocationIdInfoFile);

    if (!argsMap.keySet().contains("--tideGaugeLocationsDefFileName")) {
      throw new RuntimeException(mmi+
        "Must have the --tideGaugeLocationsDefFileName=<tide gauges definition file name> defined in argsMap");
    }

    final String tideGaugeLocationsDefFileName= argsMap.get("--tideGaugeLocationsDefFileName");

    if (!argsMap.keySet().contains("--tideGaugeInputDataFile")) {
      throw new RuntimeException(mmi+
         "Must have the --tideGaugeInputDataFile=<complete path to the tide gauge input data file> defined in argsMap");
    }

    final String tideGaugeInputDataFile= argsMap.get("--tideGaugeInputDataFile");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Now find the two nearest CHS tide gauges from this WDS grid point location
    final String tideGaugesInfoFile= WLToolsIO.getMainCfgDir() + File.separator +
      IWLAdjustmentIO.TIDE_GAUGES_INFO_FOLDER_NAME + File.separator + tideGaugeLocationsDefFileName ;

    slog.info(mmi+"tideGaugesInfoFile="+tideGaugesInfoFile);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

   //final JsonObject spineLocationInfoJsonObj=
   //   this.getSpineJsonLocationIdInfo( spineLocationIdInfoFile );

    //this.adjLocationZCVsVDatum= spineLocationInfoJsonObj.
    //  getJsonNumber(StageIO.LOCATION_INFO_JSON_ZCIGLD_CONV_KEY).doubleValue();

    //this.adjLocationLatitude= spineLocationInfoJsonObj.
    //  getJsonNumber(StageIO.LOCATION_INFO_JSON_LATCOORD_KEY).doubleValue();

    //this.adjLocationLongitude= spineLocationInfoJsonObj.
    //  getJsonNumber(StageIO.LOCATION_INFO_JSON_LONCOORD_KEY).doubleValue();

    //this.relevantSpineClustersInfo= new HashMap<String, Map<String,HBCoords>>();

    //slog.info(mmi+"tide gauge adjustment location IGLD to ZC conversion value="+this.adjLocationZCVsVDatum);
    //slog.info(mmi+"tide gauge adjustment location coordinates=("+this.adjLocationLatitude+","+this.adjLocationLongitude+")");
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    FileInputStream jsonFileInputStream= null;

    try {
      jsonFileInputStream= new FileInputStream(tideGaugesInfoFile);

    } catch (FileNotFoundException e) {
      throw new RuntimeException(mmi+"e");
    }

    final JsonObject mainJsonMapObj= Json.
      createReader(jsonFileInputStream).readObject();

    //double minDistRad= Double.MAX_VALUE;

    // String [] twoNearestTideGaugesIds= {null, null};
    //Map<Double,String> tmpDistCheck= new HashMap<Double,String>();

    final Set<String> tgStrNumIdKeysSet= mainJsonMapObj.keySet();

    if (!tgStrNumIdKeysSet.contains(this.locationIdInfo)) {
      throw new RuntimeException(mmi+"Invalid tide gauge id -> "+this.locationIdInfo+
                                 " !! Must be one of ->"+tgStrNumIdKeysSet.toString());
    }

    //slog.info(mmi+"tgStrNumIdKeysSet.toString()="+tgStrNumIdKeysSet.toString());
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    //--- Get the tide gauge ZC conversion (-this.adjLocationZCVsVDatum to convert to ZC)
    //    from the json file.
    this.adjLocationZCVsVDatum=
      mainJsonMapObj.getJsonObject(this.locationIdInfo).
        getJsonNumber(StageIO.LOCATION_INFO_JSON_ZCIGLD_CONV_KEY).doubleValue();

    slog.info(mmi+"this.adjLocationZCVsVDatum="+this.adjLocationZCVsVDatum);
    //final JsonObject test= mainJsonMapObj.getJsonObject(this.locationIdInfo);
    //slog.info(mmi+"test:"+test.toString());

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- We can close the tide gauges info Json file now
    try {
      jsonFileInputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(mmi+e);
    }


    slog.info(mmi+" Getting inputDataType -> "+this.inputDataType.name()+
              " using the "+this.inputDataFormat.name()+" file format");

    // --- TODO: replace this if-else block by a switch-case block ??
    if (this.inputDataType == IWLAdjustmentIO.InputDataType.ECCC_H2D2) {

      if (this.modelInputDataDef == null) {
        throw new RuntimeException(mmi+"this.modelInputDataDef attribute cannot be null at this point for the"+
           IWLAdjustmentIO.InputDataType.ECCC_H2D2.name()+" input data type");
      }

      if (this.inputDataFormat == IWLAdjustmentIO.InputDataTypesFormatsDef.ASCII) {

        // --- Just need the tide gauge CHS Id. for the getH2D2ASCIIWLFProbesData
        //     method call.
        final Map<String, HBCoords> uniqueTGMapObj= new HashMap<String, HBCoords>();

        uniqueTGMapObj.put(this.locationIdInfo, null);

        this.getH2D2ASCIIWLFProbesData(uniqueTGMapObj, mainJsonMapObj); //nearestsTGEcccIds);

      } else {
        throw new RuntimeException(mmi+"Invalid inputDataFormat -> "+this.inputDataFormat.name()+
                                    " for inputDataType ->"+this.inputDataType.name()+" !!");
      }

      //if (this.inputDataFormat == IWLAdjustmentIO.InputDataTypesFormatsDef.NETCDF) {
      //  this.getH2D2NearestGPNCDFWLData(nearestsTGCoords);
      //
      //} else {
      //  throw new RuntimeException(mmi+"Invalid inputDataFormat -> "+this.inputDataFormat.name()+" !!");
      //}

    } else if ( this.inputDataType == IWLAdjustmentIO.InputDataType.CHS_PREDICTION) {

      if (this.inputDataFormat == IWLAdjustmentIO.InputDataTypesFormatsDef.JSON) {

        this.tgLocationWLData=
          this.getWLPredDataInJsonFmt(tideGaugeInputDataFile);

      } else {
        throw new RuntimeException(mmi+"Invalid inputDataFormat -> "+this.inputDataFormat.name()+
                                    " for inputDataType ->"+this.inputDataType.name()+" !!");
      }

    } else if (this.inputDataType == IWLAdjustmentIO.InputDataType.CHS_DHP_S104) {

      throw new RuntimeException(mmi+" inputDataType -> "+
                                 IWLAdjustmentIO.InputDataType.CHS_DHP_S104.name()+" not ready to be used yet!!");

    } else {
      throw new RuntimeException(mmi+"Invalid inputDataType -> "+this.inputDataType.name());
    }

    slog.info(mmi+"Done with reading the input data now");

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
