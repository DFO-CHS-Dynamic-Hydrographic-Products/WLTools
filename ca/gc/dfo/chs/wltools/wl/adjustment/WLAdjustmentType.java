package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.util.Map;
import java.util.Set;
import java.util.List;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.HashMap;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

// ---
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.nontidal.stage.StageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
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
  public WLAdjustmentType(/*NotNull*/ final HashMap<String,String> argsMap) {

    super(argsMap);

    final String mmi=
      "WLAdjustmentType(final HashMap<String,String> mainProgramOptions) constructor: ";

    slog.info(mmi+"start");

    if (!this.argsMapKeySet.contains("--locationIdInfo")) {
      throw new RuntimeException(mmi+"Must have the mandatory option: --locationIdInfo defined !!");
    }

    this.locationIdInfo= argsMap.get("--locationIdInfo");

    if (!this.argsMapKeySet.contains("--inputDataType")) {
      throw new RuntimeException(mmi+"Must have the mandatory option: --inputDataType defined !!");
    }

    final String [] inputDataTypeFmtSplit= argsMap.
      get("--inputDataType").split(IWLAdjustmentIO.INPUT_DATA_FMT_SPLIT_CHAR);

    final String checkInputDataType= inputDataTypeFmtSplit[0];
    final String checkInputDataFormat= inputDataTypeFmtSplit[1];

    if (!IWLAdjustmentIO.allowedInputDataTypes.contains(checkInputDataType)) {
      throw new RuntimeException(mmi+"Invalid input data type -> "+checkInputDataType+
                                 " ! must be one of -> "+IWLAdjustmentIO.allowedInputDataTypes.toString());
    }

    this.inputDataType= IWLAdjustmentIO.
      InputDataType.valueOf(checkInputDataType);

    final Set<String> allowedInputFormats=
      InputDataTypesFormats.get(this.inputDataType.name());

    if (!allowedInputFormats.contains(checkInputDataFormat)) {
      throw new RuntimeException(mmi+"Invalid input data format ->"+checkInputDataFormat+" for input data type -> "+
                                 this.inputDataType.name()+" ! must be one of -> "+allowedInputFormats.toString());
    }

    this.inputDataFormat= IWLAdjustmentIO.
      InputDataTypesFormatsDef.valueOf(checkInputDataFormat);

    slog.info(mmi+"Will use input data type -> "+this.inputDataType.name()+
              " with input data format -> "+this.inputDataFormat.name());

    slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

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

