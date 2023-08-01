package ca.gc.dfo.chs.wltools.nontidal.stage;

/**
 * Created on 2023-07-20.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 */

import java.util.Set;
import org.slf4j.Logger;
//import java.util.ArrayList;
import org.slf4j.LoggerFactory;

//// ---
//import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
//import ca.gc.dfo.chs.wltools.nontidal.stage.IStageIO;
//import ca.gc.dfo.chs.wltools.nontidal.stage.StageInputData;

/**
 * IO Interface for the WL stage (non-tidal) type.
 */
public class StageDataUnit {

   //private final static String whoAmI= "ca.gc.dfo.chs.wltools.nontidal.stage.StageDataUnit";

  /**
   * log utility.
   */
   //private final static Logger slog = LoggerFactory.getLogger(whoAmI);

   private Double value;
   private Double uncertainty;

   public StageDataUnit(final Double value,final Double uncertainty) {

     this.value= value;
     this.uncertainty= uncertainty;
   }

   final public Double getValue() {
     return this.value;
   }

   final public Double getUncertainty() {
     return this.uncertainty;
   }
}



