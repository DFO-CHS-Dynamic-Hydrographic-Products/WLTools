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

//import javax.validation.constraints.Size;
//---

/**
 * Bare metal class for data number crunching.
 */
public class D1Data implements INumberCrunching {
  
  /**
   * class String ID for static log.
   */
  private static final String whoAmI = "ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.D1Data";
  /**
   * static log utility.
   */
  private static final Logger staticLogger = LoggerFactory.getLogger(whoAmI);
  /**
   * object log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * Contiguous memory usage to minimize cache misses when using huge data dimension(s).
   */
  protected double[] data = null;
  
  /**
   * Default constructor.
   */
  public D1Data() {
    this.data = null;
  }
  
  /**
   * @param datadim : Data dimension.
   */
  public D1Data(/*@Min(0)*/ final int datadim) {

    this(datadim, DOUBLE_ACC_INIT);
  }
  
  /**
   * @param datadim   : Data dimension.
   * @param initValue : Data initialization value.
   */
  public D1Data(/*@Min(0)*/ final int datadim, final double initValue) {
    
    //--- Contiguous memory usage to minimize cache misses when using huge data dimension(s).
    this.data = new double[datadim];
    
    this.init(initValue);
  }
  
  /**
   * @param initValue : Data initialization value.
   * @return This D1Data object.
   */
  final public D1Data init(final double initValue) {
    
    for (int d = 0; d < this.data.length; d++) {
      this.data[d] = initValue;
    }
    
    return this;
  }
  
  /**
   * @param dataInit : double precision data array that will be copied in this.data.
   */
  //@NotNull
  public D1Data(/*@NotNull*/ final double[] dataInit) {
    
    //--- NOTE: It is a deep deepCopy here so it is NOT efficient at all for huge data !
    this.data = dataInit;
  }
  
  /**
   * Static method to get the dot product between two D1Data objects. WARNING: No checks on the respective dimensions
   * compatibility.
   *
   * @param ld1Vector : A D1Data object.
   * @param rd1Vector : Another D1Data object.
   * @return the dot product between ld1Vector and rd1Vector.
   */
  final public static double dotProd(/*@NotNull*/ final D1Data ld1Vector, /*@NotNull*/ final D1Data rd1Vector) {
    
    double dotProd = 0.0;
    
    //--- Uncomment this block for debugging purposes.
//        if (ld1Vector.data.length != rd1Vector.data.length)  {
//            staticLogger.error(whoAmI+" static dotProd :  ld1.data.length != rd1.data.length !");
//            throw new RuntimeException(this.whoAmI+" static dotProd");
//        }
    
    for (int d = 0; d < ld1Vector.data.length; d++) {
      dotProd += ld1Vector.data[d] * rd1Vector.data[d];
    }
    
    return dotProd;
  }
  
  /**
   * @param initValue : Data initialization value.
   * @param d1        : D1Data object to initialize with initValue.
   * @return The D1Data d1 object.
   */
  //@NotNull
  final public static D1Data init(final double initValue, /*@NotNull*/ final D1Data d1) {
    return d1.init(initValue);
  }
  
  //--- For possible future usage.
//    private static void checkDim(@Min(0) final int dim) {
//
//        if (dim <= 0) {
//            staticLogger.error("D1Data at: dim <= 0 !");
//            throw new RuntimeException(this.whoAmI+" at");
//        }
//    }
  
  /**
   * Add some D1Data contents to this.data contents. WARNING: No checks on the respective dimensions compatibility.
   *
   * @param d1Data2add : Another(or the same as this) D1Data object which data will be added to this.data. Must have
   *                   the same dimension(or larger) as this.data.
   * @return The D1Data object.
   */
  //@NotNull
  public D1Data add(/*@NotNull*/ final D1Data d1Data2add) {
    
    //--- Uncomment the following block for debugging purposes.
//        if (this.data.length > d1Data2add.data.length) {
//            this.log.error(this.whoAmI+" add: this.data.length > d1Data2add.data.length");
//            throw new RuntimeException("D1Data add");
//        }
    
    for (int d = 0; d < this.data.length; d++) {
      this.data[d] += d1Data2add.data[d];
    }
    
    return this;
  }
  
