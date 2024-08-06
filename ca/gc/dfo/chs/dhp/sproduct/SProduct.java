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

    // --- The SPrdFilePath MUST already exist even for the ISProductIO.FILE_READ_WRITE_MODE
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

    // --- Define this.tileGeoIdentifierInFile which should stay the same until
    //     the HDF5 file is closed
    this.tileGeoIdentifierInFile= this.readTileGeoIdentifierFromFileInUse();

    slog.info(mmi+"this.tileGeoIdentifierInFile="+this.tileGeoIdentifierInFile);

    //this.checkFileInUse();

    this.filePathInUse= SPrdFilePath;

    this.checkGeoRef();

    // --- Now create this.tileBoundingBox object
    
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

    // --- Get rid of the annoying floating point decimal noise that shows up when the auto conversion to double is done
    //     by doing a manual conversion trick to double via a temporary String
    final double tbbWestSideLonDbl= Double.parseDouble(Float.toString(tbbWestSideLon[0]));
    final double tbbSouthSideLatDbl= Double.parseDouble(Float.toString(tbbSouthSideLat[0]));
	
    final double tbbEastSideLonDbl= Double.parseDouble(Float.toString(tbbEastSideLon[0]));
    final double tbbNorthSideLatDbl= Double.parseDouble(Float.toString(tbbNorthSideLat[0]));

    //slog.info(mmi+"tbbWestSideLonDbl="+tbbWestSideLonDbl);

    this.tileBoundingBox= new RegularBoundingBox(tbbWestSideLonDbl, tbbSouthSideLatDbl, tbbEastSideLonDbl, tbbNorthSideLatDbl);

    slog.info(mmi+"this.tileBoundingBox="+this.tileBoundingBox.toString());

    slog.info(mmi+"end");
  }

  // ---
  public void closeFileInUse() {

    final String mmi= "closeFileInUse: ";

    this.checkFileInUse();

    slog.info(mmi+"Closing DHP file -> "+this.filePathInUse);

    int hdfqlCmdStatus= HDFql.execute("CLOSE FILE "+this.filePathInUse);

    if (hdfqlCmdStatus != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+"Problem with HDFql close file command for file -> "
				 +this.filePathInUse+", hdfqlCmdStatus="+hdfqlCmdStatus);
    }   
  }
    
  // ---
  public final boolean isHBCoordsInsideDHPTile(final HBCoords checkHBCoords) {

    // --- NOTE: this is using the HDF5 file that is opened and defined
    //           as the file being processed if more than one HDF5 file
    //           is used at the same time.
      
    final String mmi= "isHBCoordsInsideDHPTile: ";

    slog.info(mmi+"start");

    // --- Get the 4 DHP tile bounding box lat-lon limits from the HDF5 file

    // Float [] tbbWestSideLon= new Float [] { 0.0f };
    // Float [] tbbEastSideLon= new Float [] { 0.0f };
    // Float [] tbbSouthSideLat= new Float [] { 0.0f };
    // Float [] tbbNorthSideLat= new Float [] { 0.0f };

    // // --- Tile BB West lon (left side) limit
    // SProductIO.setTransientAttrFromGroup( ISProductIO.SWC_BBOX_LON_ATTR_ID,
    // 				          ISProductIO.ROOT_GRP_ID, HDFql.variableTransientRegister(tbbWestSideLon) );

    // // --- Tile BB East lon (right side) limit
    // SProductIO.setTransientAttrFromGroup( ISProductIO.NEC_BBOX_LON_ATTR_ID,
    // 				          ISProductIO.ROOT_GRP_ID, HDFql.variableTransientRegister(tbbEastSideLon) );    

    // // --- Tile BB South lat (bottom side) limit
    // SProductIO.setTransientAttrFromGroup( ISProductIO.SWC_BBOX_LAT_ATTR_ID,
    // 				          ISProductIO.ROOT_GRP_ID, HDFql.variableTransientRegister(tbbSouthSideLat) );

    // // --- Tile BB North lat (top side) limit
    // SProductIO.setTransientAttrFromGroup( ISProductIO.NEC_BBOX_LAT_ATTR_ID,
    // 				          ISProductIO.ROOT_GRP_ID, HDFql.variableTransientRegister(tbbNorthSideLat) );

    // //final double tbbWestSideLon= Double.parseDouble(tbbWestLonStr[0]);
    // //final double tbbEastSideLon= Double.parseDouble(tbbEastLonStr[0]);
    // //final double tbbSouthSideLat= Double.parseDouble(tbbSouthLatStr[0]);
    // //final double tbbNorthSideLat= Double.parseDouble(tbbNorthLatStr[0]);

    // slog.info(mmi+"tbbWestSideLon[0]="+tbbWestSideLon[0]);
    // slog.info(mmi+"tbbEastSideLon[0]="+tbbEastSideLon[0]);
    // slog.info(mmi+"tbbSouthSideLat[0]="+tbbSouthSideLat[0]);
    // slog.info(mmi+"tbbNorthSideLat[0]="+tbbNorthSideLat[0]);

    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);
    
    // --- Instantiate a RegularBoundingBox object with the S104 DCF2 tile b. box coordinates limits.
    //final RegularBoundingBox s104Dcf2TileBBox=
    //  new RegularBoundingBox(tbbWestSideLon[0], tbbSouthSideLat[0], tbbEastSideLon[0], tbbNorthSideLat[0]);
    
    slog.info(mmi+"end");
    
    // --- Verify that the checkHBCoords object is inside the tile bounding box.
    return this.tileBoundingBox.isHBCoordsInside(checkHBCoords);
  }
    
} // --- class SProduct
