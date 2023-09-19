//package ca.gc.dfo.iwls.fmservice.modeling.fms;
package ca.gc.dfo.chs.wltools.wl.fms;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.WLTimeNode;
import ca.gc.dfo.chs.wltools.wl.fms.FMSWLData;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.wl.fms.LegacyFMSContext;

//import ca.gc.dfo.iwls.fmservice.modeling.ForecastingContext;
//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLTimeNode;
//import ca.gc.dfo.iwls.modeling.fms.FmsParameters;
//import ca.gc.dfo.iwls.timeseries.MeasurementCustom;
//import lombok.extern.slf4j.Slf4j;

//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
//import java.util.List;

//---
//---
//---

/**
 * Generic class for FMS legacy.
 */
//@Slf4j
abstract public class FMSFactory implements IFMS {

  private final static String whoAmI=
     "ca.gc.dfo.chs.wltools.wl.fms.FMSFactory";

 /**
   * Usual static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * Contains all FM data needed.
   */
  protected FMSWLData data;
  /**
   * The forecasts duration in seconds.
   */
  protected long fcstsDurationSeconds= (long) SECONDS_PER_HOUR * FORECASTS_DURATION_HOURS;
  /**
   * Equivalent of the forecast_interval C struct defined in the source file dvfm.c of the 1990 legacy ODIN C source
   * code bundle.
   */
  protected long fcstsTimeIncrSeconds= (long) SECONDS_PER_MINUTE * FORECASTS_TIME_INCR_MINUTES_MAX;

  /**
   * Default constructor.
   */
  FMSFactory() {
    this.data = null;
  }

  ///**
  // * @param forecastingContextList : A List(min. size==1) of ForecastingContext configuration objects coming from a
  // *                               SQL-esque operational DB request.
  // */
  //FMSFactory(@NotNull @Size(min = 1) final List<ForecastingContext> forecastingContextList) {

  FMSFactory(/*@NotNull @Size(min = 1)*/ final List<LegacyFMSContext> legacyFMSContextList) {

    final String mmi=
      "FMSFactory(final List<LegacyFMSContext> legacyFMSContextList) constructor: ";

    slog.debug(mmi+"start");

    //final ForecastingContext fc0 = forecastingContextList.get(0);
    final LegacyFMSContext lfc0= legacyFMSContextList.get(0);

    //if (!checkForecastingContext(fc0)) {
    if (!checkLegacyFMSContext(lfc0)) {

      slog.error(mmi+"checkLegacyFMSContext(lfc0)==false!");
      throw new RuntimeException(mmi+:"Cannot update the WLF-QC !!");
    }

    final String station0Id = lfc0.getStationCode();

    slog.debug(mmi+"station0Id=" + station0Id);

    try {
      lfc0.getParameters();

    } catch (NullPointerException npe) {

      slog.error(mmi+"lfc0.getParameters()==null!, station0Id=" + station0Id);
      throw new RuntimeException(npe);
    }

    try {
      lfc0.getParameters().getDeltaTMinutes();

    } catch (NullPointerException npe) {

      slog.error(mmi+"lfc0.getParameters().getDeltaTMinutes()==null! station0Id=" + station0Id);
      throw new RuntimeException(npe);
    }

    final int fcstsTimeIncrMinutes= lfc0.
      getParameters().getDeltaTMinutes().intValue();

    if (fcstsTimeIncrMinutes < FORECASTS_TIME_INCR_MINUTES_MIN) {

      slog.error(mmi+"fcstsTimeIncrMinutes=" + fcstsTimeIncrMinutes +
          " minutes < FORECASTS_TIME_INCR_MINUTES_MIN=" + FORECASTS_TIME_INCR_MINUTES_MIN + " for station: " + station0Id);

      throw new RuntimeException(mmi+"Cannot update forecast ! station0Id=" + station0Id);
    }

    if (fcstsTimeIncrMinutes > FORECASTS_TIME_INCR_MINUTES_MAX) {

      slog.error(mmi+"cstsTimeIncrMinutes=" + fcstsTimeIncrMinutes +
          " minutes > FORECASTS_TIME_INCR_MINUTES_MAX=" + FORECASTS_TIME_INCR_MINUTES_MAX + " for station: " + station0Id);

      throw new RuntimeException(mmi+"Cannot update forecast ! station0Id=" + station0Id);
    }

    final int fcstDurationHours = lfc0.getParameters().getDurationHours().intValue();

    if (fcstDurationHours > FORECASTS_DURATION_HOURS_MAX) {

      slog.error(mmi+"fcstDurationHours=" + fcstDurationHours +
          " hours > FORECASTS_DURATION_HOURS_MAX=" + FORECASTS_DURATION_HOURS_MAX + " hours for station: " + station0Id);

      throw new RuntimeException(mmi+"Cannot update forecast ! station0Id=" + station0Id);
    }

    // --- TODO: Verify if we can remove the previous checks for the first LegacyFMSContext (index 0) of the
    //   legacyFMSContextList in order to avoid having redundant code.

    // --- Check the other LegacyFMSContext object (if any) in the legacyFMSContextList
    //for (final ForecastingContext fc : forecastingContextList.subList(1, forecastingContextList.size())) {
    for (final LegacyFMSContext lfc: legacyFMSContextList.subList(1, legacyFMSContextList.size())) {

      try {
        lfc.getStationCode();

      } catch (NullPointerException npe) {

        slog.error(mmi+"lfc==null !");
        throw new RuntimeException(npe);
      }

      final String stationCode = lfc.getStationCode();

      //if (!checkForecastingContext(fc)) {
      if (!checkLegacyFMSContext(lfc)) {

        slog.error(mmi+"checkLegacyFMSContext(lfc)==false for station:" + stationCode);
        throw new RuntimeException("Cannot update WLF-QC (with or without WLF-SSE) for station=" + stationCode);
      }

      slog.info(mmi+"Now checking station: "+stationCode);

      //--- Check for null objects here also ??
      final int checkTimeIncr= lfc.getParameters().getDeltaTMinutes().intValue();

      if (checkTimeIncr != fcstsTimeIncrMinutes) {

        slog.error(mmi+"checkTimeIncr=" + checkTimeIncr +
            " minutes != fcstsTimeIncrMinutes=" + fcstsTimeIncrMinutes + " minutes for station: " + stationCode);

        throw new RuntimeException("FMFactory constructor: Cannot update forecast ! station=" + stationCode);
      }

      final int checkFstDurHours = lfc.getParameters().getDurationHours().intValue();

      if (checkFstDurHours != fcstDurationHours) {

        slog.error(mmi+"checkFstDurHours=" + checkFstDurHours +
            " hours != fcstDurationHours=" + fcstDurationHours + " hours for station: " + stationCode);

        throw new RuntimeException(mmi+"Cannot update forecast ! station=" + stationCode);
      }
    }

    this.fcstsDurationSeconds = SECONDS_PER_HOUR * fcstDurationHours;
    this.fcstsTimeIncrSeconds = SECONDS_PER_MINUTE * fcstsTimeIncrMinutes;

    this.data = new FMSWLData(fcstsTimeIncrMinutes, legacyFMSContextList);//forecastingContextList);

    this.log.debug(mmi+"end\n");
  }

