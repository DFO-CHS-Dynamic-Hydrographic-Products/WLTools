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
import ca.gc.dfo.chs.wltools.nontidal.stage.StageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;

/**
 * Comments please!
 */
final public class WLAdjustment extends WLAdjustmentIO implements IWLAdjustment { //, extends IWLAdjustmentIO {//, IWLAdjustmentIO { //extends <>

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustment";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  //private double adjZCVsVDatum= 0.0;  // --- Subtract from the WL data to get the local ZC value OR add to WL data to have defined w.r. to its global vertical datum)
  //private double adjLocationLatitude= 0.0;
  //private double adJlocationLongitude= 0.0;

  //private ArrayList<WLMeasurement> wlOriginalData= null;
  //private ArrayList<WLMeasurement> wlAdjustedData= null;

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

    if (!argsMapKeySet.contains("--locationAdjType")) {

      throw new RuntimeException(mmi+"Must have the mandatory option: --locationAdjType defined !!");
    }

    final String locationAdjType= argsMap.get("--locationAdjType");

    if (!IWLAdjustment.allowedTypes.contains(locationAdjType)) {
      throw new RuntimeException(mmi+"Invalid WL location adjustment type -> "+locationAdjType+
                                 " ! Must be one of -> "+IWLAdjustment.allowedTypes.toString());
    }

    if (locationAdjType.equals(Type.IWLS.name())) {
      throw new RuntimeException(mmi+"The WL location adjustment type "+
                                 Type.IWLS.name()+" is not yet ready to be used !!");
    }

    if (locationAdjType.equals(Type.MODEL_BARYCENTRIC.name())) {
      throw new RuntimeException(mmi+"The WL location adjustment type "+
                                 Type.MODEL_BARYCENTRIC.name()+" is not yet ready to be used !!");
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

    final String [] locationIdInfoSplit=
      locationIdInfo.split(IWLAdjustmentIO.INPUT_DATA_FMT_SPLIT_CHAR);

    final String locationType= locationIdInfoSplit[0];

    if (!IWLAdjustmentIO.allowedLocationTypes.contains(locationType)) {

       throw new RuntimeException(mmi+"Invalid WL adjustement location type -> "+locationType+
                                  " !, must be one of -> "+IWLAdjustmentIO.allowedLocationTypes.toString());
    }

    if (locationType.equals(IWLAdjustmentIO.LocationType.IWLS.name())) {
      throw new RuntimeException(mmi+" Sorry! WL adjustement location type -> "+
                                 locationType+" not ready to be used for now !");
    }

    slog.info(mmi+"Will use WL location adjustment type "+locationAdjType);

    slog.info(mmi+"Will use input data type -> "+
              inputDataType+" with input data format -> "+inputDataFormat);

    slog.info(mmi+"Will use location Id info  -> "+locationIdInfo);

    if (locationType.equals(IWLAdjustmentIO.LocationType.WDS.name())) {

      final String wdsLocationIdInfoFile=
        WLToolsIO.getMainCfgDir()+"/"+locationIdInfoSplit[1];

      slog.info(mmi+"wdsLocationIdInfoFile="+wdsLocationIdInfoFile);

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
    }

    slog.info(mmi+"Test dist. rad="+Trigonometry.getDistanceInRadians(-73.552528,45.5035,-73.5425,45.528667));

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);

    slog.info(mmi+"end");
  }

}

