package org.thomaschen.streamlinedata.api;

import org.apache.catalina.User;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.thomaschen.streamlinedata.exceptions.ResourceNotFoundException;
import org.thomaschen.streamlinedata.model.TaskData;
import org.thomaschen.streamlinedata.model.UserData;
import org.thomaschen.streamlinedata.repository.TaskDataRepository;
import org.thomaschen.streamlinedata.repository.UserDataRepository;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserDataController {

    @Autowired
    UserDataRepository userDataRepository;

    @Autowired
    TaskDataRepository taskDataRepository;

    // Get all UserDatas
    @GetMapping("/")
    public List<UserData> getAllUsers() {
        return userDataRepository.findAll();
    }

    // Create new UserData
    @PostMapping("/")
    public UserData createUserData(@Valid @RequestBody UserData userData) {
        return userDataRepository.save(userData);
    }

    // Update UserData using UUID
    @PutMapping("/{id}")
    public UserData updateUserData(@PathVariable(value = "id") UUID id,
                                @Valid @RequestBody UserData userDataDetails) {
        UserData userData = userDataRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("UserData", "id", id));

        userData.setTaskEstFactor(userDataDetails.getTaskEstFactor());
        userData.setTotalOverTasks(userDataDetails.getTotalOverTasks());
        userData.setTotalUnderTasks(userDataDetails.getTotalUnderTasks());
        userData.setTotalTasksCompleted(userDataDetails.getTotalTasksCompleted());

        UserData updatedUserData = userDataRepository.save(userData);
        return updatedUserData;
    }

    // Get Specifc UserData using UUID
    @GetMapping("/{id}")
    public UserData getUserDataById(@PathVariable(value = "id") UUID id,
                                    @RequestParam(value="tags", required=false) String tag) {

        UserData userData = userDataRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("UserData", "id", id));

        if (tag == null) {
            return userData;
        } else {
            List<TaskData> tasksWithTag = taskDataRepository.findAllByOwnerAndTags(userData, tag);
            return UserData.calcUserData(tasksWithTag,
                    "Subset Statistics for " + userData.getUserId() + "'s Tasks with Tag: " + tag);
        }
    }

    // Delete Specific UserData using id
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUserData(@PathVariable(value = "id") UUID id) {
        UserData userData = userDataRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("UserData", "id", id));

        userDataRepository.delete(userData);

        return ResponseEntity.ok().build();
    }

    // Create New Task for a specific UserData
    @PostMapping("/{id}/tasks")
    public TaskData createTaskData(@PathVariable(value = "id") UUID id,
                                   @Valid @RequestBody TaskData taskData) {
        UserData userData = userDataRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("UserData", "id", id));

        userData.addTaskData(taskData);
        taskData.setOwner(userData);

        TaskData newTaskData = taskDataRepository.save(taskData);
        UserData upatedUserData = userDataRepository.save(userData);

        return newTaskData;
    }

    // Get TaskData entities owned by UserData with id
    @GetMapping("/{id}/tasks")
    public List<TaskData> getAllTaskData(@PathVariable(value = "id") UUID id,
                                         @RequestParam(value="tags", required=false) String tag) {
        UserData taskOwner = userDataRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("UserData", "id", id));

        if (tag == null) {
            return taskDataRepository.findAllByOwner(taskOwner);
        } else {
            return taskDataRepository.findAllByOwnerAndTags(taskOwner, tag);
        }
    }

    // Get UUID from name
    @GetMapping("/identity/{name}")
    public UUID getIdFromName(@PathVariable(value = "name") String userId) {
        UserData userData = userDataRepository.findByUserId(userId);
        return userData.getId();
    }
}
