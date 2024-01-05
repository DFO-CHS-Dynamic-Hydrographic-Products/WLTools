//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.adjustment;

import java.util.Set;
import java.util.Map;

// ---
import ca.gc.dfo.chs.wltools.IWLToolsIO;
//import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;

/**
 * Comments please!
 */
public interface IWLAdjustmentIO {

  // ---
  enum FullModelForecastType {

    ACTUAL(0),
    PREVIOUS(1);

    private int typeIndex;

    private FullModelForecastType(final int typeIndex) {
      this.typeIndex= typeIndex;
    }
  }

  // ---
  enum DataTypesFormatsDef {
    DHP_S104_DCF3,
    //DHP_S104_DCF2,
    ECCC_H2D2_ASCII,
    SPINE_ADHOC_ASCII,
    //IWLStationPredIO.Format.CHS_JSON
  }

  String [] DataTypesFormatsDefArr= {
    //IWLStationPredIO.Formats.CHS_JSON.name(),
    DataTypesFormatsDef.DHP_S104_DCF3.name(),
    DataTypesFormatsDef.ECCC_H2D2_ASCII.name(),
    DataTypesFormatsDef.SPINE_ADHOC_ASCII.name()
  };

  Set<String> allowedInputFormats= Set.of(DataTypesFormatsDefArr);

  // ---
  //String [] ECCC_H2D2_INPUT_FMTS= { InputDataTypesFormatsDef.NETCDF.name(),
  //                                  InputDataTypesFormatsDef.ASCII.name() };

  String [] CHS_DHP_S104_FMTS= {
    DataTypesFormatsDef.DHP_S104_DCF3.name(),
    //DataTypesFormatsDef.HDF5_DCF2.name()
  };

  //String [] ECCC_H2D2_FORECAST_INPUT_FMTS= {
  //  InputDataTypesFormatsDef.ASCII.name() //,
  //  // InputDataTypesFormatsDef.NetCDF.name()
  //};

  String [] CHS_IWLS_FMTS= {
    IWLToolsIO.Format.CHS_JSON.name()
  };

  String [] CHS_SPINE_FMTS= {
    IWLToolsIO.Format.CHS_JSON.name(),
    DataTypesFormatsDef.DHP_S104_DCF3.name(),
    DataTypesFormatsDef.SPINE_ADHOC_ASCII.name(),
  };

  // ---
  String [] CHS_TIDEGAUGE_FMTS= {
    IWLToolsIO.Format.CHS_JSON.name(),
    DataTypesFormatsDef.ECCC_H2D2_ASCII.name() //,
    //DataTypesFormatsDef.JSON.name()+ INPUT_DATA_FMT_SPLIT_CHAR + DataTypesFormatsDef.ECCC_H2D2_ASCII.name()
  };

  int H2D2_ASCII_FMT_1ST_DATA_LINE_INDEX= 2;

  String H2D2_ASCII_FMT_FLINE_SPLIT= ",";

  String H2D2_ASCII_FMT_TIMESTAMP_KEY= "epoch";

  String H2D2_ASCII_FMT_FNAME_SPLITSTR= "_";

  String ARG_NOT_DEFINED= "NONE";

  // --- TODO add code to append the FMF <YYYYMMDDhhmm> lead time string
  //     to this file name prefix
  String ADJ_HFP_ATTG_FNAME_PRFX= "adjHFPAtTG-";

  String NONADJ_FMF_ATTG_FNAME_PRFX= "nonAdjFMFAtTG-";

  String RESIDUALS_STATS_ATTG_FNAME_PRFX= "ResidualsStatsAtTG-";

  String NEW_FMF_RESIDUALS_STATS_SUBDIRNAME= "newer";
  String PRV_FMF_RESIDUALS_STATS_SUBDIRNAME= "previous";

  String FMF_RESIDUALS_STATS_TDEP_OFST_SECONDS_JSON_KEY= "secondsOffset";

    //String SHIP_CHANNEL_POINTS_DEF_DIRNAME= "channelGridPointsInfoDef";  
}
