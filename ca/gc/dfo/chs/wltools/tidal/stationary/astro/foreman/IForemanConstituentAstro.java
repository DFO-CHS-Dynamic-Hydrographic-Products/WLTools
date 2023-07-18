package ca.gc.dfo.chs.wltools.tidal.stationary.astro.foreman;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

//---

//---

import ca.gc.dfo.iwls.fmservice.modeling.tides.ITidesIO;
import ca.gc.dfo.iwls.fmservice.modeling.util.ITimeMachine;
import ca.gc.dfo.iwls.fmservice.modeling.util.ITrigonometry;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.IConstituentAstro;

/**
 * Interface for classes designed to apply Foreman's tidal prediction method.
 */
public interface IForemanConstituentAstro extends ITidesIO, ITimeMachine, ITrigonometry {
  
  /**
   * To compute(as a constant long int) the number of days(in seconds) elapsed since December 31 1899 at
   * 12:00:00UTC as it is
   * used by Foreman's tidal prediction method as the zero'th day for the computation of the astronomic parameter
   * d1(which
   * depends also on the prediction date-time start)
   */
  //
  //     NOTE 1: Are we able to use M. Foreman's method to get tidal predictions starting before December 31 1899 ???
  //
  //     NOTE 2: The result of JDK-1.8 ZonedDateTime.of(1899,12,31,12,0,0,0,ZoneId.of("GMT")).toEpochSecond() is
  //     negative since
  //             it goes backward in time from January 01 1970 at 00:00:00UTC then we convert it to a positive long
  //             here:
  
  //--- for JDK-1.7:
  //    NOTE that we can compute GREGORIAN_D0_EPOCH_SECONDS with GregorianCalendar or
  //    JDK-1.8 ZonedDateTime class but it's better to use a constant here since it always gives the same value:
  long GREGORIAN_D0_EPOCH_SECONDS = 2209032000L;
  
  //--- ZonedDateTime from JDK-1.8 java.time package only:
  //    As stated above, we need to negate the result of method ZonedDateTime.of() to get a positive value:
  //    final static long GREGORIAN_D0_EPOCH_SECONDS= -ZonedDateTime.of(1899,12,31,12,0,0,0,ZoneId.of("GMT"))
  //    .toEpochSecond();
  
  /**
   * YYYYMMDDhhmmss date-time stamp array length.
   */
  int DATE_TIME_FMT_LEN = 6;
  
  /**
   * String split index needed to deal with Foreman's traditional ASCII static main tidal constituents
   * astronomic data as defined in the dood_num.txt file of the TCWLTools package.
   */
  int SPLIT_CTL_LEN = 7;
  //final static int ITEM_2_SPLIT= 6 ;
  
  /**
   * Doodson numbers array length.
   */
  int MC_DOODSON_NUM_LEN = 6;
  
  /**
   * Main constituents satellites Doodson numbers changes array length.
   */
  int MC_DOODSON_NUM_CHG_LEN = 3;
  
  /**
   * To control that main constituents satellites data lines have the right number of items.
   */
  int MC_SATS_INFO_LEN = 5;
  
  /**
   * Used to split shallow water constituents data String lines items as in the shal_water_coef.txt file of the
   * TCWLTools package.
   */
  int DATA_COMBO_LEN = 2;
  
  /**
   * Cycles/hour to radians/second conversion factor
   */
  double CPH_2_RPS = TWO_PI / (double) SECONDS_PER_HOUR;
  
  /**
   * Used to split main constituents satellites data String lines items as defined in the dood_num.txt file of the
   * TCWLTools package.
   */
  String SAT_AMP_RATIO_SPLIT_STR = "R";
  
  //--- Constants for Sum & Moon ephemerides conputations:
  
  /**
   * Factor applied to astro d1 to get astro d2(see source code of SunMoonEphemerides class)
   */
  double ASTRO_D1_FACTOR = 0.0001;
  
  /**
   * Denominator used in computations done in SunMoonEphemerides set method.
   */
  double ASTRO_F = 360.0;
  
  //--- TODO: Replace ASTRO_F denominator usage by ASTRO_F_INV as a factor to minimize the number of divisions done
  // in computations loops.
  //final static double ASTRO_F_INV= 1.0/360.0;
  
  double ASTRO_F2 = ASTRO_F / (double) DAYS_PER_NORMAL_YEAR;
  
  //---    TODO:  Replace ASTRO_F2 denominator usage by ASTRO_F2_INV as a factor to minimize the number of divisions
  // done in computations loops.
  //final static double ASTRO_F2_INV= ASTRO_F_INV*(double)DAYS_PER_NORMAL_YEAR;
  
