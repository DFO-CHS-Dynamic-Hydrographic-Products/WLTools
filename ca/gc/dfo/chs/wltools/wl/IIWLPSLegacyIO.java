package ca.gc.dfo.chs.wltools.wl;

import ca.gc.dfo.chs.wltools.IWLToolsIO;

// ---
interface IIWLPSLegacyIO extends IWLToolsIO {

  int NB_SHIP_CHANNEL_PT_LOCS= 1061;

  String NB_SHIP_CHANNEL_PT_LOCS_STR= Integer.toString(NB_SHIP_CHANNEL_PT_LOCS);  

  String HOUR_FNAMES_COMMON_POSTFIX= ".one30."+NB_SHIP_CHANNEL_PT_LOCS_STR;
  String QUARTER_FNAME_COMMON_POSTFIX= ".one."+NB_SHIP_CHANNEL_PT_LOCS_STR;

  String UNCERTAINTIES_STATIC_FNAME= "mat_erreur.dat."+NB_SHIP_CHANNEL_PT_LOCS_STR; 
    
  // // ---
  // enum Type {
  //   ONE_DAY,
  //   THIRTY_DAYS //,
  //   //UNCERTAINTIES
  // }

  // enum QuarterFileTypeId {
  //   Q2,
  //   Q3,
  //   Q4
  // }
}
