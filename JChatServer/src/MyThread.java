import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

class MyThread implements Runnable {
	Socket s;
	ArrayList<Socket> al;
	ArrayList<String> users;
	String username;

	MyThread(Socket s, ArrayList<Socket> al, ArrayList<String> users) {
		this.s = s;
		this.al = al;
		this.users = users;
		try {
			DataInputStream dis = new DataInputStream(s.getInputStream());
			username = dis.readUTF();
			al.add(s);
			users.add(username);
			tellEveryOne("****** " + username + " Logged in at " + (new Date()) + " ******");
			sendNewUserList();
		} catch (Exception e) {
			System.err.println("MyThread constructor  " + e);
		}
	}

	public void run() {
		String s1;
		try {
			DataInputStream dis = new DataInputStream(s.getInputStream());
			do {
				s1 = dis.readUTF();
				if (s1.toLowerCase().equals(JChatServer.LOGOUT_MESSAGE))
					break;
				tellEveryOne(username + " said: " + " : " + s1);
			} while (true);
			DataOutputStream tdos = new DataOutputStream(s.getOutputStream());
			tdos.writeUTF(JChatServer.LOGOUT_MESSAGE);
			tdos.flush();
			users.remove(username);
			tellEveryOne("****** " + username + " Logged out at " + (new Date()) + " ******");
			sendNewUserList();
			al.remove(s);
			s.close();

		} catch (Exception e) {
			System.out.println("MyThread Run" + e);
		}
	}

	public void sendNewUserList() {
		tellEveryOne(JChatServer.UPDATE_USERS + users.toString());

	}

	public void tellEveryOne(String s1) {
		Iterator<Socket> i = al.iterator();
		while (i.hasNext()) {
			try {
				Socket temp = (Socket) i.next();
				DataOutputStream dos = new DataOutputStream(temp.getOutputStream());
				dos.writeUTF(s1);
				dos.flush();
			} catch (Exception e) {
				System.err.println("TellEveryOne " + e);
			}
		}
	}
}