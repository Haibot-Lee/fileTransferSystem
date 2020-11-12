import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class UserInterface {
    Client user;
    JFrame loginPage;
    JFrame homePage;

    public UserInterface() {
        user = new Client();

        //login first
        loginPage();
    }

    public void loginPage() {
        loginPage = new JFrame("Login into The File Transfer System");
        loginPage.setSize(new Dimension(800, 400));
        loginPage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container container = loginPage.getContentPane();
        container.setLayout(new GridLayout(1, 2));
        JPanel findServer = new JPanel(new GridBagLayout());
        findServer.setBorder(new EmptyBorder(0, 5, 0, 5));
        JPanel loginInfo = new JPanel(new GridBagLayout());
        loginInfo.setBorder(new EmptyBorder(50, 5, 0, 20));
        container.add(findServer);
        container.add(loginInfo);

        //Server list
        JLabel serversLabel = new JLabel("Available server:");
        GridBagConstraints s0 = new GridBagConstraints();
        s0.gridy = 0;
        s0.anchor = GridBagConstraints.WEST;
        findServer.add(serversLabel, s0);

        JList<String> serverList = new JList<String>();
        JScrollPane jsp = new JScrollPane(serverList);
        GridBagConstraints s1 = new GridBagConstraints();
        s1.gridy = 1;
        s1.weightx = 100;
        s1.weighty = 80;
        s1.fill = GridBagConstraints.BOTH;
        findServer.add(jsp, s1);

        JButton broadcast = new JButton("Broadcast");
        GridBagConstraints s2 = new GridBagConstraints();
        s2.gridy = 2;
        s2.weighty = 5;
        s2.anchor = GridBagConstraints.EAST;
        findServer.add(broadcast, s2);

        //Input loginPage info.
        JLabel ipLabel = new JLabel("Input/Choose one Server:");
        GridBagConstraints l0 = new GridBagConstraints();
        l0.gridy = 0;
        l0.weightx = 80;
        l0.anchor = GridBagConstraints.SOUTHWEST;
        loginInfo.add(ipLabel, l0);

        JTextField serverIP = new JTextField();
        GridBagConstraints l1 = new GridBagConstraints();
        l1.gridy = 1;
        l1.weighty = 10;
        l1.anchor = GridBagConstraints.NORTH;
        l1.fill = GridBagConstraints.HORIZONTAL;
        loginInfo.add(serverIP, l1);

        JLabel nameLabel = new JLabel("Input name:");
        GridBagConstraints l2 = new GridBagConstraints();
        l2.gridy = 2;
        l2.anchor = GridBagConstraints.SOUTHWEST;
        loginInfo.add(nameLabel, l2);

        JTextField name = new JTextField();
        GridBagConstraints l3 = new GridBagConstraints();
        l3.gridy = 3;
        l3.weighty = 10;
        l3.anchor = GridBagConstraints.NORTH;
        l3.fill = GridBagConstraints.HORIZONTAL;
        loginInfo.add(name, l3);

        JLabel passwordLabel = new JLabel("Input password:");
        GridBagConstraints l4 = new GridBagConstraints();
        l4.gridy = 4;
        l4.anchor = GridBagConstraints.SOUTHWEST;
        loginInfo.add(passwordLabel, l4);

        JPasswordField password = new JPasswordField();
        GridBagConstraints l5 = new GridBagConstraints();
        l5.gridy = 5;
        l5.weighty = 10;
        l5.anchor = GridBagConstraints.NORTH;
        l5.fill = GridBagConstraints.HORIZONTAL;
        loginInfo.add(password, l5);

        JButton submit = new JButton("Login");
        GridBagConstraints l6 = new GridBagConstraints();
        l6.gridy = 6;
        l6.weighty = 10;
        l6.anchor = GridBagConstraints.EAST;
        loginInfo.add(submit, l6);

        //ActionListeners
        broadcast.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] list = new String[20];
                list[0] = "127.0.0.1";
                for (int i = 1; i < list.length; i++) {
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

        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    user.login(serverIP.getText(), name.getText(), new String(password.getPassword()));
                    String reply = user.getReply();
                    if (reply.equals("accept")) {
                        loginPage.setVisible(false);
                        homePage(name.getText());
                    } else {
                        JOptionPane.showMessageDialog(null, reply, "Failed to login", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ioException) {
                    JOptionPane.showMessageDialog(null, ioException, "Failed to login", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        loginPage.setVisible(true);
    }

    public void homePage(String memberName) {
        homePage = new JFrame(memberName + ": welcome to the File Transfer System");
        homePage.setSize(new Dimension(800, 600));
        homePage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container container = homePage.getContentPane();
        container.setLayout(new GridBagLayout());

        homePage.setVisible(true);
    }

    public static void main(String[] args) {
        //start Server
        Thread server = new Thread(() -> {
            try {
                new Server(args[0], args[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.start();

        UserInterface ui = new UserInterface();
    }

}