  /**
   * Add some D1Data contents to this.data contents and put the results in a third D1Data object. WARNING: No checks
   * on the respective dimensions compatibility.
   *
   * @param d1Data2add   : Another(or the same as this) D1Data object which data will be added to this.data. Must have
   *                     the same dimension(or larger) as this.data.
   * @param d1DataResult : Another(or the same) D1Data object wher to put the addition results. Must have the same
   *                     dimension(or larger) as this.data.
   * @return d1DataResult
   */
  //@NotNull
  public D1Data add(/*@NotNull*/ final D1Data d1Data2add, /*@NotNull*/ final D1Data d1DataResult) {
    
    //--- Uncomment the following two blocks for debugging purposes.
//        if (this.data.length > d1Data2add.data.length) {
//            this.log.error(this.whoAmI+" add : this.data.length > d1Data2add.data.length");
//            throw new RuntimeException(this.whoAmI+" add");
//        }
//
//        if (this.data.length > d1DataResult.data.length) {
//            this.log.error(this.whoAmI+" add : this.data.length > d1DataResult.data.length");
//            throw new RuntimeException(this.whoAmI+ " add");
//        }
    
    for (int d = 0; d < this.data.length; d++) {
      d1DataResult.data[d] = this.data[d] + d1Data2add.data[d];
    }
    
    return d1DataResult;
  }
  
  //--- For possible future usage:
//   public final D2 colD1xRowD1(final D1 rowD1, final MultiDimArrayIndexing multiDimArrayIndexingType) {
//
//     final int squDim= this.data.length;
//
//     //--- Compute the matrix which is the result of a Nx1*1xN vectors product.
//     //    Here the Nx1 vector is this D1 Object and the 1xN is the D1 argument rowD1
//     final D2 ret= (multiDimArrayIndexingType==MultiDimArrayIndexing.ROW_MAJOR ? new D2RM(squDim,Init
//     .DOUBLE_ACC_INIT) : new D2CM(squDim,Init.DOUBLE_ACC_INIT) );
//
//     for (int c= 0; c< squDim; c++) {
//       final double thisData= this.data[c];
//
//       for (int r= 0; r< squDim; r++) {
//         ret.put(c,r,thisData*rowD1.data[r]);
//       }
//     }
//
//     return ret;
//   }
  
  /**
   * Get this.data value at index at. WARNING: No array lower,upper bounds checking here.
   *
   * @param at : Data index
   * @return The data value at index at in this.data.
   */
  final public double at(/*@Min(0)*/ final int at) {
    
    //--- WARNING: No array bound checking here.
    return this.data[at];
  }
  
  /**
   * Deep deepCopy of a D1Data object that we want to deepCopy the contents in this.data.
   * WARNING1: This is REALLY NOT optimal for huge data dimensions !
   * WARNING2: No checks on the respective dimensions compatibility.
   *
   * @param from : Another D1Data that we want to deepCopy in this.data.
   * @return The D1Data object this.
   */
  //@NotNull
  final public D1Data deepCopy(/*@NotNull*/ final D1Data from) {
    
    //--- Uncomment the following two blocks for debugging purposes.
//        if (this==from) {
//            this.log.error(this.whoAmI+" deepCopy: this==from !");
//            throw new RuntimeException( this.whoAmI+" deepCopy");
//        }
//
//        if (this.data.length<from.data.length) {
//            this.log.error(this.whoAmI+" deepCopy: this.data.length<from.data.length !");
//            throw new RuntimeException(this.whoAmI+" deepCopy");
//        }
    
    //--- NOTE: The simple objects references assignation is NOT a deep deepCopy.
    //          We need to loop to really get what we want here:
    //this.data= fr.data;
    
    for (int d = 0; d < from.data.length; d++) {
      this.data[d] = from.data[d];
    }
    
    return this;
  }
  
