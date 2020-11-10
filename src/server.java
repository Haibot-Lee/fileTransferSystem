import java.io.*;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class server {
    String sharedDir;

    //    ServerSocket udpSocket = new ServerSocket(9998);
    ServerSocket tcpSocket = new ServerSocket(9999);
    ArrayList<Socket> list = new ArrayList<Socket>();

    public server(String dirPath, String listPath) throws IOException {
//        System.out.println("Listening at UDP port 9998...");
        System.out.println("Listening at TCP port 9999...");

        sharedDir = dirPath + "\\";

        MemberDB memberDB = new MemberDB(listPath);

        while (true) {
            Socket memberSocket = tcpSocket.accept();

            synchronized (list) {
                list.add(memberSocket);
            }

            Thread t = new Thread(() -> {
                try {
                    if (loginCheck(memberSocket, memberDB))
                        receiveCmd(memberSocket);
                } catch (IOException e) {
                    System.err.println("connection dropped.");
                }
                synchronized (list) {
                    list.remove(memberSocket);
                }
            });
            t.start();
        }
    }

    private boolean loginCheck(Socket memberSocket, MemberDB memberDB) throws IOException {
        DataInputStream in = new DataInputStream(memberSocket.getInputStream());
        int len = in.readInt();
        byte[] buffer = new byte[len];
        in.read(buffer, 0, len);
        String loginMsg = new String(buffer);
        String[] loginInfo = loginMsg.split(" ");

        boolean ifLogin = false;
        String reply = "member does not exist";

        for (int i = 0; i < memberDB.getSize(); i++) {
            if (loginInfo[0].equals(memberDB.getMember(i).getName())) {
                if (loginInfo[1].equals(memberDB.getMember(i).getPassword())) {
                    ifLogin = true;
                    reply = "accept";
                    System.out.printf("Total %d clients are connected.\n", list.size());
                    System.out.printf("Established a connection to host %s:%d\n\n", memberSocket.getInetAddress(), memberSocket.getPort());
                    break;
                } else {
                    reply = "wrong password";
                    break;
                }
            }
        }

        reply(reply, memberSocket);
        return ifLogin;

    }

    private void reply(String reply, Socket destSocket) {
        synchronized (list) {
            try {
                byte[] data = reply.getBytes();
                int len = data.length;
                DataOutputStream out = new DataOutputStream(destSocket.getOutputStream());
                out.writeInt(len);
                out.write(data, 0, len);
            } catch (IOException e) {
                // the connection is dropped but the socket is not yet removed.
            }

        }
    }

    private void receiveCmd(Socket memberSocket) throws IOException {
        DataInputStream in = new DataInputStream(memberSocket.getInputStream());
        while (true) {
            int len = in.readInt();
            byte[] buffer = new byte[len];
            in.read(buffer, 0, len);
            String option = new String(buffer);
            String[] options = option.split(" ");
            System.out.println("Option: " + option);

            //TODO: realize the option from client
            switch (options[0]) {
                case "read":
                    read(options[1]);
                    break;
                case "create":
                    create(options[1]);
                    break;
                case "upload":
                    upload(memberSocket);
                    break;
                case "download":
                    download("sub", memberSocket);
                    break;
                case "deleteFile":
                    delete(options[1]);
                    break;
                case "rename":
                    rename(options[1], options[2]);
                    break;
                case "detail":
                    detail(options[1]);
                    break;
                default:
                    System.out.println("Invalid option");
                    break;
            }
        }
    }

    //option on shared root directory
    private void read(String pathname) {
        File path;
        if (pathname.equals(".")) {
            path = new File(sharedDir);
        } else {
            path = new File(sharedDir + pathname);
        }
        File[] files = path.listFiles();
        ArrayList<String> info = new ArrayList<>();

        for (File f : files) {
            if (f.isDirectory()) {
                info.add(String.format("%s %10s %s\n", new Date(f.lastModified()), "<DIR>", f.getName()));
            } else {
                info.add(String.format("%s %9dB %s\n", new Date(f.lastModified()), f.length(), f.getName()));
            }
        }

        for (String n : info) {
            System.out.print(n);
        }
    }

    private void create(String name) {
        File file = new File(sharedDir + name);
        if (file.exists()) {
            System.out.printf("%s exists!\n", file.isDirectory() ? "Directory" : "Filr");
        } else {
            file.mkdirs();
            System.out.println("Dir created");
        }
    }

    private void upload(Socket memberSocket) throws IOException {
        DataInputStream in = new DataInputStream(memberSocket.getInputStream());
        int len = in.readInt();
        byte[] buffer = new byte[len];
        in.read(buffer, 0, len);
        String[] fileInfo = (new String(buffer)).split(" ");
        File file = new File(sharedDir + fileInfo[0]);

        FileOutputStream outFile = new FileOutputStream(file);
        int size = Integer.parseInt(fileInfo[1]);
        int transCnt = size / 1024 + 1;
        for (int i = 0; i < transCnt; i++) {

            byte[] content = new byte[1024];
            int len2 = in.readInt();
            in.read(content, 0, len2);
            outFile.write(content, 0, len2);
            size -= 1024;
        }
        outFile.close();
        System.out.println("receive one file");
    }

    private void download(String name, Socket memberSocket) throws IOException {
        DataOutputStream out = new DataOutputStream(memberSocket.getOutputStream());

        File file = new File(sharedDir + name);
        if (!file.exists()) {
            String reply = "File does not exist";
            out.writeInt(reply.length());
            out.write(reply.getBytes(), 0, reply.length());
            return;
        }
        if (file.isDirectory()) {
            String reply = "Can not download directory";
            out.writeInt(reply.length());
            out.write(reply.getBytes(), 0, reply.length());
            return;
        }

        String fileName = file.getName();
        long fileSize = file.length();
        String fileInfo = String.format("%s %d", fileName, fileSize);
        out.writeInt(fileInfo.length());
        out.write(fileInfo.getBytes(), 0, fileInfo.length());

        FileInputStream inFile = new FileInputStream(file);
        while (fileSize > 0) {
            byte[] buffer = new byte[1024];
            int len = inFile.read(buffer);
            fileSize -= len;
            out.writeInt(len);
            out.write(buffer, 0, len);
        }
        inFile.close();
    }

    private void delete(String name) {
        File file = new File(sharedDir + name);
        if (file.exists()) {
            if (!file.isDirectory()) {
                file.delete();
                System.out.println("Delete successfully.");
            } else {
                File[] files = file.listFiles();
                if (files.length == 0) {
                    file.delete();
                    System.out.println("Delete successfully.");
                } else {
                    Scanner in = new Scanner(System.in);
                    System.out.println("The directory " + name + " is not empty! Do you still want to delete it?");
                    String op = in.nextLine();
                    if (op.equals("yes")) {
                        deleteAll(file);
                        System.out.println("Delete successfully.");
                    }
                }
            }
        } else {
            System.out.println("The file does not exist.");
        }
    }

    private void deleteAll(File file) {
        if (file.isFile()) {
            file.delete();
        } else {
            for (File files : file.listFiles()) {
                deleteAll(files);
            }
        }
        file.delete();
    }

    private void rename(String sourceName, String destName) {
        if (new File(sharedDir + sourceName).exists()) {
            if (!new File(sharedDir + destName).exists()) {
                new File(sharedDir + sourceName).renameTo(new File(sharedDir + destName));
            } else {
                System.out.println("The file exists.");
            }
        } else {
            System.out.println("The file doesn't exist");
        }
    }

    //calculate the size of the file and convert it to the version which we can directly read
    private String convertthesize(double value){
        String size = "";
        if (value < 1024) {
            size = value + "B";
        } else {
            value = new BigDecimal(value / 1024).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
        }
        if (value < 1024) {
            size = value + "KB";
        } else {
            value = new BigDecimal(value / 1024).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
        }
        if (value < 1024) {
            size = value + "MB";
        } else {
            value = new BigDecimal(value / 1024).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
            size = value + "GB";
        }
        return size;
    }

    private void detail(String filename) {
        File file = new File(sharedDir + filename);
        String size = "";
        long length = 0;
        String datefromate = "yyyy-MM-dd HH:mm:ss";
        String createdtime = "";
        String lastmodifiedtime = "";
        SimpleDateFormat sdf = new SimpleDateFormat(datefromate);
        String currenttime = "";
        String type = "";
        String NumberOfDir = "";
        String NumberOfFile = "";
        String root = "";
        String name = "";
        if (file.exists()) {
            //get name
            name = file.getName();

            //get the position
            root = file.getAbsolutePath();

            //get size which we can directly read
            size = convertthesize((double) file.length());

            //get the created time
            Path path = Paths.get(filename);
            BasicFileAttributeView basicview = Files.getFileAttributeView(path, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
            BasicFileAttributes attr;
            try {
                attr = basicview.readAttributes();
                Date createDate = new Date(attr.creationTime().toMillis());
                createdtime = sdf.format(createDate);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //get type
            if (file.isFile()) {
                //get last modified time
                Calendar cal = Calendar.getInstance();
                cal.set(1970, 0, 1, 0, 0, 0);
                lastmodifiedtime = sdf.format(cal.getTime());

                //get current time
                Date date = new Date();
                currenttime = sdf.format(date);

                //get type
                type = "File";
            } else {
                //get type
                type = "Directory";

                //get the numbers of the files and directory
                File[] files = file.listFiles();
                int numberofdir = 0;
                int numberoffile = 0;

                for (File f : files) {
                    if (f.isDirectory()) {
                        numberofdir++;
                    } else {
                        numberoffile++;
                    }
                }
                NumberOfDir = String.valueOf(numberofdir);
                NumberOfFile = String.valueOf(numberoffile);
            }

            //send the data to the client
            if (file.isFile()) {
                System.out.println("type: " + type);
                System.out.println("name: " + name);
                System.out.println("position: " + root);
                System.out.println("size: " + size);
                System.out.println("size: " + length);
                System.out.println("create time: " + createdtime);
                System.out.println("last modified time: " + lastmodifiedtime);
                System.out.println("interview time: " + currenttime);
            } else if (file.isDirectory()) {
                System.out.println("type: " + type);
                System.out.println("name: " + name);
                System.out.println("position: " + root);
                System.out.println("size: " + size);
                System.out.println("size: " + length);
                System.out.println("content: " + NumberOfFile + "file(s) and " + NumberOfDir + "directory");
                System.out.println("create time: " + createdtime);
            }
        } else {
            System.out.println("The file doesn't exist");
        }
    }

    //start server
    public static void main(String[] args) throws IOException {
        new server(args[0], args[1]);
    }
}
