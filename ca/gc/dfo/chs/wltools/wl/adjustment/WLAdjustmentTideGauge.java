package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.lang.Math;
import java.lang.Enum;
import java.util.Arrays;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;
import java.util.ArrayList;
import java.nio.file.Files;
import java.util.Collections;
import java.util.NavigableSet;
import java.nio.file.DirectoryStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

// ---
import java.time.Instant;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

// ---
import ca.gc.dfo.chs.wltools.wl.IWL;
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.wl.IWLLocation;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.util.ITimeMachine;
import ca.gc.dfo.chs.wltools.wl.ITideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
import ca.gc.dfo.chs.wltools.numbercrunching.INumberCrunching;
import ca.gc.dfo.chs.wltools.tidal.nonstationary.INonStationaryIO;

/**
 * Comments please!
 */
final public class WLAdjustmentTideGauge extends WLAdjustmentType {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.adjustment.WLAdjustmentTideGauge";

 /**
   * Usual class static log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  private Instant referenceTime= null; //Instant(Clock.systemUTC());

  //private List<MeasurementCustom> tgLocationWLOData= null;
  //private ArrayList<MeasurementCustom> tgLocationWLPData= null;
  //private List<MeasurementCustom> tgLocationWLFData= null;

  private Double wloQCThresholdAbsVal= null;

  /**
   * Comments please!
   */
  public WLAdjustmentTideGauge() {
    super();
  }

  /**
   * Comments please!
   */
  public WLAdjustmentTideGauge(/*@NotNull*/ final HashMap<String,String> argsMap) {

    super(IWLAdjustment.Type.TideGauge, argsMap);

    final String mmi= "WLAdjustmentTideGauge main constructor: ";

    slog.info(mmi+"start: this.locationIdInfo="+this.locationIdInfo); //wdsLocationIdInfoFile="+wdsLocationIdInfoFile);

    final Set<String> argsMapKeysSet =argsMap.keySet();

    if (!argsMapKeysSet.contains("--wloQCThresholdAbsVal")) {
      throw new RuntimeException(mmi+
        "Must have the --wloQCThresholdAbsVal=<min. number of WL obs> defined in argsMap");
    }

    this.wloQCThresholdAbsVal= Double.parseDouble(argsMap.get("--wloQCThresholdAbsVal"));

    slog.info(mmi+"this.wloQCThresholdAbsVal="+this.wloQCThresholdAbsVal);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    if (!argsMapKeysSet.contains("--minNumberOfObs")) {
      throw new RuntimeException(mmi+
        "Must have the --minNumberOfObs=<min. number of WL obs> defined in argsMap");
    }

    this.minNumberOfObs= Integer.parseInt(argsMap.get("--minNumberOfObs"));

    slog.info(mmi+"this.minNumberOfObs="+this.minNumberOfObs);

    if (!argsMapKeysSet.contains("--referenceTimeISOFormat")) {
      throw new RuntimeException(mmi+
        "Must have the --referenceTimeISOFormat=<ISO format time string> defined in argsMap");
    }

    this.referenceTime= Instant.parse(argsMap.get("--referenceTimeISOFormat"));

    slog.info(mmi+"this.referenceTime="+this.referenceTime.toString());
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    if (!argsMapKeysSet.contains("--tideGaugeLocationsInfoFileName")) {
      throw new RuntimeException(mmi+
        "Must have the --tideGaugeLocationsInfoFileName=<tide gauges definition file name> defined in the argsMap");
    }

    final String tideGaugeLocationsInfoFileName= argsMap.get("--tideGaugeLocationsInfoFileName");

    slog.info(mmi+"tideGaugeLocationsInfoFileName="+tideGaugeLocationsInfoFileName);

    if (!argsMapKeysSet.contains("--tideGaugePredictInputDataInfo")) {
      throw new RuntimeException(mmi+
         "Must have the --tideGaugePredictInputDataInfo=<file format>:<complete path to the tide gauge WLP input data file> defined in argsMap");
    }

    final String [] tideGaugePredictInputDataInfo= argsMap.
       get("--tideGaugePredictInputDataInfo").split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR) ;

    if (!IWLStationPredIO.allowedFormats.contains(tideGaugePredictInputDataInfo[0])) {

      throw new RuntimeException(mmi+"Invalid tideGaugePredictInputData file format -> "+
        tideGaugePredictInputDataInfo[0]+" Must be one of -> "+IWLStationPredIO.allowedFormats.toString());
    }

    this.predictInputDataFormat= IWLToolsIO.
      Format.valueOf(tideGaugePredictInputDataInfo[0]);

    final String tideGaugePredictInputDataFile= tideGaugePredictInputDataInfo[1];  ;//argsMap.get("--tideGaugePredictInputDataFile");

    if (!argsMapKeysSet.contains("--tideGaugeWLODataInfo")) {
      throw new RuntimeException(mmi+
         "Must have the --tideGaugeWLODataInfo=<TG WLO file format>:<complete path to the tide gauge input WLO data file> defined in the argsMap");
    }

    final String checkTGWLODataInfo= argsMap.get("--tideGaugeWLODataInfo");

    // --- Check if we have WL obs to use, checkTideGaugeWLODataInfo is set to
    //     ARG_NOT_DEFINED Sting if there are no WLO data to use for tje tide gauge(s)
    //     TODO: Move this block after the WL prediction data read block.
    //if (!checkTGWLODataInfo.equals(IWLAdjustmentIO.ARG_NOT_DEFINED)) {

    //--- Do some preliminary checks for the WLO data before reading it (if any) later.
    final String [] tideGaugeWLODataInfo=
      checkTGWLODataInfo.split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

    //if (!IWLStationPredIO.allowedFormats.contains(tideGaugeWLODataInfo[0])) {
    if (!IWLToolsIO.allowedFormats.contains(tideGaugeWLODataInfo[0])) {

      throw new RuntimeException(mmi+"Invalid TG WLO Input Data file format -> "+
	tideGaugeWLODataInfo[0]+" Must be one of -> "+IWLToolsIO.allowedFormats.toString());			 
      //tideGaugeWLODataInfo[0]+" Must be one of -> "+IWLStationPredIO.allowedFormats.toString());
    }

