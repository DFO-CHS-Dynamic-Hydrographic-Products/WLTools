//package ca.gc.dfo.iwls.fmservice.modeling.wl;
package ca.gc.dfo.chs.wltools;

/**
 *
 */

/**
 * 
 */
public class WLToolsIO implements IWLToolsIO {

   static private String mainCfgDir= null;

   //public WLToolsIO(final String mainCfgDirArg) {
   //  mainCfgDir= mainCfgDirArg;
   //}

   final protected static String setMainCfgDir(final String mainCfgDirArg) {
     mainCfgDir= mainCfgDirArg;
     return mainCfgDir;
   }

   final public static String getMainCfgDir() {
     return mainCfgDir;
   }
}