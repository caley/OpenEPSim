package latticesim;

import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;

public class TransitionsTest {
    private SimOptions loadTestOptions(String testName) throws Exception {
        return SimOptions.fromJSON(getClass().getResourceAsStream(testName));
    }

    /**
     * Load the lines of expected output as a *sorted* array of strings.
     */
    private String [] loadExpectedOutput(String outputName) {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(getClass().getResourceAsStream(outputName)));

        return reader.lines().toArray(String[]::new);
    }

    @Test
    public void asepInitialTransitions() throws Exception {
        SimOptions options = loadTestOptions("asep-test-options.json");
        String [] expectedTransitions = loadExpectedOutput(
            "asep-test-out1.json");
        Arrays.sort(expectedTransitions);

        LatticeConfiguration config = new LatticeConfiguration(
            options.L, options.nstates, options.initialState);
        config.setMaxWindow(options.getMaxWindow());

        Transitions transitions = new Transitions(config, options.transitions);

        String [] outputTransitions = transitions.transitionsJSON();
        Arrays.sort(outputTransitions);

        assertArrayEquals(outputTransitions, expectedTransitions);
    }

    @Test
    public void asepDoTransition() throws Exception {
        SimOptions options = loadTestOptions("asep-test-options.json");
        String [] expectedTransitions = loadExpectedOutput(
            "asep-test-out2.json");
        Arrays.sort(expectedTransitions);

        LatticeConfiguration config = new LatticeConfiguration(
            options.L, options.nstates, options.initialState);
        config.setMaxWindow(options.getMaxWindow());

        Transitions transitions = new Transitions(config, options.transitions);

        // Do this transition
        String thisTransition = "{\"rate\":1.0,\"c\":1,\"position\":8,\"window\":2,\"ispec\":1,\"fromC\":2}";

        transitions.forceTransition(thisTransition);

        String [] outputTransitions = transitions.transitionsJSON();
        Arrays.sort(outputTransitions);

        assertArrayEquals(outputTransitions, expectedTransitions);
    }

}
