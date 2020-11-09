import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class client {
    Socket socket;
    DataOutputStream out;
    String serverReply = "";

    public client(String serverIp) throws IOException {
        socket = new Socket(serverIp, 9999);
    }

    public String getReply() throws IOException {
        DataInputStream in = new DataInputStream(socket.getInputStream());

        Thread t = new Thread(() -> {
            try {
//                while (true) {
                    System.out.println("Waiting...");
                    int len = in.readInt();
                    byte[] buffer = new byte[len];
                    in.read(buffer, 0, len);
                    serverReply = new String(buffer, 0, len);
                    System.out.println(">" + serverReply);
//                }
            } catch (IOException ex) {
                System.err.println("Connection dropped!");
                System.exit(-1);
            }
        });
        t.start();

        return "";
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

    //test area
    public static void main(String[] args) {
        try {
            client c = new client("127.0.0.1");
            c.login("amy", "123");
            c.getReply();

            boolean ifLogin = false;

            int i = 0;
            System.out.println("into while");
            while (true) {
                i++;
//                System.out.println(i + ": " + c.serverReply);
                System.out.println();
                if (!c.serverReply.equals("")) {
                    if (c.serverReply.equals("accept")) {
                        ifLogin = true;
                        break;
                    } else {
                        break;
                    }
                }
            }
            System.out.println("out while");

            if (ifLogin) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Please input options:");
                while (true) {
                    String str = scanner.nextLine();
                    c.sendCmd(str);
                }
            }

        } catch (IOException e) {
            System.err.println("Unable to connect server");
            System.exit(-1);
        }

    }

}