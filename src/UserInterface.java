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

public class UserInterface {
    Client user;
    JFrame loginPage;
    JFrame homePage;
    JTree fileTree;
    String currentTreePath = "";
    String parentTreePath = "";
    String createAt = "";

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
        s0.weightx = 50;
        s0.anchor = GridBagConstraints.WEST;
        findServer.add(serversLabel, s0);

        JList<String> serverList = new JList<String>();
//        String[] list = {};
//        try {
//            user.broadcasts(3);
//            list = user.getServer();
//        } catch (IOException ioException) {
//            ioException.printStackTrace();
//        }
//        serverList.setListData(list);
        JScrollPane jsp = new JScrollPane(serverList);
        GridBagConstraints s1 = new GridBagConstraints();
        s1.gridy = 1;
        s1.gridwidth = 2;
        s1.weighty = 80;
        s1.fill = GridBagConstraints.BOTH;
        findServer.add(jsp, s1);

        JButton broadcast = new JButton("Broadcast");
        GridBagConstraints s3 = new GridBagConstraints();
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
                String[] list = {};
                try {
                    user.broadcasts(3);
                    list = user.getServer();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                serverList.setListData(list);
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
        int noOfButtons = 8;
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
        buttons[7] = new JButton("Refresh");
        for (int i = 0; i < buttons.length; i++) {
            control.add(buttons[i]);
        }

        //ActionListeners

        //get the path of file tree
        fileTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (!fileTree.isSelectionEmpty()) {
                    TreePath selectionPath = fileTree.getSelectionPath();
                    Object[] paths = selectionPath.getPath();

                    if (paths.length == 1) {
                        currentTreePath = "";
                        parentTreePath = "";
                        createAt = "";
                        return;
                    }

                    String current = "";
                    for (int i = 1; i < paths.length - 1; i++) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i];
                        current += "\\" + node.getUserObject();
                    }

                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[paths.length - 1];
                    currentTreePath = current + "\\" + node.getUserObject();
                    parentTreePath = current;
                    if (node.getAllowsChildren()) {
                        createAt = currentTreePath;
                    } else {
                        createAt = parentTreePath;
                    }

                    System.out.println(currentTreePath);
                    System.out.println(createAt);
                    System.out.println(parentTreePath);
                }
            }
        });

        //Logout
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

        //Create
        buttons[1].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String type = (String) JOptionPane.showInputDialog(homePage, "Create file or folder?", "Create", JOptionPane.PLAIN_MESSAGE, null, new String[]{"Folder", "File"}, null);
                    if (type == null) return;

                    String fileName;
                    String display = "Please input the name: ";
                    do {
                        do {
                            fileName = JOptionPane.showInputDialog(homePage, display);
                            if (fileName == null) return;
                            display = type + " name can not be null!";
                        } while (fileName.equals(""));

                        user.sendMsg("create>" + type + ">" + createAt + "\\" + fileName);
                        display = type + " exist already! Use another name: ";
                    } while (user.getReply().equals("Exists already"));

                    JOptionPane.showMessageDialog(homePage, "Created!", "", JOptionPane.INFORMATION_MESSAGE);
                    fileTree = constructTree(fileTree);

                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        //Upload
        buttons[2].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String fileName = "";
                while (fileName.equals("")) {
                    fileName = JOptionPane.showInputDialog(homePage, "Input one file you want to upload:", "Upload", JOptionPane.YES_NO_CANCEL_OPTION);
                    fileName = "members.txt";
                    if (fileName == null) return;
                }
                try {
                    user.sendMsg("upload>" + createAt);
                    JOptionPane.showMessageDialog(homePage, user.upload(fileName), "", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        //Download
        buttons[3].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!currentTreePath.equals("")) {
                    try {
                        user.sendMsg("download>" + currentTreePath);
                        String reply = user.download();
                        if (reply.equals("Can not download directory")) {
                            JOptionPane.showMessageDialog(homePage, reply, "", JOptionPane.WARNING_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(homePage, reply, "", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(homePage, "Choose one file that you want to download first!", "", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        //Delete
        buttons[4].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!currentTreePath.equals("")) {
                    try {
                        user.sendMsg("delete>" + currentTreePath);
                        String getmsg = user.getReply();
                        if (getmsg.equals("It is not empty. Do you still want to delete it?")) {
                            int n = JOptionPane.showConfirmDialog(homePage, "It is not empty, do you want to delete it anyway?", "", JOptionPane.YES_NO_OPTION);
                            if (n == 0) {
                                user.sendMsg("yes");
                                JOptionPane.showMessageDialog(homePage, user.getReply(), "", JOptionPane.INFORMATION_MESSAGE);
                                fileTree = constructTree(fileTree);
                            } else {
                                user.sendMsg("no");
                                JOptionPane.showMessageDialog(homePage, user.getReply(), "", JOptionPane.INFORMATION_MESSAGE);
                            }
                        } else {
                            JOptionPane.showMessageDialog(homePage, getmsg, "", JOptionPane.INFORMATION_MESSAGE);
                            fileTree = constructTree(fileTree);
                        }
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(homePage, "Choose one file that you want to delete first!", "", JOptionPane.WARNING_MESSAGE);
                }


            }
        });

        //Rename
        buttons[5].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!currentTreePath.equals("")) {
                    String newname = "";
                    String display = "PLease input a new name: ";
                    try {
                        //this "while" is used to avoid the empty name during using
                        while (newname.equals("")) {
                            newname = JOptionPane.showInputDialog(homePage, display);
                            //to handle cancel option
                            if (newname == null) return;

                            if (!newname.equals("")) {
                                user.sendMsg("rename>" + currentTreePath + ">" + parentTreePath + "\\" + newname);

                                if (user.getReply().equals("Renamed successfully")) {
                                    JOptionPane.showMessageDialog(homePage, "Renamed successfully", "", JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    display = "The file exists. please input a new name: ";
                                    newname = "";
                                }

                            } else {
                                JOptionPane.showMessageDialog(homePage, "The name cannot be empty!", "", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(homePage, "Choose one file that you want to rename first!", "", JOptionPane.WARNING_MESSAGE);
                }
                fileTree = constructTree(fileTree);
            }
        });

        //Detail
        buttons[6].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!currentTreePath.equals("")) {
                    String[] details = {};
                    try {
                        user.sendMsg("detail>" + currentTreePath);
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
                    JOptionPane.showMessageDialog(homePage, "Choose one file that you want to read details first!", "", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        //Refresh
        buttons[7].addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileTree = constructTree(fileTree);
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
            user.sendMsg("read>" + path);
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
                new Server("C:\\Users\\mrli\\Desktop", "members.txt");
//                new Server(args[0], args[1]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        server.start();

        UserInterface ui = new UserInterface();
    }
}
