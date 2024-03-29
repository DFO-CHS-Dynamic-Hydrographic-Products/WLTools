package ca.gc.dfo.chs.dhp;

// --
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

// ---
public interface ISProductIO {

  // ---
  enum FeatId {
    S104,
    S111
  }

  // ---
  Map<FeatId,String> FEATURE_IDS=
    Collections.unmodifiableMap(			
       new HashMap<FeatId, String>() { {
	   
 	 put(FeatId.S104,"WaterLevel");
	 put(FeatId.S111,"SurfaceCurrent");
	 
       } }       
    );

  // ---
  enum S104TrendFlag {
    Unknown,
    Increasing,
    Decreasing,
    Steady
  }

  // ---
  Map<S104TrendFlag,Byte> S104_TREND_FLAGS=
    Collections.unmodifiableMap(			
       new HashMap<S104TrendFlag,Byte>() {{
	   
	 put(S104TrendFlag.Unknown,    (byte)0);
	 put(S104TrendFlag.Decreasing, (byte)1);
	 put(S104TrendFlag.Increasing, (byte)2);
	 put(S104TrendFlag.Steady,     (byte)3);
	 
       }}       
    );

  // ---
  String FEAT_CMPD_TYPE_UNCERT_ID= "Uncertainty";
    
  // ---
  String S104_CMPD_TYPE_HGHT_ID= FEATURE_IDS.get(FeatId.S104) + "Height";
  String S104_CMPD_TYPE_TRND_ID= FEATURE_IDS.get(FeatId.S104) + "Trend";

  // // --- Cannot have more than 9999 of ship channel
  // //     point locations.
  // int MAX_SCLOCS_NB= 9999;  
								      
  String CURRENTS_PR0D_ID= FeatId.S111.name(); //"S111";
  String WATLEVLS_PR0D_ID= FeatId.S104.name(); //"S104";
     
  String HDF5_FEXT= ".h5";

  String PKG_CFG_MAIN_DIR= "dhp";

  String PKG_CFG_MAIN_TMPLF_DIR=
    PKG_CFG_MAIN_DIR + File.separator + "templates";

  String PKG_CFG_MAIN_DCF8_TMPLF_DIR=
    PKG_CFG_MAIN_TMPLF_DIR + File.separator + "DCF8";

  String PKG_CFG_MAIN_DCF8_104_TMPLF_DIR=
    PKG_CFG_MAIN_DCF8_TMPLF_DIR + File.separator + FeatId.S104.name(); //WATLEVELS_PR0D_ID;
    
  // --- Relative path of the S104 DCF8 template file for the HSTLT_WLPS of the lower St. Lawrence (a.k.a Spine)
  //     (NOTE: The version v1.0 is hardcoded here, not a good idea we need to do something else more generic here!) 
  String PKG_LOWSTL_S104_DCF8_TMPLF_RPATH= PKG_CFG_MAIN_DCF8_104_TMPLF_DIR +
    File.separator + "v1.0" + File.separator +"DCF8_104_HSTLT_WLPS_LOWSTL_30D_U" + HDF5_FEXT;

  // ---
  String GRP_SEP_ID= "/";
    
  String ROOT_GRP_ID= GRP_SEP_ID;

  String GRP_PRFX= "Group_";
    
  String ISSUE_HHMMSS_ID=   "issueTime";    
  String ISSUE_YYYYMMDD_ID= "issueDate";

  String FCST_ID= ".01";
  String PRED_ID= ".02";
  String OBSV_ID= ".03";  

  String LEAST_RECENT_TIMESTAMP_ID= "dateTimeOfFirstRecord";
  String MOST_RECENT_TIMESTAMP_ID= "dateTimeOfLastRecord";   

  String NB_GROUPS_ID= "numGRP";
  String NB_STATIONS_ID= "numberOfStations";

  String NB_TIMESTAMPS_ID= "numberOfTimes";
  String TIME_INTRV_ID= "timeRecordInterval";

  String VAL_DSET_ID= "values";

  // String SCLOC_STN_ID_PRFX= "STLT_WLPS ship channel point location #";

  // --- Specific HDF5 attritbutes ids for DCF8
  String DCF8_STN_LAST_TIMESTAMP_ID= "endDateTime";
  String DCF8_STN_FIRST_TIMESTAMP_ID= "startDateTime";
    
  String DCF8_STNID_ID= "stationIdentification";

  String DCF8_STN_NAME_ID= "stationName";   
}
