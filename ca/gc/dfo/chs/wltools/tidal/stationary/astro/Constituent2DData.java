//package ca.gc.dfo.iwls.fmservice.modeling.tides.astro;
package ca.gc.dfo.chs.wltools.tidal.stationary.astro;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

//---
//import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Min;

//import javax.validation.constraints.Size;
//import javax.validation.constraints.NotNull;
//---

/**
 * Class for 2D tidal constituents data group.
 */
final public class Constituent2DData {
  
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * Array of Constituent2D objects.
   */
  protected Constituent2D[] data = null;
  
  /**
   * Default constructor
   */
  public Constituent2DData() {
    this.clear();
  }
  
  //--- TODO: implement constructor Constituent2DData(this.tcDataMap, this.astroInfosFactory)
  
  //--- For possible future usage
//    public Constituent2DData(@Min(1) final int howMany) {
//
//        if (howMany <= 0 ) {
//            this.log.error("Constituent2DData constructor: howMany <= 0!");
//            throw new RuntimeException("Constituent2DData constructor");
//        }
//
//        this.data= new Constituent2D[howMany];
//
//        this.init();
//    }
//
  
  //--- For possible future usage
//    public Constituent2DData(@NotNull @Size(min=1) final List<Constituent2D> c2dl) {
//
//        if (c2dl.size() <= 0 ) {
//            this.log.error("Constituent2DData constructor: c2dl.size() <= 0!");
//            throw new RuntimeException("Constituent2DData constructor");
//        }
//
//        this.data= new Constituent2D[c2dl.size()];
//
//        //this.init();
//
//        int it= 0;
//
//        for (final Constituent2D c2d : c2dl) {
//            this.data[it++]= new Constituent2D(c2d);
//        }
//    }
  
  //---
  protected final void clear() {
    this.data = null;
  }
  
  /**
   * @param idx : The indice of the Constituent2D object wanted in this.data
   * @return The Constituent2D object at indice idx in this.data if indice is ok, null otherwise.
   */
  public final Constituent2D getDataItem(@Min(0) final int idx) {
    
    try {
      this.data.hashCode();
      
    } catch (NullPointerException e) {
      
      this.log.error("Constituent2DData getDataItem:this.data==null !!");
      throw new RuntimeException(e);
    }
    
    return (idx < this.data.length) ? this.data[idx] : null;
  }
  
  public final int size() {
    return this.data.length;
  }
  
  /**
   * initialize this.data contents
   */
  protected final void init() {
    
    try {
      this.data.hashCode();
      
    } catch (NullPointerException e) {
      
      this.log.error("Constituent2DData init:this.data==null !!");
      throw new RuntimeException(e);
    }
    
    for (int it = 0; it < this.data.length; it++) {
      this.data[it].clear(0.0);
    }
  }
}
