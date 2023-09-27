//package ca.gc.dfo.iwls.fmservice.modeling.fms;
package ca.gc.dfo.chs.wltools.wl.fms;

/**
 *
 */

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.wl.fms.IFMS;
import ca.gc.dfo.chs.wltools.wl.fms.FMSInput;
import ca.gc.dfo.chs.wltools.wl.fms.FMSConfig;
import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.wl.fms.FMSLongTermWLOffset;
import ca.gc.dfo.chs.wltools.wl.fms.legacy.LegacyFMSResidual;

//---
//import ca.gc.dfo.iwls.fmservice.modeling.ForecastingContext;
//import ca.gc.dfo.iwls.fmservice.modeling.fms.legacy.LegacyFMSResidual;
//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
//import ca.gc.dfo.iwls.timeseries.MeasurementCustom;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
//import java.util.List;
//import java.util.ArrayList;
//---2
//---
//---

/**
 * Generic class for WL errors residuals computations. It allows the use of more than one stand-alone(without
 * external storm surge forecasts)
 * method depending on the WL stations being processed.
 */
abstract public class FMSResidualFactory extends FMSLongTermWLOffset implements IFMS {

  private static final String whoAmI= "ca.gc.dfo.chs.wltools.wl.fms.FMSResidualFactory";

  /**
   * static log utility.
   */
  private static final Logger slog= LoggerFactory.getLogger(whoAmI);

  ///**
  // * log utility.
  // */
  //private final Logger log = LoggerFactory.getLogger(this.getClass());

  //--- For possible future usage.
  //protected double forecastDuration= (double)FORECASTS_DURATION_HOURS*SECONDS_PER_HOUR;
  /**
   * One generic residual object for each WL station so each residual is related to one main WL station.
   */
  protected String stationId= null;

  /**
   * Time-stamp of the last update of residual errors statistics:
   */
  protected SecondsSinceEpoch lastUpdateSse= null;

  /**
   * To keep track of all the WLStationTimeNode already processed.(NOTE: the pstr objects references of all the
   * WLStationTimeNode chained objects MUST be valid)
   */
  protected WLStationTimeNode lastLagNodeAdded= null;

  /**
   * CovData: Equivalent of C struct model_status_type in the source file dvfm.c of the 1990 legacy ODIN C source
   * code bundle.
   */
  protected FMSCov covData= null;

  /**
   * The WL errors residuals computations method used for a given WL station(see IFM interface and or confluance
   * documentation for more details)
   */
  private IFMS.ResidualMethod residualMethod= IFMS.ResidualMethod.LEGACY;

  /**
   * Missing WLOs data counter. It logged at the end of the computations.
   */
  private int nbMissingWLO = 0;

  //--- constructors for possible future usage.
//    public FMResidualFactory() { }
//
//    public FMResidualFactory(final String stationId) {
//      this(stationId,ResidualMethod.LEGACY);
//    }

  /**
   * @param stationId    : The usual CHS TG station string Id.
   * @param residualMethod : The ResidualMethod type to use.
   */
  public FMSResidualFactory(/*@NotNull*/ final String stationId, /*@NotNull*/ final IFMS.ResidualMethod residualMethod) {

    super();

    final String mmi= "FMSResidualFactory main constructor: ";

    this.stationId = stationId;
    this.residualMethod = residualMethod;

    this.lastUpdateSse = new SecondsSinceEpoch(0L);
    this.covData = null;

    this.nbMissingWLO = 0;

    slog.info(mmi+"this.stationCode=" +this.stationCode+
              ", this.residualMethod=" + this.residualMethod);
  }

  final String getStationId() {
    return this.stationId;
  }

