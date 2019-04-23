package org.thomaschen.streamlinedata.api;

import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.thomaschen.streamlinedata.exceptions.ResourceNotFoundException;
import org.thomaschen.streamlinedata.model.ClusterService;
import org.thomaschen.streamlinedata.model.TaskData;
import org.thomaschen.streamlinedata.model.UserData;
import org.thomaschen.streamlinedata.repository.TaskDataRepository;
import org.thomaschen.streamlinedata.repository.UserDataRepository;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/data")
public class DataController {

    @Autowired
    UserDataRepository userDataRepository;

    @Autowired
    TaskDataRepository taskDataRepository;

    @GetMapping("/")
    public String index() {
        return "Hello, Welcome to the Streamline Data API";
    }

    // Get Specific Task Clusters using User UUID
    @PostMapping("/{id}/cluster")
    public List<CentroidCluster<TaskData>> getClusterData(@PathVariable(value = "id") UUID id,
                                                          @Valid @RequestBody TaskData taskData) {

        UserData owner = userDataRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("UserData", "id", id));

        List<TaskData> points = taskDataRepository.findAllByOwner(owner);

        if (points == null) {
            throw new ResourceNotFoundException("Tasks", "user", id);
        }

        return ClusterService.exeKmeansCluster(points, ClusterService.calcK(points.size()));
    }
}

