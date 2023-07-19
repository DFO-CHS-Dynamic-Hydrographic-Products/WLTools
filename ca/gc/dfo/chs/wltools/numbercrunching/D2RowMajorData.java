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
 * in row major array ordering(a.k.a. C multidimensional array mapping order). Matrix indices are beginning at 0 of
 * course.
 */
public final class D2RowMajorData extends D2Data implements INumberCrunching {
  
  /**
   * class String ID for static logging.
   */
  private static final String whoAmI = "ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.D2RowMajorData";
  /**
   * static log utility.
   */
  private static final Logger staticLog = LoggerFactory.getLogger(whoAmI);
  /**
   * object log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  
  /**
   * Default constructor.
   */
  public D2RowMajorData() {
    super();
    this.multiDimArrayIndexingType = MultiDimArrayIndexingType.ROW_MAJOR;
  }
  
  /**
   * @param ncolsAndRows : Same number of rows and columns: square matrix.
   */
  public D2RowMajorData(/*@Min(1)*/ final int ncolsAndRows) {
    super(ncolsAndRows, DOUBLE_NO_DATA, MultiDimArrayIndexingType.ROW_MAJOR);
  }
  
  /**
   * @param ncolsAndRows : Same number of rows and columns: square matrix.
   * @param initVal      : Double value to use for data initialization.
   */
  public D2RowMajorData(/*@Min(1)*/ final int ncolsAndRows, /*@Min(1)*/ final double initVal) {
    super(ncolsAndRows, initVal, MultiDimArrayIndexingType.ROW_MAJOR);
  }
  
  /**
   * @param ncols : Number of columns.
   * @param nrows : Numeber of rows.
   */
  public D2RowMajorData(/*@Min(1)*/ final int ncols, /*@Min(1)*/ final int nrows) {
    super(ncols, nrows, DOUBLE_NO_DATA, MultiDimArrayIndexingType.ROW_MAJOR);
  }
  
  /**
   * @param ncols   : Number of columns.
   * @param nrows   : Numeber of rows.
   * @param initVal : Double value to use for data initialization.
   */
  public D2RowMajorData(/*@Min(1)*/ final int ncols,
                        /*@Min(1)*/ final int nrows,
                                    final double initVal) {
    super(ncols, nrows, initVal, MultiDimArrayIndexingType.ROW_MAJOR);
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
    return this.data[D2Data.rowMajor(col, row, this.nrows)];
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
  public final double colDotProd(/*@Min(0)*/ final int col, /*@NotNull*/ final D1Data rowVector) {
    
    double dp = 0.0;
    
    //--- [ v0 , v1, ..., v(nrows-1) ] * [ m(0,col)
    //                                     m(1,col)
    //                                     ...
    //                                     m(nrows-1,col) ]
    
    for (int r = 0; r < this.nrows; r++) {
      dp += this.data[D2Data.rowMajor(col, r, this.nrows)] * rowVector.data[r];
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
    
    //--- Fool-proof dimensions validation: uncomment it for debugging purposes.
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
//            this.log.error("this.ncols != rowVector.data.length");
//            throw new RuntimeException(this.whoAmI+" colD1xRowD1") ;
//        }
    
    final int squDim = this.ncols;
    
    //--- Compute the matrix which is the result of a Nx1*1xN vectors product
    //    Here colVector is the Nx1 vector and rowVector is the 1xN vector
    //    NOTE: The outer loop on squDim is a good candidate for threads parallelization.
    for (int c = 0; c < squDim; c++) {
      
      final double cvData = colVector.data[c];
      
      for (int r = 0; r < squDim; r++) {
        this.data[D2Data.rowMajor(c, r, this.nrows)] = cvData * rowVector.data[r];
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
      dp += this.data[D2Data.rowMajor(c, row, this.nrows)] * colVector.data[c];
    }
    
    return dp;
  }
  
  /**
   * Set this.data at indices col,row with a value.
   * WARNING: We need performance so no validity checks are made if col,row indices are compatible with this.data
   * dimensions.
   *
   * @param col   : The column index of this.data.
   * @param row   : The row index of this.data.
   * @param value : A double value used to set this.data at indices col,row
   * @return The D2Data object.
   */
  @Override
  public final D2Data put(/*@Min(0)*/ final int col,
                          /*@Min(0)*/ final int row,
                                      final double value) {
    
    //try {
    
    this.data[D2Data.rowMajor(col, row, this.nrows)] = value;
    
    //} catch (ArrayIndexOutOfBoundsException e) {
    //  throw new RuntimeException(e) ;
    //}
    
    return this;
  }
  
  /**
   * @return A string representation of this.data.
   */
  //@NotNull
  @Override
  public final String toString() {
    
    StringBuffer ret = new StringBuffer("\n\n ");
    
    for (int c = 0; c < this.ncols; c++) {
      for (int r = 0; r < this.nrows; r++) {
        ret.append(" " + this.data[D2Data.rowMajor(c, r, this.nrows)]);
      }
      
      ret.append("\n ");
    }
    
    //ret.append("\n ");
    
    return ret.toString();
  }
  
}
