package org.example.server;

import org.example.server.handler.Handler;
import org.example.server.request.Request;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final ExecutorService executorService;
    private final Map<String, Map<String, Handler>> handlers;
    private final Handler notFoundHandler = (request, out) -> {
        try {
            out.write((
                    "HTTP/1.1 404 Not Found\r\n" +
                            "Content-Length: 0\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    };

    public Server(int threadPoolSize) {
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
        this.handlers = new ConcurrentHashMap<>();
    }

    public void addHandler(String method, String path, Handler handler) {
        if(handlers.get(method) == null){
            handlers.put(method, new ConcurrentHashMap<>());
        }
        handlers.get(method).put(path, handler);
    }

    public void listen(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                final var socket = serverSocket.accept();
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        Server.this.process(socket);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void process(Socket socket) {
        try (socket;
             final var in = socket.getInputStream();
             final var out = new BufferedOutputStream(socket.getOutputStream());) {

            var request = Request.fromInputStream(in);
            var pathHandlerMap = handlers.get(request.getMethod());
            if (pathHandlerMap == null) {
                notFoundHandler.handle(request, out);
                return;
            }
            var handler = pathHandlerMap.get(request.getPath());
            if (handler == null) {
                notFoundHandler.handle(request, out);
                return;
            }
            handler.handle(request, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
