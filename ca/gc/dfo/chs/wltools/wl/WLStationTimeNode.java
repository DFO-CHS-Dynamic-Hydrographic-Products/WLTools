//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl;

/**
 *
 */

//---
import org.slf4j.Logger;
import java.time.Instant;
import org.slf4j.LoggerFactory;
//import javax.validation.constraints.NotNull;

import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.TimeNodeFactory;
/**
 * ca.gc.dfo.chs.wltools.util.MeasurementCustom;
 * abstract wrapper class that mimics the official IWLS ca.gc.dfo.iwls.timeseries.MeasurementCustom class.
 * This is a placeholder of the same name and we use it to be able to use elsewhere the wltools code
 * developped alongside the IWLS code base. We will then be able to switch back quickly to the official
 * IWLS package MeasurementCustom class usage if needed.
 */
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;

//---
//---
//---

/**
 *      class WLStationTimeNode contains references to PREDICTION, OBSERVATION, FORECAST and
 *  possibly EXT_STORM_SURGE(if any) data from the JDBC database contained in an array of
 *  WLMeasurement wrapping objects for one WL station and for the same seconds-since-epoch time
 *  stamp.(i.e. the seconcds-since-epoch time-stamps for the four WL types MUST be the same).
 *
 *     This WLStationTimeNode object structure allows a better control on data time-synchorization
 *  which is the main problem(i.e. the main cause of nasty bugs)with the traditional use of
 *  separate arrays for each WL type.
 */
public class WLStationTimeNode extends TimeNodeFactory implements IWL {

  final private static String whoAmI= "ca.gc.dfo.chs.wltools.wl.WLStationTimeNode";

  /**
   * static private log utility
   */
  private static final Logger slog= LoggerFactory.getLogger(whoAmI);

  //protected EnumMap<WLType,WLMeasurement> data= new EnumMap<>(WLType.class);
  /**
   * A reference to an already existing array of WLMeasurement[PREDICTION, OBSERVATION, QC_FORECAST or MODEL_FORECAST
   * (if any)] objects.
   */
  protected WLMeasurement[] wlmData= null;

  /**
   * ZWL object for storing the computed or estimated residual surge component.
   */
  protected WLZE surge= null;

  /**
   * Measurement object for storing the updated forecasted WL value for a given time-stamp.
   */
  private MeasurementCustom updatedForecast = null;

  public WLStationTimeNode() {

    this.wlmData = null;

    this.surge = new WLZE(0.0, 0.0);

    this.updatedForecast = null;
  }

  //--- for possible future usage:
//    public WLStationTimeNode(@NotNull final WLStationTimeNode from) {
//
//        super(from,false);
//
//        //--- Inline references assignations to get optimal performance instead of looping on WLType values:
//        this.data[PREDICTION]     = from.data[PREDICTION];
//        this.data[OBSERVATION]    = from.data[OBSERVATION];
//        this.data[FORECAST]       = from.data[FORECAST];
//        this.data[EXT_STORM_SURGE]= from.data[EXT_STORM_SURGE];
//
////        for (final WLType type : IWL.WLType.values() ) {
////            this.data.put(type,from.data.get(type));
////        }
//
//        if (from.surge==null) {
//            this.surge= new WL(0.0,0.0);
//        } else {
//            this.surge= from.surge;
//        }
//    }

  /**
   * @param pstr:   (could be null) The WLStationTimeNode object which is just before(i.e. in the past) in time
   *                compared to the SecondsSinceEpoch argument.
   * @param sse:    An already existing SecondsSinceEpoch object containing the UTC time-stamp since the epoch for this
   *                WLStationTimeNode object.
   * @param wlmData: An already existing array of WLMeasurement type containing the references to PREDICTION,
   *                OBSERVATION, FORECAST or EXT_STORM_SURGE WL data.
   */
  public WLStationTimeNode(final WLStationTimeNode pstr,
                           /*@NotNull*/ final SecondsSinceEpoch sse,
                           /*@NotNull*/ final WLMeasurement[] wlmData) {

    super(sse, pstr, null);

    //final String mmi= "WLStationTimeNode main constructor: ";

    //slog.info(mmi+"this="+this+", this dt=" + this.sse.dateTimeString(true));

    //if (pstr != null) {
    //  slog.info(mmi+"pstr="+pstr+", pstr.futr="+pstr.futr+
    //            ", pstr dt=" + pstr.sse.dateTimeString(true));
    //}

    //--- RECALL: this.dbData is just a reference to na already existing array of WLMeasurement objects:
    this.wlmData= wlmData;

    this.updatedForecast= null;

    this.surge= new WLZE(0.0, 0.0);
  }

  /**
   * Validate the time synchronization of all the WLMeasurement objects contained in the dbData arrray of a
   * WLStationTimeNode object.
   *
   * @return boolean
   */
  //@NotNull
  public final boolean checkTimeSync() {

    final long prdSse= this.wlmData[PREDICTION].seconds();

    boolean ret= true; //( (prdSse == this.data[FORECAST].seconds()) && (prdSse == this.data[EXT_STORM_SURGE]
    // .seconds()) );

    if (this.wlmData[OBSERVATION].measurement != null) {
      ret= (ret && (prdSse == this.wlmData[OBSERVATION].seconds()));
    }

    if (this.wlmData[QC_FORECAST].measurement != null) {
      ret= (ret && (prdSse == this.wlmData[QC_FORECAST].seconds()));
    }

    if (this.wlmData[MODEL_FORECAST].measurement != null) {
      ret= (ret && (prdSse == this.wlmData[MODEL_FORECAST].seconds()));
    }

    ret= (ret && (this.sse.seconds() == prdSse));

    return ret;
  }

