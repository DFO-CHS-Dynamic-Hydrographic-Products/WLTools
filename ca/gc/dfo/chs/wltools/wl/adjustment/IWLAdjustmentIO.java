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

  String INPUT_DATA_FMT_SPLIT_CHAR= ":";

  String OUTPUT_DATA_FMT_SPLIT_CHAR= "-";

  //enum LocationType {
  //  WDS,
  //  IWLS
  //}

  //String [] LOCATION_TYPES_DEF= { LocationType.WDS.name(),
  //                                LocationType.IWLS.name() };

  //Set<String> allowedLocationTypes= Set.of(LOCATION_TYPES_DEF);

  enum InputDataType {
    ECCC_H2D2, //:NETCDF,
    //H2D2:ASCII,
    DHP_S104,
    IWLS //:JSON
  }

  enum InputDataTypesFormatsDef {
    HDF5_DCF3,
    ASCII,
    JSON
  }

  // ---
  //String [] ECCC_H2D2_INPUT_FMTS= { InputDataTypesFormatsDef.NETCDF.name(),
  //                                  InputDataTypesFormatsDef.ASCII.name() };

  String [] DHP_S104_INPUT_FMTS= { InputDataTypesFormatsDef.HDF5_DCF3.name() };

  String [] ECCC_H2D2_INPUT_FMTS= { InputDataTypesFormatsDef.ASCII.name() };

  String [] IWLS_INPUT_FMTS= { InputDataTypesFormatsDef.JSON.name() };

  // --- TODO: Use the InputDataTypesFormatsDef enum objects as keys to this
  //     InputDataTypesFormats Map instead of the related Strings ??
  Map< String, Set<String> > InputDataTypesFormats= Map.of(
    InputDataType.DHP_S104.name() , Set.of(DHP_S104_INPUT_FMTS),
    InputDataType.ECCC_H2D2.name(), Set.of(ECCC_H2D2_INPUT_FMTS),
    InputDataType.IWLS.name()     , Set.of(IWLS_INPUT_FMTS)
  );

  Set<String> allowedInputDataTypes= InputDataTypesFormats.keySet();

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

   int H2D2_ASCII_FMT_1ST_DATA_LINE_INDEX= 2;

   String H2D2_ASCII_FMT_FLINE_SPLIT= ",";

   String H2D2_ASCII_FMT_TIMESTAMP_KEY= "epoch";

   String H2D2_ASCII_FMT_FNAME_SPLITSTR= "_";
}

