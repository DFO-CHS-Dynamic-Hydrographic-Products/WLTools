//package ca.gc.dfo.iwls.fmservice.modeling.fms;
package ca.gc.dfo.chs.wltools.wl.fms;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.wl.WLTimeNode;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;

//import ca.gc.dfo.iwls.fmservice.modeling.ForecastingContext;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.ITides;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.ITidesIO;
//import ca.gc.dfo.iwls.fmservice.modeling.util.ASCIIFileIO;
//import ca.gc.dfo.iwls.fmservice.modeling.util.SecondsSinceEpoch;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLStationTimeNode;
//import ca.gc.dfo.iwls.fmservice.modeling.wl.WLTimeNode;
//import ca.gc.dfo.iwls.modeling.fms.FmsParameters;
//import ca.gc.dfo.iwls.timeseries.MeasurementCustom;

//import java.util.Map;
//---
//---
//---
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.Constituent1D;
//import ca.gc.dfo.iwls.fmservice.modeling.tides.astro.AstroInfosFactory;

/**
 * Group one (or more) WL station(s) data and configuration needed for the production of the default FM service
 * forecasts.
 */
final public class FMSWLData implements IFMS, ITidal, ITidalIO {

  private final static String whoAmI= "ca.gc.dfo.chs.wltools.wl.fms";

  /**
   * log utility.
   */
  private static final Logger slog = LoggerFactory.getLogger(whoAmI);

  /**
   * List of WLTimeNode object(s). Either one(default case) or possibly more WLTimeNode objects.
   */
  List<WLTimeNode> timeNodes = null;

  /**
   * List of FMWLStation objects for one or more WL station(s).
   */
  List<FMSWLStation> allStationsData = null;

  /**
   * The seconds since epoch representing the time at which the ForecastingContext was issued (i.e. it separates the
   * past from the future time-stamps in real tine regardless of the time stamp of the last available valid WLOs)
   */
  private long referenceSse = 0L;

  /**
   * List of WLStationTimeNode object(s) used as a temporary storage for subsequent usage.(see method FMWLData
   * .newFMSTimeNode below in this src file).
   */
  private List<WLStationTimeNode> wlsnaTmp = null;

  /**
   * @param fcstsTimeIncrMinutes   : Time increment between each WLF data
   * @param forecastingContextList : List of ForecastingContext(Must have at least one item).
   */
  FMSWLData(/*@Min(0)*/ final int fcstsTimeIncrMinutes,
           /*@NotNull @Size(min = 1) */ final List<FMSConfig> fmsConfigList) {
          //  /*@NotNull @Size(min = 1) */ final List<ForecastingContext> forecastingContextList) {

    final String mmi= "FMSWLData main constructor: ";

    //--- Check forecastingContextList before doing anything else with it.
    try {
      fmsConfigList.size();

    } catch (NullPointerException npe) {

      slog.error(mmi+"fmsConfigList==null !");
      throw new RuntimeException(mmi+npe);
    }

    if (fmsConfigList.size() == 0) {

      slog.error(mmi+"fmsConfigList.size()==0 !");
      throw new RuntimeException(mmi+"Cannot update forecast !");
    }

    try {
      fmsConfigList.get(0).getReferenceTime();

    } catch (NullPointerException npe) {

      slog.error(mmi+"forecastingContextList.get(0)==null !");
      throw new RuntimeException(mmi+npe);
    }

    try {
      forecastingContextList.get(0).getReferenceTime().getEpochSecond();

    } catch (NullPointerException npe) {
      
      this.log.error("FMWLData constructor: forecastingContextList.get(0).getEpochSecond()==null !");
      throw new RuntimeException(npe);
    }
    
    this.log.debug("FMWLData constructor start");
    
    this.referenceSse = forecastingContextList.get(0).getReferenceTime().getEpochSecond();
  
    this.log.debug("FMWLData constructor: this.referenceSse date-time is: " + SecondsSinceEpoch.dtFmtString(this.referenceSse, true));
    
    this.allStationsData = new ArrayList<>(forecastingContextList.size());
    
    this.wlsnaTmp = new ArrayList<>(forecastingContextList.size());
    
    this.populateFMSWLStationsData(fcstsTimeIncrMinutes, forecastingContextList);
    
    this.log.debug("FMWLData constructor end: this.timeNodes.size()=" + this.timeNodes.size());
  }
  
