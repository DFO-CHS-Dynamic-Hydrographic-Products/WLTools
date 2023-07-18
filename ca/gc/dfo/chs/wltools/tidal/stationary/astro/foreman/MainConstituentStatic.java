package ca.gc.dfo.iwls.fmservice.modeling.tides.astro.foreman;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

//---

import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.ConstituentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

//import javax.validation.constraints.Min;
//---
//---

/**
 * Specific class for one main constituent static data(see source code of class ConstituentsStaticData) as defined
 * in the dood_num.txt file of the TCWLTools package.
 */
final public class MainConstituentStatic extends ConstituentFactory implements IForemanConstituentAstro {
  
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * Static main constituent phase correction as defined in M.Foreman's ASCII configuration file or coming from a
   * database.
   */
  protected double phaseCorrection = 0.0;
  /**
   * Static main constituent six doodson numbers as defined in M.Foreman's ASCII configuration file or coming from a
   * database.
   */
  protected int[] doodsonNumbers = null;  //---- Must have DoodsonNumbers.size() == MC_DOODSON_NUM_LEN
  // Satellites array is variable
  /**
   * Static main constituent fictituous satellite date as defined in M.Foreman's ASCII configuration file or coming
   * from a database.
   */
  protected MainConstituentSatellite[] satellites = null; //---- Number of MainConstSat objects contained by
  
  /**
   * Default constructor
   */
  public MainConstituentStatic() {
    super();
  }
  
  /**
   * Parse Foreman's static data contained by a List of Lists of Strings.
   *
   * @param name                      : Main constituent name.
   * @param mainConstituentStaticData : List of List of Strings which defines the main constituent static data.
   */
  public MainConstituentStatic(@NotNull final String name,
                               @NotNull @Size(min = 1) final List<List<String>> mainConstituentStaticData) {
    
    super(name);
    
    int numSats = 0;
    int satCount = 0;
    
    this.log.debug("MainConstituentStatic constructor: Processing main tidal constituent: " + name);
    
    this.doodsonNumbers = new int[MC_DOODSON_NUM_LEN];
    
    List<String> tmpAL = mainConstituentStaticData.get(0);
    
    if (tmpAL.size() == SPLIT_CTL_LEN) {
      
      //--- Need to deal with annoying 70's Fortran formatted ASCII data format artifact:
      for (int k = 0; k < SPLIT_CTL_LEN - 2; k++) {
        this.doodsonNumbers[k] = Integer.parseInt(tmpAL.get(k));
      }
      
      final String fortranArtifact = tmpAL.get(SPLIT_CTL_LEN - 2);
      
      switch (fortranArtifact.length()) {
        
        case 7:
          
          this.phaseCorrection = Double.parseDouble(fortranArtifact.substring(2));
          this.doodsonNumbers[SPLIT_CTL_LEN - 2] = Integer.parseInt(fortranArtifact.substring(0, 2));
          break;
        
        case 6:
          
          this.phaseCorrection = Double.parseDouble(fortranArtifact.substring(1));
          this.doodsonNumbers[SPLIT_CTL_LEN - 2] = Integer.parseInt(fortranArtifact.substring(0, 1));
          break;
        
        default:
          
          this.log.error("MainConstituentStatic constructor: Legacy Fortran artifact string data must contains 7 or 6" +
              " chars!");
          throw new RuntimeException("MainConstituentStatic constructor");
          //break ;
        
      }
      
      //System.out.println("last Doodson Number="+this.DoodsonNumbers[5]) ;
      
      numSats = Integer.parseInt(tmpAL.get(SPLIT_CTL_LEN - 1));
      //numSats= Integer.parseInt( tmpAL.get(Frmn.SPLIT_CTL_LEN-1).toString() );
      
    } else {
      
      //--- No data bundled together. Normal blank space data separation.
      for (int k = 0; k < SPLIT_CTL_LEN - 1; k++) {
        
        this.doodsonNumbers[k] = Integer.parseInt(tmpAL.get(k));
        
        //this.DoodsonNumbers[k] = Integer.parseInt( tmpAL.get(k).toString() ) ;
        //System.out.println("Doodson Number="+this.DoodsonNumbers[k]);
      }
      
      this.phaseCorrection = Double.parseDouble(tmpAL.get(SPLIT_CTL_LEN - 1));
      
      numSats = Integer.parseInt(tmpAL.get(SPLIT_CTL_LEN));
    }
    
    //System.out.println("this="+this) ;
    //System.out.println("numSats="+numSats);
    
    if (numSats == 0) {
      
      //System.out.println("No satellites for const. \""+this.ConstNameStr+"\"") ;
      
      //--- No satellites static data to process here. Just return from constructor.
      return;
    }
    
    //--- Now process this main contstituent satellites static data.
    this.satellites = new MainConstituentSatellite[numSats];
    
    this.log.debug("having " + numSats + " satellites for main const:" + name);
    
    satCount = 0;
    
    for (int k = 1; k < mainConstituentStaticData.size(); k++) {
      
      tmpAL = mainConstituentStaticData.get(k);
      
      if ((tmpAL.size() % MC_SATS_INFO_LEN) != 0) {
        
        this.log.error("MainConstituentStatic constructor: Main constituent satellites data line must contain a " +
            "multiple of \"+MC_SATS_INFO_LEN+\" items !");
        throw new RuntimeException("MainConstituentStatic constructor");
      }
      
      int wk = 0;
      
      final int[] doodNumsTmp = new int[MC_DOODSON_NUM_CHG_LEN];
      
      //----
      while (wk <= (tmpAL.size() - MC_SATS_INFO_LEN)) {
        
        int ampRatioFlagTmp = 0;
        
        final List<String> subList = tmpAL.subList(wk, wk + MC_SATS_INFO_LEN);
        
        for (int ik = 0; ik < MC_DOODSON_NUM_CHG_LEN; ik++) {
          doodNumsTmp[ik] = Integer.parseInt(subList.get(ik));
        }
        
        final String[] ampRatioStrArr = subList.get(MC_SATS_INFO_LEN - 1).split(SAT_AMP_RATIO_SPLIT_STR);
        
        if (ampRatioStrArr.length == 2) {
          ampRatioFlagTmp = Integer.parseInt(ampRatioStrArr[1]);
        }
        
        //System.out.println("satCount="+satCount) ;
        
        this.satellites[satCount++] =
            new MainConstituentSatellite(ampRatioFlagTmp, doodNumsTmp,
                Double.parseDouble(ampRatioStrArr[0]),
                Double.parseDouble(subList.get(MC_SATS_INFO_LEN - 2)));
        
        wk += MC_SATS_INFO_LEN;
      }
    }
    
    this.log.debug("Done with main tidal constituent: " + name);
  }
  
