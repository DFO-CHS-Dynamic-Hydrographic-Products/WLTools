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
       new HashMap<FeatId, String>() {{
 	 put(FeatId.S104,"WaterLevel");
	 put(FeatId.S111,"SurfaceCurrent");
       }}       
    );
								      
  String CURRENTS_PR0D_ID= FeatId.S111.name(); //"S111";
  String WATLEVLS_PR0D_ID= FeatId.S104.name(); //"S104";
     
  String HDF5_FEXT= ".h5";

  String PKG_CFG_MAIN_DIR= "dhp";

  String PKG_CFG_MAIN_TMPLF_DIR= PKG_CFG_MAIN_DIR + File.separator + "templates";

  String PKG_CFG_MAIN_DCF8_TMPLF_DIR= PKG_CFG_MAIN_TMPLF_DIR + File.separator + "DCF8";

  String PKG_CFG_MAIN_DCF8_104_TMPLF_DIR= PKG_CFG_MAIN_DCF8_TMPLF_DIR + File.separator + FeatId.S104.name(); //WATLEVELS_PR0D_ID;
    
  // --- Relative path of the S104 DCF8 template file for the HSTLT_WLPS of the lower St. Lawrence (a.k.a Spine)
  //     (NOTE: The version v1.0 is hardcoded here, not a good idea we need to do something else more generic here!) 
  String PKG_LOWSTL_S104_DCF8_TMPLF_RPATH= PKG_CFG_MAIN_DCF8_104_TMPLF_DIR +
    File.separator + "v1.0" + File.separator +"DCF8_104_HSTLT_WLPS_LOWSTL_30D_U" + HDF5_FEXT;

  // ---
  String GRP_SEP_ID= "/";
    
  String ROOT_GRP_ID= GRP_SEP_ID;

  String ISSUE_HHMMSS_ID=   "issueTime";    
  String ISSUE_YYYYMMDD_ID= "issueDate";

  String FCST_ID= ".01";
  String PRED_ID= ".02";
  String OBSV_ID= ".03";  

  String LEAST_RECENT_TIMESTAMP_ID= "dateTimeOfFirstRecord";
  String MOST_RECENT_TIMESTAMP_ID= "dateTimeOfLastRecord";   

  String NB_GROUPS_ID= "numGRP";
  String NB_STATIONS_ID= "numberOfStations";
}
