package uaic.fii.control.service;

import uaic.fii.model.Instance;

import java.io.IOException;
import java.util.List;

public interface InstanceService {
    List<Instance> getAll() throws IOException;
    Instance getInstanceByName(String name) throws IOException;
}
