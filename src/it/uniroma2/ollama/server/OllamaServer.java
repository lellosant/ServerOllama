package it.uniroma2.ollama.server;


import com.google.gson.Gson;
import com.google.gson.JsonObject;
import spark.Spark;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static spark.Spark.*;

public class OllamaServer {

    private final Gson gson;
    private final int port;
    private final int timeout;
    private final String model;
    private final String ollamaEndpoint;
    private final HttpClient httpClient;
    private String requestModel;

    public OllamaServer(int port, String model, String ollamaEndpoint, int timeout) {
        this.port = port;
        this.model = model;
        this.requestModel = model;
        this.ollamaEndpoint = ollamaEndpoint;
        this.gson = new Gson();
        this.httpClient = HttpClient.newHttpClient();
        this.timeout = timeout;
    }

    public void start() {
        Spark.port(port); // Imposta la porta del server

        // Endpoint POST /generate
        Spark.post("/generate", (req, res) -> {

            res.type("application/json");
            // Inizio cronometro
            long startTime = System.currentTimeMillis();

            // Log della richiesta
            System.out.println("=== Nuova richiesta ===");
            System.out.println("Metodo: " + req.requestMethod());
            System.out.println("URL: " + req.url());
            //System.out.println("Headers:");
            //req.headers().forEach(h -> System.out.println("  " + h + ": " + req.headers(h)));
            //System.out.println("Body: " + req.body());
            try {
                // Legge il JSON ricevuto
                JsonObject requestJson = gson.fromJson(req.body(), JsonObject.class);

                if (requestJson == null || !requestJson.has("prompt")) {
                    res.status(400);
                    System.out.println("Stato: 400");
                    JsonObject responseJson = new JsonObject();
                    responseJson.addProperty("error", "Manca il campo 'prompt'");
                    return gson.toJson(responseJson);

                }

                if ( !requestJson.has("model")) {
                    requestModel = model;
                }else {
                    requestModel = requestJson.get("model").getAsString();
                }

                String prompt = requestJson.get("prompt").getAsString();


                // 🔹 Chiamata al server Ollama
                JsonObject payload = new JsonObject();

                payload.addProperty("model", requestModel);
                payload.addProperty("prompt", prompt);
                payload.addProperty("stream", false); // <-- disabilita streaming
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(ollamaEndpoint))
                        .timeout(Duration.ofSeconds(timeout))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                        .build();

                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                String ollamaResponse = response.body();

                // Restituisce la risposta al client
                JsonObject responseJson = new JsonObject();
                responseJson.addProperty("response", ollamaResponse);
                System.out.println("Stato: 200");
                return gson.toJson(responseJson);



            } catch (Exception e) {
                res.status(500);
                System.out.println("Stato: 500");
                JsonObject errorJson = new JsonObject();
                errorJson.addProperty("error", e.getMessage());
                return gson.toJson(errorJson);


            } finally {

                // Fine cronometro
                long endTime = System.currentTimeMillis();
                long durationMs = endTime - startTime;

                String formatted;
                if (durationMs >= 1000) {
                    double seconds = durationMs / 1000.0;
                    formatted = String.format("%.2fs", seconds);
                } else {
                    formatted = durationMs + "ms";
                }
                System.out.println("Tempo elaborazione: " + formatted);
                System.out.println("=======================");
            }
        });

        System.out.println("Server avviato su http://localhost:8080");
    }
}
