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
    //public class SProduct implements ISProductIO {

  private final static String whoAmI= "ca.gc.dfo.chs.dhp.SProductIO";

  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  protected String tileGeoId= null;
    
  protected Map<String, RegularBoundingBox> tilesBoundingBoxes= null;

  // ---
  final public String readTileGeoIdFromFileInUse() {
    
    // --- This seems strange but this is what HDFql needs to be able
    //     to extract an String attribute from an HDF5 file GROUP structure
    //     (unary array of String objects with just one String object in it)
    String [] tileGeoIdTmp= new String [] { new String() };

    SProductIO.setTransientAttrFromGroup(ISProductIO.TILE_GEO_ID_ATTR_ID,
					 ISProductIO.ROOT_GRP_ID, HDFql.variableTransientRegister(tileGeoIdTmp));
    return tileGeoIdTmp[0];
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
