package uaic.fii.solver.ga.search.neighbourhood;

import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Solution;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractNeighbourhoodGeneration {

    protected EVRPTWInstance instance;

    public AbstractNeighbourhoodGeneration(EVRPTWInstance instance) {
        this.instance = instance;
    }

    public abstract List<Solution> generate(Solution solution);

}
