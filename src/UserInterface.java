import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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
        JPanel findServer = new JPanel(new GridBagLayout());
        JPanel loginInfo = new JPanel(new GridLayout(7, 1));
        container.add(findServer);
        container.add(loginInfo);

        //Server list
        JLabel serversLabel = new JLabel("Choose one Server:");
        GridBagConstraints s0 = new GridBagConstraints();
        s0.gridx = 0;
        s0.gridy = 0;
        s0.anchor = GridBagConstraints.WEST;
        findServer.add(serversLabel, s0);

        JList<String> serverList = new JList<String>();
        JScrollPane jsp = new JScrollPane(serverList);
        GridBagConstraints s1 = new GridBagConstraints();
        s1.gridx = 0;
        s1.gridy = 1;
        s1.weightx = 100;
        s1.weighty = 80;
        s1.fill = GridBagConstraints.BOTH;
        findServer.add(jsp, s1);

        JButton broadcast = new JButton("Broadcast");
        GridBagConstraints s2 = new GridBagConstraints();
        s2.gridx = 0;
        s2.gridy = 2;
        s2.weighty = 5;
        s2.anchor = GridBagConstraints.EAST;
        findServer.add(broadcast, s2);

        //Input login info.
        JLabel ipLabel = new JLabel("Input/Choose one Server:");
        loginInfo.add(ipLabel);
        JTextField serverIP = new JTextField();
//        serverIP.setPreferredSize(new Dimension(200, 30));
        loginInfo.add(serverIP);

        JLabel nameLabel = new JLabel("Input name:");
        loginInfo.add(nameLabel);
        JTextField name = new JTextField();
//        name.setPreferredSize(new Dimension(200, 30));
        loginInfo.add(name);

        JLabel passwordLabel = new JLabel("Input name:");
        loginInfo.add(passwordLabel);
        JPasswordField password = new JPasswordField();
//        password.setPreferredSize(new Dimension(200, 30));
        loginInfo.add(password);

        JButton submit = new JButton("Login");
        loginInfo.add(submit);

        //ActionListeners
        broadcast.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] list = new String[20];
                for (int i = 0; i < list.length; i++) {
                    list[i] = "item" + i;
                }
                serverList.setListData(list);
            }
        });

        serverList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                serverIP.setText(serverList.getSelectedValue());
            }
        });

        login.setVisible(true);
        return false;
    }

    public static void main(String[] args) {
        //start Server


        UserInterface ui = new UserInterface();
    }

}
