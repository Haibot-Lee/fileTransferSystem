import javax.swing.*;
import java.awt.*;

public class UserInterface extends JFrame {

    public UserInterface() {
        this.setTitle("Welcome to the File Transfer System");
        this.setSize(new Dimension(320, 240));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if (loginPage() == true)
            this.setVisible(true);
    }

    public boolean loginPage() {
        JFrame login = new JFrame("Login");
        login.setSize(new Dimension(800, 400));
        login.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container container = login.getContentPane();
        container.setLayout(new GridLayout(1, 2));
        JPanel loginInfo = new JPanel();
        JPanel findServer = new JPanel();
        container.add(findServer);
        container.add(loginInfo);

        //Find server
        String[] lists = {"111", "222", "333", "444", "555", "666", "777"};
        JList<String> list = new JList<String>();
        list.setListData(lists);
        JScrollPane jsp = new JScrollPane(list);
        jsp.setBounds(50, 100, 200, 100);
        findServer.add(jsp);
        findServer.setLayout(new GridLayout());

        //Input login info.


        login.setVisible(true);

        return false;
    }

    public static void main(String[] args) {
        UserInterface ui = new UserInterface();
    }

}
