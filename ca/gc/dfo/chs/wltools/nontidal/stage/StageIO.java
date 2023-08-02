package ca.gc.dfo.chs.wltools.nontidal.stage;

/**
 * Created on 2023-07-20.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 */

import java.util.Set;
import org.slf4j.Logger;
import java.util.HashMap;
//import java.util.ArrayList;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

//// ---
//import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStageIO;
import ca.gc.dfo.chs.wltools.nontidal.stage.StageDataUnit;
import ca.gc.dfo.chs.wltools.nontidal.stage.StageInputData;

/**
 * IO Interface for the WL stage (non-tidal) type.
 */
public class StageIO implements IStageIO {

   private final static String whoAmI= "ca.gc.dfo.chs.wltools.nontidal.stage.StageIO";

  /**
   * log utility.
   */
   private final static Logger slog = LoggerFactory.getLogger(whoAmI);

   public StageIO() {}

  /**
   * comments please!
   */
   final protected StageInputData getTimeStampedInputDataAt(/*NotNull*/final String tsStr,
                                                            /*NotNull*/final JsonObject mainJsonStageDataObject,
                                                            /*NotNull*/final int inputDataTimeStampsStrLen,
                                                            /*NotNull*/final boolean isItClimatologicInput,
                                                            /*NotNull*/final boolean checkForMissingData ) {
     final String mmi= "getTimeStampedInputDataAt: ";

     //slog.info(mmi+"start, tsStr="+tsStr+
     //          ", inputDataTimeStampsStrLen="+inputDataTimeStampsStrLen+
     //          ", isItClimatologicInput="+isItClimatologicInput);

     // --- inputDataTimeStampsStrLen MUST obviously be > 0 here
     String localTsStr= new String(tsStr).substring(0,inputDataTimeStampsStrLen);

     //slog.info("getTimeStampedInputDataAt: localTsStr="+localTsStr);

     // --- Climatologic DB data have year (YYYY) string in its
     //     strings timestamped data so just replace the 1st four
     //     characters from the localTsStr.
     if (isItClimatologicInput) {

       localTsStr= CLIMATO_YEAR_PLACEHOLDER + tsStr.
          substring(CLIMATO_YEAR_PLACEHOLDER.length(),localTsStr.length());
     }

     //slog.info(mmi+"localTsStr aft if="+localTsStr);

     final JsonObject tmpInputDataDict=
       mainJsonStageDataObject.getJsonObject(localTsStr);

     // --- Could have to check for missing data.
     if (checkForMissingData && tmpInputDataDict == null) {

       throw new RuntimeException(mmi+
                                  "tmpInputDataDict == null !!, timestamp string ->"+
                                  localTsStr+" not found in JsonObject");
     }

     // --- Return null if we do not need to
     //     check for missing data and if
     //     tmpInputDataDict is itself null
     //     (which means that the input data
     //      is missing to this timestamp
     StageInputData stageInputDataRet= null;

     // ---
     if (tmpInputDataDict != null) {

       final Set<String> stageInputDataCoeffIds= tmpInputDataDict.keySet();

       //slog.info("getTimeStampedInputDataAt: inputDataDict="+tmpInputDataDict.toString());
       //slog.info("getTimeStampedInputDataAt: stageInputDataCoeffIds="+stageInputDataCoeffIds.toString());

       final HashMap<String,StageDataUnit>
         stageInputDataUnits= new HashMap<String,StageDataUnit>();

       for (final String stageDataCoeffId: stageInputDataCoeffIds) {

         final double stageValue= tmpInputDataDict.
           getJsonNumber(stageDataCoeffId).doubleValue();

         //slog.info("getTimeStampedInputDataAt: debug stageDataCoeffId="+stageDataCoeffId);
         //slog.info("getTimeStampedInputDataAt: debug stageValue="+stageValue);
         //slog.info("getTimeStampedInputDataAt: debug System.exit(0)");
         //System.exit(0);

         stageInputDataUnits.put( stageDataCoeffId,
                                  new StageDataUnit(stageValue,0.0));
       }

       // --- Create the StageInputData that will be returned with
       //     the stageInputDataUnits HashMap<String,StageDataUnit> object
       stageInputDataRet= new StageInputData(stageInputDataUnits);
     }

     //slog.info(mmi+"end");
     //slog.info("getTimeStampedInputDataAt: debug System.exit(0)");
     //System.exit(0);

     return stageInputDataRet;
   }
}



