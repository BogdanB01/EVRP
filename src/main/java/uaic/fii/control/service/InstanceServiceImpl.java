package uaic.fii.control.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import uaic.fii.model.Instance;
import uaic.fii.solver.model.EVRPTWInstance;
import uaic.fii.solver.verifier.SchneiderLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstanceServiceImpl implements InstanceService {

    private static final ClassPathResource INSTANCES = new ClassPathResource("input");

    @Override
    public List<Instance> getAll() throws IOException {
        return Files.walk(Paths.get(INSTANCES.getPath()))
                .filter(Files::isRegularFile)
                .map(this::mapToInstance)
                .collect(Collectors.toList());
    }

    @Override
    public Instance getInstanceByName(String name) throws IOException {
        String path = String.format("%s/%s.txt", INSTANCES.getPath(), name);
        EVRPTWInstance evrptwInstance = new SchneiderLoader().load(new File(path));

        Instance instance = mapToInstance(Paths.get(path));
        instance.setCustomers(evrptwInstance.getCustomers());
        instance.setRechargingStations(evrptwInstance.getRechargingStations());
        instance.setDepot(evrptwInstance.getDepot());

        return instance;
    }

    private Instance mapToInstance(Path path) {
        String fileName = path.getFileName().toString();
        int position = fileName.lastIndexOf(".");
        String instanceName = position != -1 ? fileName.substring(0, position) : fileName;
        return new Instance(instanceName, path.toString());
    }
}
