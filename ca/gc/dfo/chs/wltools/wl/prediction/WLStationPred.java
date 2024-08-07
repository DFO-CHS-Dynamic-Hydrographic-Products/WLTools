//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl.prediction;

/**
 * Created by Gilles Mercier on 2018-01-10.
 */

//---
import java.util.Set;
import java.util.List;
import org.slf4j.Logger;
import java.time.Instant;
import java.util.HashMap;
import org.slf4j.LoggerFactory;
//import javax.validation.constraints.NotNull;

// ---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

// ---
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.wl.IWLLocation;
import ca.gc.dfo.chs.wltools.util.ITimeMachine;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStage;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStageIO;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPred;
import ca.gc.dfo.chs.wltools.wl.prediction.IWLStationPredIO;
import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPredFactory;

/**
 * Class for the computation of water level predictions
 */
final public class WLStationPred extends WLStationPredFactory {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.wl.prediction.WLStationPred";

  /**
   * Usual class instance log utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  //private String outputDirectory= null;
  //private List<MeasurementCustom> predictionData= null;

  /**
   * Default constuctor:
   */
  public WLStationPred() {
    super();
  }

 /**
   * Parse the main program arguments:
   */
  public WLStationPred(/*NotNull*/ final HashMap<String,String> argsMap) {

    final String mmi= "WLStationPred main constructor: ";

    slog.info(mmi+"start");

    final Set<String> argsMapKeySet= argsMap.keySet();

    if (!argsMapKeySet.contains("--startTimeISOFormat")) {
      throw new RuntimeException(mmi+
        "Must have the mandatory prediction location info option: --startTimeISOFormat=<YYYY-MM-DDThh:mm:ss> defined");
    }

    // --- Fool-proof check for the startTimeISOFormat?
    //     Assuming that the machine time is in UTC
    final String startTimeISOFormat= argsMap.get("--startTimeISOFormat");

    slog.info(mmi+"startTimeISOFormat="+startTimeISOFormat);

    final Instant startTimeInstant= Instant.parse(startTimeISOFormat); // ("2014-01-01T00:00:00Z");

    final long startTimeSeconds=
      startTimeInstant.toEpochMilli() / ITimeMachine.SEC_TO_MILLISEC;

    //try {
    //  WLToolsIO.getOutputDirectory().length();
    //} catch (NullPointerException e) {
    //  throw new RuntimeException(mmi+e);
    //}
    //this.outputDirectory= WLToolsIO.getOutputDirectory();

    //if (argsMapKeySet.contains("--outputDirectory")) {
    //this.outputDirectory= argsMap.get("--outputDirectory");
    //}

    slog.info(mmi+"Will use WLToolsIO.getOutputDirectory()="+
                WLToolsIO.getOutputDirectory()+" to write prediction results");

    long predDurationInDays= IWLStationPred.DEFAULT_DAYS_DURATION_IN_FUTURE;

    if (argsMapKeySet.contains("--predDurationInDays")) {
      predDurationInDays= Long.parseLong(argsMap.get("--predDurationInDays"));
    }

    if (predDurationInDays > IWLStationPred.MAX_DAYS_DURATION) {

      throw new RuntimeException(mmi+"predDurationInDays -> "+predDurationInDays+
                                 " is larger than IWLStationPred.MAX_DAYS_DURATION_IN_FUTURE -> "+
                                 IWLStationPred.MAX_DAYS_DURATION);
    }

    final long endTimeSeconds= startTimeSeconds +
      predDurationInDays * (long)ITimeMachine.SECONDS_PER_DAY; // + 40L*24L*3600L;

    long timeIncrInSeconds= IWLStationPred.DEFAULT_TIME_INCR_SECONDS;

    if (argsMapKeySet.contains("--timeIncrInSeconds")) {
      timeIncrInSeconds= Long.parseLong(argsMap.get("--timeIncrInSeconds"));
    }

    if (timeIncrInSeconds > IWLStationPred.MAX_TIME_INCR_SECONDS) {
      throw new RuntimeException(mmi+"timeIncrInSeconds ->"+timeIncrInSeconds+
                                 "is larger than IWLStationPred.MAX_TIME_INCR_SECONDS -> "+
                                 IWLStationPred.MAX_TIME_INCR_SECONDS);
    }

    if (timeIncrInSeconds < IWLStationPred.MIN_TIME_INCR_SECONDS) {
      throw new RuntimeException(mmi+"timeIncrInSeconds ->"+timeIncrInSeconds+
                                 "is smaller than IWLStationPred.MIN_TIME_INCR_SECONDS -> "+
                                 IWLStationPred.MIN_TIME_INCR_SECONDS);
    }

    slog.info(mmi+"startTimeInstant="+startTimeInstant.toString());
    slog.info(mmi+"startTimeSeconds="+startTimeSeconds);
    slog.info(mmi+"endTimeSeconds="+endTimeSeconds);
    slog.info(mmi+"timeIncrInSeconds="+timeIncrInSeconds);

    //slog.info(mmi+"Debug System.exit(0)");
    //System.exit(0);

    if (!argsMapKeySet.contains("--stationPredType")) {
      throw new RuntimeException(mmi+"Must have the mandatory prediction location info option: --stationPredType" );
    }

    final String stationPredType= argsMap.get("--stationPredType" );

    System.out.println(mmi+"stationPredType="+stationPredType);

    final String [] stationPredTypeSplit= stationPredType.split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

    //if (!stationPredType.equals(IWLStationPred.Type.TIDAL.name()+":"+ITidal.Method.NON_STATIONARY_FOREMAN.name())) {

    final String mainPredType= stationPredTypeSplit[0];
    
    if (!mainPredType.equals(IWLStationPred.Type.TIDAL.name())) {
      throw new RuntimeException(mmi+"Only "+IWLStationPred.Type.TIDAL.name()+" main prediction type allowed for now !!");
    }

    // if ( !stationPredTypeSplit[0].equals(ITidal.Method.NON_STATIONARY_FOREMAN.name())
    // 	&& !stationPredTypeSplit[0].equals(ITidal.Method.NON_STATIONARY_STAGE.name()) ) {
    //   throw new RuntimeException(mmi+"Only "+ITidal.Method.NON_STATIONARY_FOREMAN.name()+" or "+
    // 				 ITidal.Method.NON_STATIONARY_STAGE.name()+" predictions methods allowed for now!!");
    // }

    if (!argsMapKeySet.contains("--stationIdInfo")) {
      throw new RuntimeException(mmi+"Must have the mandatory prediction location info option: --stationIdInfo" );
    }

    final String stationIdInfo= argsMap.get("--stationIdInfo" );

    //System.out.println(mmi+"stationPredType="+stationPredType);
    System.out.println(mmi+"stationIdInfo="+stationIdInfo);

    //WLStationPredFactory wlStnPrdFct= null;

    //final String [] stationPredTypeSplit=
    //  stationPredType.split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);
    //final String mainPredType= stationPredTypeSplit[0];

    if ( mainPredType.equals(IWLStationPred.Type.TIDAL.name())) { //  ("TIDAL") ) {

      // --- Tidal pred. method is ITidal.Method.NON_STATIONARY_FOREMAN by default.
      ITidal.Method tidalMethod= ITidal.Method.NON_STATIONARY_FOREMAN;

      final String specTidalMethod= stationPredTypeSplit[1];

      if (specTidalMethod.equals(ITidal.Method.STATIONARY_FOREMAN.name())) {

        //tidalMethod= ITidal.Method.STATIONARY_FOREMAN;

        throw new RuntimeException(mmi+"The"+
                                   ITidal.Method.STATIONARY_FOREMAN.name()+
                                   " tidal prediction method is not allowed for now!!");
      }

      // --- Default type for the stage discharge data.
      IStage.Type stageType= IStage.Type.DISCHARGE_CFG_STATIC;

      if ( specTidalMethod.equals(ITidal.Method.NON_STATIONARY_FOREMAN.name())
	   || specTidalMethod.equals(ITidal.Method.NON_STATIONARY_STAGE.name()) ) {

	// --- ITidal.Method.NON_STATIONARY_FOREMAN default method.
	tidalMethod= ITidal.Method.NON_STATIONARY_FOREMAN;

	if (specTidalMethod.equals(ITidal.Method.NON_STATIONARY_STAGE.name())) {
	  tidalMethod= ITidal.Method.NON_STATIONARY_STAGE;
	}

	slog.info(mmi+"Using tidalMethod="+tidalMethod.name());
	  
        //// --- Default type for the stage discharge data.
        //IStage.Type stageType= IStage.Type.DISCHARGE_CFG_STATIC;
	  
        if (!argsMapKeySet.contains("--stageType")) {
          throw new RuntimeException(mmi+"Must have the --stageType option defined if tidal method is"+
                                     ITidal.Method.NON_STATIONARY_FOREMAN.name()+ " !!");
        }

	// --- type for the stage discharge data.
        final String stageTypeCheck= argsMap.get("--stageType");

        if (stageTypeCheck.equals(IStage.Type.DISCHARGE_FROM_MODEL.name())) {
          stageType= IStage.Type.DISCHARGE_FROM_MODEL;
        }

        if (stageType != IStage.Type.DISCHARGE_CFG_STATIC) {
          throw new RuntimeException(mmi+"Only "+IStage.Type.DISCHARGE_CFG_STATIC.name()+
                                   " stage type allowed for now !!");
        }
      }

      // ---
      if (!argsMapKeySet.contains("--tidalConstsInputInfo")) {

        throw new RuntimeException(mmi+
                                   "Must have the --tidalConstsInputInfo option defined if mainPredType == TIDAL !!");
      }

      //final String tidalConstsInputFileFormat= argsMap.get("--tidalConstsInputFileFormat");
      final String tidalConstsInputInfo= argsMap.get("--tidalConstsInputInfo");

      final String checkTidalConstInputFileFmt=
        tidalConstsInputInfo.split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR)[0];

      // ---
      if (!checkTidalConstInputFileFmt.
            equals(ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON.name())) {

        throw new RuntimeException(mmi+"Only the:"+
                                   ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON.name()+
                                   " tidal prediction input file format allowed for now!!");
      }

      //final ITidalIO.WLConstituentsInputFileFormat
      //  tidalConstsInputFileFmt= ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON;

      slog.info(mmi+"Doing the tidal prediction configuration");
      //slog.info(mmi+"debug System.exit(0)");
      //System.exit(0);

      // --- Specific configuration for a tidal prediction.
      super.configureTidalPred(stationIdInfo,
                               startTimeSeconds, //testStartTime, //unixTimeNow,
                               endTimeSeconds,//endPredTime,
                               timeIncrInSeconds,//180L,//180L, //900L, //3600L,//900L,
                               tidalMethod, //ITidal.Method.NON_STATIONARY_FOREMAN,
                               null, //nsTCInputFile
                               tidalConstsInputInfo,
                               //tidalConstsInputFileFmt,//ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON
                               stageType, // --- IStage.Type.DISCHARGE_CFG_STATIC: Stage data taken from inner config DB, ignored by STATIONARY_FOREMAN method
                               null, // --- Stage input data file, ignored by STATIONARY_FOREMAN method
                               null  // --- IStage.Type.DISCHARGE_CFG_STATIC IStageIO.FileFormat is JSON by default, ignored by STATIONARY_FOREMAN method
                              );

    } // --- mainPredType.equals(IWLStationPred.Type.TIDAL.name()

