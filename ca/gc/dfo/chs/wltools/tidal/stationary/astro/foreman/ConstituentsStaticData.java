//package ca.gc.dfo.iwls.fmservice.modeling.tides.astro.foreman;
package ca.gc.dfo.chs.wltools.tidal.stationary.astro.foreman;

/**
 * Created by Gilles Mercier on 2018-01-03.
 */

//---

import ca.gc.dfo.chs.wltools.util.IASCIIFileIO;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.AstroInfosFactory;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.AstroInfosFactory;
//import ca.gc.dfo.iwls.fmservice.modeling.util.IASCIIFileIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

//import javax.validation.constraints.Min;
//import javax.validation.constraints.Size;
//---
//---
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.ConstituentFactory;

/**
 * Utility class for tidal constituents static data storage.
 * It contains all the statically defined data in TCWLTools configuration ASCII files dood_numb.txt and
 * shal_water_coef.txt.
 */
final public class ConstituentsStaticData implements IForemanConstituentAstro, IASCIIFileIO {
  
  /**
   * static log utility.
   */
  static private final Logger staticLog = LoggerFactory.getLogger("ConstituentsStaticData");
  /**
   * Main constituents static data as defined in TCWLTools configuration ASCII file dood_numb.txt.
   */
  protected static MainConstituentStatic[] mcStaticData = null;
  /**
   * Shallow water constituents static data as defined in TCWLTools configuration ASCII file shal_water_coef.txt.
   */
  protected static ShallowWaterConstituentStatic[] swcStaticData = null;
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  
  /**
   * @return true if the mcStaticData and swcStaticData arrays are both not null, false otherwise.
   */
  protected final static boolean ok() {
    return ((mcStaticData != null) && (swcStaticData != null));
  }
  
  /**
   * Set the  mcStaticData and swcStaticData arrays with the statically defined final data of enum
   * MainConstituentsLegacyDef
   * and enum ShallowWaterConstituentsLegacyDef of the IForemanConstituentAstro interface.
   */
  protected final static void set() {
    
    //--- We could have something already set in this.mcStaticData and this.swcStaticData so get rid of it before
    //    doing anything else.
    ConstituentsStaticData.clear();
    
    staticLog.debug("ConstituentsStaticData set : Start !");
    
    final Map shWtConstsHM = new HashMap<String, List<String>>();
    final Map mainConstsHM = new HashMap<String, List<List<String>>>();
    
    //--- Extract MainConstituentsLegacyDef data in a HashMap:
    for (final MainConstituentsLegacyDef mainConstituentsLegacyDef : MainConstituentsLegacyDef.values()) {
      
      //--- Get rid of the underscore prefix:
      final String mc = mainConstituentsLegacyDef.toString().substring(1);
      
      if (!AstroInfosFactory.validateConstName(mc, IForemanConstituentAstro.TC_NAMES)) {
        
        staticLog.error("ConstituentsStaticData set: Unknown main tidal constituent -> \"" + mc + "\"");
        throw new RuntimeException("ConstituentsStaticData set");
      }
      
      //staticLog.debug("set() : Processing main constiuent -> "+mc);
      
      //List<String> tmpALExt= Arrays.asList(strSplitArr).subList(1,strSplitArr.length);
      
      List<List<String>> tmpList = new ArrayList<>();
      
      tmpList.add(Arrays.asList(mainConstituentsLegacyDef.mandatoryDef.trim().split(SPLIT_STRING)));
      
      if (mainConstituentsLegacyDef.satellitesDef != null) {
        
        //staticLog.debug("set() : Got "+mce.satellitesDef.length+" satellite(s) for "+mc);
        
        for (final String satelliteDef : mainConstituentsLegacyDef.satellitesDef) {
          tmpList.add(Arrays.asList(satelliteDef.trim().split(SPLIT_STRING)));
        }
      }
      
      mainConstsHM.put(mc, tmpList);
      
      //staticLog.debug("mc="+mc+", mc data="+mainConstsHM.get(mc).toString());
    }
    
    //--- Extract ShallowWaterConstituentsLegacyDef data in a HashMap:
    for (final ShallowWaterConstituentsLegacyDef shallowWaterConstituentsLegacyDef :
        ShallowWaterConstituentsLegacyDef.values()) {
      
      //--- Get rid of the underscore prefix:
      final String swc = shallowWaterConstituentsLegacyDef.toString().substring(1);
      
      if (!AstroInfosFactory.validateConstName(swc, IForemanConstituentAstro.TC_NAMES)) {
        
        staticLog.error("ConstituentsStaticData set: Unknown shallow water tidal constituent -> \"" + swc + "\"");
        throw new RuntimeException("ConstituentsStaticData set");
      }
      
      //staticLog.debug("set() : Processing shallow water constiuent -> "+swc);
      
      shWtConstsHM.put(swc,
          Arrays.asList(shallowWaterConstituentsLegacyDef.mainConstituentsDerivationDef.trim().split(SPLIT_STRING)));
      
      //staticLog.debug("swc="+swc+", swc data="+shWtConstsHM.get(swc).toString());
    }
    
    //--- Populate this.mcStaticData and this.swcStaticData with the static data extracted from the enums.
    setStaticData(mainConstsHM, shWtConstsHM);
    
    staticLog.debug("set() : end !");
    
    //mainConstsHM.put("Z0", );
  }
  
