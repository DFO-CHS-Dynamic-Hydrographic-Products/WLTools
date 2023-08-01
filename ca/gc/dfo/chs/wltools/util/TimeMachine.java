//package ca.gc.dfo.iwls.fmservice.modeling.util;
package ca.gc.dfo.chs.wltools.util;

/**
 *
 */

//--- Works with both JDK-1.7 and JDK-1.8

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

//import javax.validation.constraints.Min;
//---
//import ca.gc.dfo.chs.wltools.util.ITimeMachine;

/**
 * abstract class TimeMachine to manage time related data.
 */
abstract public class TimeMachine implements ITimeMachine {
  
  /**
   * static log utility.
   */
  private static final Logger staticLogger = LoggerFactory.getLogger("TimeMachine");
  
  /**
   * @param cld : A Calendar object
   * @param tz  : boolean flag to signal that the time-zone info should be appended to the returned String.
   * @return String representing the Calendar time-stamp
   */
   public static String dateTimeString(/*@NotNull*/ final Calendar cld, final boolean tz) {
  //public static String dateTimeString(final Calendar cld, final boolean tz) {

    //--- Need to increment cld.get(Calendar.MONTH) by GRGCAL_MONTH_OFFSET== 1 to
    //    get the normal 1->12 Month convention.
    final String dateTimeStr= Integer.toString(cld.get(Calendar.YEAR)) +
        prependZero(Integer.toString(cld.get(Calendar.MONTH) + GRGCAL_MONTH_OFFSET)) +
        prependZero(Integer.toString(cld.get(Calendar.DAY_OF_MONTH))) + // + "." +
        TIMESTAMP_SEP +
        prependZero(Integer.toString(cld.get(Calendar.HOUR_OF_DAY))) +
        prependZero(Integer.toString(cld.get(Calendar.MINUTE))) +
        prependZero(Integer.toString(cld.get(Calendar.SECOND)));

    //--- Append time zone ID to the returned String.
    return dateTimeStr + (tz ? ":(TZ:" + cld.getTimeZone().getID() + ")" : "");
  }
  
  /**
   * @param integerString : String version of an integer.
   * @return "0" String prepended to integerString if needed(for date-time formatting stuff)
   */
  //public static String prependZero(@NotNull final String integerString) {
  public static String prependZero(final String integerString) {

    return (integerString.length() < 2 ? "0" + integerString : integerString);
  }
  
  /**
   * @param dateTimeData : { YYYY, MM, DD, hh, mm, ss } Year, Month, Day, Hour, minutes, seconds array.
   * @return long representing the UTC time-stamp in seconds since the epoch.
   */
  //public static long getUTCLongSinceEpoch(@NotNull final int[] dateTimeData) {
  public static long getUTCLongSinceEpoch(final int[] dateTimeData) {

    //--- JDK-1.7 and JDK-1.8
    
    //---- We are only able to get milliseconds with Calendar interface so we have to
    //     divide dtsSinceEpoch.getTimeInMillis() by SEC_TO_MILLISEC to get seconds from 1970-01-01::00:00:00 UTC
    return getUTCCalendarSinceEpoch(dateTimeData).getTimeInMillis() / SEC_TO_MILLISEC;
  }
  
  /**
   * @param dateTimeData : { YYYY, MM, DD, hh, mm, ss } Year, Month, Day, Hour, minutes, seconds array.
   * @return A Calendar object with its time-stamp set according to the date-time data.
   */
  //public static Calendar getUTCCalendarSinceEpoch(@NotNull @Size(min = 6) final int[] dateTimeData) {
  public static Calendar getUTCCalendarSinceEpoch(final int[] dateTimeData) {

    //--- JDK-1.7 and JDK-1.8
    checkDateTimeData(dateTimeData);
    
    //--- ***IMPORTANT NOTE***: The GregorianCalendar constructor needs a month between 0 and 11
    //                          hence the dateTimeData[MONTH_INDEX]-GRGCAL_MONTH_OFFSET subtraction:
    final Calendar grgCld = new GregorianCalendar(dateTimeData[YEAR_INDEX],
        dateTimeData[MONTH_INDEX] - GRGCAL_MONTH_OFFSET,
        dateTimeData[MDAY_INDEX],
        dateTimeData[HOURS_INDEX],
        dateTimeData[MINUTES_INDEX],
        dateTimeData[SECONDS_INDEX]);
    
    //--- UTC time zone mandatory here !!
    grgCld.setTimeZone(TimeZone.getTimeZone(CALENDAR_ZULU_TIME_ZONE));
    
    //---- We are only able to get time in milliseconds with Calendar interface so we have to
    //     divide dtsSinceEpoch.getTimeInMillis() by 1000 to get seconds from 1970-01-01::00:00:00 UTC
    return grgCld;
  }
  
