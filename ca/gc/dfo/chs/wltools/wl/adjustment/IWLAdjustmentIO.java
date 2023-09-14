//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.adjustment;

import java.util.Set;
import java.util.Map;

/**
 * Comments please!
 */
public interface IWLAdjustmentIO {

  //String SPINE_STL_TIDE_GAUGES_INFO_FNAME= "";

  String TIDE_GAUGES_INFO_FOLDER_NAME= "tideGaugeInfo";

  String TIDE_GAUGES_INFO_ECCC_IDS_KEY= "ECCC_ID";

  String INPUT_DATA_TYPE_SPLIT_STR= "::";

  String INPUT_DATA_FMT_SPLIT_CHAR= ":";

  String OUTPUT_DATA_FMT_SPLIT_CHAR= "-";

  enum DataType {
    CHS_IWLS,
    CHS_SPINE,
    //CHS_DHP_S104,
    CHS_TIDEGAUGE
    //CHS_PREDICTION,
    //ECCC_H2D2_FORECAST
  }

  //String [] allowedDataTypesDef= {
  //  DataType.CHS_IWLS.name(),
  //  DataType.CHS_DHP_S104.name(),
  //  DataType.CHS_TIDEGAUGEname()
  //};
  //Set<String> allowedDataTypes= Set.of(allowedDataTypesDef);

  //Map< String, Set<String> > allowedDataTypes= Map.of( );

  enum DataTypesFormatsDef {
    JSON,
    DHP_S104_DCF3,
    //DHP_S104_DCF2,
    ECCC_H2D2_ASCII,
    SPINE_ADHOC_ASCII
  }

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
    DataTypesFormatsDef.JSON.name()
  };

  String [] CHS_SPINE_FMTS= {
    DataTypesFormatsDef.JSON.name(),
    DataTypesFormatsDef.DHP_S104_DCF3.name(),
    DataTypesFormatsDef.SPINE_ADHOC_ASCII.name(),
  };

  // ---
  String [] CHS_TIDEGAUGE_FMTS= {
    DataTypesFormatsDef.JSON.name(),
    DataTypesFormatsDef.ECCC_H2D2_ASCII.name(),
    DataTypesFormatsDef.JSON.name()+ INPUT_DATA_FMT_SPLIT_CHAR + DataTypesFormatsDef.ECCC_H2D2_ASCII.name()
  };


  // --- TODO: Use the InputDataTypesFormatsDef enum objects as keys to this
  //     InputDataTypesFormats Map instead of the related Strings ??
  Map< String, Set<String> > DataTypesFormats= Map.of(
    DataType.CHS_IWLS.name()      , Set.of(CHS_IWLS_FMTS),
    DataType.CHS_SPINE.name()     , Set.of(CHS_SPINE_FMTS),
    DataType.CHS_TIDEGAUGE.name() , Set.of(CHS_TIDEGAUGE_FMTS)
    //InputDataType.CHS_PREDICTION.name(), Set.of(CHS_PREDICTION_INPUT_FMTS),
    //InputDataType.ECCC_H2D2_FORECAST.name() , Set.of(ECCC_H2D2_FORECAST_INPUT_FMTS)
  );

  Set<String> allowedDataTypes= DataTypesFormats.keySet();

  int H2D2_ASCII_FMT_1ST_DATA_LINE_INDEX= 2;

  String H2D2_ASCII_FMT_FLINE_SPLIT= ",";

  String H2D2_ASCII_FMT_TIMESTAMP_KEY= "epoch";

  String H2D2_ASCII_FMT_FNAME_SPLITSTR= "_";

  //enum ECCC_H2D2_WLF_NAMES {
  //  SURFACEN, //--- WLF values on FEM nodes (triangles vertices)
  //  SURFACEE  //--- WLF values on FEM edges (egdes that connect the triangle vertices)
  //}

  //String ECCC_H2D2_TIME_ATTR_NAME= "Time";

  // --- Define the names of the coordinates datasets names that are used by the ECCC_H2D2
  //     two different WLF datasets.
  //Map<ECCC_H2D2_WLF_NAMES,String> ECCC_H2D2_COORDS_DSETS_NAMES= Map.of(
  //  ECCC_H2D2_WLF_NAMES.SURFACEN, "St_Lawrence_River_node_lon:St_Lawrence_River_node_lat",
  //  ECCC_H2D2_WLF_NAMES.SURFACEE, "St_Lawrence_River_edge_lon:St_Lawrence_River_edge_lat"
  //);
}
