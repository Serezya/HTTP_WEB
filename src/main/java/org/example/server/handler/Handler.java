package org.example.server.handler;

import org.example.server.request.Request;

import java.io.BufferedOutputStream;

@FunctionalInterface
public interface Handler {

    void handle(Request request, BufferedOutputStream out);
}
