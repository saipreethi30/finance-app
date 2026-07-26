package com.smartfinance;

import com.smartfinance.console.ConsoleApp;
import com.smartfinance.javafx.JavaFxApp;
import com.smartfinance.swing.SwingApp;

import javax.swing.SwingUtilities;
import java.util.Scanner;

/**
 * Main launcher — choose Console, Swing, or JavaFX UI.
 *
 * Run: mvn javafx:run
 * Or:  java -jar target/smart-finance-tracker-1.0.0.jar
 */
public class SmartFinanceApp {

    public static void main(String[] args) {
        if (args.length > 0) {
            launchMode(args[0]);
            return;
        }

        System.out.println("===========================================");
        System.out.println("     SMART FINANCE TRACKER");
        System.out.println("===========================================");
        System.out.println("  1. Console (text menu)");
        System.out.println("  2. Swing   (desktop GUI)");
        System.out.println("  3. JavaFX  (charts + modern UI)");
        System.out.println("  0. Exit");
        System.out.print("Choose UI: ");

        Scanner scanner = new Scanner(System.in);
        String choice = scanner.nextLine().trim();
        launchMode(choice);
    }

    private static void launchMode(String choice) {
        switch (choice) {
            case "1", "console" -> new ConsoleApp().start();
            case "2", "swing" -> SwingUtilities.invokeLater(() -> new SwingApp().start());
            case "3", "javafx" -> JavaFxApp.launch(JavaFxApp.class);
            case "0", "exit" -> System.out.println("Bye.");
            default -> System.out.println("Unknown option. Use 1, 2, or 3.");
        }
    }
}
