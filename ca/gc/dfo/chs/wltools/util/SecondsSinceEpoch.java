//package ca.gc.dfo.iwls.fmservice.modeling.util;
package ca.gc.dfo.chs.wltools.util;

/**
 *
 */

//---

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

//--- Works with both JDK-1.7 and JDK-1.8

/**
 * class SecondsSinceEpoch: manage time-stamps data. Using final class qualifier to get better performance.
 */
public final class SecondsSinceEpoch extends TimeMachine implements ITimeMachine {
  
  /**
   * log utility
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * Calendar object allowing JDK-1.7 usage(JDK-1.8 still not everywhere for now 2018-03-21)
   */
  private Calendar cld = null;
  
  /**
   * @param utcDateTimeData : { YYYY, MM, DD, hh, mm, ss } UTC Year, Month, Day, Hour, minutes, seconds array.
   */
  //public SecondsSinceEpoch(@NotNull final int[] utcDateTimeData) {
  public SecondsSinceEpoch(final int[] utcDateTimeData) {

    this();
    
    this.cld = getUTCCalendarSinceEpoch(utcDateTimeData);
  }
  
  /**
   * Default constructor.
   */
  public SecondsSinceEpoch() {
    this.cld = null;
  }
  
  //--- Constructor kept for possible future usage:
//    /**
//     * @param sse : Another SecondsSinceEpoch
//     */
//    public SecondsSinceEpoch(@NotNull final SecondsSinceEpoch sse) {
//
//        if (sse==null) {
//            this.log.error("sse==null !");
//            throw new RuntimeException("SecondsSinceEpoch constructor.");
//        }
//
//        if (sse.cld==null) {
//            this.log.error("sse.cld==null !");
//            throw new RuntimeException("SecondsSinceEpoch constructor.");
//        }
//
//        if (this.cld != null) {
//            this.log.warn("this.cld != null: it will be re-created !");
//        }
//
//        this.cld= new GregorianCalendar(sse.cld.get(Calendar.YEAR), sse.cld.get(Calendar.MONTH), sse.cld.get
//        (Calendar.DAY_OF_MONTH),
//                                        sse.cld.get(Calendar.HOUR_OF_DAY), sse.cld.get(Calendar.MINUTE), sse.cld
//                                        .get(Calendar.SECOND));
//
//        this.cld.setTimeZone(TimeZone.getTimeZone(CALENDAR_ZULU_TIME_ZONE));
//    }
  
  /**
   * @param tsSeconds : A time-stamp in seconds since the epoch.
   */
  //public SecondsSinceEpoch(@Min(0) final long tsSeconds) {
  public SecondsSinceEpoch(final long tsSeconds) {

    this.cld = new GregorianCalendar();
    
    this.cld.setTimeZone(TimeZone.getTimeZone(CALENDAR_ZULU_TIME_ZONE));
    
    this.set(tsSeconds);
  }
  
  /**
   * @param tsSeconds : A time-stamp in seconds since the epoch.
   */
  //public final void set(@Min(0) final long tsSeconds) {
  public final void set(final long tsSeconds) {

    //--- WARNING: No check on this.cld here. Could be null.
    //             Could be used in loops so we need performance here:
//        if (this.cld==null) {
//            this.log.error("this.cld == null !");
//            throw new RuntimeException("SecondsSinceEpoch set");
//        }
    
    //--- Unfortunately, Calendar class keeps its time in millisec:
    this.cld.setTimeInMillis(SEC_TO_MILLISEC * tsSeconds);
  }
  
  /**
   * @param tsSeconds : A time-stamp in seconds since the epoch.
   * @return String representing the time-stamp of tsSeconds.
   */
  public static final String dtFmtString(final long tsSeconds) {
    
    return dtFmtString(tsSeconds, false);
  }
  
  /**
   * @param tsSeconds : A time-stamp in seconds since the epoch.
   * @param tz        : boolean to signal that the time-zone info. is wanted in the result.
   * @return String representing the time-stamp of tsSeconds.
   */
  //@NotNull
  //public static final String dtFmtString(@Min(0) final long tsSeconds, final boolean tz) {
  public static final String dtFmtString(final long tsSeconds, final boolean tz) {

    final SecondsSinceEpoch sse = new SecondsSinceEpoch(tsSeconds);
    
    return sse.dateTimeString(tz);
  }
  
