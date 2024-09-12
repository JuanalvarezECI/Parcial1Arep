package org.example.Service;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CalculatorService {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(36001);
        System.out.println("Calculadora en el puerto 36001");

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

                if (path.startsWith("/compreflex")) {
                    handleComputeRequest(out, path);
                }
            }
        }
    }

    private static void handleComputeRequest(PrintWriter out, String path) {
        try {
            if (path.contains("?") && path.split("\\?").length > 1) {
                String query = path.split("\\?")[1];
                if (query.contains("=") && query.split("=").length > 1) {
                    String command = query.split("=")[1];
                    String[] parts = command.split("\\(");
                    String operation = parts[0].toLowerCase(); // Ensure method name is lowercase
                    List<Double> params = Arrays.stream(parts[1].replace(")", "").split(","))
                            .map(Double::parseDouble)
                            .collect(Collectors.toList());

                    String response;
                    if (operation.equals("bbl")) {
                        response = bubbleSort(params);
                    } else {
                        response = invokeMathMethod(operation, params);
                    }

                    out.println("HTTP/1.1 200 OK");
                    out.println("Content-Type: application/json");
                    out.println("Content-Length: " + response.length());
                    out.println();
                    out.println(response);
                } else {
                    out.println("HTTP/1.1 400 Bad Request");
                }
            } else {
                out.println("HTTP/1.1 400 Bad Request");
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("HTTP/1.1 500 Internal Server Error");
        }
    }

    private static String bubbleSort(List<Double> params) {
        Double[] array = params.toArray(new Double[0]);
        for (int i = 0; i < array.length - 1; i++) {
            for (int j = 0; j < array.length - i - 1; j++) {
                if (array[j] > array[j + 1]) {
                    double temp = array[j];
                    array[j] = array[j + 1];
                    array[j + 1] = temp;
                }
            }
        }
        return Arrays.toString(array);
    }

    private static String invokeMathMethod(String operation, List<Double> params) throws Exception {
        try {
            Class<?> mathClass = Math.class;
            Method method = mathClass.getMethod(operation, double.class);
            double result = (double) method.invoke(null, params.get(0));
            return String.valueOf(result);
        } catch (NoSuchMethodException e) {
            return "{\"error\": \"No such method: " + operation + "\"}";
        } catch (Exception e) {
            return "{\"error\": \"Error invoking method: " + e.getMessage() + "\"}";
        }
    }
}