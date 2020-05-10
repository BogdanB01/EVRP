package uaic.fii.solver.ga;

import uaic.fii.model.Solution;
import uaic.fii.solver.ga.search.TabuSearch;
import uaic.fii.solver.greedy.BeasleyHeuristic;
import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Node;
import uaic.fii.model.Route;
import uaic.fii.util.Algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeneticAlgorithm {

    private EVRPTWInstance instance;
    private Population population;
    private Population initialPopulation;
    private int maxGenerations; // The number of generations to run
    private int k; // For tournament selection
    private double crossoverRate; // Odds of crossover occurring
    private double mutationRate; // Odds of mutation occurring
    private double localSearchRate; // Odds of local search occurring
    private Random random;
    private CrossoverType crossoverType = CrossoverType.UNIFORM_ORDER;
    private MutationType mutationType = MutationType.INSERTION;

    private boolean finished;

    // Results
    private double averageDistanceOfFirstGeneration;
    private double averageDistanceOfLastGeneration;
    private double bestDistanceOfFirstGeneration;
    private double bestDistanceOfLastGeneration;
    private List<Double> averageDistanceOfEachGeneration;
    private List<Double> bestDistanceOfEachGeneration;
    private double areaUnderAverageDistances;
    private double areaUnderBestDistances;
    private double timeTaken;

    public GeneticAlgorithm(EVRPTWInstance instance) {
        this.instance = instance;
        initialPopulation = Population.getRandomPopulation(instance, 30, new Random());
        population = initialPopulation.deepCopy();
        maxGenerations = 100;
        k = 2;
        crossoverRate = 0.95;
        mutationRate = 0.05;
        localSearchRate = 0.2;
        random = new Random();
        finished = false;

        bestDistanceOfFirstGeneration = initialPopulation.getFittest().getDistance();
        averageDistanceOfFirstGeneration = initialPopulation.getAverageDistance();
        averageDistanceOfEachGeneration = new ArrayList<>();
        bestDistanceOfEachGeneration = new ArrayList<>();
    }

    public void setPopulation(Population population) {
        if (population == null) {
            throw new IllegalArgumentException("Parameter population cannot be null");
        }
        initialPopulation = population;
        this.population = initialPopulation.deepCopy();
        averageDistanceOfFirstGeneration = population.getAverageDistance();
        bestDistanceOfFirstGeneration = population.getFittest().getDistance();
    }

    public void setMaxGenerations(int maxGenerations) {
        if (maxGenerations < 0) {
            throw new IllegalArgumentException("Parameter cannot be negative");
        }
        this.maxGenerations = maxGenerations;
    }

    public void setK(int k) {
        if (k < 0) {
            throw new IllegalArgumentException("Parameter cannot be negative");
        }
        this.k = k;
    }

    public void setCrossoverRate(double crossoverRate) {
        if (crossoverRate < 0 || crossoverRate > 1) {
            throw new IllegalArgumentException("Parameter must be between 0 and 1 inclusive");
        }
        this.crossoverRate = crossoverRate;
    }

    public void setMutationRate(double mutationRate) {
        if (mutationRate < 0 || mutationRate > 1) {
            throw new IllegalArgumentException("Parameter must be between 0 and 1 inclusive");
        }
        this.mutationRate = mutationRate;
    }

    public void setLocalSearchRate(double localSearchRate) {
        if (localSearchRate < 0 || localSearchRate > 1) {
            throw new IllegalArgumentException("Parameter must be between 1 and 0 inclusive");
        }
        this.localSearchRate = localSearchRate;
    }

    public void setRandom(Random random) {
        if (random == null) {
            throw new IllegalArgumentException("Parameter cannot be null");
        }
        this.random = random;
    }

    public double getAverageDistanceOfFirstGeneration() {
        if (!finished) {
            throw new IllegalArgumentException("Genetic algorithm is still running");
        }
        return averageDistanceOfFirstGeneration;
    }

    public double getAverageDistanceOfLastGeneration() {
        if (!finished) {
            throw new IllegalArgumentException("Genetic algorithm is still running");
        }
        return averageDistanceOfLastGeneration;
    }

    public double getBestDistanceOfFirstGeneration() {
        if (!finished) {
            throw new IllegalArgumentException("Genetic algorithm is still running");
        }
        return bestDistanceOfFirstGeneration;
    }

    public double getBestDistanceOfLastGeneration() {
        if (!finished) {
            throw new IllegalArgumentException("Genetic algorithm is still running");
        }
        return bestDistanceOfLastGeneration;
    }

    public List<Double> getAverageDistanceOfEachGeneration() {
        if (!finished) {
            throw new IllegalArgumentException("Genetic algorithm is still running");
        }
        return averageDistanceOfEachGeneration;
    }

    public List<Double> getBestDistanceOfEachGeneration() {
        if (!finished) {
            throw new IllegalArgumentException("Genetic algorithm is still running");
        }
        return bestDistanceOfEachGeneration;
    }

    public double getAreaUnderAverageDistances() {
        if (!finished) {
            throw new IllegalArgumentException("Genetic algorithm is still running");
        }
        return areaUnderAverageDistances;
    }

    public double getAreaUnderBestDistances() {
        if (!finished) {
            throw new IllegalArgumentException("Genetic algorithm is still running");
        }
        return areaUnderBestDistances;
    }

    public void setCrossoverType (CrossoverType crossoverType) {
        this.crossoverType = crossoverType;
    }

    public void setMutationType(MutationType mutationType) {
        this.mutationType = mutationType;
    }

    public void run() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < maxGenerations; i++) {
            population = createNextGeneration();
            averageDistanceOfEachGeneration.add(population.getAverageDistance());
            areaUnderAverageDistances += population.getAverageDistance();
            bestDistanceOfEachGeneration.add(population.getFittest().getDistance());
            areaUnderBestDistances += population.getFittest().getDistance();
        }
        finished = true;
        averageDistanceOfLastGeneration = population.getAverageDistance();
        bestDistanceOfLastGeneration = population.getFittest().getDistance();
        timeTaken = System.currentTimeMillis() - start;
    }



    private Chromosome performTabuSearch(Chromosome chromosome) {
        List<Route> routes = decodeChromosome(chromosome);
        Solution solution = new Solution(instance, Algorithm.GA, routes);
        Solution improved  = new TabuSearch(instance).execute(solution);
        return extractChromosomeFromSolution(improved);
    }

    private Chromosome extractChromosomeFromSolution(Solution solution) {
        Node[] customers = solution.getRoutes().stream()
                .map(Route::getNodes)
                .flatMap(Collection::stream)
                .filter(instance::isCustomer)
                .toArray(Node[]::new);
        return new Chromosome(instance, customers);
    }

    private Population createNextGeneration() {
        Population nextGeneration = new Population(instance, population.size());

        while (nextGeneration.size() < population.size() - 1) {
            Chromosome c1 = Selection.tournamentSelection(population, k, random);
            Chromosome c2 = Selection.tournamentSelection(population, k, random);

            boolean doCrossover = (random.nextDouble() <= crossoverRate);
            boolean doMutate1 = (random.nextDouble() <= mutationRate);
            boolean doMutate2 = (random.nextDouble() <= mutationRate);
            boolean doLocalSearch1 = (random.nextDouble() <= localSearchRate);
            boolean doLocalSearch2 = (random.nextDouble() <= localSearchRate);

            if (doCrossover) {
                List<Chromosome> children = crossover(c1, c2);
                c1 = children.get(0);
                c2 = children.get(1);
            }

            if (doMutate1) c1 = mutate(c1);
            if (doMutate2) c2 = mutate(c2);

            if (doLocalSearch1) c1 = performTabuSearch(c1);
            if (doLocalSearch2) c2 = performTabuSearch(c2);

            nextGeneration.add(c1);
            nextGeneration.add(c2);
        }

        // If there is one space left, fill it up
        if (nextGeneration.size() != population.size()) {
            nextGeneration.add(Selection.tournamentSelection(population, k, random));
        }

        if (nextGeneration.size() != population.size()) {
            throw new AssertionError("Next generation population should be full");
        }

        return nextGeneration;
    }

    private List<Chromosome> crossover(Chromosome c1, Chromosome c2) {
        List<Chromosome> children;
        if (crossoverType == CrossoverType.UNIFORM_ORDER) {
            children = Crossover.uniformOrder(instance, c1, c2, random);
        } else if (crossoverType == CrossoverType.ONE_POINT) {
            children = Crossover.onePointCrossover(instance, c1, c2, random);
        } else {
            children = Crossover.orderCrossover(instance, c1, c2, random);
        }
        return children;
    }

    private Chromosome mutate(Chromosome chromosome) {
        if (mutationType == MutationType.INSERTION) {
            return Mutation.insertion(instance, chromosome, random);
        } else if (mutationType == MutationType.RECIPROCAL_EXCHANGE) {
            return Mutation.reciprocalExchange(instance, chromosome, random);
        } else if (mutationType == MutationType.SCRAMBLE) {
            return Mutation.scrambleMutation(instance, chromosome, random);
        }
        return Mutation.insertion(instance, chromosome, random);
    }

    private List<Route> decodeChromosome(Chromosome chromosome) {
        List<Node> giantRoute = Stream.of(chromosome.getArray()).collect(Collectors.toList());
        BeasleyHeuristic heuristic = new BeasleyHeuristic(instance, giantRoute);
        return heuristic.solve();
    }

    public void printProperties () {
        System.out.println("----------Genetic Algorithm Properties----------");
        System.out.println("Number of Customers:   " + population.getFittest().getArray().length);
        System.out.println("Population Size:    " + population.size());
        System.out.println("Max. Generation:    " + maxGenerations);
        System.out.println("k Value:            " + k);
        System.out.println("Local Search Rate:  " + localSearchRate);
        System.out.println("Crossover Type:     " + crossoverType);
        System.out.println("Crossover Rate:     " + (crossoverRate * 100) + "%");
        System.out.println("Mutation Type: " + mutationType);
        System.out.println("Mutation Rate: " + (mutationRate * 100) + "%");
    }

    public void printResults () {

        if (!finished) {
            throw new IllegalArgumentException("Genetic algorithm was never run.");
        }

        System.out.println("-----------Genetic Algorithm Results------------");
        System.out.println("Average Distance of First Generation:  " +
                getAverageDistanceOfFirstGeneration());
        System.out.println("Average Distance of Last Generation:   " +
                getAverageDistanceOfLastGeneration());
        System.out.println("Best Distance of First Generation:     " +
                getBestDistanceOfFirstGeneration());
        System.out.println("Best Distance of Last Generation:      " +
                getBestDistanceOfLastGeneration());
        System.out.println("Area Under Average Distance:           " +
                getAreaUnderAverageDistances());
        System.out.println("Area Under Average Distance:           " +
                getAreaUnderBestDistances());
    }

    public Solution getSolution() {
        if (!finished) {
            throw new IllegalArgumentException("Genetic algorithm was never run.");
        }
        List<Route> routes = decodeChromosome(population.getFittest());
        Solution solution = new Solution(instance, Algorithm.GA, routes, getBestDistanceOfLastGeneration(), timeTaken);
        return new TabuSearch(instance).execute(solution);
    }

    public enum CrossoverType {
        UNIFORM_ORDER,
        ONE_POINT,
        TWO_POINT
    }

    public enum MutationType {
        INSERTION,
        RECIPROCAL_EXCHANGE,
        SCRAMBLE
    }
}
