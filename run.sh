OPENEPSIM_DIR=`dirname $0`

CLASSPATH="$OPENEPSIM_DIR/target/classes:$OPENEPSIM_DIR/lib/gson-2.8.2.jar:$OPENEPSIM_DIR/lib/commons-math3-3.6.1.jar"

java -classpath "$CLASSPATH" openepsim.OpenEPSimulation
