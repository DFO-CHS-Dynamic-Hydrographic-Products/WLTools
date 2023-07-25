package ca.gc.dfo.chs.wltools.nontidal.stage;

/**
 * Created on 2023-07-20.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 */

import ca.gc.dfo.chs.wltools.util.Coefficient;

/**
 * Comments please!
 */
final public class StageCoefficient extends Coefficient {

   /**
    * Comments please!
    */
   protected long timeLagSeconds= 0L;

   /**
    * Comments please!
    */
   public StageCoefficient() {
      this.timeLagSeconds= 0L;
   }

   /**
    * Comments please!
    */
   public StageCoefficient(final long timeLagSeconds) {
      this.timeLagSeconds= timeLagSeconds;
   }

   /**
    * Comments please!
    */
   final public long getTimeLagSeconds() {
      return this.timeLagSeconds;
   }
}
