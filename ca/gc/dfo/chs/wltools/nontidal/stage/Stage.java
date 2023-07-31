package ca.gc.dfo.chs.wltools.nontidal.stage;

/**
 * Created on 2023-07-20.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 */

// ---
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;


// ---
//import ca.gc.dfo.chs.wltools.util.Coefficient;

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
final public class Stage implements IStage, IStageIO {

   private final static String whoAmI="ca.gc.dfo.chs.wltools.nontidal.stage.Stage";

  /**
   * log utility.
   */
   private final static Logger slog = LoggerFactory.getLogger(whoAmI);

  /**
   * List of Map object(s) of StageCoefficient object(s).
   */
   protected HashMap<String,StageCoefficient> coefficients;

  /**
   * List of Map object(s) of StageInputData object(s).
   */
   protected HashMap<String,StageInputData> inputData;

  /**
   * basic constructor
   */
   public Stage() {
      this.coefficients= null;
      this.inputData= null;
   }

  /**
   * Comments please!
   */
   public Stage(/*NotNull*/final IStage.Type type,
                final String stageInputDataFile,
                final IStageIO.FileFormat stageInputDataFileFormat) {

     this();

   }


  /**
   * Comments please!
   */
   public Stage(final HashMap<String,StageInputData> inputData,
                final HashMap<String,StageCoefficient> coefficients) {

      this.coefficients= coefficients;
      this.inputData= inputData;
   }

  /**
   * Comments please!
   */
   final public Stage setCoeffcientsMap(/*@NotNull*/ final JsonObject stageJsonObj) {

      slog.info("setCoeffcientsMap: start");

      this.coefficients= new HashMap<String,StageCoefficient>();;

      // --- Set the zero'th order Stage coefficient
      this.coefficients.put( STAGE_JSON_ZEROTH_ORDER_KEY,
          new StageCoefficient(stageJsonObj.getJsonNumber(STAGE_JSON_ZEROTH_ORDER_KEY).doubleValue()) );

      slog.info("setCoeffcientsMap: Zero'th order coefficient value="+this.coefficients.get(STAGE_JSON_ZEROTH_ORDER_KEY).getValue());

      final Set<String> coefficientsIdsSet= stageJsonObj.keySet();

      // ---- coefficientsIdsSet.size() MUST be odd here!
      final int nbNonZeroThOrderCoeffs= (coefficientsIdsSet.size()-1)/2;

      // --- nbNonZeroThOrderCoeffs must be even here.
      if (nbNonZeroThOrderCoeffs % 2 !=0 ) {
         throw new RuntimeException("nbNonZeroThOrderCoeffs % 2 !=0");
      }

      for (Integer coeffOrder= 1; coeffOrder<= nbNonZeroThOrderCoeffs; coeffOrder++) {

         final String coeffOrderKey=
            IStageIO.STAGE_JSON_DN_KEYS_BEG + coeffOrder.toString();

         final String coeffFactorKey= coeffOrderKey +
            IStageIO.STAGE_JSON_KEYS_SPLIT + IStageIO.STAGE_JSON_DNFCT_KEYS;

         final String coeffHoursLagKey= coeffOrderKey +
            IStageIO.STAGE_JSON_KEYS_SPLIT + IStageIO.STAGE_JSON_DNLAG_KEYS;

          //this.log.info("coeffFactorKey="+coeffFactorKey);
          //this.log.info("coeffHoursLagKey="+coeffHoursLagKey);

          final double coeffFactorValue= stageJsonObj.getJsonNumber(coeffFactorKey).doubleValue();
          final long   coeffHoursLagValue= stageJsonObj.getJsonNumber(coeffHoursLagKey).longValue();

          slog.info("setCoeffcientsMap: coeffFactorKey="+coeffFactorKey+", coeffFactorValue="+coeffFactorValue);
          slog.info("setCoeffcientsMap: coeffHoursLagKey="+coeffHoursLagKey+",coeffHoursLagValue="+coeffHoursLagValue);

          // --- uncertaintu is 0.0 for now.
          this.coefficients.put( coeffOrderKey,
                                 new StageCoefficient(coeffFactorValue, 0.0, coeffHoursLagValue));
      }

      slog.info("setCoeffcientsMap: end");

      return this;
   }

   //final public Stage setCoeffcientsMap(final Map<String,StageCoefficient> coefficients) {
   //   this.coefficients= coefficients;
   //   return this;
   //}

   final public Stage setInputDataMap(final HashMap<String,StageInputData> inputData) {

      this.inputData= inputData;
      return this;
   }

  /**
   * Comments please!
   */
   final public HashMap<String,StageCoefficient> getCoeffcientsMap() {
      return this.coefficients;
   }

  /**
   * Comments please!
   */
   final public HashMap<String,StageInputData> getInputDataMap() {
     return this.inputData;
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
