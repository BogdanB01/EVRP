package uaic.fii.boundry.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import uaic.fii.control.service.InstanceService;
import uaic.fii.model.Instance;
import uaic.fii.solver.model.EVRPTWInstance;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "api/instances")
@CrossOrigin
public class InstanceController {

    private InstanceService instanceService;

    @Autowired
    public InstanceController(InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    @GetMapping
    public List<Instance> getAll() throws IOException {
        return instanceService.getAll();
    }

    @GetMapping(value = "/filter")
    public Instance getInstanceByName(@RequestParam(name = "name") String name) throws IOException {
        return instanceService.getInstanceByName(name);
    }

}
