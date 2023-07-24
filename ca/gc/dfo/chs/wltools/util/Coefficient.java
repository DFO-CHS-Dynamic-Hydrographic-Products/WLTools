package ca.gc.dfo.chs.wltools.util;

/**
 * Created on 2023-07-20.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 */

public class Coefficient {

   protected double value;
   protected double uncertainty;

   public Coefficient() {
      this.value= this.uncertainty= 0.0;
   }

   public Coefficient(final double value, final double uncertainty) {
      this.value= value;
      this.uncertainty= uncertainty;
   }

   final public double getValue() {
      return this.value;
   }

   final public double getUncertainty() {
      return this.uncertainty;
   }

   final public void setValue(final double value) {
      this.value= value;
   }

   final public void setUncertainty(final double uncertainty) {
      this.uncertainty= uncertainty;
   }
}
