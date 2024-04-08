//package ca.gc.dfo.iwls.fmservice.modeling.tides.astro.foreman;
package ca.gc.dfo.chs.wltools.tidal.stationary.astro.foreman;

/**
 * Created by Gilles Mercier on 2018-01-03.
 */

//---
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.IConstituentAstro;
import ca.gc.dfo.chs.wltools.tidal.stationary.astro.ConstituentFactory;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.ConstituentFactory;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.IConstituentAstro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.ArrayList;

//import java.util.Map;
//import javax.validation.constraints.Min;
//---
//---

/**
 * Utility class for constituents static data storage.
 */
final public class ForemanAstroInfos extends ForemanAstroInfosFactory implements IForemanConstituentAstro {
  
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * List of ConstituentFactory references to a subset of MainConstituentStatic objects
   */
  private List<ConstituentFactory> mcStaticDataSubset = null;
  /**
   * List of ConstituentFactory references to a subset of ShallowWaterConstituentStatic objects
   */
  private List<ConstituentFactory> swcStaticDataSubset = null;
  
  //--- Constructor for possible future usaqge
//    public ForemanAstroInfos() {
//
//        super();
//        this.log.debug("Start !");
//
//        this.clear();
//        //this.nbThread=1;
//        this.log.debug("Done !");
//    }
  
  /**
   * @param latPosRadians    : Latitude in radians of a location(WL station OR a grid point).
   * @param startTimeSeconds : Date time-stamp in seconds since the epoch to use as a temporal reference for Sun
   *                         and Moon ephemerides computations.
   * @param constNames       : Set of tidal constituents names to use.
   */
  public ForemanAstroInfos(final double latPosRadians, 
                           final long startTimeSeconds,
                           /*@NotNull @Size(min = 1)*/ final Set constNames) {
    
    super(latPosRadians, startTimeSeconds);
    
    try {
      constNames.size();
      
    } catch (NullPointerException e) {
      
      this.log.error("ForemanAstroInfos constructor: constNames==null!!");
      throw new RuntimeException(e);
    }
    
    this.log.debug("ForemanAstro constructor: Start !");
    
    if (!ConstituentsStaticData.ok()) {
      
      this.log.debug("ForemanAstroInfos constructor : ConstituentsStaticData.ok() == false. need to do the static " +
          "data setup.");
      ConstituentsStaticData.set();
    }
    
    final String[] constsNamesArr = new String[constNames.size()];
    
    int it = 0;
    
    for (final Object object : constNames.toArray()) {
      constsNamesArr[it++] = (String) object;
    }
    
    //this.log.debug("constsNamesArr[0]="+constsNamesArr[0]);
    
    this.set(this.latitudeRad, constsNamesArr);
    
    this.log.debug("ForemanAstro constr. : Done !");
  }
  
