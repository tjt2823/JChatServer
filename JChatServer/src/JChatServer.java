import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * The main server class. This is initialized first before
 * initializing the clients.
 * 
 * @author Tom Thomas *
 */
class JChatServer
{
	ArrayList<Socket> clients = new ArrayList<Socket>();	//A list of clients
	ArrayList<String> users = new ArrayList<String>();	//A list of user names
	ServerSocket ss;
	Socket s;

	public final static int PORT = 10;
	public final static String UPDATE_USERS = "updateuserlist:";	//Message to update users
	public final static String LOGOUT_MESSAGE = "@@logmeout@@:";	//Message to log the user out

	public JChatServer()
	{
		try {
			ss = new ServerSocket(PORT);
			System.out.println("Server Started " + ss);
			
			//Listens for new connections and starts a new thread for each client
			while (true)
			{
				s = ss.accept();
				Runnable r = new MyThread(s, clients, users);
				Thread t = new Thread(r);
				t.start();
			}
		} catch (Exception e) {
			System.err.println("Server constructor" + e);
		}
	}

	/**
	 * Initializes the server
	 * @param args
	 */
	public static void main(String[] args)
	{
		new JChatServer();
	}
}