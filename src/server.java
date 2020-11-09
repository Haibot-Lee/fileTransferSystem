import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class server {
    File sharedDir;

    //    ServerSocket udpSocket = new ServerSocket(9998);
    ServerSocket tcpSocket = new ServerSocket(9999);
    ArrayList<Socket> list = new ArrayList<Socket>();

    public server(String dirPath, String listPath) throws IOException {
//        System.out.println("Listening at UDP port 9998...");
        System.out.println("Listening at TCP port 9999...");

        sharedDir = new File(dirPath);

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

        reply(reply.getBytes(), reply.getBytes().length, memberSocket);
        return ifLogin;

    }

    private void reply(byte[] data, int len, Socket destSocket) {
        synchronized (list) {
            try {
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
            //System.out.println("Option: " + option);

            //TODO: realize the option from client
            switch (options[0]) {
                case "read":
                    read("test");
                    break;
                case "create":
                    create(options[1]);
                    break;
                case "upload":
                    upload();
                    break;
                case "download":
                    download();
                    break;
                case "deleteFile":
                    delete(options[1]);
                    break;
                case "rename":
                    rename(options[1],options[2]);
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

    //opyion on shared root directory
    private void read(String pathname) {
        File path = new File(pathname);
        File[] files = path.listFiles();
        ArrayList<String> info = new ArrayList<>();

        for(File f : files){
            if(f.isDirectory()) {
                info.add(String.format("%s %10s %s\n",new Date(f.lastModified()), "<DIR>",f.getName()));
            }else {
                info.add(String.format("%s %9dB %s\n",new Date(f.lastModified()),f.length(),f.getName()));
            }
        }

        for(String n : info) {
            System.out.print(n);
        }
    }

    private void create(String name) {
        File file = new File("test\\" + name);
        if (file.exists()) {
            System.out.printf("%s exists!\n", file.isDirectory() ? "Directory" : "Filr");
        } else {
            file.mkdirs();
            System.out.println("Created.");
        }
    }

    private void upload() {

    }

    private void download() {

    }

    private void delete(String name) {
        File file = new File("test\\"+name);
        if(file.exists()) {
            if(!file.isDirectory()) {
                file.delete();
                System.out.println("Delete successfully.");
            }else {
                File[] files = file.listFiles();
                if(files.length==0) {
                    file.delete();
                    System.out.println("Delete successfully.");
                }else {
                    Scanner in = new Scanner(System.in);
                    System.out.println("The directory "+name+" is not empty! Do you still want to delete it?");
                    String op = in.nextLine();
                    if(op.equals("yes")){
                        deleteAll(file);
                        System.out.println("Delete successfully.");
                    }
                }
            }
        }else {
            System.out.println("The file does not exist.");
        }
    }

    public void deleteAll(File file){
        if(file.isFile()){
            file.delete();
        }else{
            for(File files : file.listFiles()){
                deleteAll(files);
            }
        }
        file.delete();
    }

    private void rename(String sourcename, String destname) {
        if(new File("test\\"+sourcename).exists()) {
            new File("test\\" + sourcename).renameTo(new File("test\\" + destname));
        }else{
            System.out.println("yje file doesn't exist");
        }
    }

    private void detail() {

    }

    //start server
    public static void main(String[] args) throws IOException {
        new server(args[0], args[1]);
    }
}
