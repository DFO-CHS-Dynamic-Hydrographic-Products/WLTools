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
import ca.gc.dfo.chs.wltools.util.HBCoords;
import ca.gc.dfo.chs.wltools.util.RegularBoundingBox;

// ---
import ca.gc.dfo.chs.dhp.sproduct.ISProductIO;

// ---
abstract public class SProductIO implements ISProductIO {

  private final static String whoAmI= "ca.gc.dfo.chs.dhp.SProductIO";

  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  protected String tileGeoIdentifierInFile= null;
    
  protected RegularBoundingBox tileBoundingBox= null;

  // // --- To deal with the possibility that DCF2 pixels
  // //     are overlapping for neighbor tiles (i.e. that
  // //     the DCF2 tiles have pixels right on all their
  // //     bounding boxes sides. If true then it also
  // //     means that the DCF2 grid origin coordinates
  // //     are the same as the tiles bounding boxes
  // //     South-West corner coordinates.
  // protected boolean dcf2PixelsOverlap= false;

  protected String filePathInUse= null;

  // ---
  final public String readTileGeoIdentifierFromFileInUse() {
    
    // --- This seems strange but this is what HDFql needs to be able
    //     to extract an String attribute from an HDF5 file GROUP structure
    //     (unary array of String objects with just one String object in it)
    String [] tileGeoIdTmp= new String [] { new String() };

    SProductIO.setTransientAttrFromGroup(ISProductIO.TILE_GEO_ID_ATTR_ID,
					 ISProductIO.ROOT_GRP_ID, HDFql.variableTransientRegister(tileGeoIdTmp));
    return tileGeoIdTmp[0];
  }

  // ---
  final public void checkFileInUse() {

    final String mmi= "checkFileInUse: ";

    slog.info(mmi+"Checking if file in use is the right one");

    final String checkGeoIdentity= this.readTileGeoIdentifierFromFileInUse();

    if (!this.tileGeoIdentifierInFile.equals(checkGeoIdentity)) {
      throw new RuntimeException(mmi+"Wrong HDF5 file in use !! checkGeoIdentity="+checkGeoIdentity+
				 ",this.tileGeoIdentifierInFile= "+this.tileGeoIdentifierInFile+", stopping exec !!"); 
    }

    slog.info(mmi+"File in use is the right one");
  }

      // ---
  public final void checkGeoRef() {

    // --- NOTE: this is using the HDF5 file that is opened and defined
    //           as the file being processed if more than one HDF5 file
    //           is used at the same time.
      
    final String mmi= "checkGeoRef: ";

    slog.info(mmi+"start");

    this.checkFileInUse();
    
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
  static public void setTransientAttrFromGroup(final String attrId, final String groupId, final int transientAttrId ) {
      
    final String mmi= "setTransientAttrFromGroup; ";
    
    if (transientAttrId < 0) {
      throw new RuntimeException(mmi+"Invalid transientAttrId="+transientAttrId);
    }

    int cqc= HDFql.execute("USE GROUP "+groupId);

    if (cqc != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+"Group: "+groupId+" not found in the HDF5 input file !! ");
    }    

    cqc= HDFql.execute("SELECT FROM ATTRIBUTE "+attrId+" INTO MEMORY "+ transientAttrId);

    if (cqc != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+"Problem with HDFql command SELECT FROM ATTRIBUTE  "+
				 attrId+" INTO MEMORY, error code cqc="+cqc+", transientAttrId="+transientAttrId);
    }
    
  } // --- method setTransientAttrFromGroup
    
  // ---
  static public void updTransientAttrInGroup(final String attrId, final String groupId, final int transientAttrId ) {

    final String mmi= "updTransientAttrInGroup: ";

    //slog.info(mmi+"transientAttrId="+transientAttrId);

    if (transientAttrId < 0) {
      throw new RuntimeException(mmi+"Invalid transientAttrId="+transientAttrId);
    }
      
    int cqc= HDFql.execute("USE GROUP "+groupId);

    if (cqc != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+"Group: "+groupId+" not found in HDF5 output file!");
    }
      
    cqc= HDFql.execute("INSERT INTO ATTRIBUTE "+attrId+" VALUES FROM MEMORY "+ transientAttrId);

    if (cqc != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+"Problem with HDFql command INSERT INTO ATTRIBUTE "+
				 attrId+" VALUES FROM MEMORY, error code cqc="+cqc+", transientAttrId="+transientAttrId);
    }
  } // --- method updTransientAttrInGroup
    
} // --- class SProductIO
