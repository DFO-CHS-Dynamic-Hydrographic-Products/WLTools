//package ca.gc.dfo.iwls.fmservice.modeling.numbercrunching;
package ca.gc.dfo.chs.wltools.numbercrunching;

/**
 *
 */

//---
//import javax.validation.constraints.Min;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.NotNull;

//---

/**
 * Utility class for statistics computations using D1Data and D2Data classes.
 */
abstract public class Statistics implements INumberCrunching {
  
  /**
   * static log utility
   */
  private static final Logger staticLogger = LoggerFactory.getLogger("Statistics");
  
  //public Statistics() { }
  
  /**
   * @param matrix : D2Data object with nrows x ncols (a.k.a. matrix)
   * @return A ncols x 1 D1Data object (a.k.a. vector) having the matrix columns (unbiased) standard deviations as
   * elements
   * (i.e. item 0 of the returned D1Data object is the standard deviation of the 0th column of the D2Data
   * matrix argument).
   */
  //@NotNull
  public static D1Data columnsStdDev(/*@NotNull*/ final D2Data matrix) {
    
    if (matrix.nrows < 2) {
      staticLogger.error("Statistics columnsStdDev: matrix.nrows < 2 !");
      throw new RuntimeException("Statistics columnsStdDev");
    }
    
    if (matrix.ncols == 0) {
      staticLogger.error("Statistics columnsStdDev: matrix.ncols == 0 !");
      throw new RuntimeException("Statistics columnsStdDev");
    }
    
    double colAvg = 0.0;
    
    final D1Data ret = new D1Data(matrix.ncols);
    
    //--- NOTE: This is more efficient with a row major array indexing type
    for (int col = 0; col < matrix.ncols; col++) {
      
      //--- column mean:
      for (int row = 0; row < matrix.nrows; row++) {
        colAvg += matrix.at(col, row);
      }
      
      colAvg /= matrix.nrows;
      
      double colStdDev = 0.0;

 //--- column std dev
      for (int row = 0; row < matrix.nrows; row++) {
        
        final double diff = matrix.at(col, row) - colAvg;
        
        colStdDev += diff * diff;
      }
      
      //--- NOTE: Need to divide by matrix.nrows-1 to get the unbiased std dev.
      ret.data[col] = Math.sqrt(colStdDev / (matrix.nrows - 1));
    }
    
    return ret;
  }
  
  /**
   * @param matrix : D2Data object with nrows x ncols (a.k.a. matrix)
   * @return A nrows x 1 D1Data object (a.k.a. vector) having the matrix rows (unbiased) standard deviations as elements
   * (i.e. item 0 of the returned D1Data object is the standard deviation of the 0th row of the D2Data matrix
   * argument).
   */
  //@NotNull
  public static D1Data rowsStdDev(/*@NotNull*/ final D2Data matrix) {
    
    if (matrix.ncols < 2) {
      staticLogger.error("Statistics rowsStdDev: matrix.ncols < 2 !");
      throw new RuntimeException("Statistics rowsStdDev");
    }
    
    if (matrix.nrows == 0) {
      staticLogger.error("Statistics rowsStdDev: matrix.nrows == 0 !");
      throw new RuntimeException("Statistics rowsStdDev");
    }
    
    double rowAvg = 0.0;
    
    final D1Data ret = new D1Data(matrix.nrows);
    
    //--- NOTE: This is more efficient with a column major array indexing type
    for (int row = 0; row < matrix.nrows; row++) {
      
      //--- row mean:
      for (int col = 0; col < matrix.ncols; col++) {
        rowAvg += matrix.at(col, row);
      }
      
      rowAvg /= matrix.ncols;
      
      double rowStdDev = 0.0;
      
      //--- column std dev
      for (int col = 0; col < matrix.ncols; col++) {
        
        final double diff = matrix.at(col, row) - rowAvg;
        
        rowStdDev += diff * diff;
      }
      
      //--- NOTE: Need to divide by matrix.ncols-1 to get the unbiased std dev.
      ret.data[row] = Math.sqrt(rowStdDev / (matrix.ncols - 1));
    }
    
    return ret;
  }
}
