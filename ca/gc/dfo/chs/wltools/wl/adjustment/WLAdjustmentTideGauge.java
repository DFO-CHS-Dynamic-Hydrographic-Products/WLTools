package ca.gc.dfo.chs.wltools.wl.adjustment;

//---
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.List;
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
import java.util.Collection;
import java.util.NavigableSet;
//import java.util.stream.Stream;
import java.nio.file.DirectoryStream;
//import java.nio.file.PathMatcher;
//import java.nio.file.FileSystems;

//import java.awt.geom.Point2D; //.Double;
//import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

// ---
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.wl.fms.FMS;
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.wl.IWLLocation;
import ca.gc.dfo.chs.wltools.wl.fms.FMSInput;
import ca.gc.dfo.chs.wltools.wl.WLMeasurement;
import ca.gc.dfo.chs.wltools.wl.fms.FMSFactory;
import ca.gc.dfo.chs.wltools.util.Trigonometry;
import ca.gc.dfo.chs.wltools.util.ITimeMachine;
import ca.gc.dfo.chs.wltools.wl.ITideGaugeConfig;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
//import ca.gc.dfo.chs.wltools.nontidal.stage.StageIO;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustment;
import ca.gc.dfo.chs.wltools.wl.adjustment.IWLAdjustmentIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
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

  //private List<MeasurementCustom> tgLocationWLOData= null;
  //private ArrayList<MeasurementCustom> tgLocationWLPData= null;
  //private List<MeasurementCustom> tgLocationWLFData= null;

  /**
   * Comments please!
   */
  public WLAdjustmentTideGauge() {
    super();

    //this.tgLocationWLPData= null;
    //this.tgLocationWLOData=
    //  this.tgLocationWLPData=
    //    this.tgLocationWLFData = null;
  }


  /**
   * Comments please!
   */
  public WLAdjustmentTideGauge(/*@NotNull*/ final HashMap<String,String> argsMap) {

    super(IWLAdjustment.Type.TideGauge, argsMap);

    final String mmi= "WLAdjustmentTideGauge main constructor: ";

    slog.info(mmi+"start: this.locationIdInfo="+this.locationIdInfo); //wdsLocationIdInfoFile="+wdsLocationIdInfoFile);

    final Set<String> argsMapKeysSet =argsMap.keySet();

    if (!argsMapKeysSet.contains("--tideGaugeLocationsDefFileName")) {
      throw new RuntimeException(mmi+
        "Must have the --tideGaugeLocationsDefFileName=<tide gauges definition file name> defined in argsMap");
    }

    final String tideGaugeLocationsDefFileName= argsMap.get("--tideGaugeLocationsDefFileName");

    if (!argsMapKeysSet.contains("--tideGaugePredictInputDataInfo")) {
      throw new RuntimeException(mmi+
         "Must have the --tideGaugePredictInputDataInfo=<file format>:<complete path to the tide gauge WLP input data file> defined in argsMap");
    }

    final String [] tideGaugePredictInputDataInfo= argsMap.
       get("--tideGaugePredictInputDataInfo").split(IWLAdjustmentIO.INPUT_DATA_FMT_SPLIT_CHAR) ;

    if (!IWLStationPredIO.allowedFormats.contains(tideGaugePredictInputDataInfo[0])) {

      throw new RuntimeException(mmi+"Invalid tideGaugePredictInputData file format -> "+
        tideGaugePredictInputDataInfo[0]+" Must be one of -> "+IWLStationPredIO.allowedFormats.toString());
    }

    this.predictInputDataFormat= IWLStationPredIO.
      Format.valueOf(tideGaugePredictInputDataInfo[0]);

    final String tideGaugePredictInputDataFile= tideGaugePredictInputDataInfo[1];  ;//argsMap.get("--tideGaugePredictInputDataFile");

    if (!argsMapKeysSet.contains("--tideGaugeWLODataInfo")) {
      throw new RuntimeException(mmi+
         "Must have the --tideGaugeInputDataInfo=<TG WLO file format>:<complete path to the tide gauge input WLO data file> defined in the argsMap");
    }

    final String [] tideGaugeWLODataInfo= argsMap.
      get("--tideGaugeWLODataInfo").split(IWLAdjustmentIO.INPUT_DATA_FMT_SPLIT_CHAR);

    if (!IWLStationPredIO.allowedFormats.contains(tideGaugeWLODataInfo[0])) {

      throw new RuntimeException(mmi+"Invalid TG WLO Input Data file format -> "+
        tideGaugeWLODataInfo[0]+" Must be one of -> "+IWLStationPredIO.allowedFormats.toString());
    }

    this.obsInputDataFormat=
      IWLStationPredIO.Format.valueOf(tideGaugeWLODataInfo[0]);

    // --- Extract the path of the WLO data file for the TG.
    final String tideGaugeWLODataFile= tideGaugeWLODataInfo[1]; ;//argsMap.get("--tideGaugeWLODataFile");

    // --- Verify that we have the same name id. for the TG between the file name and
    //     the this.locationIdInfo attrbute.
    final String [] tideGaugeWLODataFilePathSplit= tideGaugeWLODataFile.split(File.separator);

    //--- Extract the 1st part of the WLO data file which MUST be the same string id. as for the
    //    this.locationIdInfo attribute.
    final String tideGaugeNameIdFromFileName=
      tideGaugeWLODataFilePathSplit[tideGaugeWLODataFilePathSplit.length-1].split(IWLAdjustmentIO.OUTPUT_DATA_FMT_SPLIT_CHAR)[0];

    if (!tideGaugeNameIdFromFileName.equals(this.locationIdInfo)) {
      throw new RuntimeException(mmi+"tideGaugeNameIdFromFileName="+tideGaugeNameIdFromFileName+
                                " is NOT the same tg station id. as this.locationIdInfo="+this.locationIdInfo);
    }

    slog.info(mmi+"tideGaugeWLODataFile="+tideGaugeWLODataFile);
    slog.info(mmi+"tideGaugePredictInputDataFile="+tideGaugePredictInputDataFile);
    slog.info(mmi+"this.modelForecastInputDataInfo="+this.modelForecastInputDataInfo);
    //System.out(mmi+"Debug System.exit(0)");
    //System.exit(0);

    if (argsMapKeysSet.contains("--tideGaugeAdjMethods")) {

      final String [] tideGaugeAdjMethodCheck= argsMap.
        get("--tideGaugeAdjMethods").split(IWLAdjustmentIO.INPUT_DATA_FMT_SPLIT_CHAR);

      if (!IWLAdjustment.allowedTideGaugeAdjMethods.contains(tideGaugeAdjMethodCheck[0]) ) {
        throw new RuntimeException(mmi+"Invalid tide gauge WL adjustment method -> "+tideGaugeAdjMethodCheck[0]+
                                    " Must be one of -> "+IWLAdjustment.allowedTideGaugeAdjMethods.toString());
      }

      this.predictAdjType= IWLAdjustment.
        TideGaugeAdjMethod.valueOf(tideGaugeAdjMethodCheck[0]);

      if (tideGaugeAdjMethodCheck.length == 2) {

        if (!IWLAdjustment.allowedTideGaugeAdjMethods.contains(tideGaugeAdjMethodCheck[1]) ) {
          throw new RuntimeException(mmi+"Invalid tide gauge WL adjustment method -> "+tideGaugeAdjMethodCheck[1]+
                                    " Must be one of -> "+IWLAdjustment.allowedTideGaugeAdjMethods.toString() );
        }

        this.forecastAdjType= IWLAdjustment.
          TideGaugeAdjMethod.valueOf(tideGaugeAdjMethodCheck[1]);
      }
    }

    if (this.predictAdjType != IWLAdjustment.TideGaugeAdjMethod.CHS_IWLS_QC) {
      slog.info(mmi+"Only the tide gauge WL prediction adjustment type -> "+
                IWLAdjustment.TideGaugeAdjMethod.CHS_IWLS_QC.name()+" is allowed for now !");
    }

    //if ( this.forecastAdjType != null ) {
    //  if (this.forecastAdjType != IWLAdjustment.TideGaugeAdjMethod.ECCC_H2D2_FORECAST_AUTOREG) {
    //    slog.info(mmi+"Only the tide gauge WL forecast adjustment type -> "+
    //            IWLAdjustment.TideGaugeAdjMethod.ECCC_H2D2_FORECAST_AUTOREG.name()+" is allowed for now !");
    //  }
    //}

    slog.info(mmi+"this.predictAdjType="+this.predictAdjType.name());
    slog.info(mmi+"this.forecastAdjType="+this.forecastAdjType.name());
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    // --- Now find the two nearest CHS tide gauges from this WDS grid point location
    final String tideGaugesInfoFile= WLToolsIO.getMainCfgDir() + File.separator +
      ITideGaugeConfig.INFO_FOLDER_NAME + File.separator + tideGaugeLocationsDefFileName ;

    slog.info(mmi+"tideGaugesInfoFile="+tideGaugesInfoFile);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    FileInputStream jsonFileInputStream= null;

    try {
      jsonFileInputStream= new FileInputStream(tideGaugesInfoFile);

    } catch (FileNotFoundException e) {
      throw new RuntimeException(mmi+e);
    }

    final JsonObject mainJsonMapObj= Json.
      createReader(jsonFileInputStream).readObject();

    //double minDistRad= Double.MAX_VALUE;
    // String [] twoNearestTideGaugesIds= {null, null};
    //Map<Double,String> tmpDistCheck= new HashMap<Double,String>();

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
    this.location.
      setConfig(mainJsonMapObj.getJsonObject(this.locationIdInfo));

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

    if (this.predictInputDataFormat == IWLStationPredIO.Format.CHS_JSON ) {

      // --- Here we do not need to check if the time stamps of the predictions are
      //     consistent with what we want hence the -1 for the 2nd arg. for the
      //     WLAdjustmentIO.getWLDataInJsonFmt() method. We also assume that the WL
      //     predictions values are already referred to a global or regional vertical
      //     datum (and not the local CHS ZC) hence the 0.0 value for the 3rd argument
      //     of WLAdjustmentIO.getWLDataInJsonFmt() method.
      //this.tgLocationWLPData=
      this.locationPredData= WLAdjustmentIO.
        getWLDataInJsonFmt(tideGaugePredictInputDataFile,-1L,0.0);

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
    this.prdDataTimeIntervalSeconds= MeasurementCustom.
      getDataTimeIntervallSeconds(this.locationPredData);

    slog.info(mmi+"Done with reading prediction input data from file -> "+tideGaugePredictInputDataFile);
    slog.info(mmi+"this.locationPredData.size()="+this.locationPredData.size());
    slog.info(mmi+"this.locationPredData time increment intervall="+this.prdDataTimeIntervalSeconds);
    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    slog.info(mmi+"Reading the TG obs (WLO) at location -> "+
              this.location.getIdentity()+" data using "+this.obsInputDataFormat.name());

    if (this.obsInputDataFormat == IWLStationPredIO.Format.CHS_JSON ) {

      this.nearestObsData= new HashMap<String,List<MeasurementCustom>>();

      // --- Read the WLO data in a temp. List<MeasurementCustom> object
      final List<MeasurementCustom> tmpWLOMcList= WLAdjustmentIO.
        getWLDataInJsonFmt(tideGaugeWLODataFile, this.prdDataTimeIntervalSeconds, this.adjLocationZCVsVDatum);

      slog.info(mmi+"tmpWLOMcList.size()="+tmpWLOMcList.size());
      slog.info(mmi+"tmpWLOMcList.get(0).getValue()="+tmpWLOMcList.get(0).getValue());
      slog.info(mmi+"Debug System.exit(0)");
      System.exit(0);

      // --- Assign the temp. List<MeasurementCustom> object to the this.nearestObsData object
      //     using the TG location id as key but apply the WLMeasurement.removeHFWLOscillations
      //     method to it before the assignation.
      this.nearestObsData.put(this.location.getIdentity(),
                              WLMeasurement.removeHFWLOscillations(this.prdDataTimeIntervalSeconds,tmpWLOMcList)) ;
                             //this.getWLDataInJsonFmt(tideGaugeWLODataFile, prdTimeIncrSeconds));

      slog.info(mmi+"Done with reading the TG obs (WLO) at location -> "+this.location.getIdentity());
      slog.info(mmi+"this.nearestObsData.get(this.location.getIdentity()).size()="+
                this.nearestObsData.get(this.location.getIdentity()).size());

      //slog.info(mmi+"Debug System.exit(0)");
      //System.exit(0);

    } else {
      throw new RuntimeException(mmi+"Invalid TG observation input data format -> "+this.obsInputDataFormat.name());
    }

    // ---
    if (this.forecastAdjType != null) {

      if (this.forecastAdjType != IWLAdjustment.
            TideGaugeAdjMethod.SINGLE_TIMEDEP_FMF_ERROR_STATS) {

        slog.info(mmi+"Only the tide gauge WL forecast adjustment type -> "+
                  IWLAdjustment.TideGaugeAdjMethod.SINGLE_TIMEDEP_FMF_ERROR_STATS.name()+" is allowed for now !");
      }

     this.fullForecastModelName= argsMapKeysSet.
       contains("--fullForecastModelName") ? argsMap.get("--fullForecastModelName") : IWLAdjustment.DEFAULT_MODEL_NAME;

      if (this.modelForecastInputDataInfo == null) {
        throw new RuntimeException(mmi+
                  "this.modelForecastInputDataInfo attribute cannot be null at this point if this.forecastAdjType is not null !");
      }

      slog.info(mmi+"this.modelForecastInputDataInfo="+this.modelForecastInputDataInfo);

      // --- Just need the tide gauge CHS Id. for the getH2D2ASCIIWLFProbesData
      //     method call.
      final Map<String, HBCoords> uniqueTGMapObj= new HashMap<String, HBCoords>();
      uniqueTGMapObj.put(this.location.getIdentity(), null);

      String prevFMFASCIIDataFilePath= null;

      if (this.modelForecastInputDataFormat == IWLAdjustmentIO.DataTypesFormatsDef.ECCC_H2D2_ASCII) {

        // --- Just need the tide gauge CHS Id. for the getH2D2ASCIIWLFProbesData
        //     method call.
        //final Map<String, HBCoords> uniqueTGMapObj= new HashMap<String, HBCoords>();
        //uniqueTGMapObj.put(this.location.getIdentity(), null);

        //this.nearestModelData= new HashMap<String, List<MeasurementCustom>>();

        // --- Here the this.modelForecastInputDataInfo attribute is the complete path to
        //     an ECCC_H2D2 probes (at the CHS TGs locations in fact) file of the ECCC_H2D2_ASCII
        //     format. It should be the H2D2 model forecast data of the actual synoptic run as
        //     it is specified with the IWLAdjustmentIO.FullModelForecastType.ACTUAL.ordinal()
        //     argument to this method.
        prevFMFASCIIDataFilePath= this.getH2D2ASCIIWLFProbesData(this.modelForecastInputDataInfo,
                                                                 uniqueTGMapObj, mainJsonMapObj,
                                                                 IWLAdjustmentIO.FullModelForecastType.ACTUAL.ordinal()); // , this.nearestModelData);

        slog.info(mmi+"Done with reading the model full forecast at TG location -> "+this.location.getIdentity());

        //slog.info(mmi+"this.nearestModelData.size()="+this.nearestModelData.size());
        //slog.info(mmi+"this.nearestModelData.get(IWLAdjustmentIO.FullModelForecastType.ACTUAL).get(this.location.getIdentity()).get(0).getValue()="+
        //              this.nearestModelData.get(IWLAdjustmentIO.FullModelForecastType.ACTUAL.ordinal()).get(this.location.getIdentity()).get(0).getValue());

        //slog.info(mmi+"previousFMFASCIIDataFilePath="+previousFMFASCIIDataFilePath);

        //this.getH2D2ASCIIWLFProbesData( prevFMFASCIIDataFilePath,
        //                                uniqueTGMapObj, mainJsonMapObj,
        //                                IWLAdjustmentIO.FullModelForecastType.PREVIOUS.ordinal() );

        //slog.info(mmi+"this.nearestModelData.get(IWLAdjustmentIO.FullModelForecastType.PREVIOUS).get(this.location.getIdentity()).get(0).getValue()="+
        //              this.nearestModelData.get(IWLAdjustmentIO.FullModelForecastType.PREVIOUS.ordinal()).get(this.location.getIdentity()).get(0).getValue());

      } else {
        throw new RuntimeException(mmi+"Invalid this.modelForecastInputDataFormat -> "
                                   +this.modelForecastInputDataFormat.name() ); //+" for inputDataType ->"+this.inputDataType.name()+" !!");
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

      slog.info(mmi+"Now doing full model forecast correction-adjustment using previous forecast(s) data: prevFMFASCIIDataFilePath="+prevFMFASCIIDataFilePath);

      this.adjustFullModelForecast(prevFMFASCIIDataFilePath, uniqueTGMapObj, mainJsonMapObj);

      slog.info(mmi+"Done with the full model forecast correction-adjustment");

      slog.info(mmi+"Debug System.exit(0)");
      System.exit(0);

    } // --- this.forecastAdjType != null

    slog.info(mmi+"Done with reading the WL input data to adjust, now doing the setup for the IWLS FMS legacy wl adjustment algo");

    // --- Instantiate the FMSInput object using the argsMap and this object.
    this.fmsInputObj= new FMSInput(this);

    // --- and instantiate the FMS object itself with the FMSInput object
    this.fmsObj= new FMS(this.fmsInputObj);
    //this.fmsObj= new FMS(new FMSInput(this));O

    slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);
  }

  ///**
  // * Comments please.
  // */
  final public List<MeasurementCustom> getAdjustment(final String outputDirectory) {

    final String mmi= "getAdjustment: ";

    slog.info(mmi+"start");

    // --- 1.) Get the adjustment-correction for the model forecast (ECCC P. Matte's algo).

    // --- 2.) Get the adjustment-correction for the predictions (IWLS WLF-QC algo) and merge
    //         the result of the adjustment-correction for the model forecast done at the
    //         previous step with it.
    //final fmsContext fmsContextObj= this.getFmsContext(this);
    this.locationAdjustedData= this.
       fmsObj.update().getNewForecastData();

    if (outputDirectory != null) {
      fmsObj.writeAllDataInCSVFiles(this.fmsInputObj.getFirstInstantForWriting(), outputDirectory);
    }

    slog.info(mmi+"end");

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    return this.locationAdjustedData; //adjustmentRet;
  }
}
