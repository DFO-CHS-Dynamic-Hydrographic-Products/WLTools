//package ca.gc.dfo.iwls.fmservice.modeling.numbercrunching;
package ca.gc.dfo.chs.wltools.numbercrunching;

/**
 *
 */

//---

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.Min;
//import javax.validation.constraints.NotNull;

//---

/**
 * Number crunching utility data object that can be represented in two dimension(a.k.a. as a rectangular or square
 * matrix)
 * in column major array ordering(a.k.a. Fortran multidimensional array mapping order). Matrix indices are beginning
 * at 0 of course.
 */
public final class D2ColumnMajorData extends D2Data implements INumberCrunching {
  
  /**
   * class String ID for static log.
   */
  private static final String whoAmI = "ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.D2ColumnMajorData";
  /**
   * static log utility.
   */
  private static final Logger staticLogger = LoggerFactory.getLogger(whoAmI);
  /**
   * object log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  
  /**
   * Default constructor.
   */
  public D2ColumnMajorData() {
    super();
    this.multiDimArrayIndexingType = MultiDimArrayIndexingType.COLUMN_MAJOR;
  }
  
  /**
   * @param ncolsAndRows : Square dimensions.
   */
  public D2ColumnMajorData(/*@Min(1)*/ final int ncolsAndRows) {

    super(ncolsAndRows, DOUBLE_NO_DATA, MultiDimArrayIndexingType.COLUMN_MAJOR);
  }
  
  /**
   * @param ncolsAndRows : Square dimensions.
   * @param initVal      : Initialization value.
   */
  public D2ColumnMajorData(/*@Min(1)*/ final int ncolsAndRows, final double initVal) {

    super(ncolsAndRows, initVal, MultiDimArrayIndexingType.COLUMN_MAJOR);
  }
  
  /**
   * @param ncols : Number of columns.
   * @param nrows : Number of rows.
   */
  public D2ColumnMajorData(/*@Min(1)*/ final int ncols, /*@Min(1)*/ final int nrows) {

    super(ncols, nrows, DOUBLE_NO_DATA, MultiDimArrayIndexingType.COLUMN_MAJOR);
  }
  
  /**
   * @param ncols   : Number of columns.
   * @param nrows   : Number of rows.
   * @param initVal : Initialization value.
   */
  public D2ColumnMajorData(/*@Min(1)*/ final int ncols,
                           /*@Min(1)*/ final int nrows,
                                       final double initVal) {

    super(ncols, nrows, initVal, MultiDimArrayIndexingType.COLUMN_MAJOR);
  }
  
  /**
   * D2Data matrix element selection.
   * WARNING: We need performance so no checks if col, row are valid indices.
   *
   * @param col : The column number of the element wanted.
   * @param row : The row number of the element wanted.
   * @return The Matrix element data wanted.
   */
  @Override
  public final double at(/*@Min(0)*/ final int col, /*@Min(0)*/ final int row) {
    return this.data[D2Data.colMajor(col, row, this.ncols)];
  }
  
  /**
   * D1Data row vector and D2Data matrix column scalar product operation.
   * WARNING: We need performance so no checks if col is a valid index or if the D1Data vector dimension is
   * compatible with this.nrows)
   *
   * @param col       : The column index of this.data. Must obviously be less than this.ncols.
   * @param rowVector : The row vector which must have at least nrows elements.
   * @return The dot product of the row vector argument and the column col of this.data.
   */
  @Override
  public final double colDotProd(/*@Min(0)*/  final int col,
                                 /*@NotNull*/ final D1Data rowVector) {
    
    double dp = 0.0;
    
    //--- [ v0 , v1, ..., v(nrows-1) ] * [ m(0,col)
    //                                     m(1,col)
    //                                     ...
    //                                     m(nrows-1,col) ]
    
    for (int r = 0; r < this.nrows; r++) {
      dp += this.data[D2Data.colMajor(col, r, this.ncols)] * rowVector.data[r];
    }
    
    return dp;
  }
  
