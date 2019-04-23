package org.thomaschen.streamlinedata.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class UserDataTest {
    private static UserData testUserData;
    private static List<TaskData> testTaskDatas;
    private static Set<String> singleTagsList = new HashSet<String>();
    private static Set<String> doubleTagsList = new HashSet<String>();
    private static Set<String> tripleTagsList = new HashSet<String>();

    @BeforeEach
    void setUp() {
        singleTagsList.add("tag1");
        doubleTagsList.add("tag1");
        tripleTagsList.add("tag1");
        doubleTagsList.add("tag2");
        tripleTagsList.add("tag2");
        tripleTagsList.add("tag3");

        testUserData = new UserData(
                "user1",
                0,
                0,
                0,
                0.0,
                0.0);

        testTaskDatas = Arrays.asList(
                new TaskData(testUserData, 1000L, 1200L, singleTagsList),
                new TaskData(testUserData, 1000L, 1000L, singleTagsList),
                new TaskData(testUserData, 1000L, 1400L, singleTagsList),
                new TaskData(testUserData, 1000L, 200L, doubleTagsList),
                new TaskData(testUserData, 1000L, 3600L, tripleTagsList),
                new TaskData(testUserData, 1000L, 4600L, tripleTagsList)
        );

        for (TaskData task : testTaskDatas) {
            testUserData.addTaskData(task);
        }
    }

    @AfterEach
    void tearDown() {
        setUp();
    }

    @Test
    void calcUserData() {
        UserData temp = UserData.calcUserData(testTaskDatas, "user1");
        assertEquals(testUserData.getAvgTaskTime(), temp.getAvgTaskTime());
        assertEquals(testUserData.getTaskEstFactor(), temp.getTaskEstFactor());
        assertEquals(testUserData.getTotalOverTasks(), temp.getTotalOverTasks());
        assertEquals(testUserData.getTotalUnderTasks(), temp.getTotalUnderTasks());
        assertEquals(testUserData.getTotalTasksCompleted(), temp.getTotalTasksCompleted());
    }

    @Test
    void addTaskData() {
        UserData temp =  new UserData(
                "temp1",
                0,
                0,
                0,
                0.0,
                0.0);

        temp.addTaskData(testTaskDatas.get(0));
        assertEquals((double) temp.getAvgTaskTime(), 1200.0);
        assertEquals((double) temp.getTaskEstFactor(), 1.2);
        assertEquals((int) temp.getTotalTasksCompleted(), 1);
        assertEquals((int) temp.getTotalOverTasks(), 1);
        assertEquals((int) temp.getTotalUnderTasks(), 0);

        temp.addTaskData(testTaskDatas.get(1));
        assertEquals((double) temp.getAvgTaskTime(), 1100.0);
        assertEquals((double) temp.getTaskEstFactor(), 1.1);
        assertEquals((int) temp.getTotalTasksCompleted(), 2);
        assertEquals((int) temp.getTotalOverTasks(), 1);
        assertEquals((int) temp.getTotalUnderTasks(), 1);
    }

    @Test
    void subtractTaskData() {
        UserData temp =  new UserData(
                "temp1",
                0,
                0,
                0,
                0.0,
                0.0);

        temp.addTaskData(testTaskDatas.get(0));
        temp.addTaskData(testTaskDatas.get(1));
        temp.removeTaskData(testTaskDatas.get(1));

        assertEquals((double) temp.getAvgTaskTime(), 1200.0);
        assertEquals((double) temp.getTaskEstFactor(), 1.2);
        assertEquals((int) temp.getTotalTasksCompleted(), 1);
        assertEquals((int) temp.getTotalOverTasks(), 1);
        assertEquals((int) temp.getTotalUnderTasks(), 0);

        temp.addTaskData(testTaskDatas.get(1));
        temp.removeTaskData(testTaskDatas.get(0));

        assertEquals((double) temp.getAvgTaskTime(), 1000.0);
        assertEquals((double) temp.getTaskEstFactor(), 1.0);
        assertEquals((int) temp.getTotalTasksCompleted(), 1);
        assertEquals((int) temp.getTotalOverTasks(), 0);
        assertEquals((int) temp.getTotalUnderTasks(), 1);
    }

    @Test
    void removeTaskData() {
        UserData temp =  new UserData(
                "temp1",
                0,
                0,
                0,
                0.0,
                0.0);

        temp.addTaskData(testTaskDatas.get(0));
        temp.removeTaskData(testTaskDatas.get(0));
        assertEquals(0, (int) temp.getTotalTasksCompleted());
    }

    @Test
    void getId() {
        assertNotNull(testUserData.getId());
    }

    @Test
    void getUserId() {
        assertEquals("user1", testUserData.getUserId());
    }

    @Test
    void getCreatedAt() {
        assertNotNull(testUserData.getCreatedAt());
    }

    @Test
    void getUpdatedAt() {
        assertNotNull(testUserData.getUpdatedAt());
    }

    @Test
    void getTotalTasksCompleted() {
        assertEquals(6, (int) testUserData.getTotalTasksCompleted());
    }

    @Test
    void getTotalUnderTasks() {
        assertEquals(2, (int) testUserData.getTotalUnderTasks());
    }

    @Test
    void getTotalOverTasks() {
        assertEquals(4, (int) testUserData.getTotalOverTasks());
    }

    @Test
    void getTaskEstFactor() {
        assertEquals(2, (double) testUserData.getTaskEstFactor());
    }

    @Test
    void getAvgTaskTime() {
        assertEquals(2000, (double) testUserData.getAvgTaskTime());
    }

    @Test
    void setUserId() {
        testUserData.setUserId("user2");
        assertEquals("user2", testUserData.getUserId());
    }

    @Test
    void setUpdatedAt() {
        Calendar now = Calendar.getInstance();
        testUserData.setUpdatedAt(now);
        assertEquals(now, testUserData.getUpdatedAt());
    }

    @Test
    void setTotalTasksCompleted() {
        testUserData.setTotalTasksCompleted(12341);
        assertEquals(12341, (int) testUserData.getTotalTasksCompleted());
    }

    @Test
    void setTotalUnderTasks() {
        testUserData.setTotalUnderTasks(12);
        assertEquals(12, (int) testUserData.getTotalUnderTasks());
    }

    @Test
    void setTotalOverTasks() {
        testUserData.setTotalOverTasks(15);
        assertEquals(15, (int) testUserData.getTotalOverTasks());
    }

    @Test
    void setTaskEstFactor() {
        testUserData.setTaskEstFactor(1.0);
        assertEquals(1.0, (double) testUserData.getTaskEstFactor());
    }

    @Test
    void setAvgTaskTime() {
        testUserData.setTaskEstFactor(12.6512);
        assertEquals(12.6512, (double) testUserData.getTaskEstFactor());
    }

    @Test
    void equals() {
        UserData temp = new UserData(
                "user1",
                0,
                0,
                0,
                0.0,
                0.0);

        assertNotEquals(temp, testUserData);
    }
}