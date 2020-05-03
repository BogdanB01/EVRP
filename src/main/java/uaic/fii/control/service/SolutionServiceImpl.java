package uaic.fii.control.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import uaic.fii.model.EVRPTWInstance;
import uaic.fii.model.Solution;
import uaic.fii.solver.verifier.RoutesLoader;
import uaic.fii.util.Algorithm;

import java.io.File;
import java.io.IOException;

@Service
public class SolutionServiceImpl implements SolutionService {

    private InstanceService instanceService;

    private static final ClassPathResource SOLUTIONS = new ClassPathResource("/solution");

    @Autowired
    public SolutionServiceImpl(InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    @Override
    public Solution getSolutionByNameAndAlgorithm(String name, Algorithm algorithm) throws IOException {
        EVRPTWInstance instance = instanceService.getInstanceByName(name);
        String path = String.join("/", SOLUTIONS.getPath(), algorithm.getSaveLocation(), name) + ".txt";

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        File file = new File(classloader.getResource(path).getFile());

        return RoutesLoader.create(instance).load(file);
    }
}
