package ca.gc.dfo.chs.wltools.nontidal.stage;

/**
 * Created on 2023-07-20.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 */

// ---
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import org.slf4j.Logger;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

// ---
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.TimeZone;
import java.util.Calendar;
import java.util.GregorianCalendar;

// ---
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.util.TimeMachine;
import ca.gc.dfo.chs.wltools.util.ITimeMachine;

/**
 * class for the WL stage (non-tidal) type. The WL values are calculated
 * with a "stage" equation in which the constant coefficients are normally
 * coming from a linear (OLS, ridge) regression using river discharges and-or
 * possibly atmospheric parameters (normally wind velocity, wind direction
 * and atmos pressure). The stage equation is a simple polynomial and has a
 * minimum of one constant coefficient (c0) and is of the form (for N+1 coefficients):
 *
 * C0 + C1*<C1 related value> + C2*<C2 related value> + ... + CN*<CN related value>
 *
 * and the <CN related value> are provided by the client methods that are using this method.
 *
 * This class is inherited by the (stationary) tidal-astro related classes to be used
 * as the WL Z0 (average) of the tidal-astro part. Obviously in this case, the List<WLZE>
 * attribute has only one item.
 */
final public class Stage extends StageIO implements IStage {//, IStageIO {

   private final static String whoAmI= "ca.gc.dfo.chs.wltools.nontidal.stage.Stage";

  /**
   * log utility.
   */
   private final static Logger slog = LoggerFactory.getLogger(whoAmI);

  /**
   * List of Map object(s) of StageCoefficient object(s).
   */
   protected HashMap<String,StageCoefficient> coefficients;

  /**
   * HashMap object(s) of time stamped StageInputData object(s).
   */
   //protected HashMap<String,StageInputData> inputData;
   protected HashMap<Long, StageInputData> timeStampedInputData;

   protected Set<String> timeStampedInputDataCoeffIdsCheck;

  /**
   * basic constructor
   */
   public Stage() {
     this.coefficients= null;
     this.timeStampedInputData= null;
   }

