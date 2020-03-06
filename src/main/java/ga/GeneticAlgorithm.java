package ga;

import model.Customer;
import model.EVRPTWInstance;
import model.Node;
import model.Route;
import org.javatuples.Pair;

import java.util.*;

public class GeneticAlgorithm {

    private EVRPTWInstance instance;
    private List<Chromosome> population;
    private int populationSize;
    private static final Random RANDOM = new Random();

    public GeneticAlgorithm(EVRPTWInstance instance) {
        this.instance = instance;
        population = new ArrayList<>();
    }

    public void getInitialPopulation() {
        for (int i = 0; i < 10; i++) {
            Chromosome chromosome = new Chromosome(instance);
            chromosome.initRandomChromosome();
            population.add(chromosome);
        }
    }

    public void solve() {
        getInitialPopulation();
        for (int i = 0; i < 100; i++) {

        }
    }


    public Pair<Chromosome, Chromosome> rouletteWheelSelection() {
        final int selectionSize = 2;
        double[] cumulativeFitness = new double[populationSize];
        cumulativeFitness[0]  = population.get(0).getFitness();

        for (int i = 1; i < population.size(); i++) {
            double fitness = population.get(i).getFitness();
            cumulativeFitness[i] = cumulativeFitness[i - 1] + fitness;
        }

        List<Chromosome> selected = new ArrayList<>();
        for (int i = 0; i < selectionSize; i++) {
            double randomFitness = RANDOM.nextDouble() * cumulativeFitness[cumulativeFitness.length - 1];
            int index = Arrays.binarySearch(cumulativeFitness, randomFitness);
            if (index < 0) {
                index = Math.abs(index + 1);
            }
            selected.add(population.get(index));
        }
        return Pair.fromCollection(selected);
    }

    public void crossOver(Chromosome first, Chromosome second) {
        Chromosome firstOffspring = new Chromosome(instance);
        Chromosome secondOffspring = new Chromosome(instance);

        Route firstBest = first.getBestRouteInGene();
        Route secondBest = second.getBestRouteInGene();
        Set<Customer> firstBestCustomers = firstBest.getCustomers();
        Set<Customer> secondBestCustomers = secondBest.getCustomers();

        List<Route> firstRoutes = new ArrayList<>(first.getGenes());
        List<Route> secondRoutes = new ArrayList<>(second.getGenes());

        firstRoutes.remove(firstBest);
        secondRoutes.remove(secondBest);

        for (Route route : secondRoutes) {
            List<Node> nodes = new ArrayList<>(route.getNodes());
            Iterator<Node> iterator = nodes.iterator();
            while (iterator.hasNext()) {
                Node node = iterator.next();
                if (instance.isCustomer(node)) {
                    if (firstBestCustomers.contains(instance.getCustomer(node))) {
                        iterator.remove();
                    }
                }
            }
        }

        for (Route route : firstRoutes) {
            List<Node> nodes = new ArrayList<>(route.getNodes());
            Iterator<Node> iterator = nodes.iterator();
            while (iterator.hasNext()) {
                Node node = iterator.next();
                if (instance.isCustomer(node)) {
                    if (secondBestCustomers.contains(instance.getCustomer(node))) {
                        iterator.remove();
                    }
                }
            }
        }

        firstOffspring.getGenes().add(firstBest);
        firstOffspring.getGenes().addAll(secondRoutes);

        secondOffspring.getGenes().add(secondBest);
        secondOffspring.getGenes().addAll(firstRoutes);
    }


}
