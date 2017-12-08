package latticesim;

import com.google.gson.Gson;

/**
 * Class to collect various time-averagedstatistics from a simulation run,
 * primarily the site-wise per-species densities.
 */
public class DensityStatistics {
    private class DensityResults {
        double tTotal;
        double [][] density;
        double [][] speciesDensity;
        int [][][] counts;

        DensityResults() {
            density = new double[nstates][L];
            speciesDensity = new double[nstates][L + 1];
        }
    }

    private final int L;
    private final int nstates;

    private double tTotal;
    private double [][] tOccupied;
    private double [][] tSpeciesOccupation;

    private int [][][] counts;

    public DensityStatistics(
        int L, int nstates, SimOptions.LocalTransitionSpec [] specs
    ) {
        this.L = L;
        this.nstates = nstates;

        this.tOccupied = new double[nstates][L];

        this.tSpeciesOccupation = new double[nstates][L + 1];

        this.counts = new int[specs.length][][];

        for (int i = 0; i < specs.length; i++) {
            if (specs[i].count) {
                int d = specs[i].rates.length;
                this.counts[i] = new int[d][d];
            }
        }
    }

    /**
     *  Update statistics to count time dt spent in configuration
     *  config.
     *
     *  @param config The lattice configuration.
     *  @param dt Time spent in this configuration.
     */
    public void update(LatticeConfiguration config, double dt) {
        for (int i = 0; i < L; i++) {
            tOccupied[config.getConfigurationInt(i)][i] += dt;
        }

        for (int c = 0; c < nstates; c++) {
            tSpeciesOccupation[c][config.countSpecies(c)] += dt;
        }

        tTotal += dt;
    }

    /**
     * Count occurences of given of given local transition.  For example,
     * this can be used to record the time integrated current.
     *
     * @param spec Index into SimOptions.transitions array.
     * @param toC The configuration we step into.
     * @param fromC The configuration we leave.
     */
    public void countTransition(int spec, int toC, int fromC) {
        if (this.counts[spec] != null) {
            this.counts[spec][toC][fromC]++;
        }
    }

    /**
     * Return a string summary of the collected statistics
     * in JSON forma.
     */
    public String summary() {
        DensityResults results = new DensityResults();

        results.tTotal = tTotal;

        if (tTotal > 0) {
            for (int j = 0; j < nstates; j++) {
                for (int i = 0; i < L; i++) {
                    results.density[j][i] = tOccupied[j][i] / tTotal;
                }

                for (int i = 0; i < L + 1; i++) {
                    results.speciesDensity[j][i]
                        = tSpeciesOccupation[j][i] / tTotal;
                }
            }
        }

        results.counts = counts;

        Gson gson = new Gson();

        return gson.toJson(results);
    }
}
