package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.HashMap;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

// ---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

// ---
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.wl.WLLocation;
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.ASCIIFileIO;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.wl.TideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentFMF;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;

/**
 * Comments please!
 */
abstract public class WLAdjustmentType
  extends WLAdjustmentFMF implements IWLAdjustmentType {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentType";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  //private IWLAdjustment.Type adjType= null;

  // --- Default IWLAdjustment.TideGaugeAdjMethod is IWLAdjustment.TideGaugeAdjMethod.CHS_IWLS_QC
  //     for the prediction (WLP) data.
  // protected IWLAdjustment.TideGaugeAdjMethod
  //    predictAdjType= IWLAdjustment.TideGaugeAdjMethod.CHS_IWLS_QC;

  // --- Default IWLAdjustment.TideGaugeAdjMethod is IWLAdjustment.TideGaugeAdjMethod.ECCC_H2D2_FORECAST_AUTOREG
  //     for the forecast (WLF) data.
  //protected IWLAdjustment.TideGaugeAdjMethod forecastAdjType=
  //  IWLAdjustment.TideGaugeAdjMethod.SINGLE_TIMEDEP_FMF_ERROR_STATS; //MULT_TIMEDEP_FMF_ERROR_STATS;

  protected IWLAdjustment.ForecastAdjMethod forecastAdjMethod=
    IWLAdjustment.ForecastAdjMethod.TGS_SINGLE_TIMEDEP_FMF_ERROR_STATS; //MULT_TIMEDEP_FMF_ERROR_STATS;    

  protected IWLAdjustment.SpinePPWriteCtrl spinePPWriteCtrl= IWLAdjustment.SpinePPWriteCtrl.BOTH_SIDES;

  /**
   * Comments please!
   */
  public WLAdjustmentType() {

    super();

    //this.wlOriginalData=
    //  this.wlAdjustedData= null;
  }

  /**
   * Parse the main program arguments using a constructor.
   */
  public WLAdjustmentType(/*NotNull*/ final WLAdjustment.Type adjType,
                          /*NotNull*/ final HashMap<String,String> argsMap) {

    super(adjType, argsMap);

    final String mmi= "WLAdjustmentType main constructor: ";

    slog.info(mmi+"start, this.adjType="+this.adjType);

    if (!this.argsMapKeySet.contains("--locationIdInfo")) {
      throw new RuntimeException(mmi+"Must have the mandatory option: --locationIdInfo defined !!");
    }

    // --- 
    this.locationIdInfo= argsMap.get("--locationIdInfo");

    slog.info(mmi+"this.locationIdInfo="+this.locationIdInfo);
    
    // --- Get only the base name of the this.locationIdInfo option value if it is a
    //     path to a file having the IWLToolsIO.JSON_FEXT file name extension.
    //     TODO: Verify if this is still needed.
    final String identityInfo=
      new File(this.locationIdInfo).getName().replace(IWLToolsIO.JSON_FEXT,"");

    slog.info(mmi+"identityInfo="+identityInfo);

    // --- Process the identity String depending on the adjustment type.
    //     TODO: Could be done in the specific dervived classes constructors?
    if (this.adjType == IWLAdjustment.Type.TideGauge) {

      slog.info(mmi+"Using "+ IWLAdjustment.Type.TideGauge.name()+" adjustment type");
	
      this.location= new TideGaugeConfig(identityInfo);

    } else if (this.adjType == IWLAdjustment.Type.SpineIPP) {

      slog.info(mmi+"Using "+ IWLAdjustment.Type.SpineIPP.name()+" adjustment type");	
	
      // --- Get the ids of the two CHS tide gauges that define the spatial interpolation range for the ship
      //     channel point locations that are in-between those two TG locations.
      final String [] spineInterpTGIdsInfo= identityInfo.split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

      if (spineInterpTGIdsInfo.length != 3) {
	throw new RuntimeException(mmi+"spineInterpTGIdsInfo string array must have length of 3 here!");
      }
      
      // --- Only processing two tide gauges at a time for SpineIPP for now because
      //     it runs under an ECCC maestro LOOP parallelization instance
      this.locations= new ArrayList<TideGaugeConfig>(2);

      // --- Allocate the two TideGaugeConfig objects for the two tide gauge locations 
      this.locations.add(0, new TideGaugeConfig(spineInterpTGIdsInfo[0]));
      this.locations.add(1, new TideGaugeConfig(spineInterpTGIdsInfo[1]));

      slog.info(mmi+"this.locations TG 0 id.="+this.locations.get(0).getIdentity());
      slog.info(mmi+"this.locations TG 1 id.="+this.locations.get(1).getIdentity());

      // --- Get the flag that controls which tide gauge(s) results have to be
      //     written for the outputs (Need to avoid writing the same results twice on disk)
      final String checkSpinePPWriteCtrlType= spineInterpTGIdsInfo[2];

      // --- Check if this flag is valid.
      if (!this.allowedSpinePPWriteCtrl.contains(checkSpinePPWriteCtrlType)) {
	throw new RuntimeException(mmi+"Invalid IWLAdjustment.SpinePPWriteCtrl type -> "+checkSpinePPWriteCtrlType);
      }
      
      // --- Set this.spinePPWriteCtrl enum value using the spineInterpTGIdsInfo[2] string
      this.spinePPWriteCtrl= IWLAdjustment.SpinePPWriteCtrl.valueOf(spineInterpTGIdsInfo[2]);

      slog.info(mmi+"this.spinePPWriteCtrl="+this.spinePPWriteCtrl.name());
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
      
    } else if (this.adjType == IWLAdjustment.Type.SpineFPP) {

      slog.info(mmi+"Using "+ IWLAdjustment.Type.SpineFPP.name()+" adjustment type");

      // --- Get all the ids of the CHS tide gauges that define the spatial interpolation range(s) for the ship
      //     channel point locations that are in-between those TG locations.
      final String [] spineInterpTGIdsInfo= identityInfo.split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

      if (spineInterpTGIdsInfo.length < 2) {
	throw new RuntimeException(mmi+"spineInterpTGIdsInfo String array must have at least two items here!");
      }

      slog.info(mmi+"spineInterpTGIdsInfo.length="+spineInterpTGIdsInfo.length);

      // --- Allocate all the TideGaugeConfig objects for all the tide gauge locations defined in
      //     the spineInterpTGIdsInfo String array
      this.locations= new ArrayList<TideGaugeConfig>(spineInterpTGIdsInfo.length);

      // ---
      for (int tgIter= 0; tgIter < spineInterpTGIdsInfo.length; tgIter++ ) {

	slog.info(mmi+"Instantiating TideGaugeConfig object for tide gauge -> "+spineInterpTGIdsInfo[tgIter]);
	  
	this.locations.add(tgIter, new TideGaugeConfig(spineInterpTGIdsInfo[tgIter]));
      }

      //slog.info(mmi+"this.locations.size()="+this.locations.size());
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
      
    } else {
       throw new RuntimeException(mmi+"Invalid adjustment type "+this.adjType.name()+" !!");
    }

    // --- NOTE: --modelInputDataDef=<path> <path> could be the path of an ASCII file that contains
    //           all the needed model input data itself (eg. H2D2 WL probes forecast data) OR the
    //           path of an ASCII file that defines all the paths to the model WL forecast input data
    //           files that are needed (e.g. H2D2 NetCDF file).
    if (this.argsMapKeySet.contains("--modelForecastInputDataInfo")) {

      final String [] modelForecastInputDataInfo= argsMap
        .get("--modelForecastInputDataInfo").split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

      if (!allowedInputFormats.contains(modelForecastInputDataInfo[0])) {

        throw new RuntimeException(mmi+"Invalid model forecast Input Data file format -> "+
                                 modelForecastInputDataInfo[0]+" Must be one of -> "+allowedInputFormats.toString());
      }

      this.modelForecastInputDataFormat= IWLAdjustmentIO
        .DataTypesFormatsDef.valueOf(modelForecastInputDataInfo[0]);

      // --- modelInputDataInfo[1] could be: only one model forecast input data file OR an
      //     ASCII file that defines a list of at least two model forecast input data files
      this.modelForecastInputDataInfo= modelForecastInputDataInfo[1];

      slog.info(mmi+"this.modelForecastInputDataFormat="+this.modelForecastInputDataFormat.name());
      slog.info(mmi+"this.modelForecastInputDataInfo="+this.modelForecastInputDataInfo);
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
      
    } // --- if (this.argsMapKeySet.contains("--modelForecastInputDataInfo") block

    slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
  }

  // ---
  final public WLLocation getLocation() {
    return this.location;
  }

  // ---
  final public String getLocationIdentity() {
    return this.location.getIdentity();
  }

  // ---
  final public String getFullForecastModelName() {
    return this.fullForecastModelName;
  }

  // ---
  final public List<MeasurementCustom> getLocationPredData() {
    return (List<MeasurementCustom>) this.locationPredData;
  }

  // ---
  final public List<MeasurementCustom> getNearestObsData() {
    return (List<MeasurementCustom>) this.nearestObsData.get(this.location.getIdentity());
  }

  // --- 
  final public List<MeasurementCustom> getNearestObsData(final String locationId) {
    return (List<MeasurementCustom>) this.nearestObsData.get(locationId);
  }

  // ---
  final public List<MeasurementCustom> getNearestModelData(final int whichType) {
    return (List<MeasurementCustom>) this.nearestModelData.get(whichType).get(this.location.getIdentity());
  }

  // --- 
  final public List<MeasurementCustom> getNearestModelData(final int whichType, final String locationId) {
    return (List<MeasurementCustom>) this.nearestModelData.get(whichType).get(locationId);
  }

  // ---
  final public String getFMFLeadTimeECCCOperStr() {
    return this.getFMFLeadTimeInstantECCCOperStr();
  }
   
  // ---
  final public WLAdjustmentType adjustFullModelForecast(final HashMap<String,String> argsMap, final String prevFMFInputDataFilePath,  //final String prevFMFASCIIDataFilePath,
                                                        final Map<String, HBCoords> uniqueTGMapObj, final JsonObject mainJsonMapObj ) {
    final String mmi= "adjustFullModelForecast: ";

    slog.info(mmi+"start: this.modelForecastInputDataFormat.name()"+this.modelForecastInputDataFormat.name()); 
    slog.info(mmi+"prevFMFInputDataFilePath="+prevFMFInputDataFilePath);
    slog.info(mmi+"Debug exit 0");
    System.exit(0);

    try {
      prevFMFInputDataFilePath.length();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    try {
      uniqueTGMapObj.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe);
    }

    try {
      mainJsonMapObj.size();
    } catch (NullPointerException e) {
       //slog.error(mmi+"mainJsonMapObj is null !!");
      throw new RuntimeException(mmi+e);
    }

    // ---
    switch (this.forecastAdjMethod) {

      // ---
      case TGS_SINGLE_TIMEDEP_FMF_ERROR_STATS:

        if (!this.haveWLOData) {
	  throw new RuntimeException(mmi+"this.haveWLOData cannot be false for the "+this.forecastAdjMethod.name()+" FMF adjustment type !");
        }

	if (this.modelForecastInputDataFormat==
	      IWLAdjustmentIO.DataTypesFormatsDef.S104DCF2) {
	    
          throw new RuntimeException(mmi+"Cannot use the "+this.forecastAdjMethod.name()+
				     " forecast adjustment method combined with the "+IWLAdjustmentIO.DataTypesFormatsDef.S104DCF2+" model input data format type !");
	}
	  
        this.singleTimeDepFMFErrorStatsAdj(prevFMFInputDataFilePath,
                                           uniqueTGMapObj, mainJsonMapObj);

        //slog.info(mmi+"Done with "+this.forecastAdjMethod.name()+" calculations");

        break;

      // ---
      case TGS_MULT_TIMEDEP_FMF_ERROR_STATS:
 
	  //if (!argsMap.containsKey("--tgResidualsStatsIODirectory")) {
	if (!argsMap.containsKey("--modelWLResidualsAtTGStatsIODir")) {
          throw new RuntimeException(mmi+
            "Must have the --modelWLResidualsAtTGStatsIODir=<IO main folder for the FMF residuals stats at tide gauges> defined in the argsMap!!");
        }

        //final String tgResidualsStatsIODirectory= argsMap.get("--tgResidualsStatsIODirectory");
	final String modelWLResidualsAtTGStatsIODir= argsMap.get("--modelWLResidualsAtTGStatsIODir");

        this.multTimeDepFMFErrorStatsAdj(prevFMFInputDataFilePath,
                                         uniqueTGMapObj, mainJsonMapObj,
					 modelWLResidualsAtTGStatsIODir);
	                                 //tgResidualsStatsIODirectory);

        //slog.info(mmi+"Done with MULT_TIMEDEP_FMF_ERROR_STATS");
        //slog.info(mmi+"Debug exit 0");
        //System.exit(0);

        break;

      default:
        throw new RuntimeException(mmi+"Invalid this.forecastAdjMethod -> "+this.forecastAdjMethod.name());
    }

    slog.info(mmi+"Done with "+this.forecastAdjMethod.name()+" calculations");

    slog.info(mmi+"end");
    slog.info(mmi+"Debug exit 0");
    System.exit(0);

    return this;
  } // --- adjustFullModelForecast method
}

