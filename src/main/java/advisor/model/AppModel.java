package advisor.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppModel {

    private boolean authorized = false;
    private final Authenticator authenticator;

    private final String AUTH_SERVER_PATH;
    private final String API_SERVER_PATH;
    private String accessToken;
    private HttpClient httpClient = HttpClient.newHttpClient();
    private Map<String, String> categoryMap = new HashMap<>();

    private final String API_PATH_FEATURED = "/v1/browse/featured-playlists";
    private final String API_PATH_NEW = "/v1/browse/new-releases";
    private final String API_PATH_CATEGORIES = "/v1/browse/categories";
    private final String API_PATH_PLAYLISTS = "/v1/browse/categories/%s/playlists";


    public AppModel(String authServerPath, String ApiServerPath) {
        this.AUTH_SERVER_PATH = authServerPath;
        this.API_SERVER_PATH = ApiServerPath;
        this.authenticator = new Authenticator(AUTH_SERVER_PATH, this);
    }

    public void authorize() throws InterruptedException {
        authenticator.authorize();
        this.accessToken = authenticator.getAccessToken();
    }


    // Getters
    public boolean isAuthorized() {
        return this.authorized;
    }

    public String sendRequest(String PathAPI) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(PathAPI))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public List<String> getFeaturedList() throws IOException, InterruptedException {
        String json = sendRequest(API_SERVER_PATH + API_PATH_FEATURED);
        List<String> list = new ArrayList<>();
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        JsonObject playlists = jo.get("playlists").getAsJsonObject();
        JsonArray items = playlists.get("items").getAsJsonArray();
        for (JsonElement elem: items) {
            JsonObject ob = elem.getAsJsonObject();
            String name = ob.get("name").getAsString();
            JsonObject externalURL = ob.get("external_urls").getAsJsonObject();
            String href = externalURL.get("spotify").getAsString();
            String sb = name +
                    "\n" +
                    href +
                    "\n";
            list.add(sb);
        }
        return list;
    }

    public List<String> getNewList() throws IOException, InterruptedException {
        String json = sendRequest(API_SERVER_PATH + API_PATH_NEW);
        List<String> list = new ArrayList<>();
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        JsonObject albums = jo.get("albums").getAsJsonObject();
        JsonArray items = albums.get("items").getAsJsonArray();
        for (JsonElement elem: items) {
            JsonObject ob = elem.getAsJsonObject();
            JsonObject externalURL = ob.get("external_urls").getAsJsonObject();

            String str = elem.getAsJsonObject().get("name").getAsString() +
                    "\n" +
                    generateArtistsList(ob.get("artists").getAsJsonArray()) +
                    "\n" +
                    externalURL.get("spotify").getAsString() +
                    "\n";

            list.add(str);
        }
        return list;
    }

    private String generateArtistsList(JsonArray artists) {
        StringBuilder artistsList = new StringBuilder();
        for (JsonElement jsonElement: artists) {
            artistsList.append(artistsList.toString().equals("") ? "[" : ", ");
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            artistsList.append(jsonObject.get("name").getAsString());
        }
        artistsList.append("]");
        return artistsList.toString();
    }

    public List<String> getCategories() throws IOException, InterruptedException {
        List<String> list = new ArrayList<>();
        String json = sendRequest(API_SERVER_PATH + API_PATH_CATEGORIES);
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();
        JsonObject categories = jo.get("categories").getAsJsonObject();
        JsonArray items = categories.getAsJsonArray("items");

        for (JsonElement elem: items) {
            JsonObject ob = elem.getAsJsonObject();
            String name = ob.get("name").getAsString();
            list.add(name);

            // Create map of category names and IDs for future use
            String id = ob.get("id").getAsString();
            this.categoryMap.put(name, id);
        }

        return list;
    }

    public List<String> getPlaylists(String categoryName) throws IOException, InterruptedException {
        List<String> list = new ArrayList<>();
        getCategories();
        String categoryID = getCategoryID(categoryName);

        if (categoryID == null) {
            return null;
        }

        String json = sendRequest(String.format(API_SERVER_PATH + API_PATH_PLAYLISTS, categoryID));
        JsonObject jo = JsonParser.parseString(json).getAsJsonObject();

        try {
            JsonObject error = jo.get("error").getAsJsonObject();
            String errorMessage = error.get("message").getAsString();
            list.add(errorMessage);
            return list;
        } catch (NullPointerException ignored) {}

        JsonObject playlists = jo.get("playlists").getAsJsonObject();
        JsonArray items = playlists.get("items").getAsJsonArray();
        for (JsonElement elem: items) {
            JsonObject ob = elem.getAsJsonObject();
            String name = ob.get("name").getAsString();
            JsonObject externalURL = ob.get("external_urls").getAsJsonObject();
            String href = externalURL.get("spotify").getAsString();
            String str = name +
                    "\n" +
                    href +
                    "\n";
            list.add(str);
        }

        return list;
    }

    private String getCategoryID(String name) {
        return this.categoryMap.get(name);
    }


    // Setters
    public void setAuth(boolean authorized) {
        this.authorized = authorized;
    }

}
