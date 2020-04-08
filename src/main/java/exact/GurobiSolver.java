package exact;

import gurobi.*;
import model.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GurobiSolver {

    private EVRPTWInstance instance;

    public GurobiSolver(EVRPTWInstance instance) {
        this.instance = instance;
    }

    private List<RechargingStation> getRechargingStations() {
        List<RechargingStation> chargers = new ArrayList<>();
        int numberOfCustomers = instance.getNumCustomers();
        int numberOfVehicles = instance.getMaxVehicles();

        // the maximum number of visits to a single recharging station
        // is at most numberOfCustomers + numberOfVehicles
        for (RechargingStation charger : instance.getRechargingStations()) {
            for (int i = 0; i < 3; i++) {
                chargers.add(charger);
            }
        }

        return chargers;
    }

    public void solve() throws GRBException {
        GRBEnv env = new GRBEnv("mip.log");
        env.start();
        GRBModel model = new GRBModel(env);

        List<Customer> I = instance.getCustomers();
        List<RechargingStation> F = getRechargingStations();
        List<Node> V = Stream.of(I, F).flatMap(List::stream).collect(Collectors.toList());

        List<Node> V0 = new ArrayList<>(V);
        V0.add(instance.getDepot());

        GRBVar[][] x = new GRBVar[V0.size()][V0.size()];
        GRBLinExpr objective = new GRBLinExpr();
        for (int i = 0; i < V0.size(); i++) {
            for (int j = 0; j < V0.size(); j++) {
                if (i == j) continue;
                x[i][j] = model.addVar(0.0, 1.0, 0.0,
                        GRB.BINARY, String.format("X[%d][%d]", i, j));
                objective.addTerm(instance.getTravelDistance(V0.get(i), V0.get(j)), x[i][j]);
            }
        }
        model.setObjective(objective, GRB.MINIMIZE);

        GRBVar[] t = new GRBVar[V0.size()];
        GRBVar[] u = new GRBVar[V0.size()];
        GRBVar[] y = new GRBVar[V0.size()];

        for (int i = 0; i < V0.size(); i++) {
            Node.TimeWindow tw = instance.getTimeWindow(V0.get(i));
            t[i] = model.addVar(tw.getStart(), tw.getEnd(), 0.0, GRB.CONTINUOUS, "t[" + i + "]");
            u[i] = model.addVar(0.0, instance.getVehicleCapacity(), 0.0, GRB.CONTINUOUS, "u[" + i + "]");
            y[i] = model.addVar(0.0, instance.getVehicleEnergyCapacity(), 0.0, GRB.CONTINUOUS, "y[" + i + "]");
        }

        // Constraint (2)
        for (int i = 0; i < I.size(); i++) {
            GRBLinExpr lhs = new GRBLinExpr();
            for (int j = 0; j < V0.size(); j++) {
                if (i == j) continue;
                lhs.addTerm(1.0, x[i][j]);
            }
            model.addConstr(lhs, GRB.EQUAL, 1, "Connectivity of customers constraint");
        }

        // Constraint (3)
        for (int i = I.size(); i < V0.size() - 1; i++) {
            GRBLinExpr lhs = new GRBLinExpr();
            for (int j = 0; j < V0.size(); j++) {
                if (i == j) continue;
                lhs.addTerm(1.0, x[i][j]);
            }
            model.addConstr(lhs, GRB.LESS_EQUAL, 1, "Connectivity of visits to recharging stations constraint");
        }

        // Constraint (4)
        for (int j = 0; j < V.size(); j++) {
            GRBLinExpr lhs = new GRBLinExpr();
            for (int i = 0; i < V0.size(); i++) {
                if (i == j) continue;
                lhs.addTerm(1.0, x[j][i]);
                lhs.addTerm(-1.0, x[i][j]);
            }
            model.addConstr(lhs, GRB.EQUAL, 0, "Flow conservation constraint");
        }

        double l0 = instance.getDepot().getTimeWindow().getEnd();

        // Constraint (5)
        for (int i = 0; i < I.size(); i++) {
            for (int j = 0; j < V0.size(); j++) {
                if (i == j) continue;
                GRBLinExpr lhs = new GRBLinExpr();
                lhs.addTerm(1, t[i]);
                lhs.addTerm(instance.getTravelTime(V0.get(i), V0.get(j))
                        + instance.getServiceTime(V0.get(i)), x[i][j]);

                GRBLinExpr expr = new GRBLinExpr();
                expr.addConstant(1);
                expr.addTerm(-1, x[i][j]);
                lhs.multAdd(-l0, expr);
                model.addConstr(lhs, GRB.LESS_EQUAL, t[j], "Time feasibility for arc " + j + " leaving the customers and the depot");
            }
        }
/*
        for (int j = 0; j < V0.size(); j++) {
            int i = V0.size() - 1;
            if (i == j) continue;
            GRBLinExpr lhs = new GRBLinExpr();
            lhs.addTerm(1, t[i]);
            lhs.addTerm(instance.getTravelTime(V0.get(j), V0.get(i))
                    + instance.getServiceTime(V0.get(i)), x[i][j]);

            GRBLinExpr expr = new GRBLinExpr();
            expr.addConstant(1);
            expr.addTerm(-1, x[i][j]);
            lhs.multAdd(-l0, expr);
            model.addConstr(lhs, GRB.LESS_EQUAL, t[j], "Time feasibility for arc " + j + " leaving the customers and the depot");
        }*/

        double g = instance.getRechargingRate(F.get(0));
        double Q = instance.getVehicleEnergyCapacity();
        double C = instance.getVehicleCapacity();


       // Constraint (6)
       for (int i = I.size(); i < V0.size() - 1; i++) {
            for (int j = 0; j < V0.size(); j++) {
                if (i == j) continue;
                GRBLinExpr lhs = new GRBLinExpr();
                lhs.addTerm(1, t[i]);
                lhs.addTerm(instance.getTravelTime(V0.get(i), V0.get(j)), x[i][j]);

                GRBLinExpr expr = new GRBLinExpr();
                expr.addConstant(Q);
                expr.addTerm(-1, y[i]);
                lhs.multAdd(g, expr);

                expr = new GRBLinExpr();
                expr.addConstant(1);
                expr.addTerm(-1, x[i][j]);

                lhs.multAdd(-(l0 + g * Q), expr);
                model.addConstr(lhs, GRB.LESS_EQUAL, t[j], "Time feasibility for arc " + j + " leaving the recharging stations");
            }
        }

        // Constraint 7
        for (int j = 0; j < V0.size(); j++) {
            Node.TimeWindow timeWindow = instance.getTimeWindow(V0.get(j));
            model.addConstr(timeWindow.getStart(), GRB.LESS_EQUAL, t[j], String.format("e[%d] <= t[%d]", j, j));
            model.addConstr(t[j], GRB.LESS_EQUAL, timeWindow.getEnd(), String.format("t[%d] <= l[%d]", j, j));
        }

       // Constraint 7
       for (int j = 0; j < V0.size(); j++) {
            for (int i = 0; i < V0.size(); i++) {
                if (V0.get(i).equals(V0.get(j))) continue;
                //if (i == j) continue;
                GRBLinExpr left = new GRBLinExpr();
                left.addTerm(1, u[j]);

                GRBLinExpr right = new GRBLinExpr();

                right.addTerm(1, u[i]);
                right.addTerm(-instance.getDemand(V0.get(i)), x[i][j]);

                right.addConstant(C);
                right.addTerm(-C, x[i][j]);

                model.addConstr(left, GRB.LESS_EQUAL, right, "");
                model.addConstr(left, GRB.GREATER_EQUAL, 0, "");
            }
        }


        // Constraint (9)
        model.addConstr(0, GRB.LESS_EQUAL, u[u.length - 1], "");
        model.addConstr(u[u.length - 1], GRB.LESS_EQUAL, C, "");


        double r = instance.getVehicleEnergyConsumption();

        // Constraint (10)
        for (int j = 0; j < V0.size(); j++) {
            for (int i = 0; i < I.size(); i++) {
                //if (V0.get(j).equals(I.get(i))) continue;
                if (i == j) continue;
                GRBLinExpr left = new GRBLinExpr();
                left.addTerm(1, y[j]);

                GRBLinExpr right = new GRBLinExpr();
                right.addTerm(1, y[i]);
                right.addTerm(- r * instance.getTravelDistance(V0.get(j),
                        I.get(i)), x[i][j]);

                right.addConstant(Q);
                right.addTerm(-Q, x[i][j]);

                model.addConstr(left, GRB.LESS_EQUAL, right, "");
                model.addConstr(left, GRB.GREATER_EQUAL, 0, "");
            }
        }

        // Constraint (11)
        for (int j = 0; j < V0.size(); j++) {
            for (int i = I.size(); i < V0.size(); i++) {
                if (i == j) continue;

                GRBLinExpr right = new GRBLinExpr();
                right.addConstant(Q);
                right.addTerm(-r * instance.getTravelDistance(V0.get(j), V0.get(i)), x[i][j]);
                model.addConstr(y[j], GRB.LESS_EQUAL, right, "Ensure that battery charge never falls below 0");
            }
        }

        model.optimize();

        List<Route> routes = new ArrayList<>();
        Route route = new Route(instance);

        int indexOfDepot = x.length - 1;
      /*  for (int j = 0; j < x.length; j++) {
            if (x[indexOfDepot][j].get(GRB.DoubleAttr.X) == 1) {
                int i = x.length - 1;
                int k = j;
                while (true) {
                    if ( ! route.getEnd().equals(V0.get(i))) {
                        route.addNode(V0.get(i));
                    }
                    if ( ! route.getEnd().equals(V0.get(k))) {
                        route.addNode(V0.get(k));
                    }
                    if (k == indexOfDepot) {
                        break;
                    }
                    i = k;
                    for (int l = 0; l < x[i].length; l++) {
                        if (x[i][l].get(GRB.DoubleAttr.X) == 1) {
                            k = l;
                            break;
                        }
                    }
                }
                routes.add(route);
                route = new Route(instance);
            }
        }*/

        for (Route p : routes) {
            System.out.println(p.getNodes() + " " + p.isFeasible());
        }
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x.length; j++) {
                if (x[i][j] != null && x[i][j].get(GRB.DoubleAttr.X) == 1.0) {
                    System.out.println(String.format("X[%d][%d]", i, j));
                    System.out.println(V0.get(i) + " -> " + V0.get(j));
                }
            }
        }

        for (int i = 0; i < V0.size(); i++) {
            System.out.println("t[" +  i  + "] = " + t[i].get(GRB.DoubleAttr.X));
            System.out.println("y[" +  i  + "] = " + y[i].get(GRB.DoubleAttr.X));
        }

        System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));

        model.dispose();
        env.dispose();
    }



    private Node getRechargingStationByIthVisit(int i) {
        return null;
    }


    public static void main(String[] args) {
        // depot -> id 0
        // customers -> 1 -> n
        // recharging stations
        // depot -> n + 1

    }
}
