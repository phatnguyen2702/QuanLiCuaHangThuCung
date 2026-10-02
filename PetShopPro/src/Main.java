import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import ui.MainFrame;

public class Main {
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
