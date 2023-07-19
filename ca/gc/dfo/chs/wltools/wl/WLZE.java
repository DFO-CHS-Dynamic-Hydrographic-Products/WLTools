package ca.gc.dfo.iwls.fmservice.modeling.wl;

/**
 *
 */

//---

import ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.D1Data;
import ca.gc.dfo.iwls.fmservice.modeling.numbercrunching.D2Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

//---
//---

/**
 * class WLZE contains a Z water level value and its associated error.
 * (Using WLZE name instead of simple WL name to avoid confusion with WL acronym used in the source code comments.)
 */
public class WLZE implements IWL {
  
  //--- TODO: Implement a child class wich contains a reference to a(probably static) vertically geo-referenced
  // (geolatte?) object.
  
  /**
   * Using a static Logger Object for static methods
   */
  private static final Logger staticLogger = LoggerFactory.getLogger("WL");
  /**
   * A water level which could be a full Z water level offset from a vertical reference(a local zero chart OR a
   * global one) OR a surge value.
   */
  protected double zw = 0.0;
  /**
   * zw error(uncertainty).
   */
  protected double error = 0.0;

//    public WLZE() {
//        this.set(0.0,0.0);
//    }
//    public WLZE(final double zw) {
//        this.set(zw,0.0);
//    }
  
  /**
   * @param zw    : Z water level(or surge) value.
   * @param error : Z error(uncertainty).
   */
  public WLZE(final double zw, final double error) {
    this.set(zw, error);
  }
  
  /**
   * Set the Z and the errors values of a WLZE object.
   *
   * @param zw    : The WL Z value to set.
   * @param error : The WL error value to set.
   * @return WLZE : this WLZE object.
   */
  public final WLZE set(final double zw, final double error) {
    
    this.zw = zw;
    this.error = error;
    
    return this;
  }
  
  /**
   * Add a Z value to all the Z values of a List of WLZE objects
   *
   * @param zw        : The Z value to add.
   * @param wlzeBunch : The List of WLZE objects.
   * @return List of WLZE : The List WLZEa.
   */
  @NotNull
  public final static List<WLZE> add(final double zw, @NotNull @Size(min = 1) final List<WLZE> wlzeBunch) {
    
    if (wlzeBunch.isEmpty()) {
      staticLogger.error("WLZE add: wlzeBunch is Empty !");
      throw new RuntimeException("WLZE add method");
    }
    
    for (final WLZE wlze : wlzeBunch) {
      wlze.zw += zw;
    }
    
    return wlzeBunch;
  }
  
  /**
   * Multiply all the Z values of a List of WLZE objects with a double value.
   *
   * @param zw        : The Z value to add.
   * @param wlzeBunch : The List of WLZE objects.
   * @return List of WLZE : The List WLZEa.
   */
  public final static List<WLZE> multWith(final double zw, @NotNull @Size(min = 1) final List<WLZE> wlzeBunch) {
    
    for (final WLZE wlze : wlzeBunch) {
      wlze.zw *= zw;
    }
    
    return wlzeBunch;
  }
  
  /**
   * Create a new D1Data object with all the Z Values of a List of WLZE objects.
   *
   * @param wlzeBunch The List of WLZE objects.
   * @return new D1Data object
   */
  public final static D1Data populateD1(@NotNull @Size(min = 1) final List<WLZE> wlzeBunch) {
    
    if (wlzeBunch.isEmpty()) {
      staticLogger.error("WLZE populateD1: wlzeBunch.isEmpty() !");
      throw new RuntimeException("WLZE populateD1 method");
    }
    
    final D1Data d1 = new D1Data(wlzeBunch.size());
    
    int d = 0;
    
    for (final WLZE wlze : wlzeBunch) {
      d1.put(wlze.zw, d++);
    }
    
    return d1;
  }
  
  /**
   * Fill a column of an already existing D2Data(matrix) object with all the Z values of a List of WLZE objects.
   *
   * @param col       : The index of the column of the D2Data object.
   * @param wlzeBunch : A List of WLZE objects.
   * @param matrix    : A D2Data object.
   * @return matrix: The D2Data object with the column at index col filled with the Z values of the List of WLZE
   * objects.
   */
  public final static D2Data populateD2Col(@Min(0) final int col, @NotNull @Size(min = 1) final List<WLZE> wlzeBunch,
                                           @NotNull final D2Data matrix) {
    
    if (wlzeBunch.isEmpty()) {
      staticLogger.error("WLZE populateD2Col: wlzeBunch.isEmpty() !");
      throw new RuntimeException("WLZE populateD2Col method");
    }
    
    if (col < 0) {
      staticLogger.error("WLZE populateD2Col: col < 0 !");
      throw new RuntimeException("WLZE populateD2Col method");
    }
    
    if (col >= matrix.ncols()) {
      staticLogger.error("WLZE populateD2Col: col >= matrix.ncols() !");
      throw new RuntimeException("WLZE populateD2Col method");
    }
    
    if (wlzeBunch.size() > matrix.nrows()) {
      staticLogger.error("WLZE populateD2Col: wlzeBunch.size() > matrix.nrows() !");
      throw new RuntimeException("WLZE populateD2Col method");
    }
    
    int row = 0;
    
    for (final WLZE wlze : wlzeBunch) {
      matrix.put(col, row++, wlze.zw);
    }
    
    return matrix;
  }
  
