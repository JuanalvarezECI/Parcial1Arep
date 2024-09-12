package org.example.Service;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class FachadaService {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(36000);
        System.out.println("Fachada en el puerto 36000");

        while (true) {
            try (Socket clientSocket = serverSocket.accept();
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                String inputLine;
                StringBuilder request = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    request.append(inputLine);
                    if (!in.ready()) {
                        break;
                    }
                }

                String requestLine = request.toString().split("\n")[0];
                String[] requestParts = requestLine.split(" ");
                String path = requestParts[1];

                if (path.equals("/calculadora")) {
                    handleClientRequest(out);
                } else if (path.startsWith("/computar")) {
                    handleComputeRequest(out, path);
                }
            }
        }
    }

    private static void handleClientRequest(PrintWriter out) {
        String response = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<title>Calculadora Web</title>" +
                "</head>" +
                "<body>" +
                "<h1>Calculadora Web</h1>" +
                "<form id=\"calcForm\">" +
                "<input type=\"text\" id=\"command\" placeholder=\"Comando (e.g., sqrt(16))\">" +
                "<button type=\"submit\">Calcular</button>" +
                "</form>" +
                "<div id=\"result\"></div>" +
                "<script>" +
                "document.getElementById('calcForm').addEventListener('submit', function(event) {" +
                "event.preventDefault();" +
                "const command = document.getElementById('command').value;" +
                "fetch(`/computar?comando=${encodeURIComponent(command)}`)" +
                ".then(response => response.json())" +
                ".then(data => {" +
                "document.getElementById('result').innerText = JSON.stringify(data);" +
                "});" +
                "});" +
                "</script>" +
                "</body>" +
                "</html>";
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: text/html");
        out.println("Content-Length: " + response.length());
        out.println();
        out.println(response);
    }

    private static void handleComputeRequest(PrintWriter out, String path) throws IOException {
        String query = path.split("\\?")[1];
        String command = query.split("=")[1];
        String calculatorServiceUrl = "http://localhost:36001/compreflex?comando=" + URLEncoder.encode(command, "UTF-8");

        URL url = new URL(calculatorServiceUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        String response = content.toString();
        out.println("HTTP/1.1 200 OK");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + response.length());
        out.println();
        out.println(response);
    }
}