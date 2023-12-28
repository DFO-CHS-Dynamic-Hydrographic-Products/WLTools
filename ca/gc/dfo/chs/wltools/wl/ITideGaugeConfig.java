//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl;

import ca.gc.dfo.chs.wltools.wl.IWLLocation;

/**
 *
 */

public interface ITideGaugeConfig extends IWLLocation {

  String INFO_FOLDER_NAME= "tideGaugeInfo";

  String INFO_ECCC_ID_JSON_KEY= "ECCC_ID";
  String INFO_FMS_CONFIG_JSON_KEY= "FMSConfig";
  String INFO_HUMAN_READABLE_NAME_JSON_KEY= "HRName";
  String INFO_UPSTREAM_NEIGH_TGID_JSON_KEY= "upstreamTG";
  String INFO_DOWNSTREAM_NEIGH_TGID_JSON_KEY= "downstreamTG";
  String INFO_NEAREST_SPINE_POINT_ID_JSON_KEY= "nearestSpinePointId";

  String INFO_NEIGH_TGID_UNDEFINED= "None";
}
