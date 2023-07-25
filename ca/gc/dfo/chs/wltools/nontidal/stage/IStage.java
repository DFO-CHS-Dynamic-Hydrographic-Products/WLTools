package ca.gc.dfo.chs.wltools.nontidal.stage;

/**
 * Created on 2023-07-20.
 * @author Gilles Mercier (DFO-CHS-ENAV-DHP)
 */

//import java.util.List;

//// ---
//import ca.gc.dfo.chs.wltools.util.Coefficient;

/**
 * Interface for the WL stage (non-tidal) type. The WL values are calculated
 * with a "stage" equation in which the constant coefficients are normally
 * coming from a linear (OLS, ridge) regression using river discharges and-or
 * possibly atmospheric parameters (normally wind velocity, wind direction
 * and atmos pressure).
 */
public interface IStage {

   enum StageDataType {
      DISCHARGE,
      ATMOS,
      DISCHARGE_AND_ATMOS
   }

   enum StageDataStatus {
      FROM_MODEL,
      CLIMATOLOGY
   }

   ///**
   // * evaluate the stage polynomial with the factor values provided by
   // * the client method that is using this method.
   // */
   //double evaluate( /*@NotNull @Size(min = 1)*/ final double [] factorDValues); //, final boolean addC0);O
   //double [] evaluateWithUncertainties( /*@NotNull @Size(min = 1)*/ final double [] factorDValues);
}



