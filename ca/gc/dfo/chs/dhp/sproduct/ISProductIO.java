package ca.gc.dfo.chs.dhp;

// --
import java.io.File;

// ---
public interface ISProductIO {

  enum Id {
    S104,
    S111
  }
    
  String CURRENTS_PR0D_ID= Id.S111.name(); //"S111";
  String WATLEVLS_PR0D_ID= Id.S104.name(); //"S104";
    
  String HDF5_FEXT= ".h5";

  String PKG_CFG_MAIN_DIR= "dhp";

  String PKG_CFG_MAIN_TMPLF_DIR= PKG_CFG_MAIN_DIR + File.separator + "templates";

  String PKG_CFG_MAIN_DCF8_TMPLF_DIR= PKG_CFG_MAIN_TMPLF_DIR + File.separator + "DCF8";

  String PKG_CFG_MAIN_DCF8_104_TMPLF_DIR= PKG_CFG_MAIN_DCF8_TMPLF_DIR + File.separator + Id.S104.name(); //WATLEVELS_PR0D_ID;
    
  // --- Relative path of the S104 DCF8 template file for the HSTLT_WLPS of the lower St. Lawrence (a.k.a Spine)
  //     (NOTE: The version v1.0 is hardcoded here, not a good idea we need something else) 
  String PKG_LOWSTL_S104_DCF8_TMPLF_RPATH= PKG_CFG_MAIN_DCF8_104_TMPLF_DIR +
    File.separator + "v1.0" + File.separator +"DCF8_104_HSTLT_WLPS_LOWSTL_30D_U" + HDF5_FEXT;
}
