package ca.gc.dfo.chs.modeldata;

import java.util.Map;
import java.util.Set;

import ca.gc.dfo.chs.wltools.IWLToolsIO;
//import ca.gc.dfo.chs.currtools.ICurrToolsIO;

// ---
public interface IModelDataExtraction extends IWLToolsIO { //, ICurrToolsIO {
   
  enum Type {
    // 2DSlice,
    // 3DSlice,
    // shipWayPointsVertProf, // --- e.g. currents vertical profile under one or more (lat,lon) coordinates point(s), only valid for currents obviously
    shipWayPointsSurface      // --- one or more (lat,lon) coordinates point(s), valid for both water levels and currents 
  }

  String [] allowedTypesDef= {
    Type.shipWayPointsSurface.name(),
  };

  // ---
  Set<String> allowedTypes= Set.of(allowedTypesDef);

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