  ///**
  // * @param forecastingContext : A ForecastingContext configuration object.
  // * @return true if the ForecastingContext object is OK and ready to use for some(not all) of its attributes.
  // */
  //protected final boolean checkForecastingContext(@NotNull final ForecastingContext forecastingContext) {

  protected final boolean checkLegacyFMSContext(@NotNull final LegacyFMSContext legacyFMSContext) {

    boolean ret = true;

    final String stationId = legacyFMSContext.getStationCode();

    //final FmsParameters fmsParameters = forecastingContext.getFmsParameters();

    if (fmsParameters == null) {
      
      this.log.error("FMSFactory checkForecastingContext: fmsParameters==null for station:" + stationId);
      ret = false;
      
    } else if (fmsParameters.getForecast() == null) {
      
      this.log.error("FMSFactory checkForecastingContext: fmsParameters.getForecast()==null for station:" + stationId);
      ret = false;
      
    } else if (fmsParameters.getForecast().getDeltaTMinutes() == null) {
      
      this.log.error("FMSFactory checkForecastingContext: fmsParameters.getForecast().getDeltaTMinutes()==null for " +
          "station:" + stationId);
      ret = false;
      
    } else if (forecastingContext.getPredictions() == null) {
      
      this.log.error("FMSFactory checkForecastingContext: fc.getPredictions()==null for station:" + stationId);
      ret = false;
    }
    
    return ret;
  }
  
  /**
   * @param wlTimeNode      : A WLTimeNode object.
   * @param nextMeasurement : A Measurement object supposed to be in the future compared to the wlTimeNode object.
   * @return true if nextMeasurement is at this.fcstsTimeIncrSeconds seconds in the future compared to the wlTimeNode
   * object.
   */
  final boolean checkFutureTimeDiff(@NotNull final WLTimeNode wlTimeNode,
                                    @NotNull final MeasurementCustom nextMeasurement) {
    return ((nextMeasurement.getEventDate().getEpochSecond() - wlTimeNode.seconds()) == this.fcstsTimeIncrSeconds);
  }
  
  /**
   * @param pstrWLTimeNode : A WLTimeNode object which have already been processed.
   * @return A new WLTimeNode object with the WL predictions errors residuals computations done and which is in the
   * future compared
   * to the pstrWLTimeNode object.
   */
  @NotNull
  final WLTimeNode getNewFMSTimeNode(@NotNull final WLTimeNode pstrWLTimeNode) {
    
    //--- NOTE: The incremented SecondsSinceEpoch time-stamp is created here.
    return this.data.newFMSTimeNode(pstrWLTimeNode,
        new SecondsSinceEpoch(pstrWLTimeNode.getSse().seconds() + this.fcstsTimeIncrSeconds));
  }
  
  /**
   * @return The current List of WLTimeNode objects.
   */
  @Size(min = 1)
  final List<WLTimeNode> timeNodes() {
    return this.data.timeNodes;
  }
  
  /**
   * FM Service forecast generic method update.
   *
   * @return this generic FMSFactory object.
   */
  abstract public FMSFactory update();
}
