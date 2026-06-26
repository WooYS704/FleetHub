package view;

import controller.VehicleController;
import javax.swing.SwingUtilities;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame view = new MainFrame();
            new VehicleController(view);
            view.setVisible(true);
        });
    }
}