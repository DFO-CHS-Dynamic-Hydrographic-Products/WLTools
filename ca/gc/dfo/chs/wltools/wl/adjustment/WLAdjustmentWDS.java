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

  public WLAdjustmentWDS(/*@NotNull*/ final String wdsLocationIdInfoFile) {

    final String mmi= "WLAdjustmentWDS(final String wdsLocationIdInfoFile) constructor ";

    slog.info(mmi+"start: wdsLocationIdInfoFile="+wdsLocationIdInfoFile);

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

