package ca.gc.dfo.chs.dhp.sproduct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// --- HDFql lib
//import as.hdfql.HDFql;
//import as.hdfql.HDFqlCursor;
//import as.hdfql.HDFqlConstants;

// ---
import ca.gc.dfo.chs.dhp.sproduct.ISProductIO;

// ---
public class S104HeightTrendCompoundType implements ISProductIO {

  //private final static String whoAmI= "ca.gc.dfo.chs.dhp.S104HeightTrendCompoundType";
  //private final static Logger slog= LoggerFactory.getLogger(whoAmI);

  // --- Need to use an upper case 1st letter in the attributes
  //     names for the HDFql insert operation ??
  protected float WaterLevelHeight;
  protected byte  WaterLevelTrend;

  // private Float WaterLevelHeight;
  // private Float Uncertainty;
  // private Byte  WaterLevelTrend;

  // --- IMPORTANT: We absolutely need to define a default constructor
  //     here otherwise the HDFql native lib crashes without displaying
  //     a meaningful error message.
  public S104HeightTrendCompoundType() { }

  // ---
  public S104HeightTrendCompoundType(final float waterLevelHeight, final byte waterLevelTrend) {
      
    this.WaterLevelHeight= waterLevelHeight;
    this.WaterLevelTrend= waterLevelTrend;
  }

  // ---
  static public S104HeightTrendCompoundType [] createAndInitArray(final int dim) {

    S104HeightTrendCompoundType [] retArr= new S104HeightTrendCompoundType[dim];

    for(int idx= 0; idx < dim; idx++) {
      retArr[idx]= new S104HeightTrendCompoundType();
    }

    return retArr;
  }

  // ---
  public S104HeightTrendCompoundType set(final float waterLevelHeight,final byte waterLevelTrend) {
      
    this.WaterLevelHeight= waterLevelHeight;
    this.WaterLevelTrend= waterLevelTrend;

    return this;
  } 

  // ---
  final public float getWaterLevelHeight() {
  //public Float getWaterLevelHeight() {  
    return this.WaterLevelHeight;
  }

  // // ---
  // public float getUncertainty() {
  // //public Float getUncertainty() {
  //   return this.Uncertainty;
  // }  

  // ---
  final public byte getWaterLevelTrend() {
  //public Byte getWaterLevelTrend() {
    return this.WaterLevelTrend;
  }  
 
} // --- class S104HeightTrendCompoundType
