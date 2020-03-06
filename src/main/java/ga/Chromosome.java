package ga;

import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Chromosome {
    private EVRPTWInstance instance;

    private List<Route> genes;

    private static final int PUNISHMENT_VALUE = 10;
    private static final Random RANDOM = new Random();

    public Chromosome(EVRPTWInstance instance) {
        this.instance = instance;
    }

    private double getObjective() {
        double value = 0;
        for (Route route : genes) {
            value += route.getTotalDistance();
        }

        for (Route route : genes) {
            for (Customer customer : instance.getCustomers()) {
                value += getPunishValue(route, customer);
            }
        }

        for (Route route : genes) {
            value += Double.MAX_VALUE * Math.max(route.getPayloadCapacity() - instance.getVehicleCapacity(), 0);
        }

        for (Route route : genes) {
            for (Node node : route.getNodes()) {
                value += Double.MAX_VALUE * Math.max(route.calculateRemainingTankCapacity(node), 0);
            }
        }

        return value;
    }

    private double getPunishValue(Route route, Customer customer) {
        double time = route.calculateArrivingTimeAtCustomer(customer);
        if (time == 0) return 0;

        Node.TimeWindow timeWindow = instance.getTimeWindow(customer);
        double firstTerm = PUNISHMENT_VALUE * Math.max(timeWindow.getStart() - time, 0);
        double secondTerm = PUNISHMENT_VALUE * Math.max(time - timeWindow.getEnd(), 0);

        return firstTerm + secondTerm;
    }

    // swap mutation
    public void mutate() {
        for (Route route : genes) {
            List<Node> nodes = route.getNodes();
            int first = RANDOM.nextInt(nodes.size());
            int second = RANDOM.nextInt(nodes.size());
            while (second == first) {
                second = RANDOM.nextInt(nodes.size());
            }
            Collections.swap(nodes, first, second);
        }
    }

    public double getFitness() {
        return 1 / getObjective();
    }

    /* initialization ensures the customer demand of each node is satisfied
     and do not exceed the loading capacity of the electric vehicle */
    public void initRandomChromosome() {
        this.genes = new ArrayList<>();

        List<Customer> customers = instance.getCustomers();
        List<RechargingStation> rechargingStations = instance.getRechargingStations();
        List<Node> nodes = Stream.of(customers, rechargingStations).flatMap(List::stream).collect(Collectors.toList());
        Collections.shuffle(nodes);

        double currentCapacity = 0;
        Route route = new Route(instance);
        for (int i = 0; i < nodes.size() - 1; i++) {
            currentCapacity += instance.getDemand(nodes.get(i));
            double nextCapacity = currentCapacity + instance.getDemand(nodes.get(i));
            if (currentCapacity <= instance.getVehicleCapacity()) {
                route.addNode(nodes.get(i));
            }
            if (nextCapacity > instance.getVehicleCapacity()) {
                route.addNode(instance.getDepot());
                genes.add(route);
                route = new Route(instance);
                currentCapacity = 0;
            }
        }
    }

    public List<Route> getGenes() {
        return genes;
    }

    public void removeCustomer(Customer customer) {
        genes.forEach(e -> e.getNodes().remove(customer));
    }

    public Route getBestRouteInGene() {
        int bestScore = Integer.MIN_VALUE;
        Route bestRoute = null;
        for (Route route : genes) {
            int score = route.evalRoute();
            if (score > bestScore) {
                bestScore = score;
                bestRoute = route;
            }
        }
        return bestRoute;
    }



}
