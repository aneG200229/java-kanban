import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private File tempFile;

    @BeforeEach
    public void setUp() throws IOException {
        // Создаём временный файл перед каждым тестом
        tempFile = File.createTempFile("tasks", ".txt");
        tempFile.deleteOnExit();
    }

    @Test
    public void testSaveAndLoadEmptyFile() throws IOException {
        // Создаём менеджер
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        // Сохраняем пустой менеджер
        manager.save();

        // Проверяем, что файл содержит только заголовок
        List<String> lines = Files.readAllLines(tempFile.toPath(), StandardCharsets.UTF_8);
        assertEquals(1, lines.size(), "Файл должен содержать только заголовок");
        assertEquals("id,type,name,status,description,epic", lines.get(0), "Заголовок должен быть корректным");

        // Загружаем пустой файл
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        // Проверяем, что списки задач пусты
        assertTrue(loadedManager.getTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loadedManager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    public void testSaveMultipleTasks() throws IOException, IllegalArgumentException {
        // Создаём менеджер и задачи
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
        Task task = new Task("Task1", "Description1", Status.NEW);
        Epic epic = new Epic("Epic1", "Description2");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Description3", Status.NEW, epic.getId());
        manager.createTask(task);
        manager.createSubtask(subtask);

        // Проверяем содержимое файла
        List<String> lines = Files.readAllLines(tempFile.toPath(), StandardCharsets.UTF_8);
        assertEquals(4, lines.size(), "Файл должен содержать 4 строки (заголовок + 3 задачи)");
        assertEquals("id,type,name,status,description,epic", lines.get(0), "Заголовок должен быть корректным");

        boolean taskFound = false, epicFound = false, subtaskFound = false;
        for (int i = 1; i < lines.size(); i++) {
            String[] fields = lines.get(i).split(",");
            if (fields[1].equals("TASK") && fields[2].equals("Task1")) taskFound = true;
            if (fields[1].equals("EPIC") && fields[2].equals("Epic1")) epicFound = true;
            if (fields[1].equals("SUBTASK") && fields[2].equals("Subtask1")) subtaskFound = true;
        }
        assertTrue(taskFound, "Задача Task1 должна быть сохранена");
        assertTrue(epicFound, "Эпик Epic1 должен быть сохранен");
        assertTrue(subtaskFound, "Подзадача Subtask1 должна быть сохранена");
    }

    @Test
    public void testLoadMultipleTasks() throws IllegalArgumentException {
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);
        Task task = new Task("Task1", "Description1", Status.NEW);
        Epic epic = new Epic("Epic1", "Description2");
        manager.createEpic(epic);
        Subtask subtask = new Subtask("Subtask1", "Description3", Status.NEW, epic.getId());
        manager.createTask(task);
        manager.createSubtask(subtask);

        System.out.println("Сохраняем данные...");
        manager.save();

        List<String> lines = null;
        try {
            lines = Files.readAllLines(tempFile.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Содержимое файла перед загрузкой: " + lines);
        assertFalse(lines.isEmpty(), "Файл не должен быть пустым");
        assertEquals(4, lines.size(), "Файл должен содержать 4 строки (заголовок + 3 задачи)");

        System.out.println("Загружаем данные...");
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> loadedTasks = loadedManager.getTasks();
        System.out.println("Загруженные задачи: " + loadedTasks);
        assertEquals(1, loadedTasks.size(), "Должна быть 1 задача");
        assertEquals("Task1", loadedTasks.get(0).getName(), "Имя задачи должно совпадать");

        List<Epic> loadedEpics = loadedManager.getEpics();
        System.out.println("Загруженные эпики: " + loadedEpics);
        assertEquals(1, loadedEpics.size(), "Должен быть 1 эпик");
        assertEquals("Epic1", loadedEpics.get(0).getName(), "Имя эпика должно совпадать");

        List<Subtask> loadedSubtasks = loadedManager.getSubtasks();
        System.out.println("Загруженные подзадачи: " + loadedSubtasks);
        assertEquals(1, loadedSubtasks.size(), "Должна быть 1 подзадача");
        assertEquals("Subtask1", loadedSubtasks.get(0).getName(), "Имя подзадачи должно совпадать");
    }
}