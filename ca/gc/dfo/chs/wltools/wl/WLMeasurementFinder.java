//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl;

/**
 * Created by Gilles Mercier on 2017-12-08.
 */

import java.util.List;
import java.time.Instant;

// --
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//---
import ca.gc.dfo.chs.wltools.util.ITimeMachine;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;

//import ca.gc.dfo.iwls.fmservice.modeling.util.ITimeMachine;
//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
//import ca.gc.dfo.iwls.timeseries.MeasurementCustom;
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

  static final private String whoAmI= "ca.gc.dfo.chs.wltools.wl.WLMeasurementFinder";

  /**
   * static private logger utility
   */
  static final private Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * The first(i.e. the time-stamp of the first Measurement item in the List at index 0) time-stamp
   * of the first item of the wrapped Measurement List.
   */
  private long frstSse= 0L;

  /**
   * The last(i.e. the time-stamp of the last Measurement item in the List at index List.size()-1)
   * time-stamp of the last item of the wrapped Measurement List.
   */
  private long lastSse= 0L;

  /**
   * To flag the time ordering of the Measurement List(We do not assume that the time-stamps of
   * the Measurement List items are going from the least to the more recent in time)
   */
  private ArrowOfTime arrowOfTime= ArrowOfTime.FORWARD;

  /**
   * A List of MeasurementCustom objects.
   */
  private List<MeasurementCustom> mcDataList= null;

  /**
   * @param mcDataList: A List of MeasurementCustom objects.
   */
  public WLMeasurementFinder(/*@NotNull @Size(min = 1)*/ final List<MeasurementCustom> mcDataList) {

    super();

    final String mmi= "WLMeasurementFinder constructor: ";

    slog.debug(mmi+"start");

    this.mcDataList= mcDataList;

    // --- Add a try-except fool-proof block here for this.mcDataList ?
    this.frstSse= this.mcDataList.
      get(0).getEventDate().getEpochSecond();

    if (this.mcDataList.size() == 1) {

      slog.debug(mmi+"this.mcDataList.size()==1 !, setting this.lastSse= this.frstSse !!");

      this.lastSse= this.frstSse;
      //  this.mcDataList.get(0).getEventDate().getEpochSecond();

    } else {

      this.lastSse = this.mcDataList.
        get(this.mcDataList.size() - 1).getEventDate().getEpochSecond();

      if (frstSse == lastSse) {

        slog.error(mmi+"frstSse==lastSse!");
        throw new RuntimeException(mmi+"Cannot update QC and full model forecast !!");
      }
    }

    //this.log.debug("frstSse="+ SecondsSinceEpoch.dtFmtString(this.frstSse,true)+", lastSse="+ SecondsSinceEpoch
    // .dtFmtString(this.lastSse,true));

    //--- Determine the sense of the ArrowOfTime of the List.
    this.arrowOfTime = ((frstSse < lastSse) ? ArrowOfTime.FORWARD : ArrowOfTime.BACKWARD);

    slog.debug(mmi+"end: this.arrowOfTime=" + this.arrowOfTime.toString());
  }

  /**
   * @return List of Measurement objects : this.dbData
   */
  final public List<MeasurementCustom> getMCDataList() {
    return this.mcDataList;
  }

  /**
   * Find the Measurement having a given time-stamp in the wrapped List.
   *
   * @param seconds : The time-stamp to find.
   * @return Measurement : The Measurement if found(null if not found).
   */
  final public MeasurementCustom find(/*@Min(0)*/ final long seconds) {

    final String mmi="find: ";

    MeasurementCustom ret= null;

    //slog.debug(mmi+": seconds dt: "+
    //           SecondsSinceEpoch.dtFmtString(seconds, true) + ", this.trackIndex=" + this.trackIndex);
    //slog.info(mmi+"start: start: seconds ts="+Instant.ofEpochSecond(seconds).toString());
    //slog.info(mmi+"this.trackIndex="+this.trackIndex);

    if (this.mcDataList != null) {

      final long sseCheck= this.mcDataList.
        get(this.trackIndex).getEventDate().getEpochSecond();

      //slog.debug(mmi+"sseCheck dt: "+SecondsSinceEpoch.dtFmtString(sseCheck,true));
      //slog.info(mmi+"sseCheck ts="+Instant.ofEpochSecond(sseCheck).toString());

      if (sseCheck == seconds) {
        ret= this.mcDataList.get(trackIndex);

      } else {
        ret= (seconds > sseCheck) ? this.findInFuture(seconds) : this.findInPast(seconds);
      }
    }

    slog.debug(mmi+"end, seconds dt: "+
               SecondsSinceEpoch.dtFmtString(seconds, true) + ", this.trackIndex=" + this.trackIndex + ", ret=" + ret);

    //slog.debug(mmi+"Debug exit 0");
    //System.exit(0);

    return ret;
  }

  /**
   * Find a specific Measurement in the future.
   *
   * @param seconds : The time-stamp to find.
   * @return Measurement : The Measurement if found(null if not found).
   */
  final public MeasurementCustom findInFuture(/*@Min(0)*/ final long seconds) {

    //final String mmi= "findInFuture: ";

    MeasurementCustom ret= null;

    //slog.debug(mmi+"start : seconds dt:" + SecondsSinceEpoch.dtFmtString(seconds,true));

    if (this.mcDataList.size() == 1) {

      final MeasurementCustom mc0= this.mcDataList.get(0);

      ret= (mc0.getEventDate().getEpochSecond() == seconds) ? mc0 : null;

    } else {

      ret= (this.arrowOfTime == ArrowOfTime.FORWARD) ? this.findFwdMode(seconds) : this.findBwdMode(seconds);
    }

    //slog.debug(mmi+"end : seconds dt:" + SecondsSinceEpoch.dtFmtString(seconds, true) + ", ret=" + ret);

    return ret;
  }

  /**
   * Find a specific Measurement in the past.
   *
   * @param seconds : The time-stamp to find.
   * @return Measurement : The Measurement if found(null if not found).
   */
  final public MeasurementCustom findInPast(/*@Min(0)*/ final long seconds) {

    final String mmi= "findInPast: ";

    //slog.debug(mmi+" Need to be (re-)tested before using this method ! exit 1 !");
    //System.exit(1);
    //slog.info(mmi+"start: seconds ts="+Instant.ofEpochSecond(seconds).toString());
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    MeasurementCustom ret= null;

    if (this.mcDataList.size() == 1) {

      final MeasurementCustom mc0= this.mcDataList.get(0);

      ret= (mc0.getEventDate().getEpochSecond() == seconds) ? mc0 : null;

    } else {

      ret= (this.arrowOfTime == ArrowOfTime.FORWARD) ? this.findBwdMode(seconds) : this.findFwdMode(seconds);
    }

    slog.debug(mmi+"end: ret="+ret);

    return ret;
  }

  /**
   * Find a specific Measurement in forward index mode(i.e. increasing indices) from the List
   * for a specific time-stamp in seconds since the epoch.
   *
   * @param seconds : The time-stamp to find in the List.
   * @return Measurement : The Measurement if found(null if not found).
   */
  final public MeasurementCustom findFwdMode(/*@Min(0)*/ final long seconds) {

    //final String mmi= "findFwdMode: ";

    MeasurementCustom ret= null;

    //slog.debug(mmi+"start : seconds dt:" +
    //          SecondsSinceEpoch.dtFmtString(seconds,true) + ", this.trackIndex=" + this.trackIndex);

    //--- The check if the Measurement wanted at the time-stamp represented
    //    by the argument seconds is located at trackIndex is already done.
    if (this.trackIndex == (this.mcDataList.size() - 2)) {

      final MeasurementCustom nextMc= this.mcDataList.get(++this.trackIndex);

      ret= (nextMc.getEventDate().getEpochSecond() == seconds) ? nextMc : null;

    } else if (this.trackIndex <= (this.mcDataList.size() - 3)) {

      final int nextMcIndex= this.trackIndex + 1;

      final MeasurementCustom nextMc= this.mcDataList.get(nextMcIndex);

      if (nextMc.getEventDate().getEpochSecond() == seconds) {

        ret= nextMc;

        this.trackIndex= nextMcIndex;

      } else {

        //--- Start the forward search at index nextMcIndex+1
        this.findWLForward(seconds, nextMcIndex + 1, this.mcDataList);

        ret= this.measurement;
      }
    }

    //slog.debug(mmi+"end : seconds dt:" +
    //          SecondsSinceEpoch.dtFmtString(seconds, true) + ", this.trackIndex=" + this.trackIndex + ", ret=" + ret);

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

    final String mmi= "findBwdMode: ";

    MeasurementCustom ret= null;

    //slog.debug(mmi+" Need to be (re-)tested before using this method ! exit 1 !");
    //System.exit(1)
    //slog.info(mmi+"start: seconds ts="+Instant.ofEpochSecond(seconds).toString());
    //slog.info(mmi+"this.trackIndex="+this.trackIndex);
    //slog.info(mmi+"this.mcDataList.get(this.trackIndex),getEventDate()="+this.mcDataList.get(this.trackIndex).getEventDate().toString());
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    //--- The check if the Measurement wanted at the time-stamp represented
    //    by the argument seconds is located at trackIndex is already done.
    if (this.trackIndex == 1) {

      final MeasurementCustom prevMc= this.mcDataList.get(--this.trackIndex);

      ret= (prevMc.getEventDate().getEpochSecond() == seconds) ? prevMc : null;

    } else if (this.trackIndex >= 2) {

      final int prevMcIndex= this.trackIndex - 1;

      final MeasurementCustom prevMc= this.mcDataList.get(prevMcIndex);

      if (prevMc.getEventDate().getEpochSecond() == seconds) {

        ret= prevMc;

        this.trackIndex= prevMcIndex;

      } else {

        //--- Start the backward search at index prevIndex-1
        this.findWLBackward(seconds, prevMcIndex - 1, this.mcDataList);

        ret= this.measurement;
      }
    }

    slog.debug(mmi+"end: ret="+ret);
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    return ret;
  }

  /**
   * @return long : The lastSse at the last index in the wrapped List
   */
  //@Min(0)
  final public long getLastSse() {

    return ((this.mcDataList != null) && (this.mcDataList.size() > 0)) ?
      this.mcDataList.get(this.mcDataList.size() - 1).getEventDate().getEpochSecond() : 0L;
  }

  /**
   * @return long : The absolute value of the time-stamps difference between the first two Measurement items of the
   * wrapped List.
   */
  //@Min(0)
  final public long getSecondsIncrement() {

    return Math.abs(this.mcDataList.get(1).getEventDate().getEpochSecond() -
                    this.mcDataList.get(0).getEventDate().getEpochSecond());
  }

  /**
   * @return int : The size of the wrapped Measurement List
   */
  final public int size() {
    return this.mcDataList.size();
  }
}
