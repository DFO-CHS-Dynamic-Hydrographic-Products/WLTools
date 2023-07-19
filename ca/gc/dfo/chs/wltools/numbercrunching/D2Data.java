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
 * Generic class for two dimension matrix data.
 */
public abstract class D2Data extends D1Data implements INumberCrunching {
  
  /**
   * class String ID for static logging.
   */
  private static final String whoAmI = "ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.D2Data";
  /**
   * static log utility.
   */
  private static final Logger staticLog = LoggerFactory.getLogger(whoAmI);
  /**
   * object log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * The multi-dimensional memory mapping type used. Default is row major(a.k.a. C 2D array mapping)
   */
  protected MultiDimArrayIndexingType multiDimArrayIndexingType = MultiDimArrayIndexingType.ROW_MAJOR;
  /**
   * Number of columns.
   */
  //@Min(1)
  protected int ncols = 1;
  /**
   * Number of rows.
   */
  //@Min(1)
  protected int nrows = 1;
  
  /**
   * @param nbcolsAndRows : 2D square dimensions.
   */
  public D2Data(/*@Min(1)*/ final int nbcolsAndRows) {
    //--- Square matrix:
    this(nbcolsAndRows, nbcolsAndRows, DOUBLE_NO_DATA);
  }
  
  /**
   * @param nbcols    : Number of columns.
   * @param nbrows    : Number of rows.
   * @param initValue : Data initialization value.
   */
  public D2Data(/*@Min(1)*/ final int nbcols, /*@Min(1)*/ final int nbrows, final double initValue) {
    
    this();
    
    //--- NOTE: A 1 x 1 matrix is allowed but  1 x n or n x 1 are not. Just use the simple D1Data class for vectors.
    
    if ((nbcols == 1) && (nbrows > 1)) {
      this.log.error(whoAmI + " constructor: nbcols==1!, You must use a simple D1Data with dimension nbrows: " + nbrows);
      throw new RuntimeException(whoAmI + " constructor");
    }
    
    if ((nbrows == 1) && (nbcols > 1)) {
      this.log.error(whoAmI + " constructor : nbcols==1!, You must use a simple D1Data with dimension nbcols: " + nbcols);
      throw new RuntimeException(whoAmI + " constructor");
    }
    
    //--- Check the total dimension before allocating
    final long TDIM = nbcols * nbrows;
    
    if (TDIM > Integer.MAX_VALUE) {
      this.log.error(whoAmI + " constructor: nbcols*nbrows > Integer.MAX_VALUE !");
      throw new RuntimeException(whoAmI + " constructor");
    }
    
    //--- NOTE: We map a 2D matrix in a 1D vector to be sure to have an optimal(i.e. minimizing cache misses) memory
    // mapping for the data.
    this.data = new double[(int) TDIM];
    this.init(initValue);
    
    this.ncols = nbcols;
    this.nrows = nbrows;
  }
  
  /**
   * Default constructor.
   */
  public D2Data() {
    super();
    this.multiDimArrayIndexingType = MultiDimArrayIndexingType.ROW_MAJOR;
  }
  
  /**
   * @param nbcolsAndRows : 2D square dimensions.
   * @param initValue     : Data initialization value.
   */
  public D2Data(/*@Min(1)*/ final int nbcolsAndRows, final double initValue) {
    //--- Square matrix:
    this(nbcolsAndRows, nbcolsAndRows, initValue);
  }
  
  /**
   * @param nbcolsAndRows             : 2D square dimensions.
   * @param multiDimArrayIndexingType : The multi-dimensional memory mapping type wanted.
   */
  public D2Data(/*@Min(1)*/ final int nbcolsAndRows,
                /*@NotNull*/ final MultiDimArrayIndexingType multiDimArrayIndexingType) {

    this(nbcolsAndRows, DOUBLE_ACC_INIT, multiDimArrayIndexingType);
  }
  
  /**
   * @param nbcolsAndRows             : 2D square dimensions.
   * @param initValue                 : Data initialization value.
   * @param multiDimArrayIndexingType : The multi-dimensional memory mapping type wanted.
   */
  public D2Data(/*@Min(1)*/  final int nbcolsAndRows, final double initValue,
                /*@NotNull*/ final MultiDimArrayIndexingType multiDimArrayIndexingType) {

    this(nbcolsAndRows, nbcolsAndRows, initValue, multiDimArrayIndexingType);
  }
  
  /**
   * @param ncols                     : Number of columns.
   * @param nrows                     : Number of rows.
   * @param initValue                 : Data initialization value.
   * @param multiDimArrayIndexingType : The multi-dimensional memory mapping type wanted.
   */
  public D2Data(/*@Min(1)*/  final int ncols,
                /*@Min(1)*/  final int nrows,
                             final double initValue,
                /*@NotNull*/ final MultiDimArrayIndexingType multiDimArrayIndexingType) {
    
    this(ncols, nrows, initValue);
    this.multiDimArrayIndexingType = multiDimArrayIndexingType;
  }
  
