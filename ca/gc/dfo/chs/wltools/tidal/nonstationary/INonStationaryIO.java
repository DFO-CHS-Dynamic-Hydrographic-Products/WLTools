package ca.gc.dfo.chs.wltools.tidal.nonstationary;

/**
 * Created on 2023-07-20.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 */

import ca.gc.dfo.chs.wltools.tidal.ITidalIO;

// ---
public interface INonStationaryIO extends ITidalIO {

   String STAGE_JSON_DICT_KEY= "Stage";

   String STAGE_JSON_D0_KEY= "s0"; // --- The stage zero order coefficient json dict key. (WL average or Z0 in classic HA jargon)

   String STAGE_JSON_DNFCT_KEYS= "s*.factor";  // --- The stage order>=1 coefficient factors json dict keys.
   String STAGE_JSON_DNLAG_KEYS= "s*.hoursLag"; // --- The stage order>=1 coefficient time lags (in hours) json dict keys.

   String TIDAL_CONSTS_JSON_DICT_KEY= "Fluvial+Tidal"; // --- The json dict key for the non-stationary tidal constituents data.

   String TIDAL_CONSTS_JSON_AMP_KEYS= "CS*.amp"; // --- The json dict keys for the amplitudes values
   String TIDAL_CONSTS_JSON_PHA_KEYS= "CS*.pha"; // --- The json dict keys for the phases (greenwich lagged) values

   String STATION_INFO_JSON_DICT_KEY= "channelGridPointInfo"; // --- The json dict key for the station info dict.

   String STATION_INFO_JSON_LATCOORD_KEY= "lat";
   String STATION_INFO_JSON_LONCOORD_KEY= "lon";

   String STATION_INFO_JSON_ZCIGLD_CONV_KEY= "zcVsIGLD"; //--- Implies that tha the preductions are referred to IGLD datum
                                                         //    WL(IGLD) - zcVsIGLD to get the ZC values
}



