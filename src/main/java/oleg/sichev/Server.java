package oleg.sichev;

import java.io.BufferedReader;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    static Scanner scanner = new Scanner(System.in);

    static void startServer(List<String> validPaths) {
        try (final var serverSocket = new ServerSocket(9999)) {
            System.out.println("Сервер запущен\nДля ожидания новых подключений напишите 1, " +
                    "для отключения сервера любую другую кнопку");
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
        System.out.println("Ожидаем подключения новых клиентов");
        Socket clientSocket = serverSocket.accept();
        ExecutorService executorService = Executors.newFixedThreadPool(64);
        executorService.submit(() -> {
            handleRequest(clientSocket, validPaths);
        });
    }

    static Request getRequest(BufferedReader in) throws IOException {
        String[] requestLineParts = in.readLine().split(" ");

        if (requestLineParts.length != 3) {
            return null;
        }

        String[] pathAndQueryParts = requestLineParts[1].split("\\?", 2);
        String path = pathAndQueryParts[0];
        String query = pathAndQueryParts.length > 1 ? pathAndQueryParts[1] : "";

        return new Request(requestLineParts[0].toUpperCase(), path, requestLineParts[2], query);
    }

    static void handleRequest(Socket clientSocket, List<String> validPaths) {
        while (true) {
            try (
                    final var socket = clientSocket;
                    final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final var out = new BufferedOutputStream(socket.getOutputStream())
            ) {
                Request request = getRequest(in);
                if (request == null) {
                    break;
                }

                if (!request.getMethod().equals("GET")) {
                    continue;
                }

                if (!validPaths.contains(request.getPath())) {
                    continue;
                }

                final var body = Files.readAllBytes(Path.of("./content" + request.getPath()));
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