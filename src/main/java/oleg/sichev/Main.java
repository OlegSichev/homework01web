package oleg.sichev;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import static oleg.sichev.Server.*;

public class Main {

    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html",
                "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

        System.out.println("Привет! Для запуска сервера нажмите 1, для выхода из приложения любую другую кнопку");
        int input = scanner.nextInt();
        if (input == 1) {
            startServer(validPaths);
        } else {
            System.exit(0);
        }
    }
}