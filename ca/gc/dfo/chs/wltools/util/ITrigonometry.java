//package ca.gc.dfo.iwls.fmservice.modeling.util;
package ca.gc.dfo.chs.wltools.util;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

/**
 * Interface defining trigonometric constants.
 */
public interface ITrigonometry extends ITimeMachine {
  
  double TWO_PI = 2.0 * Math.PI;
  
  double DEGREES_2_RADIANS = Math.PI / 180.0;
  
  double RADIANS_2_DEGREES = 1.0 / DEGREES_2_RADIANS;
  
  //--- Cycles to radians conversion factor.
  double CYCLES_2_RADIANS = TWO_PI;
  
  double DEGREES_PER_HOUR = 360.0 / HOURS_PER_DAY;
}
