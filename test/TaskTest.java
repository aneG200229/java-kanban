import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class TaskTest {

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Задача", "Описание", Status.NEW);
        Task task2 = new Task("Другая задача", "Другое описание", Status.IN_PROGRESS);

        task1.setId(1);
        task2.setId(1);

        assertEquals(task1, task2, "Задачи с одинаковым ID должны быть равны");
    }

    @Test
    void subtasksWithSameIdShouldBeEqual() {
        Subtask subtask1 = new Subtask("Подзадача", "Описание", Status.NEW, 5);
        Subtask subtask2 = new Subtask("Подзадача2", "Другое", Status.DONE, 5);

        subtask1.setId(100);
        subtask2.setId(100);

        assertEquals(subtask1, subtask2, "Subtask с одинаковым ID должны быть равны");
    }

    @Test
    void subtaskWithSameIdAsEpicShouldNotBeAdded() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Epic epic = new Epic("Эпик", "Описание");
        epic.setId(100);
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Ошибка", "Описание", Status.NEW, 100);
        subtask.setId(100);

        manager.createSubtask(subtask);


        assertNull(manager.getSubtaskById(100), "Подзадача с ID, равным Epic, не должна быть добавлена.");
    }

    @Test
    void subtaskCannotHaveItselfAsEpic() {
        Subtask subtask = new Subtask("Ошибка", "Описание", Status.NEW, 42);
        subtask.setId(42); // ID совпадает с epicId

        InMemoryTaskManager manager = new InMemoryTaskManager();
        manager.createSubtask(subtask);

        assertNull(manager.getSubtaskById(42), "Subtask не должен ссылаться сам на себя как на Epic");
    }

    @Test
    void managersShouldReturnInitializedInstances() {
        TaskManager manager = Managers.getDefault();
        HistoryManager history = Managers.getDefaultHistory();

        assertNotNull(manager, "TaskManager должен быть инициализирован");
        assertNotNull(history, "HistoryManager должен быть инициализирован");
    }

    @Test
    void shouldCreateAndFindDifferentTasksById() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task task = new Task("Задача", "Описание", Status.NEW);
        manager.createTask(task);

        Epic epic = new Epic("Эпик", "Описание");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Подзадача", "Описание", Status.NEW, epic.getId());
        manager.createSubtask(subtask);

        assertEquals(task, manager.getTaskById(task.getId()));
        assertEquals(epic, manager.getEpicById(epic.getId()));
        assertEquals(subtask, manager.getSubtaskById(subtask.getId()));
    }

    @Test
    void manuallyAndAutoAssignedIdsShouldNotConflict() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Task manualTask = new Task("Ручная задача", "Описание", Status.NEW);
        manualTask.setId(99);
        manager.createTask(manualTask);

        Task autoTask = new Task("Автоматическая задача", "Описание", Status.NEW);
        manager.createTask(autoTask);

        assertNotEquals(manualTask.getId(), autoTask.getId(), "ID не должны конфликтовать");
    }

    @Test
    void taskShouldRemainUnchangedAfterAddition() {
        InMemoryTaskManager manager = new InMemoryTaskManager();
        Task task = new Task("Оригинал", "Описание", Status.NEW);
        manager.createTask(task);

        Task stored = manager.getTaskById(task.getId());

        assertEquals(task.getName(), stored.getName());
        assertEquals(task.getDescription(), stored.getDescription());
        assertEquals(task.getStatus(), stored.getStatus());
    }

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
        Task task1 = new Task("Task 1", "Desc", Status.NEW);
        Task task2 = new Task("Task 2", "Desc", Status.NEW);
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
        Task task1 = new Task("Task 1", "Desc", Status.NEW);
        Task task2 = new Task("Task 2", "Desc", Status.NEW);
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
        Task task1 = new Task("Task 1", "Desc", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Desc", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description", Status.NEW);
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
        Task task1 = new Task("Task 1", "Desc", Status.NEW);
        task1.setId(1);
        manager.add(task1);
        manager.remove(1);
        List<Task> history = manager.getHistory();
        assertTrue(history.isEmpty(), "History should be empty after removing single task");
    }

    @Test
    void shouldRemoveFirstTask_whenRemovingHead() {
        InMemoryHistoryManager manager = new InMemoryHistoryManager();
        Task task1 = new Task("Task 1", "Desc", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Desc", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description", Status.NEW);
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
        Task task1 = new Task("Task 1", "Desc", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task 2", "Desc", Status.NEW);
        task2.setId(2);
        Task task3 = new Task("Task 3", "Description", Status.NEW);
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