package openepsim;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 *  Compare simulation results for the ASEP to exact analytical
 *  results.
 *
 *  See https://arxiv.org/abs/cond-mat/0312457 for details.
 */
public class ASEPPhasesTest {
    private SimOptions loadTestOptions(String testName) throws Exception {
        return SimOptions.fromJSON(getClass().getResourceAsStream(testName));
    }

    private void checkSimulationOutput(
        String optionFile,
        double expectedDensity,
        double expectedCurrent,
        double tolerance
    ) throws Exception {
        SimOptions options = loadTestOptions(optionFile);

        OpenEPSimulation sim = new OpenEPSimulation(options.seed);

        DensityStatistics stats
        =
        new DensityStatistics(options.L, options.nstates, options.transitions);

        LatticeConfiguration config = sim.simulate(options, stats);

        // Check current
        double simCurrent = (
            stats.getTransitionCount(0, 1, 0)
            -
            stats.getTransitionCount(0, 0, 1)
        ) / stats.getTotalTime();

        assertEquals(expectedCurrent, simCurrent, tolerance);

        // Check density
        double [] densities = stats.getDensityProfile(1);

        double simDensity = 0;
        for (int i = 0; i < densities.length; i++) {
            simDensity += densities[i];
        }

        simDensity /= densities.length;

        assertEquals(expectedDensity, simDensity, tolerance);
    }

    @Test
    public void checkLDPhase() throws Exception{
        String optionFile = "asepLD-options.json";
        double expectedDensity = 0.250;
        double expectedCurrent = 0.169;
        double tolerance = 0.01;

        checkSimulationOutput(
            optionFile, expectedDensity, expectedCurrent, tolerance);
    }

    @Test
    public void checkHDPhase() throws Exception{
        String optionFile = "asepHD-options.json";
        double expectedDensity = 0.800;
        double expectedCurrent = 0.144;
        double tolerance = 0.01;

        checkSimulationOutput(
            optionFile, expectedDensity, expectedCurrent, tolerance);
    }

    @Test
    public void checkMCPhase() throws Exception{
        String optionFile = "asepMC-options.json";
        double expectedDensity = 0.500;
        double expectedCurrent = 0.225;
        double tolerance = 0.01;

        checkSimulationOutput(
            optionFile, expectedDensity, expectedCurrent, tolerance);
    }
}