  /**
   * Derivative factor applied to the 2nd degree polynomial sun moon ephemerides coefficients.
   */
  double DEG2_DERIVATIVE_FACTOR = 0.00000002;
  
  /**
   * Derivative factor applied to the 3rd degree polynomial sun moon ephemerides coefficients.
   */
  double DEG3_DERIVATIVE_FACTOR = 0.000000000003;
  
  /**
   * Coefficients of the polynomial expression used for the computation the mean longitude of the sun.
   */
  double[] SUN_EPH_POLY_COEFF_H = {279.696678,
      0.9856473354,
      0.00002267};
  /**
   * 1st time derivative of the polynomial expression used for the computation the mean longitude of the sun.
   */
  double[] SUN_EPH_POLY_COEFF_DH = {SUN_EPH_POLY_COEFF_H[1],
      (DEG2_DERIVATIVE_FACTOR * SUN_EPH_POLY_COEFF_H[2])};
  
  //HARD-CODED: final static double SUN_EPH_POLY_COEFF_DH [ ]= {  0.9856473354 ,
  //HARD-CODED:                                                  ( 0.00000002 * 0.00002267 ) } ;
  
  /**
   * Coefficients of the polynomial expression used for the computation the Mean longitude of the solar perigee
   */
  double[] SUN_EPH_POLY_COEFF_PP = {281.220833,
      0.0000470684,
      0.0000339,
      0.00000007};
  /**
   * 1st time derivative of the polynomial expression used for the computation the Mean longitude of the solar perigee
   */
  double[] SUN_EPH_POLY_COEFF_DPP = {SUN_EPH_POLY_COEFF_PP[1],
      (DEG2_DERIVATIVE_FACTOR * SUN_EPH_POLY_COEFF_PP[2]),
      (DEG3_DERIVATIVE_FACTOR * SUN_EPH_POLY_COEFF_PP[3])};
  
  //HARD-CODED: final static double SUN_EPH_POLY_COEFF_DPP [ ]= { 0.0000470684,
  //HARD-CODED:                                                 ( 0.00000002 * 0.0000339 ),
  //HARD-CODED:                                                 ( 0.000000000003 * 0.00000007 ) } ;
  
  /**
   * Coefficients of the polynomial expression used for the computation the mean longitude of the moon.
   */
  double[] MOON_EPH_POLY_COEFF_S = {270.434164,
      13.1763965268,
      0.000085,
      0.000000039};
  /**
   * 1st time derivative of the polynomial expression used for the computation the mean longitude of the moon.
   */
  double[] MOON_EPH_POLY_COEFF_DS = {MOON_EPH_POLY_COEFF_S[1],
      (DEG2_DERIVATIVE_FACTOR * MOON_EPH_POLY_COEFF_S[2]),
      (DEG3_DERIVATIVE_FACTOR * MOON_EPH_POLY_COEFF_S[3])};
  
  //HARD-CODED: final static double MOON_EPH_POLY_COEFF_DS [ ]= { 13.1763965268,
  //HARD-CODED:                                                  ( 0.00000002 * 0.000085 ),
  //HARD-CODED:                                                  ( 0.000000000003 * 0.000000039 ) } ;
  
  /**
   * Coefficients of the polynomial expression used for the computation the mean longitude of the lunar perigee.
   */
  double[] MOON_EPH_POLY_COEFF_P = {334.329556,
      0.1114040803,
      0.0007739,
      0.00000026};
  /**
   * 1st time derivative of the polynomial expression used for the computation the mean longitude of the lunar perigee.
   */
  double[] MOON_EPH_POLY_COEFF_DP = {MOON_EPH_POLY_COEFF_P[1],
      (DEG2_DERIVATIVE_FACTOR * MOON_EPH_POLY_COEFF_P[2]),
      (DEG3_DERIVATIVE_FACTOR * MOON_EPH_POLY_COEFF_P[3])};
  
  //HARD-CODED: final static double MOON_EPH_POLY_COEFF_DP [ ]= { 0.1114040803,
  //HARD-CODED:                                                   ( 0.00000002 * 0.0007739),
  //HARD-CODED:                                                   ( 0.000000000003 * 0.00000026 ) } ;
  
  /**
   * Coefficients of the polynomial expression used for the computation the mean longitude of the negative of the
   * longitude of the mean ascending node.
   */
  double[] MEAN_ASCMODE_EPH_POLY_COEFF_NP = {-259.183275,
      0.0529539222,
      0.0001557,
      0.00000005};
  