  /**
   * @param latPosRadians    : Latitude in radians of a location(WL station OR a grid point).
   * @param constsNamesArray : Array of Tidal constituents names to use.
   * @return The ForemanAstroInfos object.
   */
  protected final ForemanAstroInfos set(final double latPosRadians,
                                        /*@NotNull @Size(min = 1)*/ final String[] constsNamesArray) {
    
    try {
      constsNamesArray.hashCode();
      
    } catch (NullPointerException e) {
      
      this.log.error("ForemanAstroInfos set: constsNamesArray==null!!");
      throw new RuntimeException(e);
    }
    
    if (constsNamesArray.length == 0) {
      
      this.log.error("ForemanAstroInfos set: constsNamesArray.length==0 !!");
      throw new RuntimeException("ForemanAstroInfos set");
    }
    
    if (!ConstituentsStaticData.ok()) {
      
      this.log.error("ForemanAstroInfos set: ConstituentsStaticData.ok() == false !");
      throw new RuntimeException("ForemanAstroInfos set");
    }
    
    //--- Get the main constituents static data objects references subset:
    this.mcStaticDataSubset = ConstituentFactory
       .getSubsetList(constsNamesArray, TC_NAMES, ConstituentsStaticData.mcStaticData);
    
    final int nbMainConsts = this.mcStaticDataSubset.size();
    
    if (nbMainConsts == 0) {
      
      this.log.error("ForemanAstroInfos set: No valid MainConstStaticData objects returned by Constituent.getSubset " +
          "method !");
      throw new RuntimeException("ForemanAstroInfos set");
    }
    
    this.log.debug("ForemanAstroInfos set: We have " + nbMainConsts + " main tidal constituents to " +
        "process.");
    
    //--- Get the shallow water constituents static data objects references subset:
    this.swcStaticDataSubset= ConstituentFactory
      .getSubsetList(constsNamesArray,TC_NAMES, ConstituentsStaticData.swcStaticData);
    
    final int nbSWConsts = this.swcStaticDataSubset.size();
    
    if (nbSWConsts > 0) {
      this.log.debug("ForemanAstroInfos set: And we also have " + nbSWConsts + " shallow water " +
          "tidal constituents to process.");
    }
    
    //--- Allocate this.infos array to contain nbMainConsts+nbSWConsts ForemanConstituentAstro objects:
    this.infos = new ForemanConstituentAstro[nbMainConsts + nbSWConsts];
    
    //--- Temp. local array for main constituents astro infos. only.
    //    TODO: Use a Map<ForemanConstituentAstro> instead of an array of ForemanConstituentAstro objects
    //          for the tmpMcInfos local object.
    IConstituentAstro[] tmpMcInfos = new ForemanConstituentAstro[nbMainConsts];

    List<String> tmpMcCheckList= new ArrayList<String>();
    
    int tcit = 0;
    
    //--- NOTE: We MUST always process main tidal constituents before the shallow water constituents (if any) because
    // they
    //          depends on one or more main tidal constituents and consequently the shallow water constituents
    //          objects MUST
    //          all be located after all the main tidal constituents objects in this.infos array:
    for (final ConstituentFactory constituentFactory : this.mcStaticDataSubset) {
      
      this.log.debug("ForemanAstroInfos set: Processing main const. : " + constituentFactory.getName());
      
      //--- NOTE: MUST cast constituentFactory object to a MainConstituentStatic here
      //          Also assign the newly created MainConstituent object in tmpMcInfos for usage by the shallow water
      //          constituents(if any) and also use the ForemanConstituentAstro update method to get a ready to use new
      //          MainConstituent object.
      this.infos[tcit]=
         tmpMcInfos[tcit++]= new
            MainConstituent((MainConstituentStatic) constituentFactory).update(latPosRadians, this.sunMoonEphemerides);

      tmpMcCheckList.add(constituentFactory.getName());
      
      //this.log.debug("main const.:"+constituentFactory.getName()+" updated");
    }
    
    if (nbSWConsts > 0) {
      
      this.log.debug("ForemanAstroInfos set: Processing shallow-water tidal constituent(s)");

      //--- NOTE: It is possible to have some shallow water constituents in some analysis results without having all their
      //          related main constituents being present in the same analysis results. We need to add those main
      //          constituents ForemanConstituentAstro objects in the  tmpMcInfos array in order to have all we
      //          need for the orphaned shallow water constituents. Note that those main constituents will not be
      //          used for the tidal predictions because we do not have their amplitudes nor their Greenwich phases
      //          lags in the constituents input file.
      List<ForemanConstituentAstro> missingMCFList = new ArrayList<ForemanConstituentAstro>();

      //--- Loop on all the shallow water consts. that need to be used in order to
      //    check it their related main const(s) are present in the analysis results.
      for (final ConstituentFactory swConstituentFactory: this.swcStaticDataSubset) {

	List<String> missingMcNames= new ArrayList<String>();

	//--- Need to do an ugly cast the swConstituentFactory as a ShallowWaterConstituentStatic
	//    to be able to use its getMainConstituentsNamesList() method.
	//    TODO: implement somethig cleaner than that.
	final List<String> checkMcNamesList=
           ((ShallowWaterConstituentStatic)swConstituentFactory).getMainConstituentsNamesList();

	for (final String checkMcName: checkMcNamesList) {
	     
	  if (!tmpMcCheckList.contains(checkMcName)) {

	    this.log.warn("ForemanAstroInfos set: Missing Main const -> "+checkMcName+
			  " in the analysis results file for its derived shallow wat. const. -> "+swConstituentFactory.getName()+
			  ", getting the main const. static parameters even if the main const itself will not be used directly for the prediction");
	    
	    missingMcNames.add(checkMcName);
	  }
	}

	if (missingMcNames.size() > 0 ) {
	    
	  final List<ConstituentFactory> missingMcStaticList = ConstituentFactory
	      .getSubsetList( (String[])missingMcNames.toArray(), TC_NAMES, ConstituentsStaticData.mcStaticData);

	  for (final ConstituentFactory missingMcStatic: missingMcStaticList) {
	      
	     missingMCFList.add( new MainConstituent((MainConstituentStatic) missingMcStatic).update(latPosRadians, this.sunMoonEphemerides) );
	  }
	}
      }

      //--- Need to re-define tmpMcInfos array with the tmpMCFList but only
      //    if the tmpMCFList size is larger than tmpMcInfos size (which means
      //    that we have some orphaned shallow water const(s). in the analyis results file.
      if (missingMCFList.size() > tmpMcInfos.length) {

	IConstituentAstro[] newTmpMcInfos= new ForemanConstituentAstro[tmpMcInfos.length + missingMCFList.size()];

	//--- copy the main consts. ForemanConstituentAstro objects of the tmpMcInfos array in the newTmpMcInfos
	for (int fca= 0; fca < tmpMcInfos.length; fca++) {
	  newTmpMcInfos[fca]= tmpMcInfos[fca];
	}

	//--- copy the missing main consts. ForemanConstituentAstro objects of the missingMCFList in the newTmpMcInfos
	for (int fca= 0; fca < missingMCFList.size(); fca++) {
	  newTmpMcInfos[fca]= missingMCFList.get(fca);
	}

	//--- re-define the tmpMcInfos ForemanConstituentAstro array with the newTmpMcInfos ForemanConstituentAstro array
	tmpMcInfos= newTmpMcInfos; //(ConstituentFactory[]) tmpMCFList.toArray();
      }

      //--- Now set the shallow water constituent(s) parameters for the tidal prediction  	  
      for (final ConstituentFactory swConstituentFactory : this.swcStaticDataSubset) {
        
        this.log.debug("ForemanAstroInfos set: Processing shallow-water tidal const. -> " + swConstituentFactory.getName());
        
        //--- NOTE: MUST cast cfit object to a ShallowWaterConstituentStatic here
        //         (Beware that a cast to a ShallowWaterConstituent compile ok but will probably cause a crash at run
        //         time!)
        //          and also use the ForemanConstituentAstro update method to get a ready to use new
        //          ShallowWaterConstituent object.
        this.infos[tcit++] = new
           ShallowWaterConstituent((ShallowWaterConstituentStatic) swConstituentFactory,
               (ConstituentFactory[]) tmpMcInfos).update(latPosRadians, this.sunMoonEphemerides);
      }
    }
    
    //--- Apply the ForemanConstituentAstro.applyZero2PISandwich method to all the new ForemanConstituentAstro objects.
    ForemanConstituentAstro.applyZero2PISandwich((ForemanConstituentAstro[]) this.infos);

//        for(final IConstituentAstro ica: this.infos) {
//            ((ConstituentFactory)ica).display();
//        }
    
    this.log.debug("ForemanAstroInfos set: end");
    
    return this;
  }
  
