package ca.gc.dfo.chs.wltools.nontidal.stage;

/**
 * Created on 2023-07-20.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 */

// ---
import java.util.Set;
import java.util.Map;
import java.util.List;
import org.slf4j.Logger;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NavigableSet;
import java.util.LinkedHashMap;
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
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.wl.IWLLocation;
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
   private final static Logger slog= LoggerFactory.getLogger(whoAmI);

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

     final String mmi= " constructor: ";

     slog.info(mmi+"Start, stationId="+stationId+", type="+type);

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
       final String [] stationIdStrSplit= 
         stationId.split(IWLToolsIO.INPUT_DATA_FMT_SPLIT_CHAR);

       if (stationIdStrSplit.length != 3) {
         throw new RuntimeException(mmi+"ERROR: stationIdStrSplit.length != 3 !!");
       }

       final String stationDischargeClusterDirName= stationIdStrSplit[0];
       final String stationDischargeClusterName= stationIdStrSplit[1];

       final String stationDischargeJsonFileName= stationIdStrSplit[2] +
         IStageIO.LOCATION_DISCHARGE_INPUT_FNAME_SUFFIX + IWLToolsIO.JSON_FEXT;

       //slog.info(mmi+"WLToolsIO.getMainCfgDir()="+WLToolsIO.getMainCfgDir());

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

     //slog.info(mmi+"stageInputDataFileLocal="+stageInputDataFileLocal);
     //slog.info(mmi+"debug System.exit(0)");
     //System.exit(0);

     // --- TODO: implement a switch block to deal with the file formats.
     if (stageInputDataFileFormatLocal != IStageIO.FileFormat.JSON) {

       throw new RuntimeException(mmi+"Invalid input file format -> "+
                                  stageInputDataFileFormatLocal.name());
     }

     slog.info(mmi+"stageInputDataFileLocal="+stageInputDataFileLocal);

     FileInputStream jsonFileInputStream= null;

     try {
       jsonFileInputStream= new
         FileInputStream(stageInputDataFileLocal);

     } catch (FileNotFoundException e) {
       throw new RuntimeException(mmi+e);
     }

     final JsonObject mainJsonStageDataObject=
       Json.createReader(jsonFileInputStream).readObject();

     // --- TODO: add fool-proof checks on all the Json dict keys.
     final List<String> inputDataTimeStampsStrings= new
       ArrayList<String>(mainJsonStageDataObject.keySet());

     final String inputDataTimeStampStr0= inputDataTimeStampsStrings.get(0);

     //slog.info("Stage constructor: inputDataTimeStampStr0="+ inputDataTimeStampStr0);

     final int inputDataTimeStampsStrLen= inputDataTimeStampStr0.length();

     //slog.info("Stage constructor: debug System.exit(0)");
     //System.exit(0);

     final JsonObject stageJsonDataDict0=
       mainJsonStageDataObject.getJsonObject(inputDataTimeStampStr0);//climTimeStampsStrings.get(0));

     //slog.info("Stage constructor: "+ stageJsonDataDict0.toString());

     this.timeStampedInputDataCoeffIdsCheck= stageJsonDataDict0.keySet();

     slog.info(mmi+"this.coefficientsIdsCheck="+
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

     // TODO: Use Instant here instead of Calendar
     // https://github.com/DFO-CHS-Dynamic-Hydrographic-Products/WLTools/issues/15
     Calendar gcld= new GregorianCalendar().
       getInstance(TimeZone.getTimeZone("GMT"));

     // --- Create the HashMap<Long,StageInputData> object
     //     that will store all the input data for the
     //     non-stationary tidal predictions
     this.timeStampedInputData= new HashMap<Long,StageInputData>();

     // ---
     LinkedHashMap<Long,Long> previousValidTimeStamps= new LinkedHashMap<Long,Long>();

     // --- local LinkedHashMap to store the valid stage input data.
     LinkedHashMap<Long,StageInputData>
       validInputDataForTimeInterp= new LinkedHashMap<Long,StageInputData>();

     Long previousValidTimeStamp= null;

     // --- TODO: Implement temporal interpolation when the stage input data
     //     time increments are larger than the time increment wanted for the
     //     predictions.
     for (int tsIter= 0; tsIter<= nbTimeStamps; tsIter++) {

        final Long tsIterSeconds=
          timeStartBufferForLags + tsIter*timeIncrSeconds;

        // --- Need millisecs for gcld.setTimeInMillis method.
        gcld.setTimeInMillis(tsIterSeconds*ITimeMachine.SEC_TO_MILLISEC);

        //--- MUST prepend the dot character by two backslashes
        //    to get the string split operator working properly here
        final String [] tsIterStrSplit= TimeMachine.
          dateTimeString( gcld, false).split("\\"+ITimeMachine.TIMESTAMP_SEP);

        final String tsIterStr= tsIterStrSplit[0]+tsIterStrSplit[1];

        //slog.info(mmi+"tsIterStr="+tsIterStr);

        final StageInputData stageInputData= this.getTimeStampedInputDataAt(tsIterStr,
                                                                            mainJsonStageDataObject,
                                                                            inputDataTimeStampsStrLen,
                                                                            isItClimatologicInput,
                                                                            false);
        // --- stageInputData could be null but ww put it in the
        //     this.timeStampedInputData Map to signal that it needs
        //     to be created later in this method using linear time interpoilation
        this.timeStampedInputData.put(tsIterSeconds,stageInputData);

        //// --- Keep the non-null stageInputData) in the local
        ////     validInputDataForTimeInterp Map to use it for
        ////     missing data time interpolation later in this method
        if (stageInputData != null){

          validInputDataForTimeInterp.put(tsIterSeconds,stageInputData);
          previousValidTimeStamp= tsIterSeconds;

        } else {

          //slog.info(mmi+"missing input data for tsIterStr="+tsIterStr);

          previousValidTimeStamps.put(tsIterSeconds,previousValidTimeStamp);

          //slog.info("Stage constructor: tsIterSeconds="+tsIterSeconds+", previousValidTimeStamp="+previousValidTimeStamp);
          //slog.info("Stage constructor: debug System.exit(0)");
          //System.exit(0);
        }
     }

     slog.info(mmi+"done with getting data from json input file -> "+stageInputDataFileLocal);
     //slog.info("Stage constructor: debug System.exit(0)");
     //System.exit(0);

     // --- Need to have at least the 1st and last StageInputData objects
     //     being valid (i.e. not null)
     if ( this.timeStampedInputData.get(timeStartBufferForLags) == null ) {
       throw new RuntimeException(mmi+" this.timeStampedInputData.get(timeStartBufferForLags) == null !!");
        //System.exit(1);
     }

     if ( this.timeStampedInputData.get(timeEndBufferForLags) == null ) {
       throw new RuntimeException(mmi+" this.timeStampedInputData.get(timeEndBufferForLags) == null !!");
        //System.exit(1);
     }

     // --- Extract the valid time stamps from the validInputDataForTimeInterp
     //     LinkedHashMap (hoping that the increasing order is still there)
     final NavigableSet<Long> validInputTimeStamps= new
       TreeSet<Long>(validInputDataForTimeInterp.keySet());

       //TreeSet((Collection<Long>)validInputDataForTimeInterp.keySet());

     // --- We should have the 1st (smaller) time stamp as the
     //     first item in validInputTimeStamps
     final Long timeStampSeconds0= validInputTimeStamps.first();

     // --- Get all the other time stamps after the first.
     final NavigableSet<Long> remainingTimeStamps=
       validInputTimeStamps.tailSet(timeStampSeconds0,false);

     // --- Extract the second time stamp after the first.
     final Long timeStampSeconds1= remainingTimeStamps.first();

     final Long inputDataTimeIncr= timeStampSeconds1 - timeStampSeconds0;

     slog.info(mmi+"inputDataTimeIncr="+inputDataTimeIncr);

     Long prevTimeStampSeconds= timeStampSeconds0;

     // ---  Verify that the input data time stamps are
     //      consistent in terms of time increments in
     //      increasing order.
     for ( final Long timeStampCheck: remainingTimeStamps ) {

       if ( (timeStampCheck-prevTimeStampSeconds) != inputDataTimeIncr) {
         throw new RuntimeException(mmi+"time increment inconsistency found in input data time stamps !!");
       }

       prevTimeStampSeconds= timeStampCheck;
     }

     slog.info(mmi+"Input data time stamps are consistent in terms of time increments");
     //slog.info(mmi+"timeStampSeconds0="+timeStampSeconds0);
     //slog.info(mmi+"timeStampSeconds1="+timeStampSeconds1);
     //slog.info(mmi+"inputDataTimeIncr="+inputDataTimeIncr);

     if (previousValidTimeStamps.size() > 0) {

        slog.info(mmi+"Need to do time interpolation for stage input data");

        // --- Use a final timeInterpFactor instead of applying
        //     the same division operation (which is costly) in the following loop.
        final double timeInterpFactor= 1.0/(double)inputDataTimeIncr;

        for (final Long missingDataTimeStamp: previousValidTimeStamps.keySet()) {

           final Long prevValidDataTimeStamp= previousValidTimeStamps.get(missingDataTimeStamp);

           final Long nextValidDataTimeStamp= prevValidDataTimeStamp + inputDataTimeIncr;

           //slog.info(mmi+"missingDataTimeStamp="+missingDataTimeStamp+
           //              ", prevValidDataTimeStamp="+prevValidDataTimeStamp+
           //              ", nextValidDataTimeStamp="+nextValidDataTimeStamp);

           final double prevTimeInterpWeight= 1.0 -
             timeInterpFactor * (missingDataTimeStamp-prevValidDataTimeStamp);

           //slog.info(mmi+"prevTimeInterpWeight="+prevTimeInterpWeight);
           //slog.info(mmi+"debug System.exit(0)");
           //System.exit(0);

           final StageInputData prevStageInputData=
              this.timeStampedInputData.get(prevValidDataTimeStamp);

           final StageInputData nextStageInputData=
              this.timeStampedInputData.get(nextValidDataTimeStamp);

           final HashMap<String,StageDataUnit>
              stageInputDataUnits= new HashMap<String,StageDataUnit>();

           for (final String stageDataCoeffId: prevStageInputData.getCoefficientIds()) {

              final double prevValidDataValue=
                prevStageInputData.getValueForCoeff(stageDataCoeffId);

              final double nextValidDataValue=
                nextStageInputData.getValueForCoeff(stageDataCoeffId);

              final double timeInterpValue= prevTimeInterpWeight *
                 prevValidDataValue + (1.0 -prevTimeInterpWeight) * nextValidDataValue;

              //slog.info(mmi+"stageDataCoeffId="+stageDataCoeffId+", prevValidDataValue="+prevValidDataValue);
              //slog.info(mmi+"stageDataCoeffId="+stageDataCoeffId+", nextValidDataValue="+nextValidDataValue);
              //slog.info(mmi+"stageDataCoeffId="+stageDataCoeffId+", timeInterpValue="+timeInterpValue);
              //slog.info(mmi+"debug System.exit(0)");
              //System.exit(0);

              // --- Create the new StageDataUnit for this interpolated stage coefficient
              stageInputDataUnits.put(stageDataCoeffId,
                                      new StageDataUnit(timeInterpValue,0.0));
           }

           // --- Create and add the new StageInputData object with its time interpolated data
           //     with the other StageInputData objects in this.timeStampedInputData
           this.timeStampedInputData.put(missingDataTimeStamp,
                                         new StageInputData(stageInputDataUnits));

           //final StageInputData interpStageInputData= new HashMap<String,>;

           //slog.info(mmi+"debug System.exit(0)");
           //System.exit(0);
        }

        slog.info(mmi+"Done with time interpolation for stage input data");
     }

     //slog.info(mmi+"debug System.exit(0)");
     //System.exit(0);

     // --- Check if we need to do time interpolation for the stage data.
     //for (final Long tsIterSeconds: this.timeStampedInputData.keySet()) {
     //   final StageInputData checkStageInputData= this.timeStampedInputData.get(tsIterSeconds);
     //   if (checkStageInputData == null) {
     //   }
     //   final StageInputData previousValidStageInputData= this.timeStampedInputData.get(tsIterSeconds)
     //}

     try {
       jsonFileInputStream.close();

     } catch (IOException e) {
       throw new RuntimeException(mmi+e);
     }

     slog.info(mmi+"end");

     //slog.info(mmi+"debug System.exit(0)");
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
