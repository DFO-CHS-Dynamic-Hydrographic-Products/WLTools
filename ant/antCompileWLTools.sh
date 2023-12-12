#!/bin/bash

# --- Modify according to the code location context.
javaCodeBaseMainDir=../
javaClassesMainDestDir=${javaCodeBaseMainDir}/lib

# --- force recompilation of the main program source file
#     (Still need to implement dependencies for ant)
rm -f ${javaCodeBaseMainDir}/bin/*.class

export CLASSPATH=${javaClassesMainDestDir}:${javaCodeBaseMainDir}:${CLASSPATH}


ant -buildfile ${javaCodeBaseMainDir}/ant/antCompHDFql.xml
if [ ${?} -ne 0 ];
then
  echo "ant -buildfile ${javaCodeBaseMainDir}/ant/antCompHDFql.xml FAILED!!"
  exit 1
fi

ant -buildfile ${javaCodeBaseMainDir}/ant/antCompLib.xml

if [ ${?} -ne 0 ];
then
  echo "ant -buildfile ${javaCodeBaseMainDir}/ant/antCompLib.xml FAILED!!"
  exit 1
fi

ant -buildfile ${javaCodeBaseMainDir}/ant/antCompMain.xml

if [ ${?} -ne 0 ];
then
  echo "ant -buildfile ${javaCodeBaseMainDir}/ant/antCompMain.xml FAILED!!"
  exit 1
fi
