package ca.gc.dfo.chs.dhp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// --- HDFql lib
import as.hdfql.HDFql;
import as.hdfql.HDFqlCursor;
import as.hdfql.HDFqlConstants;

// ---
import ca.gc.dfo.chs.dhp.ISProductIO;

// ---
//abstract public class SProduct implements ISProductIO {
public class SProduct implements ISProductIO {

  private final static String whoAmI= "ca.gc.dfo.chs.dhp.SProduct";

  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // --- 
  public class S104DataCompoundType {
      
    double WaterLevelHeight;
    byte   WaterLevelTrend;
    double Uncertainty;

    public S104DataCompoundType(final double waterLevelHeight, final byte waterLevelTrend, final double uncertainty) {
	this.WaterLevelHeight= waterLevelHeight;
	this.WaterLevelTrend= waterLevelTrend;
	this.Uncertainty= uncertainty;
    }
  }
    
  // ---
  static public void updTransientAttrInGroup(final String attrId, final String groupId, final int transientAttrId ) {

    final String mmi= "updateTransientAttrInGroup: ";
      
    int cqc= HDFql.execute("USE GROUP "+groupId);

    if (cqc != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+"Group: "+groupId+" not found in HDF5 output file!");
    }
      
    cqc= HDFql.execute("INSERT INTO ATTRIBUTE "+attrId+" VALUES FROM MEMORY "+ transientAttrId);

    if (cqc != HDFqlConstants.SUCCESS) {
      throw new RuntimeException(mmi+
        "Problem with HDFql command INSERT INTO ATTRIBUTE "+attrId+" VALUES FROM MEMORY ");
    }  
  }
    
}
