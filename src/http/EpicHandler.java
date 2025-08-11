package http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.google.gson.Gson;
import managers.TaskManager;
import tasks.Epic;
import tasks.Subtask;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class EpicHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public EpicHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        if (method.equals("GET")) {
            if (pathParts.length == 2) {
                List<Epic> epics = manager.getEpics();
                sendTextOk(exchange, epics);

            } else if (pathParts.length == 3) {
                try {
                    int id = Integer.parseInt(pathParts[2]);
                    Epic epic = manager.getEpicById(id);
                    if (epic != null) {
                        sendTextOk(exchange, epic);
                    } else {
                        sendIdNotFound(exchange, id);
                    }
                } catch (NumberFormatException e) {
                    sendIncorrectId(exchange, pathParts[2]);
                }
            } else if (pathParts.length == 4 && pathParts[3].equals("subtasks")) {
                try {
                    int id = Integer.parseInt(pathParts[2]);
                    Epic epic = manager.getEpicById(id);
                    if (epic != null) {
                        List<Subtask> jsonResponse = manager.getSubtasksByEpicId(id);
                        sendTextOk(exchange, jsonResponse);
                    } else {
                        sendIdNotFound(exchange, id);
                    }
                } catch (NumberFormatException e) {
                    sendIncorrectId(exchange, pathParts[2]);
                }
            }
        } else if (method.equals("POST")) {
            InputStream inputStream = exchange.getRequestBody();
            String body = new String(inputStream.readAllBytes());
            Epic epic = gson.fromJson(body, Epic.class);
            manager.createEpic(epic);
            sendTextCreatedOk(exchange, "tasks.Epic успешно создан");
        } else if (method.equals("DELETE")) {
            if (pathParts.length == 3) {
                int id = Integer.parseInt(pathParts[2]);
                try {
                    manager.removeEpicById(id);
                    sendTextOk(exchange, "tasks.Epic успешно удален");
                } catch (NumberFormatException e) {
                    sendIdNotFound(exchange, id);
                }
            }
        }
    }
}
