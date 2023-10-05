//package ca.gc.dfo.iwls.fmservice.modeling.fms;
package ca.gc.dfo.chs.wltools.wl.fms;

import java.util.List;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.wl.WLTimeNode;
//import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.util.ASCIIFileIO;
import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
import ca.gc.dfo.chs.wltools.util.SecondsSinceEpoch;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.wl.fms.FMSWLStationData;

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
final public class FMSWLData implements IFMS { //, ITidal, ITidalIO {

  private final static String whoAmI= "ca.gc.dfo.chs.wltools.wl.fms.FMSWLData";

  /**
   * log utility.
   */
  private static final Logger slog= LoggerFactory.getLogger(whoAmI);

  /**
   * List of WLTimeNode object(s). Either one(default case) or possibly more WLTimeNode objects.
   */
  List<WLTimeNode> timeNodes= null;

  /**
   * List of FMWLStation objects for one or more WL station(s).
   */
  List<FMSWLStation> allStationsData= null;

  /**
   * The seconds since epoch representing the time at which the ForecastingContext was issued (i.e. it separates the
   * past from the future time-stamps in real tine regardless of the time stamp of the last available valid WLOs)
   */
  private long referenceSse= 0L;

  /**
   * List of WLStationTimeNode object(s) used as a temporary storage for subsequent usage.(see method FMWLData
   * .newFMSTimeNode below in this src file).
   */
  private List<WLStationTimeNode> wlsnaTmp= null;

  /**
   * @param fcstsTimeIncrMinutes   : Time increment between each WLF data
   * @param forecastingContextList : List of ForecastingContext(Must have at least one item).
   */
  FMSWLData(/*@Min(0)*/ final long fcstsTimeIncrMinutes,
            /*@NotNull @Size(min = 1) */ final List<FMSInput> fmsInputList) {

          // /*@NotNull @Size(min = 1) */ final List<FMSConfig> fmsConfigList) {
          //  /*@NotNull @Size(min = 1) */ final List<ForecastingContext> forecastingContextList) {

    final String mmi= "FMSWLData main constructor: ";

    slog.info(mmi+"start");

    //--- Check forecastingContextList before doing anything else with it.
    try {
      fmsInputList.size();

    } catch (NullPointerException npe) {

      slog.error(mmi+"fmsInputList==null !");
      throw new RuntimeException(mmi+npe);
    }

    if (fmsInputList.size() == 0) {

      slog.error(mmi+"fmsInputList.size()==0 !");
      throw new RuntimeException(mmi+"Cannot update forecast !");
    }

    try {
      fmsInputList.get(0).getReferenceTime();

    } catch (NullPointerException npe) {

      slog.error(mmi+"fmsInputList.get(0)==null !");
      throw new RuntimeException(mmi+npe);
    }

    try {
      fmsInputList.get(0).getReferenceTime().getEpochSecond();

    } catch (NullPointerException npe) {

      slog.error(mmi+"fmsInputList.get(0).getReferenceTime().getEpochSecond()==null !");
      throw new RuntimeException(npe);
    }

    this.referenceSse= fmsInputList.get(0).getReferenceTimeInSeconds(); //forecastingContextList.get(0).getReferenceTime().getEpochSecond();

    slog.info(mmi+"this.referenceSse date-time is: "+
              SecondsSinceEpoch.dtFmtString(this.referenceSse, true));

    this.allStationsData= new ArrayList<FMSWLStation>(fmsInputList.size());

    this.wlsnaTmp= new ArrayList<WLStationTimeNode>(fmsInputList.size());

    this.populateFMSWLStationsData(fcstsTimeIncrMinutes, fmsInputList); //forecastingContextList);

    slog.info(mmi+"end: this.timeNodes.size()=" + this.timeNodes.size());
    slog.info(mmi+"Debug exit 0");
    System.exit(0);
  }

  // --- Return the updatedForecastData List<MeasurementCustom> object
  //     of the TG station being processed (which is always at index 0)
  final List<MeasurementCustom> getUpdatedForecastDataForTGStn0() {
    return this.allStationsData.get(0).getUpdatedForecastData();
  }

