import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class client {
    ArrayList<String> servers = new ArrayList<String>();
    Socket tcpSocket;

    public client() throws IOException {

    }

    public void broadcasts(int times) throws IOException {
        DatagramSocket udpSocket = new DatagramSocket(3333);
        byte[] msg = "Finding server...".getBytes();
        InetAddress dest = InetAddress.getByName("255.255.255.255");
        DatagramPacket packet = new DatagramPacket(msg, msg.length, dest, 9998);
        for (int i = 0; i < times; i++) {
            udpSocket.send(packet);
        }
        udpSocket.close();
    }

    public void receiveIP() throws IOException {
        DatagramSocket udpSocket = new DatagramSocket(3333);

        Thread udp = new Thread(() -> {
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                    udpSocket.receive(packet);
                    byte[] data = packet.getData();
                    String str = new String(data, 0, packet.getLength());

                    if (str.equals("Here is a server")) {
                        String ip = packet.getAddress().toString();
                        ip = ip.substring(ip.lastIndexOf("/") + 1);
                        System.out.println(ip);
                        if (!servers.contains(ip)) {
                            servers.add(ip);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Failed to receive from server!");
                }
            }
        });
        udp.start();

    }

    public void login(String serverIp, String member, String password) throws IOException {
        tcpSocket = new Socket(serverIp, 9999);
        DataOutputStream out = new DataOutputStream(tcpSocket.getOutputStream());

        String loginInfo = member + " " + password;
        out.writeInt(loginInfo.length());
        out.write(loginInfo.getBytes(), 0, loginInfo.length());
    }

    public void sendCmd(String option) throws IOException {
        DataOutputStream out = new DataOutputStream(tcpSocket.getOutputStream());
        out.writeInt(option.length());
        out.write(option.getBytes(), 0, option.length());
    }

    public String getReply() throws IOException {
        String reply = "";
        DataInputStream in = new DataInputStream(tcpSocket.getInputStream());
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
        DataOutputStream out = new DataOutputStream(tcpSocket.getOutputStream());
        File file = new File(filePath);
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

    public void download() throws IOException {
        DataInputStream in = new DataInputStream(tcpSocket.getInputStream());
        int len = in.readInt();
        byte[] buffer = new byte[len];
        in.read(buffer, 0, len);
        String reply = new String(buffer);
        if (reply.equals("File does not exist") || reply.equals("Can not download directory")) {
            System.out.println(reply);
        } else {
            String[] fileInfo = reply.split(" ");
            File file = new File("C:\\Users\\mrli\\Desktop\\" + fileInfo[0]);

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
    }

    //test area
    public static void main(String[] args) {
        try {

            client c = new client();
            c.broadcasts(5);
            c.receiveIP();
            c.login(c.servers.get(0), "amy", "123");

            String reply = c.getReply();
            if (reply.equals("accept")) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Please input options:");
                while (true) {
                    String str = scanner.nextLine();
                    c.sendCmd(str);
                    if (str.equals("upload")) {
                        c.upload("C:\\Users\\mrli\\Desktop\\test.log");
                    }
                    if (str.equals("download")) {
                        c.download();
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