  /**
   * @param tz : boolean to signal that the time-zone info. is wanted in the result.
   * @return String representing the time-stamp with OR without time-zone info. of the SecondsSinceEpoch current object.
   */
  //@NotNull
  public final String dateTimeString(final boolean tz) {
    return TimeMachine.dateTimeString(this.cld, tz);
  }
  
  /**
   * @param tsSeconds : A time-stamp in seconds since the epoch.
   * @return String representing the time-stamp of tsSeconds formatted a la ODIN DB: YYYY/MM/DD::hh:mm:ss;   *.***;
   */
  //public static final String odinDtFmtString(@Min(0) final long tsSeconds) {
  public static final String odinDtFmtString(final long tsSeconds) {

    return odinDtFmtString(new SecondsSinceEpoch(tsSeconds));
  }
  
  /**
   * @param sse : A SecondsSinceEpoch object
   * @return String representing the time-stamp of tsSeconds formatted a la ODIN DB: YYYY/MM/DD::hh:mm:ss;   *.***;
   */
  //public static final String odinDtFmtString(@NotNull final SecondsSinceEpoch sse) {
  public static final String odinDtFmtString(final SecondsSinceEpoch sse) {

    final String odinDtFmtString =
        sse.cld.get(Calendar.YEAR) + "/" +
            prependZero(Integer.toString(sse.cld.get(Calendar.MONTH) + GRGCAL_MONTH_OFFSET)) + "/" +
            prependZero(Integer.toString(sse.cld.get(Calendar.DAY_OF_MONTH))) + "::" +
            prependZero(Integer.toString(sse.cld.get(Calendar.HOUR_OF_DAY))) + ":" +
            prependZero(Integer.toString(sse.cld.get(Calendar.MINUTE))) + ":" +
            prependZero(Integer.toString(sse.cld.get(Calendar.SECOND)));
    
    return odinDtFmtString;
  }
  
  /**
   * @param item: The integer code (YEAR, MONTH, DAY_OF_MONTH, HOUR, MINUTE, SECOND) of the Calendar item wanted
   * @return The integer value of the  Calendar item wanted.
   */
  public final int getCalendarItem(final int item) {
    
    //--- WARNING: No check for the Calendar integer code validity
    //             because this method could be used in loops and
    //             we need performance.
    
    return this.cld.get(item);
  }
  
  /**
   * @return String representing the time-stamp without time-zone info. of the SecondsSinceEpoch current object.
   */
  //@NotNull
  public final String dateTimeString() {
    return TimeMachine.dateTimeString(this.cld, false);
  }
  
  /**
   * @param sseIncr : A time increment in seconds.
   * @return the current SecondsSinceEpoch object.
   */
  //@NotNull
  //public final SecondsSinceEpoch incr(@Min(0) final long sseIncr) {
  public final SecondsSinceEpoch incr(final long sseIncr) {

    //--- WARNING: No check on this.cld here. Could be null.
//        if (this.cld==null) {
//            this.log.error("this.cld == null !");
//            throw new RuntimeException("SecondsSinceEpoch incr");
//        }
    
    //System.out.println("bef: this.cld.getTimeInMillis()="+ Long.toString(this.cld.getTimeInMillis()));
    
    //--- Increment the time-stamp(in millisec) of this.cld by sseIncr:
    this.cld.setTimeInMillis(this.incrLong(sseIncr));
    
    //System.out.println("aft: this.cld.getTimeInMillis()="+ Long.toString(this.cld.getTimeInMillis()));
    
    return this;
  }
  
  /**
   * @param timeIncrSeconds : A time increment in seconds.
   * @return The time-stamp in millisec of this.cld incremented by SEC_TO_MILLISEC*timeIncrSeconds
   */
  public final long incrLong(final long timeIncrSeconds) {
    
    //--- WARNING: No check on this.cld here. Could be null.
    return this.cld.getTimeInMillis() + SEC_TO_MILLISEC * timeIncrSeconds;
  }
  
  /**
   * @return A long representing the time-stamp since the epoch of the Calendar object attribute.
   */
  public final long seconds() {
    
    //--- WARNING: No check on this.cld here. Could be null.
    //             Could be used in loops so we need performance here:
//        if (this.cld==null) {
//            this.log.error("this.cld == null !");
//            throw new RuntimeException("SecondsSinceEpoch seconds()");
//        }
    
    //--- Need to return seconds here not milliseconds:
    return this.cld.getTimeInMillis() / SEC_TO_MILLISEC;
  }
}