    // --- Define the WLO input data file format:
    this.obsInputDataFormat=
      IWLToolsIO.Format.valueOf(tideGaugeWLODataInfo[0]);

    // --- Define the path of the WLO data file for the TG.
    this.tideGaugeWLODataFile= tideGaugeWLODataInfo[1]; ;//argsMap.get("--tideGaugeWLODataFile");

    slog.info(mmi+"tideGaugeWLODataFile="+tideGaugeWLODataFile);

    // --- Verify that we have the same name id. for the TG between the file name and
    //     the this.locationIdInfo attrbute.
    final String [] tideGaugeWLODataFilePathSplit= tideGaugeWLODataFile.split(File.separator);

    //--- Extract the 1st part of the WLO data file which MUST be the same string id. as for the
    //    this.locationIdInfo attribute. We can also have to get rid of the IWLToolsIO.JSON_FEXT
    //    file name extension (if any) with the replaceAll(IWLToolsIO.JSON_FEXT) call.
    final String tideGaugeNameIdFromFileName=
      tideGaugeWLODataFilePathSplit[ tideGaugeWLODataFilePathSplit.length-1 ]
	.split(IWLToolsIO.OUTPUT_DATA_FMT_SPLIT_CHAR)[0].replaceAll(IWLToolsIO.JSON_FEXT,"");
    
    slog.info(mmi+"tideGaugeNameIdFromFileName="+tideGaugeNameIdFromFileName);

