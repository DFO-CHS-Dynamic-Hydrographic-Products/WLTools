//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl;


/**
 *
 */

public interface IWLLocation {

  String ID_SPLIT_CHAR= ":";

  String INFO_JSON_FNAME_EXT= ".json";

  String INFO_JSON_LATCOORD_KEY= "lat";
  String INFO_JSON_LONCOORD_KEY= "lon";

  String INFO_JSON_ZCIGLD_CONV_KEY= "zcVsIGLD"; //--- Implies that tha the preductions are referred to IGLD datum
                                                         //    WL(IGLD) - zcVsIGLD to get the ZC values

  String INFO_JSON_DICT_KEY= "channelGridPointInfo"; // --- The json dict key for the station info dict

  abstract public IWLLocation setConfig(final JsonObject );
}
