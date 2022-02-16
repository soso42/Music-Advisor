package advisor.model;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class Server {

    private HttpServer server;
    private int port;
    private static final int HTTP_OK_STATUS = 200;
    private static final int HTTP_BAD_AUTH_STATUS = 401;
    private static final String HTTP_OK_MESSAGE = "Got the code. Return back to your program.";
    private static final String HTTP_BAD_AUTH_MESSAGE = "Authorization code not found. Try again.";

    public Server(Authenticator authenticator, int port) throws IOException {

        this.server = HttpServer.create();
        this.port = port;
        this.server.bind(new InetSocketAddress(port), 0);

        this.server.createContext("/",
                new HttpHandler() {
                    public void handle(HttpExchange exchange) throws IOException {
                        String response;
                        //Create a response form the request query parameters
                        URI uri = exchange.getRequestURI();
                        response = uri.getQuery();
                        if (response != null && response.contains("code")) {
                            String[] arr = response.split("=");
                            authenticator.setCode(arr[1]);
                            writeMessageToBrowser(exchange, HTTP_OK_STATUS, HTTP_OK_MESSAGE);
                        } else {
                            writeMessageToBrowser(exchange, HTTP_BAD_AUTH_STATUS, HTTP_BAD_AUTH_MESSAGE);
                        }
                    }
                }
        );
    }

    private void writeMessageToBrowser(HttpExchange exchange, int httpStatus, String message) throws IOException {
        OutputStream os;
        //Set the response header status and length
        exchange.sendResponseHeaders(httpStatus, message.length());
        //Write the response string
        os = exchange.getResponseBody();
        os.write(message.getBytes());
        os.close();
    }

    protected String getRedirectURI() {
        return "http://localhost:" + this.port + "/";
    }

    public void start() {
        this.server.start();
    }

    public void stop() {
        this.server.stop(1);
    }

}