  /**
   * 1st time derivative of the polynomial expression used for the computation the mean longitude of the negative of
   * the longitude of the mean ascending node.
   */
  double[] MEAN_ASCMODE_EPH_POLY_COEFF_DNP = {MEAN_ASCMODE_EPH_POLY_COEFF_NP[1],
      (DEG2_DERIVATIVE_FACTOR * MEAN_ASCMODE_EPH_POLY_COEFF_NP[2]),
      (DEG3_DERIVATIVE_FACTOR * MEAN_ASCMODE_EPH_POLY_COEFF_NP[3])};
  
  //HARD-CODED: final static double MEAN_ASCMODE_EPH_POLY_COEFF_DNP [ ]= { 0.0529539222,
  //HARD-CODED:                                                          (0.00000002 * 0.0001557),
  //HARD-CODED:                                                          (0.000000000003 * 0.00000005) } ;
  
  //--- Some other constants specific for the astronomic informations computations
  
  /**
   * Using cosinus constant index defined only once for cos-sin array passed between methods.
   */
  int COS_INDEX = 0;
  
  /**
   * Using sinus constant index defined only once for cos-sin array passed between methods.
   */
  int SIN_INDEX = 1;
  
  /**
   * Using switch blocks decision constant defined only once here instead of hard-coded stuff as it is done in
   * TCWLTools package fortran src code.
   */
  int FV_NODAL_ADJ_FLAG_1 = 1;
  
  /**
   * Using switch blocks decision constant defined only once here instead of hard-coded stuff as it is done in
   * TCWLTools package fortran src code.
   */
  int FV_NODAL_ADJ_FLAG_2 = 2;
  
  /**
   * Default constant value used for nodal modulation adjustment factor for amplitude in ForemanConstituentAstro
   * constructor. Defined only once
   * here instead of hard-coded stuff as it is done in TCWLTools package fortran src code.
   */
  double F_NODAl_MOD_ADJ_INIT = 1.0;
  
  /**
   * Constant value used in the computations of nodal modulation adjustment factor for amplitude and also for the
   * astronomical argument in
   * ForemanConstituentAstro getMainConstCosSinAcc method. Defined only once here instead of hard-coded stuff as it
   * is done in TCWLTools
   * package fortran src code.
   */
  double FV_NODAL_ADJ_CONSTANT_1 = 0.36309;
  
  /**
   * Constant value used in the computations of nodal modulation adjustment factor for amplitude and also for the
   * astronomical argument in
   * ForemanConstituentAstro getMainConstCosSinAcc method. Defined only once here instead of hard-coded stuff as it
   * is done in TCWLTools
   * package fortran src code.
   */
  double FV_NODAL_ADJ_CONSTANT_2 = 2.59808;
  
  /**
   * Constant value used in the initialization of the cosinus data accumulator in ForemanConstituentAstro
   * getMainConstCosSinAcc method.
   * Defined only once here instead of hard-coded stuff as it is done in TCWLTool package fortran src code.
   */
  double COS_ACC_INIT = 1.0;
  
  /**
   * Constant value used in the initialization of the sinus data accumulator in ForemanConstituentAstro
   * getMainConstCosSinAcc method.
   * Defined only once here instead of hard-coded stuff as it is done in TCWLTool package fortran src code.
   */
  double SIN_ACC_INIT = 0.0;
  
  /**
   * Constant factor value used in the ForemanAstroInfosFactory getMainConstAstroArgument method. Defined only once
   * here instead of
   * hard-coded stuff as it is done in TCWLTool package fortran src code.
   */
  double V_ASTRO_PHASE_INT_THRESHOLD = 2;
  
  //--- All(including shallow water) 146 tidal constituents strings IDs as used in M. Foreman's package:
  
