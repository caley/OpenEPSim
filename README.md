# latticesim
General simulation code for ASEP like models on 1D lattices.

## Very brief instructions
Download the jars for [gson](https://github.com/google/gson) and [Apache Common Maths](http://commons.apache.org/proper/commons-math/) and update env.sh to point to them.

Now compiles with [Maven](https://maven.apache.org/)
```
mvn compile
```

To run the ASEP simulation from the samples folder
```
./run.sh < samples/asep.json > asep.out.json
```

Python script to plot resulting density profiles (uses matplotlib)
```
python plotdensity.py asep.out.json
```
