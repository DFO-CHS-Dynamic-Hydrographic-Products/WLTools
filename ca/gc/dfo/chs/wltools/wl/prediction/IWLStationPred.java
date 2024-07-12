//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.prediction;

/**
 * Created by Gilles Mercier on 2018-01-10.
 */

import java.util.Set;
import java.util.List;

// ---
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPred;

//---
//import ca.gc.dfo.iwls.fmservice.modeling.tides.ITides;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.ITidesIO;


// --- TODO: Change class name from IWLStationPred to IWLLocationPred
/**
 * Class for the computation of water level predictions at a
 * specific location (tide gauge or model grid point).
 */
public interface IWLStationPred  {

  long TIME_NOT_DEFINED= -1L;

  long MIN_TIME_INCR_SECONDS= 60L;
  long MAX_TIME_INCR_SECONDS= 3600L;
  long DEFAULT_TIME_INCR_SECONDS= 180L;//900L;

  long DEFAULT_DAYS_DURATION_IN_PAST= 20L;
  long DEFAULT_DAYS_DURATION_IN_FUTURE= 40L;
  long MAX_DAYS_DURATION= 180L; //120L;

  // --- Define prediction types
  enum Type {
    TIDAL, // --- Can be of the non-stationary type (a.k.a mixed stage-discharge influence and astronomic tides) or stage-discharge only.
    CLIMATOLOGY // --- simple climatologic WL data (direct usage, year wrap-around timestamps)
  }

  String [] allowedTypesDef= { Type.TIDAL.name(), Type.CLIMATOLOGY.name() };

  Set<String> allowedTypes= Set.of(allowedTypesDef);

  //abstract public List<MeasurementCustom> getAllPredictions();
  abstract public IWLStationPred getAllPredictions();
  abstract public List<MeasurementCustom> getPredictionData();
}
