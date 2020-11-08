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
        DataInputStream in = new DataInputStream(socket.getInputStream());

        Thread t = new Thread(() -> {
            byte[] buffer = new byte[1024];
            try {
                while (true) {
                    int len = in.readInt();
                    in.read(buffer, 0, len);
                    serverReply = new String(buffer, 0, len);
                }
            } catch (IOException ex) {
                System.err.println("Connection dropped!");
                System.exit(-1);
            }
        });
        t.start();
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

            boolean ifLogin = false;
            while (true) {
                if (!c.serverReply.equals("")) {
                    if (c.serverReply.equals("accept")) {
                        ifLogin = true;
                        break;
                    } else {
                        break;
                    }
                }
            }

            System.out.println(c.serverReply);
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