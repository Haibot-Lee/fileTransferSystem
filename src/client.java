import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class client {
    Socket socket;
    DataOutputStream out;

    public client() {

    }

    public void login(String serverIp, String member, String password) throws IOException {
        socket = new Socket(serverIp, 9999);
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
            client c = new client();
            c.login("127.0.0.1", "amy", "123");

            Scanner scanner = new Scanner(System.in);
            System.out.println("Please input messages:");
            while (true) {
                String str = scanner.nextLine();
                c.sendCmd(str);
            }

        } catch (IOException e) {
            System.err.println("Unable to connect server");
            System.exit(-1);
        }

    }

}