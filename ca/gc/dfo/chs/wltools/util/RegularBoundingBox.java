package ca.gc.dfo.chs.wltools.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// ---
import ca.gc.dfo.chs.wltools.util.HBCoords;

// --- NOTE: Assuming that the EPSG:4326 CRS is always used.
public final class RegularBoundingBox {

  private final static String whoAmI=
    "ca.gc.dfo.chs.wltools.util.RegularBoundingBox";

 /**
   * private logger utility.
   */
  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // --- Only EPSG:4326 normally.
  private HBCoords SouthWestCorner= null;
  private HBCoords NorthEastCorner= null;

  // ---
  public RegularBoundingBox() {

    this.SouthWestCorner=
      this.NorthEastCorner= null;
  }

  // ---
  public RegularBoundingBox(final double swcLongitude, final double swcLatitude,
			    final double necLongitude, final double necLatitude) {

    final String mmi= "constructor: ";

    // --- Assuming EPSG:4326 
    if (swcLatitude >= necLatitude) {
      throw new RuntimeException(mmi+"Cannot have swcLatitude -> "+swcLatitude+" >= necLatitude -> "+necLatitude+" !!");
    }

    // --- bounding box that straddle the equator are not allowed
    if (swcLatitude < 0.0 && necLatitude > 0.0) {
      throw new RuntimeException(mmi+"Cannot have swcLatitude -> "+swcLatitude+" < 0.0 and necLatitude -> "+necLatitude+" > 0.0 !!");
    }

    // --- Assuming EPSG:4326
    if (swcLongitude >= necLongitude) {
      throw new RuntimeException(mmi+"Cannot have swcLongitude -> "+swcLongitude+" >= necLongitude -> "+necLongitude+" !!");
    }

    // --- bounding box that straddle the longitudes wrap around in the pacific ocean are not allowed
    if (swcLongitude > 0.0 && necLongitude < 0.0) {
      throw new RuntimeException(mmi+"Cannot have swcLongitude -> "+swcLongitude+" > 0.0 and necLongitude -> "+necLongitude+" < 0.0 !!");
    }

    // --- bounding box that straddle the Greenwhich meridian are not allowed
    if (swcLongitude < 0.0 && necLongitude > 0.0) {
      throw new RuntimeException(mmi+"Cannot have swcLongitude -> "+swcLongitude+" < 0.0 and necLongitude -> "+necLongitude+" > 0.0 !!");
    }  
    
    this.SouthWestCorner= new HBCoords(swcLongitude,swcLatitude);
    this.NorthEastCorner= new HBCoords(necLongitude,necLatitude);
  }

  // ---  
  public RegularBoundingBox(final HBCoords swcHBCoords, final HBCoords necHBCoords) {
      
    // ---
    this(swcHBCoords.getLongitude(), swcHBCoords.getLatitude(),
	 necHBCoords.getLongitude(), necHBCoords.getLatitude());  
  }

  // ---
  public String toString() {

    return "South-West corner -> ("+this.SouthWestCorner.getLongitude()+","+ this.SouthWestCorner.getLatitude()+")"+
	   ", North-East corner -> ("+this.NorthEastCorner.getLongitude()+","+ this.NorthEastCorner.getLatitude()+")";
  }

  // ---
  public HBCoords getSouthWestCornerHBCoordsCopy() {
    return new HBCoords( this.SouthWestCorner.getLongitude(), this.SouthWestCorner.getLatitude() );
  }

  // ---
  public HBCoords getNorthEastCornerHBCoordsCopy() {
    return new HBCoords( this.NorthEastCorner.getLongitude(), this.NorthEastCorner.getLatitude() );
  }  

  // --- Assuming EPSG:4326 for checkHBCoords
  public boolean isHBCoordsInside(final HBCoords checkHBCoords, final boolean excludeUpperSides) {

    final String mmi= "isHBCoordsinside: ";

    try {
      checkHBCoords.getLatitude();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe+" checkHBCoords cannot be null here !!");
    }

    final double checkHBCoordsLon= checkHBCoords.getLongitude();
    final double checkHBCoordsLat= checkHBCoords.getLatitude();

    // --- We always include the lower sides of the bounding box for the check
    //     (i.e. West and South sides).
    final boolean lowerSidesCheck= ( checkHBCoordsLat >= this.SouthWestCorner.getLatitude() &&
				     checkHBCoordsLon >= this.SouthWestCorner.getLongitude() );

    // --- But we allow to exclude the upper (East and North) sides of the b. box
    //     as being part of this b. box
    final double bbEastSideLon= this.NorthEastCorner.getLongitude();
    final double bbNorthSideLat= this.NorthEastCorner.getLatitude();
    
    final boolean upperSidesCheck= excludeUpperSides ?
      ( checkHBCoordsLat < bbNorthSideLat && checkHBCoordsLon < bbEastSideLon ) :
	(checkHBCoordsLat <= bbNorthSideLat && checkHBCoordsLon <= bbEastSideLon) ;

    return (lowerSidesCheck && upperSidesCheck);	
	
    // --- Assuming EPSG:4326 for checkHBCoords
    //return ( checkHBCoordsLat >= this.SouthWestCorner.getLatitude()  &&
    //     checkHBCoordsLat <= this.NorthEastCorner.getLatitude()  &&
    //     checkHBCoordsLon >= this.SouthWestCorner.getLongitude() &&
    //     checkHBCoordsLon <= this.NorthEastCorner.getLongitude()    );
  }	
}
