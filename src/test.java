import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class test {
    public static void main(String[] args) {

        localFileTree("tree");
    }

    private static void localFileTree(String title) {
        final String[] returnPath = {null};

        JDialog local = new JDialog(new JFrame(), title);
        local.setSize(new Dimension(400, 400));
        Container container = local.getContentPane();
        container.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        JButton confirm = new JButton("Confirm");
        JButton cancel = new JButton("Cancel");
        buttonPanel.add(confirm);
        buttonPanel.add(cancel);
        container.add(buttonPanel, BorderLayout.SOUTH);

        //Tree
        JTree localTree = new JTree();
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root", true);
//        localTree.setRootVisible(false);

        for (Path disk : FileSystems.getDefault().getRootDirectories()) {
            DefaultMutableTreeNode diskNode = new DefaultMutableTreeNode(disk.toString(), true);
            root.add(diskNode);
        }

        DefaultTreeModel treeModel = new DefaultTreeModel(root, true);
        localTree.setModel(treeModel);
        JScrollPane scrollPane = new JScrollPane(localTree);
        container.add(scrollPane, BorderLayout.CENTER);
        local.setVisible(true);

        //ActionListeners
        //get the path of file tree
        localTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if (!localTree.isSelectionEmpty()) {
                    TreePath selectionPath = localTree.getSelectionPath();
                    Object[] paths = selectionPath.getPath();

                    String current = "";
                    for (int i = 1; i < paths.length; i++) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[i];
                        current += node.getUserObject();
                        if (i != 1 && i != paths.length - 1) current += "\\";
                    }

                    System.out.println("?: " + current);
                }
            }
        });

        //Expand Node
        localTree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                TreePath path = event.getPath();
                Object[] paths = path.getPath();

                String current = "";
                DefaultMutableTreeNode node = null;
                for (int i = 1; i < paths.length; i++) {
                    node = (DefaultMutableTreeNode) paths[i];
                    current += node.getUserObject();
                    if (i != 1) current += "\\";
                }
                getLocalFiles(current, node);
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {

            }
        });

        confirm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                local.dispose();
            }
        });
    }

    private static void getLocalFiles(String path, DefaultMutableTreeNode node) {
        File[] files = (new File(path)).listFiles();

        if (files != null) {
            for (File f : files) {
                DefaultMutableTreeNode temp;
                if (f.isDirectory()) {
                    temp = new DefaultMutableTreeNode(f.getName(), true);
                } else {
                    temp = new DefaultMutableTreeNode(f.getName(), false);
                }
                node.add(temp);
            }
        }

    }
}