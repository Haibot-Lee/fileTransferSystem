import java.io.File;

public class server {
    File sharedDir;
    File memberList;

    public server(String dirPath, String listPath) {
        sharedDir = new File(dirPath);
        memberList = new File(listPath);
    }

    //test area
    public static void main(String[] args) {

    }
}
