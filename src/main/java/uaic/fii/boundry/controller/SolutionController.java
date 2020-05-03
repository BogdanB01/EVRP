package uaic.fii.boundry.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uaic.fii.control.service.SolutionService;
import uaic.fii.model.Solution;
import uaic.fii.util.Algorithm;

import java.io.IOException;

@RestController
@RequestMapping(value = "api/solutions")
@CrossOrigin
public class SolutionController {

    private SolutionService solutionService;

    @Autowired
    public SolutionController(SolutionService solutionService) {
        this.solutionService = solutionService;
    }

    @GetMapping(value = "/filter")
    public Solution getSolution(@RequestParam(name = "name") String name,
                                @RequestParam(name = "algorithm") String algorithm) throws IOException {
        return solutionService.getSolutionByNameAndAlgorithm(name, Algorithm.valueOf(algorithm));
    }
}