  /**
   * Populate all the objects structures of all the ForecastingContext objects of the forecastingContextList argument.
   * NOTE: forecastingContextList must have already been controlled in terms of objects existence.
   *
   * @param fcstsTimeIncrMinutes   : Time increment between each WLF data
   * @param forecastingContextList : The List of ForecastingContext objects (minimum size 1).
   * @return This FMWLData object fully populated.
   */
  @NotNull
  private final FMSWLData populateFMSWLStationsData(@Min(0) final int fcstsTimeIncrMinutes,
                                                    @NotNull @Size(min = 1) final List<ForecastingContext> forecastingContextList) {
  
    this.log.debug("FMWLData populateFMSWLStationsData start: fcstsTimeIncrMinutes=" + fcstsTimeIncrMinutes + ", " +
        "fcList.size()=" + forecastingContextList.size());
    
    int sit = 0;
    
    final List<IFMSResidual> stationsResiduals = new ArrayList<>();
    
    final ForecastingContext fc0 = forecastingContextList.get(0);
    
    final String residualMethod = fc0.getFmsParameters().getResidual().getMethod();
    
    if (forecastingContextList.size() > 1) {
      
      this.log.debug("FMWLData populateFMSWLStationsData: fcList.size()>1 : Need to validate ForecastingContext items" +
          " with each other.");
      
      if (FMSResidualFactory.validateStationsFMSConfigParameters(residualMethod, forecastingContextList)) {
        
        this.log.error("FMWLData populateFMSWLStationsData: ForecastingContext items validation failed");
        throw new RuntimeException("FMWLData populateFMSWLStationsData: Cannot update forecast !");
      }
      
      this.log.debug("FMWLData populateFMSWLStationsData: fcList.size()>1 : Success for ForecastingContext items " +
          "validation.");
    }
    
    //--- Create new FMWLStationData Objects in this.allStationsData:
    for (final ForecastingContext forecastingContext : forecastingContextList) {
      
      if (forecastingContext == null) {
        
        this.log.error("FMWLData populateFMSWLStationsData: fc==null!");
        throw new RuntimeException("FMWLData populateFMSWLStationsData: Cannot update forecast !");
      }
      
      final String stationId = forecastingContext.getStationCode();
  
      this.log.debug("FMWLData populateFMSWLStationsData: Populating residuals errors statistics " +
          "data structures for " +
          "station: " + stationId);
      
      //--- Check fc.getStationInfo():
      if (forecastingContext.getStationInfo() == null) {
        
        this.log.error("FMWLData populateFMSWLStationsData: fc.getStationInfo()==null for station: " + stationId);
        throw new RuntimeException("FMWLData populateFMSWLStationsData: Cannot update forecast !");
      }
      
      //--- No more com.vividsolutions.jts.geom.Point object in IWLS Station class.
      //    But it could eventually comeback.
//            if (fc.getStationInfo().getLocation()==null) {
//                this.log.error("fc.getStationInfo().getLocation()==null for station: "+stationId);
//                 throw new RuntimeException("FMWLData populateFMSWLStationsData: Cannot update forecast !");
//            }
//            final double stationLon= fc.getStationInfo().getLocation().getCoordinates()[0].x;
//            final double stationLat= fc.getStationInfo().getLocation().getCoordinates()[0].y;
      
      double stationLat = 0.0;
      double stationLon = 0.0;
      
      try {
        stationLat = forecastingContext.getStationInfo().getLatitude();
        
      } catch (NullPointerException npe) {
        
        this.log.error("FMWLData populateFMSWLStationsData: stationLat= forecastingContext.getStationInfo()" +
            ".getLatitude() produced a NullPointerException !");
        throw new RuntimeException(npe);
      }
      
      try {
        stationLon = forecastingContext.getStationInfo().getLongitude();
        
      } catch (NullPointerException npe) {
        
        this.log.error("FMWLData populateFMSWLStationsData: stationLon= forecastingContext.getStationInfo()" +
            ".getLongitude() produced a NullPointerException !");
        throw new RuntimeException(npe);
      }
      
      //final double stationLat= forecastingContext.getStationInfo().getLatitude();
      //final double stationLon= forecastingContext.getStationInfo().getLongitude();
      
      this.log.debug("FMWLData populateFMSWLStationsData: Station coordinates in decimal degrees: longitude -> " + stationLon + ", latitude -> " + stationLat);
      
      final FmsParameters fmsParameters = forecastingContext.getFmsParameters();
      
      if (fmsParameters == null) {
        
        this.log.error("FMWLData populateFMSWLStationsData: fmsParameters==null !");
        throw new RuntimeException("FMWLData populateFMSWLStationsData: Cannot update forecast !");
      }
      
      final int tauHours = fmsParameters.getResidual().getTauHours().intValue();
      
      if (forecastingContext.getPredictions().size() == 0) {
        
        this.log.error("FMWLData populateFMSWLStationsData: fc.getPredictions().size()==0 for station: " + stationId);
        this.log.error("FMWLData populateFMSWLStationsData: Cannot compute new forecast for for station: " + stationId);
        throw new RuntimeException("FMWLData populateFMSWLStationsData: Cannot update forecast !");
        
        //--- The rest of the block is the code using local tidal prediction data. It was used to test new Java tidal
        // prediction package.
        //    Normally, this tidal package will be implemented(we hope!) operationnaly to be able to use it in a
        //    suitable way.
        //    The code is kept in comments if more dev. tests are needed.
//                this.log.warn("FMWLData populateFMSWLStationsData: fc.getPredictions().size()==0 for station:
//                "+stationId+ " Need to compute all its predictions !");
//
//                final long timeIncrSeconds= SECONDS_PER_MINUTE*fmsParameters.getForecast().getDeltaTMinutes()
//                .longValue();
//
//                //--- NOTE: Need to start at the nearest hour to get Foreman's method to work properly
//                (ASTRO_UDPATE_OFFSET_SECONDS==3600)
//                final long refSsePastRounded= TimeMachine.roundPastToTimeIncrSeconds(ASTRO_UDPATE_OFFSET_SECONDS,
//                this.referenceSse);
//
//                final long startTimeSeconds= refSsePastRounded - SECONDS_PER_HOUR*tauHours;
//                this.log.debug("FMWLData populateFMSWLStationsData: startTimeSeconds dt=" +SecondsSinceEpoch
//                .dtFmtString(startTimeSeconds,true));
//
//                final long endTimeSeconds= refSsePastRounded + SECONDS_PER_HOUR*fmsParameters.getForecast()
//                .getDurationHours().longValue();
//                this.log.debug("FMWLData populateFMSWLStationsData: endTimeSeconds dt=" +SecondsSinceEpoch
//                .dtFmtString(endTimeSeconds,true));
//
//                final long ts= Instant.now().toEpochMilli();
//
//                WLStationTidalPredictions.computeForecastingContextPredictions(Method.FOREMAN,
//                                                                               WLConstituentsInputFileFormat.TCF,
//                                                                               startTimeSeconds, endTimeSeconds,
//                                                                               timeIncrSeconds, forecastingContext);
//
//                                                                        //TEST BACKWARD: endTimeSeconds,
//                                                                        //startTimeSeconds, timeIncrSeconds, fc);
//                final long tf= Instant.now().toEpochMilli();
//
//                this.log.debug("FMWLData populateFMSWLStationsData: WLP produced in "+(tf-ts)+" millisecs");
      
      } else {
  
        this.log.debug("FMWLData populateFMSWLStationsData: checking if WLP uncertainty data " +
            "exists.");
  
        for (final MeasurementCustom msr : forecastingContext.getPredictions()) {
          
          if (msr.getUncertainty() == null) {

//                        this.log.debug("FMWLData populateFMSWLStationsData: msr.getUncertainty()==null at
//                        time-stamp:"+
//                                       SecondsSinceEpoch.dtFmtString(msr.getEventDate().getEpochSecond(),true)+
//                                       " ! Set it to default: "+PREDICTIONS_ERROR_ESTIMATE_METERS+" meters.");
            
            msr.setUncertainty(PREDICTIONS_ERROR_ESTIMATE_METERS);
          }
        }
      }
  
      final List<MeasurementCustom> wlpList = forecastingContext.getPredictions();
      
      if (wlpList == null) {
        
        this.log.error("FMWLData populateFMSWLStationsData: wlpList cannot be null at this point !");
        throw new RuntimeException("FMWLData populateFMSWLStationsData: Cannot update forecast !");
        
      } else if (wlpList.size() == 0) {
        
        this.log.error("FMWLData populateFMSWLStationsData: wlpList size cannot be 0 at this point !");
        throw new RuntimeException("FMWLData populateFMSWLStationsData: Cannot update forecast !");
      }
  
      final MeasurementCustom wlp0 = wlpList.get(0);
      
      final long firstWlpSeconds = wlp0.getEventDate().getEpochSecond();
  
      this.log.debug("FMWLData populateFMSWLStationsData: WLP size=" + wlpList.size());
      this.log.debug("FMWLData populateFMSWLStationsData: WLP dt0 Instant=" + wlp0.getEventDate().toString());
      this.log.debug("FMWLData populateFMSWLStationsData: WLP dt0 SSE=" + SecondsSinceEpoch.dtFmtString(firstWlpSeconds, true));
      this.log.debug("FMWLData populateFMSWLStationsData: WLP dt0 Z value=" + wlp0.getValue());
      
      //--- Control WLO data:
      final List<MeasurementCustom> wloList = forecastingContext.getObservations();
      
      if (wloList.size() > 0) {
        
        if (!FMSWLStationDBObjects.validateDBObjects(wloList, tauHours)) {
          
          this.log.warn("FMWLData populateFMSWLStationsData: Not enough WLO retreived from the database for station: "
              + stationId + ", the resulting forecast will not be optimal !");
        }
  
        final MeasurementCustom wlo0 = wloList.get(0);
        final long firstWloSeconds = wlo0.getEventDate().getEpochSecond();
  
        this.log.debug("FMWLData populateFMSWLStationsData: WLO size=" + wloList.size());
        this.log.debug("FMWLData populateFMSWLStationsData: 1st retreived WLO Instant=" + wlo0.getEventDate().toString());
        this.log.debug("FMWLData populateFMSWLStationsData: 1st retreived WLO  SSE=" + SecondsSinceEpoch.dtFmtString(firstWloSeconds, true));
        this.log.debug("FMWLData populateFMSWLStationsData: 1st retreived WLO dt0 Z value=" + wlo0.getValue());
  
        final MeasurementCustom wloLast = wloList.get(wloList.size() - 1);
  
        this.log.debug("FMWLData populateFMSWLStationsData: more recent WLO dt Instant " +
            "retreived=" + wloLast.getEventDate().toString());
        this.log.debug("FMWLData populateFMSWLStationsData: more recent WLO dt SSE retreived=" + SecondsSinceEpoch.dtFmtString(wloLast.getEventDate().getEpochSecond(), true));
        this.log.debug("FMWLData populateFMSWLStationsData: more recent WLO dt Z value " +
            "retreived=" + wloLast.getValue());
        
      } else {
        this.log.warn("FMWLData populateFMSWLStationsData: fc.getObservations().size()==0  for station: "
            + stationId + ", no residual error statistics computations will be done !");
      }
      
      //--- 1st check on the WLF data. Just report on the WLP and WLF synchronisation here.
      //    Serious erros errors with WLF data are handled later in FMSWLStationDBObjects constructor.
      if (forecastingContext.getForecasts().size() > 0) {
        
        if (!FMSWLStationDBObjects.validateDBObjects(forecastingContext.getForecasts(), tauHours)) {
          
          this.log.warn("FMWLData populateFMSWLStationsData: FMSWLStationDBObjects.validateDBObjects failed for last " +
              "forecast data retrieved from the DB");
          this.log.warn("FMWLData populateFMSWLStationsData: WLF data seems not usable!");
        }
        
      } else {
        this.log.warn("FMWLData populateFMSWLStationsData: fc.getForecasts().size()==0 for station:" + stationId);
      }
      
      this.allStationsData.add(new FMSWLStation(sit++, forecastingContext));
      
      this.log.debug("FMWLData populateFMSWLStationsData: Adding station:" + stationId + " residual to " +
          "stationsResiduals ");
      
      stationsResiduals.add(this.getFMSWLStationResidual(stationId));
    }
    
    final long fcstsTimeIncrSeconds = SECONDS_PER_MINUTE * fcstsTimeIncrMinutes;
    
    //--- Check if the WLP time-increments are the same for all stations
    //    and set the stations statistics dependencies(Objects references).
    for (final FMSWLStation fmsd : this.allStationsData) {
      
      this.log.debug("fcstsTimeIncrSeconds=" + fcstsTimeIncrSeconds + ", station=" + fmsd.getStationCode() + ", fmsd" +
          ".secondsIncr=" + fmsd.secondsIncr);
      
      if (fmsd.secondsIncr != fcstsTimeIncrSeconds) {
        
        this.log.error("FMWLData populateFMSWLStationsData: fmsd.secondsIncr=" + fmsd.secondsIncr +
            "!= fcstsTimeIncrSeconds=" + fcstsTimeIncrSeconds + " for station:" + fmsd.getStationCode());
        
        throw new RuntimeException("FMWLData populateFMSWLStationsData: Cannot update forecast !");
      }
      
      //--- Set the FMResidualFactory objects references in the covariance statistics data stuctures
      //    of the underlying FMResidualFactory object of the current fmsd.residual
      fmsd.residual.getFMSResidualFactory().setAuxCovsResiduals(fmsd.getStationCode(), stationsResiduals);
    }
    
    //--- 1st time-stamp of the WL prediction data retreived from the DB:
    final SecondsSinceEpoch sseStart =
        new SecondsSinceEpoch(fc0.getPredictions().get(0).getEventDate().getEpochSecond());
  
    this.log.debug("FMWLData populateFMSWLStationsData: Starting new forecast(s) residuals errors" +
        " statistics at date " +
        "time-stamp: " + sseStart.dateTimeString(true));
    
    this.timeNodes = new ArrayList<>(this.allStationsData.get(0).predictionsSize());
    
    //--- Setup the 1st WLTimeNode:
    //    NOTE: the WLTimeNode argument is null here because we have no past data before sseStart time-stamp:
    this.newFMSTimeNode(null, sseStart);
  
    this.log.debug("populateFMSWLStationsData end");
    
    return this;
  }
  
