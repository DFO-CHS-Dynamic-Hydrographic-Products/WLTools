package ca.gc.dfo.chs.util.spine;
import as.hdfql.*;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Read s-104 Dcf8 file and generate the 5 ASCII files needed by SPINE
 */
public class S104Dcf8ToAscii {

    static class SpineData {
        /**
        * Data Class holding the information needed to generate the 5 ASCII files needed by SPINE
        */

        public Instant startTime;
        public Instant endTime;
        public List<MeasurementCustomBundle> data;
    }

    public static void runConversion(String timeString, String outputDir, String h5Path, String type) {
        /**
        * Create spineData object from S-111 file and run file writing script
        */

        // Create Instant from ISO time string
        // Instant.parse will trow dateTime exception if in wrong format
        Instant timeInstant = Instant.parse(timeString);


        // Generate SpineData
        final SpineData fileData = s104ToSpineData(h5Path);

        // Write files to output directory 
        genSpineAsciiFiles(timeInstant, outputDir, fileData, type);
    };

    static SpineData s104ToSpineData(String h5Path) {
        /**
        * Read s-104 Dcf8 file and return a Spine Data Class object
        */

        SpineData spineData = new SpineData();
        List<MeasurementCustomBundle> data = new ArrayList<>();;

        // Read h5 file
        final String hdfqlUseFile = "USE FILE " + h5Path;
        HDFql.execute(hdfqlUseFile);

        // Parse first and last timestamps
        final String hdfqlSelectStartTime = "SELECT FROM WaterLevel/WaterLevel.01/dateTimeOfFirstRecord";
        HDFql.execute(hdfqlSelectStartTime);
        HDFql.cursorFirst();
        String startTimeStr = HDFql.cursorGetChar();
        startTimeStr = startTimeStr.substring(0,4)+ "-" + startTimeStr.substring(4,6) + "-" + startTimeStr.substring(6,11)+":" +startTimeStr.substring(11,13) +":" + startTimeStr.substring(13,16);
        final Instant startTime =Instant.parse(startTimeStr);

        final String hdfqlSelectEndTime = "SELECT FROM WaterLevel/WaterLevel.01/dateTimeOfLastRecord";
        HDFql.execute(hdfqlSelectEndTime);
        HDFql.cursorFirst();
        String endTimeStr = HDFql.cursorGetChar();
        endTimeStr = endTimeStr.substring(0,4)+ "-" + endTimeStr.substring(4,6) + "-" + endTimeStr.substring(6,11)+":" +endTimeStr.substring(11,13) +":" + endTimeStr.substring(13,16);

        final Instant endTime =Instant.parse(endTimeStr);

        

        // Read data group and return MeasurementCustomBundle List
        
        // Get Number of Groups
        String hdfqlGetGroupsNo = "SHOW WaterLevel/WaterLevel.01/ LIKE \"Group\"";
        HDFql.execute(hdfqlGetGroupsNo);
        Long numGroupLong = HDFql.cursorGetCount();
        Integer numGroup= Math.toIntExact(numGroupLong);

        

        // Create Arrays of group names
        String[] groupsNames = new String[numGroup];
        HDFql.variableRegister(groupsNames);
        String hdfqlGetGroupNames = "SHOW WaterLevel/WaterLevel.01/ LIKE \"Group\" INTO MEMORY " + HDFql.variableGetNumber(groupsNames);
        HDFql.execute(hdfqlGetGroupNames);
        HDFql.variableUnregister(groupsNames);

        // Get Data
        for (String i: groupsNames) {
            String groupPath = "WaterLevel/WaterLevel.01/" + i;
            String hdfqlSelectValues = "SELECT FROM " + groupPath + "/values";
            HDFql.execute(hdfqlSelectValues);

            // Loop trough group and generate MeasurementCustom items
            List<MeasurementCustom> bundledValues = new ArrayList<>();
            Integer minutesAfterStart = 0;
            while(HDFql.cursorNext() == HDFql.SUCCESS) {
                // eventDate
                Instant eventDate = startTime.plus(minutesAfterStart, ChronoUnit.MINUTES);
                minutesAfterStart = Integer.valueOf(minutesAfterStart.intValue() + 3);
                // value
                Float floatValue = HDFql.cursorGetFloat();
                Double value = (double) floatValue;
                HDFql.cursorNext();
                // Skip trend
                HDFql.cursorNext();
                // uncertainty
                float floatUncertainty = HDFql.cursorGetFloat();
                Double uncertainty = (double) floatUncertainty;
                MeasurementCustom iMeasurement = new MeasurementCustom();
                iMeasurement.setEventDate(eventDate);
                iMeasurement.setValue(value);
                iMeasurement.setUncertainty(uncertainty);
                bundledValues.add(iMeasurement);
                
            }
            
            // Generate MeasurementCustomBundle and append to array
             MeasurementCustomBundle GroupMCB = new MeasurementCustomBundle(bundledValues);
             data.add(GroupMCB);

    
            }
        
        //Populate spineData object
        spineData.data = data;
        spineData.startTime = startTime;
        spineData.endTime = endTime;

        return spineData;

    }