  /**
   * Do a D2Data matrix x D1Data column vector product. WARNING: No checks on the respective dimensions compatibility.
   *
   * @param d2Matrix    : D2Data matrix.
   * @param d1ColVector : D1Data column vector.
   * @return this D1Data holding the D2Data matrix x D1Data column vector product.
   */
  //@NotNull
  final public D1Data D2xColD1(/*@NotNull*/ final D2Data d2Matrix, /*@NotNull*/ final D1Data d1ColVector) {
    
    //--- Uncomment the following two blocks for debugging purposes.
//        if (this.data.length != d2Matrix.ncols) {
//            this.log.error(this.whoAmI+" D2xColD1: this.data.length != d2M.ncols !");
//            throw new RuntimeException(this.whoAmI+" D2xColD1");
//        }
//
//        if (this.data.length != d1ColVector.data.length) {
//            this.log.error(this.whoAmI+" D2xColD1: this.data.length != d1V.data.length !");
//            throw new RuntimeException(this.whoAmI+" D2xColD1");
//        }
    
    for (int d = 0; d < this.data.length; d++) {
      this.data[d] = d2Matrix.rowDotProd(d, d1ColVector);
    }
    
    //this.log.debug(this.whoAmI+" D2xColD1: this="+this.toString());
    
    return this;
  }
  
  /**
   * Dot product of a D1Data object with itself.
   *
   * @return The dot product with itself.
   */
  final public double dotProd() {
    
    double dotProd = 0.0;
    
    //--- NOTE: another could be this.
    for (int d = 0; d < this.data.length; d++) {
      dotProd += this.data[d] * this.data[d];
    }
    
    return dotProd;
  }
  
  /**
   * Dot product of this D1Data object with another D1Data object. WARNING: No checks on the respective dimensions
   * compatibility.
   *
   * @param d1Vector :  another D1Data object.
   * @return The dot product of this D1Data object with d1Vector
   */
  final public double dotProd(/*@NotNull*/ final D1Data d1Vector) {
    
    double dotProd = 0.0;
    
    //--- Uncomment this block for debugging purposes.
//        if (d1Vector.data.length != this.data.length)  {
//            this.log.error(this.whoAmI+" dotProd: d1.data.length != this.data.length !");
//            throw new RuntimeException(this.whoAmI+" dotProd");
//        }
    
    //--- NOTE: another could be this.
    for (int d = 0; d < this.data.length; d++) {
      dotProd += this.data[d] * d1Vector.data[d];
    }
    
    return dotProd;
  }
  
  /**
   * @param value : The multiplication value.
   * @return This D1Data object.
   */
  /*@NotNull*/
  final public D1Data multWith(final double value) {
    
    for (int d = 0; d < this.data.length; d++) {
      this.data[d] *= value;
    }
    
    return this;
  }
  
  //--- mehod for possible future usage
//    @NotNull
//    final public D1Data multWith(final double dval, @NotNull final D1Data target) {
//
//        if (target.data.length!=this.data.length) {
//            this.log.error("D1Data multWith : target.data.length!=this.data.length !");
//            throw new RuntimeException(this.whoAmI+" multWith") ;
//        }
//
//        for (int d=0; d< this.data.length; d++) {
//            target.data[d]= dval*this.data[d];
//        }
//
//        return target;
//    }
  
  /**
   * @param value : The value to put at index index. WARNING: No check if index is valid.
   * @param index : The index where to put value.
   */
  final public void put(final double value, /*@Min(0)*/ final int index) {
    this.data[index] = value;
  }
  
  /**
   * Get the product of this.data(considered as a row vector) with a D2Data matrix. WARNING: No checks on the
   * respective dimensions compatibility.
   *
   * @param d2Matrix : A D2Data matrix object.
   * @return A new D1Data object resulting from the product of this.data(vector) with the D2Data object d2Matrix.
   */
  @NotNull
  final public D1Data rowD1xD2(/*@NotNull*/ final D2Data d2Matrix) {
    
    final D1Data rhd1 = new D1Data(this.data.length);
    
    return this.rowD1xD2(d2Matrix, rhd1);
    
  }
  