  /**
   * Get the IFMResidual residual of a FMWLStation object for a WL station.
   *
   * @param stationId A WL station traditional SINECO String Id.
   * @return The IFMResidual wanted if found.
   */
  @NotNull
  private final IFMSResidual getFMSWLStationResidual(@NotNull final String stationId) {
    
    this.log.debug("FMWLData getFMSWLStationResidual: Getting stationId:" + stationId + " residual");
    
    //--- Check if this.getFMSWLStation(stationId) found what we want.
    final FMSWLStation checkIt = this.getFMSWLStation(stationId);
    
    if (checkIt == null) {
      
      this.log.error("FMWLData getFMSWLStationResidual: this.getFMSWLStation(stationId) returned null for station: " + stationId);
      throw new RuntimeException("FMWLData getFMSWLStationResidual: Cannot update forecast !");
    }
    
    return checkIt.residual;
  }
  
  /**
   * @param pstrWLTimeNode : The WLTimeNode object just before in time compared to the SecondsSinceEpoch sse object
   *                       time-stamp.
   * @param sse            : A SecondsSinceEpoch object having the time-stamp where we want a new WLStationTimeNode
   *                       object ready
   *                       to use.
   * @return A new WLTimeNode ready to use.
   */
  protected final WLTimeNode newFMSTimeNode(final WLTimeNode pstrWLTimeNode, @NotNull final SecondsSinceEpoch sse) {
    
    this.log.debug("FMWLData newFMSTimeNode start: sse dt=" + sse.dateTimeString(true));
    
    for (final FMSWLStation station : this.allStationsData) {
      
      this.wlsnaTmp.add(station.getNewWLStationFMTimeNode(pstrWLTimeNode, sse, this.referenceSse));
      this.log.debug("FMWLData newFMSTimeNode: station: " + station.getStationCode() + " processed for time-stamp: " + sse.dateTimeString(true));
    }
    
    final WLTimeNode wltn = new WLTimeNode(pstrWLTimeNode, this.wlsnaTmp);
    
    this.log.debug("FMWLData newFMSTimeNode: wltn=" + wltn);
    this.log.debug("FMWLData newFMSTimeNode: wltn dt=" + wltn.getSse().dateTimeString(true));
    
    this.timeNodes.add(wltn);

//        final WLStationTimeNode check= this.timeNodes.get(this.timeNodes.size()-1).getStationNode(0);
//        this.log.debug("FMWLData newFMSTimeNode: check="+check);
//        this.log.debug("FMWLData newFMSTimeNode: check dt="+check.getSse().dateTimeString(true));
//        this.log.debug("FMWLData newFMSTimeNode: check pstr="+check.pstr());
//        if (check.pstr()!=null) {
//            this.log.debug("FMWLData newFMSTimeNode: check pstr dt="+check.pstr().getSse().dateTimeString(true));
//            this.log.debug("FMWLData newFMSTimeNode: check pstr.futr="+check.pstr().futr());
//        }
    
    //--- MUST clear this.wlsnaTmp List here for the next time-stamp iteration:
    this.wlsnaTmp.clear();
    
    this.log.debug("FMWLData newFMSTimeNode: this.timeNodes.size()=" + this.timeNodes.size());
    
    this.log.debug("FMWLData newFMSTimeNode end.");
    
    return wltn;
  }
  
