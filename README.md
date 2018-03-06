# OpenEPSim: Open Exclusion Process Simulator

[Input configuration](#input-configuration)




OpenEPS simulates exclusion processes on finite lattices, such as the [ASEP](https://arxiv.org/abs/cond-mat/0611701).  It handles multi-species models, arbitrary local interactions involving 1 or more neighbouring sites, and open boundary conditions (periodic boundaries are not supported).  Output can include time-averaged density profiles and currents, and distributions of the number of particles of each species.

To define a model and run a simulation, all that is needed is to specify the transition matrix, and a few other parameters.  The input and output is in [JSON](https://www.json.org/) format.  Full details of how to build and run OpenEPS, and the input and output formats are given below.  But first we give a quick example.

## Quick start guide

OpenEPS is written in Java.  Compiled versions are available [here](???), so that only the Java runtime (version 1.8???) is required to run it.  As an example, we will look at the configuration file [asep.json](samples/asep.json).

Assuming [something.jar](???) and [asep.json](???) have been saved to the current directory, we can run the simulation as
```shell
$ java -classpath something.jar openeps.OpenEPSimulator < asep.json > results.json
```
Let's now explain what that will do (or has done already if you're keen).

### Input configuration

The configuration file specifies the transition matrix for the open boundary ASEP, following the notation from [here](https://arxiv.org/abs/cond-mat/0609645).  We have taken boundary and bulk rates

> α = 0.29, β = 0.22, γ = 0.12, δ = 0.13, p = 1, q = 0.3.

This is the configuration file asep.json (with most of the comments left out):
```javascript
{
  "L": 200,

  "tSkip": 0,

  "tMax": 500000,

  "nstates": 2,

  "transitions": [
      // Left boundary
      {
          "window": 1,
          "from": 1,
          "to": 1,
          "count": true,
          "rates": [
              [0, 0.12],
              [0.29, 0]
          ]
      },

      // Bulk hopping
      {
          "window": 2,
          "from": 1,
          "to": -2,
          "rates": [
              [0, 0, 0, 0],
              [0, 0, 1, 0],
              [0, 0.3, 0, 0],
              [0, 0, 0, 0]
          ]
      },

      // Right boundary
      {
          "window": 1,
          "from": -1,
          "to": -1,
          "rates": [
              [0, 0.22],
              [0.13, 0]
          ]
      }
  ]
}
```

First
```
"L": 200
```
sets the number of sites to 200.  Sites are labelled left to right (1, 2, ..., L), or right to left (-1, -2, ..., -L).  So for this example, both '200' and '-1' would refer to the same site.

As each site of the ASEP has two states (empty or occupied), we have
```
"nstates": 2
```

We'll look at the bulk part of the transition matrix first
```
{
    "window": 2,
    "from": 1,
    "to": -2,
    "rates": [
        [0, 0, 0, 0],
        [0, 0, 1, 0],
        [0, 0.3, 0, 0],
        [0, 0, 0, 0]
    ]
}
```
`"window": 2` specifies that this matrix applies to two neighbouring sites.  The `"from"` and `"to"` entries specify that the matrix is to be applied at sites (1,2), (2,3), ..., (-2, -1) = (199, 200) (because L = 200 in this example).  `"rates"` gives the local transition matrix, in the usual basis (see TODO REF), and so must be of size nstates<sup>window</sup>.  A transition matrix should have negative entries on the diagonal, so that the columns sum to zero.  OpenEPS will insert the correct entries, so we can leave the diagonal entries as zero.

The left boundary matrix is specified by
```
{
    "window": 1,
    "from": 1,
    "to": 1,
    "count": true,
    "rates": [
        [0, 0.12],
        [0.29, 0]
    ]
}
```
It has `"window": 1` to specify that this matrix applies to a single site, and `"from"` and `"to"` are set to 1, so it is applied to the leftmost site.  `"rates"` specifies the injection and extraction rates (α = 0.29, and γ = 0.12).  `"count": true` tells OpenEPS to count the number of times each event occurs - this allows us to measure the current (a little like the current counting deformation of the transition matrix).

The right boundary is similar, but it has `"from"` and `"to"` set to -1, so that it is applied to the rightmost site.

Finally, `"tMax": 500000` specifies how long to run the simulation for (simulation time units).  `"tSkip": 0` tells OpenEPS to record statistics from the beginning of the run, but we could set it greater than zero to skip some some initial part.

### Results

In the above example, the simulation output is written to results.json.  We'll look at just an edited snippet of it:
```
{
  "options": {...},
  "results": {
    "density":[
    [0.6136124458963576,0.5697727375152507,0.5287006021014491,0.49270236944673473,0.45945597929829235,0.4315336312930617,0.407855282575572,0.3871513067377864,0.36742625435206605,0.3505489219535033,0.3351897765138729,0.3223181253438335,0.3123143017965667,0.303200252157777,0.2950027856021224,0.2889895897364133,0.28372286332798413,0.2795785808732041,0.2754119764129561,0.2715824417522548,0.2685563040889099,0.2655981483018454,0.2640067565888113,0.2611805373130109,0.26052807840176373,0.259105732519901,0.25806895004560776,...],
    [0.386387554103628,0.43022726248468085,0.4712993978984931,0.5072976305532152,0.540544020701648,0.5684663687068996,0.5921447174244232,0.6128486932622469,0.6325737456479313,0.6494510780465081,0.6648102234860775,0.6776818746561228,0.6876856982033551,0.6967997478421131,0.70499721439781,0.7110104102635096,0.7162771366718487,0.7204214191267312,0.7245880235869685,0.7284175582476695,0.7314436959110004,0.7344018516980817,...]
    ],
    "counts":[
      [[0,89000],
       [23313,0]], null, null
    ]
  },
  ...
}
```
The first entry `"options"` contains a complete copy of the input configuration (in this case asep.json).  This is to ease keeping track of simulation runs.

The actual results come under `"results"`.  Shown above are the per-site densities for species 0 and 1, i.e. the fraction of time each site is empty or occupied, respectively, so that pairwise the entries add to 1.  Plotted, these density profiles look like this: TODO.

The `"counts"` entry records injections (23313) and extractions (89000) ??? negative??? at site 1.  The two 'null' elements are for the bulk and right boundary transitions.

TODO fix "counts" bug (?) and compare to asymptotic values.

## Build and run instructions
OpenEPS
## Input format

## Output format


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