  /**
   * Return the WLMeasurement wanted according to the WLType argument provided that the underlying wrapped
   * Measurement object is not null.
   *
   * @param type WLType PREDICTION, OBSERVATION, QC_FORECAST or MODEL_FORECAST
   * @return the WLMeasurement object  wanted according to the WLType argument.
   */
  final public WLMeasurement get(/*@NotNull*/ final WLType type) {

    final WLMeasurement checkIt= this.wlmData[type.ordinal()];

    return (checkIt.measurement != null) ? checkIt : null;
  }

  /**
   * @return double : The surge error(uncertainty) of a WLStationTimeNode object.
   */
  //@NotNull
  public final double getSurgeError() {
    return this.surge.error;
  }

  /**
   * @return double : The surge value(z elevation) of a WLStationTimeNode object.
   */
  //@NotNull
  public final double getSurgeZw() {
    return this.surge.zw;
  }

  /**
   * Compute a surge with the FORECAST intead of the OBSERVATION.
   *
   * @return double : the surge computed with the FORECAST data
   */
  //@NotNull
  public final double getQCFSurge() {

    //this.log.debug("this.dbData[FORECAST]="+this.dbData[FORECAST]);
    //this.log.debug("this.dbData[PREDICTION]="+this.dbData[PREDICTION]);

    return this.surge.setZw(this.wlmData[QC_FORECAST].getDoubleZValue() -
                            this.wlmData[PREDICTION].getDoubleZValue());
  }

  /**
   * Return the updated Measurement of a WLStationTimeNode object.
   *
   * @return The updated forecast Measurement object.
   */
  public MeasurementCustom getUpdatedForecast() {
    return this.updatedForecast;
  }

  /**
   * To merge stand-alone data validation forecast data value with external WL storm surge data according to the
   * StormSurgeWLType argument
   *
   * @param stormSurgeWLType : storm surge data type(WLSSF_FULL or WLSSF_DE_TIDED)
   * @param ssfWeight        : The time-interpolation weight used for the merge
   * @return WLStationTimeNode
   */
  //@NotNull
  final public WLStationTimeNode mergeWithFullModelForecast(/*@NotNull*/ final SurgeOffsetWLType surgeOffsetWLType, final double fmfWeight) {

    return ( (surgeOffsetWLType == SurgeOffsetWLType.WLSO_FULL) ?
             this.mergeFullModelForecast(fmfWeight) : this.mergeDeTidedFMF(fmfWeight) );
  }

  /**
   * To merge stand-alone data validation forecast data value with external full(tide+surge) WL storm surge data type.
   *
   * @param ssfWeight : The time-interpolation weight used for the merge
   * @return WLStationTimeNode
   */
  //@NotNull
  final private WLStationTimeNode mergeFullModelForecast(final double fmfWeight) {

    final double mergedValue = ((1.0 - fmfWeight) * this.updatedForecast.getValue() +
                                fmfWeight * this.wlmData[MODEL_FORECAST].getDoubleZValue());

    this.updatedForecast.setValue(mergedValue);

    return this;
  }

  /**
   * To merge stand-alone data validation forecast with external de-tided(surge only) WL storm surge data type.
   *
   * @param ssfWeight : The time-interpolation weight used for the merge
   * @return WLStationTimeNode
   */
  //@NotNull
  final public WLStationTimeNode mergeDeTidedFMF(final double fmfWeight) {

    //--- Only need to add time weighted de-tided storm-surge to the forecast signal data:
    final double mergedValue= (this.updatedForecast.getValue() +
                               fmfWeight * this.wlmData[MODEL_FORECAST].getDoubleZValue());

    this.updatedForecast.setValue(mergedValue);

    return this;
  }

  /**
   * Set the newly updated stand-alone data validation forecast data value of a WLStationTimeNode object.
   *
   * @param instant              : Already existing and fully defined Instant object with the same time-stamp as the
   *                             WLStationTimeNode object.
   * @param updatedForecastValue : The newly updated stand-alone data validation forecast data value.
   * @param updatedForecastError : The uncertainty of the newly updated stand-alone data validation forecast data value.
   * @return Measurement : new Measurement object which contains the newly updated stand-alone data validation
   * forecast data value.
   */
  //@NotNull
  public final MeasurementCustom setUpdatedforecast(/*@NotNull*/ final Instant instant,
                                                    final double updatedForecastValue,
                                                    final double updatedForecastError) {

    final String mmi= "setUpdatedforecast: ";

    this.updatedForecast= (this.updatedForecast == null) ?
                           new MeasurementCustom() : this.updatedForecast;

    //--- Fool-proof validation for the time-synchronization of this WLStationTimeNode with the Instant object:
    if (instant.getEpochSecond() != this.seconds()) {

      slog.error(mmi+"instant.getEpochSecond()!=this.seconds()");
      throw new RuntimeException(mmi);
    }

    this.updatedForecast.setEventDate(instant);
    this.updatedForecast.setValue(updatedForecastValue);
    this.updatedForecast.setUncertainty(updatedForecastError);

    return this.updatedForecast;
  }

  /**
   * Set the ZWL surge attributes
   *
   * @param zw    : The surge value(z elevation) of a WLStationTimeNode object.
   * @param error : The surge error(uncertainty) of a WLStationTimeNode object.
   * @return ZWL : the newly updated ZWL object of a WLStationTimeNode object.
   */
  //@NotNull
  public final WLZE setSurge(final double zw, final double error) {
    return this.surge.set(zw, error);
  }
}