  /**
   * Return a FMWLStation object contained in this.allStationsData. WARNING: The client method must check for
   * possible a null return.
   *
   * @param stationId : A WL station traditional SINECO String Id.
   * @return The FMWLStation object for the WL station wanted
   */
  @NotNull
  private final FMSWLStation getFMSWLStation(@NotNull final String stationId) {
    
    FMSWLStationDBObjects ret = null;
    
    for (final FMSWLStationDBObjects wlsd : this.allStationsData) {
      
      if (wlsd.getStationCode().equals(stationId)) {
        ret = wlsd;
        break;
      }
    }
    
    //--- FMWLStation inherits from WLStationDBObjects class
    return (FMSWLStation) ret;
  }
  
  /**
   * Utility method for writing all the results in local disk .csv files formatted in the legacy ODIM dbquery results.
   *
   * @param outDir : The local disk directory where to write the results.
   */
  public final void writeResults(final String outDir) {
    
    this.log.debug("FMWLData writeResults: start.");
    
    int stn = 0;
    
    for (final FMSWLStation station : this.allStationsData) {
      ASCIIFileIO.writeOdinAsciiFmtFile(station.getStationCode(), this.timeNodes.get(0).getStationNode(stn++),
          station.udpatedForecastData, outDir);
    }
    
    this.log.debug("FMWLData writeResults: end");
  }
}
