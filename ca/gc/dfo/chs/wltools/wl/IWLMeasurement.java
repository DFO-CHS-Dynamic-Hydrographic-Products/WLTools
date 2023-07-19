//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools.wl;

/**
 *
 */

//---

//import javax.validation.constraints.NotNull;

/**
 * interface for Measurement class wrapping.
 */
public interface IWLMeasurement extends IWL {
  
  /**
   * @return The wrapped Measurement seconds since the epoch(normally in UTC)
   */
  long seconds();
  
  /**
   * @return The wrapped Measurement object water leval uncertainty in double precision.
   */
  double getDoubleZError();
  
  /**
   * @return The wrapped Measurement object water leval in double precision.
   */
  double getDoubleZValue();
  
  //--- The following four methods declarations are kept for possible future usage:
  //abstract public double getZDoubleError();
  //abstract public double getZDoubleValue();
  //abstract public double setZError(final double zError);
  //abstract public double setZValue(final double zValue);
  
  /**
   * Set the wrapped Measurement object water level value and uncertainty attributes:
   *
   * @param zValue : The new water level(Z) value
   * @param zError : The new uncertainty of the water level.
   * @return IWLMeasurement object
   */
  //@NotNull
  IWLMeasurement set(final double zValue, final double zError);
}
