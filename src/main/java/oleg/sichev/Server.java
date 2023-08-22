package oleg.sichev;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    static Scanner scanner = new Scanner(System.in);

    static void startServer(List<String> validPaths) {
        try (final var serverSocket = new ServerSocket(9999)) {
            System.out.println("Сервер запущен\nДля ожидания новых подключений напишите 1, для отключения сервера любую " +
                    "другую кнопку");
            int input = scanner.nextInt();
            if (input == 1) {
                newConnect(serverSocket, validPaths);
            } else {
                System.out.println("Завершение работы сервера");
                serverSocket.close();
                System.exit(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void newConnect(ServerSocket serverSocket, List<String> validPaths) throws IOException {
        Socket clientSocket = serverSocket.accept();
        ExecutorService executorService = Executors.newFixedThreadPool(64);
        executorService.submit(() -> {
            logic(clientSocket, validPaths);
        });
    }

    static Map<String, String> getParameters(String query) {
        List<NameValuePair> queryParams = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
        Map<String, String> parameters = new HashMap<>();
        for (NameValuePair param : queryParams) {
            parameters.put(param.getName(), param.getValue() == null ? "" : param.getValue());
        }
        return parameters;
    }

    static String getQueryParam(Map<String, String> parameters, String name) {
        return parameters.get(name);
    }

    static void logic(Socket clientSocket, List<String> validPaths) {
        while (true) {
            try (
                    final var socket = clientSocket;
                    final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final var out = new BufferedOutputStream(socket.getOutputStream())
            ) {
                final var requestLine = in.readLine();
                if (requestLine == null) {
                    break;
                }

                final var parts = requestLine.split(" ");
                if (parts.length != 3) {
                    continue;
                }
                final var method = parts[0].toUpperCase();
                if (!method.equals("GET")) {
                    continue;
                }
                final var path = parts[1];
                if (!validPaths.contains(path)) {
                    continue;
                }

                final var query = URLEncodedUtils.parse(URI.create(path), StandardCharsets.UTF_8);
                final var queryParams = new HashMap<String, String>();
                for (NameValuePair pair : query) {
                    queryParams.put(pair.getName(), pair.getValue());
                }

                final var body = Files.readAllBytes(Path.of("./content" + path));
                final var length = body.length;

                out.write(("HTTP/1.1 200 OK\r\nContent-Length: " + length + "\r\n\r\n").getBytes());
                out.write(body);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}