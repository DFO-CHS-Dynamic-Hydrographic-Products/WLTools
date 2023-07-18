package ca.gc.dfo.iwls.fmservice.modeling.tides.astro.foreman;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

//---

import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.AstroInfosFactory;
import ca.gc.dfo.iwls.fmservice.modeling.util.ITimeMachine;
import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.Calendar;

//import javax.validation.constraints.Min;
//---
//---
//mport ca.gc.dfo.iwls.fmservice.modeling.util.TimeMachine;

/**
 * Generic class for Foreman's method astronomic informations.
 */
abstract public class ForemanAstroInfosFactory extends AstroInfosFactory implements IForemanConstituentAstro,
    ITimeMachine {
  
  /**
   * static log utility.
   */
  private final static Logger staticLog = LoggerFactory.getLogger("ForemanAstro");
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * Sun and Moon ephemerides computation and storage object.
   */
  protected SunMoonEphemerides sunMoonEphemerides = null;
  
  /**
   * Default constructor.
   */
  public ForemanAstroInfosFactory() {
    
    super();
    
    this.log.debug("ForemanAstroInfosFactory constructor: Start !");
    
    if (this.sunMoonEphemerides != null) {
      this.sunMoonEphemerides.clear();
    }
    
    this.log.debug("ForemanAstroInfosFactory constructor: Done !");
  }
  
  /**
   * @param latitudeRad      : Latitude in radians of a location(WL station OR a grid point).
   * @param startTimeSeconds : Date time-stamp in seconds since the epoch to use as a temporal reference for Sun
   *                         and Moon ephemerides computations.
   */
  public ForemanAstroInfosFactory(final double latitudeRad, final long startTimeSeconds) {
    
    super(Method.FOREMAN, latitudeRad, new SecondsSinceEpoch(startTimeSeconds));
    
    this.log.debug("ForemanAstroInfosFFactory constructor : Start with latitudeRad=" + latitudeRad + ", " +
        "startTimeSeconds dt=" + SecondsSinceEpoch.dtFmtString(startTimeSeconds, true));
    
    if (this.sunMoonEphemerides != null) {
      this.sunMoonEphemerides.clear();
    }
    
    this.sunMoonEphemerides = new SunMoonEphemerides(this.sse);
    
    this.log.debug("ForemanAstroInfosFFactory constructor : end");
  }
  
  /**
   * @param latitudeRad           : Latitude in radians of a location(WL station OR a grid point).
   * @param yearMonthDayHourArray : A { YYYY, MM, DD, hh, mm, ss } UTC Year, Month, Day, Hour, minutes, seconds array.
   */
  public ForemanAstroInfosFactory(final double latitudeRad,
                                  @NotNull @Size(min = DATE_TIME_FMT_LEN) final int[] yearMonthDayHourArray) {
    
    super(Method.FOREMAN, latitudeRad, SecondsSinceEpoch.getUTCLongSinceEpoch(yearMonthDayHourArray));
    
    this.log.debug("ForemanAstroFactory constructor : Start with argument YearMonthDayHourArray -> " + Arrays.toString(yearMonthDayHourArray));
    
    if (this.sunMoonEphemerides != null) {
      this.sunMoonEphemerides.clear();
    }
    
    this.sunMoonEphemerides = new SunMoonEphemerides(this.sse);
    
    //this.sunMoonEphemerides= new SunMoonEphemerides ( yearMonthDayHourArr[HOURS_INDEX], getAstroD1(
    // yearMonthDayHourArr ) );
    
    this.log.debug("ForemanAstroFactory constructor : Done with argument YearMonthDayHourArr -> " + Arrays.toString(yearMonthDayHourArray));
  }
  
  /**
   * @param latPosRadians                 : Latitude in radians of a location(WL station OR a grid point).
   * @param sunMoonEphemerides            A SunMoonEphemerides object used(but not modified) in the computations.
   * @param mainConstituentSatelliteArray : An array of MainConstituentSatellite data objects.
   * @param cosSinAcc                     : Array of cos-sin accumulators.
   * @return The array of cos-sin accumulators.
   */
  @NotNull
  protected final static double[] getMainConstCosSinAcc(final double latPosRadians,
                                                        @NotNull final SunMoonEphemerides sunMoonEphemerides,
                                                        @NotNull @Size(min = 1) final MainConstituentSatellite[] mainConstituentSatelliteArray, @NotNull @Size(min = SIN_INDEX + 1) final double[] cosSinAcc) {
    
    final double sinLatRad = Math.sin(latPosRadians);
    final double adj1Fact = (1.0 - 5.0 * sinLatRad * sinLatRad) / sinLatRad;
    
    //--- Alway (re-)initialize cosSinAcc contents before (re-)using it.
    cosSinAcc[COS_INDEX] = COS_ACC_INIT;
    cosSinAcc[SIN_INDEX] = SIN_ACC_INIT;
    
    for (final MainConstituentSatellite mainConstituentSatellite : mainConstituentSatelliteArray) {
      
      //--- NOTE: No checks here for a potentially null mainConstituentSatellite object, we need performance here.
      
      double cosSinArg = mainConstituentSatellite.phaseCorrection +
          mainConstituentSatellite.doodsonNumChanges[0] * sunMoonEphemerides.moonP +
          mainConstituentSatellite.doodsonNumChanges[1] * sunMoonEphemerides.meanAscModeNP +
          mainConstituentSatellite.doodsonNumChanges[2] * sunMoonEphemerides.sunPP;
      
      //--- Need only the decimal part of cosSinArg:
      cosSinArg = TWO_PI * (cosSinArg - ((int) cosSinArg));
      
      double cosSinFact;
      
      final double satAmplitudeRatio = mainConstituentSatellite.amplitudeRatio;
      
      switch (mainConstituentSatellite.amplitudeRatioFlag) {
        
        case FV_NODAL_ADJ_FLAG_1:
          
          cosSinFact = FV_NODAL_ADJ_CONSTANT_1 * satAmplitudeRatio * adj1Fact;
          break;
        
        case FV_NODAL_ADJ_FLAG_2:
          
          cosSinFact = FV_NODAL_ADJ_CONSTANT_2 * satAmplitudeRatio * sinLatRad;
          break;
        
        default:
          
          cosSinFact = satAmplitudeRatio;
          break;
      }
      
      //--- M. Foreman's Fortran source code :
      //
      //       sumc=sumc+rr*cos(uu*twopi)
      //       sums=sums+rr*sin(uu*twopi)
      //
      cosSinAcc[COS_INDEX] += cosSinFact * Math.cos(cosSinArg);
      cosSinAcc[SIN_INDEX] += cosSinFact * Math.sin(cosSinArg);
    }
    
    return cosSinAcc;
  }
  
  /**
   * @param sunMoonEphemerides : A SunMoonEphemerides object used(but not modified) in the computations.
   * @param doodsonNumsArray   : An array of Doodson numbers used(but not modified) in the computations.
   * @return The updated frequency of a tidal constituent.
   */
  protected final static double getMainConstTidalFrequency(@NotNull final SunMoonEphemerides sunMoonEphemerides,
                                                           @NotNull @Size(min = MC_DOODSON_NUM_LEN) final int[] doodsonNumsArray) {
    
    //--- NOTE: No checks here for a potentially null sunMoonEphemerides and-or doodsonNumsArray objects, we need
    // performance here.
    
    //--- TODO: Use a multiplication by HOURS_PER_NORMAL_YEAR_INVD instead of a division by HOURS_PER_NORMAL_YEAR for
    // better performance.
    
    return ((doodsonNumsArray[0] * sunMoonEphemerides.dTau +
        doodsonNumsArray[1] * sunMoonEphemerides.moonDS +
        doodsonNumsArray[2] * sunMoonEphemerides.sunDH +
        doodsonNumsArray[3] * sunMoonEphemerides.moonDP +
        doodsonNumsArray[4] * sunMoonEphemerides.meanAscModeDNP +
        doodsonNumsArray[5] * sunMoonEphemerides.sunDPP) / (double) HOURS_PER_NORMAL_YEAR); // *
    // HOURS_PER_NORMAL_YEAR_INVD;
  }
  
  /**
   * @param phaseCorrection    : Constituent phase correction.
   * @param sunMoonEphemerides : A SunMoonEphemerides object used(but not modified) in the computations.
   * @param doodsonNumsArray   : An array of Doodson numbers used(but not modified) in the computations.
   * @return The astronomic argument needed.
   */
  protected final static double getMainConstAstroArgument(final double phaseCorrection,
                                                          @NotNull final SunMoonEphemerides sunMoonEphemerides,
                                                          @NotNull @Size(min = MC_DOODSON_NUM_LEN) final int[] doodsonNumsArray) {
    
    //--- M. Foreman's Fortran source code for the V astronomical argument:
    //
    //      dbl=ii(k)*tau+jj(k)*s+kk(k)*h+ll(k)*p+mm(k)*enp+nn(k)*pp+semi(k)
    //
    //     !WITH s,h,p,enp,pp -> SUN MOON EPHEMERIDES
    //
    //     !WITH ii(k),jj(k),kk(k),ll(k),mm(k),nn(k) -> DOODSON NUMBERS FOR CONSTITUENT k
    //
    //     !WITH semi -> PHASE CORRECTION
    //
    
    //--- NOTE: No checks here for a potentially null sunMoonEphemerides and-or doodsonNumsArray objects, we need
    // performance here.
    
    final double vAstro = (doodsonNumsArray[0] * sunMoonEphemerides.tau +
        doodsonNumsArray[1] * sunMoonEphemerides.moonS +
        doodsonNumsArray[2] * sunMoonEphemerides.sunH +
        doodsonNumsArray[3] * sunMoonEphemerides.moonP +
        doodsonNumsArray[4] * sunMoonEphemerides.meanAscModeNP +
        doodsonNumsArray[5] * sunMoonEphemerides.sunPP) + phaseCorrection;
    
    //--- Only the fractional part of vAstro is needed if it is superior to
    //    int constant "V_ASTRO_PHASE_INT_THRESHOLD"
    //    ( V_ASTRO_PHASE_INT_THRESHOLD is actually 2)
    
    //System.out.println("getMainConstAstroArgument: vAstro bef. return="+vAstro);
    
    //--- ECCC C/C++ version of the return statement kept for the historic aspect.
    //    return ( vAstro - ( ( int ) vAstro / V_ASTRO_PHASE_INT_THRESHOLD  ) * V_ASTRO_PHASE_INT_THRESHOLD ) ;
    
    //--- Java version of the return statement:
    //    The int cast operation must be done after the vAstro / V_ASTRO_PHASE_INT_THRESHOLD
    //    doubleing point division ( i.e. V_ASTRO_PHASE_INT_THRESHOLD is automagically
    //    promoted to double before the division )
    return vAstro - V_ASTRO_PHASE_INT_THRESHOLD * (int) (vAstro / V_ASTRO_PHASE_INT_THRESHOLD);
  }
  
  /**
   * @param timePosLIncr : The time increment in seconds to add to this.sse to define the new time reference for the
   *                     astronomic informations.
   * @return A generic ForemanAstroInfosFactory object.
   */
  @NotNull
  @Override
  public ForemanAstroInfosFactory updateTimeReference(final long timePosLIncr) {
    
    //--- NOTE: timePosLIncr could be < 0 here:
    //    It is the AstroInfosFactory updateTimeReference method.
    super.updateTimeReference(timePosLIncr);
    
    //final long ts= Instant.now().toEpochMilli();
    
    this.sunMoonEphemerides.set(this.sse.getCalendarItem(Calendar.HOUR_OF_DAY), getAstroD1(this.sse.seconds()));
    
    //final long tf= Instant.now().toEpochMilli();
    //this.log.debug("ForemanAstroInfosFactory updateTimeReference: (tf-ts)="+(tf-ts));
    
    return this;
  }
  
  /**
   * @param seconds : Seconds since the epoch of a time referencei in UTC.
   * @return The number of days elapsed since December 31 1899 at 12:00:00UTC
   */
  protected final static double getAstroD1(final long seconds) {
    
    // TEST
    //staticLog.debug("(sseL + GREGORIAN_D0_EPOCH_SECONDS)/(double)SECONDS_PER_DAY="+(sseL +
    // GREGORIAN_D0_EPOCH_SECONDS)/(double)SECONDS_PER_DAY);
    //staticLog.debug("(sseL + GREGORIAN_D0_EPOCH_SECONDS)*="+(sseL + GREGORIAN_D0_EPOCH_SECONDS)
    // *SECONDS_PER_DAY_INVDP);
    
    //--- More efficient
    //--- TODO: Use the following multiplication instead of a division to get a better performance.
    //return (sseL + GREGORIAN_D0_EPOCH_SECONDS)*SECONDS_PER_DAY_INVDP;
    
    //--- Less efficient using division.
    return (seconds + GREGORIAN_D0_EPOCH_SECONDS) / (double) SECONDS_PER_DAY;
  }
  
  //--- For possible future usage
//    protected final static double getAstroD1(@NotNull @Size(min=DATE_TIME_FMT_LEN) final int [] dateTimeVector ) {
//
//        //--- DateTimeVector must have DATE_TIME_FMT6_LEN int elements:
////        try {
////
////            final int check= dateTimeVector[DATE_TIME_FMT_LEN-1];
////
////        } catch(ArrayIndexOutOfBoundsException e) {
////
////            staticLog.error("getAstroD1: dateTimeVector[] array must have dimension DATE_TIME_FMT_LEN= " +
// Integer.toString(DATE_TIME_FMT_LEN)) ;
////            throw new RuntimeException(e);
////        }
//
//        return getAstroD1(TimeMachine.getUTCLongSinceEpoch(dateTimeVector));
//    }
  
  //--- Traditional way(i.e. with the method SunMoonEphemerides.getGregorianDay translated in Javafrom the Fortran
  // sub-routine GDAY of M. Foreman's package)
  //    to compute astronomic parameter d1. It is kept here just for historical reasons.
  //
//    protected final static double getAstroD1Old(@NotNull @Size(min=DATE_TIME_FMT_LEN)  final int dateTimeVector[] ) {
//
//        //--- DateTimeVector must have DATE_TIME_FMT6_LEN int elements:
//        try {
//
//            final int check= dateTimeVector[DATE_TIME_FMT_LEN-1];
//
//        } catch(ArrayIndexOutOfBoundsException e) {
//
//            staticLog.error("getAstroD1Old: dateTimeVector[] array must have dimension DATE_TIME_FMT_LEN= " +
//            Integer.toString(DATE_TIME_FMT_LEN));
//            throw new RuntimeException(e);
//        }
//
//        final long gDay= SunMoonEphemerides.getGregorianDay(dateTimeVector[YEAR_INDEX],
//        dateTimeVector[MONTH_INDEX],dateTimeVector[MDAY_INDEX]);
//
//        // ***NOTE*** M. Foreman's algorithms for astronomic arguments computations are using December 31 1899 at
//        //            12UTC as the gregorian day 0 so we need to subtract HALF_GREGORIAN_DAY_ADJUSTEMENT to get the
//        //            right astroD1 value here.
//        return ((double)gDay + ((double)dateTimeVector[HOURS_INDEX])/24.0 -
//                (double)LAST_GREGORIAN_DAY_OF_19TH_CENTURY - HALF_GREGORIAN_DAY_ADJUSTEMENT ) ;
//    }

}
