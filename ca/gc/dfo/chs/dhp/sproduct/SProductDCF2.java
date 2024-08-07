package ca.gc.dfo.chs.dhp.sproduct;

// ---
import java.util.Map;

// ---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// --- HDFql lib
import as.hdfql.HDFql;
import as.hdfql.HDFqlCursor;
import as.hdfql.HDFqlConstants;

// ---
import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.util.IHBGeom;
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.util.RegularBoundingBox;

// ---
import ca.gc.dfo.chs.dhp.sproduct.SProduct;
import ca.gc.dfo.chs.dhp.sproduct.ISProductIO;

// ---
public class SProductDCF2 extends SProduct implements ISProductIO {

  private final static String whoAmI= "ca.gc.dfo.chs.dhp.SProductDCF2";

  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  protected double gridOriginLon= IHBGeom.UNDEFINED_COORD;
  protected double gridOriginLat= IHBGeom.UNDEFINED_COORD;

  protected double gridLonSpacing= IHBGeom.UNDEFINED_COORD;
  protected double gridLatSpacing= IHBGeom.UNDEFINED_COORD;

  // --- havePixelsOverlap:
  //
  //        A boolean used To deal with the possibility
  //     that DCF2 pixels are overlapping for neighbor
  //     tiles (i.e. that the DCF2 tiles have pixels
  //     right on all their bounding boxes sides.
  //
  //       If true then it also means that the DCF2 grid
  //     origin (i.e. the pixel at the (0.0) grid location)
  //     coordinates are the exact same as the tiles bounding
  //     boxes South-West corner coordinates.
  //
  //       If false then it means that the DCF2 grid
  //     origin (i.e. the pixel at the (0.0) grid location)
  //     is located at one half-pixel width (in degrees)
  //     from the tile South-West corner.
  //     
  protected boolean havePixelsOverlap= false;

  // --- 
  public SProductDCF2(final String SPrdFilePath, final String openModeStr,
		      final ISProductIO.FeatId featureDataTypeId, final String dotPlusH5GrpNumId ) { //final String dataTypeHDF5GroupId) {

    super(SPrdFilePath, openModeStr);
      
    final String mmi= "constructor: ";

    slog.info(mmi+"start: featureDataTypeId.name()="+featureDataTypeId.name()+", dotPlusH5GrpNumId="+dotPlusH5GrpNumId);

    this.checkFileInUse();

    try {
      this.tileBoundingBox.hashCode();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe+" this.tileBoundingBox cannot be null here !!");
    }

    // --- Build the HDF5 GROUP id to use here:
    //     TODO: move this to the SProduct super-class constructor
    //           and use a class member String object to store it??
    final String dataTypeH5GrpId= ISProductIO.ROOT_GRP_ID +
	                          ISProductIO.FEATURE_IDS.get(featureDataTypeId) +
	                          ISProductIO.GRP_SEP_ID +
	                          ISProductIO.FEATURE_IDS.get(featureDataTypeId) + dotPlusH5GrpNumId;

