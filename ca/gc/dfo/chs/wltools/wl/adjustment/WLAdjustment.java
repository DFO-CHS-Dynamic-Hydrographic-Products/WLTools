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
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentWDS;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentType;

/**
 * Comments please!
 */
final public class WLAdjustment implements IWLAdjustment { // extends WLAdjustmentIO

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustment";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  //private IWLAdjustment.Type adjType= null;

  private IWLAdjustmentType adjInstance= null;

  /**
   * Comments please!
   */
  public WLAdjustment() {

    super();

    //this.wlOriginalData=
    //  this.wlAdjustedData= null;
  }

  /**
   * Parse the main program arguments using a constructor.
   */
  public WLAdjustment(/*NotNull*/ final HashMap<String,String> argsMap) {

    final String mmi=
      "WLAdjustment(final HasMap<String,String> mainProgramOptions) constructor: ";

    slog.info(mmi+"start");

    final Set<String> argsMapKeySet= argsMap.keySet();

    if (!argsMapKeySet.contains("--adjType")) {
      throw new RuntimeException(mmi+"Must have the mandatory option: --adjType defined !!");
    }

    final String adjType= argsMap.get("--adjType");

    if (!IWLAdjustment.allowedTypes.contains(adjType)) {
      throw new RuntimeException(mmi+"Invalid WL adjustment type -> "+adjType+
                                 " ! Must be one of -> "+IWLAdjustment.allowedTypes.toString());
    }

    //if (locationAdjType.equals(Type.IWLS.name())) {
    //  throw new RuntimeException(mmi+"The WL location adjustment type "+
    //                             Type.IWLS.name()+" is not yet ready to be used !!");
    //}

    if (adjType.equals(Type.MODEL_BARYCENTRIC.name())) {
      throw new RuntimeException(mmi+"The WL adjustment type "+
                                 Type.MODEL_BARYCENTRIC.name()+" is not yet ready to be used !!");
    }

    if (adjType.equals(Type.MODEL_NEAREST_NEIGHBOR.name())) {
      throw new RuntimeException(mmi+"The WL adjustment type "+
                                 Type.MODEL_NEAREST_NEIGHBOR.name()+" is not yet ready to be used !!");
    }

    //slog.info(mmi+"Will use WL location adjustment type "+locationAdjType);

    if (!argsMapKeySet.contains("--inputDataType")) {
      throw new RuntimeException(mmi+"Must have the mandatory option: --inputDataType defined !!");
    }

    final String [] inputDataTypeFmtSplit= argsMap.
      get("--inputDataType").split(IWLAdjustmentIO.INPUT_DATA_FMT_SPLIT_CHAR);

    final String inputDataType= inputDataTypeFmtSplit[0];
    final String inputDataFormat= inputDataTypeFmtSplit[1];

    if (!IWLAdjustmentIO.allowedInputDataTypes.contains(inputDataType)) {
      throw new RuntimeException(mmi+"Invalid input data type -> "+inputDataType+
                                 " ! must be one of -> "+IWLAdjustmentIO.allowedInputDataTypes.toString());
    }

    final Set<String> allowedInputFormats=
      IWLAdjustmentIO.InputDataTypesFormats.get(inputDataType);

    if (!allowedInputFormats.contains(inputDataFormat)) {
      throw new RuntimeException(mmi+"Invalid input data format ->"+inputDataFormat+
                                 " for input data type -> "+inputDataType+" ! must be one of -> "+allowedInputFormats.toString());
    }

    if (!argsMapKeySet.contains("--locationIdInfo")) {
      throw new RuntimeException(mmi+"Must have the mandatory option: --locationIdInfo defined !!");
    }

    final String locationIdInfo= argsMap.get("--locationIdInfo");

    //final String [] locationIdInfoSplit=
    //  locationIdInfo.split(IWLAdjustmentIO.INPUT_DATA_FMT_SPLIT_CHAR);

    //final String checkLocationType= locationIdInfoSplit[0];

    //if (!IWLAdjustmentIO.allowedLocationTypes.contains(checkLocationType)) {
    //  throw new RuntimeException(mmi+"Invalid WL adjustement location type -> "+checkLocationType+
    //                             " !, must be one of -> "+IWLAdjustmentIO.allowedLocationTypes.toString());
    //}

    //if (checkLocationType.equals(IWLAdjustmentIO.LocationType.IWLS.name())) {
    //  throw new RuntimeException(mmi+" Sorry! WL adjustement location type -> "+
    //                             checkLocationType+" not ready to be used for now !");
    //}

    slog.info(mmi+"Will use WL adjustment type "+adjType);

    slog.info(mmi+"Will use input data type -> "+
              inputDataType+" with input data format -> "+inputDataFormat);

    slog.info(mmi+"Will use location Id info  -> "+locationIdInfo);

    if (adjType.equals(IWLAdjustment.Type.WDS.name())) {

      slog.info(mmi+"Doing WDS type WL adjustment setup");

      //this.adjType=IWLAdjustment.Type.WDS;

      final String wdsLocationIdInfoFile=
        WLToolsIO.getMainCfgDir() + "/"+ locationIdInfo;

      slog.info(mmi+"wdsLocationIdInfoFile="+wdsLocationIdInfoFile);

      this.adjInstance= new WLAdjustmentWDS(wdsLocationIdInfoFile);

      slog.info(mmi+"Done with WDS type WL adjustment setup");

    }

    //slog.info(mmi+"Test dist. rad="+Trigonometry.getDistanceInRadians(-73.552528,45.5035,-73.5425,45.528667));

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

    slog.info(mmi+"end");
  }

  /**
   * Comments please.
   */
  final public List<MeasurementCustom> getAdjustment() {

    //final String mmi= "getAdjustment: ";
    //List<MeasurementCustom> adjustmentRet= null;
    ///slog.info(mmi+"start: this.adjType.name()="+this.adjType.name());

    return this.adjInstance.getAdjustment();
  }
}

