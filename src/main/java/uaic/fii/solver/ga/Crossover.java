package uaic.fii.solver.ga;

import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Node;

import java.util.*;

public class Crossover {

    private Crossover() {}

    static List<Chromosome> uniformOrder (EVRPTWInstance instance, Chromosome p1, Chromosome p2, Random r) {

        Node[] parent1 = p1.getArray();
        Node[] parent2 = p2.getArray();

        Node[] child1 = new Node[parent1.length];
        Node[] child2 = new Node[parent2.length];

        Set<Node> customersInChild1 = new HashSet<>();
        Set<Node> customersInChild2 = new HashSet<>();

        List<Node> customersNotInChild1 = new ArrayList<>();
        List<Node> customersNotInChild2 = new ArrayList<>();

        List<Chromosome> children = new ArrayList<>();

        int[] bitMask = generateBitMask(parent1.length, r);

        // Inherit the cities of the same parent where the bit-mask is 1.
        // Example: child 1 has all the same cities as parent 1 at the indexes where the bit-mask is 1.
        for (int i = 0; i < bitMask.length; i++) {
            if (bitMask[i] == 1) {
                child1[i] = parent1[i];
                child2[i] = parent2[i];
                customersInChild1.add(parent1[i]);
                customersInChild2.add(parent2[i]);
            }
        }

        // Get the customers of the opposite parent if the child does not already contain them.
        for (int i = 0; i < child1.length; i++) {
            if (child1[i] == null && !customersInChild1.contains(parent2[i])) {
                child1[i] = parent2[i];
                customersInChild1.add(parent2[i]);
            } else if (child1[i] != null && !customersInChild1.contains(parent2[i])) {
                customersNotInChild1.add(parent2[i]);
            }
            if (child2[i] == null && !customersInChild2.contains(parent1[i])) {
                child2[i] = parent1[i];
                customersInChild2.add(parent1[i]);
            } else if (child2[i] != null && !customersInChild2.contains(parent1[i])) {
                customersNotInChild2.add(parent1[i]);
            }
        }

        // Fill in the blanks.
        for (int i = 0; i < child1.length; i++) {
            if (child1[i] == null) {
                child1[i] = customersNotInChild1.remove(0);
            }
            if (child2[i] == null) {
                child2[i] = customersNotInChild2.remove(0);
            }
        }

        if (!customersNotInChild1.isEmpty() || !customersNotInChild2.isEmpty()) {
            throw new AssertionError("Lists should be empty.");
        }

        Chromosome childOne = new Chromosome(instance, child1);
        Chromosome childTwo = new Chromosome(instance, child2);
        children.add(childOne);
        children.add(childTwo);

        return children;
    }

    private static int[] generateBitMask (int size, Random random) {

        int[] array = new int[size];

        for (int i = 0; i < array.length; i++) {
            array[i] = (random.nextInt(2) == 0) ? 0 : 1;
        }

        return array;
    }

    static List<Chromosome> onePointCrossover (EVRPTWInstance instance, Chromosome p1, Chromosome p2, Random r) {
        Node[] parent1 = p1.getArray();
        Node[] parent2 = p2.getArray();

        Node[] child1 = new Node[parent1.length];
        Node[] child2 = new Node[parent2.length];

        Set<Node> customersInChild1 = new HashSet<>();
        Set<Node> customersInChild2 = new HashSet<>();

        List<Node> customersNotInChild1 = new ArrayList<>();
        List<Node> customersNotInChild2 = new ArrayList<>();

        List<Chromosome> children = new ArrayList<>();
        int totalCustomers = parent1.length;

        int randomPoint = r.nextInt(totalCustomers);

        // Inherit the customers up to the point.
        for (int i = 0; i < randomPoint; i++) {
            child1[i] = parent1[i];
            child2[i] = parent2[i];
            customersInChild1.add(parent1[i]);
            customersInChild2.add(parent2[i]);
        }

        // Get the customers of the opposite parent if the child does not already contain them.
        for (int i = randomPoint; i < totalCustomers; i++) {
            if ( ! customersInChild1.contains(parent2[i])) {
                customersInChild1.add(parent2[i]);
                child1[i] = parent2[i];
            }
            if (!customersInChild2.contains(parent1[i])) {
                customersInChild2.add(parent1[i]);
                child2[i] = parent1[i];
            }
        }

        // Find all the customers that are still missing from each child.
        for (int i = 0; i < totalCustomers; i++) {
            if ( ! customersInChild1.contains(parent2[i])) {
                customersNotInChild1.add(parent2[i]);
            }
            if (!customersInChild2.contains(parent1[i])) {
                customersNotInChild2.add(parent1[i]);
            }
        }

        // Find which spots are still empty in each child.
        List<Integer> emptySpotsC1 = new ArrayList<>();
        List<Integer> emptySpotsC2 = new ArrayList<>();
        for (int i = 0; i < totalCustomers; i++) {
            if (child1[i] == null) {
                emptySpotsC1.add(i);
            }
            if (child2[i] == null) {
                emptySpotsC2.add(i);
            }
        }

        // Fill in the empty spots.
        for (Node node: customersNotInChild1) {
            child1[emptySpotsC1.remove(0)] = node;
        }
        for (Node node: customersNotInChild2) {
            if (emptySpotsC2.size() == 0) {
                String test = "";
            }
            child2[emptySpotsC2.remove(0)] = node;

        }

        Chromosome childOne = new Chromosome(instance, child1);
        Chromosome childTwo = new Chromosome(instance, child2);
        children.add(childOne);
        children.add(childTwo);

        return children;
    }

