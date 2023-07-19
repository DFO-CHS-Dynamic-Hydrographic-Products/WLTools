//package ca.gc.dfo.iwls.fmservice.modeling.numbercrunching;
package ca.gc.dfo.chs.wltools.numbercrunching;

/**
 *
 */

/**
 * Interface for NumberCrunching utilities.
 */
public interface INumberCrunching {
  
  //--- int constants
  int UNDEFINED = -1;
  
  //--- double precision constants:
  double DOUBLE_ACC_INIT = 0.0;
  double DOUBLE_NO_DATA = -888888888.8888;
  
  /**
   * Define 1D data(vector) types
   */
  enum D1Type {ROW_VECTOR, COLUMN_VECTOR}
  
  /**
   * Define 2D matrix memory mappings: row major(a.k.a. C mapping) and column major(a.k.a. Fortran mapping)
   */
  enum MultiDimArrayIndexingType {
    ROW_MAJOR,    //--- C multidimensional array mapping order.
    COLUMN_MAJOR  //--- Fortran multidimensional array mapping order.
  }
}
