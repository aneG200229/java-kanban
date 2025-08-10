import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class HttpTaskServer {
    private HttpServer server;
    private Gson gson;
    private TaskManager manager;

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new BaseHttpHandler.LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new BaseHttpHandler.DurationAdapter()).create();
        this.server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/tasks", new TasksHandler());
        server.createContext("/subtasks", new SubtaskHandler());
        server.createContext("/epics", new EpicHandler());
        server.createContext("/history", new HistoryHandler());
        server.createContext("/prioritized", new PrioritizedHandler());
    }

    public Gson getGson() {
        return gson;
    }

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(taskManager);
        server.start();
        System.out.println("Сервер запущен!");

    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    class TasksHandler extends BaseHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            BaseHttpHandler baseHttpHandler = new BaseHttpHandler();
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            String[] pathParts = path.split("/");

            boolean hasId = pathParts.length > 2;

            if (method.equals("GET")) {
                if (hasId) {
                    try {
                        int id = Integer.parseInt(pathParts[2]);
                        Task task = manager.getTaskById(id);
                        if (task != null) {
                            baseHttpHandler.sendTextOk(exchange, task);
                        } else {
                            baseHttpHandler.sendIdNotFound(exchange, id);
                        }

                    } catch (NumberFormatException e) {
                        baseHttpHandler.sendIncorrectId(exchange, pathParts[2]);
                    }
                } else {
                    List<Task> tasks = manager.getTasks();
                    baseHttpHandler.sendTextOk(exchange, tasks);
                }
            } else if (method.equals("POST")) {
                InputStream inputStream = exchange.getRequestBody();
                String body = new String(inputStream.readAllBytes());
                Task task = gson.fromJson(body, Task.class);
                if (task.getId() == 0) {
                    int sizeBefore = manager.getTasks().size();
                    manager.createTask(task);
                    int sizeAfter = manager.getTasks().size();
                    if (sizeAfter > sizeBefore) {
                        String response = "Задача успешно создана";
                        baseHttpHandler.sendTextCreatedOk(exchange, response);
                    } else {
                        baseHttpHandler.sendHasOverlaps(exchange);
                    }
                } else {
                    manager.updateTask(task);
                    String response = "Задача успешно обновлена";
                    baseHttpHandler.sendTextOk(exchange, response);
                }
            } else if (method.equals("DELETE")) {
                try {
                    int id = Integer.parseInt(pathParts[2]);
                    manager.removeTaskById(id);
                    String response = "Задача успешно удалена";
                    baseHttpHandler.sendTextOk(exchange, response);
                } catch (NumberFormatException e) {
                    int id = Integer.parseInt(pathParts[2]);
                    baseHttpHandler.sendIdNotFound(exchange, id);
                }
            }

        }
    }

    class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
        BaseHttpHandler baseHttpHandler = new BaseHttpHandler();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");
            boolean hasId = pathParts.length > 2;

            if (method.equals("GET")) {
                if (hasId) {
                    try {
                        int id = Integer.parseInt(pathParts[2]);
                        Subtask subtask = manager.getSubtaskById(id);
                        if (subtask != null) {
                            baseHttpHandler.sendTextOk(exchange, subtask);
                        } else {
                            baseHttpHandler.sendIdNotFound(exchange, id);
                        }

                    } catch (NumberFormatException e) {
                        baseHttpHandler.sendIncorrectId(exchange, pathParts[2]);
                    }
                } else {
                    List<Subtask> subtasks = manager.getSubtasks();
                    baseHttpHandler.sendTextOk(exchange, subtasks);
                }
            } else if (method.equals("POST")) {
                InputStream inputStream = exchange.getRequestBody();
                String body = new String(inputStream.readAllBytes());
                Subtask subtask = gson.fromJson(body, Subtask.class);
                if (subtask.getId() == 0) {
                    int sizeBefore = manager.getSubtasks().size();
                    manager.createSubtask(subtask);
                    int sizeAfter = manager.getSubtasks().size();
                    if (sizeAfter > sizeBefore) {
                        String response = "Подзадача успешно создана";
                        baseHttpHandler.sendTextCreatedOk(exchange, response);

                    } else {
                        baseHttpHandler.sendHasOverlaps(exchange);
                    }
                } else {
                    manager.updateSubtask(subtask);
                    baseHttpHandler.sendTextCreatedOk(exchange, "Задача успешно обновлена");
                }
            } else if (method.equals("DELETE")) {
                if (hasId) {

                    try {
                        int id = Integer.parseInt(pathParts[2]);
                        manager.removeSubtaskById(id);
                        baseHttpHandler.sendTextOk(exchange, "Подзадача обновлена");
                    } catch (NumberFormatException e) {
                        baseHttpHandler.sendIncorrectId(exchange, pathParts[2]);
                    }
                }
            }

        }
    }

    class EpicHandler extends BaseHttpHandler implements HttpHandler {
        BaseHttpHandler baseHttpHandler = new BaseHttpHandler();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String[] pathParts = path.split("/");

            if (method.equals("GET")) {
                if (pathParts.length == 2) {
                    List<Epic> epics = manager.getEpics();
                    baseHttpHandler.sendTextOk(exchange, epics);

                } else if (pathParts.length == 3) {
                    try {
                        int id = Integer.parseInt(pathParts[2]);
                        Epic epic = manager.getEpicById(id);
                        if (epic != null) {
                            baseHttpHandler.sendTextOk(exchange, epic);
                        } else {
                            baseHttpHandler.sendIdNotFound(exchange, id);
                        }
                    } catch (NumberFormatException e) {
                        baseHttpHandler.sendIncorrectId(exchange, pathParts[2]);
                    }
                } else if (pathParts.length == 4 && pathParts[3].equals("subtasks")) {
                    try {
                        int id = Integer.parseInt(pathParts[2]);
                        Epic epic = manager.getEpicById(id);
                        if (epic != null) {
                            List<Subtask> jsonResponse = manager.getSubtasksByEpicId(id);
                            baseHttpHandler.sendTextOk(exchange, jsonResponse);
                        } else {
                            baseHttpHandler.sendIdNotFound(exchange, id);
                        }
                    } catch (NumberFormatException e) {
                        baseHttpHandler.sendIncorrectId(exchange, pathParts[2]);
                    }
                }
            } else if (method.equals("POST")) {
                InputStream inputStream = exchange.getRequestBody();
                String body = new String(inputStream.readAllBytes());
                Epic epic = gson.fromJson(body, Epic.class);
                manager.createEpic(epic);
                baseHttpHandler.sendTextCreatedOk(exchange, "Epic успешно создан");
            } else if (method.equals("DELETE")) {
                if (pathParts.length == 3) {
                    int id = Integer.parseInt(pathParts[2]);
                    try {
                        manager.removeEpicById(id);
                        baseHttpHandler.sendTextOk(exchange, "Epic успешно удален");
                    } catch (NumberFormatException e) {
                        baseHttpHandler.sendIdNotFound(exchange, id);
                    }
                }
            }
        }
    }

    class HistoryHandler extends BaseHttpHandler implements HttpHandler {
        BaseHttpHandler baseHttpHandler = new BaseHttpHandler();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if (method.equals("GET")) {
                List<Task> history = manager.getHistory();
                baseHttpHandler.sendTextOk(exchange, history);
            }
        }
    }

    class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

        BaseHttpHandler baseHttpHandler = new BaseHttpHandler();

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();

            if (method.equals("GET")) {
                List<Task> prioritized = manager.getPrioritizedTasks();
                baseHttpHandler.sendTextOk(exchange, prioritized);
            }
        }
    }
}