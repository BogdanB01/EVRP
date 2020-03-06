import model.*;

import java.util.ArrayList;
import java.util.List;

class Heuristic {
    private EVRPTWInstance instance;
    private List<Customer> customers;
    private RouteHelper routeHelper;

    Heuristic(EVRPTWInstance instance) {
        this.instance = instance;
        this.routeHelper = new RouteHelper(instance);
        this.customers = new ArrayList<>(instance.getCustomers());
        translateCustomersByDepot();
        customers.sort(Customer.ANGLE_COMPARATOR);
    }


    List<Route> construct() {
        List<Route> routes = new ArrayList<>();
        while (customers.size() > 0) {
            Route route = createRoute();
            System.out.println(route.getNodes());
            customers.removeAll(route.getCustomers());
            routes.add(route);
        }
        return routes;
    }

    private Route createRoute() {
        Route route = new Route(instance);
        List<Customer> infeasibleCustomers = new ArrayList<>();
        for (Customer customer : customers) {
            if (!routeHelper.isInsertionFeasible(route, customer, true)) {
                infeasibleCustomers.add(customer);
            } else {
                route.addNode(customer);
            }
        }

        // last customer must recharge before going to the next customer of to the depot
        if (!route.isEmpty() && !routeHelper.isDepotReachable(route)) {
            RechargingStation rechargingStation = routeHelper.getNearestCharger(route.getEnd());
            route.insertRechargingStation(rechargingStation);

            // try to insert previously infeasible customers
            for (Customer customer : infeasibleCustomers) {
                if (routeHelper.isInsertionFeasible(route, customer, false)) {
                    route.addNode(customer);
                }
            }
        } else if (route.isEmpty()) {
            for (Customer customer : infeasibleCustomers) {
                route.addNode(routeHelper.getNearestChargerBetweenNodes(route.getEnd(), customer));
                if (routeHelper.isInsertionFeasible(route, customer, true)) {
                    route.addNode(customer);
                    route.addNode(routeHelper.getNearestChargerBetweenNodes(route.getEnd(), instance.getDepot()));
                    break;
                }
            }
        }
        route.addNode(instance.getDepot());
        return route;
    }

    private void translateCustomersByDepot() {
        for (Customer customer : customers) {
            customer.translateByDepot(instance.getDepot());
        }
    }
}
