#
# Sample script showing how to extract species density data from output JSON
# file, and plot it.
#
# Script uses Python with Matplotlib https://matplotlib.org/
#
# Usage:
#
#     python plotspeciesdensity.py resultsfile.json [species1 species2...]
#
# e.g. for asep-results.json, to plot species just '1'
#
#     python plotspeciesdensity.py asep-results.json 1
#
# or to plot all species
#
#     python plotspeciesdensity.py asep-results.json
#


import json
import matplotlib.pyplot
import os.path
import sys

if __name__ == "__main__":
    resultsfile = sys.argv[1]

    fp = open(resultsfile)
    results = json.load(fp)
    fp.close()

    title = os.path.basename(resultsfile)

    specdensities = results['results']['speciesDensity']

    xs = range(0, len(specdensities[0]))

    if len(sys.argv) > 2:
        showspecies = map(int, sys.argv[2:])
    else:
        showspecies = range(0, len(specdensities))

    for i in showspecies:
        matplotlib.pyplot.plot(xs, specdensities[i], "o-", label=str(i))

    matplotlib.pyplot.legend()
    matplotlib.pyplot.gcf().canvas.set_window_title(title)

    matplotlib.pyplot.show()
