import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
                    detail();
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

    private void rename(String sourcename, String destname) {
        if (new File(sharedDir + sourcename).exists()) {
            if(!new File(sharedDir + destname).exists()) {
                new File(sharedDir + sourcename).renameTo(new File(sharedDir + destname));
            }else{
                System.out.println("The file exists.");
            }
        } else {
            System.out.println("The file doesn't exist");
        }
    }

    private void detail() {

    }

    //start server
    public static void main(String[] args) throws IOException {
        new server(args[0], args[1]);
    }
}
