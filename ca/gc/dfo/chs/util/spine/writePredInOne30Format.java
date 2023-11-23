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

final public class writePredInOne30Format {

  static public void main (String[] args) {

    final String inputFilesListFile= args[0];

    System.out.println("inputFilesListFile="+inputFilesListFile);

    final List<String> filesList= ASCIIFileIO.getFileLinesAsArrayList(inputFilesListFile);

    ArrayList<ArrayList<Float>> dataIn= new ArrayList<ArrayList<Float>>(filesList.size());
    //List<String> oLines= new ArrayList<String>();

    final float encodeFactor= 1000.0f;
    //final float decodeFactor= 1.0f/encodeFactor;

    final int dataSize= 14400;

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

      dataIn.add(gpIdx, new ArrayList<Float>(dataSize));

      for (int itemIter= 0; itemIter< jsonWLDataArray.size(); itemIter++) {

        if (itemIter== dataSize) break;

        final JsonObject jsonWLDataObj=
          jsonWLDataArray.getJsonObject(itemIter);

        //final double wlDataValue= jsonWLDataObj.
        //   getJsonNumber("value").floatValue() ;

        dataIn.get(gpIdx).add(Float.valueOf((float)jsonWLDataObj.getJsonNumber("value").doubleValue()));
      }

      try {
        jsonFileInputStream.close();

      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      gpIdx++;

     System.out.println("Done with WLDataJsonFile:"+WLDataJsonFile);

      //final String [] lineSplit= line.split(";");
      //System.out.println("lineSplit[0]="+lineSplit[0]);
      //System.out.println("lineSplit[1]="+lineSplit[1]);
      //ArrayList<Float> tmpList= new ArrayList<Float>( lineSplit.length);
      //for (int vidx=0; vidx< lineSplit.length; vidx++ ) {
      //  if (lineSplit[vidx].equals("00000")) {
      //     //System.out.println("00000 item at lineIdx="+lineIdx+", vidx="+vidx);
      //     //System.out.flush();
      //     //System.exit(0);
      //     tmpList.add(vidx,0.0f);
      //  } else {
      //  //System.out.println("lineSplit[vidx]="+lineSplit[vidx]);
      //  //final String ztStrValue= lineSplit[vidx].replaceAll("^0+", "");
      //  //System.out.println("ztStrValue="+ztStrValue);
      //  //System.out.flush();
      //     tmpList.add(vidx, decodeFactor * Float.parseFloat( lineSplit[vidx].replaceAll("^0+", "") ) );
      //  //System.out.println("lineSplit[vidx].replaceAll="+lineSplit[vidx].replaceAll("^0+", ""));
      //
      //  //System.out.println("tmpList.get(vidx)="+tmpList.get(vidx));
      //  //System.out.println("Debug exit 0");
      //  //System.exit(0);
      //  }
      //}
      //dataConv.add(lineIdx, tmpList);
      //System.out.println("Debug exit 0");
      //System.exit(0);
      //lineIdx++;
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

    for (int didx= 0; didx< dataIn.size(); didx++ ) {

      //String encodedValuesBundleLine= "";

      StringBuilder encodedValuesBundleLine= new StringBuilder();

      final ArrayList<Float> dList= dataIn.get(didx);

      for (int vidx= 0; vidx< dList.size()-1; vidx++ ) {

        //System.out.println("dList.get(vidx)="+dList.get(vidx));
        //System.out.flush();

       //(int)(encodeFactor*dList.get(vidx)))+";";

        //encodedValuesBundleLine += String.format("%05d", (int)(encodeFactor*dList.get(vidx)))+";";    //(int)(encodeFactor*dList.get(vidx).floatValue()))+";";

        encodedValuesBundleLine.append( String.format("%05d", (int)(encodeFactor*dList.get(vidx)))+";");

        //System.out.println("encodedValuesBundleLine="+encodedValuesBundleLine);
        //System.out.println("Debug exit 0");
        //System.exit(0);
      }

      //encodedValuesBundleLine += String.format("%05d", (int)(encodeFactor*dList.get(dList.size()-1)) );  //(int)(encodeFactor*dList.get(dList.size()-1).floatValue()) );

      encodedValuesBundleLine.append( String.format("%05d", (int)(encodeFactor*dList.get(dList.size()-1)) ));

      try {
        ofile.write(encodedValuesBundleLine+" \n");
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
