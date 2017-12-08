#
#
# Plot density profiles from simulation results.  Usage:
#     python plotdensity.py resultsfile.json
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

    i = 0
    for ds in densities:
        matplotlib.pyplot.plot(xs, ds, "o-", label=str(i))

        i += 1

    matplotlib.pyplot.legend()
    matplotlib.pyplot.gcf().canvas.set_window_title(title)

    matplotlib.pyplot.show()
