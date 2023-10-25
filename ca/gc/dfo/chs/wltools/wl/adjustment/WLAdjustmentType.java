package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.HashMap;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

// ---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

// ---
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.wl.WLLocation;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.ASCIIFileIO;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.wl.TideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
//import ca.gc.dfo.chs.wltools.nontidal.stage.StageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentFMF;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
//import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO.InputDataType;

/**
 * Comments please!
 */
abstract public class WLAdjustmentType
  extends WLAdjustmentFMF implements IWLAdjustmentType {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentType";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  //private IWLAdjustment.Type adjType= null;

  // --- Default IWLAdjustment.TideGaugeAdjMethod is IWLAdjustment.TideGaugeAdjMethod.CHS_IWLS_QC
  //     for the prediction (WLP) data.
  protected IWLAdjustment.TideGaugeAdjMethod
    predictAdjType= IWLAdjustment.TideGaugeAdjMethod.CHS_IWLS_QC;

  // --- Default IWLAdjustment.TideGaugeAdjMethod is IWLAdjustment.TideGaugeAdjMethod.ECCC_H2D2_FORECAST_AUTOREG
  //     for the forecast (WLF) data.
  protected IWLAdjustment.TideGaugeAdjMethod forecastAdjType=
    IWLAdjustment.TideGaugeAdjMethod.SIMPLE_TIMEDEP_FMF_ERROR_STATS; //ECCC_H2D2_FORECAST_AUTOREG;

  /**
   * Comments please!
   */
  public WLAdjustmentType() {

    super();

    //this.wlOriginalData=
    //  this.wlAdjustedData= null;
  }

  /**
   * Parse the main program arguments using a constructor.
   */
  public WLAdjustmentType(/*NotNull*/ final WLAdjustment.Type adjType,
                          /*NotNull*/ final HashMap<String,String> argsMap) {

    super(adjType, argsMap);

    final String mmi= "WLAdjustmentType main constructor: ";

    slog.info(mmi+"start");

    if (!this.argsMapKeySet.contains("--locationIdInfo")) {
      throw new RuntimeException(mmi+"Must have the mandatory option: --locationIdInfo defined !!");
    }

    // --- Get only the base name of the
    this.locationIdInfo= argsMap.get("--locationIdInfo");

    // --- Get only the base name of the this.locationIdInfo file path.
    //this.locationId= new File(this.locationIdInfo).
    final String identity=
      new File(this.locationIdInfo).getName().replace(IWLStationPredIO.JSON_FEXT,"");

    if (this.adjType == IWLAdjustment.Type.TideGauge) {

      this.location= new TideGaugeConfig(identity);

    } else if (this.adjType == IWLAdjustment.Type.SpineIPP) {

      this.location= new WLLocation(identity);

      throw new RuntimeException(mmi+"SpineIPP adjustment type not ready yet !!");

    } else if (this.adjType == IWLAdjustment.Type.SpineFPP) {

      this.location= new WLLocation(identity);

      throw new RuntimeException(mmi+"SpineFPP adjustment type not ready yet !!");

    } else {
       throw new RuntimeException(mmi+"Invalid adjustment type "+this.adjType.name()+" !!");
    }

    // --- NOTE: --modelInputDataDef=<path> <path> could be the path of an ASCII file that contains
    //           all the needed model input data itself (eg. H2D2 WL probes forecast data) OR the
    //           path of an ASCII file that defines all the paths to the model WL forecast input data
    //           files that are needed (e.g. H2D2 NetCDF file).
    if (this.argsMapKeySet.contains("--modelForecastInputDataInfo")) {

      final String [] modelForecastInputDataInfo= argsMap.
        get("--modelForecastInputDataInfo").split(IWLAdjustmentIO.INPUT_DATA_FMT_SPLIT_CHAR);

      if (!allowedInputFormats.contains(modelForecastInputDataInfo[0])) {

        throw new RuntimeException(mmi+"Invalid model forecast Input Data file format -> "+
                                 modelForecastInputDataInfo[0]+" Must be one of -> "+allowedInputFormats.toString());
      }

      this.modelForecastInputDataFormat= IWLAdjustmentIO.
        DataTypesFormatsDef.valueOf(modelForecastInputDataInfo[0]);

      // --- modelInputDataInfo[1] could be: only one model forecast input data file OR an
      //     ASCII file that defines a list of at least two model forecast input data files
      this.modelForecastInputDataInfo= modelForecastInputDataInfo[1];

      slog.info(mmi+"this.modelForecastInputDataFormat="+this.modelForecastInputDataFormat.name());
      slog.info(mmi+"this.modelForecastInputDataInfo="+this.modelForecastInputDataInfo);
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
    }

    slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
  }

  final public WLLocation getLocation() {
    return this.location;
  }

  final public String getIdentity() {
    return this.location.getIdentity();
  }

  final public String getFullForecastModelName() {
    return this.fullForecastModelName;
  }

  final public List<MeasurementCustom> getLocationPredData() {
    return (List<MeasurementCustom>) this.locationPredData;
  }

  final public List<MeasurementCustom> getNearestObsData() {
    return (List<MeasurementCustom>) this.nearestObsData.get(this.location.getIdentity());
  }

  final public List<MeasurementCustom> getNearestObsData(final String locationId) {
    return (List<MeasurementCustom>) this.nearestObsData.get(locationId);
  }

  final public List<MeasurementCustom> getNearestModelData(final int whichType) {
    return (List<MeasurementCustom>) this.nearestModelData.get(whichType).get(this.location.getIdentity());
  }

  final public List<MeasurementCustom> getNearestModelData(final int whichType, final String locationId) {
    return (List<MeasurementCustom>) this.nearestModelData.get(whichType).get(locationId);
  }

  // ---
  final public WLAdjustmentType adjustFullModelForecast() {

    final String mmi= "adjustFullModelForecast: ";

    slog.info(mmi+"start");

    switch (this.forecastAdjType) {

      case SIMPLE_TIMEDEP_FMF_ERROR_STATS:

        this.simpleTimeDepFMFErrorStatsAdj();
        break;

      default:
       throw new RuntimeException(mmi+"Invalid this.forecastAdjType -> "+this.forecastAdjType.name());
    }

   slog.info(mmi+"end");
   slog.info(mmi+"Debug exit 0");
   System.exit(0);

    return this;
  }

  ///**
  // * Comments please.
  // */
  //public List<MeasurementCustom> getAdjustment(final String outputDirectory) { }
  //
  //  final String mmi= "getAdjustment: ";
  //
  //  //List<MeasurementCustom> adjustmentRet= null;
  //
  //  slog.info(mmi+"start: this.adjType.name()="+this.adjType.name());
  //
  //  if (this.adjType.equals(IWLAdjustment.Type.WDS.name())) {
  //
  //      slog.info(mmi+"Will do WLF adjustment of the WDS type");
  //
  //    // this.locationAdjustedData this.getWDSAdjustment();
  //
  //  }
  //
  //  slog.info(mmi+"end");
  //
  //  slog.info(mmi+"Debug System.exit(0)");
  //  System.exit(0);
  //
  //  return this.locationAdjustedData; //adjustmentRet;
  //}
}

