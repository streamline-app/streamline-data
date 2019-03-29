package org.thomaschen.streamlinedata.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.thomaschen.streamlinedata.exceptions.InvalidArithmeticException;
import org.thomaschen.streamlinedata.exceptions.ResourceNotFoundException;
import org.thomaschen.streamlinedata.model.TaskData;
import org.thomaschen.streamlinedata.model.UserData;
import org.thomaschen.streamlinedata.repository.TaskDataRepository;
import org.thomaschen.streamlinedata.repository.UserDataRepository;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
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

        if (taskData.getExpDuration() == 0) {
            throw new InvalidArithmeticException("TaskData", "expDuration", "0");
        }

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

    // Get TaskData Points
    @GetMapping("/{id}/tasks/timeseries")
    public String getUserTimeSeriesData(@PathVariable(value = "id") UUID id,
                                        @RequestParam(value="tags", required=false) String tag)  {
        UserData taskOwner = userDataRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("UserData", "id", id));

        List<TaskData> tasks = null;
        if (tag == null) {
            tasks = taskDataRepository.findAllByOwnerOrderByCreatedAt(taskOwner);
        } else {
            tasks =  taskDataRepository.findAllByOwnerAndTagsOrderByCreatedAt(taskOwner, tag);
        }

        if (tasks == null) {
            return "[]";
        }

        Double runningEstFactor = 0.0;
        Integer totalTasks = 0;

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode childNodes = mapper.createArrayNode();
        for (TaskData taskData : tasks) {
            JsonNode element = mapper.createObjectNode();

            Double currTaskEstFactor = (double) taskData.getActualDuration() / (double) taskData.getExpDuration();
            runningEstFactor = (runningEstFactor * totalTasks + currTaskEstFactor) /
                    (totalTasks + 1);

            ((ObjectNode) element).put("value", runningEstFactor);

            String strDate = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            strDate = sdf.format(taskData.getCreatedAt().getTime());

            ((ObjectNode) element).put("name", strDate);
            childNodes.add(element);

            totalTasks++;
        }

        String timeseries = "";
        try {
            timeseries = mapper.writeValueAsString(childNodes);
        } catch (JsonProcessingException jpe) {
            System.err.println(jpe.toString());
        }
        return timeseries;
    }

    @PostMapping("/{id}/predictions")
    public Double getNewTaskPrediction(@PathVariable(value = "id") UUID id,
                                       @Valid @RequestBody TaskData taskData) {
        UserData userData = userDataRepository.findById(id)
                .orElseThrow( () -> new ResourceNotFoundException("UserData", "id", id));

        return taskData.getExpDuration() + userData.getAvgTaskTime() / 2;
    }

    // Get UUID from name
    @GetMapping("/identity/{name}")
    public UUID getIdFromName(@PathVariable(value = "name") String userId) {
        UserData userData = userDataRepository.findByUserId(userId);
        return userData.getId();
    }

    // Delete all users
    @DeleteMapping("/")
    public ResponseEntity<?> deleteAllUsers() {
        List<UserData> allUsers = userDataRepository.findAll();

        for (UserData user : allUsers) {
            userDataRepository.delete(user);
        }

        return ResponseEntity.ok().build();
    }
}
