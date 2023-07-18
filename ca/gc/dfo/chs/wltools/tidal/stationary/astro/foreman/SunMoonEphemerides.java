package ca.gc.dfo.iwls.fmservice.modeling.tides.astro.foreman;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

//---

import ca.gc.dfo.iwls.fmservice.modeling.util.ITimeMachine;
import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Calendar;

//import javax.validation.constraints.Size;
//import javax.validation.constraints.NotNull;
//---
//---

/**
 * Specific class for Sun and Moon ephemerides computations. It is a Java port of M. Foreman's
 * Fortran subroutines astro and (some parts of)vuf.
 */
final public class SunMoonEphemerides implements IForemanConstituentAstro, ITimeMachine {
  
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * Flag to signal that the object is ready to use(i.e. new ephemerides result produced)
   */
  protected boolean smeSet = false;
  /**
   * Mean longitude of the sun.
   */
  protected double sunH = 0.0;
  /**
   * 1st time derivative of sunH
   */
  protected double sunDH = 0.0;
  /**
   * Mean longitude of the solar perigee
   */
  protected double sunPP = 0.0;
  /**
   * 1st time derivative of sunP
   */
  protected double sunDPP = 0.0;
  /**
   * Mean longitude of the moon
   */
  protected double moonS = 0.0;
  /**
   * 1st time derivative of moonS
   */
  protected double moonDS = 0.0;
  /**
   * Mean longitude of the lunar perigee
   */
  protected double moonP = 0.0;
  /**
   * 1st time derivative of moonP
   */
  protected double moonDP = 0.0;
  /**
   * Negative of the longitude of the mean ascending node.
   */
  protected double meanAscModeNP = 0.0;
  /**
   * 1st time derivative of meanAscModeNP.
   */
  protected double meanAscModeDNP = 0.0;
  /**
   * Lunar time.
   */
  protected double tau = 0.0;
  /**
   * lunar time rate of change(?).
   */
  protected double dTau = 0.0;
  
  /**
   * Default constructor
   */
  public SunMoonEphemerides() {
    this.clear();
  }
  
  /**
   * Clear object data at 0.0
   */
  protected final void clear() {
    
    this.smeSet = false;
    
    this.sunH =
        this.sunDH =
            this.sunPP =
                this.sunDPP =
                    this.moonS =
                        this.moonDS =
                            this.moonP =
                                this.moonDP =
                                    this.meanAscModeNP =
                                        this.meanAscModeDNP = 0.0;
    
    this.tau =
        this.dTau = 0.0;
  }
  
  /**
   * @param sse : A SecondsSinceEpoch which time-stamp will be used as the time-reference for the new
   *            SunMoonEphemerides object.
   */
  public SunMoonEphemerides(@NotNull final SecondsSinceEpoch sse) {
    this(sse.getCalendarItem(Calendar.HOUR_OF_DAY), ForemanAstroInfosFactory.getAstroD1(sse.seconds()));
  }
  
  /**
   * @param dayHour : Hour of the day of the time-stamp used as the time-reference for the new SunMoonEphemerides
   *                object.
   * @param astroD1 : Number of days in double precision since December 31 1899 at noon(see method
   *                ForemanAstroInfosFactory.getAstroD1 for details).
   */
  public SunMoonEphemerides(@Min(0) final int dayHour, final double astroD1) {
    
    this.set(dayHour, astroD1);
  }
  
