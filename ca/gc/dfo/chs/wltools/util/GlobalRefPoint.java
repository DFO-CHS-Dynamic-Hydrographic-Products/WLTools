//package ca.gc.dfo.iwls.fmservice.modeling.geo;
package ca.gc.dfo.chs.wltools.util;

/**
 *
 */

//---
import ca.gc.dfo.chs.wltools.util.IGeo;
import ca.gc.dfo.chs.wltools.util.HBCoords;

//import com.vividsolutions.jts.geom.Coordinate;
//import com.vividsolutions.jts.geom.GeometryFactory;
//import com.vividsolutions.jts.geom.Point;
//import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
//import javax.validation.constraints.NotNull;

//---
//import com.vividsolutions.jts.geom.CoordinateSequence;
//import org.geolatte.geom.Position;

//--- Class declaration using com.vividsolutions.jts.geom.Point inheritance.
//    It could be re-activated if the ca.gc.dfo.iwls.station.Station
//    class re-activate its own com.vividsolutions.jts.geom.Point as an attribute.
//abstract public class GlobalRefPoint extends Point implements IGeo {

/**
 * Global horizontal and vertical datums for a WL(or grid point) location.
 */
abstract public class GlobalRefPoint extends HBCoords implements IGeo {
  
  /**
   * Use a standard(OGC compliant) global vertical datum.
   */
  protected GlobalVerticalDatum globalVerticalDatum = GlobalVerticalDatum.NAVD88;
  /**
   * Global vertical offset(from a GlobalVerticalDatum) in meters of a 3D water modelling point.
   */
  protected double globalVerticalOffset = 0.0;
  /**
   * com.vividsolutions.jts.geom.Point as an attribute.
   * The horizontal datum(a.k.a. SRS - Spatial Reference System) is the default one used in Point class for now.
   */
  //private Point grefPoint = null;
  //private HBCoords grefPoint = null;

  //--- Constructor for GlobalRefPoint which is not inheriting from Point class but uses this.grefPoint
  /**
   * @param longitude            : The longitude to use for the GlobalRefPoint.
   * @param latitude             : The latitude to use for the GlobalRefPoint.
   * @param zElev                : The Z vertical elevation(which could be referred to a local datum OR the
   *                             GlobalVerticalDatum) to
   *                             use for the GlobalRefPoint.
   * @param globalVerticalDatum  : The GlobalVerticalDatum to use for the GlobalRefPoint.
   * @param globalVerticalOffset : The global vertical offset referred to the GlobalVerticalDatum used. It is Zero if
   *                             zElev is already referred to the GlobalVerticalDatum used.
   */
  public GlobalRefPoint(final double longitude, final double latitude, final double zElev,
                        /*@NotNull*/ final GlobalVerticalDatum globalVerticalDatum, final double globalVerticalOffset) {

    //final Coordinate[] coordinates = {new Coordinate(longitude, latitude, zElev)};
    //this.grefPoint = new Point(new CoordinateArraySequence(coordinates), new GeometryFactory());

    super(longitude,latitude);

    //---
    this.globalVerticalDatum = globalVerticalDatum;
    this.globalVerticalOffset = globalVerticalOffset;
  }

  //--- Constructor that can be re-activated if the ca.gc.dfo.iwls.station.Station
  //    class re-activate its own com.vividsolutions.jts.geom.Point as an attribute.
//    public GlobalRefPoint(@NotNull final CoordinateSequence coordinates, @NotNull final GeometryFactory factory,
//    @NotNull final GlobalVerticalDatum gvd, final double gvo) {
//
//        super(coordinates, factory);
//        this.globalVerticalDatum= gvd;
//        this.globalVerticalOffset= gvo;
//    }

  //--- Constructor that can be re-activated if the ca.gc.dfo.iwls.station class re-activate its own com
  // .vividsolutions.jts.geom.Point as an attribute.
//    public GlobalRef(final double latitude,final double longitude,@NotNull final GeometryFactory factory, @NotNull
//    GlobalVerticalDatum gvd, final double gvo) {
//
//        //super(new Coordinate(longitude,latitude,0), factory);
//        //super(coordinates, factory);
//        this.globalVerticalDatum= gvd;
//        this.globalVerticalOffset= gvo;
//    }
}
