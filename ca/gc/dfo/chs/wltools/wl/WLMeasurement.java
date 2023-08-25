//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl;

// ---
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;

//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
//import ca.gc.dfo.iwls.timeseries.MeasurementCustom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

import java.util.List;
import java.time.Instant;


/**
 * abstract wrapper class for the official IWLS package Measurement class.
 */
abstract public class WLMeasurement implements IWLMeasurement {

  /**
   * private logger utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * To keep track of where we are in a List of WLMeasurement to speed-up searches based on a time-stamp.
   */
  protected int trackIndex = 0;
  /**
   * The wrapped Measurement object(which is only a reference, no new object creation)
   */
  protected MeasurementCustom measurement = null;

  public WLMeasurement() {

    this.trackIndex = 0;
    this.measurement = null;
  }

  /**
   * @param measurement :  The wrapped Measurement object.
   */
  public WLMeasurement(final MeasurementCustom measurement) {
    this.measurement = measurement;
  }
  
  /**
   * @param msm : A Measurement object
   * @return String : The date-time-stamp String representation of the time-stamp of the  Measurement object argument.
   */
  public static String dateTimeString(/*@NotNull*/ final MeasurementCustom msm) {
    return SecondsSinceEpoch.dtFmtString(msm.getEventDate().getEpochSecond(), true);
  }
  
  /**
   * Try to recursively find a Measurement having a specific time-stamp
   * in a List in backward mode and set this.measurement with it if it is found.
   *
   * @param seconds    : The specific time-stamp wanted
   * @param startIndex : The start index in the List
   * @param msma       : The Measurement List.
   * @return WLMeasurement : this
   */
  protected final WLMeasurement findWLBackward(/*@Min(0)*/ final long seconds,
                                               /*@Min(0)*/ final int startIndex,
                                               /*@NotNull @Size(min = 2)*/ final List<MeasurementCustom> msma) {
    
    if (startIndex == 0) {
  
      final MeasurementCustom ms = msma.get(0);
      final long sse = ms.getEventDate().getEpochSecond();
      
      this.measurement = (sse == seconds) ? ms : null;
      this.trackIndex = 0;
      
    } else if (startIndex < msma.size()) {
      
      //--- Verify 1st that the sse wanted is at startIndex
      final MeasurementCustom ms = msma.get(startIndex);
      final long sse = ms.getEventDate().getEpochSecond();
      
      if (sse == seconds) {
        
        this.measurement = ms;
        this.trackIndex = startIndex;
        
      } else if (startIndex > 0) {
        
        //--- NOTE: recursive call:
        this.findWLBackward(seconds, startIndex - 1, msma);
      }
      
    } else {
      
      this.log.warn("startIndex>=msma.size() !");
      this.measurement = null;
      this.trackIndex = 0;
    }
    
    return this;
  }
  
  /**
   * Try to recursively find a Measurement having a specific time-stamp in
   * a List in forward mode and set this.measurement with it if it is found
   *
   * @param seconds    : The specific time-stamp wanted
   * @param startIndex : The start index in the List
   * @param msma       : The Measurement List.
   * @return WLMeasurement : this
   */
  protected final WLMeasurement findWLForward(/*@Min(0)*/ final long seconds,
                                              /*@Min(0)*/ final int startIndex,
                                              /*@NotNull @Size(min = 2)*/ final List<MeasurementCustom> msma) {
    
    this.log.debug("WLMeasurement findWLForward start: seconds dt=" + SecondsSinceEpoch.dtFmtString(seconds, true) +
        ", startIndex=" + startIndex);
    
    if (startIndex == (msma.size() - 1)) {
  
      final MeasurementCustom ms = msma.get(startIndex);
      final long sse = ms.getEventDate().getEpochSecond();
      
      this.measurement = (sse == seconds) ? ms : null;
      this.trackIndex = startIndex;
      
    } else if (startIndex <= (msma.size() - 2)) {
      
      //--- Verify 1st that the sse wanted is at startIndex
      final MeasurementCustom ms = msma.get(startIndex);
      final long sse = ms.getEventDate().getEpochSecond();
      
      if (sse == seconds) {
        
        this.measurement = ms;
        this.trackIndex = startIndex;
        
      } else {
        
        //--- NOTE: recursive call:
        this.findWLForward(seconds, startIndex + 1, msma);
      }
      
    } else {
      
      this.log.warn("startIndex>=msma.size() !");
      this.measurement = null;
      this.trackIndex = 0;
    }
    
    this.log.debug("WLMeasurement findWLForward start: seconds dt=" + SecondsSinceEpoch.dtFmtString(seconds, true) +
        ", this.measurement=" + this.measurement);
    
    return this;
  }
  
  /**
   * @return Instant : this.measurement.getEventDate()
   */
  public final Instant getInstant() {
    return this.measurement.getEventDate();
  }
  
  //--- Kept for possible future usage:
//    @Override
//    final public double getZDoubleError() {
//        return this.measurement.getUncertainty();
//    }
//
//
//    @Override
//    final public double getZDoubleValue() {
//        return this.measurement.getValue();
//    }
  
  /**
   * @return double : (double)this.measurement.getUncertainty()
   */
  @Override
  final public double getDoubleZError() {
    
    this.log.debug("this.measurement=" + this.measurement);
    
    return this.measurement.getUncertainty();
  }
  
  /**
   * @return double : (double)this.measurement.getValue()
   */
  @Override
  final public double getDoubleZValue() {
    return this.measurement.getValue();
  }

//--- Kept for possible future usage
//    /**
//     * Set the Z value error of this.measurement
//     * @param zError
//     * @return double : zError
//     */
//    @Override
//    final public double setZError(@Min(0) final double zError) {
//        this.measurement.setUncertainty(zError);
//        return zError;
//    }
//
//    /**
//     * Set the Z value of this.measurement
//     * @param zValue
//     * @return double : zValue
//     */
//    @Override
//    final public double setZValue(final double zValue) {
//        this.measurement.setValue(zValue);
//        return zValue;
//    }
  
  /**
   * @return long : return this.measurement seconds since the epoch.
   */
  //@Min(0)
  @Override
  final public long seconds() {
    return this.measurement.getEventDate().getEpochSecond();
  }
  
  /**
   * Set the Z value and its error of this.measurement
   *
   * @param zValue : The WL Z value to use for the set operation.
   * @param zError : The WL Z error value to use for the set operation.
   * @return WLMeasurement : this
   */
  @Override
  final public WLMeasurement set(final double zValue,
                                 /*@Min(0)*/ final double zError) {
    
    this.measurement.setValue(zValue);
    this.measurement.setUncertainty(zError);
    
    return this;
  }
}
