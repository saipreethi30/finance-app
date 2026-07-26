package com.smartfinance.javafx;

import com.smartfinance.service.FinanceContext;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * JavaFX application entry — charts, modern UI, full feature set.
 */
public class JavaFxApp extends Application {

    private final FinanceContext context = new FinanceContext();

    @Override
    public void start(Stage stage) {
        LoginController login = new LoginController(context, () -> {
            DashboardController dashboard = new DashboardController(context, stage);
            dashboard.show();
        });
        login.show(stage);
        stage.show();
    }
}
