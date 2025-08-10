import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpTaskServerSubtasksTest {
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
    public void testGetEmptySubtasksList() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body().trim());
    }

    @Test
    public void testCreateSubtask() throws IOException, InterruptedException {
        // Сначала создаем эпик
        Epic epic = new Epic("Test Epic", "Testing epic");
        String epicJson = gson.toJson(epic);

        HttpRequest createEpicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        client.send(createEpicRequest, HttpResponse.BodyHandlers.ofString());

        // Создаем подзадачу (правильный порядок: name, description, epicId, status, duration, startTime)
        Subtask subtask = new Subtask("Test Subtask", "Testing subtask", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now(), 1);
        String json = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals("Подзадача успешно создана", response.body());
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Testing epic");
        String epicJson = gson.toJson(epic);
        HttpRequest createEpicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        client.send(createEpicRequest, HttpResponse.BodyHandlers.ofString());

        // Создаем подзадачу
        Subtask subtask = new Subtask("Test Subtask", "Testing subtask", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now(), 1);
        String json = gson.toJson(subtask);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        client.send(createRequest, HttpResponse.BodyHandlers.ofString());

        // Получаем подзадачу по ID
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/2"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("\"id\":2"));
        assertTrue(getResponse.body().contains("Test Subtask"));
    }

    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Testing epic");
        String epicJson = gson.toJson(epic);
        HttpRequest createEpicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        client.send(createEpicRequest, HttpResponse.BodyHandlers.ofString());

        // Создаем подзадачи
        Subtask subtask1 = new Subtask("Subtask1", "Description1", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1), 1);
        Subtask subtask2 = new Subtask("Subtask2", "Description2", Status.NEW,
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(2), 1);

        String jsonSubtask1 = gson.toJson(subtask1);
        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonSubtask1))
                .build();
        client.send(request1, HttpResponse.BodyHandlers.ofString());

        String jsonSubtask2 = gson.toJson(subtask2);
        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonSubtask2))
                .build();
        client.send(request2, HttpResponse.BodyHandlers.ofString());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Subtask1"));
        assertTrue(response.body().contains("Subtask2"));
    }

    @Test
    public void testSubtaskNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/999"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Testing epic");
        String epicJson = gson.toJson(epic);
        HttpRequest createEpicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        client.send(createEpicRequest, HttpResponse.BodyHandlers.ofString());

        // Создаем подзадачу
        Subtask subtask = new Subtask("Test Subtask", "Testing subtask", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now(), 1);
        String json = gson.toJson(subtask);

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        client.send(createRequest, HttpResponse.BodyHandlers.ofString());

        // Удаляем подзадачу
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/2"))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode());
        assertEquals("Подзадача обновлена", deleteResponse.body());
    }

    @Test
    public void testCreateSubtaskWithTimeOverlap() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Testing epic");
        String epicJson = gson.toJson(epic);
        HttpRequest createEpicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        client.send(createEpicRequest, HttpResponse.BodyHandlers.ofString());

        // Создаем первую подзадачу
        LocalDateTime startTime1 = LocalDateTime.of(2025, 1, 1, 10, 0);
        Subtask subtask1 = new Subtask("Subtask1", "Desc1", Status.NEW, Duration.ofHours(2), startTime1, 1);
        String jsonSubtask1 = gson.toJson(subtask1);

        HttpRequest createRequest1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonSubtask1))
                .build();
        client.send(createRequest1, HttpResponse.BodyHandlers.ofString());

        // Создаем вторую подзадачу с пересечением времени
        LocalDateTime startTime2 = LocalDateTime.of(2025, 1, 1, 11, 0);
        Subtask subtask2 = new Subtask("Subtask2", "Desc2", Status.NEW, Duration.ofHours(2), startTime2, 1);
        String jsonSubtask2 = gson.toJson(subtask2);

        HttpRequest createRequest2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonSubtask2))
                .build();

        HttpResponse<String> response = client.send(createRequest2, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        // Создаем эпик
        Epic epic = new Epic("Test Epic", "Testing epic");
        String epicJson = gson.toJson(epic);
        HttpRequest createEpicRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        client.send(createEpicRequest, HttpResponse.BodyHandlers.ofString());

        // Создаем подзадачу
        Subtask subtask = new Subtask("Старое имя", "Старое описание", Status.NEW,
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1), 1);
        String jsonSubtask1 = gson.toJson(subtask);
        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonSubtask1))
                .build();
        client.send(createRequest, HttpResponse.BodyHandlers.ofString());

        // Обновляем подзадачу
        Subtask updatedSubtask = new Subtask("Новое имя", "Новое описание", Status.IN_PROGRESS,
                Duration.ofMinutes(45), LocalDateTime.now().plusHours(2), 1);
        updatedSubtask.setId(2);
        String jsonSubtask2 = gson.toJson(updatedSubtask);
        HttpRequest updateRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonSubtask2))
                .build();
        HttpResponse<String> updateResponse = client.send(updateRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, updateResponse.statusCode());

        // Проверяем обновление
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/2"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        Subtask retrievedSubtask = gson.fromJson(getResponse.body(), Subtask.class);

        assertEquals("Новое имя", retrievedSubtask.getName());
        assertEquals("Новое описание", retrievedSubtask.getDescription());
        assertEquals(Status.IN_PROGRESS, retrievedSubtask.getStatus());
        assertEquals(2, retrievedSubtask.getId());
    }

    @Test
    public void testGetSubtaskWithInvalidId() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/abc"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Некорректный id"));
    }
}