package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exception.TaskOverlapException;
import managers.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;


    public TasksHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

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
                    Task task = manager.getTaskById(id);
                    if (task != null) {
                        sendTextOk(exchange, task);
                    } else {
                        sendIdNotFound(exchange, id);
                    }

                } catch (NumberFormatException e) {
                    sendIncorrectId(exchange, pathParts[2]);
                }
            } else {
                List<Task> tasks = manager.getTasks();
                sendTextOk(exchange, tasks);
            }
        } else if (method.equals("POST")) {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes());
            Task task = gson.fromJson(body, Task.class);
            if (task.getId() == 0) {
                try {
                    manager.createTask(task);
                    String response = "Задача успешно создана";
                    sendTextCreatedOk(exchange, response);
                } catch (TaskOverlapException e) {
                    sendHasOverlaps(exchange);
                }
            } else {
                try {
                    manager.updateTask(task);
                    String response = "Задача успешно обновлена";
                    sendTextOk(exchange, response);
                } catch (TaskOverlapException e) {
                    sendHasOverlaps(exchange);
                }

            }
        } else if (method.equals("DELETE")) {
            int id = Integer.parseInt(pathParts[2]);
            try {
                manager.removeTaskById(id);
                String response = "Задача успешно удалена";
                sendTextOk(exchange, response);
            } catch (NumberFormatException e) {
                sendIdNotFound(exchange, id);
            }
        }

    }
}