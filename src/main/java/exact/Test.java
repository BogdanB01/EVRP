package exact;

import gurobi.*;
import model.Customer;
import model.EVRPTWInstance;
import model.Node;
import model.RechargingStation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Test {

    private EVRPTWInstance instance;

    private void createObjectiveFunction(EVRPTWInstance instance) {
        this.instance = instance;
    }

    private void createObjectiveFunction(GRBEnv env) throws GRBException {
        // create empty model
        GRBModel model = new GRBModel(env);

        List<Customer> customers = instance.getCustomers();
        List<RechargingStation> rechargingStations = instance.getRechargingStations();

        List<Node> V0 = Stream.of(customers, rechargingStations).flatMap(List::stream).collect(Collectors.toList());
        V0.add(instance.getDepot());

        GRBVar[][] vars = new GRBVar[V0.size()][V0.size()];

        for (int i = 0; i < V0.size(); i++) {
            for (int j = 0; j <= i; j++) {
                vars[i][j] = model.addVar(0.0, 1.0,
                        instance.getTravelDistance(V0.get(i), V0.get(j)),
                        GRB.BINARY, "X" + String.valueOf(i) + String.valueOf(j));
                vars[j][i] = vars[i][j];
            }
        }

        GRBLinExpr lhs = new GRBLinExpr();
        for (int j = 0; j < V0.size(); j++) {
            for (int i = 0; i < customers.size(); i++) {
                if (i == j) continue;
                lhs.addTerm(1.0, vars[j][i]);
            }
        }
        model.addConstr(lhs, GRB.EQUAL, 1, "First constraint");

        lhs = new GRBLinExpr();
        for (int j = 0; j < V0.size(); j++) {
            for (int i = 0; i < instance.getRechargingStations().size(); i++) {

            }
        }
    }

    public static void main(String[] args) {
        try {
            GRBEnv env = new GRBEnv("mip.log");
            env.start();

        } catch (GRBException e) {
            e.printStackTrace();
        }

    }
}