  /**
   * Fill a row of an already existing D2Data(matrix) object with all the Z values of a List of WLZE objects.
   *
   * @param row       : The index of the row of the D2Data object.
   * @param wlzeBunch : A List of WLZE objects.
   * @param matrix    : A D2Data object.
   * @return matrix: The D2Data object with the row at index row filled with the Z values of the List of WLZE objects.
   */
  public final static D2Data populateD2Row(@Min(0) final int row, @NotNull @Size(min = 1) final List<WLZE> wlzeBunch,
                                           @NotNull final D2Data matrix) {
    
    if (wlzeBunch.isEmpty()) {
      staticLogger.error("WLZE populateD2Row: wlzeBunch.isEmpty() !");
      throw new RuntimeException("WLZE populateD2Row method");
    }
    
    if (row < 0) {
      staticLogger.error("\"WLZE populateD2Row: row < 0 !");
      throw new RuntimeException("WLZE populateD2Row method");
    }
    
    //final int nrows= matrix.nrows();
    
    if (row >= matrix.nrows()) {
      staticLogger.error("WLZE populateD2Row: row >= matrix.nrows() !");
      throw new RuntimeException("WLZE populateD2Row method");
    }
    
    if (wlzeBunch.size() > matrix.ncols()) {
      staticLogger.error("WLZE populateD2Row: wlzeBunch.size() > matrix.ncols() !");
      throw new RuntimeException("WLZE populateD2Row method");
    }
    
    int col = 0;
    
    for (final WLZE wlze : wlzeBunch) {
      matrix.put(col++, row, wlze.zw);
    }
    
    return matrix;
  }
  
  /**
   * Subtract a Z value from all the Z values of a List of WLZE objects
   *
   * @param zw        : The Z value to subtract.
   * @param wlzeBunch : The List of WLZE objects
   * @return List of WLZE objects.
   */
  public final static List<WLZE> subtract(final double zw, @NotNull @Size(min = 1) final List<WLZE> wlzeBunch) {
    
    for (final WLZE wlze : wlzeBunch) {
      wlze.zw -= zw;
    }
    
    return wlzeBunch;
  }
  
  /**
   * Compute the standard deviation of all the Z values of a List of WLZE objects.
   *
   * @param wlzeBunch : A List of WLZE objects.
   * @return WL standard deviation (unbiased) estimate in double precision
   */
  public final static double stdDev(@NotNull @Size(min = 2) final List<WLZE> wlzeBunch) {
    
    if (wlzeBunch.isEmpty()) {
      staticLogger.error("WLZE stdDev: wlzeBunch.isEmpty() !");
      throw new RuntimeException("WLZE stdDev method");
    }
    
    if (wlzeBunch.size() < 2) {
      staticLogger.error("WLZE stdDev: wlzeBunch.size() < 2 !");
      throw new RuntimeException("WLZE stdDev method");
    }
    
    double stdDev = 0.0;
    
    final double mean = mean(wlzeBunch);
    
    for (final WLZE wlze : wlzeBunch) {
      
      final double diff = wlze.zw - mean;
      
      stdDev += diff * diff;
    }
    
    //--- Use wlzeBunch.size()-1 to get the unbiased standard deviation:
    return Math.sqrt(stdDev / (wlzeBunch.size() - 1));
  }
  
  /**
   * Compute the double mean of all the Z values of a List of WLZE objects:
   *
   * @param wlzeBunch : The List of WLZE objects.
   * @return double : The Z values mean of the List WLZEa.
   */
  protected final static double mean(@NotNull @Size(min = 1) final List<WLZE> wlzeBunch) {
    
    if (wlzeBunch.isEmpty()) {
      staticLogger.error("WLZE mean: wlzeBunch is Empty !");
      throw new RuntimeException("WLZE mean method");
    }
    
    //--- arithmetic mean
    double zwAcc = 0.0;
    
    for (final WLZE wlze : wlzeBunch) {
      zwAcc += wlze.zw;
    }
    
    return zwAcc / wlzeBunch.size();
  }
  
  /**
   * Add a Z value to a WLZE zw value.
   *
   * @param zw : The double value to add to this.zw
   * @return WLZE : this WLZE
   */
  @NotNull
  public final WLZE add2Zw(final double zw) {
    
    this.zw += zw;
    
    return this;
  }
  
  /**
   * Add the Z value of another WLZE object to a WLZE zw value.
   *
   * @param wlze : Another WLZE object.
   * @return WLZE : this WLZE
   */
  @NotNull
  public final WLZE add2Zw(@NotNull final WLZE wlze) {
    
    this.zw += wlze.zw;
    
    return this;
  }
  
  /**
   * @return thie.error : the WLZE error value atttibute.
   */
  public final double getError() {
    return this.error;
  }
  
  /**
   * @return this.zw : the WLZE Z value atttibute.
   */
  public final double getZw() {
    return this.zw;
  }
  
  /**
   * Multiply the Z value of a WLZE object with the Z value of another WLZE object.
   *
   * @param wlze : Another WLZE object.
   * @return WLZEE : this WLZEE.
   */
  public final WLZE multZw(@NotNull final WLZE wlze) {
    
    this.zw *= wlze.zw;
    
    return this;
  }
  
  /**
   * Set the Z value of a WLZE object.
   *
   * @param zw: The WL Z value to set.
   * @return this.zw
   */
  public final double setZw(final double zw) {
    return (this.zw = zw);
  }
  
  /**
   * Subtract a Z value from this WLZE zw value.
   *
   * @param zw : The double value to subtract from this.zw
   * @return WLZE : this WLZE object.
   */
  @NotNull
  public final WLZE subtrac2Zw(final double zw) {
    
    this.zw -= zw;
    
    return this;
  }
  
  /**
   * Subtract the Z value of another WLZE object from the Z value of this WLZE object.
   *
   * @param wlz : Another WLZE object.
   * @return WLZE: this WLZE object.
   */
  public final WLZE subtractZw(@NotNull final WLZE wlz) {
    
    this.zw -= wlz.zw;
    
    return this;
  }
}
