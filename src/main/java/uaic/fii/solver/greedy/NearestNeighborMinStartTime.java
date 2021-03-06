package uaic.fii.solver.greedy;

import uaic.fii.model.Customer;
import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class NearestNeighborMinStartTime implements GiantRouteConstructionStrategy {

    private final EVRPTWInstance instance;
    private final List<Customer> customers;

    private static final double TOLERANCE = 1.8;

    public NearestNeighborMinStartTime(EVRPTWInstance instance) {
        this.instance = instance;
        this.customers = instance.getCustomers();
    }

    @Override
    public List<Node> construct() {
        List<Node> giantRoute = new ArrayList<>();
        AtomicReference<Node> lastPosition = new AtomicReference<>(instance.getDepot());

        while (giantRoute.size() != customers.size()) {
            List<Node> possibleSuccessors = customers.stream()
                    .filter(n -> !giantRoute.contains(n))
                    .collect(Collectors.toList());
            double minimumDistance = possibleSuccessors.stream()
                    .mapToDouble(n -> instance.getTravelDistance(n, lastPosition.get()))
                    .min().orElseThrow(NoSuchElementException::new);
            possibleSuccessors.removeIf(n -> instance.getTravelDistance(n, lastPosition.get()) > minimumDistance * TOLERANCE);
            possibleSuccessors.sort(Comparator.comparing(n -> instance.getTravelDistance(n, lastPosition.get())));

            Node successor = possibleSuccessors.stream()
                    .min(Comparator.comparing(n -> instance.getTimeWindow(n).getStart()))
                    .orElseThrow(NoSuchElementException::new);
            giantRoute.add(successor);
            lastPosition.set(successor);
        }
        return giantRoute;
    }
}
