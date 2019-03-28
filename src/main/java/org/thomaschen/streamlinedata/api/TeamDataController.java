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
@RequestMapping("/api/teams")
public class TeamDataController {

    @Autowired
    UserDataRepository userDataRepository;

    @Autowired
    TaskDataRepository taskDataRepository;

    // Create new UserData as unique Team
    @PostMapping("/")
    public UserData createUserDataAsTeam(@Valid @RequestBody UserData userData) {
        // Ensures unique userId
        userData.setUserId("t" + userData.getUserId());
        return userDataRepository.save(userData);
    }

    // Get UUID from name
    @GetMapping("/identity/{name}")
    public UUID getIdFromTeamName(@PathVariable(value = "name") String userId) {
        UserData userData = userDataRepository.findByUserId("t" + userId);
        return userData.getId();
    }
}
