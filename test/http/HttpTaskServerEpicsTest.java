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

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpTaskServerEpicsTest {
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

    @Test
    public void testGetEmptyEpicsList() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body().trim());
    }

    @Test
    public void testCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test tasks.Epic", "Testing epic");

        String json = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals("tasks.Epic успешно создан", response.body());
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Test tasks.Epic", "Testing epic");
        String json = gson.toJson(epic);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.send(createRequest, HttpResponse.BodyHandlers.ofString());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/1"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("\"id\":1"));
        assertTrue(getResponse.body().contains("Test tasks.Epic"));
    }

    @Test
    public void testGetAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic1", "Description1");
        Epic epic2 = new Epic("Epic2", "Description2");

        String jsonEpic1 = gson.toJson(epic1);
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonEpic1))
                .build();
        client.send(request1, HttpResponse.BodyHandlers.ofString());

        String jsonEpic2 = gson.toJson(epic2);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonEpic2))
                .build();
        client.send(request2, HttpResponse.BodyHandlers.ofString());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Epic1"));
        assertTrue(response.body().contains("Epic2"));
    }

    @Test
    public void testEpicNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test tasks.Epic", "Testing epic");
        String json = gson.toJson(epic);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        client.send(createRequest, HttpResponse.BodyHandlers.ofString());

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/1"))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode());
        assertEquals("tasks.Epic успешно удален", deleteResponse.body());
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test tasks.Epic", "Testing epic");
        String epicJson = gson.toJson(epic);

        HttpRequest createEpicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        client.send(createEpicRequest, HttpResponse.BodyHandlers.ofString());

        // Создаем подзадачу для эпика (правильный порядок: name, description, epicId, status, duration, startTime)
        Subtask subtask = new Subtask("Subtask1", "Description", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now(), 1);
        String subtaskJson = gson.toJson(subtask);

        HttpRequest createSubtaskRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();
        client.send(createSubtaskRequest, HttpResponse.BodyHandlers.ofString());

        // Получаем подзадачи эпика
        HttpRequest getSubtasksRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/1/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(getSubtasksRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Subtask1"));
    }

    @Test
    public void testGetEpicSubtasksNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testGetEpicWithInvalidId() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/abc"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Некорректный id"));
    }
}