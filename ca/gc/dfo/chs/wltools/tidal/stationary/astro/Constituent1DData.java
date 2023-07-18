package ca.gc.dfo.iwls.fmservice.modeling.tides.astro;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

//---

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

//---

/**
 * Class for 1D tidal constituents data group.
 */
final public class Constituent1DData {
  
  /**
   * log utility
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * Array of Constituent1D objects.
   */
  protected Constituent1D[] data = null;
  
  /**
   * Default constructor.
   */
  public Constituent1DData() {
    this.clear();
  }
  
  protected final void clear() {
    this.data = null;
  }
  
  /**
   * @param howMany : The size of this.data
   */
  public Constituent1DData(@Min(1) final int howMany) {
    
    if (howMany <= 0) {
      
      this.log.error("Constituent1DData constructor: howMany <= 0!");
      throw new RuntimeException("Constituent1DData constructor");
    }
    
    this.data = new Constituent1D[howMany];
    
    this.init();
  }
  
  protected final void init() {
    
    for (int it = 0; it < this.data.length; it++) {
      this.data[it].clear(0.0);
    }
  }

//    //--- for possible future usage
//    public final double accumulateTidalAmplitudes(@Min(0L) final long timeIncrSeconds, @NotNull final
//    AstroInfosFactory astro) {
//
//        //--- TODO: Verify constituents order to avoid using the wrong astronomic infos.
//
//        if (this.constituents.length != astro.infos.length) {
//
//            this.log.error("Constituent1DData accumulateTidalAmplitudes: this.constituents.length != astro.infos
//            .length !" );
//            throw new RuntimeException("onstituent1DData accumulateTidalAmplitudes");
//        }
//
//        astro.updateTimeReference(timeIncrSeconds);
//
//        double tidalAmplitude= 0.0;
//
//        for (final Constituent1D c1d : this.constituents) {
//
//            tidalAmplitude += c1d.getTidalAmplitude();
//        }
//
//        // int c= 0;
//        // for (final IConstituentAstro infoItem : astro.infos) {
//        // 	   //--- Slower(but less error prone) constituent search in this.data array.
//        // 	   acc += infoItem.computeTidalAmplitude( this.getDataItem(infoItem.getName()) );
//        // 	   //--- Quick direct array access but no checks for constituent order consistency.
//        // 	   //    TODO: Check if the slower constituent search in this.data is significantly less
//        // 	   //          performant than the direct unchecked array access.
//        // 	   //acc += infoItem.computeTidalAmplitude(this.data[c++]);
//        // }
//
//        return tidalAmplitude;
//    }
  
  /**
   * @param constituent1DList : List of Constituent1D objects.
   */
  public Constituent1DData(@NotNull @Size(min = 1) final List<Constituent1D> constituent1DList) {
    
    if (constituent1DList.size() <= 0) {
      
      this.log.error("Constituent1DData constructor: constituent1DList.size() <= 0!");
      throw new RuntimeException("Constituent1DData constructor");
    }
    
    this.data = new Constituent1D[constituent1DList.size()];
    
    int it = 0;
    
    for (final Constituent1D constituent1D : constituent1DList) {
      this.data[it++] = new Constituent1D(constituent1D);
    }
  }
  
  //--- for possible future usage
//    /**
//     * @param whichOne : The name of a wanted tidal constituent.
//     * @return The Constituent1D wanted if found, null otherwise.
//     */
//    protected final Constituent1D getDataItem(@NotNull final String whichOne) {
//
//        try {
//            this.data.hashCode();
//
//        } catch (NullPointerException e) {

//            this.log.error("Constituent1DData getDataItem: this.data==null !!");
//            throw new RuntimeException("Constituent1DData getDataItem");
//        }
//
//        Constituent1D ret= null;
//
//        //--- Slow(but no constituent order consistency errors) array search access in this.data.
//        for (int it = 0; it< this.data.length; it++) {
//
//            if (this.data[it].equals(whichOne)) {
//
//                ret= this.data[it];
//                break;
//            }
//        }
//
////        if (ret==null) {
////            this.log.error("Constituent1DData getDataItem: ret==null !, Did not found tidal constituent:
// "+whichOne+ " in this.data !");
////            throw new RuntimeException("Constituent1DData getDataItem");
////        }
//
//        return ret;
//    }
  
  //--- for possible future usage
  // public Constituent1D getDataItem(final int idx) {
  //    return (idx < this.data.length) ? this.data[idx]: null;
  // }
  
  /**
   * @param tcDataMap         : Map of Constituent1D objects.
   * @param astroInfosFactory : AstroInfosFactory object.
   */
  public Constituent1DData(@NotNull @Size(min = 1) final Map<String, Constituent1D> tcDataMap,
                           @NotNull final AstroInfosFactory astroInfosFactory) {
    
    try {
      tcDataMap.size();
      
    } catch (NullPointerException e) {
      
      this.log.error("Constituent1DData constructor: tcDataMap==null !!");
      throw new RuntimeException(e);
    }
    
    try {
      astroInfosFactory.size();
      
    } catch (NullPointerException e) {
      
      this.log.error("Constituent1DData constructor: astroInfosFactory==null !!");
      throw new RuntimeException(e);
    }
    
    this.data = new Constituent1D[astroInfosFactory.size()];
    
    Constituent1D.setAstroInfosReferences(astroInfosFactory, tcDataMap);
    
    int cidx = 0;
    
    for (final IConstituentAstro iConstituentAstro : astroInfosFactory.infos) {
      
      this.data[cidx++] = tcDataMap.get(iConstituentAstro.getName());
    }
  }
  
  /**
   * @return The size of this.data.
   */
  public final int size() {
    return this.data.length;
  }
  
  // protected final boolean verifyDataOrder(final AstroFactory astro) {
  
  //     boolean ret= true;
  
  //     int c= 0;
  
  //     // for (final IConstituent ic : astro.infos) {
  //     // 	   if ( !ic.getName().equals(this.data[c++].getName()) ) {
  //     // 	       this.log.error("Found constituents order inconsistency between this.data and astro  !" );
  //     // 	       ret= false;
  //     // 	       break;
  //     // 	   }
  //     // }
  
  //     return ret;
  // }
}
