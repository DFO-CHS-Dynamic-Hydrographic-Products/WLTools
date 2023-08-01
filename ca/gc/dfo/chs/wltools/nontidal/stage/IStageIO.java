package ca.gc.dfo.chs.wltools.nontidal.stage;

/**
 * Created on 2023-07-20.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 */

//import java.util.List;

//// ---
//import ca.gc.dfo.chs.wltools.util.Coefficient;

/**
 * IO Interface for the WL stage (non-tidal) type.
 */
public interface IStageIO {

   String STAGE_JSON_DICT_KEY= "Stage";

   String STAGE_JSON_ZEROTH_ORDER_KEY= "CS0";
   //String STAGE_JSON_D0_KEY= "CS0";
   //String STAGE_JSON_D0_KEY= "s0"; // --- The stage zero order coefficient json dict key. (WL average or Z0 in classic HA jargon)

   String STAGE_JSON_KEYS_SPLIT= ".";

   String STAGE_JSON_DN_KEYS_BEG= "CS";

   String STAGE_JSON_DNFCT_KEYS= "factor";
   String STAGE_JSON_DNLAG_KEYS= "hoursLag";

   //String STAGE_JSON_DNFCT_KEYS= "CS*.factor";  // --- The stage order>=1 coefficient factors json dict keys.
   //String STAGE_JSON_DNLAG_KEYS= "CS*.hoursLag"; // --- The stage order>=1 coefficient time lags (in hours) json dict keys.
   //String STAGE_JSON_DNFCT_KEYS= "s*.factor";  // --- The stage order>=1 coefficient factors json dict keys.
   //String STAGE_JSON_DNLAG_KEYS= "s*.hoursLag"; // --- The stage order>=1 coefficient time lags (in hours) json dict keys.

   String STATION_INFO_JSON_DICT_KEY= "channelGridPointInfo"; // --- The json dict key for the station info dict.

   String STATION_INFO_JSON_LATCOORD_KEY= "lat";
   String STATION_INFO_JSON_LONCOORD_KEY= "lon";

   String STATION_INFO_JSON_FNAME_EXT= ".json";

   String STATION_INFO_JSON_ZCIGLD_CONV_KEY= "zcVsIGLD"; //--- Implies that tha the preductions are referred to IGLD datum
                                                         //    WL(IGLD) - zcVsIGLD to get the ZC values
   String STATION_ID_SPLIT_CHAR= ":";

   String CLIMATO_YEAR_PLACEHOLDER= "YYYY";

   enum FileFormat {
     JSON
   };
}



