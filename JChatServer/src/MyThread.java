import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

/**
 * Thread class that is run by the server
 * 
 * @author Tom Thomas
 */
class MyThread implements Runnable
{
	Socket s;
	ArrayList<Socket> clients;
	ArrayList<String> users;
	String username;

	/**
	 * The thread is initialized when a client connects to the server which happens
	 * when users log in. The thread then sends this message to all clients listening.
	 * 
	 * @param s
	 * @param clients
	 * @param users
	 */
	MyThread(Socket s, ArrayList<Socket> clients, ArrayList<String> users)
	{
		this.s = s;
		this.clients = clients;
		this.users = users;
		
		try
		{
			DataInputStream dIS = new DataInputStream(s.getInputStream());
			username = dIS.readUTF();
			clients.add(s);
			users.add(username);
			tellEveryOne("****** " + username + " Logged in at " + (new Date()) + " ******");
			sendNewUserList();
		} catch (Exception e)
		{
			System.err.println("MyThread constructor  " + e);
		}
	}

	/**
	 * Runs on a loop listening for input messages and sends messages to
	 * clients listening.
	 */
	public void run()
	{
		String s1;
		try
		{
			DataInputStream dIS = new DataInputStream(s.getInputStream());
			do
			{
				s1 = dIS.readUTF();
				
				if (s1.toLowerCase().equals(JChatServer.LOGOUT_MESSAGE))
					break;
				
				tellEveryOne(username + " said: " + " : " + s1);
			} while (true);
			
			DataOutputStream dOS = new DataOutputStream(s.getOutputStream());
			dOS.writeUTF(JChatServer.LOGOUT_MESSAGE);
			dOS.flush();
			
			users.remove(username);
			tellEveryOne("****** " + username + " Logged out at " + (new Date()) + " ******");
			sendNewUserList();
			clients.remove(s);
			s.close();

		} catch (Exception e)
		{
			System.out.println("MyThread Run" + e);
		}
	}

	/**
	 * Called when a user logs out.
	 */
	public void sendNewUserList()
	{
		tellEveryOne(JChatServer.UPDATE_USERS + users.toString());
	}

	/**
	 * Sends messages to all clients
	 * 
	 * @param s1 The message to send
	 */
	public void tellEveryOne(String s1)
	{
		Iterator<Socket> i = clients.iterator();
		while (i.hasNext())
		{
			try
			{
				Socket temp = (Socket) i.next();
				DataOutputStream dOS = new DataOutputStream(temp.getOutputStream());
				dOS.writeUTF(s1);
				dOS.flush();
			} catch (Exception e)
			{
				System.err.println("TellEveryOne " + e);
			}
		}
	}
}