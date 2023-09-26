//package ca.gc.dfo.iwls.fmservice.modeling.fms;
package ca.gc.dfo.chs.wltools.wl.fms;

/**
 *
 */

//---
//import java.util.List;

import ca.gc.dfo.chs.wltools.wl.fms.FMSInput;
import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;

//import ca.gc.dfo.iwls.fmservice.modeling.ForecastingContext;
//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

//---
//import ca.gc.dfo.iwls.timeseries.Measurement;
//---
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLMeasurement;

/**
 * Interface for the generic WL errors residuals methods.
 */
public interface IFMSResidual extends IFMS {

  /**
   * @return A generic FMResidualFactory type object.
   */
  //@NotNull
  FMSResidualFactory getFMSResidualFactory();

  /**
   * @param forecastingContext : A ca.gc.dfo.iwls.fmservice.modeling.ForecastingContext object.
   * @param lastWLOSse         : The time-stamp(seconds since the epoch) of the last valid WLO available for the
   *                           station.
   * @return A generic IFMSResidual type object.
   */
  //@NotNull
  //IFMSResidual getIFMSResidual(/*@NotNull*/ final ForecastingContext forecastingContext, @Min(0) final long lastWLOSse);
  IFMSResidual getIFMSResidual(/*@NotNull*/ final FMSInput fmsInput, /*@Min(0)*/ final long lastWLOSse);

  int getNbMissingWLO();

  //--- For possible future usage.
  //abstract public @Min(0) long getSseStart();

  /**
   * @param pstrWLStationTimeNode : A WLStationTimeNode object which is just before in time compared to the
   *                              SecondsSinceEpoch sse object time-stamp.
   * @param sse                   : A SecondsSinceEpoch with the next(in the future) time stamp to use.
   * @param data                  : An array of 4 FMSWLMeasurement(PREDICTION, OBSERVATION, FORECAST, EXT_STORM_SURGE
   *                              (which could be
   *                              NULL)) objects.
   * @return A new WLStationTimeNode object ready to be used.
   */
  //@NotNull
  WLStationTimeNode newFMSTimeNode(/*@NotNull*/ final WLStationTimeNode pstrWLStationTimeNode,
                                   /*@NotNull*/ final SecondsSinceEpoch sse,
                                   /*@NotNull @Size(min = 4)*/ final FMSWLMeasurement[] data);

  /**
   * Main computation method for errors residuals statistics computations.
   *
   * @param stationCode       : The WL station usual ID String
   * @param stillGotWLOs      : A boolean to signal that the WLStationTimeNode object is in the past compared to the
   *                          last
   *                          valid WLO available for the station.
   * @param wlStationTimeNode : WLStationTimeNode object.
   * @return The processed WLStationTimeNode taken as last argument.
   */
  //@NotNull
  WLStationTimeNode processWLStationTimeNode(/*@NotNull*/ final String stationCode,
                                                          final boolean stillGotWLOs,
                                             /*@NotNull*/ final WLStationTimeNode wlStationTimeNode);
}
