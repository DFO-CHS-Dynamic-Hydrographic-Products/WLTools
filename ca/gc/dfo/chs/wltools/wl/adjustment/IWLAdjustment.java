//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.adjustment;

import java.util.Set;
//import java.util.Map;

import ca.gc.dfo.chs.wltools.util.ITimeMachine;

/**
 * Comments please!
 */
public interface IWLAdjustment extends ITimeMachine {

  //--- 3mins in seconds for the maximum WL nearest neigh. time
  //    interpolation
  long MAX_TIMEINCR_DIFF_FOR_NNEIGH_TIMEINTERP_SECONDS= 180;

  // --- Maximum time difference in seconds for the removal of the high frequency
  //     WLO data oscillations (WL measurements at tide gauges) 15mins here.
  long MAX_TIMEDIFF_FOR_HF_OSCILLATIONS_REMOVAL_SECONDS= 900;

  // --- 15mins in seconds for the maximum time interval for
  //     the full model forecast data
  long MAX_FULL_FORECAST_TIME_INTERVAL_SECONDS= 900;

  long MIN_FULL_FORECAST_DURATION_HOURS= 48;

  long MIN_FULL_FORECAST_DURATION_SECONDS=
       MIN_FULL_FORECAST_DURATION_HOURS * SECONDS_PER_HOUR;

  long SYNOP_RUNS_TIME_OFFSET_HOUR= 6;
  long SHORT_TERM_FORECAST_TS_OFFSET_HOURS= SYNOP_RUNS_TIME_OFFSET_HOUR;

  long SHORT_TERM_FORECAST_TS_OFFSET_SECONDS=
       SHORT_TERM_FORECAST_TS_OFFSET_HOURS * SECONDS_PER_HOUR;

  String DEFAULT_MODEL_NAME= "MODEL_NAME_NOT_DEFINED";

  String DEFAULT_H2D2_NAME= "ECCC_H2D2";

  enum StormSurgeForecastModel {
    ECCC_H2D2_SLFE //,
    //DFO_NEMO_SJ100,
    //DFO_NEMO_VH20,
  }

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
    SINGLE_TIMEDEP_FMF_ERROR_STATS,
    MULT_TIMEDEP_FMF_ERROR_STATS //,
    //ECCC_H2D2_FORECAST_AUTOREG
  }

  String [] TideGaugeAdjMethodsDef= {
    TideGaugeAdjMethod.CHS_IWLS_QC.name(),
    TideGaugeAdjMethod.SINGLE_TIMEDEP_FMF_ERROR_STATS.name(),
    TideGaugeAdjMethod.MULT_TIMEDEP_FMF_ERROR_STATS.name()
    //TideGaugeAdjMethod.ECCC_H2D2_FORECAST_AUTOREG.name()
  };

  Set<String> allowedTideGaugeAdjMethods= Set.of(TideGaugeAdjMethodsDef);

  //enum Target {
  //  WDSFluvial //,
  //  // WDSOceanic
  //}
}
