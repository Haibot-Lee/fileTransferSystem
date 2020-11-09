import javax.swing.*;

public class userInterface extends JFrame {

    public static void loginPage() {
        JFrame login = new JFrame("File Transfer System");
        login.setSize(900, 300);
        login.setVisible(true);
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        loginPage();
    }

}
