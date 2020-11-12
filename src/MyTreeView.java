import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

public class MyTreeView extends JFrame {
    public MyTreeView() throws IOException {
        JFrame homePage = new JFrame(": welcome to the File Transfer System");
        homePage.setSize(new Dimension(800, 600));
        homePage.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container container = homePage.getContentPane();
        container.setLayout(new GridBagLayout());

        JTree tree = new JTree();
        GridBagConstraints p1 = new GridBagConstraints();
        p1.weightx = 80;
        p1.fill = GridBagConstraints.BOTH;
        container.add(tree, p1);

        DefaultMutableTreeNode node = new DefaultMutableTreeNode("Hello");
        node.add(new DefaultMutableTreeNode("World"));
        DefaultTreeModel tmodel = new DefaultTreeModel(node);
        tree.setModel(tmodel);

        JPanel buttons = new JPanel(new GridLayout(5, 1));
        GridBagConstraints p2 = new GridBagConstraints();
        p2.gridx = 1;
        p2.weightx = 20;
        container.add(buttons, p2);

        homePage.setVisible(true);
    }

    public static void main(String[] args) throws IOException {
        new MyTreeView();
    }
}
