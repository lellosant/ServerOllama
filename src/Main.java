import it.uniroma2.Ollama.server.OllamaConfig;
import it.uniroma2.Ollama.server.OllamaServer;

import java.io.File;

public class Main {

    public static void main(String[] args) {

        try {
            File configFile = new File("config.txt");
            if (!configFile.exists()) {
                System.out.println("File config.txt non trovato, uso valori di default.");
                // Puoi creare la Config con valori di default manualmente
                // oppure terminare l'applicazione
            } else {
                OllamaConfig config = new OllamaConfig("config.txt");
                int port = config.getInt("port", 8080);
                String model = config.getString("model", "llama3");
                String ollamaEndpoint = config.getString("ollama_endpoint", "http://localhost:11434/api/generate");
                int timeout = config.getInt("request_timeout",30);
                OllamaServer server = new OllamaServer(port, model,ollamaEndpoint,timeout);
                server.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
 }
}
