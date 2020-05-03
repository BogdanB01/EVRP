package uaic.fii.solver.verifier;

import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Node;
import uaic.fii.model.Route;
import uaic.fii.model.Solution;

import java.io.*;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class RoutesLoader {
    private EVRPTWInstance instance;

    private RoutesLoader(EVRPTWInstance instance) {
        this.instance = instance;
    }

    public Solution load(File solutionFile) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(solutionFile));

            // if the first line starts with an #, then ignore it
            // first value is the objective value
            String line = nextLine(in);
            while(line.startsWith("#")) line = nextLine(in);

            double cost;
            try {
                cost = Double.parseDouble(line);
            } catch(NumberFormatException e) {
                System.err.println("Error: first line of the solution has to be the cost (number)");
                return null;
            }

            // second line is the time taken
            line = nextLine((in));
            double time;
            try {
                cost = Double.parseDouble(line);
            } catch(NumberFormatException e) {
                System.err.println("Error: second line of the solution has to be the time taken (number)");
                return null;
            }

            List<Route> routes = new ArrayList<>();
            try {
                while((line = nextLine(in)) != null)
                    routes.add(parseLine(line, instance));
            } catch(RuntimeException e) {
                e.printStackTrace();
                System.err.println("Error: exception while parsing the nodes in the solution");
                return null;
            }
            Solution solution = new Solution(routes);
            solution.setInstance(instance);
            solution.setCost(cost);
            return solution;
        } catch(FileNotFoundException e) {
            System.err.println("Error: couldn't open solution file " + solutionFile.getPath());
            return null;
        } catch(IOException e) {
            System.err.println("Error while reading solution file (IOException)");
            return null;
        }
    }

    private static String nextLine(BufferedReader in) throws IOException {
        String line = in.readLine();
        while(line != null && (line = line.trim()).equals(""))
            line = in.readLine();
        return line;
    }

    private static Route parseLine(String line, EVRPTWInstance instance) throws RuntimeException {
        List<Node> list = new ArrayList<>();
        StringTokenizer tok = new StringTokenizer(line, " ,");
        for(; tok.hasMoreTokens(); ) {
            String id = tok.nextToken();
            if(id.startsWith("D"))
                list.add(instance.getDepot());
            else if(id.startsWith("S"))
                list.add(instance.getRechargingStation(id));
            else if(id.startsWith("C"))
                list.add(instance.getCustomer(id));
            else
                throw new RuntimeException();
        }
        return new Route(instance, list);
    }

    public static RoutesLoader create(EVRPTWInstance instance) {
        return new RoutesLoader(instance);
    }
}
