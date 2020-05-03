package uaic.fii.solver.exact;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBVar;
import uaic.fii.model.*;
import uaic.fii.util.Algorithm;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EVRPTWModel extends GurobiModel {

    private GRBVar x[][];
    private GRBVar t[];
    private GRBVar u[];
    private GRBVar y[];
    private List<Integer> V;
    private List<Integer> V0;
    private List<Integer> Vn;
    private List<Integer> V0n;
    private List<Integer> I;
    private List<Integer> I0;
    private List<Integer> F;
    private List<Integer> F0;
    private Double l0;
    private Double Q;
    private Double g;
    private Double r;
    private Double C;

    public EVRPTWModel(EVRPTWInstance instance) {
        super(instance);
        instance.addDummies();
        initSets();
        l0 = instance.getDepot().getTimeWindow().getEnd();
        Q = instance.getVehicleEnergyCapacity();
        g = instance.getRechargingRate();
        r = instance.getVehicleEnergyConsumption();
        C = instance.getVehicleCapacity();
    }

    private List<Integer> mapToIds(List<? extends Node> nodes) {
        return nodes.stream().map(e -> e.id).collect(Collectors.toList());
    }

    private void initSets() {
        I = mapToIds(instance.getCustomers());
        I0 = new ArrayList<>(I);
        I0.add(instance.getDepot().id);

        F = mapToIds(instance.getRechargingStations());
        F0 = new ArrayList<>(F);
        F0.add(instance.getDepot().id);

        V = new ArrayList<>(I);
        V.addAll(F);

        V0 = new ArrayList<>(V);
        V0.add(instance.getDepot().id);

        Vn = new ArrayList<>(V);
        Vn.add(instance.getNodes().get(instance.getNodes().size() - 1).id);

        V0n = new ArrayList<>(V0);
        V0n.add(instance.getNodes().get(instance.getNodes().size() - 1).id);
    }

    @Override
    protected void createVariables() throws GRBException {
        this.x = new GRBVar[V0.size() + 1][Vn.size() + 1];
        this.t = new GRBVar[V0n.size()];
        this.u = new GRBVar[V0n.size()];
        this.y = new GRBVar[V0n.size()];

        for (Integer i : V0n) {
            Node node = instance.getById(i);
            Node.TimeWindow tw = instance.getTimeWindow(node);
            t[i] = model.addVar(tw.getStart(), tw.getEnd(), 0.0, GRB.CONTINUOUS, "t[" + i + "]");
            u[i] = model.addVar(0.0, instance.getVehicleCapacity(), 0.0, GRB.CONTINUOUS, "u[" + i + "]");
            y[i] = model.addVar(0.0, instance.getVehicleEnergyCapacity(), 0.0, GRB.CONTINUOUS, "y[" + i + "]");
        }
    }

    @Override
    protected void createConstraints() throws GRBException {
        addConnectivityOfCustomersConstraint();
        addConnectivityOfVisitsToChargersConstraint();
        addFlowConservationConstraint();
        addTimeFeasibilityForCustomersConstraint();
        addTimeFeasibilityForRechargingStationsConstraint();
        addTimeWindowConstraint();
        addNonNegativeCargoLoadForCustomersConstraint();
        addNonNegativeCargoLoadForDepotConstraint();
        addBatteryChargeForCustomersConstraint();
        addBatteryChargeForChargersConstraint();
    }

    private void addTimeFeasibilityForCustomersConstraint() throws GRBException {
        for (Integer j : Vn) {
            for (Integer i : I0) {
                Node from = instance.getById(j);
                Node to = instance.getById(i);
                if (from.id != to.id) {
                    GRBLinExpr lhs = new GRBLinExpr();
                    lhs.addTerm(1, t[to.id]);
                    lhs.addTerm(instance.getTravelTime(from, to) + instance.getServiceTime(to), x[to.id][from.id]);

                    GRBLinExpr expr = new GRBLinExpr();
                    expr.addConstant(1);
                    expr.addTerm(-1, x[to.id][from.id]);
                    lhs.multAdd(-l0, expr);
                    model.addConstr(lhs, GRB.LESS_EQUAL, t[from.id],"Time Feasibility for customers constraint");
                }
            }
        }
    }

    private void addTimeFeasibilityForRechargingStationsConstraint() throws GRBException {
        for (Integer j : Vn) {
            for (Integer i : F) {
                Node from = instance.getById(j);
                Node to = instance.getById(i);
                if (from.id != to.id) {
                    GRBLinExpr lhs = new GRBLinExpr();
                    lhs.addTerm(1, t[to.id]);
                    lhs.addTerm(instance.getTravelTime(from, to), x[to.id][from.id]);

                    GRBLinExpr expr = new GRBLinExpr();
                    expr.addConstant(Q);
                    expr.addTerm(-1, y[to.id]);
                    lhs.multAdd(g, expr);

                    expr = new GRBLinExpr();
                    expr.addConstant(1);
                    expr.addTerm(-1, x[to.id][from.id]);
                    lhs.multAdd(-(l0 + g * Q), expr);
                    model.addConstr(lhs, GRB.LESS_EQUAL, t[from.id], "Time feasibility for recharging stations constraint");
                }
            }
        }
    }

    private void addTimeWindowConstraint() throws GRBException {
        for (Integer j : V0n) {
            Node node = instance.getById(j);
            Node.TimeWindow timeWindow = instance.getTimeWindow(node);
            model.addConstr(timeWindow.getStart(), GRB.LESS_EQUAL, t[node.id], String.format("e[%d] <= t[%d]", node.id, node.id));
            model.addConstr(t[node.id], GRB.LESS_EQUAL, timeWindow.getEnd(), String.format("t[%d] <= l[%d]", node.id, node.id));
        }
    }

    private void addNonNegativeCargoLoadForCustomersConstraint() throws GRBException {
        for (Integer j : Vn) {
            for (Integer i : V0) {
                Node from = instance.getById(j);
                Node to = instance.getById(i);
                if (from.id != to.id) {
                    GRBLinExpr rhs = new GRBLinExpr();
                    rhs.addTerm(1, u[to.id]);
                    rhs.addTerm(-instance.getDemand(to), x[to.id][from.id]);

                    GRBLinExpr expr = new GRBLinExpr();
                    expr.addConstant(1);
                    expr.addTerm(-1, x[to.id][from.id]);
                    rhs.multAdd(C, expr);

                    model.addConstr(0, GRB.LESS_EQUAL, u[from.id], "");
                    model.addConstr(u[from.id], GRB.LESS_EQUAL, rhs, "");
                }
            }
        }
    }

    private void addNonNegativeCargoLoadForDepotConstraint() throws GRBException {
        model.addConstr(0, GRB.LESS_EQUAL, u[instance.getDepot().id], "");
        model.addConstr(u[instance.getDepot().id], GRB.LESS_EQUAL, C, "");
    }

    private void addBatteryChargeForCustomersConstraint() throws GRBException {
        for (Integer j : Vn) {
            for (Integer i : I) {
                Node from = instance.getById(j);
                Node to = instance.getById(i);
                if (from.id != to.id) {
                    GRBLinExpr rhs = new GRBLinExpr();
                    rhs.addTerm(1, y[to.id]);
                    rhs.addTerm(-r * instance.getTravelDistance(to, from), x[to.id][from.id]);

                    GRBLinExpr expr = new GRBLinExpr();
                    expr.addConstant(1);
                    expr.addTerm(-1, x[to.id][from.id]);
                    rhs.multAdd(Q, expr);

                    model.addConstr(y[from.id], GRB.GREATER_EQUAL, 0, "y[j] >= 0");
                    model.addConstr(y[from.id], GRB.LESS_EQUAL, rhs, "");
                }
            }
        }
    }

    private void addBatteryChargeForChargersConstraint() throws GRBException {
        for (Integer j : Vn) {
            for (Integer i : F0) {
                Node from = instance.getById(j);
                Node to = instance.getById(i);
                if (from.id != to.id) {
                    GRBLinExpr expr = new GRBLinExpr();
                    expr.addConstant(Q);
                    expr.addTerm(-r * instance.getTravelDistance(to, from), x[to.id][from.id]);
                    model.addConstr(y[from.id], GRB.LESS_EQUAL, expr, "");
                }
            }
        }
    }

    private void addFlowConservationConstraint() throws GRBException {
        for (Integer j : V) {
            Node from = instance.getById(j);
            GRBLinExpr expr = new GRBLinExpr();
            for (Integer i : Vn) {
                Node to = instance.getById(i);
                if (from.id != to.id) {
                    expr.addTerm(1, x[from.id][to.id]);
                }
            }
            for (Integer i : V0) {
                Node to = instance.getById(i);
                if (from.id != to.id) {
                    expr.addTerm(-1, x[to.id][from.id]);
                }
            }
            model.addConstr(expr, GRB.EQUAL, 0, "Flow conservation constraint");
        }
    }

    private void addConnectivityOfVisitsToChargersConstraint() throws GRBException {
        for (Integer i : F) {
            GRBLinExpr expr = new GRBLinExpr();
            for (Integer j : Vn) {
                Node from = instance.getById(i);
                Node to = instance.getById(j);
                if (from.id != to.id) {
                    expr.addTerm(1.0, x[from.id][to.id]);
                }
            }
            model.addConstr(expr, GRB.LESS_EQUAL, 1, "Connectivity of visits to recharging stations");
        }
    }

    private void addConnectivityOfCustomersConstraint() throws GRBException {
        for (Integer i : I) {
            GRBLinExpr expr = new GRBLinExpr();
            for (Integer j : Vn) {
                Node from = instance.getById(i);
                Node to = instance.getById(j);
                if (from.id != to.id) {
                    expr.addTerm(1.0, x[from.id][to.id]);
                }
            }
            model.addConstr(expr, GRB.EQUAL, 1, "Connectivity of customers constraint");
        }
    }

    @Override
    protected void createObjective() throws GRBException {
        GRBLinExpr minimizeDistance = new GRBLinExpr();
        for (Integer i : V0) {
            for (Integer j : Vn) {
                Node from = instance.getById(i);
                Node to = instance.getById(j);
                if (from.id != to.id) {
                    x[from.id][to.id] = model.addVar(0.0, 1.0, 0.0,
                            GRB.BINARY, String.format("X[%d][%d]", from.id, to.id));
                    minimizeDistance.addTerm(instance.getTravelDistance(from, to), x[from.id][to.id]);
                }
            }
        }

        GRBLinExpr minimizeFleetSize = new GRBLinExpr();
        for (Integer j : V) {
            Node node = instance.getById(j);
            minimizeFleetSize.addTerm(1.0, x[instance.getDepot().id][node.id]);
        }

        double maximumDistance = 0;
        for (Customer customer : instance.getCustomers()) {
            maximumDistance += instance.getTravelDistance(instance.getDepot(), customer) * 2;
        }

        final double alpha = 1.0 / instance.getMaxVehicles();
        final double beta = 1.0 / maximumDistance;

        //GRBLinExpr objective = new GRBLinExpr();
     //   objective.multAdd(alpha, minimizeFleetSize);
        //objective.multAdd(beta, minimizeDistance);


     //   model.setObjectiveN(minimizeFleetSize, 0, 1, alpha, 0, 0, "Minimize Fleet Size Objective");
      //  model.setObjectiveN(minimizeDistance, 1, 2, beta, 0, 0, "Minimize Distance Objective");
        model.setObjective(minimizeDistance, GRB.MINIMIZE);
    }

    @Override
    protected Solution extractSolution() throws GRBException {
        List<Route> routes = new ArrayList<>();
        Route route = new Route(instance);

        int start = instance.getDepot().id;
        int end = instance.getById(instance.getNumNodes()  - 1).id;

        for (int j = 0; j < x[start].length; j++) {
            if (x[start][j] != null && x[start][j].get(GRB.DoubleAttr.X) == 1) {
                int i = start;
                int k = j;
                while (true) {
                    if ( ! route.getEnd().equals(instance.getById(i))) {
                        route.addNode(instance.getById(i));
                    }
                    route.addNode(instance.getById(k));

                    if (k == end) {
                        break;
                    }
                    i = k;
                    for (int l = 0; l < x[i].length; l++) {
                        if (x[i][l] != null && x[i][l].get(GRB.DoubleAttr.X) == 1) {
                            k = l;
                            break;
                        }
                    }
                }
                routes.add(route);
                route = new Route(instance);
            }
        }

        return new Solution(instance, Algorithm.EXACT, routes);
    }
}
