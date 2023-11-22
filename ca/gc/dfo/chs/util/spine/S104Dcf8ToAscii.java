package ca.gc.dfo.chs.util.spine;
import as.hdfql.HDFql;
import ca.gc.dfo.chs.wltools.util.MeasurementCustom;
import ca.gc.dfo.chs.wltools.util.MeasurementCustomBundle;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

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
        final String startTimeStr = HDFql.cursorGetChar();
        final Instant startTime =Instant.parse(startTimeStr);

        final String hdfqlSelectEndTime = "SELECT FROM WaterLevel/WaterLevel.01/dateTimeOfLastRecord";
        HDFql.execute(hdfqlSelectEndTime);
        HDFql.cursorFirst();
        final String endTimeStr = HDFql.cursorGetChar();
        final Instant endTime =Instant.parse(endTimeStr);

        

        // Read data group and return MeasurementCustomBundle List
        
        // Get Number of Groups
        String hdfqlGetGroupsNo = "SHOW WaterLevel/WaterLevel.01/ LIKE \"Group\"";
        HDFql.execute(hdfqlGetGroupsNo);
        Integer numGroup = HDFql.cursorGetCount();

        

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
        
        //Populate spine data object
        spineData.data = data;
        spineData.startTime = startTime;
        spineData.endTime = endTime;

        return spineData;

    }

    static void genSpineAsciiFiles () {
        /**
        * Generate the five ASCII files needed by SPINE
        * YYMMDDHH.one30.1061
        * YYMMDDHH.Q2.one.1061
        * YYMMDDHH.Q3.one.1061
        * YYMMDDHH.Q4.one.1061
        * mat_erreur.dat.1061
        */
    }
    
}
