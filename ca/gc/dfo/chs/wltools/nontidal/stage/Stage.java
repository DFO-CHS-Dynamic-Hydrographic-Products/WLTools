package ca.gc.dfo.chs.wltools.stage;

/**
 * Created on 2023-07-20.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 */

// ---
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ---
import ca.gc.dfo.chs.wltools.util.Coefficient;

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
public class Stage implements IStage {

  /**
   * static log utility.
   */
   private static final Logger staticLogger = LoggerFactory.getLogger("Stage");

  /**
   * List of Coefficients (can have only one item)
   */
   protected List<Coefficient> coefficients;

  /**
   * basic constructor
   */
   public Stage() {
      this.coefficients= null;
   }

  /**
   * constructor taking a List<Coefficient> arg.
   */
   public Stage(final List<Coefficient> coefficients) {
      this.coefficients= coefficients;
   }

   //final public List<Coefficient> getCoefficients() {
   //   return this.coefficients;
   //}

  /**
   * method that returns the stage calculation
   */
   //final double evaluate(final List<Double> factorDValues) {
   final public double evaluate( /*@NotNull @Size(min = 1)*/ final double [] factorDValues, final boolean addC0) {

      // --- Assuming that factorDValues is not null
      //     here if @NotNull is not activated.

      // --- Can renove the following if block check if @Size(min = 1) is activated
      if (factorDValues.length == 0) {
         staticLogger.error("Stage.evaluate(): factorDValues.length == 0");
         throw new RuntimeException("Stage.evaluate()");
      }

      //if (factorDValues.size() != this.coefficients.size()) {
      if (factorDValues.length + 1 != this.coefficients.size()) {
         staticLogger.error("Stage.evaluate(): factorDValues.size() != this.coefficients.size()");
         throw new RuntimeException("Stage.evaluate()");
      }

      double retAcc= 0.0;

      // --- No unsigned int in Java unfortunately.
      for (int cf= 0; cf < factorDValues.length; cf++) {
         retAcc += this.coefficients.get(cf+1).getValue() * factorDValues[cf];
      }

      // --- Could have to add the C0 coefficient after all the C1, C2, ..., CN
      //     terms have been added together
      return addC0 ? retAcc + this.coefficients.get(0).getValue() : retAcc;
   }

   // --- TODO: Implement evaluateWithUncertainties
   //final double [] evaluateWithUncertainties( /*@NotNull @Size(min = 1)*/ final double [] factorDValues) {
   //    double [] retAcc = {0.0, 0.0, 0.0};
   //    return retAcc;
   //}
}