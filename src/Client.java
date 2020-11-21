import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
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
                System.out.println("socket closed");
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

    public void upload(File file) throws IOException {
        long fileSize = file.length();
        DataOutputStream out = new DataOutputStream(tcpSocket.getOutputStream());
        FileInputStream inFile = new FileInputStream(file);

        while (fileSize >= 1024) {
            byte[] buffer = new byte[1024];
            int len = inFile.read(buffer);
            fileSize -= len;
            out.write(buffer, 0, buffer.length);
        }
        byte[] buffer = new byte[(int) fileSize];
        inFile.read(buffer);
        out.write(buffer, 0, buffer.length);

        inFile.close();
    }

    public void download(String filePathInServer, String downloadPath) throws IOException {
        sendMsg("download>" + filePathInServer);
        String reply = getReply();
        String[] fileInfo = reply.split(">");
        File file = new File(downloadPath + "\\" + fileInfo[0]);

        DataInputStream in = new DataInputStream(tcpSocket.getInputStream());
        FileOutputStream outFile = new FileOutputStream(file);
        int size = Integer.parseInt(fileInfo[1]);
        int transCnt = 0;
        if (size > 0) {
            transCnt = size / 1024;
        }

        for (int i = 0; i < transCnt; i++) {
            byte[] content = new byte[1024];
            in.read(content, 0, content.length);
            outFile.write(content, 0, content.length);
            size -= 1024;
        }

        byte[] content = new byte[size];
        in.read(content, 0, content.length);
        outFile.write(content, 0, content.length);
        outFile.close();
    }
}