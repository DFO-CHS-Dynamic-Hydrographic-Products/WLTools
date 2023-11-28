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

  //String SPINE_STL_TIDE_GAUGES_INFO_FNAME= "";

  //String TIDE_GAUGES_INFO_FOLDER_NAME= "tideGaugeInfo";
  //String TIDE_GAUGES_INFO_ECCC_IDS_KEY= "ECCC_ID";

  String INPUT_DATA_TYPE_SPLIT_STR= "::";

  String INPUT_DATA_FMT_SPLIT_CHAR= ":";

  String OUTPUT_DATA_FMT_SPLIT_CHAR= "-";

  String ISO8601_DATETIME_SEP_CHAR= "T";
  String ISO8601_YYYYMMDD_SEP_CHAR= OUTPUT_DATA_FMT_SPLIT_CHAR;

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

  String ADJ_FORECAST_ATTG_FNAME_PRFX= "AdjForecastAtTG-";
}