  /**
   * @param forecastingContext : A WL station ForecastingContext object.
   * @param lastWLOSse         : The  time-stamp seconds of the last available valid WLO data of the same WL station.
   * @return The IFMResidual object of the WL station.
   */
  //@NotNull
 // protected static IFMSResidual getIFMSResidual(@NotNull final ForecastingContext forecastingContext,
  protected static IFMSResidual getIFMSResidual(@NotNull final FMSInput fmsInput, /*@Min(0)*/ final long lastWLOSse) {

    try {
      //forecastingContext.getFmsParameters();
      fmsInput.getStationId();

    } catch (NullPointerException npe) {

      slog.error(mmi+"fmsInput is null !!");
      throw new RuntimeException(mmi+npe);
    }

    try {
    //  //forecastingContext.getFmsParameters().getResidual();
      fmsInput.getFMSResidualConfig().getMethod();

    } catch (NullPointerException npe) {
      slog.error(mmi+"fmsInput.getFMSResidualConfig() returns null !!");
      throw new RuntimeException(mmi+npe);
    }

    slog.info(mmi+"Start");

    final String residualMethodCheck= fmsInput.getFMSResidualConfig().getMethod(); //forecastingContext.getFmsParameters().getResidual().getMethod();

    try {
      residualMethodCheck.length();

    } catch (NullPointerException npe) {

      slog.error(mmi+"residualMethodCheck is null !!");
      throw new RuntimeException(mmi+npe);
    }

    IFMSResidual ret = null;

    final IFMS.ResidualMethod residualMethodWanted=
      FMSResidualFactory.getResidualMethod(residualMethodCheck);

    try {
      residualMethodWanted.hashCode();

    } catch (NullPointerException npe) {

      slog.error(mmi+"residualMethodWanted==null !!");
      throw new RuntimeException(mmi+npe);
    }

    switch (residualMethodWanted) {

      case LEGACY:

        ret= new LegacyFMSResidual().getIFMSResidual(fmsInput, lastWLOSse);
        break;

//            case SPECTRAL_NUDGING:
//                ret= new SpectralNudgingResidual().getIFMSResidual(fc,lastWLOSse);
//                break;

      default:

        slog.error(mmi+"Invalid ResidualMethod type -> " + residualMethodCheck);
        throw new RuntimeException(mmi+"Cannot update forecast !!");
    }

    if (ret == null) {

      slog.error(mmi+"Invalid WL residual errors computation method: " + residualMethodCheck);
      throw new RuntimeException(mmi+"ret==null !");

    } else {
      slog.info(mmi+"Will use ResidualMethod -> " + residualMethodWanted.toString());
    }

    slog.debug(mmi+"end");

    return ret;
  }

  /**
   * @param residualMethodCheck : A String representing the residual method wanted.
   * @return The corresponding ResidualMethod object if found, null object otherwise. The client metho must then
   * check for a null return.
   */
  protected final static IFMS.ResidualMethod getResidualMethod(final String residualMethodCheck) {

    final String mmi= "getResidualMethod: ";

    IFMS.ResidualMethod residualMethodWanted = null;

    for (final IFMS.ResidualMethod residualMethod : ResidualMethod.values()) {

      if (residualMethodCheck.equals(residualMethod.toString())) {

        residualMethodWanted= residualMethod;
        //staticLog.debug("FMResidualFactory  Will use WL residual errors computation method: "+residualMethod
        // .toString() );
        break;
      }
    }

    if (residualMethodWanted == null) {

      slog.error(mmi+"Invalid ResidualMethod -> " + residualMethodCheck);
      throw new RuntimeException(mmi+"Cannot update forecast !!");
    }

    return residualMethodWanted;
  }