  /**
   * Static definitions of all 146 tidal constituents strings IDs as used in M. Foreman's package.
   * (compare with TCWLTool package config. ASCII file const_list.txt)
   */
  String[] TC_NAMES = {
      "Z0",
      "SA",
      "SSA",
      "MSM",
      "MM",
      "MSF",
      "MF",
      "ALP1",
      "2Q1",
      "SIG1",
      "Q1",
      "RHO1",
      "O1",
      "TAU1",
      "BET1",
      "NO1",
      "CHI1",
      "PI1",
      "P1",
      "S1",
      "K1",
      "PSI1",
      "PHI1",
      "THE1",
      "J1",
      "2PO1",
      "SO1",
      "OO1",
      "UPS1",
      "ST36",
      "2NS2",
      "ST37",
      "ST1",
      "OQ2",
      "EPS2",
      "ST2",
      "ST3",
      "O2",
      "2N2",
      "MU2",
      "SNK2",
      "N2",
      "NU2",
      "ST4",
      "OP2",
      "GAM2",
      "H1",
      "M2",
      "H2",
      "MKS2",
      "ST5",
      "ST6",
      "LDA2",
      "L2",
      "2SK2",
      "T2",
      "S2",
      "R2",
      "K2",
      "MSN2",
      "ETA2",
      "ST7",
      "2SM2",
      "ST38",
      "SKM2",
      "2SN2",
      "NO3",
      "MO3",
      "M3",
      "NK3",
      "SO3",
      "MK3",
      "SP3",
      "SK3",
      "ST8",
      "N4",
      "3MS4",
      "ST39",
      "MN4",
      "ST9",
      "ST40",
      "M4",
      "ST10",
      "SN4",
      "KN4",
      "MS4",
      "MK4",
      "SL4",
      "S4",
      "SK4",
      "MNO5",
      "2MO5",
      "3MP5",
      "MNK5",
      "2MP5",
      "2MK5",
      "MSK5",
      "3KM5",
      "2SK5",
      "ST11",
      "2NM6",
      "ST12",
      "2MN6",
      "ST13",
      "ST41",
      "M6",
      "MSN6",
      "MKN6",
      "ST42",
      "2MS6",
      "2MK6",
      "NSK6",
      "2SM6",
      "MSK6",
      "S6",
      "ST14",
      "ST15",
      "M7",
      "ST16",
      "3MK7",
      "ST17",
      "ST18",
      "3MN8",
      "ST19",
      "M8",
      "ST20",
      "ST21",
      "3MS8",
      "3MK8",
      "ST22",
      "ST23",
      "ST24",
      "ST25",
      "ST26",
      "4MK9",
      "ST27",
      "ST28",
      "M10",
      "ST29",
      "ST30",
      "ST31",
      "ST32",
      "ST33",
      "M12",
      "ST34",
      "ST35"
  };
  
  //--- Define all main tidal constituent static informations in a
  //    Java enum object instead of reading an plain old ASCII file.
  //    NOTE: Need to prepend all constituents names ids. with an underscore to
  //             deal with some having a digit as the 1st character.
  
  /**
   * Define the contents of the TCWLTool package config. ASCII file dood_numb.txt(main constituents static data) in a
   * static enum
   * to avoid reading those ASCII files.
   * NOTE: We(hope that we) will eventually get all this stuff in the form of some kind of Java objects returned from a
   * DB SQL request or alternatively as XML formatted S-112 PPO data
   */
  enum MainConstituentsLegacyDef {
    
    _Z0("0  0  0  0  0  0 0.0    0", null),
    _SA("0  0  1  0  0 -1 0.0    0", null),
    _SSA("0  0  2  0  0  0 0.0    0", null),
    _MSM("0  1 -2  1  0  0  .00   0", null),
    _MM("0  1  0 -1  0  0 0.0    0", null),
    _MSF("0  2 -2  0  0  0 0.0    0", null),
    _MF("0  2  0  0  0  0 0.0    0", null),
    
    _ALP1("1 -4  2  1  0  0 -.25   2", new String[]{"-1  0  0 .75 0.0360R1", " 0 -1  0 .00 0.1906  "}),
    
    _2Q1("1 -3  0  2  0  0-0.25   5", new String[]{"-2 -2  0 .50 0.0063  ", "-1 -1  0 .75 0.0241R1", "-1  0  0 .75 0" +
        ".0607R1",
        " 0 -2  0 .50 0.0063  ", " 0 -1  0 .0  0.1885  "}),
    
    _SIG1("1 -3  2  0  0  0-0.25   4", new String[]{"-1  0  0 .75 0.0095R1", " 0 -2  0 .50 0.0061  ", " 0 -1  0 .0  0" +
        ".1884  ",
        " 2  0  0 .50 0.0087  "}),
    
    _Q1("1 -2  0  1  0  0-0.25  10", new String[]{"-2 -3  0 .50 0.0007  ", "-2 -2  0 .50 0.0039  ", "-1 -2  0 .75 0" +
        ".0010R1",
        "-1 -1  0 .75 0.0115R1", "-1  0  0 .75 0.0292R1", " 0 -2  0 .50 0.0057  ",
        "-1  0  1 .0  0.0008  ", " 0 -1  0 .0  0.1884  ", " 1  0  0 .75 0.0018R1",
        " 2  0  0 .50 0.0028  "}),
    
