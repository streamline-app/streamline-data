package org.thomaschen.streamlinedata.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskDataTest {
    private static TaskData testTaskData;
    private static UserData testUserData;
    private static List<String> tags;

    @BeforeEach
    void setUp() {
        testUserData = new UserData(
                "user1",
                212,
                112,
                100,
                1.212,
                1256.12);
        tags = Arrays.asList("tag1", "tag2", "tag3");
        testTaskData = new TaskData(testUserData, 1200L, 1000L, tags);
    }

    @AfterEach
    void tearDown() {
        setUp();
    }

    @Test
    void getTaskId() {
        assertNotNull(testTaskData.getTaskId());
    }

    @Test
    void getCreatedAt() {
        assertNotNull(testTaskData.getCreatedAt());
    }

    @Test
    void getOwner() {
        assertEquals(testUserData, testTaskData.getOwner());
    }

    @Test
    void getExpDuration() {
        assertEquals(1200L, (long) testTaskData.getExpDuration());
    }

    @Test
    void getActualDuration() {
        assertEquals(1000L, (long) testTaskData.getActualDuration());

    }

    @Test
    void getTags() {
        assertEquals(tags, testTaskData.getTags());
    }

    @Test
    void setCreatedAt() {
        Calendar current = Calendar.getInstance();
        testTaskData.setCreatedAt(current);
        assertEquals(current, testTaskData.getCreatedAt());
    }

    @Test
    void setOwner() {
        UserData newOwner = new UserData("user2", 12, 6, 6, 1.56, 2.20);
        testTaskData.setOwner(newOwner);
        assertEquals(newOwner, testTaskData.getOwner());
    }

    @Test
    void setExpDuration() {
        testTaskData.setExpDuration(1900L);
        assertEquals(1900L, (long) testTaskData.getExpDuration());
    }

    @Test
    void setActualDuration() {
        testTaskData.setActualDuration(1900L);
        assertEquals(1900L, (long) testTaskData.getActualDuration());
    }

    @Test
    void setTags() {
        tags = Arrays.asList("tag4");
        testTaskData.setTags(tags);
        assertEquals(tags, testTaskData.getTags());

    }
}