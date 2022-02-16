package advisor.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Authenticator {

    private Server server;
    private int port;
    private AppModel model;
    private String code = null;
    private final String SERVER_PATH;
    private String accessToken;

    // Static final variables
    private static final String CLIENT_ID = "b81b0fc92902433b963c35b43432a7e6";
    private static final String CLIENT_SECRET = "3521ddc1b539452bb0633c74d429a986";


    public Authenticator(String serverPath, AppModel model) {
        this.SERVER_PATH = serverPath;
        this.model = model;
        try {
            this.server = new Server(this, getRedirectPort());
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    protected String getAccessToken() {
        return this.accessToken;
    }

    protected int getRedirectPort() {
        this.port = (int)(Math.random()*90) + 8000;
        return this.port;
    }

    protected void setCode(String code) {
        this.code = code;
    }



    //
    protected void authorize() throws InterruptedException {
        server.start();
        receiveAccessCode();
        server.stop();
        receiveToken();
        // Tell model that authorization succeeded
        this.model.setAuth(true);
    }

    private void receiveAccessCode() throws InterruptedException {
        System.out.println("use this link to request the access code:");
        System.out.printf("%s/authorize?client_id=%s&redirect_uri=%s&response_type=code\n",
                SERVER_PATH, CLIENT_ID, server.getRedirectURI());
        System.out.println("waiting for code...");

        while (this.code == null) {
            Thread.sleep(1000);
        }

        System.out.println("code received");
    }

    private void receiveToken() {
        System.out.println("making http request for access_token...");
        System.out.println("response:");

        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(SERVER_PATH + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString(
                        "grant_type=authorization_code"
                                + "&code=" + code
                                + "&client_id=" + CLIENT_ID
                                + "&client_secret=" + CLIENT_SECRET
                                + "&redirect_uri=" + server.getRedirectURI()))
                .build();

        try {
            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assert response != null;
            System.out.println(response.body());
            this.accessToken = parseAccessToken(response.body());
            System.out.println("---SUCCESS---");

        } catch (InterruptedException | IOException e) {
            System.out.println("Error response");
        }
    }

    private String parseAccessToken(String json) {
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        return jo.get("access_token").getAsString();
    }
}
