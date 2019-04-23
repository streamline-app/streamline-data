package org.thomaschen.streamlinedata.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@EnableScheduling
@JsonIgnoreProperties(value = {"createdAt", "owner"},
        allowGetters = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "taskId")
public class TaskData implements Clusterable, DistanceMeasure {

    @ApiModelProperty(name = "point", hidden = true)
    @JsonIgnoreProperties
    double[] point;

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
    private Set<String> tags;

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
    public TaskData(UserData owner, Long expDuration, Long actualDuration, Set<String> tags) {
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

    public Set<String> getTags() {
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

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "TaskData{" +
                "\npoint=" + Arrays.toString(this.getPoint()) +
                ",\ntaskId=" + this.getTaskId().toString() +
                ",\ncreatedAt=" + this.getCreatedAt().getTimeInMillis() +
                ",\nowner=" + this.getOwner().getId().toString() +
                ",\nexpDuration=" + this.getExpDuration() +
                ",\nactualDuration=" + this.getActualDuration() +
                ",\ntags=" + String.join(", ", this.getTags()) +
                '}';
    }

    @Override
    public double[] getPoint() {
        double[] tagPts = this.getOwner().calcTaskTagMask(this);
        double[] statPts = {(double) expDuration, (double) actualDuration, (double) createdAt.getTimeInMillis()};

        double[] points = new double[tagPts.length + statPts.length];

        int ptCtr = 0;

        for (double val: statPts) {
            points[ptCtr] = val;
            ptCtr++;
        }

        for (double val : tagPts) {
            points[ptCtr] = val;
            ptCtr++;
        }


        return points;
    }

    @Override
    public double compute(double[] a, double[] b) throws DimensionMismatchException {

        if (a.length < b.length) {
            throw new DimensionMismatchException(a.length, b.length);
        } else if (a.length > b.length){
            throw new DimensionMismatchException(b.length, a.length);
        }

        double sumTerm = 0.0;

        for (int i = 0; i < a.length && i < b.length; i++) {
            sumTerm += Math.pow(b[i] - a[i], 2);
        }

        return Math.sqrt(sumTerm);
    }
}
