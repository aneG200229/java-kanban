import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;


public class HttpTaskServer {
    private HttpServer server;
    private Gson gson;


    public HttpTaskServer(TaskManager manager) throws IOException {
        this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new BaseHttpHandler.LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new BaseHttpHandler.DurationAdapter()).create();
        this.server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/tasks", new TasksHandler(manager, gson));
        server.createContext("/subtasks", new SubtaskHandler(manager, gson));
        server.createContext("/epics", new EpicHandler(manager, gson));
        server.createContext("/history", new HistoryHandler(manager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(manager, gson));
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


}