    _RHO1("1 -2  2 -1  0  0-0.25   5", new String[]{" 0 -2  0 .50 0.0058  ", " 0 -1  0 .0  0.1882  ", " 1  0  0 .75 0" +
        ".0131R1",
        " 2  0  0 .50 0.0576  ", " 2  1  0 .0  0.0175  "}),
    
    _O1("1 -1  0  0  0  0-0.25   8", new String[]{"-1  0  0 .25 0.0003R1", " 0 -2  0 .50 0.0058  ", " 0 -1  0 .0  0" +
        ".1885  ",
        " 1 -1  0 .25 0.0004R1", " 1  0  0 .75 0.0029R1", " 1  1  0 .25 0.0004R1",
        " 2  0  0 .50 0.0064  ", " 2  1  0 .50 0.0010  "}),
    
    _TAU1("1 -1  2  0  0  0-0.75   5", new String[]{"-2  0  0 .0  0.0446  ", "-1  0  0 .25 0.0426R1", " 0 -1  0 .50 0" +
        ".0284  ",
        " 0  1  0 .50 0.2170  ", " 0  2  0 .50 0.0142  "}),
    
    _BET1("1  0 -2  1  0  0 -.75   1", new String[]{" 0 -1  0 .00 0.2266  "}),
    
    _NO1("1  0  0  1  0  0-0.75   9", new String[]{"-2 -2  0 .50 0.0057  ", "-2 -1  0 .0  0.0665  ", "-2  0  0 .0  0" +
        ".3596  ",
        "-1 -1  0 .75 0.0331R1", "-1  0  0 .25 0.2227R1", "-1  1  0 .75 0.0290R1",
        " 0 -1  0 .50 0.0290  ", " 0  1  0 .0  0.2004  ", " 0  2  0 .50 0.0054  "}),
    
    _CHI1("1  0  2 -1  0  0-0.75   2", new String[]{" 0 -1  0 .50 0.0282  ", " 0  1  0 .0  0.2187  "}),
    
    _PI1("1  1 -3  0  0  1-0.25   1", new String[]{" 0 -1  0 .50 0.0078  "}),
    
    _P1("1  1 -2  0  0  0-0.25   6", new String[]{" 0 -2  0 .0  0.0008  ", " 0 -1  0 .50 0.0112  ", " 0  0  2 .50 0" +
        ".0004  ",
        " 1  0  0 .75 0.0004R1", " 2  0  0 .50 0.0015  ", " 2  1  0 .50 0.0003  "}),
    
    _S1("1  1 -1  0  0  1-0.75   2", new String[]{" 0  0 -2 .0  0.3534  ", " 0  1  0 .50 0.0264  "}),
    
    _K1("1  1  0  0  0  0-0.75  10", new String[]{"-2 -1  0 .0  0.0002  ", "-1 -1  0 .75 0.0001R1", "-1  0  0 .25 0" +
        ".0007R1",
        "-1  1  0 .75 0.0001R1", " 0 -2  0 .0  0.0001  ", " 0 -1  0 .50 0.0198  ",
        " 0  1  0 .0  0.1356  ", " 0  2  0 .50 0.0029  ", " 1  0  0 .25 0.0002R1",
        " 1  1  0 .25 0.0001R1"}),
    
    _PSI1("1  1  1  0  0 -1-0.75   1", new String[]{" 0  1  0 .0  0.0190  "}),
    
    _PHI1("1  1  2  0  0  0-0.75   5", new String[]{"-2  0  0 .0  0.0344  ", "-2  1  0 .0  0.0106  ", " 0  0 -2 .0  0" +
        ".0132  ",
        " 0  1  0 .50 0.0384  ", " 0  2  0 .50 0.0185  "}),
    
    _THE1("1  2 -2  1  0  0 -.75   4", new String[]{"-2 -1  0 .00  .0300  ", "-1  0  0 .25 0.0141R1", "0 -1  0 .50  " +
        ".0317   ",
        " 0  1  0 .00  .1993  "}),
    
    _J1("1  2  0 -1  0  0-0.75  10", new String[]{" 0 -1  0 .50 0.0294  ", " 0  1  0 .0  0.1980  ", " 0  2  0 .50 0" +
        ".0047  ",
        " 1 -1  0 .75 0.0027R1", " 1  0  0 .25 0.0816R1", " 1  1  0 .25 0.0331R1",
        " 1  2  0 .25 0.0027R1", " 2  0  0 .50 0.0152  ", " 2  1  0 .50 0.0098  ",
        " 2  2  0 .50 0.0057  "}),
    