    private static void doubleToSpineStringBuilder(Double num, StringBuilder stringBuilder) {
        /*
         *  Convert Double to string used in SPINE ascii files
         *  and append to existing stringBuilder object
         *  Ex.: 1.2 > "00120;"
         */
        String formatedNum;
        Long numLong = Math.round(num*100);
        if ((numLong > 99999)||(numLong < -9999)){
            throw new RuntimeException(numLong + " Invalid value in source data");
        } else if (numLong < 0){
            formatedNum = "-" + String.format("%04d", Math.abs(numLong))+";";
        } else {
            formatedNum = String.format("%05d", numLong)+";";
        }
        stringBuilder.append(formatedNum);
    }

    private static void lineBuilderSpineAscii(Instant start, Instant end, MeasurementCustomBundle mCBundle,StringBuilder stringBuilder, Boolean values) {
        /*
         * Append new line to existing StringBuilder from MeasurementCustomBundle containing Spine station data
         * Start appending values from the Instant defined in the "start" parameter
         * if values is set to False, will use uncertainty field instead of values
         */
        if (!(mCBundle.contains(start))) {
            throw new RuntimeException(start + " start time not indexed in source data");
        }
        if (!(mCBundle.contains(end))) {
            throw new RuntimeException(start + " source file doesn't contain enough data to generate file from specified start time");
        }
        /* Probably not the smartest way to do this loop */
        for ( final Instant instantIter: mCBundle.getInstantsKeySet() ) {
            if ((!(instantIter.isBefore(start))) && (instantIter.isBefore(end) || instantIter.equals(end))) {
                MeasurementCustom mc = mCBundle.getAtThisInstant(instantIter);
                if (values) {
                    doubleToSpineStringBuilder(mc.getValue(), stringBuilder);
                } else {
                    doubleToSpineStringBuilder(mc.getUncertainty(), stringBuilder);
                }
            } 

        }
        stringBuilder.append(" \n");
    }

    private static Instant getEndTime(String type,  Instant start){
        /**
        * Generate Instant corresponding to final time stamp from start time and file type
        */
        Instant end;
        if (type.equals("30") || type.equals("UU")) {
            end = start.plus(30, ChronoUnit.DAYS);
        } else {
            end = start.plus(1, ChronoUnit.DAYS);
        }
        return end;
    }

    private static String getFileName(String type){
        /**
        * Return full fill name string from type code string
        */
        String fileName;
        if(type.equals("UU")) {
            fileName = "mat_erreur.dat.1061";
        } else if(type.equals("30")) {
            fileName = "YMMDDHH.one30.1061";
        } else if(type.equals("Q2")) {
            fileName = "YYMMDDHH.Q2.one.1061";
        } else if(type.equals("Q3")) {   
            fileName = "YYMMDDHH.Q3.one.1061";
        } else if(type.equals("Q4")) {     
            fileName = "YYMMDDHH.Q4.one.1061";
        } else {
            throw new RuntimeException("Invalid type, must be 30, Q1, Q2, Q4 or UU");  
        }
        return fileName;
    }



    static void genSpineAsciiFiles(Instant timeInstant,String outputDir, SpineData fileData, String type) {
        /**
        * Generate the five ASCII files needed by SPINE
        * Valid files types:
        * 30 = YYMMDDHH.one30.1061
        * Q2 = YYMMDDHH.Q2.one.1061
        * Q3 = YYMMDDHH.Q3.one.1061
        * Q4 = YYMMDDHH.Q4.one.1061
        * UU = mat_erreur.dat.1061
        */
        StringBuilder stringBuilder = new StringBuilder();
        Instant end = getEndTime(type,timeInstant);
        String fileName = getFileName(type);


        /* Build String */
        if(type.equals("UU")) {

            for(int i = 0; i < fileData.data.size(); i++) {         
                    lineBuilderSpineAscii(timeInstant,end,fileData.data.get(i),stringBuilder,false);
            }

        } else if (type.equals("30") || type.equals("Q2") || type.equals("Q3") || type.equals("Q4")) {

            for(int i = 0; i < fileData.data.size(); i++) {
                    lineBuilderSpineAscii(timeInstant,end,fileData.data.get(i),stringBuilder,true);
            }

        } else {
            throw new RuntimeException("Invalid type, must be 30, Q1, Q2, Q4 or UU");       
        }

        
        /* Write file */
        File file = new File(outputDir + "/" + fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.append(stringBuilder);
        } catch (Exception e) {
            throw new RuntimeException("Could not write to file");
        }
    
    }
}