  /**
   * Clear all static data of the class.
   */
  protected final static void clear() {
    mcStaticData = null;
    swcStaticData = null;
  }
  
  /**
   * Populate this.mcStaticData and this.swcStaticData with the static data extracted from the enums.
   *
   * @param mainConstsHM : Map of all the MainConstituentsLegacyDef data extracted as Strings.
   * @param shWtConstsHM : Map of all the ShallowWaterConstituentsLegacyDef data extracted as Strings.
   */
  private static void setStaticData(final Map<String, List<List<String>>> mainConstsHM, final Map<String,
      List<String>> shWtConstsHM) {
    
    //--- Allocate the MainConstituentStatic and ShallowWaterConstituentStatic objects arrays:
    mcStaticData = new MainConstituentStatic[mainConstsHM.size()];
    swcStaticData = new ShallowWaterConstituentStatic[shWtConstsHM.size()];
    
    int it = 0;
    
    //--- Populate this.mcStaticData:
    for (final Iterator kit = mainConstsHM.keySet().iterator(); kit.hasNext(); ) {
      
      final String key = kit.next().toString();
      
      //--- Need to type cast here:
      final List<List<String>> tmpAL = mainConstsHM.get(key);
      
      mcStaticData[it++] = new MainConstituentStatic(key, tmpAL);
    }
    
    it = 0;
    
    //--- Populate this.swcStaticData:
    for (final Iterator kit = shWtConstsHM.keySet().iterator(); kit.hasNext(); ) {
      
      final String key = kit.next().toString();
      
      final List<String> tmpAL = shWtConstsHM.get(key);
      
      swcStaticData[it++] = new ShallowWaterConstituentStatic(key, tmpAL, mcStaticData);
    }
    
    staticLog.debug("mcStaticData.length=" + mcStaticData.length);
    staticLog.debug("swcStaticData.length=" + swcStaticData.length);
    staticLog.debug("End !");
  }
  
