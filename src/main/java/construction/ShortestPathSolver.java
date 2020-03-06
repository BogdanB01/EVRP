package construction;

import org.javatuples.Pair;

import java.util.*;

public  class ShortestPathSolver {

    private double[][] costs;
    private List<Node> unvisitedNodes;
    private Map<Integer, Node> nodes;

    public ShortestPathSolver(double[][] costs) {
        this.costs = costs;
        nodes = new HashMap<>();
        unvisitedNodes = new ArrayList<>();

        for (int i = 0; i < costs.length; i++) {
            Node node = new Node(i);
            nodes.put(i, node);
            unvisitedNodes.add(node);
        }

        unvisitedNodes.remove(nodes.get(0));

        for (int i = 0; i < costs.length; i++) {
            Node node = nodes.get(i);
            for (int j = i; j < costs.length; j++) {
                if (costs[i][j] < Double.MAX_VALUE) {
                    node.neighbors.add(nodes.get(j));
                    if (i == 0) {
                        nodes.get(j).cost = costs[i][j];
                        nodes.get(j).shortestRoute.add(Pair.with(i, j));
                    }
                }
            }
        }
    }

    public List<Pair<Integer, Integer>> solve() {
        while (unvisitedNodes.size() > 0) {
            Node closestNode = this.unvisitedNodes.stream()
                    .min(Comparator.comparing(e -> e.cost))
                    .orElseThrow(NoSuchElementException::new);

            unvisitedNodes.remove(closestNode);
            for (Node n : closestNode.neighbors) {
                double newCost;
                if (n.index != closestNode.index) {
                    newCost = closestNode.cost + costs[closestNode.index][n.index];
                } else {
                    newCost = closestNode.cost;
                }
                if (newCost < n.cost) {
                    n.cost = newCost;
                    n.shortestRoute = new ArrayList<>(closestNode.shortestRoute);
                    n.shortestRoute.add(Pair.with(closestNode.index, n.index));
                }
            }
        }
        return nodes.get(costs.length - 1).shortestRoute;
    }


    private static class Node {
        private int index;
        private List<Node> neighbors;
        private double cost = Double.MAX_VALUE;
        private List<Pair<Integer, Integer>> shortestRoute;

        Node(int index) {
            this.index = index;
            this.neighbors = new ArrayList<>();
            this.shortestRoute = new ArrayList<>();
        }
    }
}