package uaic.fii.solver.ga.search;

import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Solution;
import uaic.fii.solver.ga.search.neighbourhood.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class TabuSearch {

    private static final int TABU_ITERATIONS = 100;
    private EVRPTWInstance instance;

    public TabuSearch(EVRPTWInstance instance) {
        this.instance = instance;
    }

    public Solution execute(Solution solution) {
        Solution overallBestSolution = solution;
        Solution bestCandidate = overallBestSolution;
        Map<Solution, Integer> tabuMap = new HashMap<>();
        tabuMap.put(bestCandidate, 1);

        int iteration = 0;
        while (iteration < TABU_ITERATIONS) {
            bestCandidate = bestSolutionOfNeighbourhoods(bestCandidate, tabuMap);
            updateTabuMap(bestCandidate, tabuMap);

            if (bestCandidate.getCost() < overallBestSolution.getCost()) {
                overallBestSolution = bestCandidate;
            }
            iteration++;
        }
        return overallBestSolution;
    }

    private Solution bestSolutionOfNeighbourhoods(Solution solution, Map<Solution, Integer> tabuMap) {
        List<Solution> neighbours = getNeighbourhoods(solution);
        neighbours.removeIf(tabuMap::containsKey);
        if (neighbours.isEmpty()) {
            System.out.println("No solutions available");
            return solution;
        }
        return neighbours.stream()
                .min(Comparator.comparing(Solution::getCost))
                .orElseThrow(NoSuchElementException::new);
    }

    private List<Solution> getNeighbourhoods(Solution solution) {

        List<CompletableFuture<List<Solution>>> futures = Arrays.asList(
                CompletableFuture.supplyAsync(() -> new InterRouteExchange(instance).generate(solution)),
                CompletableFuture.supplyAsync(() -> new InterRouteRelocate(instance).generate(solution)),
                CompletableFuture.supplyAsync(() -> new StationInRe(instance).generate(solution)),
                CompletableFuture.supplyAsync(() -> new TwoOptArcExchange(instance).generate(solution)),
                CompletableFuture.supplyAsync(() -> new TwoOrOpt(instance).generate(solution)),
                CompletableFuture.supplyAsync(() -> new MoveStation(instance).generate(solution))
        );

        CompletableFuture<List<List<Solution>>> result = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(__ -> futures
                        .stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );

        try {
            List<Solution> neighbours = result.get().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            return neighbours;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        throw new RuntimeException("Error occurred exploring neighbours");
        //List<Solution> neighbours = new ArrayList<>();

        /*CompletableFuture<List<Solution>> interRouteExchangeNeighbours = CompletableFuture
                .supplyAsync(() -> new InterRouteExchange(instance).generate(solution));
        CompletableFuture<List<Solution>> interRouteRelocateNeighbours = CompletableFuture
                .supplyAsync(() -> new InterRouteRelocate(instance).generate(solution));
        CompletableFuture<List<Solution>> stationInReNeighbours = CompletableFuture
                .supplyAsync(() -> new StationInRe(instance).generate(solution));
        CompletableFuture<List<Solution>> twoOptArcExchangeNeighbours = CompletableFuture
                .supplyAsync(() -> new TwoOptArcExchange(instance).generate(solution));
        CompletableFuture<List<Solution>> twoOrOptNeighbours = CompletableFuture
                .supplyAsync(() -> new TwoOrOpt(instance).generate(solution));
        CompletableFuture<List<Solution>> moveStation = CompletableFuture
                .supplyAsync(() -> new MoveStation(instance).generate(solution));

        CompletableFuture.allOf(interRouteExchangeNeighbours, interRouteRelocateNeighbours,
                stationInReNeighbours, twoOptArcExchangeNeighbours, twoOrOptNeighbours, moveStation)
                .join();
*/

        /*
        neighbours.addAll(new InterRouteExchange(instance).generate(solution));
        neighbours.addAll(new InterRouteRelocate(instance).generate(solution));
        neighbours.addAll(new StationInRe(instance).generate(solution));
        neighbours.addAll(new TwoOptArcExchange(instance).generate(solution));
        neighbours.addAll(new TwoOrOpt(instance).generate(solution));
        neighbours.addAll(new MoveStation(instance).generate(solution));*/
      //  return neighbours;
    }

    private void updateTabuMap(Solution solution, Map<Solution, Integer> tabuMap) {
        // remove elements older than some constant
        tabuMap.values().removeIf(value -> value >= 15);
        tabuMap.put(solution, 0);
        tabuMap.replaceAll((k, v) -> v += 1);
    }
}
