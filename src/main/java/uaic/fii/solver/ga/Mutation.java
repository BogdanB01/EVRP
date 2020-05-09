package uaic.fii.solver.ga;

import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Node;

import java.util.Random;

public class Mutation {

    private Mutation () {}

    static Chromosome insertion (EVRPTWInstance instance, Chromosome chromosome, Random random) {
        Node[] customers = chromosome.getArray();
        int randomIndex = random.nextInt(customers.length);
        int randomDestination = random.nextInt(customers.length);

        if (randomIndex < randomDestination) {
            Node temp = customers[randomIndex];
            for (int i = randomIndex; i < randomDestination; i++) {
                customers[i] = customers[i + 1];
            }
            customers[randomDestination] = temp;
        } else {
            Node temp = customers[randomIndex];
            for (int i = randomIndex; i > randomDestination; i--) {
                customers[i] = customers[i-1];
            }
            customers[randomDestination] = temp;
        }
        return new Chromosome(instance, customers);
    }

    static Chromosome reciprocalExchange (EVRPTWInstance instance, Chromosome chromosome, Random random) {
        Node[] customers = chromosome.getArray();
        int l = customers.length;
        swap(customers, random.nextInt(l), random.nextInt(l));
        return new Chromosome(instance, customers);
    }

    static Chromosome scrambleMutation (EVRPTWInstance instance, Chromosome chromosome, Random random) {
        Node[] customers = chromosome.getArray();
        int randomIndexStart = random.nextInt(customers.length);
        int randomIndexEnd = random.nextInt(customers.length);

        for (int i = randomIndexStart; i % customers.length != randomIndexEnd; i++) {
            int r = random.nextInt(Math.abs(i % customers.length - randomIndexEnd));
            swap(customers, i % customers.length, (i + r) % customers.length);
        }

        return new Chromosome(instance, customers);
    }

    private static void swap (Node[] array, int i, int j) {
        Node temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

}