  /**
   * @param timePosLIncr : The time increment in seconds to add to this.sse to define the new time reference for
   *                     the astronomic informations.
   * @return The ForemanAstroInfos object.
   */
  //@NotNull
  //@Override
  public final ForemanAstroInfos updateTimeReference(final long timePosLIncr) {
    
    //--- NOTE: timePosLIncr could be < 0 here:
    super.updateTimeReference(timePosLIncr);
    
    //--- Update astronomic informations in this.infos accordinng to the time stamp increment argument.
    //    NOTE 1: Polymorphic usage of method update.
    //    NOTE 2: Cannot use threads parallelization here because shallow water constituents MUST be updated
    //            after all main constituent have been updated and the shallow water constituents objects references
    //            MUST obviously placed after all main constituents objects references in this.infos array.
    for (final ForemanConstituentAstro foremanConstituentAstro : (ForemanConstituentAstro[]) this.infos) {
      
      foremanConstituentAstro.update(this.latitudeRad, this.sunMoonEphemerides);
    }
    
    //--- Non-Polymorphic usage of method update. Need to separate main constituent and shallow water constituents
    // updates between two loops.
    //    NOTE 4: Here it is possible to use threads parallelization providding that we place a barrier before the loop
    //            for shallow water constituents.

//        //--- Always update main constituents astronomic informations before doing so for shallow water constituents.
//        for (final MainConstituent mcr : this.mainConstituents) {
//            mcr.update(this.latitudeRad,this.sunMoonEphemerides);
//        }
//
//        for (final ShallowWaterConstituent swcr : this.shallowWaterConstituents) {
//            swcr.update(this.latitudeRad,this.sunMoonEphemerides);
//        }
    
    //--- ForemanConstituentAstro.applyZero2PISandwich static method must always be applied immediately after
    //    the ForemanConstituentAstro update method have been used.
    ForemanConstituentAstro.applyZero2PISandwich((ForemanConstituentAstro[]) this.infos);
    
    return this;
  }
  
