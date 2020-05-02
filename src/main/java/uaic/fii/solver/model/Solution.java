package uaic.fii.solver.model;

import java.util.ArrayList;
import java.util.List;

public class Solution {

    private List<Route> routes;

    public Solution(List<Route> routes) {
        this.routes = new ArrayList<>(routes);
    }

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes;
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
}
