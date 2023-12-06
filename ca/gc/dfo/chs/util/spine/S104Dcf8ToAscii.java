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

    public static void runConversion(String timeString, String outputDir, String h5Path) {

        // Create Instant from ISO time string
        // Instant.parse will trow dateTime exception if in wrong format
        Instant timeInstant = Instant.parse(timeString);


        // Generate SpineData
        final SpineData fileData = s104ToSpineData(h5Path);

        // Write files to output directory 
        genSpineAsciiFiles(timeInstant, outputDir, fileData);
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
            while(HDFql.cursorNext() == HDFql.SUCCESS) {
                // eventDate
                Integer minutesAfterStart = 0;
                Instant eventDate = startTime.plus(minutesAfterStart, ChronoUnit.MINUTES);
                minutesAfterStart = minutesAfterStart + 3;
                // value
                Double value = HDFql.cursorGetDouble();
                HDFql.cursorNext();
                // Skip trend
                HDFql.cursorNext();
                // uncertainty
                Double uncertainty = HDFql.cursorGetDouble();
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

         Long numLong = Math.round(num*100);
         stringBuilder.append(String.format("%05d", numLong)+";");
    }

    private static void lineBuilderSpineAscii(Instant start,MeasurementCustomBundle mCBundle,StringBuilder stringBuilder, Boolean values) {
        /*
         * Append new line to existing StringBuilder from MeasurementCustomBundle containing Spine station data
         * Start appending values from the Instant defined in the "start" parameter
         * if values is set to False, will use uncertainty field instead of values
         */
        if (!(mCBundle.contains(start))) {
            throw new RuntimeException(start + " start time not indexed in source data");
        }
        /* Probably not the smartest way to do this loop */
        for ( final Instant instantIter: mCBundle.getInstantsKeySet() ) {
            if (!(instantIter.isBefore(start))) {
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



    static void genSpineAsciiFiles(Instant timeInstant,String outputDir, SpineData fileData) {
        /**
        * Generate the five ASCII files needed by SPINE
        * YYMMDDHH.one30.1061
        * YYMMDDHH.Q2.one.1061
        * YYMMDDHH.Q3.one.1061
        * YYMMDDHH.Q4.one.1061
        * mat_erreur.dat.1061
        */

        /* Test gen for mat_erreur.dat.1061 */
        /* Build String */
        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < fileData.data.size(); i++) {
            lineBuilderSpineAscii(timeInstant,fileData.data.get(i),stringBuilder,false);
        }

        /* Write file */
        File file = new File(outputDir + "/mat_erreur.dat.1061");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.append(stringBuilder);
        } catch (Exception e) {
            // TODO: handle exception
        }
    
    }
}
