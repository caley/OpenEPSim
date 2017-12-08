package latticesim;

import com.google.gson.Gson;

public class LatticeConfiguration {
    private final int [] lattice;
    private final int nstates;

    private int [] powers;

    /**
     * Creat a new LatticeConfiguration with specified length, number
     * of states, initial configuration.
     *
     * @param L the lattice length
     * @param nstates the number of states each site can be in
     * @param initialState initial state of the lattice.  If this
     *        is shorter than the lattice length, the remaining sites
     *        are set to zero.
     */
    public LatticeConfiguration(int L, int nstates, int [] initialState) {
        this.lattice = new int[L];
        this.nstates = nstates;

        int n = initialState.length > L ? L : initialState.length;

        for (int i = 0; i < n; i++) {
            lattice[i] = initialState[i];
        }
    }

    /**
     * Set the maximum window value used in the transitions specification.
     *
     * See SimOptions.getMaxWindow().  This is used to do some pre-calculation.
     */
    public void setMaxWindow(int window) {
        powers = new int[window];

        powers[0] = 1;

        for (int i = 1; i < window; i++) {
            powers[i] = nstates * powers[i - 1];
        }
    }

    /**
     * Get the current state at site i.
     */
    public int getConfigurationInt(int i) {
        return lattice[i];
    }

    /**
     * Get the current state at site i, ..., i + window - 1, represented
     * as an integer.
     */
    public int getConfigurationInt(int i, int window) {
        int c = lattice[i];

        for (int j = 1; j < window; j++) {
            c = nstates * c + lattice[i + j];
        }

        return c;
    }

    /**
     * Count the number of particles of species c currently on the lattice.
     */
    public int countSpecies(int c) {
        int nc = 0;

        for (int i = 0; i < lattice.length; i++) {
            if (lattice[i] == c) {
                nc++;
            }
        }

        return nc;
    }

    /**
     * Set the lattice configuration at sites i, ..., i + window - 1,
     * specified by integer c.
     *
     * @param c The local configuration.
     * @param i The position.
     * @param window The affected window.
     */
    public void setConfiguration(int c, int i, int window) {
        for (int j = 0; j < window; j++) {
            int p = powers[window - j - 1];

            lattice[i + j] = c / p;

            c = c % p;
        }
    }

    public int getLength() {
        return lattice.length;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < lattice.length; i++) {
            builder.append(lattice[i]);
        }

        return builder.toString();
    }

    public String toJson() {
        Gson gson = new Gson();

        return gson.toJson(this.lattice);
    }
}