  /**
   * Get the product of this.data(considered as a 1 x n row vector) with a n x m D2Data matrix. WARNING: No checks on
   * the respective dimensions compatibility.
   *
   * @param d2Matrix   : A D2Data matrix object.
   * @param rhd1Vector : The D1Data row vector object where to put the result of the product of this.data with d2Matrix.
   * @return A new D1Data object resulting from the product of this.data(vector) with the D2Data object d2Matrix.
   */
  //@NotNull
  final public D1Data rowD1xD2(/*@NotNull*/ final D2Data d2Matrix, /*@NotNull*/ final D1Data rhd1Vector) {
    
    //--- (1 x m) = (1 x n) * (n x m) NOTE: We can have m==n.
    
    for (int d = 0; d < this.data.length; d++) {
      rhd1Vector.data[d] = d2Matrix.colDotProd(d, this);
    }
    
    return rhd1Vector;
  }
  
  /**
   * @return The size of this.data.
   */
  //@Min(1)
  final public int size() {
    return this.data.length;
  }
  
  /**
   * Subtract a D1Data vector from this.data. WARNING: No checks on the respective dimensions compatibility.
   *
   * @param sub : A D1Data vector object to subtract from this.data
   * @return This D1Data object.
   */
  //@NotNull
  public D1Data subtract(/*@NotNull*/ final D1Data sub) {
    
    //this.log.debug(whoAmI+" subtract 1: start ");
    
    //--- Uncomment this block for debugging purposes.
//        if (this.data.length > sub.data.length) {
//            this.log.error(this.whoAmI+" subtract: this.data.length > sub.data.length");
//            throw new RuntimeException(this.whoAmI+" subtract") ;
//        }
    
    for (int d = 0; d < this.data.length; d++) {
      this.data[d] -= sub.data[d];
    }
    
    //this.log.debug(this.whoAmI+" subtract 1: end ");
    
    return this;
  }
  
  /**
   * @return A String to display this.data as a COLUMN_VECTOR.
   */
  //@NotNull
  public String toString() {
    return this.toString(D1Type.COLUMN_VECTOR);
  }
  
  /**
   * @param type : D1Type to use for the display String.
   * @return A String to display this.data as a COLUMN_VECTOR or as a ROW_VECTOR.
   */
  //@NotNull
  final public String toString(final D1Type type) {
    return (type == D1Type.ROW_VECTOR ? this.rowVectorString() : this.columnVectorString());
  }
  
  //--- For possible future usage:
//    @NotNull
//    final public D1Data subtract(@NotNull final D1Data sub, @NotNull final D1Data d1r) {
//
//        if (this.data.length > sub.data.length) {
//            this.log.error(this.whoAmI+" subtract: this.data.length > sub.data.length");
//            throw new RuntimeException(this.whoAmI+" subtract") ;
//        }
//
//        if (this.data.length > d1r.data.length) {
//            this.log.error(this.whoAmI+" subtract: this.data.length > d1r.data.length");
//            throw new RuntimeException(this.whoAmI+" subtract") ;
//        }
//
//        for (int d= 0; d< this.data.length; d++) {
//            d1r.data[d]= this.data[d] - sub.data[d];
//        }
//
//        return d1r;
//    }
  
  /**
   * @return A String to display this.data as a row vector.
   */
  //@NotNull
  final public String rowVectorString() {
    
    StringBuilder ret = new StringBuilder("\n\n " + this.data[0] + " ");
    
    for (int d = 1; d < this.data.length; d++) {
      ret.append(this.data[d] + " ");
    }
    
    ret.append("\n");
    
    return ret.toString();
  }
  
  /**
   * @return A String to display this.data as a column vector.
   */
  //@NotNull
  final public String columnVectorString() {
    
    StringBuilder ret = new StringBuilder("\n\n " + this.data[0] + "\n ");
    
    for (int d = 1; d < this.data.length; d++) {
      ret.append(this.data[d] + "\n ");
    }
    
    return ret.toString();
  }
}
