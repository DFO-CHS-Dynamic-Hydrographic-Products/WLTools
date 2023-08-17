//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.adjustment;

import java.util.Set;
import java.util.Map;

/**
 * Comments please!
 */
public interface IWLAdjustmentIO {

  String INPUT_DATA_FMT_SPLIT_CHAR= ":";

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
    IWLS //:JSON
  }

  enum InputDataTypesFormatsDef {
    NETCDF,
    ASCII,
    JSON
  }

  // ---
  String [] ECCC_H2D2_INPUT_FMTS= { InputDataTypesFormatsDef.NETCDF.name(),
                                    InputDataTypesFormatsDef.ASCII.name() };

  String [] IWLS_INPUT_FMTS= { InputDataTypesFormatsDef.JSON.name() };

  Map< String, Set<String> > InputDataTypesFormats= Map.of(
    InputDataType.ECCC_H2D2.name(), Set.of(ECCC_H2D2_INPUT_FMTS),
    InputDataType.IWLS.name()     , Set.of(IWLS_INPUT_FMTS)
  );

  Set<String> allowedInputDataTypes= InputDataTypesFormats.keySet();

  enum ECCC_H2D2_WLF_NAMES {
    SURFACEN, //--- WLF values on FEM nodes (triangles vertices)
    SURFACEE  //--- WLF values on FEM edges (egdes that connect the triangle vertices)
  }

  // --- Define the names of the coordinates datasets names that are used by the ECCC_H2D2
  //     two different WLF datasets.
  Map<String,String> ECCC_H2D2_COORDS_DSETS_NAMES= Map.of(
    ECCC_H2D2_WLF_NAMES.SURFACEN.name(), "St_Lawrence_River_node_lon:St_Lawrence_River_node_lat",
    ECCC_H2D2_WLF_NAMES.SURFACEE.name(), "St_Lawrence_River_edge_lon:St_Lawrence_River_edge_lat"
  );

}

