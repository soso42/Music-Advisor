package advisor.view;

import java.util.List;

public class AppView {

    public AppView() {}

    public void printListOfArrays(List<String[]> list) {
        for (String[] elem: list) {
            for (String str: elem) {
                System.out.println(str);
            }
            System.out.println();
        }
    }

    public void printList(List<String> list) {
        for (String elem: list) {
            System.out.println(elem);
        }
    }

    public void printPagedList(List<String> list, int currentPage, int totalPages, int itemsPerPage) {
//        for (String elem: list) {
//            System.out.println(elem);
//        }
        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = startIndex + itemsPerPage;

        for (int i = 0; i < list.size(); i++) {
            if (i >= startIndex && i < endIndex) {
                System.out.println(list.get(i));
            }
        }

        System.out.printf("---PAGE %d OF %d---\n", currentPage, totalPages);
    }

    public void printExitMessage() {
        System.out.println("---GOODBYE!---");
    }

    public void printAuthRequiredMessage() {
        System.out.println("Please, provide access for application.");
    }

    public void printUnknownCategoryMessage() {
        System.out.println("Unknown category name.");
    }

    public void printNoMorePagesMessage() {
        System.out.println("No more pages.");
    }

}