  /**
   * @param dayHour : Hour of the day of the time-stamp used as the time-reference for the new SunMoonEphemerides
   *                object.
   * @param astroD1 : Number of days in double precision since December 31 1899 at noon(see method
   *                ForemanAstroInfosFactory.getAstroD1 for details).
   * @return The current SunMoonEphemerides ready to use.
   */
  protected final SunMoonEphemerides set(@Min(0) final int dayHour, final double astroD1) {
    
    this.clear();
    
    //this.log.debug("Start: dayHour="+dayHour+", astroD1="+astroD1);
    
    //--- Local constants.
    final double ASTRO_D1_SQUARE = astroD1 * astroD1;
    final double ASTRO_D2 = astroD1 * ASTRO_D1_FACTOR;
    final double ASTRO_D2_SQUARE = ASTRO_D2 * ASTRO_D2;
    final double ASTRO_D2_CUBE = ASTRO_D2_SQUARE * ASTRO_D2;
    
    //--- TODO: Replace ASTRO_F denominator usage by ASTRO_F_INV as a factor to minimize the number of divisions done
    // in computations loops.
    
    //--- ASTRO_F2 now defined as a static constant in IForemanConstituent interface.
    //    TODO:  Replace ASTRO_F2 denominator usage by ASTRO_F2_INV as a factor to minimize the number of divisions
    //     done in computations loops.
    //final double ASTRO_F2= ASTRO_F/(double)DAYS_PER_NORMAL_YEAR;
    
    //--- Initializations.
    this.sunH = (SUN_EPH_POLY_COEFF_H[0] +
        SUN_EPH_POLY_COEFF_H[1] * astroD1 +
        SUN_EPH_POLY_COEFF_H[2] * ASTRO_D2_SQUARE) / ASTRO_F;
    
    this.sunH -= Math.floor(this.sunH);
    
    this.sunPP = (SUN_EPH_POLY_COEFF_PP[0] +
        SUN_EPH_POLY_COEFF_PP[1] * astroD1 +
        SUN_EPH_POLY_COEFF_PP[2] * ASTRO_D2_SQUARE +
        SUN_EPH_POLY_COEFF_PP[3] * ASTRO_D2_CUBE) / ASTRO_F;
    
    this.sunPP -= Math.floor(this.sunPP);
    
    this.sunDH = (SUN_EPH_POLY_COEFF_DH[0] +
        SUN_EPH_POLY_COEFF_DH[1] * astroD1) / ASTRO_F2;
    
    this.sunDPP = (SUN_EPH_POLY_COEFF_DPP[0] +
        SUN_EPH_POLY_COEFF_DPP[1] * astroD1 +
        SUN_EPH_POLY_COEFF_DPP[2] * ASTRO_D1_SQUARE) / ASTRO_F2;
    
    this.moonS = (MOON_EPH_POLY_COEFF_S[0] +
        MOON_EPH_POLY_COEFF_S[1] * astroD1 +
        MOON_EPH_POLY_COEFF_S[2] * ASTRO_D2_SQUARE +
        MOON_EPH_POLY_COEFF_S[3] * ASTRO_D2_CUBE) / ASTRO_F;
    
    this.moonS -= Math.floor(this.moonS);
    
    this.moonP = (MOON_EPH_POLY_COEFF_P[0] +
        MOON_EPH_POLY_COEFF_P[1] * astroD1 +
        MOON_EPH_POLY_COEFF_P[2] * ASTRO_D2_SQUARE +
        MOON_EPH_POLY_COEFF_P[3] * ASTRO_D2_CUBE) / ASTRO_F;
    
    this.moonP -= Math.floor(this.moonP);
    
    this.moonDS = (MOON_EPH_POLY_COEFF_DS[0] +
        MOON_EPH_POLY_COEFF_DS[1] * astroD1 +
        MOON_EPH_POLY_COEFF_DS[2] * ASTRO_D1_SQUARE) / ASTRO_F2;
    
    this.moonDP = (MOON_EPH_POLY_COEFF_DP[0] +
        MOON_EPH_POLY_COEFF_DP[1] * astroD1 +
        MOON_EPH_POLY_COEFF_DP[2] * ASTRO_D1_SQUARE) / ASTRO_F2;
    
    this.meanAscModeNP = (MEAN_ASCMODE_EPH_POLY_COEFF_NP[0] +
        MEAN_ASCMODE_EPH_POLY_COEFF_NP[1] * astroD1 +
        MEAN_ASCMODE_EPH_POLY_COEFF_NP[2] * ASTRO_D2_SQUARE +
        MEAN_ASCMODE_EPH_POLY_COEFF_NP[3] * ASTRO_D2_CUBE) / ASTRO_F;
    
    this.meanAscModeNP -= Math.floor(this.meanAscModeNP);
    
    this.meanAscModeDNP = (MEAN_ASCMODE_EPH_POLY_COEFF_DNP[0] +
        MEAN_ASCMODE_EPH_POLY_COEFF_DNP[1] * astroD1 +
        MEAN_ASCMODE_EPH_POLY_COEFF_DNP[2] * ASTRO_D1_SQUARE) / ASTRO_F2;
    
    //--- Only the fractional part of a solar day need be retained for computing the lunar time TAU.
    this.tau = ((double) dayHour / HOURS_PER_DAY) + (this.sunH - this.moonS);
    
    this.dTau = (DAYS_PER_NORMAL_YEAR + (this.sunDH - this.moonDS));
    
    this.smeSet = true;

//         System.out.println("\nSun Moon ephemerides:") ;
//         System.out.println("this.SunH="+this.sunH);
//         System.out.println("this.SunDH="+this.sunDH);
//         System.out.println("this.SunPP="+this.sunPP);
//         System.out.println("this.SunDPP="+this.sunDPP);
//         System.out.println("this.MoonS="+this.moonS);
//         System.out.println("this.MoonDS="+this.moonDS);
//         System.out.println("this.MoonP="+this.moonP);
//         System.out.println("this.MoonDP="+this.moonDP);
//         System.out.println("this.MeanAscModeNP="+this.meanAscModeNP);
//         System.out.println("this.MeanAscModeDNP="+this.meanAscModeDNP);
//         System.out.println("Sun Moon ephemerides end\n") ;
    
    //this.log.debug("end");
    
    return this;
  }

//    public final double dTau() {
//
//        //this.log.debug("dTau 1: "+ ((double)DAYS_PER_NORMAL_YEAR + ( this.sunDH - this.moonDS )));
//        //this.log.debug("dTau 2: "+ (DAYS_PER_NORMAL_YEAR + ( this.sunDH - this.moonDS )));
//
//        return (DAYS_PER_NORMAL_YEAR + (this.sunDH - this.moonDS)) ;
//        //return ((double)DAYS_PER_NORMAL_YEAR + (this.sunDH - this.moonDS)) ;
//    }
//
//    public final static long getGregorianDay(final int year, @Min(1) final int month, @Min(1) final int day ) {
//
//        //---  Method computing the Gregorian day of the
//        //     Calendar trio Year,Month,Day passed as arguments.
//        //
//        //     Please note that it is computed since January 1 of the year 0 so it will
//        //     not give the same result as the Python method datetime.date.toordinal()
//        //     used with the same arguments. In fact, it gives the result of
//        //     datetime.date.toordinal() plus 366 which is the number of days of
//        //     bissextile year 0.
//        //
//        //     This is a Java version of the Fortran subroutine gday from M. Foreman's
//        //     package.
//        //
//        //     The following are the comments at the beginning of the gday Fortran subsroutine:
//        //     ---------------------------------------------------------------------------------
//        //!c!
//        //!c!  given day,month,year and century(each 2 digits), gday returns
//        //!c!  the day#, kd based on the gregorian calendar.
//        //!c!  the gregorian calendar, currently 'universally' in use was
//        //!c!  initiated in europe in the sixteenth century. note that gday
//        //!c!  is valid only for gregorian calendar dates.
//        //!c!
//        //!!   kd=1 corresponds to january 1, 0000
//        //!
//        //!       note that the gregorian reform of the julian calendar
//        //!       omitted 10 days in 1582 in order to restore the date
//        //!       of the vernal equinox to march 21 (the day after
//        //!       oct 4, 1582 became oct 15, 1582), and revised the leap
//        //!       year rule so that centurial years not divisible by 400
//        //!       were not leap years.
//        //!
//
//        //--- Validation of date-time arguments must
//        //    have been done elsewhere before using
//        //    this method.
//
//        long century= 0L;
//        long twoDigitYear= 0L;
//
//        long gDay= 0L;
//
//        century= (year/GREGORIAN_DAYS_CONSTANTS[3]) ;
//
//        twoDigitYear= year - (century * GREGORIAN_DAYS_CONSTANTS[3]) ;
//
//        //--- COMPUTE GREGORIAN DAY OF THE LAST DAY OF THE PREVIOUS CENTURY:
//        gDay= century * GREGORIAN_DAYS_CONSTANTS[2] + (century + GREGORIAN_DAYS_CONSTANTS[0])
//        /GREGORIAN_DAYS_CONSTANTS[1] ;
//
//        //--- ADD NUMBER OF DAYS SINCE FIRST DAY of PRESENT CENTURY UNTIL LAST DAY OF LAST YEAR.
//        gDay += twoDigitYear * DAYS_PER_NORMAL_YEAR + (twoDigitYear + GREGORIAN_DAYS_CONSTANTS[0])
//        /GREGORIAN_DAYS_CONSTANTS[1] ;
//
//        //--- SUBTRACT A DAY IF THE PRESENT CENTURY IS NOT A LEAP YEAR ( LIKE YEAR 1900 ).
//        // ( The "? 1:0" at the end of the assignation statement is a boolean to int conversion.)
//        gDay -= ( !TimeMachine.isThisALeapYear(century * GREGORIAN_DAYS_CONSTANTS[3]) ) ? 1L:0L ;
//
//        //--- ADD NUMBER OF DAYS SINCE THE BEGININING OF THE PRESENT
//        //      YEAR UNTIL LAST DAY OF LAST MONTH
//        gDay += PREVIOUS_MONTH_YEAR_DAYS[month - 1];
//
//        //--- ADD ONE DAY IF PRESENT YEAR IS A LEAP YEAR AND month > 2
//        if (month > 2) {
//            gDay += ( TimeMachine.isThisALeapYear ( year ) ) ? 1:0 ;
//        }
//
//        return gDay + day;
//    }
//
//    public final double tau(@Min(0) int dayHour ) {
//
////        this.log.debug("dayHour="+dayHour+", tau t1: ( ( double ) dayHour / ( double ) HOURS_PER_DAY ) + ( this
// .sunH - this.moonS )="
////                +( ( double ) dayHour / ( double ) HOURS_PER_DAY ) + ( this.sunH - this.moonS ));
////
////        this.log.debug("dayHour="+dayHour+", tau t2: ( ( double ) dayHour / HOURS_PER_DAY ) + ( this.sunH - this
// .moonS )="
////                +( ( double ) dayHour / HOURS_PER_DAY ) + ( this.sunH - this.moonS ));
//
//        return ((double)dayHour/HOURS_PER_DAY) + (this.sunH - this.moonS) ;
//
//        //---
//        //return ( (double)dayHour / ( double )HOURS_PER_DAY ) + ( this.sunH - this.moonS ) ;
//    }
}