  /**
   * @param nbcols : Number of columns.
   * @param nbrows : Number of rows.
   */
  public D2Data(/*@Min(1)*/ final int nbcols, /*@Min(1)*/ final int nbrows) {
    this(nbcols, nbrows, DOUBLE_ACC_INIT);
  }
  
  /**
   * @param ncols                     : Number of columns.
   * @param nrows                     : Number of rows.
   * @param multiDimArrayIndexingType : The multi-dimensional memory mapping type wanted.
   */
  public D2Data(/*@Min(1)*/  final int ncols,
                /*@Min(1)*/  final int nrows,
                /*@NotNull*/ final MultiDimArrayIndexingType multiDimArrayIndexingType) {

    this(ncols, nrows, DOUBLE_ACC_INIT, multiDimArrayIndexingType);
  }
  
  /**
   * Add d2m.data to this.data (i.e. member to member addition). We can use parent class add method for this purpose.
   * WARNING: No checks for this.data and d2m.data respective dimensions compatibility.
   *
   * @param d1Data2add : A D2Data object(which could be the same as this).
   * @return The D2Data object which is now this.data + d2m.data(i.e. member to member addition)
   */
  //@NotNull
  @Override
  public final D1Data add(/*@NotNull*/ final D1Data d1Data2add) {
    
    if (!this.checkMultiDimArrayIndexingConsistency((D2Data) d1Data2add)) {
      this.log.error(whoAmI + " add: d2m.multiDimArrayIndexingType != this.multiDimArrayIndexingType!");
      throw new RuntimeException(whoAmI + " add");
    }
    
    return super.add(d1Data2add);
  }
  
  /**
   * @param d2m : Another D2Data object.
   * @return true if this has the same multi-dimensional memory mapping type as d2m, false otherwise.
   */
  protected final boolean checkMultiDimArrayIndexingConsistency(/*@NotNull*/ final D2Data d2m) {
    return checkMultiDimArrayIndexingConsistency(this, d2m);
  }
  
  /**
   * @param d2a : A D2Data object.
   * @param d2b : Another D2Data object.
   * @return true if d2a has the same multi-dimensional memory mapping type as d2b, false otherwise.
   */
  protected static final boolean checkMultiDimArrayIndexingConsistency(/*@NotNull*/ final D2Data d2a,
                                                                       /*@NotNull*/ final D2Data d2b) {
    return (d2a.multiDimArrayIndexingType == d2b.multiDimArrayIndexingType);
  }
  
  /**
   * Column major (Fortran) 2D array mapping.
   *
   * @param col  : The column index.
   * @param row: The row index.
   * @return The column major 1D index mapping which correspond to matrix(col,row) element position.
   */
  //@Min(0)
  public final int colMajor(/*@Min(0)*/ final int col, /*@Min(0)*/ final int row) {
    return colMajor(col, row, this.ncols);
  }
  
  /**
   * Column major (Fortran) 2D array mapping.
   *
   * @param col   : The column index.
   * @param row:  The row index.
   * @param ncols : The number of columns of the 2D data mapping
   * @return The column major 1D index mapping which correspond to matrix(col,row) element position.
   */
  //@Min(0)
  public static final int colMajor(/*@Min(0)*/ final int col,
                                   /*@Min(0)*/ final int row,
                                   /*@Min(1)*/ final int ncols) {
    //return i + j*this.ni;
    return col + row * ncols;
  }
  
  /**
   * D2Data(matrix )x D1Data(column vector) product.
   * WARNING: No checks for this.data and colVector respective dimensions compatibility.
   *
   * @param colVector : Right hand side D1Data vector.
   * @return D1Data lhColVector holding this.data x colVector (matrix x column vector) result.
   */
  //@NotNull
  public final D1Data D2xColD1(/*@NotNull*/ final D1Data colVector) {
    return this.D2xColD1(colVector, new D1Data(colVector.data.length));
  }
  
  /**
   * D2Data(matrix )x D1Data(column vector) product.
   * WARNING: No checks for this.data and lhColVector respective dimensions compatibility.
   *
   * @param rhColVector : Right hand side D1Data vector.
   * @param lhColVector : Left hand side D1Data vector.
   * @return D1Data lhColVector holding this.data x lhColVector (matrix x column vector) result.
   */
  //@NotNull
  public final D1Data D2xColD1(/*@NotNull*/ final D1Data rhColVector,
                               /*@NotNull*/ final D1Data lhColVector) {
    
    for (int d = 0; d < lhColVector.data.length; d++) {
      
      //--- NOTE: Polymorphic this.rowDotProd method usage:
      lhColVector.data[d] = this.rowDotProd(d, rhColVector);
    }
    
    return lhColVector;
  }
  
