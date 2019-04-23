package org.thomaschen.streamlinedata.api;

import javafx.concurrent.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.thomaschen.streamlinedata.exceptions.ResourceNotFoundException;
import org.thomaschen.streamlinedata.model.TaskData;
import org.thomaschen.streamlinedata.model.UserData;
import org.thomaschen.streamlinedata.repository.TaskDataRepository;
import org.thomaschen.streamlinedata.repository.UserDataRepository;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    TaskDataRepository taskDataRepository;

    @Autowired
    UserDataRepository userDataRepository;

    // Get all Task Datas
    @GetMapping("/")
    public List<TaskData> getAllTasks() {
        return taskDataRepository.findAll();
    }

    // Update UserData using UUID
    @PutMapping("/{id}")
    public TaskData updateTaskData(@PathVariable(value = "id") UUID id,
                           @Valid @RequestBody TaskData taskDataDetails) {
        TaskData taskData = taskDataRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("TaskData", "id", id));

        UserData userData = userDataRepository.findById(taskData.getOwner().getId())
                .orElseThrow( () -> new ResourceNotFoundException("UserData", "id", id));

        // Remove old taskData impact on userData
        userData.subtractTaskData(taskData);

        taskData.setActualDuration(taskDataDetails.getActualDuration());
        taskData.setExpDuration(taskDataDetails.getExpDuration());
        taskData.setOwner(userData);
        taskData.setTags(taskDataDetails.getTags());

        // Add new taskData impact on userData
        userData.addTaskData(taskData);

        userDataRepository.save(userData);
        TaskData updatedTaskData = taskDataRepository.save(taskData);
        return updatedTaskData;
    }

    // Get Specific TaskData using UUID
    @GetMapping("/{id}")
    public TaskData getTaskDataById(@PathVariable(value = "id") UUID id) {
        return taskDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaskData", "id", id));
    }

    // Get Specific TaskData using UUID
    @GetMapping("/{id}/tagmask")
    public double[] getTagMaskById(@PathVariable(value = "id") UUID id) {
        TaskData taskData = taskDataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaskData", "id", id));

        return taskData.getOwner().calcTaskTagMask(taskData);
    }

    // Delete Specific TaskData using UUID
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTaskData(@PathVariable(value = "id") UUID id) {
        TaskData taskData = taskDataRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("TaskData", "id", id));

        UserData userData = userDataRepository.findById(taskData.getOwner().getId())
                .orElseThrow( () -> new ResourceNotFoundException("UserData", "id", id));

        userData.removeTaskData(taskData);

        userDataRepository.save(userData);
        taskDataRepository.delete(taskData);

        return ResponseEntity.ok().build();
    }
}
