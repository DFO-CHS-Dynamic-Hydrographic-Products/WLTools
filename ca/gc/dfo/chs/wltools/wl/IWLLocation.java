//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl;

import javax.json.JsonObject;

/**
 *
 */

public interface IWLLocation {

  String ID_SPLIT_CHAR= ":";

  String INFO_JSON_FNAME_EXT= ".json";

  String INFO_JSON_LATCOORD_KEY= "lat";
  String INFO_JSON_LONCOORD_KEY= "lon";

  String INFO_JSON_ZCIGLD_CONV_KEY= "zcVsIGLD"; //--- WL(ZC) = WL(IGLD) - zcVsIGLD to convert from IGLD to ZC values
                                                //    OR WL(IGLD) = WL(ZC) + zcVsIGLD to convert from ZC to IGLD values

  String INFO_JSON_ZCIGLD_CONV_BOOL_KEY= "convFromZCToIGLD"; // --- To do (or not) the conversion from ZC to the IGLD vertical datum.

  String INFO_JSON_SPINE_FPP_BOOL_KEY= "doSpineFPP";

  String INFO_JSON_DICT_KEY= "channelGridPointInfo"; // --- The json dict key for the station info dict

  abstract public IWLLocation setConfig(final JsonObject jsonObject);
}
