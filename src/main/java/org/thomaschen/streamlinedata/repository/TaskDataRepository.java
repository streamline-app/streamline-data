package org.thomaschen.streamlinedata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.thomaschen.streamlinedata.model.TaskData;
import org.thomaschen.streamlinedata.model.UserData;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaskDataRepository extends JpaRepository<TaskData, UUID> {
    public List<TaskData> findAllByOwner(UserData owner);
    public List<TaskData> findAllByOwnerAndTags(UserData owner, String tag);
    public List<TaskData> findAllByOwnerOrderByCreatedAt(UserData owner);
    public List<TaskData> findAllByOwnerAndTagsOrderByCreatedAt(UserData owner, String tag);
}
