import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BaseHttpHandler {
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();


    private void sendResponse(HttpExchange exchange, int statusCode, String contentType, String body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    void sendTextOk(HttpExchange exchange, Object responseObject) throws IOException {
        if (responseObject instanceof String) {
            sendResponse(exchange, 200, "text/plain; charset=UTF-8", (String) responseObject);
        } else {
            sendResponse(exchange, 200, "application/json; charset=UTF-8", gson.toJson(responseObject));
        }
    }

    void sendIdNotFound(HttpExchange exchange, int id) throws IOException {
        sendResponse(exchange, 404, "text/plain; charset=UTF-8", "Задача с id " + id + " не найдена");
    }

    void sendIncorrectId(HttpExchange exchange, String id) throws IOException {
        sendResponse(exchange, 400, "text/plain; charset=UTF-8", "Некорректный id: " + id);
    }

    void sendTextCreatedOk(HttpExchange exchange, String response) throws IOException {
        sendResponse(exchange, 201, "text/plain; charset=UTF-8", response);
    }

    void sendHasOverlaps(HttpExchange exchange) throws IOException {
        sendResponse(exchange, 406, "text/plain; charset=UTF-8", "Задача пересекается с существующими");
    }

    // Адаптеры для Gson
    static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.format(formatter));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            return LocalDateTime.parse(in.nextString(), formatter);
        }
    }

    static class DurationAdapter extends TypeAdapter<Duration> {
        @Override
        public void write(JsonWriter out, Duration value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public Duration read(JsonReader in) throws IOException {
            return Duration.parse(in.nextString());
        }
    }
}
