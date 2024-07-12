package ca.gc.dfo.chs.wltools.tidal;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

import java.util.Set;

//---
import ca.gc.dfo.chs.wltools.util.ITimeMachine;

/**
 * Interface for tidal data constants.
 */
public interface ITidal extends ITimeMachine {

  long M2_WRAP_AROUND_CYCLE_HOURS= 25L;
  
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
    
    // ---- TODO: Rename  FOREMAN As STATIONARY_FOREMAN??
    STATIONARY_FOREMAN,  //--- Mike Foreman's (DFO-Sidney B.C.) classic HA method type:

    NON_STATIONARY_FOREMAN, // --- ECCC's Pascal Matte non-stationary method. Include both the astronomic tides and
                            //     Stage-discharge influences.

    NON_STATIONARY_STAGE // --- WL predictions calculed only with stage-discharge coefficients
                         //     (from NSTide regression results or other simple WL vs discharges linear regression analysis results)
                         //     The discharges data can be climatologic or from an operational model output.

    //--- In case we want to test T_TIDE method.
    //SATIONARY_T_TIDE     //--- T_Tide method type
  }

  String [] allowedMethodsDef= {
    Method.STATIONARY_FOREMAN.name(),
    Method.NON_STATIONARY_FOREMAN.name(),
    Method.NON_STATIONARY_STAGE.name()
  };

  Set<String> allowedMethods= Set.of(allowedMethodsDef);

  public double computeTidalPrediction(final long timeStampSeconds);
}
