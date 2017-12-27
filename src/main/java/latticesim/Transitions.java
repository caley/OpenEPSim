package latticesim;

import com.google.gson.Gson;

public class Transitions {
    private class Transition {
        // Linked list, the transient is just to exclude this from the
        // gson toJson() output
        transient Transition next;

        double rate;
        int c;
        int position;
        int window;

        // These are needed to count event stats
        int ispec;
        int fromC;

        Transition(double rate, int c, int position, int window,
             int ispec, int fromC) {

            this.rate = rate;
            this.c = c;
            this.position = position;
            this.window = window;
            this.ispec = ispec;
            this.fromC = fromC;
        }

        public String toString() {
            Gson gson = new Gson();

            return gson.toJson(this);
        }
    }

    private LatticeConfiguration config;

    private SimOptions.LocalTransitionSpec [] specs;
    private int froms[];
    private int tos[];

    private Transition transitions;
    private Transition spares;

    private double exitRate;

    /**
     * Construct a Transitions object, which will calculate the
     * allowed transitions for the given LatticeConfiguration according
     * to the list of LocalTransitionSpec objects.
     */
    public Transitions(
        LatticeConfiguration config,
        SimOptions.LocalTransitionSpec [] specs
    ) {
        this.config = config;

        this.specs = specs;

        // Copy the ranges, but as absolute indices, and zero indexed
        this.froms = new int[specs.length];
        this.tos = new int[specs.length];

        int L = config.getLength();

        for (int i = 0; i < specs.length; i++) {
            froms[i] = specs[i].from;

            if (froms[i] < 0) {
                // e.g. for negative indices, last site is -1 -> L - 1
                froms[i] += L;
            } else {
                // For positive indices, first site is 1 -> 0
                froms[i] -= 1;
            }

            tos[i] = specs[i].to;

            if (tos[i] < 0) {
                tos[i] += L;
            } else {
                tos[i] -= 1;
            }
        }

        // Now calculate the initial transitions
        for (int i = 0; i < specs.length; i++) {
            SimOptions.LocalTransitionSpec spec = specs[i];

            for (int j = froms[i]; j <= tos[i]; j++) {
                int c = config.getConfigurationInt(j, spec.window);

                for (int n = 0; n < spec.rates.length; n++) {
                    double rate = spec.rates[n][c];

                    if (rate > 0) {
                        exitRate += rate;

                        addTransition(rate, n, j, spec.window, i, c);
                    }
                }
            }
        }
    }

    /**
     * Add a Transition object to the list.
     */
    private void addTransition(double rate, int c, int position, int window,
            int ispec, int fromC) {
        Transition toAdd;

        if (spares == null) {
            toAdd = new Transition(
                rate, c, position, window, ispec, fromC
            );
        } else {
            toAdd = spares;
            spares = toAdd.next;
            // toAdd.next is reset below

            toAdd.rate = rate;
            toAdd.c = c;
            toAdd.position = position;
            toAdd.window = window;
            toAdd.ispec = ispec;
            toAdd.fromC = fromC;
        }

        // Add it to the front: quicker and the order doesn't matter
        toAdd.next = transitions;
        transitions = toAdd;
    }

    /**
     *  Recalculate transitions depending on sites
     *      position, ..., position + window -1
     *
     *  Call clearDirty() before this.
     */
    private void recalculate(int position, int window) {
        for (int i = 0; i < specs.length; i++) {
            SimOptions.LocalTransitionSpec spec = specs[i];

            int jstart = Math.max(froms[i], position - spec.window + 1);
            int jend = Math.min(tos[i], position + window - 1);

            for (int j = jstart; j <= jend; j++) {
                int c = config.getConfigurationInt(j, spec.window);

                for (int n = 0; n < spec.rates.length; n++) {
                    double rate = spec.rates[n][c];

                    if (rate > 0) {
                        exitRate += rate;

                        addTransition(rate, n, j, spec.window, i, c);
                    }
                }
            }
        }
    }