  /**
   * Comments please!
   */
   public Stage(/*NotNull*/final String stationId,
                /*NotNull*/final IStage.Type type,
                /*NotNull*/final Long timeStartSeconds,
                /*NotNull*/final Long timeEndSeconds,
                /*NotNull*/final Long timeIncrSeconds,
                final String stageInputDataFile,
                final IStageIO.FileFormat stageInputDataFileFormat) {

     this();

     slog.info("Stage constructor: start");

     String stageInputDataFileLocal= stageInputDataFile; // --- Could be null
     IStageIO.FileFormat stageInputDataFileFormatLocal= stageInputDataFileFormat; // --- Could be null

     boolean isItClimatologicInput= false;

     if (type == IStage.Type.DISCHARGE_CFG_STATIC) {

       isItClimatologicInput= true;

       // --- Just ignore the stageInputDataFile and the stageInputDataFileFormat here since the IStage.DISCHARGE_CFG_STATIC
       //     implies that we take the stage input discharge data from the package internal DB which implies that the input
       //      file format is Json
       stageInputDataFileFormatLocal= IStageIO.FileFormat.JSON;

       //--- Get the path of the stage input discharge data from the package DB using the stationId String
       //    and the static WLToolsIO.mainCfgDir
       final String [] stationIdStrSplit= stationId.split(IStageIO.STATION_ID_SPLIT_CHAR);

       final String stationDischargeClusterDirName= stationIdStrSplit[0];
       final String stationDischargeClusterName= stationIdStrSplit[1];
       final String stationDischargeJsonFileName= stationIdStrSplit[2] + IStageIO.STATION_INFO_JSON_FNAME_EXT;

       slog.info("Stage constructor: WLToolsIO.getMainCfgDir()="+WLToolsIO.getMainCfgDir());

       //final String dischInputFileInDB= WLToolsIO.getMainCfgDir() +
       stageInputDataFileLocal= WLToolsIO.getMainCfgDir() +
                                WLToolsIO.PKG_CFG_TIDAL_NON_STATIONARY_DIR +
                                stationDischargeClusterDirName +
                                WLToolsIO.PKG_CFG_TIDAL_NON_STATIONARY_STAGE_DISCH_CLUSTERS_DIRNAME +
                                stationDischargeClusterName +
                                WLToolsIO.PKG_CFG_TIDAL_NON_STATIONARY_STAGE_CLIM_DISCH_DIRNAME +
                                stationDischargeJsonFileName;

       //slog.info("Stage constructor: stageInputDataFileLocal="+stageInputDataFileLocal);
     }

     ///slog.info("Stage constructor: stageInputDataFileLocal="+stageInputDataFileLocal);
     //slog.info("Stage constructor: debug System.exit(0)");
     //System.exit(0);

     // --- TODO: implement a switch block to deal with the file formats.
     if (stageInputDataFileFormatLocal != IStageIO.FileFormat.JSON) {
       throw new RuntimeException("Stage constructor: Invalid input file format -> "+stageInputDataFileFormatLocal.name());
     }

     slog.info("Stage constructor: stageInputDataFileLocal="+stageInputDataFileLocal);

     FileInputStream jsonFileInputStream= null;

     try {
       jsonFileInputStream= new FileInputStream(stageInputDataFileLocal);

     } catch (FileNotFoundException e) {
       throw new RuntimeException(e);
     }

     final JsonObject mainJsonStageDataObject=
       Json.createReader(jsonFileInputStream).readObject();

     // --- TODO: add fool-proof checks on all the Json dict keys.
     final List<String> inputDataTimeStampsStrings=
       new ArrayList<String>(mainJsonStageDataObject.keySet());

     final String inputDataTimeStampStr0= inputDataTimeStampsStrings.get(0);

     //slog.info("Stage constructor: inputDataTimeStampStr0="+ inputDataTimeStampStr0);

     final int inputDataTimeStampsStrLen= inputDataTimeStampStr0.length();

     //slog.info("Stage constructor: debug System.exit(0)");
     //System.exit(0);

     final JsonObject stageJsonDataDict0=
       mainJsonStageDataObject.getJsonObject(inputDataTimeStampStr0);//climTimeStampsStrings.get(0));

     //slog.info("Stage constructor: "+ stageJsonDataDict0.toString());

     this.timeStampedInputDataCoeffIdsCheck= stageJsonDataDict0.keySet();

     slog.info("Stage constructor: this.coefficientsIdsCheck="+
               this.timeStampedInputDataCoeffIdsCheck.toString());
     //slog.info("Stage constructor: debug System.exit(0)");
     //System.exit(0);

     //--- timeStartSeconds, timeEndSeconds and timeIncrSeconds MUST
     //    have been validated before we get here.
     final Long timeStartBufferForLags= timeStartSeconds -
        NUM_DAYS_BUFFER_FOR_LAGS * ITimeMachine.SECONDS_PER_DAY;

     final Long timeEndBufferForLags= timeEndSeconds +
        NUM_DAYS_BUFFER_FOR_LAGS * ITimeMachine.SECONDS_PER_DAY;

     // --- Check nbTimeStamps value here >> Must be at least 1
     final int nbTimeStamps=
       (int)((timeEndBufferForLags - timeStartBufferForLags)/timeIncrSeconds);

     //List<String> tmpTimeStampsList=
     //  new ArrayList<String>(nbTimeStamps);

     Calendar gcld= new GregorianCalendar().
       getInstance(TimeZone.getTimeZone("GMT"));

     this.timeStampedInputData= new HashMap<Long,StageInputData>();

     // --- TODO: Implement temporal interpolation when the stage input data
     //     time increments are larger than the time increment wanted for the
     //     predictions.
     for (int tsIter= 0; tsIter< nbTimeStamps; tsIter++) {

        final Long tsIterSeconds=
          timeStartBufferForLags + tsIter*timeIncrSeconds;

        // --- Need millisecs for gcld.setTimeInMillis method.
        gcld.setTimeInMillis(tsIterSeconds*ITimeMachine.SEC_TO_MILLISEC);

        //--- MUST prepend the dot character by two backslashes
        //    to get the string split operator working properly here
        final String [] tsIterStrSplit= TimeMachine.
          dateTimeString( gcld, false).split("\\"+ITimeMachine.TIMESTAMP_SEP);

        final String tsIterStr= tsIterStrSplit[0]+tsIterStrSplit[1];

        slog.info("Stage constructor: tsIterStr="+tsIterStr);

        final StageInputData stageInputData= this.getTimeStampedInputDataAt(tsIterStr,
                                                                            mainJsonStageDataObject,
                                                                            inputDataTimeStampsStrLen,
                                                                            isItClimatologicInput,
                                                                            false);
        if (stageInputData==null){
          slog.info("Stage constructor: missing input data for tsIterStr="+tsIterStr);
          slog.info("Stage constructor: debug System.exit(0)");
          System.exit(0);
        }

        this.timeStampedInputData.put(tsIterSeconds,stageInputData);

        // --- Join the two Strings of tsIterStrSplit here.
        //tmpTimeStampsList.add(tsIter, tsIterStrSplit[0]+tsIterStrSplit[1]);

        //slog.info("Stage constructor: tmpTimeStampsList.get(0)="+tmpTimeStampsList.get(0));
        //slog.info("Stage constructor: debug System.exit(0)");
        //System.exit(0);
     }

     slog.info("Stage constructor: done with getting data from json input file -> "+stageInputDataFileLocal);
     //slog.info("Stage constructor: debug System.exit(0)");
     //System.exit(0);

     // --- Check if we need to do time interpolation for the stage data.

     try {
       jsonFileInputStream.close();

     } catch (IOException e) {
       throw new RuntimeException(e);
     }

     slog.info("Stage constructor: end");
     //slog.info("Stage constructor: debug System.exit(0)");
     //System.exit(0);

   }

