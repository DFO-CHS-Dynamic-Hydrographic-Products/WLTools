package ca.gc.dfo.chs.dhp.sproduct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// --- HDFql lib
import as.hdfql.HDFql;
import as.hdfql.HDFqlCursor;
import as.hdfql.HDFqlConstants;

// ---
import ca.gc.dfo.chs.dhp.sproduct.ISProductIO;

// ---
abstract public class SProduct extends SProductIO {
    //public class SProduct implements ISProductIO {

  private final static String whoAmI= "ca.gc.dfo.chs.dhp.SProduct";

  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // ---
  public static final void checkGeoRef() {

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
} // --- class SProduct
