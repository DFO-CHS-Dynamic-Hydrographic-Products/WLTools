package ca.gc.dfo.chs.wltools.wl.fms;

//import ca.gc.dfo.iwls.fmservice.modeling.ForecastingContext;

import ca.gc.dfo.chs.wltools.wl.fms.IFMS;
import ca.gc.dfo.chs.wltools.wl.WLTimeNode;
import ca.gc.dfo.chs.wltools.wl.fms.FMSInput;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.wl.fms.FMSFactory;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
//import ca.gc.dfo.chs.wltools.wl.fms.LegacyFMSContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

//---
//---
//---

/**
 * FM Service master class.
 * Created by Gilles Mercier on 2017-12-04.
 * Adapted for Spine 2.0 API by Gilles Mercier on 2023-09-19.
 */
public final class FMS extends FMSFactory implements IFMS {

  private final static String whoAmI= "ca.gc.dfo.chs.wltools.wl.fms.FMS";

  /**
   * static log utility.
   */
  final static private Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * Default constructor.
   */
  public FMS() {
    super();
  }

  /**
   * @param forecastingContext : A ForecastingContext object
   */
  //public FMS(@NotNull final ForecastingContext forecastingContext) {

  /**
   * @param 
   */
  public FMS(/*@NotNull*/ final FMSInput fmsInput ) {

    //this(getFcList(forecastingContext));

    this( getSingleFMSIList(fmsInput) );
  }

  /**
   * @param forecastingContextList : A List(min size==1) of ForecastingContext objects
   */
  //private FMS(@NotNull @Size(min = 1) final List<ForecastingContext> forecastingContextList) {
  //  super(forecastingContextList);

  /**
   * @param legacyFMSContextList : A List(min size==1) of LegacyFMSContext objects
   */
  private FMS(/*@NotNull @Size(min = 1)*/ final List<FMSInput> fmsInputList) {
    super(fmsInputList);
  }

  /**
   * Trick method to get a List with a single ForecastingContext object.
   *
   * @param fmsInput : A FMSInput object.
   * @return A new List of one FMSInput object (i.e. fmsInput).
   */
  //@NotNull
  //private static List<ForecastingContext> getSingleFcList(@NotNull final ForecastingContext forecastingContext) {

  private static List<FMSInput> getSingleFMSIList(/*@NotNull*/ final FMSInput fmsInput) {

    //final List<ForecastingContext> fcList = new ArrayList<>(1);
    //fcList.add(forecastingContext);

    final List<FMSInput> singleFMSIList = new ArrayList<FMSInput>(1);

    singleFMSIList.add(fmsInput);

    return singleFMSIList;
  }

  /**
   * Get a new WL forecast for one or more WL stations.
   *
   * @return A generic FMSFactory object.
   */
  @Override
  public final FMSFactory update() {

    final String mmi= "update: ";

    final long t0 = Instant.now().toEpochMilli();

    slog.info(mmi+"start.");

    //--- Get the 1st WLTimeNode which must be already processed.
    WLTimeNode wlTimeNodeIter = this.timeNodes().get(0);

    try {
      wlTimeNodeIter.getSse();

    } catch (NullPointerException npe) {

      slog.info(mmi+"wlTimeNodeIter==null!");
      throw new RuntimeException(npe);
    }

    slog.info(mmi+"pstr0 date-time: " + wlTimeNodeIter.getSse().dateTimeString(true));

    try {
      this.data.allStationsData.size();

    } catch (NullPointerException npe) {

      slog.error(mmi+"this.data.allStationsData==null!");
      throw new RuntimeException(npe);
    }

    final String station0Id= this.data.
      allStationsData.get(0).getStationId();

    slog.info(mmi+"station0Id=" + station0Id);

    final List<MeasurementCustom> predictionsMeasurements0=
      this.data.allStationsData.get(0).getMeasurementList(WLType.PREDICTION);

    try {
      predictionsMeasurements0.size();

    } catch (NullPointerException npe) {

      slog.error(mmi+"predictionsMeasurements0==null!");
      throw new RuntimeException(npe);
    }

    slog.info(mmi+"prd1 date-time1: "+SecondsSinceEpoch.
      dtFmtString(predictionsMeasurements0.get(1).getEventDate().getEpochSecond(), true));

    //--- Iteration on WLP data which MUST have all time-stamps for the total time duration(past and future)
    for (final MeasurementCustom prdmIter:
           predictionsMeasurements0.subList(1,predictionsMeasurements0.size()) ) {

      try {
        prdmIter.hashCode();

      } catch (NullPointerException npe) {

        slog.error(mmi+"prdmIter==null! station0Id=" + station0Id);
        throw new RuntimeException(npe);
      }

      //--- Check if the time increment is constant AND in the future compared to the previous WLP data.
      //    Stop exec if not because this is a severe error.
      if (!this.checkFutureTimeDiff(wlTimeNodeIter, prdmIter)) {

        slog.error(mmi+"Invalid time-stamp difference between previous iteration WLTimeNode at:"+
          wlTimeNodeIter.getSse().dateTimeString(true)+" and next Measurement data at:"+WLMeasurement.dateTimeString(prdmIter));

        slog.error(mmi+"It should be " + this.fcstsTimeIncrSeconds + " seconds between them !");

        throw new RuntimeException(mmi+"Cannot update forecast ! station0Id=" + station0Id);
      }

      slog.debug(mmi+"prdm=" + prdmIter + ", prdm date-time=" + SecondsSinceEpoch.
        dtFmtString(prdmIter.getEventDate().getEpochSecond()) + ", pstr dt=" + wlTimeNodeIter.getSse().dateTimeString(true));

      //--- Build a new and processed FMS time node with the previous FMS time node as argument.

      wlTimeNodeIter = this.getNewFMSTimeNode(wlTimeNodeIter);

      slog.info(mmi+"done with WLTimeNode date-time processing: "+
        wlTimeNodeIter.getSse().dateTimeString(true) + "\n");
    }

    final long tf= Instant.now().toEpochMilli();

    //--- Report the number of missing WLO data items for each station.
    for (final FMSWLStation station : this.data.allStationsData) {

     slog.info(mmi+"Nb. missing WLO Measurement objects for station: "+
               station.getStationId() + " is " + station.getIFMSResidual().getNbMissingWLO());
    }

    slog.info(mmi+"t0 dt=" + SecondsSinceEpoch.dtFmtString(t0 / SEC_TO_MILLISEC, true));
    slog.info(mmi+"tf dt=" + SecondsSinceEpoch.dtFmtString(tf / SEC_TO_MILLISEC, true));
    slog.info(mmi+"End method FM update: milliseconds elapsed=" + (tf - t0));

    //--- Results Validation, remove for production code.
    //this.data.writeResults("C:\\Users\\MercierGi\\Data\\tmp");

    return this;
  }
}