    _OO1("1  3  0  0  0  0-0.75   8", new String[]{"-2 -1  0 .50 0.0037  ", "-2  0  0 .0  0.1496  ", "-2  1  0 .0  0" +
        ".0296  ",
        "-1  0  0 .25 0.0240R1", "-1  1  0 .25 0.0099R1", " 0  1  0 .0  0.6398  ",
        " 0  2  0 .0  0.1342  ", " 0  3  0 .0  0.0086  "}),
    
    _UPS1("1  4  0 -1  0  0 -.75   5", new String[]{"-2  0  0 .00 0.0611  ", " 0  1  0 .00 0.6399  ", " 0  2  0 .00 0" +
        ".1318  ",
        " 1  0  0 .25 0.0289R1", " 1  1  0 .25 0.0257R1"}),
    
    _OQ2("2 -3  0  3  0  0 0.0    2", new String[]{"-1  0  0 .25 0.1042R2", " 0 -1  0 .50 0.0386  "}),
    
    _EPS2("2 -3  2  1  0  0 0.0    3", new String[]{"-1 -1  0 .25 0.0075R2", "-1  0  0 .25 0.0402R2", " 0 -1  0 .50 0" +
        ".0373  "}),
    
    _2N2("2 -2  0  2  0  0 0.0    4", new String[]{"-2 -2  0 .50 0.0061  ", "-1 -1  0 .25 0.0117R2", "-1  0  0 .25 0" +
        ".0678R2",
        " 0 -1  0 .50 0.0374  "}),
    
    _MU2("2 -2  2  0  0  0 0.0    3", new String[]{"-1 -1  0 .25 0.0018R2", "-1  0  0 .25 0.0104R2", " 0 -1  0 .50 0" +
        ".0375  "}),
    
    _N2("2 -1  0  1  0  0 0.0    4", new String[]{"-2 -2  0 .50 0.0039  ", "-1  0  1 .00 0.0008  ", " 0 -2  0 .00 0" +
        ".0005  ",
        " 0 -1  0 .50 0.0373  "}),
    
    _NU2("2 -1  2 -1  0  0 0.0    4", new String[]{" 0 -1  0 .50 0.0373  ", " 1  0  0 .75 0.0042R2", " 2  0  0 .0  0" +
        ".0042  ",
        " 2  1  0 .50 0.0036  "}),
    
    _GAM2("2  0 -2  2  0  0 -.50   3", new String[]{" -2 -2  0 .00 0.1429 ", "-1  0  0 .25 0.0293R2", "0 -1  0 .50 0" +
        ".0330   "}),
    
    _H1("2  0 -1  0  0  1-0.50   2", new String[]{" 0 -1  0 .50 0.0224  ", " 1  0 -1 .50 0.0447  "}),
    
    _M2("2  0  0  0  0  0 0.0    9", new String[]{"-1 -1  0 .75 0.0001R2", "-1  0  0 .75 0.0004R2", " 0 -2  0 .0  0" +
        ".0005  ",
        " 0 -1  0 .50 0.0373  ", " 1 -1  0 .25 0.0001R2", " 1  0  0 .75 0.0009R2",
        " 1  1  0 .75 0.0002R2", " 2  0  0 .0  0.0006  ", " 2  1  0 .0  0.0002  "}),
    
    _H2("2  0  1  0  0 -1 0.0    1", new String[]{" 0 -1  0 .50 0.0217  "}),
    
    _LDA2("2  1 -2  1  0  0-0.50   1", new String[]{" 0 -1  0 .50 0.0448  "}),
    
    _L2("2  1  0 -1  0  0-0.50   5", new String[]{" 0 -1  0 .50 0.0366  ", " 2 -1  0 .00 0.0047  ", " 2  0  0 .50 0" +
        ".2505  ",
        " 2  1  0 .50 0.1102  ", " 2  2  0 .50 0.0156  "}),
    
    _T2("2  2 -3  0  0  1 0.0    0", null),
    
    _S2("2  2 -2  0  0  0 0.0    3", new String[]{" 0 -1  0 .0  0.0022  ", " 1  0  0 .75 0.0001R2", " 2  0  0 .0  0" +
        ".0001  "}),
    
    _R2("2  2 -1  0  0 -1-0.50   2", new String[]{" 0  0  2 .50 0.2535  ", " 0  1  2 .0  0.0141  "}),
    
    _K2("2  2  0  0  0  0 0.0    5", new String[]{"-1  0  0 .75 0.0024R2", "-1  1  0 .75 0.0004R2", " 0 -1  0 .50 0" +
        ".0128  ",
        " 0  1  0 .0  0.2980  ", " 0  2  0 .0  0.0324  "}),
    