    slog.info(mmi+"end");

    //slog.info(mmi+"debug System.exit(0)");
    //System.exit(0);
  }

  /**
   * comments please!
   */
  final public List<MeasurementCustom> getPredictionData() {
    return super.getPredictionData(); //= super.getAllPredictions();
  }

  /**
   * comments please!
   */
  final public IWLStationPred getAllPredictions() {
    return super.getAllPredictions();
  }

  /**
   * comments please!
   */
  final public void writeIfNeeded(/*@NotNull*/ IWLToolsIO.Format outputFormat) {

    final String mmi= "write: ";

    slog.info(mmi+"start: WLToolsIO.getOutputDirectory()="+WLToolsIO.getOutputDirectory());

    if (WLToolsIO.getOutputDirectory() != null) {

      if (!IWLStationPredIO.allowedFormats.contains(outputFormat.name())) {
        throw new RuntimeException(mmi+"Invalid output file format -> "+outputFormat.name());
      }

      //final String outputFile= this.outputDirectory + File.separator + ;

      slog.info(mmi+"Writing prediction results in "+WLToolsIO.getOutputDirectory()+
                " using "+ outputFormat.name()+" output file format");

      if (outputFormat.name().
            equals(IWLToolsIO.Format.CHS_JSON.name()) ) {

        this.writeJSONOutputFile(this.stationId);

      } else {
        throw new RuntimeException(mmi+"The "+outputFormat.name()+" output file format is not implemented yet !!");
      }

    } else {
      slog.info(mmi+"WLToolsIO.getOutputDirectory() is null, assuming no need to write the WL predictions data");
    }

    slog.info(mmi+"end");

    //slog.info(mmi+"debug System.exit(0)");
    //System.exit(0);
  }
}
