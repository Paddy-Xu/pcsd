#!/bin/bash
LIB_FILES=./
for f in `find . -name "*.jar"`
do
  LIB_FILES=$LIB_FILES:$f
done

javac `find . -name "*.java"` -cp $LIB_FILES
