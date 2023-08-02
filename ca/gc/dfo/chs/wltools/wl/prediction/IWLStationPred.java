//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.prediction;

/**
 * Created by Gilles Mercier on 2018-01-10.
 */

//---
//import java.time.Instant;
//import java.util.HashMap;
//import java.util.List;
//import javax.validation.constraints.Min;

//import ca.gc.dfo.iwls.fmservice.modeling.ForecastingContext;
//import ca.gc.dfo.iwls.station.Station;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

//import javax.validation.constraints.NotNull;

//---
//---

//---
//import ca.gc.dfo.iwls.fmservice.modeling.tides.ITides;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.ITidesIO;

/**
 * Class for the computation of water level predictions
 */
public interface IWLStationPred  {

  long TIME_NOT_DEFINED= -1L;

  long MIN_TIME_INCR_SECONDS= 60L;
  long MAX_TIME_INCR_SECONDS= 3600L;
  long DEFAULT_TIME_INCR_SECONDS= 900L;
}
