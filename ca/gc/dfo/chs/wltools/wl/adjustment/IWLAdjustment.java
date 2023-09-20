//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.adjustment;

import java.util.Set;
//import java.util.Map;

/**
 * Comments please!
 */
public interface IWLAdjustment {

  enum StormSurgeForecastModel {
    ECCC_H2D2_SLFE //,
    //DFO_NEMO_SJ100,
    //DFO_NEMO_VH20,
  }

  String DEFAULT_H2D2_NAME= "ECCC_H2D2";

  enum Type {
    TideGauge, // --- Implies doing adjustments at one tide gauge only.
    SpineIPP,  // --- Spine WL adjustments initial pre-processing done inside SSC-ECCC 24/7 oper. system.
    SpineFPP  // --- Spine WL adjustments final pre-processing done alongside DFO-IMTS Spine API system (Azure cloud).
  }

  // //IWLS_WLO_QC, // --- Implies using IWLS WLP and WLO data to produce the specific WLF that is used for the short-term WLO quality control by the IWLS
  //  //MODEL_NEAREST_NEIGHBOR, // --- Implies use of WLF data coming from a model OR some WLP data to interpolate on the desired location using near. neigh. interp
  //  //MODEL_BARYCENTRIC   // --- Implies usr of WLF data coming from a FEM nodel(like H2D2 family OR even NEMO native grid WLF data)
  //}

  String [] allowedTypesDef= {
    Type.TideGauge.name(),
    Type.SpineIPP.name(),
    Type.SpineFPP.name()
  }; //,
                               //Type.IWLS_WLO_QC.name(),
                               //Type.MODEL_NEAREST_NEIGHBOR.name(),
                               //Type.MODEL_BARYCENTRIC.name() };

  Set<String> allowedTypes= Set.of(allowedTypesDef);

  enum TideGaugeAdjMethod {
    CHS_IWLS_QC,
    ECCC_H2D2_FORECAST_AUTOREG
  }

  String [] TideGaugeAdjMethodsDef= {
    TideGaugeAdjMethod.CHS_IWLS_QC.name(),
    TideGaugeAdjMethod.ECCC_H2D2_FORECAST_AUTOREG.name()
  };

  Set<String> allowedTideGaugeAdjMethods= Set.of(TideGaugeAdjMethodsDef);

  //enum Target {
  //  WDSFluvial //,
  //  // WDSOceanic
  //}
}