  /**
   * Do the product of a D1Data column vector with a D1Data row vector and store the result in the D2Data object(i.e.
   * it is a Nx1*1xN vectors product)
   * WARNING: No checks to validate if the two D1Data objects have the same dimensions and if this.data dimensions
   * are also compatible with them.
   * The dimensions validation code could be de-commented for debugging purposes.
   *
   * @param colVector : A D1Data object. Must have a number of rows equal to the rowVector number of columns.
   * @param rowVector : Another D1Data object. Must have a number of columns equal to the colVector number of rows.
   * @return The D2Data object. Must have NxN square dimensions.
   */
  @Override
  public final D2Data colD1xRowD1(/*@NotNull*/ final D1Data colVector,
                                  /*@NotNull*/ final D1Data rowVector) {
    
    //--- Uncomment for debugging purposes.
//        if (this.nrows != this.ncols) {
//            this.log.error("this.nrows != this.ncols");
//            throw new RuntimeException(this.whoAmI+" colD1xRowD1") ;
//        }
//
//        if (this.nrows != colVector.data.length ) {
//            this.log.error("this.nrows != colVector.data.length");
//            throw new RuntimeException(this.whoAmI+" colD1xRowD1") ;
//        }
//
//        if (this.ncols != rowVector.data.length ) {
//            this.log.error("this.nrows != rowVector.data.length");
//            throw new RuntimeException(this.whoAmI+" colD1xRowD1") ;
//        }
    
    final int squDim = this.ncols;
    
    //--- Compute the square matrix which is the result of a Nx1*1xN vector product
    //    Here colVector is the Nx1 vector and rowVector is the 1xN vector
    //    NOTE: This outer loop is a good candidate for threads parallelization
    for (int r = 0; r < squDim; r++) {
      
      final double cvData = colVector.data[r];
      
      for (int c = 0; c < squDim; c++) {
        this.data[D2Data.colMajor(c, r, this.ncols)] = cvData * rowVector.data[c];
      }
    }
    
    return this;
  }
  
  /**
   * D1Data column vector and D2Data matrix row scalar product operation.
   * WARNING: We need performance so no checks if row is a valid index or if the column vector dimension is
   * compatible with this.ncols)
   *
   * @param row       : The row index of this.data. Must obviously be less than this.nrows.
   * @param colVector : The D1Data column vector which must have at least ncols elements.
   * @return The dot product of the D1Data column vector argument and the row row of this.data.
   */
  @Override
  public final double rowDotProd(/*@Min(0)*/  final int row,
                                 /*@NotNull*/ final D1Data colVector) {
    
    double dp = 0.0;
    
    //--- [ m(0,row), m(1,row), ..., m(ncols-1.row) ] * [ cv(0),
    //                                                    cv(1),
    //                                                    ...,
    //                                                    cv(ncols-1) ]
    
    for (int c = 0; c < this.ncols; c++) {
      dp += this.data[D2Data.colMajor(c, row, this.ncols)] * colVector.data[c];
    }
    
    return dp;
  }
  
  /**
   * Set this.value at indices col,row with a value.
   * WARNING: We need performance so no validity checks are made if col,row indices are compatible with this.value
   * dimensions.
   *
   * @param col   : The column index of this.value.
   * @param row   : The row index of this.value.
   * @param value : A double value used to set this.value at indices col,row
   * @return The D2Data object.
   */
  @Override
  public final D2Data put(/*@Min(0)*/ final int col,
                          /*@Min(0)*/ final int row,
                                      final double value) {
    
    this.data[D2Data.colMajor(col, row, this.ncols)] = value;
    
    return this;
  }
  
  /**
   * @return A string representation of this.data.
   */
  //@NotNull
  @Override
  public final String toString() {
    
    StringBuffer ret = new StringBuffer("\n\n ");
    
    for (int r = 0; r < this.nrows; r++) {
      for (int c = 0; c < this.ncols; c++) {
        ret.append(" " + this.data[D2Data.colMajor(c, r, this.nrows)]);
      }
      
      ret.append("\n ");
    }
    
    //ret.append("\n ");
    
    return ret.toString();
  }
}