  /**
   * Populate all the objects structures of all the ForecastingContext objects of the forecastingContextList argument.
   * NOTE: forecastingContextList must have already been controlled in terms of objects existence.
   *
   * @param fcstsTimeIncrMinutes   : Time increment between each WLF data
   * @param forecastingContextList : The List of ForecastingContext objects (minimum size 1).
   * @return This FMWLData object fully populated.
   */
  //@NotNull
  private final FMSWLData populateFMSWLStationsData(/*@Min(0)*/ final long fcstsTimeIncrMinutes,
                                                    /*@NotNull @Size(min = 1)*/ final List<FMSInput> fmsInputList) {
                                                    //@NotNull @Size(min = 1) final List<ForecastingContext> forecastingContextList) {

    final String mmi= "populateFMSWLStationsData: ";

    slog.debug(mmi+"start");

    slog.debug(mmi+"fcstsTimeIncrMinutes="+fcstsTimeIncrMinutes+
               ", " + "fmsInputList.size()=" + fmsInputList.size());

    int sit= 0;

    final List<IFMSResidual> stationsResiduals= new ArrayList<IFMSResidual>();

    //final ForecastingContext fc0 = forecastingContextList.get(0);
    final FMSInput fmsInput0= fmsInputList.get(0);

    final String residualMethod=
      fmsInput0.getFMSResidualConfig().getMethod();  //fc0.getFmsParameters().getResidual().getMethod();

    //if (forecastingContextList.size() > 1) {
    if (fmsInputList.size() > 1) {

      // --- Stop exec here if fmsConfig.size() > 1 since this has not been tested yet
      //throw new RuntimeException(mmi+"Use of more than one TG station has not been tested yet!");

      slog.error(mmi+"fmsInputList.size() > 1: Usage of more than one TG station has not been tested yet!! exit 1!");
      System.exit(1);

      slog.info(mmi+"fmsInputList.size() > 1: Need to validate all the related FMSConfig items with each other.");

      if (! FMSResidualFactory.validateStationsFMSConfig(residualMethod, fmsInputList)) { // forecastingContextList)) {

        slog.error(mmi+"fmsConfigList items validation failed");
        throw new RuntimeException(mmi+"Cannot update forecast !");
      }

      slog.info(mmi+"fmsInputList.size() > 1: Success for the FMSConfig items validation.");
    }

    //--- Create new FMWLStationData Objects in this.allStationsData:
    //for (final ForecastingContext forecastingContext : forecastingContextList) {
    for (final FMSInput fmsInputItem: fmsInputList) {

      if (fmsInputItem == null) {

        slog.error(mmi+"fmsInputItem==null!");
        throw new RuntimeException(mmi+"Cannot update forecast !");
      }

      final String stationId= fmsInputItem.getStationId();

      slog.debug(mmi+"Populating residuals errors statistics data structures for station: " + stationId);

      final int tauHours= (int) fmsInputItem.getFMSResidualConfig().getTauHours();

      slog.info(mmi+"tauHours="+tauHours);

      //if (forecastingContext.getPredictions().size() == 0) {
      if (fmsInputItem.getPredictions().size() == 0) {

        slog.error(mmi+"fmsInputItem.getPredictions().size()==0 for station: " + stationId);
        //slog.error("FMWLData populateFMSWLStationsData: fmsInputItem. compute new forecast for for station: " + stationId);
        throw new RuntimeException(mmi+"Cannot update forecast !");

      }
//else {
//        //slog.info(mmi+"checking if WLP uncertainty data exists.");
//        for (final MeasurementCustom msr : fmsInputItem.getPredictions()) {
//          if (msr.getUncertainty() == null) {
//                        this.log.debug("FMWLData populateFMSWLStationsData: msr.getUncertainty()==null at
//                        time-stamp:"+
//                                       SecondsSinceEpoch.dtFmtString(msr.getEventDate().getEpochSecond(),true)+
//                                       " ! Set it to default: "+PREDICTIONS_ERROR_ESTIMATE_METERS+" meters.");
//            msr.setUncertainty(PREDICTIONS_ERROR_ESTIMATE_METERS);
//          }
//        }
//      }

      final List<MeasurementCustom> wlpList= fmsInputItem.getPredictions();

      if (wlpList == null) {

        slog.error(mmi+"wlpList cannot be null at this point !");
        throw new RuntimeException(mmi+"Cannot update forecast !");

      } else if (wlpList.size() == 0) {

        slog.error(mmi+"wlpList size cannot be 0 at this point !");
        throw new RuntimeException(mmi+"Cannot update forecast !");
      }

      final MeasurementCustom wlp0= wlpList.get(0);

      final long firstWlpSeconds= wlp0.getEventDate().getEpochSecond();

      slog.info(mmi+"WLP size=" + wlpList.size());
      slog.info(mmi+"WLP dt0 Instant=" + wlp0.getEventDate().toString());
      slog.info(mmi+"WLP dt0 SSE=" + SecondsSinceEpoch.dtFmtString(firstWlpSeconds, true));
      slog.info(mmi+"WLP dt0 Z value=" + wlp0.getValue());

      //--- Control WLO data:
      final List<MeasurementCustom> wloList= fmsInputItem.getObservations();

      if (wloList.size() > 0) {

        //if (!FMSWLStationData.validate(wloList, tauHours)) {
        //  slog.warn(mmi+"Not enough WLO retreived from the database for station: "+
        //            stationId + ", the resulting forecast will not be optimal !");
        //}

        final MeasurementCustom wlo0= wloList.get(0);
        final long firstWloSeconds= wlo0.getEventDate().getEpochSecond();

        slog.info(mmi+"WLO size=" + wloList.size());
        slog.info(mmi+"1st retreived WLO Instant=" + wlo0.getEventDate().toString());
        slog.info(mmi+"1st retreived WLO SSE=" + SecondsSinceEpoch.dtFmtString(firstWloSeconds, true));
        slog.info(mmi+"1st retreived WLO dt0 Z value=" + wlo0.getValue());

        final MeasurementCustom wloLast= wloList.get(wloList.size() - 1);

        slog.info(mmi+"more recent WLO dt Instant retreived=" + wloLast.getEventDate().toString());

        slog.info(mmi+"more recent WLO dt SSE retreived="+
                  SecondsSinceEpoch.dtFmtString(wloLast.getEventDate().getEpochSecond(), true));

        slog.debug(mmi+"more recent WLO dt Z value retreived=" + wloLast.getValue());

      } else {

        slog.warn(mmi+"fmsInputItem.getObservations().size()==0  for station: "
            + stationId + ", no residual error statistics computations will be done !");
      }

      ////--- 1st check on the storm surge model forecast data. Just report on the WLP and WLF synchronisation here.
      ////    Serious erros errors with WLF data are handled later in FMSWLStationDBObjects constru.
      //if (fmsInputItem.getModelForecasts().size() > 0) {
      //  if (!FMSWLStationData.validate(fmsInputItem.getModelForecasts(), tauHours)) {
      //   slog.info(mmi+"FMSWLStationData.validate failed for last model forecast data !");
      //   slog.info(mmi+"model forecast data seems not usable!");
      //  }
      //} else {

      if (fmsInputItem.getModelForecasts().size() ==  0) {
        slog.warn(mmi+"fmsInputItem.getModelForecasts().size()==0 for station:" + stationId);
      }

      this.allStationsData.add(new FMSWLStation(sit++, fmsInputItem)); //forecastingContext));

      slog.info(mmi+"Adding station:"+stationId + " residual to stationsResiduals");

      stationsResiduals.add(this.getFMSWLStationResidual(stationId));

    } // --- for (final FMSInput fmsInputItem : FMSInputList) {

    final long fcstsTimeIncrSeconds= SECONDS_PER_MINUTE * fcstsTimeIncrMinutes;

    //--- Check if the WLP time-increments are the same for all stations
    //    and set the stations statistics dependencies(Objects references).
    for (final FMSWLStation fmsd : this.allStationsData) {

      slog.info(mmi+"fcstsTimeIncrSeconds="+fcstsTimeIncrSeconds+", station="+
        fmsd.getStationId() + ", fmsd" + ".secondsIncr=" + fmsd.secondsIncr);

      if (fmsd.secondsIncr != fcstsTimeIncrSeconds) {

        slog.error(mmi+"fmsd.secondsIncr=" + fmsd.secondsIncr +
          "!= fcstsTimeIncrSeconds="+ fcstsTimeIncrSeconds + " for station:" + fmsd.getStationId());

        throw new RuntimeException(mmi+"Cannot update forecast !");
      }

      //--- Set the FMResidualFactory objects references in the covariance statistics data stuctures
      //    of the underlying FMResidualFactory object of the current fmsd.residual
      fmsd.getIFMSResidual().getFMSResidualFactory().
        setAuxCovsResiduals(fmsd.getStationId(), stationsResiduals);
    }

    slog.info(mmi+"After fmsd.getIFMSResidual().getFMSResidualFactory()");
    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    //--- 1st time-stamp of the WL prediction data retreived from the DB:
    final SecondsSinceEpoch sseStart= new
      SecondsSinceEpoch(fmsInput0.getObservations().get(0).getEventDate().getEpochSecond());
      //SecondsSinceEpoch(fmsInput0.getPredictions().get(0).getEventDate().getEpochSecond());

    slog.info(mmi+
              "Starting new QC forecast(s) residuals errors statistics at date time-stamp: " + sseStart.dateTimeString(true));

    //slog.info(mmi+"Debug exit 0");
    //System.exit(0);

    this.timeNodes= new
      ArrayList<WLTimeNode>(this.allStationsData.get(0).predictionsSize());

    //--- Setup the 1st WLTimeNode:
    //    NOTE: the WLTimeNode argument is null here because we have no past data before sseStart time-stamp:
    this.newFMSTimeNode(null, sseStart);

    slog.info(mmi+"end");
    slog.debug(mmi+"Debug exit 0");
    System.exit(0);

    return this;
  }