  /**
   * @param pstrWLStationTimeNode : A WLStationTimeNode object which is just before in time compared to the
   *                              SecondsSinceEpoch sse object time-stamp.
   * @param sse                   : A SecondsSinceEpoch object having the time-stamp of the new WLStationTimeNode
   *                              returned.
   * @param sseFutureThreshold    : The seconds since the epoch which is the first time-stamp of the future(compared
   *                              with actual real-time not with the last valid WLO).
   * @param fmwlStation           : A FMWLStation object used in the processing computations.
   * @return A new processed WLStationTimeNode which can be used subsequently later.
   */
  //@NotNull
  protected static final WLStationTimeNode processFMSWLStation(/*@NotNull*/ final WLStationTimeNode pstrWLStationTimeNode,
                                                               /*@NotNull*/ final SecondsSinceEpoch sse,
                                                               /*@Min(0)*/  final long sseFutureThreshold,
                                                               /*@NotNull*/ final FMSWLStation fmwlStation) {
    final String mmi= "processFMSWLStation: ";

    final String stationId= fmwlStation.getStationId();

    final String dts= sse.dateTimeString(true);

    slog.info(mmi+"start: stationId=" + stationId + ", sse dts=" + dts);
    slog.info(mmi+"psr=" + pstrWLStationTimeNode);
    slog.info(mmi+"sseFutureThreshold dt=" + SecondsSinceEpoch.dtFmtString(sseFutureThreshold, true));
    slog.info(mmi+"station.lastWLOSse dt=" + SecondsSinceEpoch.dtFmtString(fmwlStation.lastWLOSse, true));

    final long seconds= sse.seconds();

    final FMSWLMeasurement[] stationMeasurementsRef= FMSWLMeasurement.
      getMeasurementsRefs(fmwlStation.getDataReferences(seconds), new FMSWLMeasurement[WLType.values().length]);

    final boolean stillGotWLOs= (seconds <= fmwlStation.lastWLOSse);

    final WLStationTimeNode wlstn = fmwlStation.residual.
      processWLStationTimeNode(stationCode, stillGotWLOs,
        fmwlStation.residual.newFMSTimeNode(pstrWLStationTimeNode, sse, stationMeasurementsRef));

    if (seconds >= sseFutureThreshold) {

      slog.debug(mmi+"seconds >= sseFutureThreshold: updating station: " + stationId + " forecast.");

      if (fmwlStation.useFullModelForecast) {

        //  slog.error("FMResidualFactory processFMSWLStation: The merging of default forecasts with external storm " +
        //      "surge forecast not available yet !");
        //  throw new RuntimeException("FMResidualFactory processFMSWLStation method");
        //--- Uncomment the following two lines when the merge of the default forecast with an external storm surge
        // forecast will be implemented.
        slog.info(mmi+"Merging the QC forecast with a full model forecast, dt=" + SecondsSinceEpoch.dtFmtString(seconds, true) );

        fmwlStation.mergeWithFullModelForecast( seconds, sseFutureThreshold, wlstn);
      }

      //--- Populate the updated WL forecasts data.
      fmwlStation.updatedForecastData.add(wlstn.getUpdatedForecast());

      //--- block for testing purposes:
      //if (seconds%3600L==0) {
      ////if (seconds==(sseFutureThreshold+7200L)) {
//            staticLog.debug("FMResidualFactory processFMSWLStation: System.exit(0)");
//            System.exit(0);
      //}

    }

    slog.info(mmi+"end: stationId=" + stationId + ", sse dts=" + dts);

    return wlstn;
  }

  /**
   * @param residualMethodCheck    : A String representing the residual method wanted.
   * @param forecastingContextList : A List(min. size==1) of ForecastingContext object(s).
   * @return true if the relevant configuration data of the ForecastingContext object(s) is(are) ready to use, false
   * otherwise.
   */
  protected static final boolean validateStationsFMSConfig(/*@NotNull*/ final String residualMethodCheck,
                                                           /*@NotNull @Size(min = 1)*/ final List<FMSConfig> fmsConfigList) {
                                                           ///*@NotNull @Size(min = 1)*/ final List<ForecastingContext> forecastingContextList) {

    final String mmi= "validateStationsFMSConfig: ";

    boolean ret= true;

    final ResidualMethod residualMethodWanted= getResidualMethod(residualMethodCheck);

    switch (residualMethodWanted) {

      case LEGACY:

        LegacyFMSResidual.validateFMSConfig(fmsConfigList);
        //LegacyFMSResidual.validateFMConfigParameters(forecastingContextList);
        break;

//            case SPECTRAL_NUDGING:
//
//                ret= new SpectralNudgingResidual.validateFMConfigParameters(fcList);
//                break;

      default:

        slog.error(mmi+"Invalid ResidualMethod type -> " + residualMethodCheck);
        throw new RuntimeException(mmi+"method");

        //---
        //break;
    }

    return ret;
  }

