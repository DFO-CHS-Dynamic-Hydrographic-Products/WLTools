//package ca.gc.dfo.iwls.fmservice.modeling.fms;
package ca.gc.dfo.chs.wltools.wl.fms;

/**
 * Created by G. Mercier on 2017-12-11.
 * Modified-adapted for the Spine 2.0 API system by G. Mercier on 2023-09-26
 */

import ca.gc.dfo.chs.wltools.wl.fms.IFMS;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltool.util.MeasurementCustom;

//---
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLMeasurement;
//import ca.gc.dfo.iwls.timeseries.MeasurementCustom;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

//---
//import ca.gc.dfo.iwls.fmservice.modeling.Forecast;
//import ca.gc.dfo.iwls.fmservice.station.ExtendedStationRepository;
//---

/**
 * Manage arrays of WLMeasurement references.
 */
public final class FMSWLMeasurement extends WLMeasurement implements IFMS {

  /**
   * Default constructor.
   */
  public FMSWLMeasurement() {
    super();
  }

  /**
   * @param measurement : A Measurement object for super class constructor.
   */
  public FMSWLMeasurement(/*@NotNull*/ final MeasurementCustom measurement) {
    super(measurement);
  }

  /**
   * Set an array of FMWLMeasurement objects with the proper FMWLMeasurement objects types.
   *
   * @param fromArray : Array of FMWLMeasurement objects to use as references.
   * @param toArray   : Array of FMWLMeasurement objects to set.
   * @return The array of FMWLMeasurement objects ready to use.
   */
  protected static FMSWLMeasurement[]
    getMeasurementsRefs(/*@NotNull*/ final FMSWLMeasurement[] fromArray, /*@NotNull*/ final FMSWLMeasurement[] toArray) {

    toArray[PREDICTION]= new FMSWLMeasurement(fromArray[PREDICTION].measurement);
    toArray[OBSERVATION]= new FMSWLMeasurement(fromArray[OBSERVATION].measurement);
    toArray[QC_FORECAST]= new FMSWLMeasurement(fromArray[QC_FORECAST].measurement);
    toArray[MODEL_FORECAST] = new FMSWLMeasurement(fromArray[MODEL_FORECAST].measurement);

    return toArray;
  }

  /**
   * Set an array of FMWLMeasurement objects with the proper Measurement objects types.
   *
   * @param prediction  : A Measurement of WLType PREDICTION
   * @param observation : A Measurement of WLType
   * @param forecast    : A Measurement of WLType
   * @param stormSurge  : A Measurement of WLType
   * @param dataArray   : An Array of FMWLMeasurement objects.
   * @return The Array of FMWLMeasurement objects dataArray.
   */
  protected static FMSWLMeasurement[]
    setMeasurementsRefs(/*@NotNull*/ final MeasurementCustom prediction, final MeasurementCustom observation,
                                     final MeasurementCustom qcForecast, final MeasurementCustom modelForecast,
                        /*@NotNull* @Size(min = 4)*/  final FMSWLMeasurement[] dataArray) {

//        System.out.println("prediction="+prediction);
//        System.out.println("observation="+observation);
//        System.out.println("forecast="+forecast);
//        System.out.println("stormSurge="+stormSurge);

    //--- Inline(instead of using a loop) assignations to get the best performance:
    dataArray[PREDICTION].measurement= prediction;
    dataArray[OBSERVATION].measurement= observation;
    dataArray[QC_FORECAST].measurement= qcForecast;
    dataArray[MODEL_FORECAST].measurement= modelForecast;

    return dataArray;
  }

  /**
   * @return The Measurement object reference.
   */
  protected final MeasurementCustom measurement() {
    return this.measurement;
  }
}
