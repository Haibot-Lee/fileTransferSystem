import sun.awt.windows.WBufferStrategy;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class client {
    String serverIp;
    Socket socket;
    DataOutputStream out;

    public client(String serverIp) throws IOException {
        this.serverIp = serverIp;
        socket = new Socket(serverIp, 9999);
    }

    public void login(String member, String password) throws IOException {
        out = new DataOutputStream(socket.getOutputStream());

        String loginInfo = member + " " + password;
        out.writeInt(loginInfo.length());
        out.write(loginInfo.getBytes(), 0, loginInfo.length());
    }

    public void sendCmd(String option) throws IOException {
        out.writeInt(option.length());
        out.write(option.getBytes(), 0, option.length());
    }

    public String getReply() throws IOException {
        String reply = "";
        DataInputStream in = new DataInputStream(socket.getInputStream());
        try {
            int len = in.readInt();
            byte[] buffer = new byte[len];
            in.read(buffer, 0, len);
            reply = new String(buffer, 0, len);

        } catch (IOException ex) {
            System.err.println("Connection dropped!");
            System.exit(-1);
        }

        return reply;
    }

    public void upload(String filePath) throws IOException {
        File file = new File(filePath);

        String fileName = file.getName();
        long fileSize = file.length();
        String fileInfo = String.format("%s %d", fileName, fileSize);
        out.writeInt(fileInfo.length());
        out.write(fileInfo.getBytes(), 0, fileInfo.length());

        FileInputStream in = new FileInputStream(file);
        while (fileSize > 0) {
            byte[] buffer = new byte[1024];
            int len = in.read(buffer);
            fileSize -= len;
            out.writeInt(len);
            out.write(buffer, 0, len);
        }
        System.out.println("upload successfully");
    }

    //test area
    public static void main(String[] args) {
        try {
//            client c = new client("158.182.8.141");
            client c = new client("127.0.0.1");
            c.login("amy", "123");

            String reply = c.getReply();
            if (reply.equals("accept")) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Please input options:");
                while (true) {
                    String str = scanner.nextLine();
                    c.sendCmd(str);
                    if (str.equals("upload")) {
                        c.upload("C:\\Users\\mrli\\Desktop\\test.txt");
                    }
                }
            } else {
                System.out.println(reply);
            }

        } catch (IOException e) {
            System.err.println("Unable to connect server");
            System.exit(-1);
        }

    }

}