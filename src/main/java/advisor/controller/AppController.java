package advisor.controller;

import advisor.model.AppModel;
import advisor.view.AppView;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class AppController {

    private final Scanner scanner = new Scanner(System.in);
    private final AppModel model;
    private final AppView view;

    private List<String> list;
    private int itemsPerPage;
    private int currentPage;
    private int totalPages;

    public AppController(AppModel model, AppView view, int itemsPerPage) {
        this.view = view;
        this.model = model;
        this.itemsPerPage = itemsPerPage;
    }

    public void start() throws InterruptedException, IOException {
        String input = scanner.nextLine();
        while (!input.equalsIgnoreCase("exit")) {
            methodController(input);
            input = scanner.nextLine();
        }
        view.printExitMessage();
    }

    private void methodController(String input) throws InterruptedException, IOException {

        String command = input;
        String chosenPlaylist = null;

        if (command.contains("playlists")) {
            chosenPlaylist = command.replace("playlists", "").strip();
            command = "playlists";
        }

        if (model.isAuthorized()) {
            switch (command) {
                case "new":
                    list = model.getNewList();
                    generatePagedList(list);
                    view.printPagedList(list, currentPage, totalPages, itemsPerPage);
                    break;
                case "featured":
                    list = model.getFeaturedList();
                    generatePagedList(list);
                    view.printPagedList(list, currentPage, totalPages, itemsPerPage);
                    break;
                case "categories":
                    list = model.getCategories();
                    generatePagedList(list);
                    view.printPagedList(list, currentPage, totalPages, itemsPerPage);
                    break;
                case "playlists":
                    list = model.getPlaylists(chosenPlaylist);
                    if (list == null) {
                        view.printUnknownCategoryMessage();
                    } else {
                        generatePagedList(list);
                        view.printPagedList(list, currentPage, totalPages, itemsPerPage);
                    }
                    break;
                case "next":
                    if (currentPage == totalPages) {
                        view.printNoMorePagesMessage();
                    } else {
                        currentPage++;
                        view.printPagedList(list, currentPage, totalPages, itemsPerPage);
                    }
                    break;
                case "prev":
                    if (currentPage == 1) {
                        view.printNoMorePagesMessage();
                    } else {
                        currentPage--;
                        view.printPagedList(list, currentPage, totalPages, itemsPerPage);
                    }
                    break;
            }
        } else if (command.equalsIgnoreCase("auth")) {
            try {
                model.authorize();
            } catch (InterruptedException ignored) {}

        } else {
            view.printAuthRequiredMessage();
        }
    }

    private void generatePagedList(List<String> list) throws IOException, InterruptedException {
        this.currentPage = 1;
        this.totalPages = list.size() % itemsPerPage == 0 ? list.size() / itemsPerPage : (list.size() / itemsPerPage) + 1;
    }

}
