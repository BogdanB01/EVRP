package uaic.fii.solver.ga;

import uaic.fii.solver.model.EVRPTWInstance;
import uaic.fii.solver.model.Solution;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractNeighbourhoodGeneration {

    protected EVRPTWInstance instance;
    protected static final Random RANDOM = new Random();

    public AbstractNeighbourhoodGeneration(EVRPTWInstance instance) {
        this.instance = instance;
    }

    protected abstract Optional<Solution> generate(Solution solution);

    protected List<int []> combinations(List<Integer> list) {
        return list.stream()
                .flatMap(a -> list.stream().map(b -> new int[] {a, b}))
                .collect(Collectors.toList());
    }

    protected List<Integer> range(int start, int end) {
        return Stream.iterate(start, n -> n + 1)
                .limit(end - start)
                .collect(Collectors.toList());
    }

    protected List<int []> cartesianProduct(List<Integer> a, List<Integer> b) {
        return a.stream()
                .flatMap(ai -> b.stream().map(bi -> new int[] {ai, bi}))
                .collect(Collectors.toList());
    }
}
