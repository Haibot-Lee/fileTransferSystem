//Used to store the members in member list

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class MemberDB {
    private ArrayList<Member> members = new ArrayList<Member>();

    //inner class of Member
    public class Member {
        private String name;
        private String password;

        public Member(String name, String password) {
            this.name = name;
            this.password = password;
        }

        public String getName() {
            return name;
        }

        public String getPassword() {
            return password;
        }
    }

    public MemberDB(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        Scanner in = new Scanner(file);
        while (in.hasNextLine()) {
            String[] memberInfo = in.nextLine().split(" ");
            Member member = new Member(memberInfo[0], memberInfo[1]);
            members.add(member);
        }

    }

    public Member getMember(int idx) {
        if (idx >= getSize()) {
            return null;
        }
        return members.get(idx);
    }

    public int getSize() {
        return members.size();
    }

}
