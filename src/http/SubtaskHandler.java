package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import exception.TaskOverlapException;
import managers.TaskManager;
import tasks.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public SubtaskHandler(TaskManager manager, Gson gson) {
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
                    Subtask subtask = manager.getSubtaskById(id);
                    if (subtask != null) {
                        sendTextOk(exchange, subtask);
                    } else {
                        sendIdNotFound(exchange, id);
                    }

                } catch (NumberFormatException e) {
                    sendIncorrectId(exchange, pathParts[2]);
                }
            } else {
                List<Subtask> subtasks = manager.getSubtasks();
                sendTextOk(exchange, subtasks);
            }
        } else if (method.equals("POST")) {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes());
            Subtask subtask = gson.fromJson(body, Subtask.class);
            if (subtask.getId() == 0) {
                try {
                    manager.createSubtask(subtask);
                    String response = "Подзадача успешно создана";
                    sendTextCreatedOk(exchange, response);
                } catch (TaskOverlapException e) {
                    sendHasOverlaps(exchange);
                }
            } else {
                try {
                    manager.updateSubtask(subtask);
                    sendTextCreatedOk(exchange, "Задача успешно обновлена");
                } catch (TaskOverlapException e) {
                    sendHasOverlaps(exchange);
                }

            }
        } else if (method.equals("DELETE")) {
            if (hasId) {
                try {
                    int id = Integer.parseInt(pathParts[2]);
                    manager.removeSubtaskById(id);
                    sendTextOk(exchange, "Подзадача обновлена");
                } catch (NumberFormatException e) {
                    sendIncorrectId(exchange, pathParts[2]);
                }
            }
        }

    }
}
