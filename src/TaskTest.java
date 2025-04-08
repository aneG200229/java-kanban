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

}