  /**
   * D2Data row(at row index  row) x D1Data column vector. To be implemented by child-classes.
   *
   * @param row       : The row index of the D2Data matrix.
   * @param colVector : A D1Data column vector.
   * @return The dot product D2Data(at row index  row) x D1Data column vector.
   */
  abstract public double rowDotProd(/*@Min(0)*/ final int row,
                                    /*@NotNull*/ final D1Data colVector);
  
  /**
   * @return this.ncols.
   */
  //@Min(1)
  public final int ncols() {
    return this.ncols;
  }
  
  /**
   * @return this.nrows.
   */
  //@Min(1)
  public final int nrows() {
    return this.nrows;
  }
  
  /**
   * Row major (C) 2D array mapping.
   *
   * @param col  : The column index.
   * @param row: The row index.
   * @return The row major 1D index mapping which correspond to matrix(col,row) element position.
   */
  //@Min(0)
  public final int rowMajor(/*@Min(0)*/ final int col,
                            /*@Min(0)*/ final int row) {
    return rowMajor(col, row, this.nrows);
  }
  
  /**
   * Row major (C) 2D array mapping.
   *
   * @param col   : The column index.
   * @param row:  The row index.
   * @param nrows : The number of rows of the 2D data mapping.
   * @return The row major 1D index mapping which correspond to matrix(col,row) element position.
   */
  //@Min(0)
  public static final int rowMajor(/*@Min(0)*/ final int col,
                                   /*@Min(0)*/ final int row,
                                   /*@Min(1)*/ final int nrows) {
    //return j + i*this.nj;
    return row + col * nrows;
  }
  
  /**
   * Subtract d2m.data to this.data (i.e. member to member subtraction). We can use parent class add method for this
   * purpose.
   * WARNING: No checks for this.data and d2m.data respective dimensions compatibility.
   *
   * @param d2m : A D2Data object(which could be the same as this).
   * @return The D2Data object which is now this.data - d2m.data(i.e. member to member subtraction)
   */
  //@NotNull
  @Override
  public final D1Data subtract(/*@NotNull*/ final D1Data d2m) {
    
    //this.log.debug(this.whoAmI +": subtract: start");
    
    if (!this.checkMultiDimArrayIndexingConsistency((D2Data) d2m)) {
      
      this.log.error(whoAmI + " subtract: d2m.multiDimArrayIndexingType != this.multiDimArrayIndexingType!");
      throw new RuntimeException(whoAmI + " subtract");
    }
    
    //this.log.debug(this.whoAmI +": subtract: end");
    
    return super.subtract(d2m);
  }
  
  /**
   * D2Data element selection. To be implemented by child-classes.
   *
   * @param col : The column index.
   * @param row : The row index.
   * @return The double data element.
   */
  abstract public double at(/*@Min(0)*/ final int col,
                            /*@Min(0)*/ final int row);
  
  /**
   * D1Data row vector x D2Data column(at column index col). To be implemented by child-classes.
   *
   * @param col       : The column index of the D2Data matrix.
   * @param rowVector : A D1Data row vector.
   * @return The dot product D1Data row vector x D2Data column.
   */
  abstract public double colDotProd(/*@Min(0)*/  final int col,
                                    /*@NotNull*/ final D1Data rowVector);
  
  //public abstract D1 D2xColD1(final D1 colD1);
  //public abstract D1 rowD1xD2(final D1 rowD1);
  
  /**
   * To be implemented by child-classes.
   *
   * @param col   : The column index.
   * @param row   : The row index.
   * @param value : The value used to set D2Data at col,row.
   * @return The D2Data object.
   */
  //@NotNull
  abstract public D2Data put(/*@Min(0)*/ final int col,
                             /*@Min(0)*/ final int row, final double value);
  
  /**
   * Do the product of a D1Data column vector with a D1Data row vector and store the result in the D2Data object(i.e.
   * it is a Nx1*1xN vectors product)
   * WARNING: No checks to validate if the two D1Data objects have the same dimensions and if this.data dimensions
   * are also compatible with them.
   * To be implemented by child-classes.
   * The dimensions validation code could be de-commented for debugging purposes.
   *
   * @param colVector : A D1Data object. Must have a number of rows equal to the rowVector number of columns.
   * @param rowVector : Another D1Data object. Must have a number of columns equal to the colVector number of rows.
   * @return The D2Data object. Must have NxN square dimensions.
   */
  //@NotNull
  abstract public D2Data colD1xRowD1(/*@NotNull*/ final D1Data colVector,
                                     /*@NotNull*/ final D1Data rowVector);
  
  /**
   * @return A string representation of this.data. To be implemented by child-classes.
   */
  //@NotNull
  abstract public String toString();
}