    slog.info(mmi+"dataTypeH5GrpId="+dataTypeH5GrpId);
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);
    
    // --- Read and store the specific DCF2 grid parameters.

    // --- coordinates of the DCF2 grid origin
    Double [] gridOriginLonTmp= new Double [] {0.0};
    Double [] gridOriginLatTmp= new Double [] {0.0};

    SProductIO.setTransientAttrFromGroup( ISProductIO.DCF2_GRID_ORIG_LON_ATTR_ID,
				          dataTypeH5GrpId, HDFql.variableTransientRegister(gridOriginLonTmp) );    

    SProductIO.setTransientAttrFromGroup( ISProductIO.DCF2_GRID_ORIG_LAT_ATTR_ID,
				          dataTypeH5GrpId, HDFql.variableTransientRegister(gridOriginLatTmp) );

    this.gridOriginLon= gridOriginLonTmp[0];
    this.gridOriginLat= gridOriginLatTmp[0];

    slog.info(mmi+"this.gridOriginLon="+this.gridOriginLon);
    slog.info(mmi+"this.gridOriginLat="+this.gridOriginLat);

    // --- longitude and latitude spacings (in decimal degrees) of the DCF2 grid.
    //     NOTE: They must be the same for now and we will see later if we will
    //           need to be able to deal with different longitude and latitude grid
    //           spacings which we have north of the 67N latitude.
    Double [] gridLonSpacingTmp =new Double [] {0.0};
    Double [] gridLatSpacingTmp= new Double [] {0.0};

    SProductIO.setTransientAttrFromGroup( ISProductIO.DCF2_GRID_SPACING_LON_ATTR_ID,
				          dataTypeH5GrpId, HDFql.variableTransientRegister(gridLonSpacingTmp) );    
    
    SProductIO.setTransientAttrFromGroup( ISProductIO.DCF2_GRID_SPACING_LAT_ATTR_ID,
				          dataTypeH5GrpId, HDFql.variableTransientRegister(gridLatSpacingTmp) );

    // --- Check if the lon-lat grid spacings are the same for now.
    final String checkGridLonSpcStr= Double.toString(gridLonSpacingTmp[0]);
    final String checkGridLatSpcStr= Double.toString(gridLatSpacingTmp[0]);

    //slog.info(mmi+"checkGridLonSpcStr="+checkGridLonSpcStr);
    //slog.info(mmi+"checkGridLatSpcStr="+checkGridLatSpcStr);

    if (!checkGridLonSpcStr.equals(checkGridLatSpcStr)) {
      throw new RuntimeException(mmi+"gridLonSpacingTmp[0]="+gridLonSpacingTmp[0]+", gridLatSpacingTmp[0]="+
				 gridLatSpacingTmp[0]+", Cannot use DCF2 lon-lat grid spacings that are not the same for now !!");
    }

    this.gridLonSpacing= gridLonSpacingTmp[0];
    this.gridLatSpacing= gridLatSpacingTmp[0];
    
    slog.info(mmi+"this.gridLonSpacing="+this.gridLonSpacing);
    slog.info(mmi+"this.gridLatSpacing="+this.gridLatSpacing);

    // --- Now check if we have a DCF2 pixels overlap at the
    //     boundaries between adjacent tiles. Just need to
    //     check if the difference between the grid origin
    //     coordinates and the South-West corner coordinates
    //     of the tile bounding box is smaller than half the
    //     lan-lat grid spacings. Again this difference must
    //     be the same for longitudes and latitudes for now.

    // --- Get the tile b. box SW corner HBCoords object
    final HBCoords tileBBSWCorner= this.tileBoundingBox.getSouthWestCornerHBCoordsCopy();

    // --- Calculate the offsets in degrees between the coordinates of the pixel at (0,0) grid location
    //     and the coordinates of the SW corner of the tile b. box. 
    final double check00PixelOffsetLon= this.gridOriginLon - tileBBSWCorner.getLongitude();
    final double check00PixelOffsetLat= this.gridOriginLat - tileBBSWCorner.getLatitude();

    if (check00PixelOffsetLon < 0.0) {
      throw new RuntimeException(mmi+"DCF2 this.gridOriginLon must be the same OR larger than the tile B. Box SW corner longitude !!"); 
    }
    
    if (check00PixelOffsetLat < 0.0) {
      throw new RuntimeException(mmi+"DCF2 this.gridOriginLat must be the same OR larger than the tile B. Box SW corner latitude !!"); 
    }

    final String check00PixelOffsetLonStr= ((Double)check00PixelOffsetLon).toString();
    final String check00PixelOffsetLatStr= ((Double)check00PixelOffsetLat).toString();

    slog.info(mmi+"check00PixelOffsetLonStr="+check00PixelOffsetLonStr);
    slog.info(mmi+"check00PixelOffsetLatStr="+check00PixelOffsetLatStr);

    // --- TODO: check for the case where we would have pixels overlap for a
    //           direction and not for the other??
    if (!check00PixelOffsetLonStr.equals(check00PixelOffsetLatStr)) {
      throw new RuntimeException(mmi+"Must have that the (0,0) pixel lon-lat offsets are the same for now!!"); 
    }

    // --- Determine if we have or not have pixels overlaps for both directions.
    final Boolean checkPixelsLonOverlap= (check00PixelOffsetLon < this.gridLonSpacing/2.0) ? Boolean.TRUE : Boolean.FALSE;
    final Boolean checkPixelsLatOverlap= (check00PixelOffsetLat < this.gridLatSpacing/2.0) ? Boolean.TRUE : Boolean.FALSE;
    
    slog.info(mmi+"checkPixelsLonOverlap="+checkPixelsLonOverlap);
    slog.info(mmi+"checkPixelsLatOverlap="+checkPixelsLatOverlap);

    // --- Could be seen as being overkill but we never know
    if (!checkPixelsLonOverlap.equals(checkPixelsLatOverlap)) {
      throw new RuntimeException(mmi+"Cannot have pixels overlap between adjacent tiles for a direction and no pixels overlap for the other direction at the same time!!");
    }

    // --- Now we can set this.havePixelsOverlap with any of the two checkPixelsLatOverlap or checkPixelsLonOverlap
    this.havePixelsOverlap= checkPixelsLatOverlap;

    slog.info(mmi+"this.havePixelsOverlap="+this.havePixelsOverlap);
    
    slog.info(mmi+"Debug exit 0");
    System.exit(0);

    slog.info(mmi+"end");
  }
    
} // --- class SProductDCF2