    _ETA2("2  3  0 -1  0  0 0.0    7", new String[]{" 0 -1  0 .50 0.0187  ", " 0  1  0 .0  0.4355  ", " 0  2  0 .0  0" +
        ".0467  ",
        " 1  0  0 .75 0.0747R2", " 1  1  0 .75 0.0482R2", " 1  2  0 .75 0.0093R2",
        " 2  0  0 .50 0.0078  "}),
    
    _M3("3  0  0  0  0  0 -.50   1", new String[]{"  0 -1  0 .50  .0564 "});
    
    final String mandatoryDef;
    final String[] satellitesDef;
    
    MainConstituentsLegacyDef(final String mandatoryDef, final String[] satellitesDef) {
      
      //--- NOTE : The last sub-String of the mandatoryDef String which gives the number of
      //           satellites fo reach main constituent is not necessary in Java but it is
      //           kept for the historical aspect.
      this.mandatoryDef = mandatoryDef;
      this.satellitesDef = satellitesDef;
    }
  }
  
  //--- Define all shallow water tidal constituent static informations in a
  //    Java enum object instead of reading an plain old ASCII file.
  //    NOTE: Need to prepend all constituents names ids. with an underscore to
  //          deal with some having a digit as the 1st character.
  
  /**
   * Define the contents of the TCWLTool package config. ASCII file shal_water_coef.txt(shallow water constituents
   * statio data) in a static
   * enum to avoid reading those ASCII files.
   * NOTE: We(hope that we) will eventually get all this stuff in the form of some kind of Java objects returned from a
   * DB SQL request or alternatively as XML formatted S-112 PPO data
   */
  enum ShallowWaterConstituentsLegacyDef {
    
