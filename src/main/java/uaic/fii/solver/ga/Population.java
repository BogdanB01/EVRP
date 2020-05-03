package uaic.fii.solver.ga;

import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Node;

import java.nio.BufferOverflowException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

public class Population {

    private PriorityQueue<Chromosome> chromosomes;
    private EVRPTWInstance instance;
    private int maxSize;

    public Population(EVRPTWInstance instance, int maxSize) {
        this.instance = instance;
        this.maxSize = maxSize;
        chromosomes = new PriorityQueue<>();
    }

    public void add(Chromosome chromosome) {
        if (chromosomes.size() == maxSize) {
            throw new BufferOverflowException();
        }
        chromosomes.add(chromosome);
    }

    public List<Chromosome> getChromosomes() {
        return new ArrayList<>(chromosomes);
    }

    public Chromosome getFittest() {
        return chromosomes.peek();
    }

    public int size() {
        return chromosomes.size();
    }

    public Population deepCopy() {
        Population population = new Population(instance, maxSize);
        chromosomes.forEach(population::add);
        return population;
    }

    public double getAverageDistance() {
        double averageDistance = 0;
        for (Chromosome chromosome : chromosomes) {
            averageDistance += chromosome.getDistance();
        }
        return averageDistance / chromosomes.size();
    }

    public static Population getRandomPopulation(EVRPTWInstance instance, int populationSize, Random random) {
        Population population = new Population(instance, populationSize);

        Node[] customers = new Node[instance.getNumCustomers()];
        instance.getCustomers().toArray(customers);

        for (int i = 0; i < populationSize; i++) {
            population.add(new Chromosome(instance, customers, random));
        }
        return population;
    }
}

















