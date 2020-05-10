package uaic.fii.solver.exact;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBModel;
import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Solution;

import java.io.IOException;

public abstract class GurobiModel {

    protected GRBEnv env;
    protected GRBModel model;
    protected EVRPTWInstance instance;

    public GurobiModel(EVRPTWInstance instance) {
        this.instance = instance;
    }

    private void start() throws GRBException {
        env = new GRBEnv("mip.log");
        model = new GRBModel(env);
        model.set(GRB.DoubleParam.TimeLimit, 7200); // maximum time is 2 hours
    }

    private void dispose() throws GRBException {
        env.dispose();
        model.dispose();
    }

    protected abstract void createVariables() throws GRBException;

    protected abstract void createConstraints() throws GRBException;

    protected abstract void createObjective() throws GRBException;

    protected abstract Solution extractSolution() throws GRBException;

    public Solution solve() throws GRBException, IOException {
        start();
        createVariables();
        createObjective();
        createConstraints();
        model.optimize();
        Solution solution = extractSolution();
        dispose();
        return solution;
    }

}
