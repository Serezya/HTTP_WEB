package org.example;

public class App {
    public static void main( String[] args ) {
        Server server = new Server();
        server.listen(9999);
    }
}
