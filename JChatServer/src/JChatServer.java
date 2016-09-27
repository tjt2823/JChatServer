import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class JChatServer
{
	ArrayList<Socket> al = new ArrayList<Socket>();
	ArrayList<String> users = new ArrayList<String>();
	ServerSocket ss;
	Socket s;

	public final static int PORT = 10;
	public final static String UPDATE_USERS = "updateuserslist:";
	public final static String LOGOUT_MESSAGE = "@@logoutme@@:";

	public JChatServer() {
		try {
			ss = new ServerSocket(PORT);
			System.out.println("Server Started " + ss);
			while (true) {
				s = ss.accept();
				Runnable r = new MyThread(s, al, users);
				Thread t = new Thread(r);
				t.start();
			}
		} catch (Exception e) {
			System.err.println("Server constructor" + e);
		}
	}

	public static void main(String[] args) {
		new JChatServer();
	}
}