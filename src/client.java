import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class client {
    Socket socket;
    DataOutputStream out;

    public client(String serverIp) throws IOException {
        socket = new Socket(serverIp, 9999);
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

            String reply = c.getReply();
            if (reply.equals("accept")) {
                Scanner scanner = new Scanner(System.in);
                System.out.println("Please input options:");
                while (true) {
                    String str = scanner.nextLine();
                    c.sendCmd(str);
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