    if (!tideGaugeNameIdFromFileName.equals(this.locationIdInfo)) {
      throw new RuntimeException(mmi+"tideGaugeNameIdFromFileName="+tideGaugeNameIdFromFileName+
                                 " is NOT the same tg station id. as this.locationIdInfo="+this.locationIdInfo);
    }

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);    

    slog.info(mmi+"tideGaugePredictInputDataFile="+tideGaugePredictInputDataFile);
    slog.info(mmi+"this.modelForecastInputDataInfo="+this.modelForecastInputDataInfo);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    if (argsMapKeysSet.contains("--forecastAdjMethod")) {

      final String checkForecastAdjMethod= argsMap.get("--forecastAdjMethod");

      if (!IWLAdjustment.allowedForecastAdjMethods.contains(checkForecastAdjMethod)) {
        throw new RuntimeException(mmi+"Invalid forecast WL adjustment method -> "+checkForecastAdjMethod+
				   " Must be one of -> "+IWLAdjustment.allowedForecastAdjMethods.toString() );
      }

      this.forecastAdjMethod= IWLAdjustment.ForecastAdjMethod.valueOf(checkForecastAdjMethod);
    }

    //if (this.predictAdjType != IWLAdjustment.TideGaugeAdjMethod.CHS_IWLS_QC) {
    //  slog.info(mmi+"Only the tide gauge WL prediction adjustment type -> "+
    //           IWLAdjustment.TideGaugeAdjMethod.CHS_IWLS_QC.name()+" is allowed for now !");
    //}

    //slog.info(mmi+"this.predictAdjType="+this.predictAdjType.name());
    slog.info(mmi+"this.forecastAdjMethod="+this.forecastAdjMethod.name());
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Get the complete path of the file that contains the infi for
    //     all the tide gauges locations.
    final String tideGaugeLocationsInfoFile= WLToolsIO
      .getTideGaugeInfoFilePath(tideGaugeLocationsInfoFileName);

    //WLToolsIO.getMainCfgDir() + File.separator +
    //ITideGaugeConfig.INFO_FOLDER_NAME + File.separator + tideGaugeLocationsDefFileName ;

    slog.info(mmi+"tideGaugeLocationsInfoFile="+tideGaugeLocationsInfoFile);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    FileInputStream jsonFileInputStream= null;

    try {
      jsonFileInputStream= new FileInputStream(tideGaugeLocationsInfoFile);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(mmi+e);
    }

    final JsonObject mainJsonMapObj= Json
      .createReader(jsonFileInputStream).readObject();

    final Set<String> tgStrNumIdKeysSet= mainJsonMapObj.keySet();

    if (!tgStrNumIdKeysSet.contains(this.locationIdInfo)) {
      throw new RuntimeException(mmi+"Invalid tide gauge id -> "+this.locationIdInfo+
                                " !! Must be one of ->"+tgStrNumIdKeysSet.toString());
    }

    slog.info(mmi+"tgStrNumIdKeysSet.toString()="+tgStrNumIdKeysSet.toString());
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Set this.location (WLLocation) object with the tide gauge
    //     Json formatted config.
    this.location.setConfig(mainJsonMapObj.getJsonObject(this.locationIdInfo));

    // --- Get the tide gauge ZC vs a global vertical datum conversion
    //     but just if we need to do a conversion.
    if ( this.location.getDoZCConvToVertDatum() )  {
      this.adjLocationZCVsVDatum= this.location.getZcVsVertDatum();
    }

    slog.info(mmi+"this.location.getIdentity()="+this.location.getIdentity()+
                  ", this.adjLocationZCVsVDatum="+this.adjLocationZCVsVDatum);

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- We can close the tide gauges info Json file now
    try {
      jsonFileInputStream.close();
    } catch (IOException e) {
      throw new RuntimeException(mmi+e);
    }

    slog.info(mmi+"Reading the prediction data using "+this.predictInputDataFormat.name());

    if (this.predictInputDataFormat == IWLToolsIO.Format.CHS_JSON ) {

      // --- Here we do not need to check if the time stamps of the predictions are
      //     consistent with what we want hence the -1L for the 2nd arg. for the
      //     WLAdjustmentIO.getWLDataInCHSJsonFmt() method. We also assume that the WL
      //     predictions values are already referred to a global or regional vertical
      //     datum (and not the local CHS ZC) hence the 0.0 value for the 3rd argument
      //     of WLAdjustmentIO.getWLDataInCHSJsonFmt() method.
      //this.tgLocationWLPData=
      this.locationPredData= WLAdjustmentIO
	.getWLDataInCHSJsonFmt(tideGaugePredictInputDataFile,-1L, 0.0, null);

    } else {
      throw new RuntimeException(mmi+"Invalid prediction input data format -> "+this.predictInputDataFormat.name());
    }

    //double predVal0= this.locationPredData.get(0).getValue();
    //double predVal1= this.locationPredData.get(1).getValue();
    //slog.info(mmi+"predVal0="+predVal0);
    //slog.info(mmi+"predVal1="+predVal1);
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    // --- Need to get the WL predictions data time intervall increment here.
    this.prdDataTimeIntervalSeconds= MeasurementCustom
      .getDataTimeIntervallSeconds(this.locationPredData);

    slog.info(mmi+"Done with reading prediction input data from file -> "+tideGaugePredictInputDataFile);
    slog.info(mmi+"this.locationPredData.size()="+this.locationPredData.size());
    slog.info(mmi+"this.locationPredData time increment intervall="+this.prdDataTimeIntervalSeconds);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    this.nearestObsData= new HashMap<String,List<MeasurementCustom>>();
    
    //---
    slog.info(mmi+"Reading the TG obs (WLO) at location -> "+
              this.location.getIdentity()+" data using "+this.obsInputDataFormat.name()+ " format");

    // --- Read-get the WLO data (if any)
    this.getTGObsData(this.wloQCThresholdAbsVal);

    slog.info(mmi+"Done with this.getTGObsData(this.wloQCThresholdAbsVal)");
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);    

    // --- Do some checks on the WLO data (if any).
    if (this.haveWLOData) {	

      try {
	this.nearestObsData.get(this.location.getIdentity()).size();
	
      } catch (NullPointerException npe) {
	throw new RuntimeException(mmi+"ERROR: this.nearestObsData.get(this.location.getIdentity()) cannot be null if this.haveWLOData is true");
      }

      final int checkNumberOfObs= this.nearestObsData.get(this.location.getIdentity()).size();
      
      if (checkNumberOfObs < this.minNumberOfObs) {
	  //throw new RuntimeException(mmi+"ERROR: We must have checkNumberOfObs -> "+
          //                       checkNumberOfObs+" >= this.minNumberOfObs -> "+this.minNumberOfObs);
 
	slog.warn(mmi+"Insuficient nb. of WLO data -> "+checkNumberOfObs+" will not use it !!");
	  
	this.haveWLOData= false;
	  
      } else {
	  
        slog.info(mmi+"Okay we have a sufficient number of obs WLs -> "+
                 checkNumberOfObs+" to use for predicition and forecast adjustments for TG -> "+this.location.getIdentity());     
      }
      
    } else {
      slog.info(mmi+"this.haveWLOData == false !!, no WLO data available for TG -> "+this.location.getIdentity());
    }

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
      
    // --- Now proceed with the FMF adjustment:
    if (this.forecastAdjMethod != null) {

      //if (this.forecastAdjType != IWLAdjustment.
      //      TideGaugeAdjMethod.SINGLE_TIMEDEP_FMF_ERROR_STATS) {
      //  slog.info(mmi+"Only the tide gauge WL forecast adjustment type -> "+
      //            IWLAdjustment.TideGaugeAdjMethod.SINGLE_TIMEDEP_FMF_ERROR_STATS.name()+" is allowed for now !");
      //}

      this.fullForecastModelName= argsMapKeysSet
        .contains("--fullForecastModelName") ? argsMap.get("--fullForecastModelName") : IWLAdjustment.DEFAULT_MODEL_NAME;

      if (this.modelForecastInputDataInfo == null) {
        throw new RuntimeException(mmi+
                  "this.modelForecastInputDataInfo attribute cannot be null at this point if this.forecastAdjType is not null !");
      }

      slog.info(mmi+"this.modelForecastInputDataInfo="+this.modelForecastInputDataInfo);

      // --- Need the tide gauge CHS Id. for the getH2D2ASCIIWLFProbesData
      //     method call.
      final Map<String, HBCoords> uniqueTGMapObj= new HashMap<String, HBCoords>();

      // --- Just define this.location for the uniqueTGMapObj Map   
      uniqueTGMapObj.put(this.location.getIdentity(), this.location);

      // --- Define the nb. hours in past to use depending on this.forecastAdjMethod:
      //     0 means that it will be automagically be determined according to the FMF
      //     data duration after its lead time
      final long nbHoursInPastArg= (this.forecastAdjMethod
	.equals(ForecastAdjMethod.TGS_SINGLE_TIMEDEP_FMF_ERROR_STATS)) ? 0L : IWLAdjustment.SYNOP_RUNS_TIME_OFFSET_HOUR;
	//(this.forecastAdjType==TideGaugeAdjMethod.SINGLE_TIMEDEP_FMF_ERROR_STATS) ? 0L : IWLAdjustment.SYNOP_RUNS_TIME_OFFSET_HOUR;

      slog.info(mmi+"nbHoursInPastArg="+nbHoursInPastArg);

      //// --- No FMF WL data conversion from a ZC by default.
      double fmfFromZCConvVal= 0.0;
      
      // --- String to hold the path of the previous FMF input data file
      //     (could be ASCII OR S104DCF2 format)
      String prevFMFInputDataFilePath= null;

      // --- OHPS-SLFE ASCII (CSV in fact) WL input data format (WL data at tide gauges only)
      if (this.modelForecastInputDataFormat==
	    IWLAdjustmentIO.DataTypesFormatsDef.ECCC_OHPS_ASCII) {
	  
        // --- Here this.modelForecastInputDataInfo attribute is the complete path to
        //     an ECCC_H2D2 probes (at the CHS TGs locations in fact) file of the ECCC_H2D2_ASCII
        //     format. It should be the OHPS model forecast data of the actual synoptic run as
        //     it is specified with the IWLAdjustmentIO.FullModelForecastType.ACTUAL.ordinal()
        //     argument to this method.
	//
	//     ***IMPORTANT***: Here the OHPS WL input data MUST already be defined with respect
	//     to the same global datum used to convert the WLO data from its ZC.
        prevFMFInputDataFilePath= this.getH2D2ASCIIWLFProbesData(this.modelForecastInputDataInfo,
                                                                 uniqueTGMapObj, mainJsonMapObj, nbHoursInPastArg,
                                                                 IWLAdjustmentIO.FullModelForecastType.ACTUAL.ordinal()); // , this.nearestModelData);

        slog.info(mmi+"Done with reading the model full forecast (FMF) WL ASCII input data at tide gauge -> "+
		  this.location.getIdentity()+" for OHPS-SLFE and for the actual synop run");

        //slog.info(mmi+"this.nearestModelData.size()="+this.nearestModelData.size());
        //slog.info(mmi+"this.nearestModelData.get(IWLAdjustmentIO.FullModelForecastType.ACTUAL).get(this.location.getIdentity()).get(0).getValue()="+
        //              this.nearestModelData.get(IWLAdjustmentIO.FullModelForecastType.ACTUAL.ordinal()).get(this.location.getIdentity()).get(0).getValue());

        //slog.info(mmi+"previousFMFASCIIDataFilePath="+previousFMFASCIIDataFilePath);

	//slog.info(mmi+"Now doing full model forecast (FMF) adjustment-correction using previous forecast(s) ASCII format input data: prevFMFASCIIDataFilePath="+prevFMFASCIIDataFilePath);
        //this.adjustFullModelForecast(argsMap, prevFMFASCIIDataFilePath, uniqueTGMapObj, mainJsonMapObj);
	//slog.info(mmi+"Debug System.exit(0)");
        //System.exit(0);	

      } else if (this.modelForecastInputDataFormat==
		   IWLAdjustmentIO.DataTypesFormatsDef.S104DCF2) {

	slog.info(mmi+"Using model FMF input data format -> "+ IWLAdjustmentIO.DataTypesFormatsDef.S104DCF2.name());

	// --- Need to use the this.referenceTime for this.fmfLeadTimeInstant here.
	this.fmfLeadTimeInstant= this.referenceTime;

	slog.info(mmi+"this.fmfLeadTimeInstant="+this.fmfLeadTimeInstant.toString());

	// --- Define the fmfFromZCConvVal using the TG location
	fmfFromZCConvVal= this.location.getZcVsVertDatum();

	if (argsMapKeysSet.contains("--fmfFromZCConvDataInfo")) {

	  slog.info(mmi+"Need to determine the conversion from ZC to a global datum for the TG location from a 2D input file for the FMF input data");

	  slog.info(mmi+"Code not implemeted yet!!");
          slog.info(mmi+"Debug System.exit(0)");
          System.exit(0);
	  
	} else {

	   // --- The conversion from ZC to a global datum of the TG location can only be used
	   //     for he FMF input data of the ECCC_OHPS_SLFE model
	   if (!this.fullForecastModelName.equals(IWLAdjustment.FMFNames.ECCC_OHPS_SLFE.name())) {
	       throw new RuntimeException(mmi+"Cannot use the TG location ZC to a global datum for the "+this.fullForecastModelName+" FMF input data !!");
	   }

	  slog.warn(mmi+"Directly using the conversion from ZC to a global datum of the TG location for the "+
		    IWLAdjustment.FMFNames.ECCC_OHPS_SLFE.name()+" FMF input data");
	}

	slog.info(mmi+"Will use fmfFromZCConvVal="+fmfFromZCConvVal);
	//slog.info(mmi+"Debug System.exit(0)");
        //System.exit(0);	

	prevFMFInputDataFilePath= this.getS104DCF2InputData(this.modelForecastInputDataInfo, fmfFromZCConvVal,
                                                            uniqueTGMapObj, mainJsonMapObj, nbHoursInPastArg,
                                                            IWLAdjustmentIO.FullModelForecastType.ACTUAL.ordinal());

        slog.info(mmi+"Done with reading the model full forecast (FMF) WL input data at tide gauge -> "+
		  this.location.getIdentity()+" for the actual synop run");

	slog.info(mmi+"Debug System.exit(0)");
        System.exit(0);
	
      } else {
        throw new RuntimeException(mmi+"Invalid this.modelForecastInputDataFormat -> "+this.modelForecastInputDataFormat.name() );
	//+" for inputDataType ->"+this.inputDataType.name()+" !!");
      }

      if (this.fmfDataTimeIntervalSeconds > MAX_FULL_FORECAST_TIME_INTERVAL_SECONDS) {
	  
        throw new RuntimeException(mmi+"Cannot have this.fmfDataTimeIntervalSeconds="+
                                   this.fmfDataTimeIntervalSeconds+" > MAX_FULL_FORECAST_TIME_INTERVAL_SECONDS="+MAX_FULL_FORECAST_TIME_INTERVAL_SECONDS);
      }

      // --- Need to have this.fmfDataTimeIntervalSeconds == this.prdDataTimeIntervalSeconds at this point.
      //     because time interp. of forecast data is not implemented yet
      if (this.fmfDataTimeIntervalSeconds != this.prdDataTimeIntervalSeconds) {
        throw new RuntimeException(mmi+"this.fmfDataTimeIntervalSeconds != this.prdDataTimeIntervalSeconds, time interp. for forecasts not implemented yet!");
      }

      slog.info(mmi+"Now doing full model forecast (FMF) adjustment-correction using previous forecast(s) data: prevFMFInputDataFilePath="+prevFMFInputDataFilePath);
      
      // ---
      this.adjustFullModelForecast(argsMap, prevFMFInputDataFilePath, uniqueTGMapObj, mainJsonMapObj, fmfFromZCConvVal); 

      slog.info(mmi+"Done with the specific FMF WL data adjustment at tide gauge -> "+this.location.getIdentity());

      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
    } // --- this.forecastAdjType != null

    // --- Legacy FMS will eventually be completely decommissioned.
    //     Keeping its usage here just in case we would need to
    //     re-activate it (very unlikely).
    // --- Instantiate the FMSInput object using the argsMap and this object.
    //this.fmsInputObj= new FMSInput(this, this.referenceTime);
    // --- and instantiate the FMS object itself with the FMSInput object
    //this.fmsObj= new FMS(this.fmsInputObj);
    //this.fmsObj= new FMS(new FMSInput(this));O

    slog.info(mmi+"end, tide gauge -> "+this.location.getIdentity());

    slog.info(mmi+"Debug System.exit(0)");
    System.exit(0);
  }

  // --- Comments please!
  final public List<MeasurementCustom> getAdjustment(final String optionalOutputDir) {
  //final public MeasurementCustomBundle getAdjustment(final String optionalOutputDir) { 

    final String mmi= "getAdjustment: ";

    slog.info(mmi+"start, tide gauge -> "+this.location.getIdentity());

    // --- Now stitch (merge) the adjusted-corrected FMF data with the last valid WLO available
    //     but only if we have that this last WLO data is more recent in time than the first
    //     FMF data time stamp.

    try {
      this.locationAdjustedData.size();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+"this.mostRecentWLOInstant cannot be null here !!");
    }
    
    // --- Get a MeasurementCustomBundle object for the this.locationAdjustedData object
    final MeasurementCustomBundle adjFMFMcb=
      new MeasurementCustomBundle(this.locationAdjustedData);

    final SortedSet<Instant> adjFMFMcbInstantsSet= adjFMFMcb.getInstantsKeySetCopy();
    
    //final Instant adjFMFMcbLeastRecentInstant= adjFMFMcb.getLeastRecentInstantCopy();

    final Instant leastRecentAdjFMFInstant= adjFMFMcbInstantsSet.first();
    final Instant mostRecentAdjFMFInstant= adjFMFMcbInstantsSet.last();

    slog.info(mmi+"leastRecentAdjFMFInstant="+leastRecentAdjFMFInstant.toString());
    slog.info(mmi+"mostRecentAdjFMFInstant="+mostRecentAdjFMFInstant.toString());

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
    
    // --- Use the last WLO data (if any) to do
    //     the short-term (~12H) fine-tuning adjustment
    //     of the corrected (time dependent residual)
    //     FMF signal at the TG.
    //     TODO: the content of this block should ideally
    //     be implemented in a generic method because the
    //     same type of fine-tuning adj. is also used on
    //     DFO-CDOS side (4 times per hour)
    if (this.haveWLOData) {
    
      try {
        this.mcbWLO.hashCode();
      } catch (NullPointerException npe) {
        throw new RuntimeException(mmi+"this.mcbWLO cannot be null here !!");
      }

      try {
        this.mostRecentWLOInstant.hashCode();
      } catch (NullPointerException npe) {
        throw new RuntimeException(mmi+"this.mostRecentWLOInstant cannot be null here !!");
      }   

      // --- Local copy of the most recent WLO Instant object
      //final Instant mostRecentWLOInstant= this.mcbWLO.getMostRecentInstantCopy();

      //slog.info(mmi+"adjFMFMcbLeastRecentInstant="+adjFMFMcbLeastRecentInstant.toString());
      slog.info(mmi+"this.mostRecentWLOInstant="+this.mostRecentWLOInstant.toString());

      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
      
      //if (!adjFMFMcbLeastRecentInstant.equals(mostRecentWLOInstant)) {
      // throw new RuntimeException(mmi+"Must have adjFMFMcbLeastRecentInstant.equals(mostRecentWLOInstant) at this point!");
      //}

      // --- Do this short-term (~12H) fine-tuning adjustment only if the
      //     this.mostRecentWLOInstant object is equal is more recent
      //     (i,e. is after in time) than the leastRecentAdjFMFInstant object      
      if ( this.mostRecentWLOInstant.equals(leastRecentAdjFMFInstant) ||
	     this.mostRecentWLOInstant.isAfter(leastRecentAdjFMFInstant) ) { 

        slog.info(mmi+"Merging the FMF corrected-adjusted data of the future with the last WLO data available at -> "+ mostRecentWLOInstant.toString());

        final double lastWLOValue= this.mcbWLO.
          getAtThisInstant(mostRecentWLOInstant).getValue();
      
        final double adjFMFValueAtLastWLOInstant=
          adjFMFMcb.getAtThisInstant(mostRecentWLOInstant).getValue();
 
        final double fmfWLOValuesDiff= lastWLOValue - adjFMFValueAtLastWLOInstant;

        slog.info(mmi+"lastWLOValue="+lastWLOValue);
        slog.info(mmi+"adjFMFValueAtLastWLOInstant="+adjFMFValueAtLastWLOInstant);
        slog.info(mmi+"fmfWLOValuesDiff="+fmfWLOValuesDiff);
        //slog.info(mmi+"Debug System.exit(0)");
        //System.exit(0);
      
        //final SortedSet<Instant> adjFMFMcbInstantsSet= adjFMFMcb.getInstantsKeySetCopy();
        //final long mergeTimeRefSeconds= adjFMFMcbLeastRecentInstant.getEpochSecond();
	
	final long mergeTimeRefSeconds= leastRecentAdjFMFInstant.getEpochSecond();

        // --- Use a final double with the inverted
        //     IWLAdjustment.SHORT_TERM_FORECAST_TS_OFFSET_SECONDS*(1.0+Math.abs(fmfWLOValuesDiff))
        //     instead of a division in repeated the Math.exp() calls in the following loop,
        //     it should give a better perf.
        //     NOTE: We also use an error modulation factor (1.0 + Math.exp(Math.abs(fmfWLOValuesDiff))) to
        //     "decrease" the time decaying factor in order to "increase" the time delay in proportion
        //     of the fmfWLOValuesDiff (error) for the merge operation done in the following loop
        final double shortTermFMFTSOffsetSecondsInv=
          1.0/(IWLAdjustment.SHORT_TERM_FORECAST_TS_OFFSET_SECONDS * (1.0 + Math.exp(Math.abs(fmfWLOValuesDiff))) );

        slog.info(mmi+"shortTermFMFTSOffsetSecondsInv="+shortTermFMFTSOffsetSecondsInv);

        // --- Loop on all the FMF Instant objects (FMF data duration in the future >= 48h)
        for (final Instant fmfAdjInstant: adjFMFMcbInstantsSet) {

          final double shortTermTimeOffsetSeconds=
	    (double)(fmfAdjInstant.getEpochSecond() - mergeTimeRefSeconds);
      
          final double shortTermTimeDecayingAdj= fmfWLOValuesDiff *
	    Math.exp(-shortTermTimeOffsetSeconds * shortTermFMFTSOffsetSecondsInv);
	
          MeasurementCustom fmfAdjMc= adjFMFMcb.getAtThisInstant(fmfAdjInstant);

          // slog.info(mmi+"shortTermTimeOffsetSeconds="+shortTermTimeOffsetSeconds);
          // slog.info(mmi+"shortTermTimeDecayingAdj="+shortTermTimeDecayingAdj);
          // slog.info(mmi+"fmfAdjMc.getValue() bef. merge="+fmfAdjMc.getValue());

          // --- NOTE: We directly adjust-merge the WL value in-situ in the MeasurementCustom
          //	   at the fmfAdjInstant in the this.locationAdjustedData List.
          fmfAdjMc.setValue(fmfAdjMc.getValue() + shortTermTimeDecayingAdj);

          // --- Convert 1 sigma errors std. dev to 2 sigma
          fmfAdjMc.setUncertainty(IWL.TWO_SIGMA_STDDEV_FACT * fmfAdjMc.getUncertainty());

          //slog.info(mmi+"fmfAdjMc.getValue() aft. merge="+fmfAdjMc.getValue());

          // if (shortTermTimeOffsetSeconds > 900) {
          // slog.info(mmi+"Debug System.exit(0)");
          // System.exit(0);   
          // }
	  
        } // --- for (final Instant fmfAdjInstant: adjFMFMcbInstantsSet) loop block
	
      } else {

	 slog.warn(mmi+"this.mostRecentWLOInstant -> "+this.mostRecentWLOInstant.toString()+
		   " is before in time the leastRecentAdjFMFInstant -> "+leastRecentAdjFMFInstant.toString()+
		   ", cannot use it for the short-term adj. at tide gauge -> "+this.location.getIdentity()+" !!");
	  
      } // ---  if-else (this.mostRecentWLOInstant.isAfter(adjFMFMcbLeastRecentInstant)) block
      
    } else  {

       slog.warn(mmi+"No valid WLO data for tide gauge -> "+this.location.getIdentity()+" !!");
	
    } // --- if (this.haveWLOData)
    
    // --- Now merge (adjust) the NSTide prediction with the last adjusted FMF WL value. 

    slog.info(mmi+"Now merge the 40 days NSTide prediction data(OR the 40 days climatologic OHPS-SLFE results for tide gauge ->"+this.location.getIdentity());

    //final Instant leastRecentAdjFMFInstant= adjFMFMcbInstantsSet.first();
    //final Instant mostRecentAdjFMFInstant= adjFMFMcbInstantsSet.last();

    slog.info(mmi+"leastRecentAdjFMFInstant="+leastRecentAdjFMFInstant.toString());
    slog.info(mmi+"mostRecentAdjFMFInstant="+mostRecentAdjFMFInstant.toString());
 
    final MeasurementCustom lastAdjFMFWLMc= 
      adjFMFMcb.getAtThisInstant(mostRecentAdjFMFInstant);
    
    final double lastAdjFMFWLValue= lastAdjFMFWLMc.getValue();
    final double lastAdjFMFWLUncertainty= lastAdjFMFWLMc.getUncertainty();
    
    slog.info(mmi+"lastAdjFMFWLValue="+lastAdjFMFWLValue);
    slog.info(mmi+"lastAdjFMFWLUncertainty="+lastAdjFMFWLUncertainty);

    slog.info(mmi+"Get simple stats for the adjFMF data");
    final MeasurementCustom adjFMFMcbStatsMc= MeasurementCustomBundle.getSimpleStats(adjFMFMcb, null, false);

    if (adjFMFMcbStatsMc.getUncertainty() < INumberCrunching.STD_DEV_MIN_VALUE) {
      throw new RuntimeException(mmi+"Std dev value too small for the adjFMF data !");
    }
    
    // --- Wrap the WL prediction data in a MeasurementCustomBundle object
    //     to ensure to have time synchronization with the FMF WL adj. data
    final MeasurementCustomBundle wlPredMCB= new MeasurementCustomBundle(this.locationPredData);

    final NavigableSet<Instant> instantsForPredStats= new
	TreeSet<Instant>(wlPredMCB.getInstantsKeySetCopy()).subSet(leastRecentAdjFMFInstant, true, mostRecentAdjFMFInstant, true);

    slog.info(mmi+"Get simple stats for the WL pred. data");
    final MeasurementCustom predStatsMc= MeasurementCustomBundle.getSimpleStats(wlPredMCB, instantsForPredStats, false);

    if (predStatsMc.getUncertainty() < INumberCrunching.STD_DEV_MIN_VALUE) {
      throw new RuntimeException(mmi+"Std dev value too small for the WL pred. data!");
    }
    
    slog.info(mmi+"adjFMFMcbStatsMc avg.="+adjFMFMcbStatsMc.getValue());
    slog.info(mmi+"adjFMFMcbStatsMc std dev="+adjFMFMcbStatsMc.getUncertainty());
    
    slog.info(mmi+"predStatsMc avg.="+predStatsMc.getValue());
    slog.info(mmi+"predStatsMc std dev="+predStatsMc.getUncertainty());

    final double wlPredsAvg= predStatsMc.getValue();

    final double avgsDiff= adjFMFMcbStatsMc.getValue() - wlPredsAvg;

    final String wlLocationIdentityStr= this.location.getIdentity();
    // (this.location.getIdentity().startsWith("0") ? this.location.getIdentity().substring(1): this.location.getIdentity());

    slog.info(mmi+"wlLocationIdentityStr="+wlLocationIdentityStr);
 
    // --- Get the int value of the num. id of the tide gauge.
    //     (Need to get rid of the starting '0' char here)
    final int wlLocationIdentityIntValue= Integer
      .parseInt(wlLocationIdentityStr.startsWith("0") ? wlLocationIdentityStr.substring(1) : wlLocationIdentityStr); //wlLocationIdentity.substring(1));

    slog.info(mmi+"wlLocationIdentityIntValue="+wlLocationIdentityIntValue);

    // --- Calculate the amplitude adjust factor which depends on the ratio of the std devs.
    //     of the adjusted FMF WL and the WL prediction. 
    double amplitudesAdjFact= 1.0 - adjFMFMcbStatsMc.getUncertainty()/predStatsMc.getUncertainty();

    // --- Do not do amplitude adjust of WL prediction for TGs upstream of FIRST_TG_WITH_WLO_AMP_AVG_ADJUST
    //     if the predStatsMc.getUncertainty() < adjFMFMcbStatsMc.getUncertainty() to avoid producting
    //     spurious oscillations with too much amplitudes for those tide gauges. The amplitude adjust for those
    //     tide gauges is done only when predStatsMc.getUncertainty() > adjFMFMcbStatsMc.getUncertainty()
    if ( wlLocationIdentityIntValue > FIRST_TG_WITH_WLO_AMP_AVG_ADJUST && predStatsMc.getUncertainty() < adjFMFMcbStatsMc.getUncertainty()) {
      slog.info(mmi+"predStatsMc.getUncertainty() < adjFMFMcbStatsMc.getUncertainty() setting amplitudesAdjFact to 0.0 here !!");
      amplitudesAdjFact= 0.0;
    }
    
    slog.info(mmi+"avgsDiff="+avgsDiff);
    slog.info(mmi+"amplitudesAdjFact="+amplitudesAdjFact);   

    // slog.info(mmi+"Debug exit 0");
    //System.exit(0);
    
    // --- Get the Instants objects to use for the WL pred. adjustments. We need to
    //     begin at the leastRecentAdjFMFInstant to ensure to be some time in the past
    //     before the mostRecentAdjFMFInstant.
    final NavigableSet<Instant> predMcbInstantsToAdj=
	new TreeSet<Instant>(wlPredMCB.getInstantsKeySetCopy()).tailSet(leastRecentAdjFMFInstant, true);

    // --- long term decaying time factor for adjusting-merging WL values
    //final double longTermFMFOffsetSecondsInvWLV= 1.0/IWLAdjustment.LONG_TERM_FORECAST_TS_OFFSET_SECONDS;

    final double leastRecentFMFOffsetSecondsInvWLV= 1.0/IWLAdjustment.LONG_TERM_FORECAST_TS_OFFSET_SECONDS;

    final long mostRecentMergeSecondsRef= mostRecentAdjFMFInstant.getEpochSecond();
    
    // ---
    for (final Instant wlPredAdjInst: predMcbInstantsToAdj) {

      final MeasurementCustom wlPredMc= wlPredMCB.getAtThisInstant(wlPredAdjInst);
      
      final double nonAdjWLPredValue= wlPredMc.getValue();

      double timeDecayingFactWLV= 1.0;

      // --- Begin to decrease the timeDecayingFactWLV only after the mostRecentAdjFMFInstant timestamp
      if (wlPredAdjInst.isAfter(mostRecentAdjFMFInstant)) {
      
        final double leastRecentOffsetSeconds=
	  (double)(wlPredAdjInst.getEpochSecond() - mostRecentMergeSecondsRef); 
      	
        //final double leastRecentTimeDecayingFactWLV=
        timeDecayingFactWLV= Math.exp(-leastRecentOffsetSeconds * leastRecentFMFOffsetSecondsInvWLV);
      }

      //slog.info(mmi+"timeDecayingFactWLV="+timeDecayingFactWLV);
      //slog.info(mmi+"Debug exit 0");
      //System.exit(0);
      
      //// --- One shot calculation for the WL pred. data adjustments for anplitude and avg..
      final double adjWLPredValue= nonAdjWLPredValue + timeDecayingFactWLV*(avgsDiff - (nonAdjWLPredValue-wlPredsAvg)*amplitudesAdjFact);
      
      //double adjWLPredValue= nonAdjWLPredValue;
      //final amplitudesAdjFact
      // if (nonAdjWLPredValue > wlPredsAvg) {
      // 	// --- Adjust the WL pred value according the averages difference and the amplitude adjustment factor
      // 	adjWLPredValue = nonAdjWLPredValue + (avgsDiff - (nonAdjWLPredValue-wlPredsAvg)*amplitudesAdjFact);
      // 	slog.info(mmi+"(nonAdjWLPredValue > wlPredsAvg): nonAdjWLPredValue="+nonAdjWLPredValue);
      // 	slog.info(mmi+"(nonAdjWLPredValue > wlPredsAvg): adjWLPredValue="+adjWLPredValue);
      // 	//slog.info(mmi+"(nonAdjWLPredValue-wlPredsAvg)*(1.0 - amplitudesAdjFact)="+(nonAdjWLPredValue-wlPredsAvg)*(1.0 - amplitudesAdjFact));
      //   //slog.info(mmi+"Debug exit 0");
      //   //System.exit(0);	
      // } else {
      // 	adjWLPredValue = nonAdjWLPredValue + (avgsDiff + (wlPredsAvg-nonAdjWLPredValue)*amplitudesAdjFact);
      // 	slog.info(mmi+"(nonAdjWLPredValue < wlPredsAvg): nonAdjWLPredValue="+nonAdjWLPredValue);
      // 	slog.info(mmi+"(nonAdjWLPredValue < wlPredsAvg): adjWLPredValue="+adjWLPredValue);
      // 	//slog.info(mmi+"(wlPredsAvg-nonAdjWLPredValue)*(1.0 - amplitudesAdjFact)="+(wlPredsAvg-nonAdjWLPredValue)*(1.0 - amplitudesAdjFact));
      //   //slog.info(mmi+"Debug exit 0");
      //   //System.exit(0);	
      // }

      wlPredMc.setValue(adjWLPredValue);

      //slog.info(mmi+"Debug exit 0");
      //System.exit(0);
	
    }

    slog.info(mmi+"After amp. + avg, adj"); 
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    //// --- Wrap the WL prediction data in a MeasurementCustomBundle object
    ////     to ensure to have time synchronization with the FMF WL adj. data
    //final MeasurementCustomBundle wlPredMCB= new MeasurementCustomBundle(this.locationPredData);

    final MeasurementCustom wlPredMcForDiff= wlPredMCB.getAtThisInstant(mostRecentAdjFMFInstant);

    try {
      wlPredMcForDiff.getValue();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+"wlPredMc cannot be null here !!");
    }

    // --- Get the difference between the last FMF WL adjusted value and
    //     the corresponding time synchronized WL prediction value.
    final double lastFMFVsPredDiff= lastAdjFMFWLValue - wlPredMcForDiff.getValue();

    slog.info(mmi+"lastFMFVsPredDiff="+lastFMFVsPredDiff);
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    //final long mostRecentMergeSecondsRef= mostRecentAdjFMFInstant.getEpochSecond();

    // --- Get copies of all the Instant object of the WL prediction data starting
    //     at the Instant object of the adj. FMF WL data that is the 1st after the
    //     last (most recent) Instant object (0therwise we end-up with a duplicate
    //     Instant having two different WL values), Need to convert the wlPredMCB.getInstantsKeySetCopy()
    //     SortedSet to a temp. TreeSet object with which we can use its tailSet method with the
    //     false arg. to begin the NavigableSet at the Instant that follows the
    //     mostRecentAdjFMFInstant object. 
    //final NavigableSet<Instant> predMcbInstantsTailSet= Collections
    //	.synchronizedSortedSet(new TreeSet<Instant>( wlPredMCB.getInstantsKeySetCopy() ).tailSet(mostRecentAdjFMFInstant,false));

    final NavigableSet<Instant> predMcbInstantsTailSet=
	new TreeSet<Instant>(wlPredMCB.getInstantsKeySetCopy()).tailSet(mostRecentAdjFMFInstant,false);

    // --- This way of defining the predMcbInstantsTailSet was causing
    //     that we ended-up with duplicate Instant having two different WL values)
    //final SortedSet<Instant> predMcbInstantsTailSet= 
    //  wlPredMCB.getInstantsKeySetCopy().tailSet(mostRecentAdjFMFInstant);

    // --- long term decaying time factor for adjusting-merging WL values
    final double longTermFMFOffsetSecondsInvWLV= 1.0/IWLAdjustment.LONG_TERM_FORECAST_TS_OFFSET_SECONDS;

    // --- long term decaying time factor for adjusting-merging WL values uncertainties
    //     It is the longTermFMFOffsetSecondsInvWLV divided by 5 so it decays 5 times
    //     more slowly than for the WL values themselves.
    final double longTermFMFOffsetSecondsInvWLU= 0.2 * longTermFMFOffsetSecondsInvWLV;

    //final MeasurementCustom checkLastLADMC= this.locationAdjustedData.get(this.locationAdjustedData.size()-1);
    //slog.info(mmi+"checkLastLADMC.getEventDate()="+checkLastLADMC.getEventDate().toString());							       
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
    
    // --- Loop on the WL prediction Instant objects starting at the last
    //     adj. FMF WL Instant object. 
    for (final Instant longTermInstant: predMcbInstantsTailSet) {
	
      //slog.info(mmi+"longTermInstant="+longTermInstant.toString());
	
      final double longTermOffsetSeconds=
	(double)(longTermInstant.getEpochSecond() - mostRecentMergeSecondsRef); 
      	
      final double longTermTimeDecayingFactWLV=
        Math.exp(-longTermOffsetSeconds * longTermFMFOffsetSecondsInvWLV);

      final double adjWLPredValue=
	wlPredMCB.getAtThisInstant(longTermInstant).getValue() + longTermTimeDecayingFactWLV * lastFMFVsPredDiff;

      final double longTermTimeDecayingFactWLU=
        Math.exp(-longTermOffsetSeconds * longTermFMFOffsetSecondsInvWLU);
      
      double adjWLPredUncertainty= lastAdjFMFWLUncertainty +
	(1.0 - longTermTimeDecayingFactWLU) * IWL.MAXIMUM_UNCERTAINTY_METERS; 

      // --- Limit the max. value of adjWLPredUncertainty at IWL.MAXIMUM_UNCERTAINTY_METERS
      adjWLPredUncertainty=
        (adjWLPredUncertainty > IWL.MAXIMUM_UNCERTAINTY_METERS) ? IWL.MAXIMUM_UNCERTAINTY_METERS: adjWLPredUncertainty;
      
      //slog.info(mmi+"longTermInstant="+longTermInstant.toString());
      //slog.info(mmi+"adjWLPredValue="+adjWLPredValue);
      //slog.info(mmi+"adjWLPredUncertainty="+adjWLPredUncertainty+"\n");

      //if (longTermOffsetSeconds > 900) {
      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);
      //}

      // --- Update the List<MeasurementCustom> object that holds the adjusted-merged combination of
      //     the FMF WL data and the long term WL prediction data for the longTermInstant being
      //     processed.
      this.locationAdjustedData.
	add( new MeasurementCustom(longTermInstant.plusSeconds(0L), adjWLPredValue, adjWLPredUncertainty));
    }

    // --- Remove the small (4-5cm max) WL oscillations that can possibly be produced by the adjustment method(s)
    this.locationAdjustedData= WLMeasurement.
      removeHFWLOscillations(MAX_TIMEINCR_DIFF_FOR_NNEIGH_TIMEINTERP_SECONDS, this.locationAdjustedData);
    
    // --- Legacy FMS will eventually be completely decommissioned.
    //     Keeping its usage here just in case we would need to
    //     re-activate it (very unlikely).
    //final fmsContext fmsContextObj= this.getFmsContext(this);
    //this.locationAdjustedData= this.
    //   fmsObj.update().getNewForecastData();

    // --- Write all data in JSON format for debugging purposes only (normally).
    if (optionalOutputDir != null) {

      slog.info(mmi+"Writing non-adjusted FMF WL data in the optionalOutputDir -> "+optionalOutputDir);

      final List<MeasurementCustom> nonAdjFMFWLData= this.nearestModelData.
	get(IWLAdjustmentIO.FullModelForecastType.ACTUAL.ordinal()).get(this.location.getIdentity());
      
      final String nonAdjFMFWLDataFName= this.getFMFLeadTimeECCCOperStr() +
	IWLToolsIO.ISO8601_YYYYMMDD_SEP_CHAR + IWLAdjustmentIO.NONADJ_FMF_ATTG_FNAME_PRFX + this.location.getIdentity();

      slog.info(mmi+"nonAdjFMFWLDataFName="+nonAdjFMFWLDataFName);
      
      WLToolsIO.writeToOutputDir( nonAdjFMFWLData,
				  IWLToolsIO.Format.CHS_JSON,
				  nonAdjFMFWLDataFName, optionalOutputDir);
    }

    slog.info(mmi+"end for tide gauge -> "+this.location.getIdentity());

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    return this.locationAdjustedData; //adjustmentRet;
  }

  // ---
  //final public Instant getReferenceTime() {
  //  return this.referenceTime;
  //}

}
