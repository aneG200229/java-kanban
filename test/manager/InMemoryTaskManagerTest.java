package manager;

import exception.TaskOverlapException;
import managers.InMemoryTaskManager;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
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
    void subtaskWithSameIdAsEpicShouldNotBeAdded() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        Epic epic = new Epic("Эпик", "Описание");
        epic.setId(100);
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Ошибка", "Описание", Status.NEW, 100);
        subtask.setId(100);

        manager.createSubtask(subtask);


        assertNull(manager.getSubtaskById(100), "Подзадача с ID, равным tasks.Epic, не должна быть добавлена.");
    }

    @Test
    void shouldNotAllowOverlappingTasks() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        LocalDateTime startTime1 = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration1 = Duration.ofHours(2);
        Task task1 = new Task("Task1", "Desc1", Status.NEW, duration1, startTime1);

        LocalDateTime startTime2 = LocalDateTime.of(2024, 1, 1, 11, 0);
        Duration duration2 = Duration.ofHours(2);
        Task task2 = new Task("Task2", "Desc2", Status.NEW, duration2, startTime2);

        manager.createTask(task1);
        assertThrows(TaskOverlapException.class, () -> {
            manager.createTask(task2);
        });

        assertEquals(1, manager.getTasks().size(), "Должна быть только одна задача");
    }

    @Test
    void shouldAllowNonOverlappingTasks() {
        InMemoryTaskManager manager = new InMemoryTaskManager();

        // Первая задача: 10:00-12:00
        LocalDateTime startTime1 = LocalDateTime.of(2024, 1, 1, 10, 0);
        Duration duration1 = Duration.ofHours(2);
        Task task1 = new Task("Task1", "Desc1", Status.NEW, duration1, startTime1);

        // Вторая задача: 13:00-15:00 (НЕ пересекается!)
        LocalDateTime startTime2 = LocalDateTime.of(2024, 1, 1, 13, 0);
        Duration duration2 = Duration.ofHours(2);
        Task task2 = new Task("Task2", "Desc2", Status.NEW, duration2, startTime2);

        manager.createTask(task1);
        manager.createTask(task2);

        assertEquals(2, manager.getTasks().size(), "Непересекающиеся задачи должны быть добавлены");
    }

    @Test
    void shouldReturnTasksInPriorityOrder() {
        InMemoryTaskManager manager = new InMemoryTaskManager();


        LocalDateTime time1 = LocalDateTime.of(2024, 1, 1, 15, 0);
        LocalDateTime time2 = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime time3 = LocalDateTime.of(2024, 1, 1, 12, 0);

        Task task1 = new Task("Task1", "Desc1", Status.NEW, Duration.ofHours(1), time1);
        Task task2 = new Task("Task2", "Desc2", Status.NEW, Duration.ofHours(1), time2);
        Task task3 = new Task("Task3", "Desc3", Status.NEW, Duration.ofHours(1), time3);

        // Добавляем в разном порядке
        manager.createTask(task1);    // 15:00
        manager.createTask(task2);    // 10:00
        manager.createTask(task3);    // 12:00


        List<Task> prioritized = manager.getPrioritizedTasks();

        // Проверяем правильный порядок
        assertEquals(3, prioritized.size());
        assertEquals(time2, prioritized.get(0).getStartTime()); // 10:00 - первый
        assertEquals(time3, prioritized.get(1).getStartTime()); // 12:00 - второй
        assertEquals(time1, prioritized.get(2).getStartTime()); // 15:00 - третий
    }
}