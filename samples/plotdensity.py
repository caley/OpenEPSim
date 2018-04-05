#
# Sample script showing how to extract density data from output JSON
# file, and plot it.
#
# Script uses Python with Matplotlib https://matplotlib.org/
#
# Usage:
#
#     python plotdensity.py resultsfile.json [species1 species2...]
#
# e.g. for asep-results.json, to plot species just '1'
#
#     python plotdensity.py asep-results.json 1
#
# or to plot all species
#
#     python plotdensity.py asep-results.json
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

    densities = results['results']['density']

    xs = range(1, len(densities[1]) + 1)

    if len(sys.argv) > 2:
        showspecies = map(int, sys.argv[2:])
    else:
        showspecies = range(0, len(densities))

    for i in showspecies:
        matplotlib.pyplot.plot(xs, densities[i], "o-", label=str(i))

    matplotlib.pyplot.legend()
    matplotlib.pyplot.gcf().canvas.set_window_title(title)

    matplotlib.pyplot.show()