  //--- For possible future usage
//    //---
//    public ForemanAstroInfos(@NotNull final String staticDataDirPath, final double latPosRadians, final long
//    startTimeSeconds, @NotNull @Size(min=1) final String [] constsNamesArr) {
//
//        super(latPosRadians,startTimeSeconds);
//
//        this.log.debug("Start !");
//
//        this.set(staticDataDirPath, this.latitudeRad, constsNamesArr);
//
//        this.log.debug("Done !");
//    }
//
//    //---
//    public ForemanAstro(@NotNull final String staticDataDirPath,
//                        final double latPosRadians, @NotNull @Size(min=DATE_TIME_FMT_LEN) final int []
//                        yearMonthDayHourArr, @NotNull @Size(min=1) final String [] constsNamesArr ) {
//
//        super(latPosRadians, yearMonthDayHourArr);
//        this.log.debug("Start !");
//
//        this.set(staticDataDirPath, this.latitudeRad, constsNamesArr);
//
//        this.log.debug("Done !");
//    }
//
//    //---
//    protected final ForemanAstroInfos set(@NotNull final String staticDataDirPath, final double latPosRadians,
//    @NotNull final Set<String> constsNames) {
//
//        this.set(staticDataDirPath, latPosRadians, constsNames.toArray(new String[0]));
//
//        return this;
//    }
//
//    //---
//    protected final ForemanAstroInfos set(@NotNull final String staticDataDirPath, final double latPosRadians,
//    @NotNull @Size(min=1) final String [] constsNamesArr) {
//
//        if ( ! ConstituentsStaticData.ok() ) {
//
//            this.log.debug("ConstituentsStaticData.ok() == false -> Need to create new ConstsStaticData Object !");
//
//            ConstituentsStaticData.set( staticDataDirPath );
//
//        } else {
//            this.log.debug("ConstituentsStaticData.ok() == true -> No need to create new ConstsStaticData Object !");
//        }
//
//        this.set(latPosRadians,constsNamesArr);
//
//        return this
//}
}
