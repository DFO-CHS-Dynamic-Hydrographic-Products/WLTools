package ca.gc.dfo.chs.dhp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// --- HDFql lib
//import as.hdfql.HDFql;
//import as.hdfql.HDFqlCursor;
//import as.hdfql.HDFqlConstants;

// ---
import ca.gc.dfo.chs.dhp.ISProductIO;

// ---
final public class S104DCF8CompoundType implements ISProductIO {

  //private final static String whoAmI= "ca.gc.dfo.chs.dhp.S104DCF8CompoundType";
  //private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // --- Need to use an upper case 1st letter in the attributes
  //   names for the HDFql insert operation ??
  private float WaterLevelHeight;
  private float Uncertainty;
  private byte  WaterLevelTrend;

  // private Float WaterLevelHeight;
  // private Float Uncertainty;
  // private Byte  WaterLevelTrend;

  // --- IMPORTANT: We absolutely need to define a default constructor
  //     here otherwise the HDFql native lib crashes without displaying
  //     a meaningful error message.
  public S104DCF8CompoundType() { }

  // ---
  public S104DCF8CompoundType(final float waterLevelHeight,
			      final float uncertainty, final byte waterLevelTrend) {
      
    this.WaterLevelHeight= waterLevelHeight;
    this.WaterLevelTrend= waterLevelTrend;
    this.Uncertainty= uncertainty;
  }

  // ---
  static public S104DCF8CompoundType [] createAndInitArray(final int dim) {

    S104DCF8CompoundType [] retArr= new S104DCF8CompoundType[dim];

    for(int idx= 0; idx < dim; idx++) {
      retArr[idx]= new S104DCF8CompoundType();
    }

    return retArr;
  }

  // ---
  public S104DCF8CompoundType set(final float waterLevelHeight,
				  final float uncertainty, final byte waterLevelTrend) {
      
    this.WaterLevelHeight= waterLevelHeight;
    this.WaterLevelTrend= waterLevelTrend;
    this.Uncertainty= uncertainty;

    return this;
  } 

  // ---
  public float getWaterLevelHeight() {
  //public Float getWaterLevelHeight() {  
    return this.WaterLevelHeight;
  }

  // ---
  public float getUncertainty() {
  //public Float getUncertainty() {
    return this.Uncertainty;
  }  

  // ---
  public byte getWaterLevelTrend() {
  //public Byte getWaterLevelTrend() {
    return this.WaterLevelTrend;
  }  
 
} // --- class S104DCF8CompoundType
