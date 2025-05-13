import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.net.URLDecoder;

public class VotingServer {
    private static Map<String, Integer> votes = new HashMap<>();

    static {
        votes.put("candidatea", 0);
        votes.put("candidateb", 0);
        votes.put("candidatec", 0);
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new StaticFileHandler());
        server.createContext("/vote", new VoteHandler());
        server.createContext("/results", new ResultsHandler());
        server.setExecutor(null);
        server.start();
        System.out.println("Server started on port 8000");
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            String filePath = "web" + path;
            File file = new File(filePath);
            if (file.exists()) {
                String contentType = getContentType(filePath);
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, file.length());
                OutputStream os = exchange.getResponseBody();
                Files.copy(file.toPath(), os);
                os.close();
            } else {
                String response = "404 - File Not Found";
                exchange.sendResponseHeaders(404, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }

        private String getContentType(String filePath) {
            if (filePath.endsWith(".html")) return "text/html";
            if (filePath.endsWith(".css")) return "text/css";
            if (filePath.endsWith(".js")) return "application/javascript";
            return "application/octet-stream";
        }
    }

    static class VoteHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String body = new String(is.readAllBytes());
                String[] parts = body.split("=");
                if (parts.length < 2) {
                    exchange.sendResponseHeaders(400, -1); // Bad request
                    return;
                }
                String candidate = URLDecoder.decode(parts[1], "UTF-8").toLowerCase().replace(" ", "");
                System.out.println("Received vote for: " + candidate);
                votes.put(candidate, votes.getOrDefault(candidate, 0) + 1);
                System.out.println("Updated votes map: " + votes);
                String response = mapToJson(votes);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    static class ResultsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                System.out.println("Returning results: " + votes);
                String response = mapToJson(votes);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    private static String mapToJson(Map<String, Integer> map) {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (sb.length() > 1) sb.append(",");
            sb.append("\"").append(entry.getKey()).append("\":").append(entry.getValue());
        }
        sb.append("}");
        return sb.toString();
    }
}