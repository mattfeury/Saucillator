javac -cp lib\GlulogitMT.jar;lib\jsyn_beta_163.jar;lib/libSmsWrapper.jnilib -classpath .;lib\jsyn_beta_163.jar;lib\GlulogicMT.jar;lib\libGlulogicMT.jnilin;lib/libSmsWrapper.jnilib -d . src/*.java

java -cp lib\GlulogitMT.jar;lib\jsyn_beta_163.jar;lib/libSmsWrapper.jnilib -classpath .;lib\jsyn_beta_163.jar;lib\GlulogicMT.jar;lib\libGlulogicMT.jnilin;lib/libSmsWrapper.jnilib -Djava.library.path=.;lib\GlulogicMT.jar;lib\libGlulogicMT.jnilib;lib/libSmsWrapper.jnilib KaossTest
