//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl;

/**
 * Created by Gilles Mercier on 2017-12-08.
 */

//---
import ca.gc.dfo.chs.wltools.util.ITimeMachine;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
/**
 * ca.gc.dfo.chs.wltools.util.MeasurementCustom;
 * abstract wrapper class that mimics the official IWLS ca.gc.dfo.iwls.timeseries.MeasurementCustom class.
 * This is a placeholder of the same name and we use it to be able to use elsewhere the wltools code
 * developped alongside the IWLS code base. We will then be able to switch back quickly to the official
 * IWLS package MeasurementCustom class usage if needed.
 */
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;

//import ca.gc.dfo.iwls.fmservice.modeling.util.ITimeMachine;
//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
//import ca.gc.dfo.iwls.timeseries.MeasurementCustom;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

//---
//---
//---

/**
 * Class WLMeasurementFinder wraps a List of Measurement objects and implements methods to find Measurements objects
 * from its List for specific time-stamps.
 */
public class WLMeasurementFinder extends WLMeasurement implements ITimeMachine {
  
  /**
   * private logger utility
   */
  final private Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * The first(i.e. the time-stamp of the first Measurement item in the List at index 0) time-stamp
   * of the first item of the wrapped Measurement List.
   */
  private long frstSse = 0L;
  /**
   * The last(i.e. the time-stamp of the last Measurement item in the List at index List.size()-1)
   * time-stamp of the last item of the wrapped Measurement List.
   */
  private long lastSse = 0L;
  /**
   * To flag the time ordering of the Measurement List(We do not assume that the time-stamps of
   * the Measurement List items are going from the least to the more recent in time)
   */
  private ArrowOfTime arrowOfTime = ArrowOfTime.FORWARD;
  /**
   * The List of Measurement objects which is normally retreived from the Oracle JDBC IWLS database.
   */
  private List<MeasurementCustom> dbData = null;
  
  /**
   * @param dbDataArg : The List of Measurement objects which is normally retreived from the Oracle JDBC IWLS database.
   */
  public WLMeasurementFinder(/*@NotNull @Size(min = 1)*/ final List<MeasurementCustom> dbDataArg) {
    
    super();
    
    this.log.debug("WLMeasurementFinder constructor start: this.dbData will be used as reference.");
    this.dbData = dbDataArg;
    
    if (this.dbData.size() == 1) {
      
      this.log.warn("WLMeasurementFinder constructor: this.dbData.size()==1 !");
      this.frstSse = this.lastSse = this.dbData.get(0).getEventDate().getEpochSecond();
      
    } else {
      
      this.frstSse = this.dbData.get(0).getEventDate().getEpochSecond();
      this.lastSse = this.dbData.get(this.dbData.size() - 1).getEventDate().getEpochSecond();
      
      if (frstSse == lastSse) {
        
        this.log.error("WLMeasurementFinder constructor: frstSse==lastSse!");
        throw new RuntimeException("WLMeasurementFinder constructor");
      }
    }
    
    //this.log.debug("frstSse="+ SecondsSinceEpoch.dtFmtString(this.frstSse,true)+", lastSse="+ SecondsSinceEpoch
    // .dtFmtString(this.lastSse,true));
    
    //--- Determine the sense of the ArrowOfTime of the List.
    this.arrowOfTime = ((frstSse < lastSse) ? ArrowOfTime.FORWARD : ArrowOfTime.BACKWARD);
    
    this.log.debug("WLMeasurementFinder construtor end: this.arrowOfTime=" + this.arrowOfTime.toString());
  }
  
  /**
   * @return List of Measurement objects : this.dbData
   */
  final public List<MeasurementCustom> getDbData() {
    return this.dbData;
  }
  
  /**
   * Find the Measurement having a given time-stamp in the wrapped List.
   *
   * @param seconds : The time-stamp to find.
   * @return Measurement : The Measurement if found(null if not found).
   */
  final public MeasurementCustom find(/*@Min(0)*/ final long seconds) {
  
    MeasurementCustom ret = null;
    
    this.log.debug("WLMeasurementFinder get start : seconds dt: " + SecondsSinceEpoch.dtFmtString(seconds, true) + "," +
        " this.trackIndex=" + this.trackIndex);
    
    if (this.dbData != null) {
      
      final long sseCheck = this.dbData.get(this.trackIndex).getEventDate().getEpochSecond();
      
      //this.log.debug("WLMeasurementFinder get: sseCheck dt: "+SecondsSinceEpoch.dtFmtString(sseCheck,true));
      
      if (sseCheck == seconds) {
        ret = this.dbData.get(trackIndex);
      } else {
        ret = (seconds > sseCheck) ? this.findInFuture(seconds) : this.findInPast(seconds);
      }
    }
    
    this.log.debug("WLMeasurementFinder get end: seconds dt: " + SecondsSinceEpoch.dtFmtString(seconds, true) + ", " +
        "this.trackIndex=" + this.trackIndex + ", ret=" + ret);
    
    return ret;
  }
  
