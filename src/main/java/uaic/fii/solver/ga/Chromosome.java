package uaic.fii.solver.ga;

import uaic.fii.solver.construction.BeasleyHeuristic;
import uaic.fii.solver.model.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Chromosome implements Comparable<Chromosome> {

    private Node[] nodes;
    private Random random;
    private EVRPTWInstance instance;
    private double distance = -1;

    public Chromosome(EVRPTWInstance instance, Node[] nodes) {
        this.instance = instance;
        this.nodes = nodes.clone();
    }

    public Chromosome(EVRPTWInstance instance, Node[] nodes, Random random) {
        this(instance, nodes);
        this.random = random;
        shuffle();
    }

    public Node[] getArray () {
        return nodes.clone();
    }

    private void shuffle() {
        for (int i = 0; i < nodes.length; i++) {
            swap(i, random.nextInt(nodes.length));
        }
    }

    private void swap(int i, int j) {
        Node temp = nodes[i];
        nodes[i] = nodes[j];
        nodes[j] = temp;
    }

    public double getDistance() {
        if (distance != -1) {
            return distance;
        }
        List<Node> giantRoute = Stream.of(nodes).collect(Collectors.toList());
        BeasleyHeuristic split = new BeasleyHeuristic(instance, giantRoute);
        List<Route> routes = split.solve();

        distance = 0;
        for (Route route : routes) {
            distance += route.getTotalDistance();
        }
        return distance;
    }

    @Override
    public int compareTo(Chromosome chromosome) {
        return Double.compare(getDistance(), chromosome.getDistance());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Chromosome that = (Chromosome) o;
        return Objects.equals(nodes, that.nodes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes);
    }
}