  //--- For possible future usage:
//    protected final static void set(@NotNull final String staticDataDirPath) {
//
//        ConstituentsStaticData.clear();
//
//        staticLog.debug("set(@NotNull final String staticDataDirPath) : Start !");
//
//        //final Map freqenciesHM= new HashMap <String,String> () ;
//        final Map shWtConstsHM= new HashMap <String,List<String>> () ;
//        final Map mainConstsHM= new HashMap <String,List<List<String> >> ();
//
//        //final String tcfFile= staticDataDirPath + "/" + FREQUENCIES_FILE_NAME;
//        final String swcFile= staticDataDirPath + "/" + SHWAT_CONSTS_FILE_NAME;
//        final String mcFile= staticDataDirPath + "/" + MAIN_CONSTS_FILE_NAME;
//
//        //final List<String> frequenciesRawLines= AsciiIO.getFileLinesAsArrayList( tcfFile ) ;
//        final List<String> mainConstsRawLines= ASCIIFileIO.getFileLinesAsArrayList( mcFile ) ;
//        final List<String> shallowWatConstsRawLines= ASCIIFileIO.getFileLinesAsArrayList( swcFile ) ;
//
//        //--- First extract main constituents static data in an HashMap.
//        for (final String str : mainConstsRawLines) {
//
//            //---- Split( blank spaces as delimiters) line in an array of Strings:
//            final String [] strSplitArr= str.trim().split(SPLIT_STRING);
//
//            final String mcName= strSplitArr[0];
//
//            staticLog.debug("Checking main constituent read: "+mcName);
//
//            if ( ! AstroInfosFactory.checkConstName( mcName, IForemanConstituentAstro.TC_NAMES) ) {
//                staticLog.error("ConstituentsStaticData set: Unknown main tidal constituent ->
//                \""+strSplitArr[0]+"\" ! Found in static main constituents data file: " + mcFile) ;
//                throw new RuntimeException("ConstituentsStaticData set");
//            }
//
//            //List<String> tmpALExt= new ArrayList<>() ;
//            //tmpALExt.clear() ;
//            //tmpALExt= ArrayListStMeth.addStringArray ( 1, strSplitArr.length, strSplitArr, tmpALExt) ;
//
//            //--- Main constituent name at index 0 in strSplitArr is already extracted so just get the remainder items
//            //    from index 1 in it.
//            List<String> tmpALExt= Arrays.asList(strSplitArr).subList(1,strSplitArr.length);
//
//            List< List<String> > tmpALAcc= new ArrayList<>() ;
//
//            if ( mainConstsHM.containsKey(mcName) ) {
//
//                //--- Some more infos for a main tidal const. which is already in the HashMap:
//                //System.out.println("Const="+strSplitArr[0]+", Adding line: "+tmp) ;
//
//                tmpALAcc= ( List<List<String>> ) mainConstsHM.get(mcName)  ;
//
//                //System.out.println("tmpAcc bef."+tmpAcc);
//
//                tmpALAcc.add( tmpALExt ) ;
//
//                //System.out.println("tmpAcc aft."+tmpAcc);
//                mainConstsHM.put(mcName, tmpALAcc) ;
//
//            } else {
//
//                //--- We have a new tidal const. data to add to the HashMap:
//                //tmpALAcc.add( new ArrayList<String> ( tmpALExt ) ) ;
//                tmpALAcc.add( tmpALExt  ) ;
//
//                //--- Populate the main consts. HashMap with the name of the constituent as the key.
//                mainConstsHM.put(mcName,tmpALAcc) ;
//
//            }
//
//            //System.out.println("\n") ;
//
//        }
//
//        for (final String str : shallowWatConstsRawLines) {
//
//            final String [] strSplitArr= str.trim().split("\\s+");
//
//            final String swcName= strSplitArr[0];
//
//            if ( !AstroInfosFactory.checkConstName( swcName, IForemanConstituentAstro.TC_NAMES ) ) {
//
//                staticLog.error("ConstituentsStaticData set: Unknown main constituent: \""+strSplitArr[0]+"\" !
//                Found in static shallow water constituents data file: "+swcFile);
//                throw new RuntimeException("ConstituentsStaticData set");
//            }
//
//            //final List<String> tmpALExt= new ArrayList<>() ;
//            //tmpALExt.clear() ;
//            //ArrayListStMeth.addStringArray(1, strSplitArr.length, strSplitArr, tmpALExt) ;
//
//            //--- Again shallow water constituent name at index 0 in strSplitArr is already extracted so just get
//            the remainder items
//            //    from index 1 in it.
//            final List<String> tmpALExt= Arrays.asList(strSplitArr).subList(1,strSplitArr.length);
//
//            shWtConstsHM.put(swcName, tmpALExt) ;
//        }
//
//        setStaticData(mainConstsHM,shWtConstsHM);
//
//        staticLog.debug("End !");
//    }

}
