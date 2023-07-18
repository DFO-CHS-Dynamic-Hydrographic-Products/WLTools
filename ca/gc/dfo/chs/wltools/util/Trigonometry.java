//package ca.gc.dfo.iwls.fmservice.modeling.util;
package ca.gc.dfo.chs.wltools.util;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

/**
 * Trigonometry computations utility.
 */
final public class Trigonometry implements ITrigonometry {
  
  /**
   * @param angle                 : An angle that could be defined in cycles
   * @param convertCycles2Radians : Flag to signal that the angle is in cycles.
   * @return The equivalent of the angle argument defined in radians between 0 and 2PI.
   */
  public static double getZero2PISandwich(final double angle, final boolean convertCycles2Radians) {
    
    return (convertCycles2Radians ? getZero2PiSandwich(CYCLES_2_RADIANS * angle) : getZero2PiSandwich(angle));
  }
  
  /**
   * @param radians: Could be positive or negative and its absolute value could be greater than 2PI
   * @return The equivalent of the radians argument defined between 0 and 2PI.
   */
  public static double getZero2PiSandwich(final double radians) {
    
    double newRad = radians;
    final double fabsRad = Math.abs(radians);
    
    if (!doubleSandwich(0.0, radians, TWO_PI)) {
      
      int intFact = (int) Math.floor(fabsRad / TWO_PI);
      
      if (intFact == 0) {
        
        newRad = TWO_PI - fabsRad;
        
      } else {
        
        newRad = fabsRad - ((double) intFact * TWO_PI);
        
        //---- Use the if shortcut instead of this -> if ( radians < 0.0 ) newRad= Trigo.TWO_DPI_RADIANS - newRad ;
        newRad = ((radians < 0.0) ? (TWO_PI - newRad) : newRad);
        
      } //--- INNER if-else block
      
    } //--- OUTER if
    
    return newRad;
  }
  
  /**
   * @param d1: Lower bound of the double range(a.k.a. sandwich).
   * @param d2: The double value to check if it's inside the range
   * @param d3: Upper bound of the double range.
   * @return true if d2 is sandwiched by d1 and d3, false otherwise
   */
  public static boolean doubleSandwich(final double d1, final double d2, final double d3) {
    
    //--- Return true if we have d1 <= d2 <= d3 i.e. d1 is clamped(sandwiched) between d2 and d3
    //    or if d1 == d2 or d2 == d3
    return (((d2 - d1) * (d2 - d3)) <= 0.0);
  }
  
}