  /**
   * Get the IFMResidual residual of a FMWLStation object for a WL station.
   *
   * @param stationId A WL station traditional SINECO String Id.
   * @return The IFMResidual wanted if found.
   */
  //@NotNull
  private final IFMSResidual getFMSWLStationResidual(/*@NotNull*/ final String stationId) {

    final String mmi= "getFMSWLStationResidual: ";

    slog.info(mmi+"Getting stationId: " + stationId + " WL IFMSResidual object");

    //--- Check if this.getFMSWLStation(stationId) found what we want.
    final FMSWLStation checkIt= this.getFMSWLStation(stationId);

    if (checkIt == null) {
      slog.error(mmi+"this.getFMSWLStation(stationId) returned null for station: " + stationId);
      throw new RuntimeException(mmi+"Cannot update forecast !");
    }

    return checkIt.getIFMSResidual();
  }

  /**
   * @param pstrWLTimeNode : The WLTimeNode object just before in time compared to the SecondsSinceEpoch sse object
   *                       time-stamp.
   * @param sse            : A SecondsSinceEpoch object having the time-stamp where we want a new WLStationTimeNode
   *                       object ready
   *                       to use.
   * @return A new WLTimeNode ready to use.
   */
  protected final WLTimeNode newFMSTimeNode(final WLTimeNode pstrWLTimeNode, /*@NotNull*/ final SecondsSinceEpoch sse) {

    final String mmi= "newFMSTimeNode: ";

    slog.info(mmi+"sse dt=" + sse.dateTimeString(true));

    for (final FMSWLStation station: this.allStationsData) {

      this.wlsnaTmp.add(station.getNewWLStationFMTimeNode(pstrWLTimeNode, sse, this.referenceSse));

      slog.info(mmi+"station: " + station.getStationId()+
                " processed for time-stamp: " + sse.dateTimeString(true));
    }

    final WLTimeNode wltn= new WLTimeNode(pstrWLTimeNode, this.wlsnaTmp);

    //slog.debug(mmi+"wltn=" + wltn);
    slog.info(mmi+"wltn dt=" + wltn.getSse().dateTimeString(true));

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

    slog.info(mmi+"this.timeNodes.size()=" + this.timeNodes.size());

    slog.info(mmi+"end.");

    return wltn;
  }

