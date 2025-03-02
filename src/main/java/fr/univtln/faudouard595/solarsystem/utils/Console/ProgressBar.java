package fr.univtln.faudouard595.solarsystem.utils.Console;

public class ProgressBar {
    private static int total;
    private static int progress;
    private static String startMessage;
    public static int barLength = 50;

    public static void init(int total) {
        init(total, "Progress : ");
    }

    public static void update() {
        progress++;
        printProgressBar();
    }

    public static void init(int total, String startMessage) {
        ProgressBar.total = total;
        ProgressBar.progress = 0;
        ProgressBar.startMessage = startMessage;
    }

    public static void printProgressBar() {
        double percentage = (double) progress / total;
        int progressLength = (int) (barLength * percentage);

        // Print the progress bar
        StringBuilder progressBar = new StringBuilder();
        progressBar.append("[");

        // Add the filled portion
        for (int i = 0; i < progressLength; i++) {
            progressBar.append("#");
        }

        // Add the unfilled portion
        for (int i = progressLength; i < barLength; i++) {
            progressBar.append(" ");
        }

        progressBar.append("] ");
        progressBar.append(progress + "/" + total); // Display percentage

        // Print the progress bar to the console
        System.out.print("\r" + startMessage + progressBar.toString());
    }

}
