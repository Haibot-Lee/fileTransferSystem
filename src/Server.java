import java.io.*;
import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Server {
    String sharedDir;

    DatagramSocket udpSocket = new DatagramSocket(9998);
    ServerSocket tcpSocket = new ServerSocket(9999);
    ArrayList<Socket> list = new ArrayList<Socket>();

    public Server(String dirPath, String listPath) throws IOException {
        sharedDir = dirPath;
        MemberDB memberDB = new MemberDB(listPath);

        System.out.println("Listening at UDP port 9998...");
        System.out.println("Listening at TCP port 9999...");

        //UDP socket
        Thread udp = new Thread(() -> {
            try {
                while (true) {
                    DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                    udpSocket.receive(packet);
                    byte[] data = packet.getData();
                    String msg = new String(data, 0, packet.getLength());

                    if (msg.equals("Finding server...")) {
                        System.out.println(packet.getAddress() + " is finding server");
                        String reply = "availableServer " + System.getenv("COMPUTERNAME");
                        udpSocket.send(new DatagramPacket(reply.getBytes(), reply.length(), packet.getAddress(), packet.getPort()));
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

    private String getReply(Socket memberSocket) throws IOException {
        DataInputStream in = new DataInputStream(memberSocket.getInputStream());
        int len = in.readInt();
        byte[] buffer = new byte[len];
        in.read(buffer, 0, len);
        String info = new String(buffer);
        return info;
    }

    private void receiveCmd(Socket memberSocket) throws IOException {
        DataInputStream in = new DataInputStream(memberSocket.getInputStream());

        while (true) {
            int len = in.readInt();
            byte[] buffer = new byte[len];
            in.read(buffer, 0, len);
            String option = new String(buffer);
            String[] options = option.split(">");
            System.out.println("Option: " + option);

            switch (options[0]) {
                case "read":
                    if (options.length == 1) {
                        read("", memberSocket);
                    } else {
                        read(options[1], memberSocket);
                    }
                    break;
                case "create":
                    create(options[1], options[2], memberSocket);
                    break;
                case "upload":
                    if (options.length == 1) {
                        upload("", memberSocket);
                    } else {
                        upload(options[1], memberSocket);
                    }
                    break;
                case "download":
                    download(options[1], memberSocket);
                    break;
                case "delete":
                    delete(options[1], memberSocket);
                    break;
                case "rename":
                    if (options.length == 3) {
                        rename(options[1], options[2], memberSocket);
                    } else {
                        reply("Invalid option", memberSocket);
                    }
                    break;
                case "detail":
                    detail(options[1], memberSocket);
                    break;
                default:
                    reply("Invalid option", memberSocket);
                    break;
            }
        }
    }

    //option on shared root directory
    private void read(String fileName, Socket memberSocket) {
        File path;
        if (fileName.equals("")) {
            path = new File(sharedDir);
        } else {
            path = new File(sharedDir + fileName);
        }

        String reply = "";

        File[] files = path.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    reply += "D/";
                } else {
                    reply += "F/";
                }
                reply += f.getName() + "\n";
            }
        }

        reply(reply, memberSocket);
    }

    private void create(String type, String path, Socket memberSocket) throws IOException {
        File file = new File(sharedDir + path);
        String reply = "";

        if (!file.exists()) {
            if (type.equals("Folder")) {
                file.mkdirs();
                reply = "Created";
            } else {
                file.createNewFile();
                reply = "Created";
            }
        } else {
            reply = "Exists already";
        }
        System.out.println(reply);
        reply(reply, memberSocket);
    }

    private void upload(String path, Socket memberSocket) throws IOException {
        String[] fileInfo = getReply(memberSocket).split(">");
        File file = new File(sharedDir + path + "\\" + fileInfo[0]);
        if (file.exists()) {
            reply("Exists", memberSocket);
        } else {
            reply("Can upload", memberSocket);
        }
        String reply = getReply(memberSocket);
        if (reply.equals("Stop upload")) {
            return;
        }

        DataInputStream in = new DataInputStream(memberSocket.getInputStream());
        FileOutputStream outFile = new FileOutputStream(file);
        int size = Integer.parseInt(fileInfo[1]);
        int transCnt = 0;
        if (size > 0) {
            transCnt = size / 1024 + 1;
        }
        for (int i = 0; i < transCnt; i++) {

            byte[] content = new byte[1024];
            int len = in.readInt();
            in.read(content, 0, len);
            outFile.write(content, 0, len);
            size -= 1024;
        }
        outFile.close();
    }

    private void download(String path, Socket memberSocket) throws IOException {
        File file = new File(sharedDir + path);

        String fileName = file.getName();
        long fileSize = file.length();
        String fileInfo = String.format("%s>%d", fileName, fileSize);
        reply(fileInfo, memberSocket);

        DataOutputStream out = new DataOutputStream(memberSocket.getOutputStream());
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

    private void delete(String fileName, Socket memberSocket) throws IOException {
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
                    reply("It is not empty. Do you still want to delete it?", memberSocket);
                    if (getReply(memberSocket).equals("yes")) {
                        deleteAll(file);
                        reply = "Delete successfully";
                    } else {
                        reply = "Canceled";
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

    private void rename(String sourceName, String destName, Socket memberSocket) throws IOException {
        if (new File(sharedDir + sourceName).exists()) {
            if (!new File(sharedDir + destName).exists()) {
                new File(sharedDir + sourceName).renameTo(new File(sharedDir + destName));
                reply("Renamed successfully", memberSocket);
            } else {
                reply("The file exists. please input a new name: ", memberSocket);
            }
        } else {
            reply("The file doesn't exist", memberSocket);
        }
    }

    //calculate the size of the file and convert it to the version which we can directly read
    private String convertSize(double value) {
        String size = "";
        if (value < 1024) {
            size = value + "B";
            return size;
        } else {
            value = new BigDecimal(value / 1024).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
        }
        if (value < 1024) {
            size = value + "KB";
            return size;
        } else {
            value = new BigDecimal(value / 1024).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
        }
        if (value < 1024) {
            size = value + "MB";
            return size;
        } else {
            value = new BigDecimal(value / 1024).setScale(2, BigDecimal.ROUND_DOWN).doubleValue();
            size = value + "GB";
        }
        return size;
    }

    private void detail(String fileName, Socket memberSocket) throws IOException {
        File file = new File(sharedDir + fileName);
        String size = "";
        long length = 0;
        String dateFormat = "yyyy-MM-dd HH:mm:ss";
        String lastmodifiedtime = "";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        String currenttime = "";
        String type = "";
        String NumberOfDir = "";
        String NumberOfFile = "";
        String createdtime = "";
        String root = "";
        String name = "";
        String reply = "";

        if (file.exists()) {
            //get name
            name = file.getName();

            //get the position
            root = fileName;

            //get last modified time
            lastmodifiedtime = sdf.format(file.lastModified());

            //get the create time
            FileTime t = Files.readAttributes(Paths.get(sharedDir + fileName), BasicFileAttributes.class).creationTime();
            createdtime = sdf.format(t.toMillis());

            //get current time
            Date date = new Date();
            currenttime = sdf.format(date);

            //get type
            if (file.isFile()) {
                type = "File";
            } else {
                type = "Directory";

                //get the numbers of the files and directory
                File[] files = file.listFiles();
                int numberofdir = 0;
                int numberoffile = 0;

                for (File f : files) {
                    if (f.isDirectory()) {
                        if (f.listFiles() != null && f.listFiles().length >= 0) {
                            numberofdir++;
                        }
                    } else {
                        numberoffile++;
                    }
                }
                NumberOfDir = String.valueOf(numberofdir);
                NumberOfFile = String.valueOf(numberoffile);
            }

            //send the data to the client
            if (file.isFile()) {
                //get size which we can directly read
                length = file.length();
                size = convertSize((double) length);

                reply("file", memberSocket);
                reply = "type: " + type + "\nname: " + name + "\nposition: " + root + "\nsize: " + size + "(" + length + " byte(s))" + "\ncreate time: " + createdtime + "\nlast modified time: " + lastmodifiedtime
                        + "\ninterview time: " + currenttime;
            } else if (file.isDirectory()) {
                length = getTotalSizeOfFilesInDir(file);
                size = convertSize((double) length);

                reply("dir", memberSocket);
                reply = "type: " + type + "\nname: " + name + "\nposition: " + root + "\nsize: " + size + "(" + length + "byte(s))" + "\ncreate time: " + createdtime + "\nlast modified time: " + lastmodifiedtime +
                        "\ncontent: " + NumberOfFile + " file(s) and " + NumberOfDir + " folder(s)." + "\ninterview time: " + currenttime;
            }
        } else {
            reply = "The file doesn't exist";
        }

        reply(reply, memberSocket);
    }

    private long getTotalSizeOfFilesInDir(final File file) {
        if (file.isFile())
            return file.length();
        final File[] children = file.listFiles();
        long total = 0;
        if (children != null)
            for (final File child : children)
                total += getTotalSizeOfFilesInDir(child);
        return total;
    }
}