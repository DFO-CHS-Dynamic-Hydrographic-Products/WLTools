package ca.gc.dfo.chs.dhp.sproduct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// --- HDFql lib
//import as.hdfql.HDFql;
//import as.hdfql.HDFqlCursor;
//import as.hdfql.HDFqlConstants;

// ---
import ca.gc.dfo.chs.dhp.sproduct.ISProductIO;
import ca.gc.dfo.chs.dhp.sproduct.S104HeightTrendCompoundType;

// ---
final public class S104DCFNCompoundType
   extends S104HeightTrendCompoundType implements ISProductIO {

  //private final static String whoAmI= "ca.gc.dfo.chs.dhp.S104DCFNCompoundType";
  //private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // --- Need to use an upper case 1st letter in the attributes
  //     names for the HDFql insert operation ?
  private float Uncertainty;

  // private Float WaterLevelHeight;
  // private Float Uncertainty;
  // private Byte  WaterLevelTrend;

  // --- IMPORTANT: We absolutely need to define a default constructor
  //     here otherwise the HDFql native lib crashes without displaying
  //     a meaningful error message.
  public S104DCFNCompoundType() { }

  // ---
  public S104DCFNCompoundType(final float waterLevelHeight,
			      final float uncertainty, final byte waterLevelTrend) {
      
    super(waterLevelHeight,waterLevelTrend);
    
    this.Uncertainty= uncertainty;
  }

  // ---
  static public S104DCFNCompoundType [] createAndInitArray(final int dim) {

    S104DCFNCompoundType [] retArr= new S104DCFNCompoundType[dim];

    for(int idx= 0; idx < dim; idx++) {
      retArr[idx]= new S104DCFNCompoundType();
    }

    return retArr;
  }

  // ---
  public S104DCFNCompoundType set(final float waterLevelHeight,
				  final float uncertainty, final byte waterLevelTrend) {
      
    super.set(waterLevelHeight, waterLevelTrend);
    
    this.Uncertainty= uncertainty;

    return this;
  } 

  // ---
  //public float getWaterLevelHeight() {
  ////public Float getWaterLevelHeight() {  
  //  return this.WaterLevelHeight;
    // }

  // ---
  final public float getUncertainty() {
  //public Float getUncertainty() {
    return this.Uncertainty;
  }  

  //// ---
  //public byte getWaterLevelTrend() {
  ////public Byte getWaterLevelTrend() {
  //  return this.WaterLevelTrend;
  //}  
 
} // --- class S104DCF8CompoundType
