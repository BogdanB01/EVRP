package uaic.fii.control.service;

import uaic.fii.model.Solution;
import uaic.fii.util.Algorithm;

import java.io.IOException;

public interface SolutionService {
    Solution getSolutionByNameAndAlgorithm(String name, Algorithm algorithm) throws IOException;
}
