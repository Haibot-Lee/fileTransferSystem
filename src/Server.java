import java.io.*;
import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class Server {
    String sharedDir;

    DatagramSocket udpSocket = new DatagramSocket(9998);
    ServerSocket tcpSocket = new ServerSocket(9999);
    ArrayList<Socket> list = new ArrayList<Socket>();

    public Server(String dirPath, String listPath) throws IOException {
        sharedDir = dirPath + "\\";
        MemberDB memberDB = new MemberDB(listPath);

        System.out.println("Listening at UDP port 9998...");
        System.out.println("Listening at TCP port 9999...");

        //UDP socket
        Thread udp = new Thread(() -> {
            try {
                int i = 0;
                while (true) {
                    DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                    udpSocket.receive(packet);
                    byte[] data = packet.getData();
                    String msg = new String(data, 0, packet.getLength());

                    if (msg.equals("Finding server...")) {
                        System.out.println("Request" + i);
                        System.out.println(packet.getAddress());
                        System.out.println(packet.getPort());

                        String reply = "availableServer " + System.getenv("COMPUTERNAME");
                        System.out.println(reply);
                        udpSocket.send(new DatagramPacket(reply.getBytes(), reply.length(), packet.getAddress(), packet.getPort()));
                        i++;
                    }
                }
            } catch (IOException e) {
                System.err.println("UDP connection dropped.");
            }
        });
        udp.start();

        //TCP socket
        while (true) {
            Socket memberSocket = tcpSocket.accept();

            synchronized (list) {
                list.add(memberSocket);
            }

            Thread tcp = new Thread(() -> {
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
            tcp.start();
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
        String reply = "Member does not exist!";

        for (int i = 0; i < memberDB.getSize(); i++) {
            if (loginInfo[0].equals(memberDB.getMember(i).getName())) {
                if (loginInfo[1].equals(memberDB.getMember(i).getPassword())) {
                    ifLogin = true;
                    reply = "accept";
                    System.out.printf("Total %d clients are connected.\n", list.size());
                    System.out.printf("Established a connection to host %s:%d\n\n", memberSocket.getInetAddress(), memberSocket.getPort());
                    break;
                } else {
                    reply = "Wrong password!";
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
                System.err.println("The connection is dropped but the socket is not yet removed");
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

            switch (options[0]) {
                case "read":
                    read(options[1], memberSocket);
                    break;
                case "create":
                    create(options[1], memberSocket);
                    break;
                case "upload":
                    upload(options[1], memberSocket);
                    break;
                case "download":
                    download(options[1], memberSocket);
                    break;
                case "delete":
                    delete(options[1], memberSocket);
                    break;
                case "rename":
                    rename(options[1], options[2], memberSocket);
                    break;
                case "detail":
                    detail(options[1], memberSocket);
                    break;
                default:
                    System.out.println("Invalid option");
                    break;
            }
        }
    }

    //option on shared root directory
    private void read(String fileName, Socket memberSocket) {
        File path;
        if (fileName.equals(".")) {
            path = new File(sharedDir);
        } else {
            path = new File(sharedDir + fileName);
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

    private void create(String path, Socket memberSocket) {
        File file = new File(sharedDir + path);
        String reply = "";

        if (file.exists()) {
            reply = String.format("%s exists!", file.isDirectory() ? "Directory" : "File");
        } else {
            file.mkdirs();
            reply = "Dir(" + path + ") created";
        }

        System.out.println(reply);
        reply(reply, memberSocket);
    }

    private void upload(String path, Socket memberSocket) throws IOException {
        DataInputStream in = new DataInputStream(memberSocket.getInputStream());
        int len = in.readInt();
        byte[] buffer = new byte[len];
        in.read(buffer, 0, len);
        String[] fileInfo = (new String(buffer)).split(" ");
        File file = new File(sharedDir + path + "\\" + fileInfo[0]);

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

    private void download(String path, Socket memberSocket) throws IOException {
        DataOutputStream out = new DataOutputStream(memberSocket.getOutputStream());

        File file = new File(sharedDir + path);
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

    private void delete(String fileName, Socket memberSocket) {
        File file = new File(sharedDir + fileName);
        String reply = "";

        if (file.exists()) {
            if (!file.isDirectory()) {
                file.delete();
                reply = "Delete successfully";
            } else {
                File[] files = file.listFiles();
                if (files.length == 0) {
                    file.delete();
                    reply = "Delete successfully";
                } else {
                    Scanner in = new Scanner(System.in);
                    System.out.println("The directory " + fileName + " is not empty! Do you still want to delete it?");
                    String op = in.nextLine();
                    if (op.equals("yes")) {
                        deleteAll(file);
                        reply = "Delete successfully";
                    }
                }
            }
        } else {
            reply = "The file does not exist";
        }

        System.out.println(reply);
        reply(reply, memberSocket);
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

    private void rename(String sourceName, String destName, Socket memberSocket) {
        String reply = "";

        if (new File(sharedDir + sourceName).exists()) {
            if (!new File(sharedDir + destName).exists()) {
                new File(sharedDir + sourceName).renameTo(new File(sharedDir + destName));
            } else {
                reply = "The file exists";
            }
        } else {
            reply = "The file doesn't exist";
        }

        System.out.println(reply);
        reply(reply, memberSocket);
    }

    //calculate the size of the file and convert it to the version which we can directly read
    private String convertthesize(double value) {
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

    private void detail(String fileName, Socket memberSocket) {
        File file = new File(sharedDir + fileName);
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
            Path path = Paths.get(fileName);
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
            String reply = "";
            if (file.isFile()) {
                reply = "type: " + type + "\nname: " + name + "\nposition: " + root + "\nsize: " + size + "\nsize: " + length + "\ncreate time: " + createdtime + "\nlast modified time: " + lastmodifiedtime
                        + "\ninterview time: " + currenttime;
//                System.out.println("type: " + type);
//                System.out.println("name: " + name);
//                System.out.println("position: " + root);
//                System.out.println("size: " + size);
//                System.out.println("size: " + length);
//                System.out.println("create time: " + createdtime);
//                System.out.println("last modified time: " + lastmodifiedtime);
//                System.out.println("interview time: " + currenttime);
            } else if (file.isDirectory()) {
                reply = "type: " + type + "\nname: " + name + "\nposition: " + root + "\nsize: " + size + "\nsize: " + length + "\ncreate time: " + createdtime +
                        "\ncontent: " + NumberOfFile + "file(s) and " + NumberOfDir + "directory";
//                System.out.println("type: " + type);
//                System.out.println("name: " + name);
//                System.out.println("position: " + root);
//                System.out.println("size: " + size);
//                System.out.println("size: " + length);
//                System.out.println("content: " + NumberOfFile + "file(s) and " + NumberOfDir + "directory");
//                System.out.println("create time: " + createdtime);
            }
            System.out.println(reply);
            reply(reply, memberSocket);
        } else {
            System.out.println("The file doesn't exist");
        }
    }

    //start server
    public static void main(String[] args) throws IOException {
        new Server(args[0], args[1]);
//        new Server("", "members.txt");
    }
}
