//package ca.gc.dfo.iwls.fmservice.modeling.tides;
package ca.gc.dfo.chs.wltools.tidal.nonstationary.prediction;

/**
 * Created on 2018-01-19.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 * Modified on 2023-07-20, Gilles Mercier
 */

//---
import ca.gc.dfo.chs.wltools.nontidal.stage.Stage;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStageIO;
import ca.gc.dfo.chs.wltools.nontidal.stage.StageInputData;
import ca.gc.dfo.chs.wltools.nontidal.stage.StageCoefficient;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.Constituent1D;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.Constituent1DData;
import ca.gc.dfo.chs.wltools.tidal.stationary.prediction.Stationary1DTidalPredFactory;
//import ca.gc.dfo.chs.wltools.tidal.stationary.prediction.TidalPredictionsFactory;

//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent1D;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent1DData;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;

import java.util.Map;
import java.util.Set;
import java.util.List;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//---
//---
//---

/**
 * River-discharge and-or atmospheric influenced (a.k.a. non-stationary) tidal prediction object.
 * This implements the prediction-reconstruction part of the NS_TIDE theory
 *  (Matte et al., Journal of Atmospheric and Oceanic Technology, 2013)
 */
final public class NonStationary1DTidalPredFactory extends Stationary1DTidalPredFactory {
  
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * List of Map objects of tidal constituents information for the non-stationary(river discharge and-or atmospheric)
   * for a specific location coming from a file or a DB.
   */
  private Map<String, Map<String, Constituent1D>> tcDataMaps= null;

  /**
   * List of Constituent1DData objects which will be used by the non-stationary tidal prediction method.
   * MUST have the number of Constituent1DData objects in the constituent1DDataItems being the same as we
   * have Map items in one Map item of tcDataMaps list.
   */
  //private Map<String, Map<String,Constituent1DData>> constituent1DDataItems= null;
  private Map<String,Constituent1DData> constituent1DDataItems= null;

  /**
   * The stage equation (polynomial) object.
   */
  private Stage stagePart= null;

  ///**
  // * To store the stage input data (river discharges and-or atmos. data) with their related time stamps
  // */
  //private Map<String,List<MeasurementCustom>> staticStageInputData= null;
  //private List<List<Map<Long,MeasurementCustom>>> staticStageInputData= null;

  /**
   * Default constructor.
   */
  public NonStationary1DTidalPredFactory() {
    
    super();
    
    this.tcDataMaps= null;
    this.stagePart= null;
    //this.staticStageInputData= null;
    this.constituent1DDataItems= null;
  }
  
  /**
   * @param timeStampSeconds : A time-stamp in seconds since the epoch where we want a single tidal prediction.
   * @return The newly computed single tidal prediction in double precision.
   */
  @Override
  final public double computeTidalPrediction(final long timeStampSeconds) {

     // --- Compute the stationary part (NOTE: no Z0 average to use here, it is rather
     //     in the stage part (the CS0 coefficient, added at the end.)
     double retAcc= super.computeTidalPrediction(timeStampSeconds);

     final Map<String,StageInputData> stageInputDataMap= this.stagePart.getInputDataMap();

     final Map<String,StageCoefficient> stageCoefficientMap= this.stagePart.getCoeffcientsMap();

     // --- Add the non-stationary parts of the signal
     for (final String stInputDataId: stageInputDataMap.keySet()) {

        // --- Calculate the non-stationary tidal argument.
        final double nsTidalArg= super.astroInfosFactory
           .computeTidalPrediction(timeStampSeconds,this.constituent1DDataItems.get(stInputDataId));

        final StageInputData stageInputData= stageInputDataMap.get(stInputDataId);

        final StageCoefficient stageCoefficient= stageCoefficientMap.get(stInputDataId);

        // ---- Apply the stage calculation with the related stage part and the non-stationart tidal arg. added together
        //      (multiply  by the time varying stage input data,note the lag time subtraction.)
        retAcc += (stageCoefficient.getValue() + nsTidalArg) *
                    stageInputData.getAtTimeStamp(timeStampSeconds - stageCoefficient.getTimeLagSeconds());
     }

     // --- 
     return retAcc + stageCoefficientMap.get(IStageIO.STAGE_JSON_D0_KEY).getValue();
  }

  /**
   * Comments please!
   */
  final public NonStationary1DTidalPredFactory getNSJSONFileData(/*@NotNull*/ final String tcInputfilePath) {

    //--- Deal with possible null tcInputfilePath String:
    try {
      tcInputfilePath.length();

    } catch (NullPointerException e) {

      this.log.error("NonStationary1DTidalPredFactory getNSJSONFileData: tcInputfilePath==null!!");
      throw new RuntimeException("NonStationary1DTidalPredFactory getNSJSONFileData");
    }

    this.log.debug("NonStationary1DTidalPredFactory getNSJSONFileData: Start, tcInputfilePath=" + tcInputfilePath);

    //--- Get the TCF format ASCII lines in a List of Strings:
    //final List<String> jsonFileLines = ASCIIFileIO.getFileLinesAsArrayList(tcInputfilePath);
    //final JsonParser tcJsonInput= Json.createParser(new FileInputStream(tcInputfilePath));

    try {
        //final FileInputStream tcInputfilePathRdr= new FileInputStream(tcInputfilePath);
       final JsonArray jsonTcDataInputArray= Json.createReader(new FileInputStream(tcInputfilePath)).readArray();

    } catch (FileNotFoundException e) {

      this.log.error("NonStationary1DTidalPredFactory getNSJSONFileData: tcInputfilePath"+tcInputfilePath+" not found !!");
      throw new RuntimeException("NonStationary1DTidalPredFactory getNSJSONFileData");
    }

    //this.log.debug("NonStationary1DTidalPredFactory getNSJSONFileData: done with Json.createParser");
    this.log.debug("NonStationary1DTidalPredFactory getNSJSONFileData: System.exit(0)");
    System.exit(0);

    return this;
  }

  /**
   * @param method           : Tidal prediction method to use.
   * @param latitudeRadians  : Latitude of the 2D point where we want 1D tidal predictions
   * @param startTimeSeconds : Time-stamp in seconds since the epoch for the time reference used for astronomic
   *                         arguments computations.
   * @param constNames       : A Set of tidal constituents names to use for the tidal predictions.
   * @return The current  object.
   */
  @Override
  final public NonStationary1DTidalPredFactory setAstroInfos(final Method method,
                                                             final double latitudeRadians,
                                                             final long startTimeSeconds,
                                                             /*@NotNull @Size(min = 1)*/ final Set constNames) {
    try {
      constNames.size();
      
    } catch (NullPointerException e) {
      
      this.log.error("NonStationary1DTidalPredFactory setAstroInfos: constNames==null !!");
      throw new RuntimeException(e);
    }
    
    this.log.debug("NonStationary1DTidalPredFactory setAstroInfos: setAstroInfos : start");

    // --- Compute the stationary astronomic information and set the stationary
    //     tidal constituents amplitudes and phases.
    super.setAstroInfos(method, latitudeRadians, startTimeSeconds, constNames);
    
    //int dimCount= 0;

    // --- Set the non-stationary tidal constituents amplitudes and phases.
    //for (Constituent1DData c1DD: this.constituent1DDataItems) {
    for (final String tcMapId: this.constituent1DDataItems.keySet()) {

        this.constituent1DDataItems.put(tcMapId,
           new Constituent1DData(this.tcDataMaps.get(tcMapId),this.astroInfosFactory));
    }

    this.log.debug("NonStationary1DTidalPredFactory setAstroInfos: end");
    
    return this;
  }
}
