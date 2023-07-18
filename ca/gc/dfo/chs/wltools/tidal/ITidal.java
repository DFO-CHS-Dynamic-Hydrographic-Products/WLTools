package ca.gc.dfo.chs.wltools.tidal;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

//---

import ca.gc.dfo.chs.wltools.util.ITimeMachine;

/**
 * Interface for tidal data constants.
 */
public interface ITidal extends ITimeMachine {
  
  /**
   * M. Foreman's method implies that the astronomic informations could only be updated for
   * multiples of 3600 seconds(1 hour)
   */
  long ASTRO_UDPATE_OFFSET_SECONDS = SECONDS_PER_HOUR;
  
  /**
   * Inverted double precision seconds per day to be used in astronomic computations loops.
   * It is computationnaly more efficient(especially in loops) to do a multiplication than to do a division.
   */
  double SECONDS_PER_DAY_INVDP = 1.0 / (double) SECONDS_PER_DAY;
  
  /**
   * Tidal methods available
   */
  enum Method {
    
    FOREMAN,  //--- Mike Foreman's (DFO-Sidney B.C.) classic HA method type :

    NON_STATIONARY // --- ECCC's Pascal Matte non-stationary method.

    //--- In case we want to test XTIDE method.
    //XTIDE     //--- XTide method type :  http://www.flaterco.com/xtide/files.html
  }
}
