package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.lang.Enum;
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

  // --- Default IWLAdjustment.TideGaugeAdjMethod is IWLAdjustment.TideGaugeAdjMethod.CHS_IWLS_QC
  //     for the prediction (WLP) data.
  private IWLAdjustment.TideGaugeAdjMethod
    predictAdjType= IWLAdjustment.TideGaugeAdjMethod.CHS_IWLS_QC;

  // --- Default IWLAdjustment.TideGaugeAdjMethod is IWLAdjustment.TideGaugeAdjMethod.ECCC_H2D2_FORECAST_AUTOREG
  //     for the forecast (WLF) data.
  private IWLAdjustment.TideGaugeAdjMethod
    forecastAdjType= IWLAdjustment.TideGaugeAdjMethod.ECCC_H2D2_FORECAST_AUTOREG;

  //private List<MeasurementCustom> tgLocationWLOData= null;

  // ---
  private List<MeasurementCustom> tgLocationWLPData= null;

  // ---
  //private List<MeasurementCustom> tgLocationWLFData= null;

  /**
   * Comments please!
   */
  public WLAdjustmentTideGauge() {
    super();

   this.tgLocationWLPData= null;

    //this.tgLocationWLOData=
    //  this.tgLocationWLPData=
    //    this.tgLocationWLFData = null;
  }


  /**
   * Comments please!
   */
  public WLAdjustmentTideGauge(/*@NotNull*/ final HashMap<String,String> argsMap) {

    super(IWLAdjustment.Type.TideGauge, argsMap);

    final String mmi=
      "WLAdjustmentTideGauge(final WLAdjustment.Type adjType, final Map<String,String> argsMap) constructor ";

    slog.info(mmi+"start: this.locationIdInfo="+this.locationIdInfo); //wdsLocationIdInfoFile="+wdsLocationIdInfoFile);

    final Set<String> argsMapKeysSet =argsMap.keySet();

    if (!argsMapKeysSet.contains("--tideGaugeLocationsDefFileName")) {
      throw new RuntimeException(mmi+
        "Must have the --tideGaugeLocationsDefFileName=<tide gauges definition file name> defined in argsMap");
    }

    final String tideGaugeLocationsDefFileName= argsMap.get("--tideGaugeLocationsDefFileName");

    if (!argsMapKeysSet.contains("--tideGaugePredictInputDataFile")) {
      throw new RuntimeException(mmi+
         "Must have the --tideGaugePredictInputDataFile=<complete path to the tide gauge WLP input data file> defined in argsMap");
    }

    final String tideGaugePredictInputDataFile= argsMap.get("--tideGaugePredictInputDataFile");

    //if (!argsMapKeysSet.contains("--tideGaugeForecastInputDataFile")) {
    //  throw new RuntimeException(mmi+
    //     "Must have the --tideGaugeForecastInputDataFile=<complete path to the tide gauge WLF input data file> defined in argsMap");
    //}
    //final String tideGaugeForecastInputDataFile
    //this.modelInputDataDef= argsMap.get("--tideGaugeForecastInputDataFile");

    if (!argsMapKeysSet.contains("--tideGaugeWLODataFile")) {
      throw new RuntimeException(mmi+
         "Must have the --tideGaugeInputDataFile=<complete path to the tide gauge input WLO data file> defined in argsMap");
    }

    final String tideGaugeWLODataFile= argsMap.get("--tideGaugeWLODataFile");

    slog.info(mmi+"tideGaugeWLODataFile="+tideGaugeWLODataFile);
    slog.info(mmi+"this.modelInputDataDef="+this.modelInputDataDef);
    //slog.info(mmi+"tideGaugeForecastInputDataFile="+tideGaugeForecastInputDataFile);
    slog.info(mmi+"tideGaugePredictInputDataFile="+tideGaugePredictInputDataFile);

    if (argsMapKeysSet.contains("--tideGaugeAdjMethods")) {

      final String [] tideGaugeAdjMethodCheck= argsMap.
        get("--tideGaugeAdjMethods").split(IWLAdjustmentIO.INPUT_DATA_FMT_SPLIT_CHAR);

      if (!IWLAdjustment.allowedTideGaugeAdjMethods.contains(tideGaugeAdjMethodCheck[0]) ) {
         throw new RuntimeException(mmi+"Invalid tide gauge WL adjustment method -> "+tideGaugeAdjMethodCheck[0]+
                                    " Must be one of -> "+IWLAdjustment.allowedTideGaugeAdjMethods.toString());
      }

      this.predictAdjType= IWLAdjustment.
        TideGaugeAdjMethod.valueOf(tideGaugeAdjMethodCheck[0]);

      if (tideGaugeAdjMethodCheck.length == 2) {

        if (!IWLAdjustment.allowedTideGaugeAdjMethods.contains(tideGaugeAdjMethodCheck[1]) ) {
          throw new RuntimeException(mmi+"Invalid tide gauge WL adjustment method -> "+tideGaugeAdjMethodCheck[1]+
                                    " Must be one of -> "+IWLAdjustment.allowedTideGaugeAdjMethods.toString() );
        }

        this.forecastAdjType= IWLAdjustment.
          TideGaugeAdjMethod.valueOf(tideGaugeAdjMethodCheck[1]);
      }
    }

    if (this.predictAdjType != IWLAdjustment.TideGaugeAdjMethod.CHS_IWLS_QC) {
      slog.info(mmi+"Only the tide gauge WL prediction adjustment type -> "+
                IWLAdjustment.TideGaugeAdjMethod.CHS_IWLS_QC.name()+" is allowed for now !");
    }

    if ( this.forecastAdjType != null ) {

      if (this.forecastAdjType != IWLAdjustment.TideGaugeAdjMethod.ECCC_H2D2_FORECAST_AUTOREG) {
        slog.info(mmi+"Only the tide gauge WL forecast adjustment type -> "+
                IWLAdjustment.TideGaugeAdjMethod.ECCC_H2D2_FORECAST_AUTOREG.name()+"is allowed for now !");
      }
    }

    slog.info(mmi+"this.predictAdjType="+this.predictAdjType.name());
    slog.info(mmi+"this.forecastAdjType="+this.forecastAdjType.name());
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Now find the two nearest CHS tide gauges from this WDS grid point location
    final String tideGaugesInfoFile= WLToolsIO.getMainCfgDir() + File.separator +
      IWLAdjustmentIO.TIDE_GAUGES_INFO_FOLDER_NAME + File.separator + tideGaugeLocationsDefFileName ;

    slog.info(mmi+"tideGaugesInfoFile="+tideGaugesInfoFile);
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

    slog.info(mmi+"Reading the prediction data using "+
               this.predictInputDataFormat.name()+" format for inputDataType -> "+this.inputDataType.name());

    if (this.predictInputDataFormat == IWLAdjustmentIO.DataTypesFormatsDef.JSON ) {
      this.tgLocationWLPData=
        this.getWLPredDataInJsonFmt(tideGaugePredictInputDataFile);
    }

    slog.info(mmi+"Done with reading prediction input data from file -> "+tideGaugePredictInputDataFile);
    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

    // ---
    if (this.forecastAdjType != null) {

      if (this.modelInputDataDef == null) {
        throw new RuntimeException(mmi+
                  "this.modelInputDataDef attribute cannot be null at this point if this.forecastAdjType is not null !");
      }

      if (this.forecastInputDataFormat == IWLAdjustmentIO.DataTypesFormatsDef.ECCC_H2D2_ASCII) {

        // --- Just need the tide gauge CHS Id. for the getH2D2ASCIIWLFProbesData
        //     method call.
        final Map<String, HBCoords> uniqueTGMapObj= new HashMap<String, HBCoords>();

        uniqueTGMapObj.put(this.locationIdInfo, null);

        this.getH2D2ASCIIWLFProbesData(uniqueTGMapObj, mainJsonMapObj); //nearestsTGEcccIds);

      } else {
        throw new RuntimeException(mmi+"Invalid this.forecastInputDataFormat -> "
                                   +this.forecastInputDataFormat.name()+" for inputDataType ->"+this.inputDataType.name()+" !!");
      }
    }
      //if (this.inputDataFormat == IWLAdjustmentIO.InputDataTypesFormatsDef.NETCDF) {
      //  this.getH2D2NearestGPNCDFWLData(nearestsTGCoords);
      //
      //} else {
      //  throw new RuntimeException(mmi+"Invalid inputDataFormat -> "+this.inputDataFormat.name()+" !!");
      //}

    //} else if ( this.inputDataType == IWLAdjustmentIO.InputDataType.CHS_PREDICTION) {
    //
    //  if (this.inputDataFormat == IWLAdjustmentIO.InputDataTypesFormatsDef.JSON) {
    //
    //    this.tgLocationWLPData=
    //      this.getWLPredDataInJsonFmt(tideGaugePredictInputDataFile);
    //
    //  } else {
    //    throw new RuntimeException(mmi+"Invalid inputDataFormat -> "+this.inputDataFormat.name()+
    //                                " for inputDataType ->"+this.inputDataType.name()+" !!");
    //}

     //} else if (this.inputDataType == IWLAdjustmentIO.InputDataType.CHS_DHP_S104) {
     //
     // throw new RuntimeException(mmi+" inputDataType -> "+
     //                            IWLAdjustmentIO.InputDataType.CHS_DHP_S104.name()+" not ready to be used yet!!");

    //} else {
    //  throw new RuntimeException(mmi+"Invalid inputDataType -> "+this.inputDataType.name());
    //}

    slog.info(mmi+"Done with reading the WL input data to adjust now");

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
