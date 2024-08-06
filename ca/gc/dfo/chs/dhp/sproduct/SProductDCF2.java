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
public class SProductDCF2 extends SProduct implements ISProductIO {

  private final static String whoAmI= "ca.gc.dfo.chs.dhp.SProductDCF2";

  private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  protected double gridOriginLon= 0.0;
  protected double gridOriginLat= 0.0;

  protected double gridLonSpacing= 0.0;
  protected double gridLatSpacing= 0.0;

  // --- 
  public SProductDCF2(final String SPrdFilePath, final String openModeStr) {

    super(SPrdFilePath, openModeStr);
      
    final String mmi= "constructor: ";

    slog.info(mmi+"start");

    // --- Read and store the specific DCF2 grid parameters.

    slog.info(mmi+"end");
  }
    
} // --- class SProductDCF2
