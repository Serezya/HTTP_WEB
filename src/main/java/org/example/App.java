package org.example;

import org.example.server.Server;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class App {
    public static void main(String[] args) {
        var server = new Server(64);

        server.addHandler("GET", "/classic.html", (request, out) -> {
            try {
                final var filePath = Path.of(".", "public", request.getPath());
                final var mimeType = Files.probeContentType(filePath);

                // special case for classic
                final var template = Files.readString(filePath);
                final var content = template.replace("{currentTime}",
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                        .getBytes();
                outWrite(mimeType, content, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.addHandler("GET", "/events.html", (request, out) -> {
            try {
                final var filePath = Path.of(".", "public", request.getPath());
                final var mimeType = Files.probeContentType(filePath);

                // special case for events
                final var content = Files.readString(filePath).getBytes();
                outWrite(mimeType, content, out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.listen(9999);
    }
    private static void outWrite(String mimeType, byte[] content, BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.write(content);
        out.flush();
    }
}


