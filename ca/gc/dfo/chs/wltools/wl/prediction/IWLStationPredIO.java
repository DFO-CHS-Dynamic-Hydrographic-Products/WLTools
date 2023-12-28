//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.prediction;

/**
 * Created by Gilles Mercier on 2018-01-10.
 */

import java.util.Set;
import java.util.List;

// ---
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
//import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPred;

//---
//import ca.gc.dfo.iwls.fmservice.modeling.tides.ITides;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.ITidesIO;

/**
 * Class for the computation of water level predictions
 */
public interface IWLStationPredIO  {

  // --- Moved to IWLToolsIO class
  //String JSON_FEXT= ".json";
  //String VALUE_JSON_KEY= "value";
  //String INSTANT_JSON_KEY= "eventDate";
  //String UNCERTAINTY_JSON_JEY= "uncertainty";
  //// ---
  //enum Format {
  //  CHS_JSON,
  //  DHP_S104_DCF8
  //  //CSV
  //}

  String [] allowedFormatsDef= {
    IWLToolsIO.Format.CHS_JSON.name(),
    IWLToolsIO.Format.DHP_S104_DCF8.name()
  }; //, OutputFormats.CSV.name() };

  Set<String> allowedFormats= Set.of(allowedFormatsDef);

  //abstract public List<MeasurementCustom> getAllPredictions();
  abstract public IWLStationPred getAllPredictions();

  abstract public List<MeasurementCustom> getPredictionData();
}
