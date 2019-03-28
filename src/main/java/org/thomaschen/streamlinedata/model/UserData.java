package org.thomaschen.streamlinedata.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@EnableScheduling
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"},
        allowGetters = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class UserData {

    /**
     * Unique identifier for a user.
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    @ApiModelProperty(hidden = true)
    private UUID id;

    /**
     * Streamline user ID
     */
    @NotNull
    @Column(unique = true)
    private String userId;

    /**
     * Creation Date/Time of the task.
     */
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    @ApiModelProperty(hidden = true)
    private Calendar createdAt;

    /**
     * Last Modified Date/time
     */
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @ApiModelProperty(hidden = true)
    private Calendar updatedAt;

    /**
     * Total Tasks Completed by the User
     */
    @NotNull(message = "The above field must not be omitted.")
    @Min(value = 0, message = "The value must be positive")
    private Integer totalTasksCompleted = 0;

    /**
     * Total Tasks Completed under time by the User
     */
    @NotNull(message = "The above field must not be omitted.")
    @Min(value = 0, message = "The value must be positive")
    private Integer totalUnderTasks = 0;

    /**
     * Total Tasks Completed over time by the User
     */
    @NotNull(message = "The above field must not be omitted.")
    @Min(value = 0, message = "The value must be positive")
    private Integer totalOverTasks = 0;

    /**
     * Global Task Estimation Factor for the user
     */
    @NotNull(message = "The above field must not be omitted.")
    private Double taskEstFactor = 0.0;

    /**
     * Global Task Estimation Factor for the user
     */
    @NotNull(message = "The above field must not be omitted.")
    private Double avgTaskTime = 0.0;

    /**
     * Hashmap of all messages currently on Board
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "owner", fetch = FetchType.EAGER)
    @MapKey(name = "taskId")
    private Map<UUID, TaskData> tasks;

    /**
     * No Param Constructor
     */
    public UserData() {

    }

    /**
     * Full Constructor for User Data Point
     * @param userId the streamline userId of the user
     * @param totalTasksCompleted total number tasks completed by user
     * @param totalUnderTasks total number of tasks completed under time by user
     * @param totalOverTasks total number of tasks complted over time by user
     * @param taskEstFactor global estimation rating for user
     */
    public UserData(String userId,
                    Integer totalTasksCompleted,
                    Integer totalUnderTasks,
                    Integer totalOverTasks,
                    Double taskEstFactor,
                    Double avgTaskTime) {
        this.id = UUID.randomUUID();

        this.userId = userId;
        this.totalTasksCompleted = totalTasksCompleted;
        this.totalUnderTasks = totalUnderTasks;
        this.totalOverTasks = totalOverTasks;
        this.taskEstFactor = taskEstFactor;
        this.avgTaskTime = avgTaskTime;

        this.createdAt = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.updatedAt = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        this.tasks = new HashMap<>();
    }

    /**
     * Calculates a UserData object from a List of Tasks
     * @param tasks the tasks to be included
     * @return a UserData object aggregating list of tasks
     */
    public static UserData calcUserData(List<TaskData> tasks, String statName) {
        UserData temp = new UserData(statName, 0,0,0,0.0,0.0);

        for (TaskData task : tasks) {
            temp.addTaskData(task);
        }

        return temp;
    }

    /**
     * Adds a single task's data to the UserData aggregate statistics
     * @param taskData the task data to be added
     */
    public void addTaskData(TaskData taskData) {
        // Calculate new average task completion time
        Double lastSumTime = this.totalTasksCompleted * this.avgTaskTime;
        this.avgTaskTime = (lastSumTime + taskData.getActualDuration()) / (this.totalTasksCompleted + 1);

        Double currTaskEstFactor = (double) taskData.getActualDuration() / (double) taskData.getExpDuration();
        this.taskEstFactor = (this.taskEstFactor * this.totalTasksCompleted + currTaskEstFactor) /
                (this.totalTasksCompleted + 1);


        if (taskData.getActualDuration() > taskData.getExpDuration()) {
            this.totalOverTasks++;
        } else {
            this.totalUnderTasks++;
        }

        this.totalTasksCompleted++;

    }

    /**
     * Removes a single task's data from the UserData aggregate statistics
     * @param taskData the task data to be removed
     */
    public void subtractTaskData(TaskData taskData) {
        if (this.totalTasksCompleted == 1) {
            this.avgTaskTime = 0.0;
            this.totalUnderTasks = 0;
            this.totalOverTasks = 0;
            this.totalTasksCompleted = 0;
            this.taskEstFactor = 0.0;
            return;
        }

        Double lastAvgTimeSum = this.avgTaskTime * (double) this.totalTasksCompleted;
        this.avgTaskTime = (lastAvgTimeSum - taskData.getActualDuration()) / (this.totalTasksCompleted - 1);

        Double lastTaskEstFactorSum = this.taskEstFactor * (double) this.totalTasksCompleted;
        this.taskEstFactor = (lastTaskEstFactorSum - (taskData.getActualDuration()/taskData.getExpDuration())) /
                (this.totalTasksCompleted - 1);

        if (taskData.getActualDuration() > taskData.getExpDuration()) {
            this.totalOverTasks--;
        } else {
            this.totalUnderTasks--;
        }

        this.totalTasksCompleted--;
    }

    /**
     * Subtracts then Deletes task's data from UserData
     * @param taskData the task data to be removed
     */
    public void removeTaskData(TaskData taskData) {
        this.subtractTaskData(taskData);
        this.tasks.remove(taskData.getTaskId());
    }

    public UUID getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Calendar getCreatedAt() {
        return createdAt;
    }

    public Calendar getUpdatedAt() {
        return updatedAt;
    }

    public Integer getTotalTasksCompleted() {
        return totalTasksCompleted;
    }

    public Integer getTotalUnderTasks() {
        return totalUnderTasks;
    }

    public Integer getTotalOverTasks() {
        return totalOverTasks;
    }

    public Double getTaskEstFactor() {
        return taskEstFactor;
    }

    public Double getAvgTaskTime() {
        return avgTaskTime;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUpdatedAt(Calendar updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setTotalTasksCompleted(Integer totalTasksCompleted) {
        this.totalTasksCompleted = totalTasksCompleted;
    }

    public void setTotalUnderTasks(Integer totalUnderTasks) {
        this.totalUnderTasks = totalUnderTasks;
    }

    public void setTotalOverTasks(Integer totalOverTasks) {
        this.totalOverTasks = totalOverTasks;
    }

    public void setTaskEstFactor(Double taskEstFactor) {
        this.taskEstFactor = taskEstFactor;
    }

    public void setAvgTaskTime(Double avgTaskTime) {
        this.avgTaskTime = avgTaskTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserData that = (UserData) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getUserId(), that.getUserId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getUserId());
    }

    @Override
    public String toString() {
        return "UserData{" +
                "id=" + id +
                ",\n userId='" + userId + '\'' +
                ",\n totalTasksCompleted=" + totalTasksCompleted +
                ",\n totalUnderTasks=" + totalUnderTasks +
                ",\n totalOverTasks=" + totalOverTasks +
                ",\n taskEstFactor=" + taskEstFactor +
                ",\n avgTaskTime=" + avgTaskTime +
                ",\n tasks=" + tasks +
                '}';
    }
}