  /**
   * Comments please!
   */
   final public StageInputData getInputDataAtTimeStamp(final Long timeStampSeconds) {
     return this.timeStampedInputData.get(timeStampSeconds);
   }

  ///**
  // */ Comments please!
  // */
  // public Stage(final HashMap<String,StageInputData> inputData,
  //              final HashMap<String,StageCoefficient> coefficients) {
  //    this.coefficients= coefficients;
  //    this.timeStampedInputData= timeStampedInputData;
  // }

  /**
   * Comments please!
   */
   final public Stage setCoeffcientsMap(/*@NotNull*/ final JsonObject stageJsonObj) {

      final String mmi= "setCoeffcientsMap: ";

      slog.info(mmi+"start");

      this.coefficients= new HashMap<String,StageCoefficient>();;

      // --- Set the zero'th order Stage coefficient
      this.coefficients.put( STAGE_JSON_ZEROTH_ORDER_KEY,
          new StageCoefficient(stageJsonObj.getJsonNumber(STAGE_JSON_ZEROTH_ORDER_KEY).doubleValue()) );

      slog.info(mmi+"Zero'th order coefficient value="+
                this.coefficients.get(STAGE_JSON_ZEROTH_ORDER_KEY).getValue());

      final Set<String> coefficientsIdsSet= stageJsonObj.keySet();

      // ---- coefficientsIdsSet.size() MUST be odd here!
      final int nbNonZeroThOrderCoeffs= (coefficientsIdsSet.size()-1)/2;

      // --- nbNonZeroThOrderCoeffs must be even here.
      if (nbNonZeroThOrderCoeffs % 2 !=0 ) {
         throw new RuntimeException(mmi+"nbNonZeroThOrderCoeffs % 2 !=0");
      }

      // ---
      for (Integer coeffOrder= 1; coeffOrder<= nbNonZeroThOrderCoeffs; coeffOrder++) {

         final String coeffOrderKey=
           IStageIO.STAGE_JSON_DN_KEYS_BEG + coeffOrder.toString();

         final String coeffFactorKey= coeffOrderKey +
           IStageIO.STAGE_JSON_KEYS_SPLIT + IStageIO.STAGE_JSON_DNFCT_KEYS;

         final String coeffHoursLagKey= coeffOrderKey +
           IStageIO.STAGE_JSON_KEYS_SPLIT + IStageIO.STAGE_JSON_DNLAG_KEYS;

          //slog.info("coeffFactorKey="+coeffFactorKey);
          //slog.info("coeffHoursLagKey="+coeffHoursLagKey);

          final double coeffFactorValue=
            stageJsonObj.getJsonNumber(coeffFactorKey).doubleValue();

          final long coeffHoursLagValue=
            stageJsonObj.getJsonNumber(coeffHoursLagKey).longValue();

          //slog.info("setCoeffcientsMap: coeffFactorKey="+coeffFactorKey+", coeffFactorValue="+coeffFactorValue);
          //slog.info("setCoeffcientsMap: coeffHoursLagKey="+coeffHoursLagKey+",coeffHoursLagValue="+coeffHoursLagValue);

          // --- stage data lags are defined in hours in the input file.
          final StageCoefficient stageCoefficient= new
            StageCoefficient(coeffFactorValue, 0.0, coeffHoursLagValue*ITimeMachine.SECONDS_PER_HOUR);

          // --- uncertaintu is 0.0 for now.
          this.coefficients.put( coeffOrderKey, stageCoefficient);
      }

      final Set<String> checkThisCoeffsIdsSet= this.coefficients.keySet();

      // --- Need to check for cofficients ids. conststency with
      //     the time varying stage input data:
      for (final String coeffIdCheck: this.timeStampedInputDataCoeffIdsCheck) {

        if ( !checkThisCoeffsIdsSet.contains(coeffIdCheck) ) {
          throw new RuntimeException(mmi+"Inconsistency between non-stationary tidal stage coefficients ids. and time varying stage input data coefficient name ids. !!");
        }
      }

      slog.info(mmi+"coefficientsIds are consistent with time varying input stage data");
      //slog.info(mmi+"debug System.exit(0)");
      //System.exit(0);

      slog.info(mmi+"end");
      //slog.info("setCoeffcientsMap: debug System.exit(0)");
      //System.exit(0);

      return this;
   }