  /**
   * @param dateTimeData : { YYYY, MM, DD, hh, mm, ss } Year, Month, Day, Hour, minutes, seconds array.
   * @return a boolean to signal that the dateTime data is OK
   */
  //private static boolean checkDateTimeData(@NotNull @Size(min = 6) final int[] dateTimeData) {
  private static boolean checkDateTimeData(final int[] dateTimeData) {
    
    if (dateTimeData.length != DATE_TIME_FMT6_LEN) {
      
      staticLogger.error("TimeMachine checkDateTimeData:  : dateTimeData must have " + DATE_TIME_FMT6_LEN + " " +
          "elements !");
      throw new RuntimeException("TimeMachine checkDateTimeData");
    }
    
    //---- We have to check if we can go before January 1 INF_YEAR_LIMIT(which is 1900 for now)
    //     at 00:00:00 UTC. M. Foreman's method uses time in gregorian days seconds since December
    //     31 1899 at 12:00 UTC.
    //
    //     TODO: It would be interesting to test if M. Foreman's method could be applied for time stamps before
    //      December 31 1899 12:00:00 UTC.
    if (dateTimeData[YEAR_INDEX] < INF_YEAR_LIMIT) {
      
      staticLogger.error("TimeMachine checkDateTimeData: Invalid YEAR -> " + dateTimeData[YEAR_INDEX]);
      throw new RuntimeException("TimeMachine checkDateTimeData");
    }
    
    if (dateTimeData[MONTH_INDEX] < 1 || dateTimeData[MONTH_INDEX] > 12) {
      
      staticLogger.error("TimeMachine checkDateTimeData: Invalid MONTH -> " + dateTimeData[MONTH_INDEX]);
      throw new RuntimeException("TimeMachine checkDateTimeData");
    }
    
    int monthDays = NORMAL_NUM_OF_DAYS_PER_MONTH[dateTimeData[MONTH_INDEX]];
    
    //--- Dealing with leap years:
    //    Recall that Calendar.JANUARY == 0
    //                Calendar.FEBRUARY== 1
    //                Calendar.MARCH   == 2
    //                ...
    //                Calendar.DECEMBER== 11
    //
    if ((dateTimeData[MONTH_INDEX] == Calendar.FEBRUARY + GRGCAL_MONTH_OFFSET) && isThisALeapYear(dateTimeData[YEAR_INDEX])) {
      monthDays = 29;
    }
    
    if ((dateTimeData[MDAY_INDEX] < 1) || (dateTimeData[MDAY_INDEX] > monthDays)) {
      
      staticLogger.error("TimeMachine checkDateTimeData: Invalid DAY -> " + dateTimeData[MDAY_INDEX]);
      throw new RuntimeException("TimeMachine checkDateTimeData");
    }
    
    if ((dateTimeData[HOURS_INDEX] < 0) || (dateTimeData[HOURS_INDEX] > HOURS_PER_DAY - 1)) {
      
      staticLogger.error("TimeMachine checkDateTimeData: Invalid HOUR -> " + dateTimeData[HOURS_INDEX]);
      throw new RuntimeException("TimeMachine checkDateTimeData");
    }
    
    if ((dateTimeData[MINUTES_INDEX] < 0) || (dateTimeData[MINUTES_INDEX] > MINUTES_PER_HOUR - 1)) {
      
      staticLogger.error("TimeMachine checkDateTimeData: Invalid MINUTES -> " + dateTimeData[MINUTES_INDEX]);
      throw new RuntimeException("TimeMachine checkDateTimeData");
    }
    
    if ((dateTimeData[SECONDS_INDEX] < 0) || (dateTimeData[SECONDS_INDEX] > SECONDS_PER_MINUTE - 1)) {
      
      staticLogger.error("TimeMachine checkDateTimeData: Invalid SECONDS -> " + dateTimeData[SECONDS_INDEX]);
      throw new RuntimeException("TimeMachine checkDateTimeData");
    }
    
    return true;
  }
  
  /**
   * @param year : YYYY four digit year.
   * @return true if year is a leap year false otherwise.
   */
  public static boolean isThisALeapYear(final long year) {
    
    //---- Is year argument a leap year ?
    //     Taken from the book "THE ART AND SCIENCE OF C" written
    //     by E. S. Roberts, ADDISON WESLEY PUBLISHING COMPANY
    
    return (((year % 4L == 0L) && (year % 100L != 0L)) || (year % 400L == 0L));
  }
  
  /**
   * @param timeIncrSeconds : A time increment in seconds
   * @param timeSeconds     : A time-stamp in seconds since the epoch.
   * @return The time-stamp in seconds since the epoch which is the nearest in the past from timeSeconds and is also
   * divisible by timeIncrSeconds
   */
  public static long roundPastToTimeIncrSeconds(final long timeIncrSeconds, final long timeSeconds) {
    
    long ret = timeSeconds;
    
    //--- NOTE: recursive call here:
    
    if (!(ret % timeIncrSeconds == 0L)) {
      
      //---
      ret = roundPastToTimeIncrSeconds(timeIncrSeconds, (timeSeconds - 1L));
    }
    
    return ret;
  }

//--- Method kept for possible future usage.
//    /**
//     * @param cld : A Calendar object
//     * @return long representing the time-stamp contained by the Calendar object cls.
//     */
//    public static final long sse(@NotNull final Calendar cld) {
//
//        return cld.getTimeInMillis()/SEC_TO_MILLISEC;
//    }

}
