//package ca.gc.dfo.iwls.fmservice.modeling.tides.astro;
package ca.gc.dfo.chs.wltools.tidal.stationary.astro;

/**
 * Created by Gilles Mercier on 2018-01-02.
 */

//---

import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.ITidesIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

//---

/**
 * Generic class for tidal constituents common data;
 */
abstract public class ConstituentFactory implements ITidalIO {
  
  /**
   * static log utility,
   */
  private static final Logger staticLog = LoggerFactory.getLogger("ConstituentFactory");
  /**
   * log utility.
   */
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  /**
   * The name of a specific tidal constituent ex. M2, O1, S2...
   */
  protected String name = null;
  
  /**
   * default constructor
   */
  public ConstituentFactory() {
    this.clear();
  }
  
  protected final void clear() {
    this.name = null;
  }
  
  /**
   * @param name : The name of a specific tidal constituent ex. M2, O1, S2...
   */
  public ConstituentFactory(@NotNull final String name) {
    this.setName(name);
  }
  
  /**
   * @param name                    : The name of a specific tidal constituent ex. M2, O1, S2.
   * @param constituentFactoryArray : An array of ConstituentFactory objects.
   * @return The ConstituentFactory object wanted if it is found in constituentFactoryArray, null otherwise.
   */
  public static final ConstituentFactory get(@NotNull final String name,
                                             @NotNull @Size(min = 1) final ConstituentFactory[] constituentFactoryArray) {
    
    ConstituentFactory ret = null;
    
    staticLog.debug("ConstituentFactory get: name=" + name + ", cstl.length=" + constituentFactoryArray.length);
    
    for (final ConstituentFactory ccnstituentfactory : constituentFactoryArray) {
      
      staticLog.debug("\"ConstituentFactory get: cst.name=" + ccnstituentfactory.name);
      
      if (name.equals(ccnstituentfactory.name)) {
        
        ret = ccnstituentfactory;
        staticLog.debug("ConstituentFactory get: found cst.name=" + ccnstituentfactory.name);
        
        break;
      }
    }
    
    return ret;
  }
  
  /**
   * @param name                   : The name of a specific tidal constituent ex. M2, O1, S2.
   * @param constituentFactoryList : A List of ConstituentFactory objects.
   * @return The ConstituentFactory object wanted if it is found in constituentFactoryList, null otherwise.
   */
  public static final ConstituentFactory get(@NotNull final String name,
                                             @NotNull @Size(min = 1) final List<ConstituentFactory> constituentFactoryList) {
    
    ConstituentFactory ret = null;
    
    for (final ConstituentFactory constituentFactory : constituentFactoryList) {
      
      if (name.equals(constituentFactory.name)) {
        ret = constituentFactory;
        break;
      }
    }
    
    return ret;
  }
  
  /**
   * @param constsNames      : An array of tidal constituents (String) names that we want to validate.
   * @param validConstsNames : An array of valid(known) tidal constituents (String) names.
   * @param staticSet        : An already existing array of valid ConstituentFactory objects.
   * @return A List of ConstituentFactory objects which is a subset of the ConstituentFactory objects contained in
   * the staticSet array.
   */
  public final static List<ConstituentFactory> getSubsetList(@NotNull @Size(min = 1) final String[] constsNames,
                                                             @NotNull @Size(min = 1) final String[] validConstsNames,
                                                             @NotNull @Size(min = 1) final ConstituentFactory[] staticSet) {
    
    try {
      constsNames.hashCode();
      
    } catch (NullPointerException e) {
      
      staticLog.error("ConstituentFactory getSubsetList: constsNames==null !!");
      throw new RuntimeException(e);
    }
    
    try {
      validConstsNames.hashCode();
      
    } catch (NullPointerException e) {
      
      staticLog.error("ConstituentFactory getSubsetList: validConstsNames==null !!");
      throw new RuntimeException(e);
    }
    
    if (constsNames.length == 0) {
      
      staticLog.error("ConstituentFactory getSubsetList: String array constsNames is emtpy !");
      throw new RuntimeException("ConstituentFactory getSubsetList");
    }
    
    if (validConstsNames.length == 0) {
      
      staticLog.error("ConstituentFactory getSubsetList: String array validConstsNames is emtpy !");
      throw new RuntimeException("ConstituentFactory getSubsetList");
    }
    
    if (validConstsNames.length < constsNames.length) {
      
      staticLog.error("ConstituentFactory getSubsetList: validConstsNames.length<constsNames.length !");
      throw new RuntimeException("ConstituentFactory getSubsetList");
    }
    
    staticLog.debug("ConstituentFactory getSubsetList: staticSet.length=" + staticSet.length);
    
    final List<ConstituentFactory> retList = new ArrayList<>();
    
    for (final String cn : constsNames) {
      
      if (!AstroInfosFactory.validateConstName(cn, validConstsNames)) {
        
        staticLog.error("ConstituentFactory getSubsetList: Invalid tidal constituent -> " + cn);
        throw new RuntimeException("ConstituentFactory getSubsetList");
      }
      
      for (final ConstituentFactory cst : staticSet) {
        
        if (cn.equals(cst.getName())) {
          retList.add(cst);
          break;
        }
      }
    }
    
    return retList;
  }
  
  @NotNull
  public final String getName() {
    return this.name;
  }
  
  /**
   * @param name : The name of a specific tidal constituent ex. M2, O1, S2.
   */
  protected final void setName(final String name) {
    this.name = name;
  }
  
  /**
   * @return A string representation of a ConstituentFactory object.
   */
  public String toString() {
    return this.getClass() + ", name=" + this.name;
  }
}