  /**
   * @param seconds : The time-stamp lag in seconds of the past at which we want a stored WLStationTimeNode.
   * @return The wanted WLStationTimeNode object which is at is time lag seconds if found, null object otherwise
   * NOTE: The client method must then check for a null return.
   */
  protected final WLStationTimeNode getLagFMSWLStationTimeNode(/*@Min(0)*/ final long seconds) {

    final String mmi= "getLagFMSWLStationTimeNode: ";

    slog.info(mmi+"this.stationId=" + this.stationId + ", seconds " + "dt=" + SecondsSinceEpoch.dtFmtString(seconds, true));

    slog.info(mmi+"this.lastLagNodeAdded=" + this.lastLagNodeAdded);

    if (this.lastLagNodeAdded != null) {
      slog.info(mmi+"this.lastLagNodeAdded dt=" + this.lastLagNodeAdded.getSse().dateTimeString(true));
    }

    //--- NOTE: Using recursive function findInPastR(seconds) of class TimeNodeFactory.
    return (this.lastLagNodeAdded != null) ? (WLStationTimeNode) this.lastLagNodeAdded.findInPastR(seconds) : null;
  }

  /**
   * @return this.nbMissingWLO
   */
  public final int getNbMissingWLO() {
    return this.nbMissingWLO;
  }

  /**
   * @return The current number of missing WLOs data.
   */
  public final int incrNbMissingWLO() {
    return (++this.nbMissingWLO);
  }

  /**
   * @param stationId     : The usual SINECO station String Id.
   * @param residualsList : A List(min. size ==1) of IFMResidual to use to properly set the temporal WL errors
   *                      covariance data structure(s).
   * @return The FMResidualFactory object itself.
   */
  //@NotNull
  public final FMSResidualFactory setAuxCovsResiduals(/*@NotNull*/ final String stationId,
                                                      /*@NotNull @Size(min = 1)*/ final List<IFMSResidual> residualsList) {

    this.covData.setAuxCovsResiduals(stationId, residualsList);

    return this.setupCheck();
  }

  /**
   * @param pstrWLStationTimeNode : A WLStationTimeNode object which is just before in time compared to the
   *                              SecondsSinceEpoch sse object time-stamp.
   * @param sse                   : A SecondsSinceEpoch with the next(in the future) time stamp to use.
   * @param data                  : A List of 4 FMSWLMeasurement(PREDICTION, OBSERVATION, FORECAST, EXT_STORM_SURGE
   *                              (which could be
   *                              NULL)) objects.
   * @return A new WLStationTimeNode object ready to be used.
   */
  //@NotNull
  abstract public WLStationTimeNode getFMSTimeNode(final WLStationTimeNode pstrWLStationTimeNode,
                                                   /*@NotNull*/ final SecondsSinceEpoch sse,
                                                   /*@NotNull @Size(min = 4)*/ final FMSWLMeasurement[] data);
  /**
   * @return The time-stamp seconds of the 1st WL data time-stamp in the past used by the residual method(it can vary
   * depending on the method used).
   */
  /*@Min(0)*/
  abstract public long getSseStart();

  /**
   * Do the setup of the residual method before starting the computations.
   *
   * @param lastWLOSse                  : The time-stamp seconds of the last available valid WLO data for a given WL
   *                                    station
   * @param predictionsMeasurementsList : A list of WL predictions(tidal or climatology or a mix of both) Measurement
   *                                    objects.
   * @return The time-stamp seconds of the 1st WL data time-stamp in the past used by the residual method(it can vary
   * depending on the method used).
   */
  abstract public long setup(final long lastWLOSse,
                             /*@NotNull @Size(min = 1)*/ final List<MeasurementCustom> predictionsMeasurementsList);

  /**
   * Check the residual method setup before starting the computations.
   *
   * @return The FMResidualFactory object.
   */
  //@NotNull
  abstract public FMSResidualFactory setupCheck();
}
