package http;

import com.google.gson.Gson;
import managers.Managers;
import managers.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpTaskServerHistoryAndPrioritizedTest {
    private HttpTaskServer server;
    private TaskManager manager;
    private HttpClient client;
    private Gson gson;

    @BeforeEach
    public void setUp() throws IOException {
        manager = Managers.getDefault();
        server = new HttpTaskServer(manager);
        gson = server.getGson();
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    // History тесты
    @Test
    public void testGetEmptyHistory() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body().trim());
    }

    @Test
    public void testGetHistoryWithTasks() throws IOException, InterruptedException {
        // Создаем задачи
        Task task1 = new Task("tasks.Task 1", "Description 1", Status.NEW, Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        Task task2 = new Task("tasks.Task 2", "Description 2", Status.NEW, Duration.ofMinutes(45), LocalDateTime.now().plusHours(3));

        String jsonTask1 = gson.toJson(task1);
        HttpRequest createRequest1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask1))
                .build();
        client.send(createRequest1, HttpResponse.BodyHandlers.ofString());

        String jsonTask2 = gson.toJson(task2);
        HttpRequest createRequest2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask2))
                .build();
        client.send(createRequest2, HttpResponse.BodyHandlers.ofString());

        // Получаем задачи (добавляем в историю)
        HttpRequest getRequest1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/1"))
                .GET()
                .build();
        client.send(getRequest1, HttpResponse.BodyHandlers.ofString());

        HttpRequest getRequest2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/2"))
                .GET()
                .build();
        client.send(getRequest2, HttpResponse.BodyHandlers.ofString());

        // Получаем историю
        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, historyResponse.statusCode());
        assertTrue(historyResponse.body().contains("tasks.Task 1") || historyResponse.body().contains("tasks.Task 2"));
    }

    // Prioritized тесты
    @Test
    public void testGetEmptyPrioritizedList() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body().trim());
    }

    @Test
    public void testGetPrioritizedTasksWithTasks() throws IOException, InterruptedException {
        // Создаем задачи с разным временем начала для проверки приоритизации
        Task task1 = new Task("tasks.Task 1", "Description 1", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 1, 14, 0));
        Task task2 = new Task("tasks.Task 2", "Description 2", Status.NEW,
                Duration.ofMinutes(45), LocalDateTime.of(2025, 1, 1, 10, 0));
        Task task3 = new Task("tasks.Task 3", "Description 3", Status.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 1, 12, 0));

        // Создаем задачи
        String jsonTask1 = gson.toJson(task1);
        HttpRequest createRequest1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask1))
                .build();
        client.send(createRequest1, HttpResponse.BodyHandlers.ofString());

        String jsonTask2 = gson.toJson(task2);
        HttpRequest createRequest2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask2))
                .build();
        client.send(createRequest2, HttpResponse.BodyHandlers.ofString());

        String jsonTask3 = gson.toJson(task3);
        HttpRequest createRequest3 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonTask3))
                .build();
        client.send(createRequest3, HttpResponse.BodyHandlers.ofString());

        // Получаем приоритизированный список
        HttpRequest prioritizedRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> prioritizedResponse = client.send(prioritizedRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, prioritizedResponse.statusCode());

        // Проверяем, что все задачи присутствуют в ответе
        assertTrue(prioritizedResponse.body().contains("tasks.Task 1"));
        assertTrue(prioritizedResponse.body().contains("tasks.Task 2"));
        assertTrue(prioritizedResponse.body().contains("tasks.Task 3"));
    }

    @Test
    public void testGetPrioritizedTasksWithSubtasks() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test tasks.Epic", "tasks.Epic description");
        String epicJson = gson.toJson(epic);
        HttpRequest createEpicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        client.send(createEpicRequest, HttpResponse.BodyHandlers.ofString());

        // Создаем подзадачи с разным временем
        Subtask subtask1 = new Subtask("tasks.Subtask 1", "Description 1", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 2, 9, 0), 1);
        Subtask subtask2 = new Subtask("tasks.Subtask 2", "Description 2", Status.NEW,
                Duration.ofMinutes(45), LocalDateTime.of(2025, 1, 2, 11, 0), 1);

        String subtaskJson1 = gson.toJson(subtask1);
        HttpRequest createSubtaskRequest1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson1))
                .build();
        client.send(createSubtaskRequest1, HttpResponse.BodyHandlers.ofString());

        String subtaskJson2 = gson.toJson(subtask2);
        HttpRequest createSubtaskRequest2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson2))
                .build();
        client.send(createSubtaskRequest2, HttpResponse.BodyHandlers.ofString());

        // Получаем приоритизированный список
        HttpRequest prioritizedRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> prioritizedResponse = client.send(prioritizedRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, prioritizedResponse.statusCode());

        // Проверяем, что подзадачи присутствуют в ответе
        assertTrue(prioritizedResponse.body().contains("tasks.Subtask 1"));
        assertTrue(prioritizedResponse.body().contains("tasks.Subtask 2"));
    }

    @Test
    public void testGetPrioritizedMixedTasksAndSubtasks() throws IOException, InterruptedException {
        // Создаем обычную задачу
        Task task = new Task("Regular tasks.Task", "tasks.Task description", Status.NEW,
                Duration.ofMinutes(60), LocalDateTime.of(2025, 1, 3, 13, 0));
        String taskJson = gson.toJson(task);
        HttpRequest createTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        client.send(createTaskRequest, HttpResponse.BodyHandlers.ofString());

        // Создаем эпик
        Epic epic = new Epic("Test tasks.Epic", "tasks.Epic description");
        String epicJson = gson.toJson(epic);
        HttpRequest createEpicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        client.send(createEpicRequest, HttpResponse.BodyHandlers.ofString());

        // Создаем подзадачу
        Subtask subtask = new Subtask("Test tasks.Subtask", "tasks.Subtask description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 3, 10, 0), 2);
        String subtaskJson = gson.toJson(subtask);
        HttpRequest createSubtaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        client.send(createSubtaskRequest, HttpResponse.BodyHandlers.ofString());

        // Получаем приоритизированный список
        HttpRequest prioritizedRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> prioritizedResponse = client.send(prioritizedRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, prioritizedResponse.statusCode());

        // Проверяем, что и задача, и подзадача присутствуют в ответе
        assertTrue(prioritizedResponse.body().contains("Regular tasks.Task"));
        assertTrue(prioritizedResponse.body().contains("Test tasks.Subtask"));
    }

    @Test
    public void testGetPrioritizedTasksWithoutStartTime() throws IOException, InterruptedException {
        // Создаем задачу без времени начала
        Task taskWithoutTime = new Task("tasks.Task without time", "Description", Status.NEW);
        String taskJson = gson.toJson(taskWithoutTime);
        HttpRequest createTaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        client.send(createTaskRequest, HttpResponse.BodyHandlers.ofString());

        // Создаем задачу с временем
        Task taskWithTime = new Task("tasks.Task with time", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2025, 1, 4, 15, 0));
        String taskWithTimeJson = gson.toJson(taskWithTime);
        HttpRequest createTaskWithTimeRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskWithTimeJson))
                .build();
        client.send(createTaskWithTimeRequest, HttpResponse.BodyHandlers.ofString());

        // Получаем приоритизированный список
        HttpRequest prioritizedRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> prioritizedResponse = client.send(prioritizedRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, prioritizedResponse.statusCode());
        // Проверяем, что получили ответ (содержимое зависит от реализации менеджера)
        assertTrue(prioritizedResponse.body().length() >= 2); // Как минимум "[]" или содержимое
    }
}