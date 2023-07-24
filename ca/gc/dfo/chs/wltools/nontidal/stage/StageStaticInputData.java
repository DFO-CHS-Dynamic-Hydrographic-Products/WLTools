package ca.gc.dfo.chs.wltools.nontidal.stage;

/**
 * Created on 2023-07-20.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 */

// ---
import java.util.Map;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ---
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;

/**
 * TODO: Add comments please!
 */
final public class StageStaticInputData {

  /**
   * static log utility
   */
   private final Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * List of time (seconds since epoch) mapped MeasurementCustom objects.
   */
   protected List<Map<Long,MeasurementCustom>> timeMappedData;

  /**
   * basic constructor
   */
   public StageStaticInputData() {
      this.timeMappedData= null;
   }

  /**
   * constructor taking a List<Coefficient> arg.
   */
   public StageStaticInputData(final List<Map<Long,MeasurementCustom>> timeMappedData) {
      this.timeMappedData= timeMappedData;
   }

}
