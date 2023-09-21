//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl;

/**
 *
 */

import ca.gc.dfo.chs.wltools.wl.IWLConfig;
import ca.gc.dfo.chs.wltools.wl.WLLocation;
import ca.gc.dfo.chs.wltools.wl.ITideGaugeConfig;
//import ca.gc.dfo.chs.wltools.util.HBCoords;

//---
//import ca.gc.dfo.chs.wltools.numbercrunching.D1Data;
//import ca.gc.dfo.chs.wltools.numbercrunching.D2Data;

//import ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.D1Data;
//import ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.D2Data;

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

//---
//---

/**
 *
 */
public class TideGaugeConfig extends WLLocation implements ITideGaugeConfig {

  String hrName= null; // --- Human readable TG name string (e.g. "Sorel")

  String ECCC_id= null; // --- The ECCC ADE numeric string id (e.g. "30316")

  public TideGaugeConfig(final String identity) {
    super(identity);
  }

  public TideGaugeConfig setConfig(final JsonObject tgJsonObj) {

    super.setConfig(tgJsonObj);

    this.ECCC_id= tgJsonObj.getJsonString(INFO_ECCC_ID_JSON_KEY);
    this.hrName= tgJsonObj.getJsonString(INFO_HUMAN_READABLE_NAME_JSON_KEY);

    //this.zcVsVertDatum= tgJsonObj.
    //  getJsonNumber(IWLConfig.LOCATION_INFO_JSON_ZCIGLD_CONV_KEY).doubleValue();

    //this.setHBCoords(tgJsonObj.getJsonNumber(IWLLocation.INFO_JSON_LONCOORD_KEY).doubleValue(),
    //                  tgJsonObj.getJsonNumber(IWLLocation.INFO_JSON_LATCOORD_KEY).doubleValue());

    return this;
  }
}