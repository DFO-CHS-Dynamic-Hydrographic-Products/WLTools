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

   // --- Defined a default buffer in days to be able to deal
   //     with stage data lags
   int NUM_DAYS_BUFFER_FOR_LAGS= 15;

   enum Type {
     DISCHARGE_CFG_STATIC, // --- Stage data is "static" in time (i.e same data each year) and is defined inside the inner config DB.
     DISCHARGE_FROM_MODEL, // --- Stage data is coming from an external model results (like ECCC DHPS or EHPS models)
     ATMOSPHERIC, // --- Taken from an atmos. model by definition.
     HYBRID       // --- DISCHARGE and ATMOSPHERIC
   }

   //enum Origin {
   //  CFG_STATIC, // --- Stage data is "static" in time (i.e same data each year) and is defined inside the inner config DB.
   //  FROM_MODEL, // --- Stage data is coming from an external model results (like ECCC DHPS or EHPS)
   //  HYBRID      // --- MIx of CFG_STATIC and FROM_MODEL.
   ///}

   ///**
   // * evaluate the stage polynomial with the factor values provided by
   // * the client method that is using this method.
   // */
   //double evaluate( /*@NotNull @Size(min = 1)*/ final double [] factorDValues); //, final boolean addC0);O
   //double [] evaluateWithUncertainties( /*@NotNull @Size(min = 1)*/ final double [] factorDValues);
}



