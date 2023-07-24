package ca.gc.dfo.chs.wltools.nontidal.stage;

/**
 * Created on 2023-07-20.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 */

import ca.gc.dfo.chs.wltools.util.Coefficient;

final public class StageCoefficient extends Coefficient {

   protected long timeLagSeconds= 0L;

   public StageCoefficient() {
      this.timeLagSeconds= 0L;
   }

   public StageCoefficient(final long timeLagSeconds) {
      this.timeLagSeconds= timeLagSeconds;
   }

   final public double getTimeLagSeconds() {
      return this.timeLagSeconds;
   }
}
