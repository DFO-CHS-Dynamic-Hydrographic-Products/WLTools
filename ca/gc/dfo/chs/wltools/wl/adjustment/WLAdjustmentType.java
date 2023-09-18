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

     //if (!this.argsMapKeySet.contains("--inputDataType")) {
    //  throw new RuntimeException(mmi+"Must have the mandatory option: --inputDataType defined !!");
    //}

    //final String [] inputDataTypeFmtSplit= argsMap.
    //  get("--inputDataType").split(IWLAdjustmentIO.INPUT_DATA_TYPE_SPLIT_STR);//INPUT_DATA_FMT_SPLIT_CHAR);

    //final String checkInputDataType= inputDataTypeFmtSplit[0];
    //final String checkInputDataFormat= inputDataTypeFmtSplit[1];

    //if (!IWLAdjustmentIO.allowedDataTypes.contains(checkInputDataType)) {
    //  throw new RuntimeException(mmi+"Invalid input data type -> "+checkInputDataType+
    //                             " ! must be one of -> "+IWLAdjustmentIO.allowedDataTypes.toString());
    //}

    //this.inputDataType= IWLAdjustmentIO.
    //  DataType.valueOf(checkInputDataType);

    //final String checkFormatForInputDataType= inputDataTypeFmtSplit[1];

    //final Set<String> allowedFormatsForInputDataType=
    //  DataTypesFormats.get(this.inputDataType.name());

    ///if (!allowedFormatsForInputDataType.contains(checkFormatForInputDataType)) {
    //
    //  throw new RuntimeException(mmi+"Invalid input data format -> "+
    //                             checkFormatForInputDataType+" for input data type -> "+
    //                             this.inputDataType.name()+" ! must be one of -> "+allowedFormatsForInputDataType.toString());
    //}

    //slog.info(mmi+"checkInputDataFormat="+checkInputDataFormat);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- We can have 2 different input data formats:
    //     one for predictions and possibly another one for model forecast.
    //final String [] checkInputDataFormats=
    //  checkFormatForInputDataType.split(IWLAdjustmentIO.INPUT_DATA_FMT_SPLIT_CHAR);

    //if (!allowedInputFormats.contains(checkInputDataFormats[0])) {
    //   throw new RuntimeException(mmi+
    //               "Invalid input data format for predictions -> "+checkInputDataFormats[0]);
    //}

    //this.predictInputDataFormat= IWLAdjustmentIO.
    //  DataTypesFormatsDef.valueOf(checkInputDataFormats[0]);

    //slog.info(mmi+"Using input data type -> "+ this.inputDataType.name()+
    //  " with prediction data format -> "+this.predictInputDataFormat.name());

    //if ( checkInputDataFormats.length == 2) {
    //  if (!allowedInputFormats.contains(checkInputDataFormats[1])) {
    //    throw new RuntimeException(mmi+
    //         "Invalid input data format for model forecast -> "+checkInputDataFormats[0]);
    //  }
    //
    //  this.forecastInputDataFormat= IWLAdjustmentIO.
    //    DataTypesFormatsDef.valueOf(checkInputDataFormats[1]);
    //  slog.info(mmi+"input data format for model forecast -> "+this.forecastInputDataFormat.name());
    //}

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

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
      slog.info(mmi+"Debug System.exit(0)");
      System.exit(0);
    }

    //final String inputDataFilesPathsDef= argsMap.get("--modelInputDataDef");
    //slog.info(mmi+"inputDataFilesPathsDef="+inputDataFilesPathsDef);
    // --- Here the inputDataFilesPathsDef value must be the path of an ASCII file that defines the
    //     complete paths of all the input data files themselves (the number of input files can be
    //     a large as 5000 so we cannot pass all their paths in the arguments)
    //this.modelInputDataFiles=
    //  ASCIIFileIO.getFileLinesAsArrayList(inputDataFilesPathsDef);
    //slog.info(mmi+"Will use "+this.modelInputDataFiles.size()+" model input data files");

    //final String firstInputFile= this.inputDataFilesPaths.get(0);
    //slog.info(mmi+"Getting the grid points information
    //for (final String inputFilePath: this.inputDataFilesPaths) {
    //   slog.info(mmi+"Processing inputFilePath="+inputFilePath);
    //   slog.info(mmi+"Debug System.exit(0)");
    //   System.exit(0);
    //}
    //}

    slog.info(mmi+"end");

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);
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

