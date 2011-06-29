#!/bin/bash
javac -cp lib/GlulogicMT.jar::lib/jsyn_beta_163.jar::lib/libSmsWrapper.jnilib  -d . src/*.java
java -cp lib/GlulogicMT.jar::lib/jsyn_beta_163.jar::lib/libSmsWrapper.jnilib -Djava.library.path=lib/ Saucillator
