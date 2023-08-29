//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.prediction;

/**
 * Created by Gilles Mercier on 2018-01-10.
 */

import java.util.Set;
import java.util.List;

// ---
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
//import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPred;

//---
//import ca.gc.dfo.iwls.fmservice.modeling.tides.ITides;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.ITidesIO;

/**
 * Class for the computation of water level predictions
 */
public interface IWLStationPredIO  {

  String JSON_FEXT= ".json";

  enum OutputFormats {
    JSON //,
    //CSV
  }

  String [] allowedOutputFormatsDef= { OutputFormats.JSON.name() }; //, OutputFormats.CSV.name() };

  Set<String> allowedOutputFormats= Set.of(allowedOutputFormatsDef);

  //abstract public List<MeasurementCustom> getAllPredictions();
  abstract public IWLStationPred getAllPredictions();
  abstract public List<MeasurementCustom> getPredictionData();
}
