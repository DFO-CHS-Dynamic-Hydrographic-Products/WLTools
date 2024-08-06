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
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.util.RegularBoundingBox;

// ---
import ca.gc.dfo.chs.dhp.sproduct.ISProductIO;

// ---
//abstract public class SProduct extends SProductIO {
public class SProduct extends SProductIO implements ISProductIO {

  private final static String whoAmI= "ca.gc.dfo.chs.dhp.SProduct";

  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // --- 
  public SProduct(final String SPrdFilePath, final String openModeStr) {

    final String mmi= "constructor: ";

    slog.info(mmi+"start: SPrdFilePath="+SPrdFilePath+", openModeStr=\""+openModeStr+"\"");

    try {
      SPrdFilePath.length();
    } catch (NullPointerException npe) {
      throw new RuntimeException(mmi+npe+"SPrdFilePath cannot be null here !!");
    }

    if (!WLToolsIO.checkForFileExistence(SPrdFilePath)) {
      throw new RuntimeException(mmi+"SPrdFilePath -> "+SPrdFilePath+" not found !!");
    }

    if (!openModeStr.equals(ISProductIO.FILE_READ_ONLY_MODE) &&
	!openModeStr.equals(ISProductIO.FILE_READ_WRITE_MODE)) {
	
      throw new RuntimeException(mmi+"Invalid file open mode -> "+openModeStr+" !!");
    }

    int hdfqlCmdStatus= HDFql.execute(openModeStr+SPrdFilePath);

    if (hdfqlCmdStatus != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+"Problem with HDFql open file command \"openModeStr\" for file -> "
				 +SPrdFilePath+", hdfqlCmdStatus="+hdfqlCmdStatus);
    }

    this.tileGeoId= this.readTileGeoIdFromFileInUse();

    slog.info(mmi+"checkFile="+this.tileGeoId);

