package org.thomaschen.streamlinedata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.thomaschen.streamlinedata.model.TaskData;
import org.thomaschen.streamlinedata.model.UserData;

import java.util.List;
import java.util.UUID;

public interface UserDataRepository extends JpaRepository<UserData, UUID> {
    public List<UserData> findByUserIdAndId(String userId, UUID id);
    public UserData findByUserId(String userId);
}