   //final public Stage setCoeffcientsMap(final Map<String,StageCoefficient> coefficients) {
   //   this.coefficients= coefficients;
   //   return this;
   //}

   //final public Stage setInputDataMap(final HashMap<String,StageInputData> inputData) {
   //   this.inputData= inputData;
   //   return this;
   //}

  /**
   * Comments please!
   */
   final public HashMap<String,StageCoefficient> getCoeffcientsMap() {
     return this.coefficients;
   }

  /**
   * Comments please!
   */
   final public HashMap<Long,StageInputData> getTimeStampedInputData() {
     return this.timeStampedInputData;
   }

   //final public List<Coefficient> getCoefficients() { return this.coefficients;
   //}

  /**
   * method that returns the stage calculation using a client defined double [] factorDValues.
   * NOTE: The factorDValues[0] value should be either 1.0 or 0.0 since the 1st coefficient
   *       is used for the polynomial order 0 (p= s0*factorDValues[0] + s1*factorDValues[1] + ... sN*factorDValues[N])
   */
   //final double evaluate(final List<Double> factorDValues) {
   //final public double evaluate( /*@NotNull @Size(min = 1)*/ final double [] factorDValues) { //, final boolean addC0) {
   //
   //   // --- Assuming that factorDValues is not null
   //   //     here if @NotNull is not activated.
   //
   //   // --- Can renove the following if block check if @Size(min = 1) is activated
   //   if (factorDValues.length == 0) {
   //      this.log.error("Stage.evaluate(): factorDValues.length == 0");
   //      throw new RuntimeException("Stage.evaluate()");
   //   }
   //
   //   if (factorDValues.length != this.coefficients.size()) {
   //   //if (factorDValues.length + 1 != this.coefficients.size()) {
   //      this.log.error("Stage.evaluate(): factorDValues.size() != this.coefficients.size()");
   //      throw new RuntimeException("Stage.evaluate()");
   //   }
   //
   //   double retAcc= 0.0;
   //
   //   int cf= 0;
   //   // --- No unsigned int in Java unfortunately.
   //   //for (int cf= 0; cf < factorDValues.length; cf++) {
   //   for (final String stageId: this.coefficients.keySet()) {
   //
   //      retAcc += this.coefficients.get(stageId).getValue() * factorDValues[cf++];
   //      //cf++;
   //   }
   //
   //   return retAcc;
   //   // --- Could have to add the C0 coefficient after all the C1, C2, ..., CN
   //   //     terms have been added together
   //   //return addC0 ? retAcc + this.coefficients.get(0).getValue() : retAcc;
   //}

   // --- TODO: Implement evaluateWithUncertainties
   //final double [] evaluateWithUncertainties( /*@NotNull @Size(min = 1)*/ final double [] factorDValues) {
   //    double [] retAcc = {0.0, 0.0, 0.0};
   //    return retAcc;
   //}
}
