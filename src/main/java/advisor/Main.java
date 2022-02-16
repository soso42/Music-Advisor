package advisor;

import advisor.controller.AppController;
import advisor.model.AppModel;
import advisor.view.AppView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static final String AUTH_SERVER_PATH = "https://accounts.spotify.com";
    private static final String API_SERVER_PATH = "https://api.spotify.com";

    public static void main(String[] args) throws IOException, InterruptedException {

        Map<String, String> arguments = getArguments(args);
        String authServerPath = getAuthServerPath(arguments);
        String apiServerPath = getAPIServerPath(arguments);
        int itemsPerPage = getNumOfItemsPerPage(arguments);

        AppModel model = new AppModel(authServerPath, apiServerPath);
        AppView view = new AppView();
        AppController controller = new AppController(model, view, itemsPerPage);

        controller.start();
    }

    private static HashMap<String, String> getArguments(String[] args) {
        HashMap<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            map.put(args[i], args[i + 1]);
        }
        return map;
    }

    private static String getAuthServerPath(Map<String, String> arguments) {
        return arguments.getOrDefault("-access", AUTH_SERVER_PATH);
    }

    private static String getAPIServerPath(Map<String, String> arguments) {
        return arguments.getOrDefault("-resource", API_SERVER_PATH);
    }

    private static int getNumOfItemsPerPage(Map<String, String> arguments) {
        return Integer.parseInt(arguments.getOrDefault("-page", "5"));
    }

}
