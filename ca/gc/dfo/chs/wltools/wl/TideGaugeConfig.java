//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl;

/**
 *
 */

//import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

import ca.gc.dfo.chs.wltools.wl.WLLocation;
//import ca.gc.dfo.chs.wltools.wl.IWLLocation;
import ca.gc.dfo.chs.wltools.wl.ITideGaugeConfig;
//import ca.gc.dfo.chs.wltools.util.HBCoords;

/**
 *
 */
final public class TideGaugeConfig extends WLLocation implements ITideGaugeConfig {

  private String hrName= null; // --- Human readable TG name string (e.g. "Sorel")

  private String ECCCId= null; // --- The ECCC ADE numeric string id (e.g. "30316")

  private String nearestSpinePointId= null;

  private String upstreamNeighTGId= null;
  private String downstreamNeighTGId= null;

  private JsonObject fmsJsonObject= null;

  private boolean configIsOkay= false;

  public TideGaugeConfig(final String identity) {
    super(identity);

    this.configIsOkay= false;
  }

  public TideGaugeConfig setConfig(final JsonObject tgJsonObj) {

    super.setConfig(tgJsonObj);

    this.ECCCId= tgJsonObj.getString(INFO_ECCC_ID_JSON_KEY);
    this.hrName = tgJsonObj.getString(INFO_HUMAN_READABLE_NAME_JSON_KEY);

    this.nearestSpinePointId=
      tgJsonObj.getString(INFO_NEAREST_SPINE_POINT_ID_JSON_KEY);

    this.upstreamNeighTGId=
      tgJsonObj.getString(INFO_UPSTREAM_NEIGH_TGID_JSON_KEY);

    this.downstreamNeighTGId=
      tgJsonObj.getString(INFO_DOWNSTREAM_NEIGH_TGID_JSON_KEY);

    this.fmsJsonObject= tgJsonObj.getJsonObject(INFO_FMS_CONFIG_JSON_KEY);

    //this.zcVsVertDatum= tgJsonObj.
    //  getJsonNumber(IWLConfig.LOCATION_INFO_JSON_ZCIGLD_CONV_KEY).doubleValue();

    //this.setHBCoords(tgJsonObj.getJsonNumber(IWLLocation.INFO_JSON_LONCOORD_KEY).doubleValue(),
    //                 tgJsonObj.getJsonNumber(IWLLocation.INFO_JSON_LATCOORD_KEY).doubleValue());

    this.configIsOkay= true;
    
    return this;
  }

  public boolean isConfigOkay() {
    return this.configIsOkay;
  }	

  public String getHrName() {
    return this.hrName;
  }

  public String getECCCId() {
    return this.ECCCId;
  }

  public String getNearestSpinePointId() {
    return this.nearestSpinePointId;
  }

  public JsonObject getFmsJsonObject () {
    return this.fmsJsonObject;
  }
}