    static List<Chromosome> orderCrossover (EVRPTWInstance instance, Chromosome c1, Chromosome c2, Random random) {
        Node[] parent1 = c1.getArray();
        Node[] parent2 = c2.getArray();

        Node[] child1 = new Node[parent1.length];
        Node[] child2 = new Node[parent2.length];

        HashSet<Node> customersInChild1 = new HashSet<>();
        HashSet<Node> customersInChild2 = new HashSet<>();

        List<Node> customersNotInChild1 = new ArrayList<>();
        List<Node> customersNotInChild2 = new ArrayList<>();

        List<Chromosome> children = new ArrayList<>();
        int totalCustomers = parent1.length;

        int firstPoint = random.nextInt(totalCustomers);
        int secondPoint = random.nextInt(totalCustomers - firstPoint) + firstPoint;

        // Inherit the customers before and after the points selected.
        for (int i = 0; i < firstPoint; i++) {
            child1[i] = parent1[i];
            child2[i] = parent2[i];
            customersInChild1.add(parent1[i]);
            customersInChild2.add(parent2[i]);
        }
        for (int i = secondPoint; i < totalCustomers; i++) {
            child1[i] = parent1[i];
            child2[i] = parent2[i];
            customersInChild1.add(parent1[i]);
            customersInChild2.add(parent2[i]);
        }

        // Get the cities of the opposite parent if the child does not already contain them.
        for (int i = firstPoint; i < secondPoint; i++) {
            if (!customersInChild1.contains(parent2[i])) {
                customersInChild1.add(parent2[i]);
                child1[i] = parent2[i];
            }
            if (!customersInChild2.contains(parent1[i])) {
                customersInChild2.add(parent1[i]);
                child2[i] = parent1[i];
            }
        }

        // Find all the cities that are still missing from each child.
        for (int i = 0; i < totalCustomers; i++) {
            if (!customersInChild1.contains(parent2[i])) {
                customersNotInChild1.add(parent2[i]);
            }
            if (!customersInChild2.contains(parent1[i])) {
                customersNotInChild2.add(parent1[i]);
            }
        }

        // Find which spots are still empty in each child.
        List<Integer> emptySpotsC1 = new ArrayList<>();
        List<Integer> emptySpotsC2 = new ArrayList<>();
        for (int i = 0; i < totalCustomers; i++) {
            if (child1[i] == null) {
                emptySpotsC1.add(i);
            }
            if (child2[i] == null) {
                emptySpotsC2.add(i);
            }
        }

        // Fill in the empty spots.
        for (Node node : customersNotInChild1) {
            child1[emptySpotsC1.remove(0)] = node;
        }
        for (Node city : customersNotInChild2) {
            child2[emptySpotsC2.remove(0)] = city;
        }

        Chromosome childOne = new Chromosome(instance, child1);
        Chromosome childTwo = new Chromosome(instance, child2);
        children.add(childOne);
        children.add(childTwo);

        return children;
    }
}

