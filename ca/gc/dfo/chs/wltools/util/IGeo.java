//package ca.gc.dfo.iwls.fmservice.modeling.geo;
package ca.gc.dfo.chs.wltools.util;

/**
 *
 */

/**
 * Interface for geo-referenced utilities.
 */
public interface IGeo {

  final static double EARTH_AVG_RADIUS_KM= 6371.008;
  final static double EARTH_AVG_RADIUS_METErS= 1000.0*EARTH_AVG_RADIUS_KM;

  /**
   * Define the vertical datums which can be used.
   */
  enum GlobalVerticalDatum {CGVD28, IGLD85, NAVD88, CGVD2013}
}