  /**
   * Return a FMWLStation object contained in this.allStationsData. WARNING: The client method must check for
   * possible a null return.
   *
   * @param stationId : A WL station traditional SINECO String Id.
   * @return The FMWLStation object for the WL station wanted
   */
  //@NotNull
  private final FMSWLStation getFMSWLStation(/*@NotNull*/ final String stationId) {

    FMSWLStationData ret= null;

    for (final FMSWLStationData wlsd : this.allStationsData) {

      if (wlsd.getStationId().equals(stationId)) {
        ret = wlsd;
        break;
      }
    }

    //--- FMWLStation inherits from WLStationDBObjects class
    return (FMSWLStation) ret;
  }

  /**
   * Utility method for writing all the results in local disk .csv files formatted in the legacy ODIN dbquery results.
   *
   * @param outDir : The local disk directory where to write the results.
   */
  public final void writeCSVOnDisk(final String outDir) {

    final String mmi= "writeCSVOnDisk: ";

    slog.info(mmi+"start.");

    int stn = 0;

    for (final FMSWLStation station : this.allStationsData) {

      ASCIIFileIO.writeOdinAsciiFmtFile(station.getStationId(),
                                        this.timeNodes.get(0).getStationNode(stn++),
                                        station.getUpdatedForecastData(), outDir);
    }

    slog.info(mmi+"end");
  }
}
