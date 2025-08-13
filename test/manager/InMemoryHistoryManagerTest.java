package manager;

import managers.InMemoryHistoryManager;
import org.junit.jupiter.api.Test;
import tasks.Status;
import tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    @Test
    void historyShouldContainPreviousVersionOfTask() {
        InMemoryHistoryManager history = new InMemoryHistoryManager();
        Task task = new Task("Test", "Desc", Status.NEW);
        task.setId(1);

        history.add(task);
        List<Task> historyList = history.getHistory();

        assertEquals(1, historyList.size());
        assertEquals(task, historyList.get(0));
    }

    @Test
    void shouldAddTasksInOrder_whenAddingMultipleTasks() {
        InMemoryHistoryManager manager = new InMemoryHistoryManager();
        Task task1 = new Task("tasks.Task 1", "Desc", Status.NEW);
        Task task2 = new Task("tasks.Task 2", "Desc", Status.NEW);
        task1.setId(1);
        task2.setId(2);
        manager.add(task1);
        manager.add(task2);
        List<Task> history = manager.getHistory();
        assertEquals(2, history.size(), "History should contain exactly 2 tasks");
        assertEquals(1, history.get(0).getId(), "First task should have ID=1");
        assertEquals(2, history.get(1).getId(), "Second task should have ID=2");

    }

    @Test
    void shouldMoveDuplicateTaskToEnd_whenAddingExistingTask() {
        InMemoryHistoryManager manager = new InMemoryHistoryManager();
        Task task1 = new Task("tasks.Task 1", "Desc", Status.NEW);
        Task task2 = new Task("tasks.Task 2", "Desc", Status.NEW);
        task1.setId(1);
        task2.setId(2);
        manager.add(task1);
        manager.add(task2);
        manager.add(task1);
        List<Task> history = manager.getHistory();
        assertEquals(2, history.size(), "History should contain 2 tasks");
        assertEquals(2, history.get(0).getId(), "First task should have ID=2");
        assertEquals(1, history.get(1).getId(), "Second task should have ID=1");
    }

    @Test
    void shouldRemoveTaskFromHistory_whenRemovingById() {
        InMemoryHistoryManager manager = new InMemoryHistoryManager();
        Task task1 = new Task("tasks.Task 1", "Desc", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("tasks.Task 2", "Desc", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("tasks.Task 3", "Description", Status.NEW);
        task3.setId(3);
        manager.add(task1);
        manager.add(task2);
        manager.add(task3);
        manager.remove(2);
        List<Task> history = manager.getHistory();
        assertEquals(2, history.size(), "History should contain 2 tasks");
        assertEquals(1, history.get(0).getId(), "First task should have ID=1");
        assertEquals(3, history.get(1).getId(), "Second task should have ID=3");
        manager.remove(999);
        assertEquals(2, history.size(), "History should remain unchanged for non-existent ID");
    }

    @Test
    void shouldClearHistory_whenRemovingSingleTask() {
        InMemoryHistoryManager manager = new InMemoryHistoryManager();
        Task task1 = new Task("tasks.Task 1", "Desc", Status.NEW);
        task1.setId(1);
        manager.add(task1);
        manager.remove(1);
        List<Task> history = manager.getHistory();
        assertTrue(history.isEmpty(), "History should be empty after removing single task");
    }

    @Test
    void shouldRemoveFirstTask_whenRemovingHead() {
        InMemoryHistoryManager manager = new InMemoryHistoryManager();
        Task task1 = new Task("tasks.Task 1", "Desc", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("tasks.Task 2", "Desc", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("tasks.Task 3", "Description", Status.NEW);
        task3.setId(3);
        manager.add(task1);
        manager.add(task2);
        manager.add(task3);
        manager.remove(1);
        List<Task> history = manager.getHistory();
        assertEquals(2, history.size(), "History should contain 2 tasks");
        assertEquals(2, history.get(0).getId(), "First task should have ID=2");
        assertEquals(3, history.get(1).getId(), "Second task should have ID=3");
    }

    @Test
    void shouldRemoveLastTask_whenRemovingTail() {
        InMemoryHistoryManager manager = new InMemoryHistoryManager();
        Task task1 = new Task("tasks.Task 1", "Desc", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("tasks.Task 2", "Desc", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("tasks.Task 3", "Description", Status.NEW);
        task3.setId(3);
        manager.add(task1);
        manager.add(task2);
        manager.add(task3);
        manager.remove(3);
        List<Task> history = manager.getHistory();
        assertEquals(2, history.size(), "History should contain 2 tasks");
        assertEquals(1, history.get(0).getId(), "First task should have ID=1");
        assertEquals(2, history.get(1).getId(), "Second task should have ID=2");
    }
}