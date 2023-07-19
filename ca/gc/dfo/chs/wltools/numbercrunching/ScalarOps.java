//package ca.gc.dfo.iwls.fmservice.modeling.numbercrunching;
package ca.gc.dfo.chs.wltools.numbercrunching;

/**
 * Abstract class for static scalar operations.
 */

/**
 * Abstract utility class for static scalar operations.
 */
public abstract class ScalarOps {
  
  /**
   * @param dval : A double value
   * @return The double precision square of dval (WARNING: No checks to verify that the result of dval*dval produce
   * an overflow)
   */
  public final static double square(final double dval) {
    
    //--- NOTE: No checks to verify that the result of dval*dval produce an overflow.
    return dval * dval;
  }
  
  //--- For possible future usage.
//    /**
//     * @param fval : A double value.
//     * @return The double precision square of fval.
//     */
//    public final static double square(final double fval) {
//        return (double)fval*fval;
//    }
}
