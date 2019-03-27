package org.thomaschen.streamlinedata.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@EnableScheduling
@JsonIgnoreProperties(value = {"createdAt", "owner"},
        allowGetters = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "taskId")
public class TaskData {

    /**
     * Unique identifier for message.
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    @ApiModelProperty(hidden = true)
    private UUID taskId;

    /**
     * Creation Date/Time of the task.
     */
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    @ApiModelProperty(hidden = true)
    private Calendar createdAt;

    /**
     * board that owns the message
     */
    @ManyToOne
    @ApiModelProperty(hidden = true)
    @JsonIdentityReference(alwaysAsId = true)
    private UserData owner;

    /**
     * Expected duration required to complete task.
     */
    @NotNull(message = "The above field must not be omitted.")
    @Min(value = 0L, message = "The value must be positive")
    private Long expDuration;

    /**
     * Actual duration required to complete task.
     */
    @NotNull(message = "The above field must not be omitted.")
    @Min(value = 0L, message = "The value must be positive")
    private Long actualDuration;

    /**
     * Queue of messages that have surpassed threshhold
     */
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> tags;

    // No Param Constructor
    public TaskData() {

    }

    /**
     * Constructor for Task Data
     * @param owner the owner of the task
     * @param expDuration expected duration of task
     * @param actualDuration actual duration
     * @param tags tags for the task
     */
    public TaskData(UserData owner,  Long expDuration, Long actualDuration, List<String> tags) {
        this.taskId = UUID.randomUUID();
        this.owner = owner;
        this.expDuration = expDuration;
        this.actualDuration = actualDuration;
        this.tags = tags;

        this.createdAt = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }

    public UUID getTaskId() {
        return taskId;
    }

    public Calendar getCreatedAt() {
        return createdAt;
    }

    public UserData getOwner() {
        return owner;
    }

    public Long getExpDuration() {
        return expDuration;
    }

    public Long getActualDuration() {
        return actualDuration;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setCreatedAt(Calendar createdAt) {
        this.createdAt = createdAt;
    }

    public void setOwner(UserData owner) {
        this.owner = owner;
    }

    public void setExpDuration(Long expDuration) {
        this.expDuration = expDuration;
    }

    public void setActualDuration(Long actualDuration) {
        this.actualDuration = actualDuration;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
