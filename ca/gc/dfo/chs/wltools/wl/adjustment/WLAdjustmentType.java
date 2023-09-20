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
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.ASCIIFileIO;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.nontidal.stage.StageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
//import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO.InputDataType;

/**
 * Comments please!
 */
abstract public class WLAdjustmentType extends WLAdjustmentIO implements IWLAdjustmentType { // implements IWLAdjustment {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentType";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  //private IWLAdjustment.Type adjType= null;

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

    final String mmi=
      "WLAdjustmentType( final WLAdjustment.Type adjType, final HashMap<String,String> mainProgramOptions) constructor: ";

    slog.info(mmi+"start");

    if (!this.argsMapKeySet.contains("--locationIdInfo")) {
      throw new RuntimeException(mmi+"Must have the mandatory option: --locationIdInfo defined !!");
    }

    // --- Get only the base name of the
    this.locationIdInfo= argsMap.get("--locationIdInfo");

    // --- Get only the base name of the this.locationIdInfo file.
    this.locationId= new File(this.locationIdInfo).
      getName().replace(IWLStationPredIO.JSON_FEXT,"");

    if (this.adjType != IWLAdjustment.Type.TideGauge) {
      throw new RuntimeException(mmi+" Only the "+IWLAdjustment.Type.TideGauge.name()+" allowed for now !!");
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

  final public String getLocationId() {
    return this.locationId;
  }

  final public String getStormSurgeForecastModelName() {
    return this.stormSurgeForecastModelName;
  }

  final public List<MeasurementCustom> getLocationPredData() {
    return (List<MeasurementCustom>) this.locationPredData;
  }

  final public List<MeasurementCustom> getNearestObsData() {
    return (List<MeasurementCustom>) this.nearestObsData.get(this.locationId);
  }

  final public List<MeasurementCustom> getNearestObsData(final String locationId) {
    return (List<MeasurementCustom>) this.nearestObsData.get(locationId);
  }

  final public List<MeasurementCustom> getNearestModelData() {
    return (List<MeasurementCustom>) this.nearestModelData.get(this.locationId);
  }

  final public List<MeasurementCustom> getNearestModelData(final String locationId) {
    return (List<MeasurementCustom>) this.nearestModelData.get(locationId);
  }

  ///**
  // * Comments please.
  // */
  //final public List<MeasurementCustom> getAdjustment() {
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

