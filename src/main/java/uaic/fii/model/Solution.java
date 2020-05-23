package uaic.fii.model;

import uaic.fii.util.Algorithm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Solution {

    private List<Route> routes;
    private EVRPTWInstance instance;
    private Algorithm algorithm;
    private Double cost;
    private Double timeTaken;

    public Solution(List<Route> routes) {
        this.routes = new ArrayList<>(routes);
    }

    public Solution(EVRPTWInstance instance, Algorithm algorithm, List<Route> routes) {
        this.instance = instance;
        this.algorithm = algorithm;
        this.routes = routes;
    }

    public Solution(EVRPTWInstance instance, Algorithm algorithm, List<Route> routes, Double cost, Double timeTaken) {
        this(instance, algorithm, routes);
        this.cost = cost;
        this.timeTaken = timeTaken;
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
    }

    public EVRPTWInstance getInstance() {
        return instance;
    }

    public Double getCost() {
        if (cost == null) {
            this.cost = routes.stream()
                    .mapToDouble(Route::getTotalDistance)
                    .sum();
        }
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Double getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(Double timeTaken) {
        this.timeTaken = timeTaken;
    }

    public void setInstance(EVRPTWInstance instance) {
        this.instance = instance;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    public int getNumberOfRoutes() {
        return routes.size();
    }

    public Route getIthRoute(int i) {
        if (i < 0 || i > routes.size() - 1) {
            throw new IllegalArgumentException("Invalid index");
        }
        return routes.get(i);
    }

    public void setIthRoute(int i, Route route) {
        if (i < 0 || i > routes.size() - 1) {
            throw new IllegalArgumentException("Invalid index");
        }
        Route ithRoute = routes.get(i);
        ithRoute = route;
    }

    public double getTotalDistance() {
        return routes.stream()
                .mapToDouble(Route::getTotalDistance)
                .sum();
    }

    public Solution copy() {
        return new Solution(this.routes);
    }

    private String getSaveLocation() {
        String base = "C:\\Users\\BogdanBo\\Documents\\Facultate\\Dizertatie\\src\\main\\resources\\solution";
        return String.join(File.separator, base, algorithm.getSaveLocation());
    }

    public void saveToFile() throws IOException {
        File file = new File(String.join(File.separator, getSaveLocation(), instance.getName()));
        file.getParentFile().mkdirs();
        file.createNewFile();
        try (FileWriter fileWriter = new FileWriter(file)) {
            // write cost
            fileWriter.write(String.valueOf(getCost()));
            fileWriter.write(System.lineSeparator());

            // write time taken
            fileWriter.write(String.valueOf(timeTaken));
            fileWriter.write(System.lineSeparator());

            // write routes
            for (Route route : routes) {
                String line = route.getNodes().stream()
                        .map(this::mapToString)
                        .collect(Collectors.joining(" "));
                fileWriter.write(line);
                fileWriter.write(System.lineSeparator());
            }
            fileWriter.flush();
        }
    }

    private String mapToString(Node node) {
        if (node instanceof Customer) {
            return ((Customer) node).getName();
        } else if (node instanceof RechargingStation) {
            return ((RechargingStation) node).getName();
        } else if (node instanceof Depot) {
            return ((Depot) node).getName();
        }
        throw new IllegalArgumentException("Unkown node type");
    }

    public boolean isFeasible() {
        return routes.stream().allMatch(Route::isFeasible);
    }

    public Solution deepClone() {
        List<Route> clonedRoutes = routes.stream()
                .map(Route::deepClone)
                .collect(Collectors.toList());
        return new Solution(instance, algorithm, clonedRoutes, cost, timeTaken);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Solution solution = (Solution) o;
        return Objects.equals(routes, solution.routes) &&
                Objects.equals(instance, solution.instance) &&
                algorithm == solution.algorithm &&
                Objects.equals(cost, solution.cost) &&
                Objects.equals(timeTaken, solution.timeTaken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routes, instance, algorithm, cost, timeTaken);
    }
}
