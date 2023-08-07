package ca.gc.dfo.chs.wltools.nontidal.stage;

/**
 * Created on 2023-07-20.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 */

import ca.gc.dfo.chs.wltools.nontidal.stage.StageDataUnit;

/**
 * Comments please!
 */
final public class StageCoefficient extends StageDataUnit {

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
   public StageCoefficient(final double value) {

      super(value,0.0);
      this.timeLagSeconds= 0L;
   }

   /**
    * Comments please!
    */
   public StageCoefficient(final double value, final double uncertainty) {

      super(value,uncertainty);
      this.timeLagSeconds= 0L;
   }

   /**
    * Comments please!
    */
   public StageCoefficient(final double value, final double uncertainty, final long timeLagSeconds) {

      super(value,uncertainty);

      this.timeLagSeconds= timeLagSeconds;
   }

   /**
    * Comments please!
    */
   final public long getTimeLagSeconds() {
      return this.timeLagSeconds;
   }
}
