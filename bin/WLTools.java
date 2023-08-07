//import javax.json;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Calendar;
import java.net.URISyntaxException;
import java.util.GregorianCalendar;

import ca.gc.dfo.chs.wltools.WLToolsIO;
import ca.gc.dfo.chs.wltools.IWLToolsIO;
import ca.gc.dfo.chs.wltools.tidal.ITidal;
import ca.gc.dfo.chs.wltools.tidal.ITidalIO;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStage;
import ca.gc.dfo.chs.wltools.nontidal.stage.IStageIO;
//import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPred;
import ca.gc.dfo.chs.wltools.wl.prediction.WLStationPredFactory;

final public class WLTools extends WLToolsIO {

   //static {
   //   System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
   //}

   //private static String mainCfgDir= "";

   // ---
   //final static String getMainCfgDir() {
   //   return mainCfgDir;
   //}

   static public void main (String[] args) {

      System.out.println("WLTools main start");

      Map<String, String> argsMap = new HashMap<>();

      for (String arg: args) {
        String[] parts = arg.split("=");
        argsMap.put(parts[0], parts[1]);
      }

      final Set<String> argsMapKeySet= argsMap.keySet();

      if (!argsMapKeySet.contains("--stationPredType")) {
        throw new RuntimeException("WLTools main: Need to have the mandatory prediction location info option: --stationPredType" );
      }

      final String stationPredType= argsMap.get("--stationPredType" );

      if (!stationPredType.equals("TIDAL:"+ITidal.Method.NON_STATIONARY_FOREMAN.name())) {
        throw new RuntimeException("WLTools main: Only TIDAL:"+
                                   ITidal.Method.NON_STATIONARY_FOREMAN.name()+" prediction method allowed for now!!");
      }

      if (!argsMapKeySet.contains("--stationIdInfo")) {
        throw new RuntimeException("WLTools main: Must have the mandatory prediction location info option: --stationIdInfo" );
      }

      final String stationIdInfo= argsMap.get("--stationIdInfo" );

      System.out.println("WLTools main: stationPredType="+stationPredType);
      System.out.println("WLTools main: stationIdInfo="+stationIdInfo);
      //System.out.println("WLTools main: debug System.exit(0)");
      //System.exit(0);

      File binDir= null;

      try {
         binDir= new File(WLTools.class.getProtectionDomain().getCodeSource().getLocation().toURI());
      } catch (URISyntaxException e) {
         throw new RuntimeException(e);
      }

      System.out.println("WLTools main: binDir="+binDir);
      //System.out.println("WLTools main: debug exit(0)");
      //System.exit(0);

      //this.super(mainExecDir + "/../"+IWLToolsIO.PKG_CFG_MAIN_DIR);
     // mainCfgDir= mainExecDir + "/../"+IWLToolsIO.PKG_CFG_MAIN_DIR;

      //final WLToolsIO io= new WLToolsIO(mainExecDir + "/../"+IWLToolsIO.PKG_CFG_MAIN_DIR);
      WLToolsIO.setMainCfgDir(binDir+ "/../"+IWLToolsIO.PKG_CFG_MAIN_DIR);

      System.out.println("WLTools main: mainCfgDir= "+WLToolsIO.getMainCfgDir());

      //System.out.println("WLTools main: debug exit(0)");
      //System.exit(0);

      //final String nsTCInputFile= WLToolsIO.getMainCfgDir()+
      //  "/tidal/nonStationary/StLawrence/dischargeClusters/Deschaillons/dischargeClimatoTFHA/gridPoint-540-TFHA.json";

      //"/home/gme042/slinks/fs7_isi_dfo_chs_enav/NSTide/output/TFHA/onedGridPointsClusters/Deschaillons/tmp/gridPoint-540-TFHA.json";

      ///final startSse= new Date().getTime()/1000L;
      final long unixTimeNow = System.currentTimeMillis() / 1000L;

      Calendar gcld= new GregorianCalendar().getInstance(TimeZone.getTimeZone("GMT"));
      gcld.set(GregorianCalendar.YEAR, 2014); //2023); //2014);
      gcld.set(GregorianCalendar.MONTH, 0); //7); //0); // --- January is month 0 in Java.
      gcld.set(GregorianCalendar.DAY_OF_MONTH,1);
      gcld.set(GregorianCalendar.HOUR_OF_DAY,0);
      gcld.set(GregorianCalendar.MINUTE,0);
      gcld.set(GregorianCalendar.SECOND,0);

      final Long testStartTime= gcld.getTimeInMillis()/1000L;

      final Long endPredTime= testStartTime + 40L*24L*3600L; //  unixTimeNow + 40L*24L*3600L

      //if (predType.equals("TIDAL:"+ITidal.Method.NON_STATIONARY_FOREMAN.name()) {
     //final String stationIdInfo=
      //   stationPredInfo+IStageIO.STATION_DISCHARGE_INPUT_FNAME_SUFFIX;

      WLStationPredFactory wlStnPrdFct= null;

      final String [] stationPredTypeSplit=
        stationPredType.split(IStageIO.STATION_ID_SPLIT_CHAR);

      final String mainPredType= stationPredTypeSplit[0];

      if ( mainPredType.equals("TIDAL") ) {

        /// --- Tidal pred. method is ITidal.Method.NON_STATIONARY_FOREMAN by default.
        ITidal.Method tidalMethod= ITidal.Method.NON_STATIONARY_FOREMAN;

        final String specTidalMethod= stationPredTypeSplit[1];

        if (specTidalMethod.equals(ITidal.Method.STATIONARY_FOREMAN.name())) {

           //tidalMethod= ITidal.Method.STATIONARY_FOREMAN;

           throw new RuntimeException("WLTools main:The"+
                                      ITidal.Method.STATIONARY_FOREMAN.name()+
                                      " tidal prediction method is not allowed for now!!");
        }

        IStage.Type stageType= IStage.Type.DISCHARGE_CFG_STATIC;

        if (tidalMethod == ITidal.Method.NON_STATIONARY_FOREMAN) {

          if (!argsMapKeySet.contains("--stageType")) {

            throw new RuntimeException("WLTools main: Must have the --stageType option defined if tidal method is"+
                                       ITidal.Method.NON_STATIONARY_FOREMAN.name()+ " !!");
          }

          final String stageTypeCheck= argsMap.get("--stageType");

          if (stageTypeCheck.equals(IStage.Type.DISCHARGE_FROM_MODEL.name())) {
            stageType= IStage.Type.DISCHARGE_FROM_MODEL;
          }

          if (stageType != IStage.Type.DISCHARGE_CFG_STATIC) {
            throw new RuntimeException("WLTools main: Only "+
                                       IStage.Type.DISCHARGE_CFG_STATIC.name()+ " stage type allowed for now !!");
          }
        }

        if (!argsMapKeySet.contains("--tidalConstsInputFileFormat")) {
          throw new RuntimeException("WLTools main: Must have the --tidalConstsInputFileFormat option defined if mainPredType == TIDAL !!");
        }

        final String tidalConstsInputFileFormat= argsMap.get("--tidalConstsInputFileFormat");

        if (!tidalConstsInputFileFormat.
               equals(ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON.name())) {

          throw new RuntimeException("WLTools main: Only the:"+
                                      ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON.name()+
                                     " tidal prediction input file format allowed for now!!");
        }

        final ITidalIO.WLConstituentsInputFileFormat
          tidalConstsInputFileFmt= ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON;

        wlStnPrdFct= new WLStationPredFactory(stationIdInfo,//"StLawrence:Deschaillons:gridPoint-540-SCD",
                                              testStartTime, //unixTimeNow,
                                              endPredTime,
                                              180L,//180L, //900L, //3600L,//900L,
                                              46.55,
                                              tidalMethod, //ITidal.Method.NON_STATIONARY_FOREMAN,
                                              null, //nsTCInputFile,
                                              tidalConstsInputFileFmt,//ITidalIO.WLConstituentsInputFileFormat.NON_STATIONARY_JSON,                                                                        IStage.Type.DISCHARGE_CFG_STATIC,
                                              stageType, // --- IStage.Type.DISCHARGE_CFG_STATIC: Stage data taken from inner config DB
                                              null, // --- Stage input data file
                                              null  // --- IStage.Type.DISCHARGE_CFG_STATIC IStageIO.FileFormat is JSON by default.
                                             );

      }

      wlStnPrdFct.getTidalPredictionsForStation();

      System.out.println("WLTools main end");
  }
}
