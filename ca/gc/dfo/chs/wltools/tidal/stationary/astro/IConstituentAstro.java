//package ca.gc.dfo.iwls.fmservice.modeling.tides.astro;
package ca.gc.dfo.chs.wltools.tidal.stationary.astro;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

//---
//import javax.validation.constraints.Min;
//import javax.validation.constraints.Size;

import javax.validation.constraints.NotNull;

/**
 * Interface for generics tidal constituents astronomic computations methods.
 */
public interface IConstituentAstro {
  
  /**
   * @return The name of a tidal constituent.
   */
  @NotNull
  String getName();
  
  /**
   * @param c1d : A Constituent1D(with constituent amplitude and Greenwich phase lag attributes) object of a
   *            specific constituent.
   * @return The double precision value of the computed tidal component related to a specific constituent.
   */
  double computeTidalAmplitude(@NotNull final Constituent1D c1d);
  
  /**
   * @param dTimePos : The seconds(in double precision) since the last update of the astronomic informations
   *                 related to a specific constituent.
   * @param c1d      : A Constituent1D(constituent amplitude and Greenwich phase lag) object of a specific
   *                 constituent.
   * @return The double precision value of the computed(at the dTimePos time offset) tidal component related to a
   * specific constituent.
   */
  double computeTidalAmplitude(final double dTimePos, @NotNull final Constituent1D c1d);
  
  /**
   * @param amplitude   : Amplitude of a specific tidal constituent.
   * @param grnwchPhLag Greenwich phase lag of a specific tidal constituent.
   * @return The double precision value of the computed tidal component related to a specific constituent.
   */
  double computeTidalAmplitude(final double amplitude, final double grnwchPhLag);
  
  /**
   * @param dTimePos    : The seconds(in double precision) since the last update of the astronomic informations
   *                    related to a specific constituent.
   * @param amplitude   : Amplitude of a specific tidal constituent.
   * @param grnwchPhLag : Greenwich phase lag of a specific tidal constituent.
   * @return The double precision value of the computed(at the dTimePos time offset) tidal component related to a
   * specific constituent.
   */
  double computeTidalAmplitude(final double dTimePos, final double amplitude, final double grnwchPhLag);
  
  /**
   * @return A IConstituentAstro object.
   */
  IConstituentAstro init();
  
  /**
   * @return A String representing the contents of a IConstituentAstro object.
   */
  String toString();
}
