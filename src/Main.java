import it.uniroma2.ollama.config.OllamaConfig;
import it.uniroma2.ollama.server.OllamaServer;

import java.io.File;
import java.io.PrintWriter;

public class Main {

    public static void main(String[] args) {

        try {
            File configFile = new File("config.txt");
            if (!configFile.exists()) {
                System.out.println("File config.txt non trovato. Creo un file di configurazione di default...");

                try (PrintWriter writer = new PrintWriter(configFile)) {
                    writer.println("port=8080");
                    writer.println("model=llama3");
                    writer.println("ollama_endpoint=http://localhost:11434/api/generate");
                    writer.println("request_timeout=30");
                }catch (Exception e){
                    System.err.println("errore nella creazione del file di configurazione :" +  e.getMessage());
                }

            }
            try{
                OllamaConfig config = new OllamaConfig("config.txt");
                int port = config.getInt("port", 8080);
                String model = config.getString("model", "llama3");
                String ollamaEndpoint = config.getString("ollama_endpoint", "http://localhost:11434/api/generate");
                int timeout = config.getInt("request_timeout",30);
                OllamaServer server = new OllamaServer(port, model,ollamaEndpoint,timeout);
                server.start();
            } catch (Exception e) {
            e.printStackTrace();
        }
 }catch (Exception e){
            e.printStackTrace();
        }
}}
