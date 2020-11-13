import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class UserInterface {
    Client user;
    JFrame loginPage;
    JFrame homePage;
    JTree fileTree;
    String currentTreePath = "";

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
        JLabel serversLabel = new JLabel("Available servers:");
        GridBagConstraints s0 = new GridBagConstraints();
        s0.gridy = 0;
        s0.gridwidth = 2;
        s0.weightx = 50;
        s0.anchor = GridBagConstraints.WEST;
        findServer.add(serversLabel, s0);

        JList<String> serverList = new JList<String>();
        JScrollPane jsp = new JScrollPane(serverList);
        GridBagConstraints s1 = new GridBagConstraints();
        s1.gridy = 1;
        s1.gridwidth = 2;
        s1.weighty = 80;
        s1.fill = GridBagConstraints.BOTH;
        findServer.add(jsp, s1);

        JLabel find = new JLabel();
        GridBagConstraints s2 = new GridBagConstraints();
        s2.gridy = 2;
        s2.weighty = 5;
        s2.anchor = GridBagConstraints.WEST;
        findServer.add(find, s2);
        find.setVisible(false);

        JButton broadcast = new JButton("Broadcast");
        GridBagConstraints s3 = new GridBagConstraints();
        s3.gridx = 1;
        s3.gridy = 2;
        s3.weighty = 5;
        s3.anchor = GridBagConstraints.EAST;
        findServer.add(broadcast, s3);


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
                try {
                    user.broadcasts(5);
                    user.receiveIP();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                String[] list = new String[user.serversIP.size()];
                for (int i = 0; i < list.length; i++) {
                    list[i] = user.serversName.get(i) + " (IP address: " + user.serversIP.get(i) + ")";
                }
                serverList.setListData(list);
                find.setText(list.length + " server(s) are found!");
                find.setVisible(true);
            }
        });

        serverList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                String ip = serverList.getSelectedValue();
                ip = ip.substring(ip.lastIndexOf("/") + 1, ip.length() - 1);
                serverIP.setText(ip);
            }
        });

        submit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
//                    user.login(serverIP.getText(), name.getText(), new String(password.getPassword()));
                    user.login("127.0.0.1", "amy", "123");
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

        //tree
        fileTree = new JTree();
        JScrollPane jsp = new JScrollPane(fileTree);
        GridBagConstraints p1 = new GridBagConstraints();
        p1.weightx = 80;
        p1.weighty = 100;
        p1.fill = GridBagConstraints.BOTH;
        container.add(jsp, p1);

        fileTree = constructTree(fileTree);

        TreeSelectionModel treeSelect = fileTree.getSelectionModel();
        treeSelect.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        //control
        int noOfButtons = 7;
        JPanel control = new JPanel(new GridLayout(noOfButtons, 1));
        GridBagConstraints p2 = new GridBagConstraints();
        p2.gridx = 1;
        p2.weightx = 20;
        p2.fill = GridBagConstraints.BOTH;
        container.add(control, p2);

        JButton[] buttons = new JButton[noOfButtons];
        buttons[0] = new JButton("Logout");
        buttons[1] = new JButton("Create");
        buttons[2] = new JButton("Upload");
        buttons[3] = new JButton("Download");
        buttons[4] = new JButton("Delete");
        buttons[5] = new JButton("Rename");
        buttons[6] = new JButton("Detail");
        for (int i = 0; i < buttons.length; i++) {
            control.add(buttons[i]);
        }

        //ActionListeners
        fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (!fileTree.isSelectionEmpty()) {
                    TreePath selectionPath = fileTree.getSelectionPath();
                    Object[] paths = selectionPath.getPath();

                    String path = "";
                    for (int i = 1; i < paths.length; i++) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i];
                        path += "\\" + node.getUserObject();
                    }

                    currentTreePath = path;
                    System.out.println(path);
                }
            }
        });

        buttons[0].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                homePage.setVisible(false);
                loginPage.setVisible(true);
                try {
                    user.tcpSocket.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        buttons[1].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileTree = constructTree(fileTree);
            }
        });

        buttons[2].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        buttons[3].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        buttons[4].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        buttons[5].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        buttons[6].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!currentTreePath.equals("")) {
                    String[] details = {};
                    try {
                        user.sendCmd("detail>" + currentTreePath);
                        details = user.getReply().split("\n");
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                    int rows = details.length;
                    JDialog detail = new JDialog(homePage, "Detail");
                    Container container = detail.getContentPane();
                    container.setLayout(new GridLayout(rows, 1));
                    for (int i = 0; i < rows; i++) {
                        container.add(new JLabel(details[i]));
                    }
                    detail.setBounds(new Rectangle(300, 300));
                    detail.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null, "Choose one file that you want to read details first!", "", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        homePage.setVisible(true);
    }

    private JTree constructTree(JTree tree) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root", true);
        getFiles("", root);
        DefaultTreeModel treeModel = new DefaultTreeModel(root, true);
        tree.setModel(treeModel);

        return tree;
    }

    private void getFiles(String path, DefaultMutableTreeNode node) {
        try {
            user.sendCmd("read>" + path);
            String reply = user.getReply();
            if (!reply.equals("")) {
                String[] files = reply.split("\n");
                for (int i = 0; i < files.length; i++) {
                    String fileName = files[i].substring(files[i].lastIndexOf("/") + 1);
                    DefaultMutableTreeNode temp;
                    if (files[i].charAt(0) == 'D' || files[i].charAt(0) == 'M') {
                        temp = new DefaultMutableTreeNode(fileName, true);
                    } else {
                        temp = new DefaultMutableTreeNode(fileName, false);
                    }
                    node.add(temp);
                    if (files[i].charAt(0) == 'D') {
                        getFiles(path + "\\" + fileName, temp);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //start Server
        Thread server = new Thread(() -> {
            try {
                new Server("C:\\Users\\e8252125", "members.txt");
//                new Server(args[0], args[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.start();

        UserInterface ui = new UserInterface();
    }

}
