package ca.gc.dfo.chs.dhp.sproduct;

// ---
//import java.util.Map;
//import java.util.Set;
import java.util.List;
import java.util.ArrayList;


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
import ca.gc.dfo.chs.wltools.wl.WLLocation;
import ca.gc.dfo.chs.modeldata.ModelDataExtraction;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
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

  protected ISProductIO.gridScanDirection gridScanDirection;
    
  protected int numPointsLongitudinal= 0;
  protected int numPointsLatitudinal= 0;

  // final int IIndex= 0;
  // final int JIndex= 1;  
    
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

    // --- Build the HDF5 /<feature dataType> GROUP String id.:
    final String dataTypeH5GrpId= ISProductIO.ROOT_GRP_ID + ISProductIO.FEATURE_IDS.get(featureDataTypeId);

    slog.info(mmi+"dataTypeH5GrpId="+dataTypeH5GrpId);
    
    // --- Build the HDF5 /<feature dataType>/<feature dataType>.NN GROUP String id:
    //     TODO: move this to the SProduct super-class constructor
    //           and use a class member String object to store it??
    final String dataTypeNNH5GrpId= dataTypeH5GrpId + ISProductIO.GRP_SEP_ID +
	                            ISProductIO.FEATURE_IDS.get(featureDataTypeId) + dotPlusH5GrpNumId;

    slog.info(mmi+"dataTypeNNH5GrpId="+dataTypeNNH5GrpId);
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);
    
    // --- Read and store the specific DCF2 grid parameters.

    // --- coordinates of the DCF2 grid origin
    Double [] gridOriginLonTmp= new Double [] {0.0};
    Double [] gridOriginLatTmp= new Double [] {0.0};

    SProductIO.setTransientAttrFromGroup( ISProductIO.DCF2_GRID_ORIG_LON_ATTR_ID,
				          dataTypeNNH5GrpId, HDFql.variableTransientRegister(gridOriginLonTmp) );    

    SProductIO.setTransientAttrFromGroup( ISProductIO.DCF2_GRID_ORIG_LAT_ATTR_ID,
				          dataTypeNNH5GrpId, HDFql.variableTransientRegister(gridOriginLatTmp) );

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
				          dataTypeNNH5GrpId, HDFql.variableTransientRegister(gridLonSpacingTmp) );    
    
    SProductIO.setTransientAttrFromGroup( ISProductIO.DCF2_GRID_SPACING_LAT_ATTR_ID,
				          dataTypeNNH5GrpId, HDFql.variableTransientRegister(gridLatSpacingTmp) );

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

    // --- Get the DCF2 grid mapping (I,J) direction.
    //     Should be "longitude,latitude" for now, we
    //     will see later if we will need to use the
    //     other type which is "latitude,longitude"
    String [] gridScanDirectionStrTmp= new String [] { new String() };

    // --- The attrribute is in the parent HDF5 /<feature dataType> GROUP 
    SProductIO.setTransientAttrFromGroup( ISProductIO.DCF2_GRID_AXES_MAPPING_ATTR_ID,
					  dataTypeH5GrpId, HDFql.variableTransientRegister(gridScanDirectionStrTmp));

    //slog.info(mmi+"gridScanDirectionStrTmp[0]="+gridScanDirectionStrTmp[0]);
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    if (!ISProductIO.allowedGScanDirections.containsKey(gridScanDirectionStrTmp[0])) {
      throw new RuntimeException(mmi+"Invalid (not allowed) DF2 grid scan direction -> "+gridScanDirectionStrTmp[0]);
    }

    // --- Define this.gridScanDirection type: 
    this.gridScanDirection= ISProductIO.allowedGScanDirections.get( gridScanDirectionStrTmp[0] );

    slog.info(mmi+"Will use this.gridScanDirection="+this.gridScanDirection.name());
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);    
	
    // --- Now get the nb. of DCF2 grid points for both axis.
    //     (assuming (I,J) LON_LAT grid mapping)
    Integer [] nbPtsLonDirTmp= new Integer [] {0};
    Integer [] nbPtsLatDirTmp= new Integer [] {0};

    SProductIO.setTransientAttrFromGroup( ISProductIO.DCF2_GRID_IAXIS_SIZE_ATTR_ID,
					  dataTypeNNH5GrpId, HDFql.variableTransientRegister(nbPtsLonDirTmp));

    SProductIO.setTransientAttrFromGroup( ISProductIO.DCF2_GRID_JAXIS_SIZE_ATTR_ID,
					  dataTypeNNH5GrpId, HDFql.variableTransientRegister(nbPtsLatDirTmp));    
    
    this.numPointsLongitudinal= nbPtsLonDirTmp[0];
    this.numPointsLatitudinal= nbPtsLatDirTmp[0];

    slog.info(mmi+"this.numPointsLongitudinal="+this.numPointsLongitudinal);
    slog.info(mmi+"his.numPointsLatitudinal="+this.numPointsLatitudinal);
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0); 

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
    
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    slog.info(mmi+"end");
  }

  // ---
  public final int [] getNearestPixelIJIndices(final WLLocation wlLocation) {

    final String mmi= "getNearestPixelIJIndices: ";
	
    // --- Use the generic ModelDataExtractionget.RegGridNearestIJIndices method here.
    //     (the true for the 3rd arg is for telling the method that it needs to consider
    //     that the upper sides of the tile b. box are not considered ad being part of
    //     the tile itself. This means that a wlLocation that would be located directly
    //     on one of the two upper sides will be considered as being outside of the tile b. box)
    final int [] nearestPixelIJIndices= ModelDataExtraction
      .getRegGridNearestIJIndices(wlLocation, this.tileBoundingBox, true, this.gridLonSpacing, this.gridLatSpacing, this.numPointsLongitudinal, this.numPointsLatitudinal);

    // --- Easier case: no pixels overlaps, the indices combo of the nearest pixel
    //     is directly defined with values in nearestPixelIJIndices so we can take
    //     them to extract the wanted data at this nearest pixel 

    // --- More complicated case: having pixels overlaps
    if (this.havePixelsOverlap) {
	
      slog.info(mmi+"Dealing with pixels overlaps case");

      // --- Determine which of West or East side is the nearest from the wlLocation.
      //if ()
    }

    slog.info(mmi+"Debug exit 0");
    System.exit(0);      
    
    return nearestPixelIJIndices;
  }

  // --- NOTE: h5CmpndTypeDataId String could be either the WL value HDF5 attribute id.
  //           or the WL trend HDF5 attribute id.
  //public final List<MeasurementCustom> getMCAtWLLocation(final WLLocation wlLocation,
  //							 final String h5CmpndTypeDataId , final String h5CmpndTypeUncrtId) {

  // --- NOTE: h5CmpndTypeDataId String could be either the WL value HDF5 attribute id.
  //           or the WL trend HDF5 attribute id
  public final List<MeasurementCustom> getMCAtGridIJIndices(final int [] nearestPixelIJIndices,
							    final String h5CmpndTypeDataId, final String h5CmpndTypeUncrtId) {
	  
    final String mmi= "getMCAtGridIJIndices: ";
      
    slog.info(mmi+"start:");

    // // --- First check that the WLLocation coordinates are indeed inside the
    // //     S104 DCF2 tile bounding box.The true as the 2nd arg. of the method
    // //     is for telling it that we need to exclude the upper sides (i.e.
    // //     North and East sides) of the tile to determine that an checkHBCoords
    // //     is inside this tile.
    // if (!this.isHBCoordsInsideDHPTile(wlLocation, true)) {
    //   throw new RuntimeException(mmi+"The WLLocation (point) object is outside the S104 DCF2 tile bounding box !!"); 
    // }

    // slog.info(mmi+"The WLLocation (point) object -> "+wlLocation.getIdentity()+" is indeed inside the DCF2 tile bounding box");
    // //slog.info(mmi+"fmfFromZCConvVal="+fmfFromZCConvVal);
    // slog.info(mmi+"h5CmpndTypeDataId="+h5CmpndTypeDataId+", h5CmpndTypeUncrtId="+h5CmpndTypeUncrtId);

    List<MeasurementCustom> mcAtWLLocation= new ArrayList<MeasurementCustom>();
    
    slog.info(mmi+"Debug exit 0");
    System.exit(0);    

    slog.info(mmi+"end");

    return mcAtWLLocation;
  }
    
} // --- class SProductDCF2
