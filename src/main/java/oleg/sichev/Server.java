package oleg.sichev;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    static Scanner scanner = new Scanner(System.in);
    static void startServer(List<String> validPaths){
        try (final var serverSocket = new ServerSocket(9999)){
            System.out.println("Сервер запущен\nДля ожидания новых подключений напишите 1, для отключения сервера любую " +
                    "другую кнопку");
            int input = scanner.nextInt();
            if (input == 1){
                newConnect(serverSocket, validPaths);
            } else {
                System.out.println("Завершение работы сервера");
                serverSocket.close();
                System.exit(0);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    static void newConnect(ServerSocket serverSocket, List<String> validPaths) throws IOException {
        Socket clientSocket = serverSocket.accept();
        ExecutorService executorService = Executors.newFixedThreadPool(64);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try (
                            final var socket = clientSocket;
                            final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            final var out = new BufferedOutputStream(socket.getOutputStream());
                    ) {
                        // read only request line for simplicity
                        // must be in form GET /path HTTP/1.1
                        final var requestLine = in.readLine();
                        final var parts = requestLine.split(" ");

                        if (parts.length != 3) {
                            // just close socket
                            continue;
                        }

                        final var path = parts[1];
                        if (!validPaths.contains(path)) {
                            out.write((
                                    "HTTP/1.1 404 Not Found\r\n" +
                                            "Content-Length: 0\r\n" +
                                            "Connection: close\r\n" +
                                            "\r\n"
                            ).getBytes());
                            out.flush();
                            continue;
                        }

                        final var filePath = Path.of(".", "public", path);
                        final var mimeType = Files.probeContentType(filePath);

                        // special case for classic
                        if (path.equals("/classic.html")) {
                            final var template = Files.readString(filePath);
                            final var content = template.replace(
                                    "{time}",
                                    LocalDateTime.now().toString()
                            ).getBytes();
                            out.write((
                                    "HTTP/1.1 200 OK\r\n" +
                                            "Content-Type: " + mimeType + "\r\n" +
                                            "Content-Length: " + content.length + "\r\n" +
                                            "Connection: close\r\n" +
                                            "\r\n"
                            ).getBytes());
                            out.write(content);
                            out.flush();
                            continue;
                        }

                        final var length = Files.size(filePath);
                        out.write((
                                "HTTP/1.1 200 OK\r\n" +
                                        "Content-Type: " + mimeType + "\r\n" +
                                        "Content-Length: " + length + "\r\n" +
                                        "Connection: close\r\n" +
                                        "\r\n"
                        ).getBytes());
                        Files.copy(filePath, out);
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
