package ca.gc.dfo.chs.wltools.nontidal.stage;

/**
 * Created on 2023-07-20.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 */

// ---
import java.util.Map;
import java.util.List;
import org.slf4j.Logger;
import java.util.HashMap;
import org.slf4j.LoggerFactory;

// ---
import ca.gc.dfo.chs.wltools.nontidal.stage.StageDataUnit;
//import ca.gc.dfo.chs.wltools.util.MeasurementCustom;

/**
 * TODO: Add comments please!
 */
final public class StageInputData {

  /**
   * static log utility
   */
   //private final Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * HashMap of MeasurementCustom objects to hold the stage input data
   * for one time stamp
   */
   //protected List<Map<Long,MeasurementCustom>> timeMappedData;
   //protected Map<Long,MeasurementCustom> timeMappedData;
   //protected HashMap<String,MeasurementCustom> dataUnits;
   protected HashMap<String,StageDataUnit> dataUnits;

  /**
   * basic constructor
   */
   public StageInputData() {
      this.dataUnits= null;
   }

  /**
   * comments please.
   */
   public StageInputData(final HashMap<String,StageDataUnit> dataUnits) {
     this.dataUnits= dataUnits;
   }

   //final public MeasurementCustom get(final String coefficientId) {
   //  return this.dataUnits.get(coefficientId);
   //}

  /**
    * comments please.
    */
   //final public HashMap<String,MeasurementCustom> getDataUnits() {
   //  return this.dataUnits;
   //}

  /**
   * comments please.
   */
   final public double getValueForCoeff(final String coefficientId) {
     return this.dataUnits.get(coefficientId).getValue();
   }

  ///**
  // * comments please!
  // */
  // final public double getAtTimeStamp(final long timeStampSeconds) {
  //    return this.timeMappedData.get(timeStampSeconds).getValue();
  // }
}