  /**
   * @return A String representing the contents of the MainConstituentStatic object.
   */
  @Override
  public final String toString() {
    
    String doodNumsStr = "DoodsonNumbers=[ ";
    
    for (int k = 0; k < MC_DOODSON_NUM_LEN - 1; k++) {
      doodNumsStr = doodNumsStr + this.doodsonNumbers[k] + ",";
    } //---- for loop
    
    doodNumsStr += this.doodsonNumbers[MC_DOODSON_NUM_LEN - 1] + " ]";
    
    String satellitesStr = ", ";
    
    if ((this.satellites != null) && (this.satellites.length > 0)) {
      
      satellitesStr += "satellites: ";
      
      for (final MainConstituentSatellite sat : this.satellites) {
        satellitesStr += " [ " + sat.toString() + " ] ";
      }
    }
    
    return this.getClass() + ", " + super.toString() + ", PhaseCorrection=" + this.phaseCorrection + ", " + doodNumsStr + satellitesStr;

//        String doodNums= "DoodsonNumbers=[" ;
//
//        this.log.debug("This MainConstStaticData:->"); ;
//        this.log.debug("name="+this.getName());
//        this.log.debug("PhaseCorrection="+ this.phaseCorrection);
//
//        for ( int k= 0; k< MC_DOODSON_NUM_LEN-1; k++ ) {
//            doodNums= doodNums + this.doodsonNumbers[k] + "," ;
//        } //---- for loop
//
//        doodNums= doodNums + this.doodsonNumbers[MC_DOODSON_NUM_LEN-1] + "]" ;
//
//        System.out.println(doodNums) ;
//
//        for ( int k= 0; k< this.satellites.length; k++ ) {
//            this.satellites[k].display() ;
//        } //---- for loop
//
//        System.out.println("<------------------------------\n");
  }
}
