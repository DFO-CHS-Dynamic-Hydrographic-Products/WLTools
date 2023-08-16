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

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

// ---
import as.hdfql.HDFql;
import as.hdfql.HDFqlCursor;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;

/**
 * Comments please!
 */
public class WLAdjustmentIO implements IWLAdjustmentIO { //extends <>

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentIO";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  protected double adjLocationLatitude= 0.0;
  protected double adjLocationLongitude= 0.0;
  protected double adjLocationZCVsVDatum= 0.0;

  protected ArrayList<WLMeasurement> locationOriginalData= null;
  protected ArrayList<WLMeasurement> locationAdjustedData= null;

  protected Map<String, ArrayList<WLMeasurement>> nearestObsData= null;

  /**
   * Comments please!
   */
  public WLAdjustmentIO() {

    this.adjLocationZCVsVDatum=
      this.adjLocationLatitude=
        this.adjLocationLongitude= 0.0;

    this.locationOriginalData=
      this.locationAdjustedData= null;

    this.nearestObsData= null;
  }

  //public WLAdjustmentIO() {
  //}

  final static JsonObject getWDSLocationIdInfo( /*@NotNull*/ final String wdsLocationIdInfoFile) {

    final String mmi= "getWDSLocationIdInfo: ";

    Map<String,String> wdsLocationIdInfo= new HashMap<String,String>();

    //--- Deal with possible null tcInputfilePath String: if @NotNull not used
    try {
      wdsLocationIdInfoFile.length();

    } catch (NullPointerException e) {

      slog.error(mmi+"wdsLocationIdInfoFile is null !!");
      throw new RuntimeException(e);
    }

    slog.info(mmi+"start: wdsLocationIdInfoFile=" + wdsLocationIdInfoFile);

    FileInputStream jsonFileInputStream= null;

    try {
       jsonFileInputStream= new FileInputStream(wdsLocationIdInfoFile);

    } catch (FileNotFoundException e) {
       throw new RuntimeException(mmi+"e");
    }

    final JsonObject mainJsonTcDataInputObj= Json.
      createReader(jsonFileInputStream).readObject();  //tmpJsonTcDataInputObj;

    // --- TODO: add fool-proof checks on all the Json dict keys.

    final JsonObject wdsLocationIdInfoJsonObj=
      mainJsonTcDataInputObj.getJsonObject(IStageIO.LOCATION_INFO_JSON_DICT_KEY);

    slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    return wdsLocationIdInfoJsonObj;
  }
}

