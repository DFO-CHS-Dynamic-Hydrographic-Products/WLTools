package ca.gc.dfo.chs.modeldata;

import java.util.Map;
import java.util.Set;

import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.dhp.sproduct.ISProductIO;
//import ca.gc.dfo.chs.currtools.ICurrToolsIO;

// ---
public interface IModelDataExtractionIO extends IWLToolsIO { //, ICurrToolsIO {

    //String locationsCoordsId= "SIMPLE_LOCATIONS_COORDS";

  String locationsCoordsSep= ";;"; // e.g: 49.69,-169.69;;52.45,-70.34;; ...

  String latLonSep= ",";
    
  enum Type {
    // 2DSlice,
    // 3DSlice,
    // shipWayPointsVertProf, // --- e.g. currents vertical profile under one or more (lat,lon) coordinates point(s), only valid for currents obviously
    shipWayPoints,    // --- one or more (lat,lon) coordinates point(s), valid for both water levels and currents, can also be Tide gauge(s) locations
  }

  String [] allowedTypesDef= {
    Type.shipWayPoints.name(),
  };

  // ---
  Set<String> allowedTypes= Set.of(allowedTypesDef);

  enum Format {
    SIMPLE_LOCATIONS_COORDS, //
    CHS_TG_JSON
  }
    
  String [] allowedPointLocationsFormatsDef= {
    Format.SIMPLE_LOCATIONS_COORDS.name(),
    Format.CHS_TG_JSON.name()
    //IWLToolsIO.Format.IWLS_JSON.name()
  };

  // ---
  enum InputDataType {
    //Currents2D,
    //Currents3D,
    WaterLevels  
  };

  String [] allowedInputDataTypesDef= {
    InputDataType.WaterLevels.name(),
  };

  Set<String> allowedInputDataTypes= Set.of(allowedInputDataTypesDef);

  String [] allowedInputDataFormatsDef= {
    ISProductIO.Format.S104DCF2.name() //,
    //locationsCoordsId
  };

  Set<String> allowedInputDataFormats= Set.of(allowedInputDataFormatsDef);
  
  // ---
  enum SpatialInterpType {
    // barycentric, // --- Using FEM TIN grid points OR NEMO native grid points as if they were from a TIN grid.
    // barnesSpatialInterp,  // --- Barnes' interp. type 
    nearestNeigh
  };

  String [] allowedSpatialInterpTypesDef= {
    SpatialInterpType.nearestNeigh.name(),
  };

  Set<String> allowedSpatialInterpTypes= Set.of(allowedSpatialInterpTypesDef);

  enum WLDatumConv {
    zc2Global,
    global2zc
  };
    
  String [] allowedWLDatumConvDef= {
    WLDatumConv.zc2Global.name(),
    WLDatumConv.global2zc.name()
  };

  Set<String> allowedWLDatumConvTypes= Set.of(allowedWLDatumConvDef);
  
}
