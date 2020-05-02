package uaic.fii.solver.ga;

import java.util.*;

public class Selection {

    // There is a 1 in 5 chance that fittest individual is not selected.
    private static final int ODDS_OF_NOT_PICKING_FITTEST = 5;

    private Selection() {}

    public static Chromosome tournamentSelection(Population population, int k, Random random) {
        List<Chromosome> kChromosomes = getKChromosomes(population.getChromosomes(), k, random);
        return getChromosome(kChromosomes, random);
    }

    private static List<Chromosome> getKChromosomes(List<Chromosome> pop, int k, Random random) {
        List<Chromosome> kChromosomes = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            Chromosome chromosome = pop.get(random.nextInt(pop.size()));
            kChromosomes.add(chromosome);
        }
        return kChromosomes;
    }

    private static Chromosome getChromosome(List<Chromosome> kChromosomes, Random random) {
        Chromosome bestChromosome = getBestChromosome(kChromosomes);
        // 1 in 5 chance to return a chromosome that is not the best.
        if (random.nextInt(ODDS_OF_NOT_PICKING_FITTEST) == 0 && kChromosomes.size() != 1) {
            kChromosomes.remove(bestChromosome);
            return kChromosomes.get(random.nextInt(kChromosomes.size()));
        }

        return bestChromosome;
    }

    private static Chromosome getBestChromosome(List<Chromosome> kChromosomes) {
        return kChromosomes.stream()
                .min(Comparator.comparing(Chromosome::getDistance))
                .orElseThrow(NoSuchElementException::new);
    }
}


















