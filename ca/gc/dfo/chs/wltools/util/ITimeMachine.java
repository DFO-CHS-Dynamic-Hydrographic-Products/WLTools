//package ca.gc.dfo.iwls.fmservice.modeling.util;
package ca.gc.dfo.chs.wltools.util;

/**
 *
 */

/**
 * Interface defining time constants.
 */
public interface ITimeMachine {

  String TIMESTAMP_SEP= "."; //"\\T"

  String CALENDAR_ZULU_TIME_ZONE = "UTC";
  /**
   * Calendar class uses milliseconds for its time-stamp.
   */
  long SEC_TO_MILLISEC = 1000L;

  /**
   * Indice of the seconds data in an { YYYY, MM, DD, hh, mm, ss } time-stamp arrays length
   */
  int SECONDS_INDEX = 5;
  int MINUTES_INDEX = 4;
  int HOURS_INDEX = 3;
  int MDAY_INDEX = 2;
  int MONTH_INDEX = 1;
  int YEAR_INDEX = 0;
  //--- self-explanatory names for the following constants.
  int SECONDS_PER_HOUR = 3600;
  int MINUTES_PER_HOUR = 60;
  int SECONDS_PER_MINUTE = 60;
  int HOURS_PER_DAY = 24;
  int DAYS_PER_NORMAL_YEAR = 365;
  int SECONDS_PER_DAY = HOURS_PER_DAY * SECONDS_PER_HOUR;
  int HOURS_PER_NORMAL_YEAR = HOURS_PER_DAY * DAYS_PER_NORMAL_YEAR;
  double HOURS_PER_NORMAL_YEAR_INVD = 1.0 / (double) HOURS_PER_NORMAL_YEAR;
  /**
   * INF_YEAR_LIMIT: Lower limit for tidal predictions
   * TODO: Test if we can go before December 31 1899 at noon for tidal predictions with M. Foreman's method.
   */
  int INF_YEAR_LIMIT = 1900;

  //--- For possible future usage
  //int DAYS_PER_LEAP_YEAR= DAYS_PER_NORMAL_YEAR + 1 ;
  //int SECONDS_PER_NORMAL_YEAR= DAYS_PER_NORMAL_YEAR * SECONDS_PER_DAY ;
  //int HOURS_PER_LEAP_YEAR= HOURS_PER_DAY * DAYS_PER_LEAP_YEAR ;
  //int LEAP_YEAR_FEBRUARY_NUM_DAYS= 29 ;
  /**
   * Recall that GregorianCalendar class consider that JANUARY==0, FEBRUARY==1, MARCH==2,... DECEMBER==11
   * We need to subtract 1 from the normal months representation to feed GregorianCalendar class
   * constructor properly to get what we want.
   */
  int GRGCAL_MONTH_OFFSET = 1;

  //--- Kept for possible future usage
  //int UI_YEAR_1900= 1900 ;
  //int CMC_TS_YEAR_FACTOR= 10000 ;
  //int CMC_TS_MONTH_FACTOR= 100 ;
  //int CMC_TS_HOUR_FACTOR= 1000000 ;
  //int CMC_TS_MIN_FACTOR= 10000 ;
  //int CMC_TS_SEC_FACTOR= 100 ;
  /**
   * DATE_TIME_FMT6_LEN: { YYYY, MM, DD, hh, mm, ss } time-stamp arrays length
   */
  int DATE_TIME_FMT6_LEN = 6;

  /** LAST_GREGORIAN_DAY_OF_19TH_CENTURY :
   *  GREGORIAN DAY OF December 31 1899
   *  USED IN FOREMAN'S SUN MOON
   *  EPHEMERIDES COMPUTATIONS.
   *  NOTE: No more need for that as of 20180322 but nonetheless kept here
   *        for the historical aspect and possible future validation tests
   */
  //int LAST_GREGORIAN_DAY_OF_19TH_CENTURY= 693961;
  //int [ ] GREGORIAN_DAYS_CONSTANTS= { 3, 4, 36524, 100 };
  //double HALF_GREGORIAN_DAY_ADJUSTEMENT= 0.5;
  /**
   * Number of days per month for a normal non-bissextile year.
   */
  int[] NORMAL_NUM_OF_DAYS_PER_MONTH = {
      31,
      28,
      31,
      30,
      31,
      30,
      31,
      31,
      30,
      31,
      30,
      31};
  
  /**
   * Flag which gives the time ordering of a List containing time-stamped data
   */
  enum ArrowOfTime {
    
    /**
     * A List begins with the least recent time-stamped data and ends
     * with the more recent time-stamped data
     */
    FORWARD(1.0), //--- Time is going forward.
    
    /**
     * A List begins with the more recent time-stamped data
     * and ends with the least recent time-stamped data.
     */
    BACKWARD(-1.0); //--- Time is going backward
    
    public final double factor;
    
    ArrowOfTime(final double factor) {
      this.factor = factor;
    }
  }
  
  //--- Kept for possible future usage
//    int PREVIOUS_MONTH_YEAR_DAYS [ ] = {
//            0,
//            31,
//            59,
//            90,
//            120,
//            151,
//            181,
//            212,
//            243,
//            273,
//            304,
//            334,
//            365 } ;
}