  /**
   * Find a specific Measurement in the future.
   *
   * @param seconds : The time-stamp to find.
   * @return Measurement : The Measurement if found(null if not found).
   */
  final public MeasurementCustom findInFuture(/*@Min(0)*/ final long seconds) {
  
    MeasurementCustom ret = null;
    
    this.log.debug("WLMeasurementFinder findInFuture start : seconds dt:" + SecondsSinceEpoch.dtFmtString(seconds,
        true));
    
    if (this.dbData.size() == 1) {
  
      final MeasurementCustom m0 = this.dbData.get(0);
      ret = (m0.getEventDate().getEpochSecond() == seconds) ? m0 : null;
      
    } else {
      ret = (this.arrowOfTime == ArrowOfTime.FORWARD) ? this.findFwdMode(seconds) : this.findBwdMode(seconds);
    }
    
    this.log.debug("WLMeasurementFinder findInFuture end : seconds dt:" + SecondsSinceEpoch.dtFmtString(seconds,
        true) + ", ret=" + ret);
    
    return ret;
  }
  
  /**
   * Find a specific Measurement in the past.
   *
   * @param seconds : The time-stamp to find.
   * @return Measurement : The Measurement if found(null if not found).
   */
  final public MeasurementCustom findInPast(/*@Min(0)*/ final long seconds) {
  
    MeasurementCustom ret = null;
    
    if (this.dbData.size() == 1) {
  
      final MeasurementCustom m0 = this.dbData.get(0);
      ret = (m0.getEventDate().getEpochSecond() == seconds) ? m0 : null;
      
    } else {
      ret = (this.arrowOfTime == ArrowOfTime.FORWARD) ? this.findBwdMode(seconds) : this.findFwdMode(seconds);
    }
    
    return ret;
  }
  
  /**
   * Find a specific Measurement in forward index mode(i.e. increasing indices)from the List
   * for a specific time-stamp in seconds since the epoch.
   *
   * @param seconds : The time-stamp to find in the List.
   * @return Measurement : The Measurement if found(null if not found).
   */
  final public MeasurementCustom findFwdMode(/*@Min(0)*/ final long seconds) {
  
    MeasurementCustom ret = null;
    
    this.log.debug("WLMeasurementFinder findFwdMode start : seconds dt:" + SecondsSinceEpoch.dtFmtString(seconds,
        true) + ", this.trackIndex=" + this.trackIndex);
    
    //--- The check if the Measurement wanted at the time-stamp represented
    //    by the argument seconds is located at trackIndex is already done.
    if (this.trackIndex == (this.dbData.size() - 2)) {
  
      final MeasurementCustom next = this.dbData.get(++this.trackIndex);
      ret = (next.getEventDate().getEpochSecond() == seconds) ? next : null;
      
    } else if (this.trackIndex <= (this.dbData.size() - 3)) {
      
      final int nextIndex = this.trackIndex + 1;
      final MeasurementCustom next = this.dbData.get(nextIndex);
      
      if (next.getEventDate().getEpochSecond() == seconds) {
        
        ret = next;
        this.trackIndex = nextIndex;
        
      } else {
        
        //--- Start the forward search at index nextIndex+1
        this.findWLForward(seconds, nextIndex + 1, this.dbData);
        ret = this.measurement;
      }
    }
    
    this.log.debug("WLMeasurementFinder findFwdMode end : seconds dt:" +
                   SecondsSinceEpoch.dtFmtString(seconds, true) + ", this.trackIndex=" + this.trackIndex + ", ret=" + ret);
    
    return ret;
  }
  
  /**
   * Find a specific Measurement in backward index mode(i.e. decreasing indices)from the List
   * for a specific time-stamp in seconds since the epoch.
   *
   * @param seconds : The time-stamp to find in the List.
   * @return Measurement : The Measurement if found(null if not found).
   */
  final public MeasurementCustom findBwdMode(/*@Min(0)*/ final long seconds) {
  
    MeasurementCustom ret = null;
    
    //--- The check if the Measurement wanted at the time-stamp represented
    //    by the argument seconds is located at trackIndex is already done.
    if (this.trackIndex == 1) {
  
      final MeasurementCustom prev = this.dbData.get(--this.trackIndex);
      ret = (prev.getEventDate().getEpochSecond() == seconds) ? prev : null;
      
    } else if (this.trackIndex >= 2) {
      
      final int prevIndex = this.trackIndex - 1;
      final MeasurementCustom prev = this.dbData.get(prevIndex);
      
      if (prev.getEventDate().getEpochSecond() == seconds) {
        
        ret = prev;
        this.trackIndex = prevIndex;
      } else {
        
        //--- Start the backward search at index prevIndex-1
        this.findWLBackward(seconds, prevIndex - 1, this.dbData);
        ret = this.measurement;
      }
    }
    
    return ret;
  }
  
  /**
   * @return long : The lastSse at the last index in the wrapped List
   */
  //@Min(0)
  final public long getLastSse() {
    return ((this.dbData != null) && (this.dbData.size() > 0)) ?
      this.dbData.get(this.dbData.size() - 1).getEventDate().getEpochSecond() : 0L;
  }
  
  /**
   * @return long : The absolute value of the time-stamps difference between the first two Measurement items of the
   * wrapped List.
   */
  //@Min(0)
  final public long getSecondsIncrement() {

    return Math.abs(this.dbData.get(1).getEventDate().getEpochSecond() -
                    this.dbData.get(0).getEventDate().getEpochSecond());
  }
  
  /**
   * @return int : The size of the wrapped Measurement List
   */
  final public int size() {
    return this.dbData.size();
  }
  
}
