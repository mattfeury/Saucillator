javac -cp lib/GlulogicMT.jar::lib/jsyn_beta_163.jar  -d . src/*.java
java -cp lib/GlulogicMT.jar::lib/jsyn_beta_163.jar -Djava.library.path=lib/ KaossTest