    /**
     * Get the total exit rate.
     */
    public double getExitRate() {
        return exitRate;
    }

    /**
     *  Randomly select then perform a transition according to r:
     *  update the lattice configuration, then recalculate affected
     *  transitions.
     *
     *  @param r a random double value in range [0, this.getExitRate()).
     */
    public void doTransition(double r) {
        // Find the first transition such that the partial sum
        // of rates is greater than r
        Transition curr = transitions;
        double rateSum = curr.rate;

        while (r >= rateSum) {
            curr = curr.next;
            rateSum += curr.rate;
        }

        config.setConfiguration(curr.c, curr.position, curr.window);

        // Remove affected transitions
        clearDirty(curr.position, curr.window);

        // Then recalculate them
        recalculate(curr.position, curr.window);
    }

    /**
     *  Like the above but record statistics.
     */
    public void doTransition(double r, DensityStatistics stats) {
        // Find the first transition such that the partial sum
        // of rates is greater than r
        Transition curr = transitions;
        double rateSum = curr.rate;

        while (r >= rateSum) {
            curr = curr.next;
            rateSum += curr.rate;
        }

        config.setConfiguration(curr.c, curr.position, curr.window);

        // Remove affected transitions
        clearDirty(curr.position, curr.window);

        // Then recalculate them
        recalculate(curr.position, curr.window);

        stats.countTransition(curr.ispec, curr.c, curr.fromC);
    }

    /**
     *  Clear transitions depending on sites
     *      position, ..., position + window -1
     */
    private void clearDirty(int position, int window) {
        int i1 = position;
        int i2 = position + window - 1;

        // Clear out start of the list
        while (transitions != null) {
            if (
                transitions.position > i2
                ||
                transitions.position + transitions.window - 1 < i1
            ) {
                break;
            }

            exitRate -= transitions.rate;

            // Remove and keep old object as spare
            Transition toRemove = transitions;
            transitions = transitions.next;
            toRemove.next = spares;
            spares = toRemove;
        }

        if (transitions == null) {
            return;
        }

        Transition lastGood = transitions;

        while (lastGood.next != null) {
            Transition curr = lastGood.next;

            if (curr.position > i2 || curr.position + curr.window - 1 < i1) {
                lastGood = curr;
            } else {
                exitRate -= curr.rate;

                // Remove and keep old object as spare
                Transition toRemove = lastGood.next;
                lastGood.next = curr.next;
                toRemove.next = spares;
                spares = toRemove;
            }
        }
    }

    /**
     * For debugging: dump the list of transitions.
     */
    void dump() {
        Transition curr = transitions;

        if (curr == null) {
            System.out.println("<null>");

            return;
        }

        while (curr != null) {
            System.out.println(curr);
            curr = curr.next;
        }
    }

    /**
     *  Return array of the current transition list with information
     *  about each transition as a JSON string.
     *
     *  This method is meant for testing/debugging.
     */
    String [] transitionsJSON() {
        // Count the number of transitions
        Transition curr = transitions;
        int numTransitions = 0;

        while (curr != null) {
            numTransitions++;

            curr = curr.next;
        }

        String [] jsonTransitions = new String[numTransitions];

        curr = transitions;
        int i = 0;

        while (curr != null) {
            jsonTransitions[i] = curr.toString();

            curr = curr.next;
            i++;
        }

        return jsonTransitions;
    }

    /**
     * XXX This method really is only meant for testing.
     *
     * Force a transition to occur, specified by a string as returned
     * by transitionsJson().
     */
    void forceTransition(String transitionSpec) {
        Transition curr = transitions;

        while (curr != null && !transitionSpec.equals(curr.toString())) {
            curr = curr.next;
        }

        // Only if we found a matching transition
        if (curr != null) {
            config.setConfiguration(curr.c, curr.position, curr.window);

            // Remove affected transitions
            clearDirty(curr.position, curr.window);

            // Then recalculate them
            recalculate(curr.position, curr.window);
        }
    }
}
