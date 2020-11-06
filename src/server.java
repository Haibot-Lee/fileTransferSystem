import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class server {
    File sharedDir;
    File memberList;

//    ServerSocket udpSocket = new ServerSocket(9998);
    ServerSocket tcpSocket = new ServerSocket(9999);
    ArrayList<Socket> list = new ArrayList<Socket>();

    public server(String dirPath, String listPath) throws IOException {
//        System.out.println("Listening at UDP port 9998...");
        System.out.println("Listening at TCP port 9999...");

        sharedDir = new File(dirPath);
        memberList = new File(listPath);

        while (true) {
            Socket memberSocket = tcpSocket.accept();

            synchronized (list) {
                list.add(memberSocket);
            }

            Thread t = new Thread(() -> {
                try {
                    if (loginCheck(memberSocket))
                        serve(memberSocket);
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

    private boolean loginCheck(Socket memberSocket) throws IOException {
        DataInputStream in = new DataInputStream(memberSocket.getInputStream());
        int len = in.readInt();
        byte[] buffer = new byte[len];
        in.read(buffer, 0, len);
        String dis = new String(buffer);

        boolean ifLogin = false;
        String reply = "reject";
        if (dis.equals("haibot: 123")) {
            ifLogin = true;
            reply = "accept";
            System.out.printf("Total %d clients are connected.\n", list.size());
            System.out.printf("Established a connection to host %s:%d\n\n", memberSocket.getInetAddress(), memberSocket.getPort());
        }

        forward(reply.getBytes(), reply.getBytes().length, memberSocket);
        return ifLogin;

    }

    private void serve(Socket memberSocket) throws IOException {
        DataInputStream in = new DataInputStream(memberSocket.getInputStream());
        while (true) {
            byte[] buffer = new byte[1024];
            int len = in.readInt();
            in.read(buffer, 0, len);
            String option = new String(buffer);
            System.out.println(option);
        }

    }

    private void forward(byte[] data, int len, Socket destSocket) {
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

    //test area
    public static void main(String[] args) throws IOException {
        server se = new server("", "");
    }
}