    _2PO1("2 2.0 P1 -1.0 O1"),
    _SO1("2 1.0 S2 -1.0 O1"),
    _ST36("3 2.0 M2  1.0 N2 -2.0 S2"),
    _2NS2("2 2.0 N2 -1.0 S2"),
    _ST37("2 3.0 M2 -2.0 S2"),
    _ST1("3 2.0 N2  1.0 K2 -2.0 S2"),
    _ST2("4 1.0 M2  1.0 N2  1.0 K2 -2.0 S2"),
    _ST3("3 2.0 M2  1.0 S2 -2.0 K2"),
    _O2("1 2.0 O1"),
    _ST4("3 2.0 K2  1.0 N2 -2.0 S2"),
    _SNK2("3 1.0 S2  1.0 N2 -1.0 K2"),
    _OP2("2 1.0 O1  1.0 P1"),
    _MKS2("3 1.0 M2  1.0 K2 -1.0 S2"),
    _ST5("3 1.0 M2  2.0 K2 -2.0 S2"),
    _ST6("4 2.0 S2  1.0 N2 -1.0 M2 -1.0 K2"),
    _2SK2("2 2.0 S2 -1.0 K2"),
    _MSN2("3 1.0 M2  1.0 S2 -1.0 N2"),
    _ST7("4 2.0 K2  1.0 M2 -1.0 S2 -1.0 N2"),
    _2SM2("2 2.0 S2 -1.0 M2"),
    _ST38("3 2.0 M2  1.0 S2 -2.0 N2"),
    _SKM2("3 1.0 S2  1.0 K2 -1.0 M2"),
    _2SN2("2 2.0 S2 -1.0 N2"),
    _NO3("2 1.0 N2  1.0 O1"),
    _MO3("2 1.0 M2  1.0 O1"),
    _NK3("2 1.0 N2  1.0 K1"),
    _SO3("2 1.0 S2  1.0 O1"),
    _MK3("2 1.0 M2  1.0 K1"),
    _SP3("2 1.0 S2  1.0 P1"),
    _SK3("2 1.0 S2  1.0 K1"),
    _ST8("3 2.0 M2  1.0 N2 -1.0 S2"),
    _N4("1 2.0 N2"),
    _3MS4("2 3.0 M2 -1.0 S2"),
    _ST39("4 1.0 M2  1.0 S2  1.0 N2 -1.0 K2"),
    _MN4("2 1.0 M2  1.0 N2"),
    _ST40("3 2.0 M2  1.0 S2 -1.0 K2"),
    _ST9("4 1.0 M2  1.0 N2  1.0 K2 -1.0 S2"),
    _M4("1 2.0 M2"),
    _ST10("3 2.0 M2  1.0 K2 -1.0 S2"),
    _SN4("2 1.0 S2  1.0 N2"),
    _KN4("2 1.0 K2  1.0 N2"),
    _MS4("2 1.0 M2  1.0 S2"),
    _MK4("2 1.0 M2  1.0 K2"),
    _SL4("2 1.0 S2  1.0 L2"),
    _S4("1 2.0 S2"),
    _SK4("2 1.0 S2  1.0 K2"),
    _MNO5("3 1.0 M2  1.0 N2  1.0 O1"),
    _2MO5("2 2.0 M2  1.0 O1"),
    _3MP5("2 3.0 M2 -1.0 P1"),
    _MNK5("3 1.0 M2  1.0 N2  1.0 K1"),
    _2MP5("2 2.0 M2  1.0 P1"),
    _2MK5("2 2.0 M2  1.0 K1"),
    _MSK5("3 1.0 M2  1.0 S2  1.0 K1"),
    _3KM5("3 1.0 K2  1.0 K1  1.0 M2"),
    _2SK5("2 2.0 S2  1.0 K1"),
    _ST11("3 3.0 N2  1.0 K2 -1.0 S2"),
    _2NM6("2 2.0 N2  1.0 M2"),
    _ST12("4 2.0 N2  1.0 M2  1.0 K2 -1.0 S2"),
    _ST41("3 3.0 M2  1.0 S2 -1.0 K2"),
    _2MN6("2 2.0 M2  1.0 N2"),
    _ST13("4 2.0 M2  1.0 N2  1.0 K2 -1.0 S2"),
    _M6("1 3.0 M2"),
    _MSN6("3 1.0 M2  1.0 S2  1.0 N2"),
    _MKN6("3 1.0 M2  1.0 K2  1.0 N2"),
    _2MS6("2 2.0 M2  1.0 S2"),
    _2MK6("2 2.0 M2  1.0 K2"),
    _NSK6("3 1.0 N2  1.0 S2  1.0 K2"),
    _2SM6("2 2.0 S2  1.0 M2"),
    _MSK6("3 1.0 M2  1.0 S2  1.0 K2"),
    _ST42("3 2.0 M2  2.0 S2 -1.0 K2"),
    _S6("1 3.0 S2"),
    _ST14("3 2.0 M2  1.0 N2  1.0 O1"),
    _ST15("3 2.0 N2  1.0 M2  1.0 K1"),
    _M7("1 3.5 M2"),
    _ST16("3 2.0 M2  1.0 S2  1.0 O1"),
    _3MK7("2 3.0 M2  1.0 K1"),
    _ST17("4 1.0 M2  1.0 S2  1.0 K2  1.0 O1"),
    _ST18("2 2.0 M2  2.0 N2"),
    _3MN8("2 3.0 M2  1.0 N2"),
    _ST19("4 3.0 M2  1.0 N2  1.0 K2 -1.0 S2"),
    _M8("1 4.0 M2"),
    _ST20("3 2.0 M2  1.0 S2  1.0 N2"),
    _ST21("3 2.0 M2  1.0 N2  1.0 K2"),
    _3MS8("2 3.0 M2  1.0 S2"),
    _3MK8("2 3.0 M2  1.0 K2"),
    _ST22("4 1.0 M2  1.0 S2  1.0 N2  1.0 K2"),
    _ST23("2 2.0 M2  2.0 S2"),
    _ST24("3 2.0 M2  1.0 S2  1.0 K2"),
    _ST25("3 2.0 M2  2.0 N2  1.0 K1"),
    _ST26("3 3.0 M2  1.0 N2  1.0 K1"),
    _4MK9("2 4.0 M2  1.0 K1"),
    _ST27("3 3.0 M2  1.0 S2  1.0 K1"),
    _ST28("2 4.0 M2  1.0 N2"),
    _M10("1 5.0 M2"),
    _ST29("3 3.0 M2  1.0 N2  1.0 S2"),
    _ST30("2 4.0 M2  1.0 S2"),
    _ST31("4 2.0 M2  1.0 N2  1.0 S2  1.0 K2"),
    _ST32("2 3.0 M2  2.0 S2"),
    _ST33("3 4.0 M2  1.0 S2  1.0 K1"),
    _M12("1 6.0 M2"),
    _ST34("2 5.0 M2  1.0 S2"),
    _ST35("4 3.0 M2 1.0 N2 1.0 K2 1.0 S2");
    
    //--- NOTE : The 1st String of String array nbMainConstituentsDerivationDef which gives
    //           the number of main constituents used by each shallow water constttuent is not
    //           necessary in Java but it is kept for the historical aspect.
    final String mainConstituentsDerivationDef;
    
    ShallowWaterConstituentsLegacyDef(final String mainConstituentsDerivationDef) {
      
      this.mainConstituentsDerivationDef = mainConstituentsDerivationDef;
    }
  }
}
