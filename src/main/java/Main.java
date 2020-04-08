import exact.EVRPTWModel;
import exact.GurobiSolver;
import gurobi.GRBException;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import model.EVRPTWInstance;
import model.Node;
import model.Route;
import verifier.SchneiderLoader;
import static guru.nidi.graphviz.model.Factory.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        File instanceFile = new File("input/c101C10.txt");
        EVRPTWInstance instance;
        try {
            instance = new SchneiderLoader().load(instanceFile);

            EVRPTWModel model = new EVRPTWModel(instance);
            model.solve();


        } catch(IOException e) {
            System.err.println("Error: error while parsing the instance file (" + instanceFile.getPath() + ")\n"
                    + "is this an actual E-VRPTW instance file?");
        } catch (GRBException e) {
            e.printStackTrace();
        }
    }

    private static void visualizeSolution(EVRPTWInstance instance, List<Route> routes) {

        MutableGraph g = mutGraph(instance.getName()).setDirected(true);

        Map<Integer, MutableNode> graphNodes = new HashMap<>();
        for (Node node : instance.getNodes()) {
            MutableNode graphNode = null;
            if (instance.isCustomer(node)) {
                graphNode = mutNode(instance.getCustomer(node).getName());
                graphNode.add(Color.RED);
            } else if (instance.isDepot(node)) {
                graphNode = mutNode(instance.getDepot().getName());
                graphNode.add(Color.BLUE);
            } else {
                graphNode = mutNode("3");//mutNode(instance.getRechargingStation(node).getName());
                graphNode.add(Color.GREEN);
            }
            graphNodes.put(node.id, graphNode);
        }

        for (Route route : routes) {
            for (int i = 0; i < route.getNodes().size() - 1; i++) {
                Node from = route.getNodes().get(i);
                Node to = route.getNodes().get(i + 1);
                graphNodes.get(from.id).addLink(graphNodes.get(to.id));
            }
        }
        try {
            Graphviz.fromGraph(g).width(200).render(Format.PNG).toFile(new File("output/ex1.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
