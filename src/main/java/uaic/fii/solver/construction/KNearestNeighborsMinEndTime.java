package uaic.fii.solver.construction;

import uaic.fii.solver.model.Customer;
import uaic.fii.solver.model.EVRPTWInstance;
import uaic.fii.solver.model.Node;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class KNearestNeighborsMinEndTime implements GiantRouteConstructionStrategy {
    private EVRPTWInstance instance;
    private static final int K = 3;
    private List<Customer> customers;

    public KNearestNeighborsMinEndTime(EVRPTWInstance instance) {
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
                    .sorted(Comparator.comparing(n -> instance.getTravelDistance(n, lastPosition.get())))
                    .collect(Collectors.toList());
            Node successor = possibleSuccessors.stream()
                    .limit(K)
                    .min(Comparator.comparing(n -> instance.getTimeWindow(n).getEnd()))
                    .orElseThrow(NoSuchElementException::new);
            giantRoute.add(successor);
            lastPosition.set(successor);
        }

        return giantRoute;
    }
}
