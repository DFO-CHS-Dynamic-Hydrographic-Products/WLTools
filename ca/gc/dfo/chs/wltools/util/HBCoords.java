package ca.gc.dfo.chs.util;

import ca.gc.dfo.chs.util.HBGeom;

// --- NOTE: This is a temporary Home Brew (HB) code that should
//     be replaced by either a class from java.awt.geom or a class
//     from the jts code https://github.com/locationtech/jts.
//     BUT we only need this class for now and it could probably
//     be overkill to use jts or the GDAL java wrappers.
public final class HBCoords implements HBGeom {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.util.HBCoords";

 /**
   * private logger utility.
   */
  private final Logger log = LoggerFactory.getLogger(whoAmI);

  // --- Only EPSG:4326 normally.
  protected double latitude;
  protected double longitude;

  public HBCoords() {
    this.latitude=
      this.longitude= HBGeom.UNDEFINED_COORD;
  }

  public HBCoords(final double latitude, final double longitude) {
    this.latitude= latitude;
    this.longitude= longitude;
  }

  public final getLatitude() {
    return this.latitude;
  }

  public final getLongitude() {
    return this.longitude;
  }

}
