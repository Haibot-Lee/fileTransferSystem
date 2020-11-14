import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client {
    Socket tcpSocket;

    //UDP
    public void broadcasts(int times) throws IOException {
        DatagramSocket udpSocket = new DatagramSocket(12345);
        byte[] msg = "Finding server...".getBytes();
        InetAddress dest = InetAddress.getByName("255.255.255.255");
        DatagramPacket packet = new DatagramPacket(msg, msg.length, dest, 9998);
        for (int i = 0; i < times; i++) {
            udpSocket.send(packet);
        }
        udpSocket.close();
    }

    public String[] getServer() throws IOException {
        DatagramSocket udpSocket = new DatagramSocket(12345);
        ArrayList<String> serversIP = new ArrayList<String>();
        ArrayList<String> serversName = new ArrayList<String>();

        Thread timer = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
                udpSocket.close();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        });
        timer.start();

        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
                udpSocket.receive(packet);
                byte[] data = packet.getData();
                String msg = new String(data, 0, packet.getLength());
                String[] msgs = msg.split(" ");

                if (msgs[0].equals("availableServer")) {

                    if (!serversIP.contains(packet.getAddress().toString())) {
                        serversIP.add(packet.getAddress().toString());
                        serversName.add(msgs[1]);
                        System.out.println("One server added");
                    }
                }
            } catch (IOException e) {
                System.err.println("socket closed");
                break;
            }
        }

        String[] list = new String[serversIP.size()];
        for (int i = 0; i < list.length; i++) {
            list[i] = serversName.get(i) + " (IP address: " + serversIP.get(i) + ")";
        }

        return list;
    }

    //TCP
    public void login(String serverIp, String member, String password) throws IOException {
        tcpSocket = new Socket(serverIp, 9999);
        DataOutputStream out = new DataOutputStream(tcpSocket.getOutputStream());

        String loginInfo = member + " " + password;
        out.writeInt(loginInfo.length());
        out.write(loginInfo.getBytes(), 0, loginInfo.length());
    }

    public void sendMsg(String option) throws IOException {
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

    public String download() throws IOException {
        String reply = getReply();
        if (reply.equals("Can not download directory")) {
            return reply;
        }

        String[] fileInfo = reply.split(">");
        System.out.println(fileInfo[0] + " " + fileInfo[1]);
        File file = new File("C:\\Users\\e8252125\\Desktop\\" + fileInfo[0]);

        DataInputStream in = new DataInputStream(tcpSocket.getInputStream());
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
        return "One file received";
    }

    public void rename() throws IOException {
        Scanner in = new Scanner(System.in);
        System.out.print("The file exists. If you want to cancel, please input yes, otherwise please input a new name: ");
        sendMsg(in.nextLine());
        String r = getReply();
        if (r.equals("The file exists. please input a new name: ")) {
            rename();
        } else {
            System.out.println(r);
        }
    }
}