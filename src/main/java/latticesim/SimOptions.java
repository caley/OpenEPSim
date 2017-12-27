package latticesim;

import java.io.InputStream;
import java.io.InputStreamReader;

import com.google.gson.Gson;

public class SimOptions {
    /**
     *  Represents a local transition specification, essentially a local
     *  transition matrix.
     */
    public class LocalTransitionSpec {
        /**
         * How many sites the transition operates on.
         */
        public final int window;

        /**
         *  The first site to apply this to.
         */
        public final int from;

        /**
         * The last site to apply this to.
         */
        public final int to;

        /**
         * Count the number of times each transition occurs
         * Note: currently this will be done per LocalTransitionSpec,
         * i.e. aggregated over all sites specified by from, to
         */
        public final boolean count;

        /**
         * The transition rates.  This is the local transition matrix.
         * The exit rates (diagonal elements) don't need to be set.
         */
        public final double [][] rates;

        private LocalTransitionSpec() {
            window = 0;
            from = 0;
            to = 0;
            count = false;
            rates = null;
        }

        /**
         * XXX These are some rough, incomplete checks that the
         * entries are valid.
         */
        private boolean isValid(int L, int nstates) {
            int absFrom = Math.abs(from);
            int absTo = Math.abs(to);

            // Check the ranges
            if (absFrom < 1 || absFrom > L) {
                System.err.println("transitions: from invalid");
                return false;
            }

            if (absTo < 1 || absTo > L) {
                System.err.println("transitions: to invalid");
                return false;
            }

            // Check the matrix dimensions
            // TODO check this is a power of the number of states
            // and is consistent with window
            if (rates == null) {
                System.err.println("transitions: rates null");
                return false;
            }

            int matrixSize = rates.length;

            if (rates.length != (int) Math.pow(nstates, window)) {
                System.err.println("transitions: rates size invalid");

                return false;
            }

            for (int i = 0; i < matrixSize; i++) {
                if (rates[i] == null || rates[i].length != matrixSize) {
                    System.err.println("transitions: rates[" + i + "] invalid");
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Get the maximum window value appearing in the list of transitions.
     */
    public int getMaxWindow() {
        int max = 0;

        if (transitions != null) {
            for (int i = 0; i < transitions.length; i++) {
                if (transitions[i].window > max) {
                    max = transitions[i].window;
                }
            }
        }

        return max;
    }

    /**
     *  The lattice length.
     */
    public final int L;

    /**
     *  The number of states per site.
     */
    public final int nstates;

    /**
     *  The local transitions.
     */
    public final LocalTransitionSpec [] transitions;

    /**
     *  The initial lattice configuration.
     */
    public final int [] initialState;

    /**
     *  Random seed: unset or 0 to not set a seed.
     */
    public final long seed;

    /**
     *  Run in verbose mode.
     */
    public final int verbose;

    /**
     *  Simulation length.
     */
    public final double tMax;

    /**
     *  Time to skip before recording statistics.
     */
    public final double tSkip;

    public SimOptions() {
        L = 0;
        nstates = 0;
        seed = 0;
        verbose = 0;
        tMax = 0;
        tSkip = 0;

        transitions = null;
        initialState = new int[0];
    }

    public static SimOptions fromJSON(String json) throws Exception {
        Gson gson = new Gson();

        SimOptions options = gson.fromJson(json, SimOptions.class);

        options.checkValid();

        return options;
    }

    public static SimOptions fromJSON(InputStream json) throws Exception {
        InputStreamReader reader = new InputStreamReader(json);

        Gson gson = new Gson();

        SimOptions options = gson.fromJson(reader, SimOptions.class);

        options.checkValid();

        return options;
    }

    /**
     * Check the options are valid
     */
    private void checkValid() throws Exception {
        if (L <= 0 || nstates <= 0) {
            throw new Exception("L, nstates must be greater than zero.");
        }

        if (transitions == null) {
            throw new Exception("Some transitions must be specified");
        }

        for (int i = 0; i < transitions.length; i++) {
            if (!transitions[i].isValid(L, nstates)) {
                throw new Exception("Invalid transitions: " + i);
            }
        }
    }

    public String toString() {
        Gson gson = new Gson();

        return gson.toJson(this);
    }
}
