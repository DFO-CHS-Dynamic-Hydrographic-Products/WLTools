//package ca.gc.dfo.iwls.fmservice.modeling.util;
//package ca.gc.dfo.chs.wltools.util;

/**
 * Created by Gilles Mercier on 2017-12-19.
 */

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;
import javax.json.JsonObject;
import javax.json.JsonReader;

//import javax.validation.constraints.NotNull;
import java.io.*;
import java.util.List;
import java.util.Locale;
import java.time.Instant;
import java.util.ArrayList;

//import java.io.IOException;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;


// ---
//import ca.gc.dfo.chs.wltools.wl.IWL;
//import ca.gc.dfo.chs.wltools.wl.WLStationTimeNode;
//import ca.gc.dfo.iwls.timeseries.MeasurementCustom;
//import ca.gc.dfo.chs.wltools.util.MeasurementCustom;

//---
//---
//---

final public class writePredInQ234OneFormat {

  static public void main (String[] args) {

    final String inputFilesListFile= args[0];

    System.out.println("inputFilesListFile="+inputFilesListFile);

    final List<String> filesList= ASCIIFileIO.getFileLinesAsArrayList(inputFilesListFile);

    ArrayList<ArrayList<Float>> dataIn= new ArrayList<ArrayList<Float>>(filesList.size());
    //List<String> oLines= new ArrayList<String>();

    final float encodeFactor= 1000.0f;
    //final float decodeFactor= 1.0f/encodeFactor;

    final int dataSize= 480;

    //final int skipBeg= 5;
    //final Instant firstRelevantTS= Instant.parse("2023-10-23T12:15:00Z");
    final Instant firstRelevantTS= Instant.parse("2023-10-23T12:45:00Z");

    final Instant lastRelevantTS= firstRelevantTS.plusSeconds(24L*3600L);

    int gpIdx= 0;

    for (final String WLDataJsonFile: filesList) {
    //for (int gpIdx= 0; gpIdx< 1061; gpIdx++) {

      System.out.println("Processing WLDataJsonFile:"+WLDataJsonFile);

      FileInputStream jsonFileInputStream= null;

      try {
        jsonFileInputStream= new FileInputStream(WLDataJsonFile);

      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }

      final JsonArray jsonWLDataArray= Json.
        createReader(jsonFileInputStream).readArray();

      dataIn.add(gpIdx, new ArrayList<Float>());

      for (int itemIter= 0; itemIter< jsonWLDataArray.size(); itemIter++) {

        //if (itemIter== dataSize) break;

        final JsonObject jsonWLDataObj=
          jsonWLDataArray.getJsonObject(itemIter);

        //final double wlDataValue= jsonWLDataObj.
        //   getJsonNumber("value").floatValue() ;

        final Instant checkInstant= Instant.parse(jsonWLDataObj.getString("eventDate"));

        if ( checkInstant.equals(lastRelevantTS) ) break;

        if ( checkInstant.isBefore(firstRelevantTS) ) continue;

        //if ( checkInstant.isAfter(lastRelevantTS) ) break;
        //System.out.println("relevant ts="+checkInstant.toString());

        dataIn.get(gpIdx).add(Float.valueOf((float)jsonWLDataObj.getJsonNumber("value").doubleValue()));

        //if ( checkInstant.equals(lastRelevantTS) ) break;
        //System.exit(0);
      }

      try {
        jsonFileInputStream.close();

      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      if (dataIn.get(gpIdx).size()!=dataSize) {
        System.out.println("dataIn.get(gpIdx).size()!=dataSize !!");
        System.out.println("dataIn.get(gpIdx).size()="+dataIn.get(gpIdx).size());
        System.exit(1);
      }

      gpIdx++;

      System.out.println("Done with WLDataJsonFile:"+WLDataJsonFile);
    }

    //System.out.println("Debug exit 0");
    //System.exit(0);
    //lineIdx= 0;

   FileWriter ofile;

    try {
      ofile= new FileWriter(args[1]);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    System.out.println("Writing this annoying and dumb format in file: "+args[1]);

    for (int didx= 0; didx< dataIn.size(); didx++ ) {

      //String encodedValuesBundleLine= "";

      StringBuilder encodedValuesBundleLine= new StringBuilder();

      final ArrayList<Float> dList= dataIn.get(didx);

      for (int vidx= 0; vidx< dList.size(); vidx++ ) {

        //System.out.println("dList.get(vidx)="+dList.get(vidx));
        //System.out.flush();

       //(int)(encodeFactor*dList.get(vidx)))+";";

        //encodedValuesBundleLine += String.format("%05d", (int)(encodeFactor*dList.get(vidx)))+";";    //(int)(encodeFactor*dList.get(vidx).floatValue()))+";";

        encodedValuesBundleLine.append( String.format("%05d", (int)(encodeFactor*dList.get(vidx)))+";");

        //System.out.println("encodedValuesBundleLine="+encodedValuesBundleLine);
        //System.out.println("Debug exit 0");
        //System.exit(0);
      }

      //encodedValuesBundleLine.append( String.format("%05d", (int)(encodeFactor*dList.get(dList.size()-1)) ));

      try {
        ofile.write(encodedValuesBundleLine+"\n");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

     //ofile.flush();

    }

    try {
      ofile.close();
    } catch (IOException e) {
       throw new RuntimeException(e);
    }

  }

}