    slog.info(mmi+"end");
  }

  // ---
  public void closeFileInUse() {
      
  }
    
  // ---
  public final void checkGeoRef() {

    // --- NOTE: this is using the HDF5 file that is opened and defined
    //           as the file being processed if more than one HDF5 file
    //           is used at the same time.
      
    final String mmi= "checkGeoRef: ";

    slog.info(mmi+"start");
    
    // --- This seems strange but this is what HDFql needs to be able
    //     to extract an String attribute from an HDF5 file GROUP structure
    //     (unary array of String objects with just one String object in it)
    String [] checkHorizDatumRefAttr= new String [] { new String() };

    // --- get the horiz. datum reference String Id.
    SProductIO.setTransientAttrFromGroup( ISProductIO.GEO_HORIZ_DATUM_REF_ATTR_ID,
	 			          ISProductIO.ROOT_GRP_ID, HDFql.variableTransientRegister(checkHorizDatumRefAttr) );

    if (!checkHorizDatumRefAttr[0].equals(ISProductIO.GEO_HORIZ_DATUM_REF_ATTR_ALLOWED)) {
	
      throw new RuntimeException(mmi+"Invalid "+ISProductIO.GEO_HORIZ_DATUM_REF_ATTR_ID+
				 " value -> "+checkHorizDatumRefAttr[0]+", it MUST be -> "+ISProductIO.GEO_HORIZ_DATUM_REF_ATTR_ALLOWED+" !!");
    }
    
    // --- Need an array of Integer objects with just one Integer object in it
    //     in order to have HDFql being able to extract an int attribute value.
    //     (NOTE: No low-level int variable can be used here) 
    Integer [] checkHorizCRSAttr= new Integer [] {0};

    // --- get the horiz. CRS String Id.
    SProductIO.setTransientAttrFromGroup( ISProductIO.GEO_HORIZ_CRS_ATTR_ID,
				          ISProductIO.ROOT_GRP_ID, HDFql.variableTransientRegister(checkHorizCRSAttr) );
    
    // --- We cannot use the != operator here since we compare Integer objects not low-level int values.
    if (!checkHorizCRSAttr[0].equals(ISProductIO.GEO_HORIZ_CRS_ATTR_ALLOWED)) {
	
      throw new RuntimeException(mmi+"Invalid "+ISProductIO.GEO_HORIZ_CRS_ATTR_ID+" value -> "+
				 checkHorizCRSAttr[0]+", it MUST be -> "+ISProductIO.GEO_HORIZ_CRS_ATTR_ALLOWED+" !!");
    }

    slog.info(mmi+"checkHorizDatumRefAttr[0]="+checkHorizDatumRefAttr[0]);
    slog.info(mmi+"checkHorizCRSAttr[0]="+checkHorizCRSAttr[0]);
    slog.info(mmi+"end");
  }

  // ---
  public static final boolean isHBCoordsInsideDHPTile(final HBCoords checkHBCoords) {

    // --- NOTE: this is using the HDF5 file that is opened and defined
    //           as the file being processed if more than one HDF5 file
    //           is used at the same time.
      
    final String mmi= "isHBCoordsInsideDHPTile: ";

    slog.info(mmi+"start");

    // --- Get the 4 DHP tile bounding box lat-lon limits from the HDF5 file

    Float [] tbbWestSideLon= new Float [] { 0.0f };
    Float [] tbbEastSideLon= new Float [] { 0.0f };
    Float [] tbbSouthSideLat= new Float [] { 0.0f };
    Float [] tbbNorthSideLat= new Float [] { 0.0f };

    // --- Tile BB West lon (left side) limit
    SProductIO.setTransientAttrFromGroup( ISProductIO.SWC_BBOX_LON_ATTR_ID,
				          ISProductIO.ROOT_GRP_ID, HDFql.variableTransientRegister(tbbWestSideLon) );

    // --- Tile BB East lon (right side) limit
    SProductIO.setTransientAttrFromGroup( ISProductIO.NEC_BBOX_LON_ATTR_ID,
				          ISProductIO.ROOT_GRP_ID, HDFql.variableTransientRegister(tbbEastSideLon) );    

    // --- Tile BB South lat (bottom side) limit
    SProductIO.setTransientAttrFromGroup( ISProductIO.SWC_BBOX_LAT_ATTR_ID,
				          ISProductIO.ROOT_GRP_ID, HDFql.variableTransientRegister(tbbSouthSideLat) );

    // --- Tile BB North lat (top side) limit
    SProductIO.setTransientAttrFromGroup( ISProductIO.NEC_BBOX_LAT_ATTR_ID,
				          ISProductIO.ROOT_GRP_ID, HDFql.variableTransientRegister(tbbNorthSideLat) );

    //final double tbbWestSideLon= Double.parseDouble(tbbWestLonStr[0]);
    //final double tbbEastSideLon= Double.parseDouble(tbbEastLonStr[0]);
    //final double tbbSouthSideLat= Double.parseDouble(tbbSouthLatStr[0]);
    //final double tbbNorthSideLat= Double.parseDouble(tbbNorthLatStr[0]);

    slog.info(mmi+"tbbWestSideLon[0]="+tbbWestSideLon[0]);
    slog.info(mmi+"tbbEastSideLon[0]="+tbbEastSideLon[0]);
    slog.info(mmi+"tbbSouthSideLat[0]="+tbbSouthSideLat[0]);
    slog.info(mmi+"tbbNorthSideLat[0]="+tbbNorthSideLat[0]);

    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);
    
    // --- Instantiate a RegularBoundingBox object with the S104 DCF2 tile b. box coordinates limits.
    final RegularBoundingBox s104Dcf2TileBBox=
      new RegularBoundingBox(tbbWestSideLon[0], tbbSouthSideLat[0], tbbEastSideLon[0], tbbNorthSideLat[0]);
    
    slog.info(mmi+"end");
    
    // --- verify that the WLLocation is indeed inside the S104 DCF tile bounding box.
    return s104Dcf2TileBBox.isHBCoordsInside(checkHBCoords);
  }
    
} // --- class SProduct
