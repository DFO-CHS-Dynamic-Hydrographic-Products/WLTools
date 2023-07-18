//package ca.gc.dfo.iwls.fmservice.modeling.tides;
package ca.gc.dfo.chs.wltools.tidal;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

//---
import ca.gc.dfo.chs.wltools.util.ITimeMachine;
import ca.gc.dfo.chs.wltools.util.ITrigonometry;
//import ca.gc.dfo.iwls.fmservice.modeling.util.ITimeMachine;
//import ca.gc.dfo.iwls.fmservice.modeling.util.ITrigonometry;

/**
 * Interface for tidal IO utilities specific constants.
 */
public interface ITidalIO extends ITidal, ITrigonometry, ITimeMachine {
  
  int TCF_LINE_NB_ITEMS = 11;

//--- Possible output in HFD5 S-194 format for OPP needs.
//    enum WLOuputFileFormat {
//        S_104
//    }
  int TCF_NAME_LINE_INDEX = 0;
  int TCF_PERIOD_LINE_INDEX = 1;
  int TCF_AMPLITUDE_LINE_INDEX = 2;
  int TCF_PHASE_LAG_LINE_INDEX = 3;
  int TCF_UTC_OFFSET_LINE_INDEX = 5;
  String TCF_COMMENT_FLAG = "||";
  //--- Temp. blue book format data directory for testing purposes only.
  //    (To be removed for production code)
  String TCF_DATA_DIR = "C:\\Users\\MercierGi\\Data\\DVFM\\BlueBook\\";
  String TCF_DATA_FILE_EXT = ".wlev";
  String TCF_UTC_OFFSET_FLAG = "!Computed";
  
  /**
   * enum for water levels tidal constituents input files formats:
   */
  enum WLConstituentsInputFileFormat {

    TCF,
    JSON
    //WEBTIDE
  }
}
