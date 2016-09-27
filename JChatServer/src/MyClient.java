import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * The client class and user facing GUI
 * 
 * @author Tom Thomas
 */
class MyClient implements ActionListener
{
	Socket s;
	DataInputStream dIS;	//Input stream for messages
	DataOutputStream dOS;	//Output stream for messages

	JButton sendButton, logoutButton, loginButton, exitButton;
	JFrame chatWindow;
	JTextArea txtBroadcast;
	JTextArea txtMessage;
	JList<String> userList;

	public void displayGUI() {
		chatWindow = new JFrame();
		txtBroadcast = new JTextArea(5, 30);
		txtBroadcast.setEditable(false);
		txtMessage = new JTextArea(2, 20);
		userList = new JList<String>();

		sendButton = new JButton("Send");
		logoutButton = new JButton("Log out");
		loginButton = new JButton("Log in");
		exitButton = new JButton("Exit");

		JPanel center1 = new JPanel();
		center1.setLayout(new BorderLayout());
		center1.add(new JLabel("Broad Cast messages from all online users", JLabel.CENTER), "North");
		center1.add(new JScrollPane(txtBroadcast), "Center");

		JPanel south1 = new JPanel();
		south1.setLayout(new FlowLayout());
		south1.add(new JScrollPane(txtMessage));
		south1.add(sendButton);

		JPanel south2 = new JPanel();
		south2.setLayout(new FlowLayout());
		south2.add(loginButton);
		south2.add(logoutButton);
		south2.add(exitButton);

		JPanel south = new JPanel();
		south.setLayout(new GridLayout(2, 1));
		south.add(south1);
		south.add(south2);

		JPanel east = new JPanel();
		east.setLayout(new BorderLayout());
		east.add(new JLabel("Online Users", JLabel.CENTER), "East");
		east.add(new JScrollPane(userList), "South");

		chatWindow.add(east, "East");

		chatWindow.add(center1, "Center");
		chatWindow.add(south, "South");

		chatWindow.pack();
		chatWindow.setTitle("Login for Chat");
		chatWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		chatWindow.setVisible(true);
		
		sendButton.addActionListener(this);
		logoutButton.addActionListener(this);
		loginButton.addActionListener(this);
		exitButton.addActionListener(this);
		logoutButton.setEnabled(false);
		loginButton.setEnabled(true);
		
		//Highlights text when message box gains focus
		txtMessage.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent fe)
			{
				txtMessage.selectAll();
			}
		});

		//Client window closing event. A message is displayed before exiting.
		chatWindow.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev)
			{
				if (s != null) {
					JOptionPane.showMessageDialog(chatWindow, "You are now logged out!", "Exit",
							JOptionPane.INFORMATION_MESSAGE);
					logoutSession();
				}
				System.exit(0);
			}
		});
	}

	public void actionPerformed(ActionEvent ev)
	{
		JButton temp = (JButton) ev.getSource();
		
		if (temp == sendButton)
		{
			if (s == null)
			{
				JOptionPane.showMessageDialog(chatWindow, "Please login first!");
				return;
			}
			
			try
			{
				dOS.writeUTF(txtMessage.getText());
				txtMessage.setText("");
				System.out.println("Booooo");
			} catch (Exception excp)
			{
				txtBroadcast.append("\nsend button click :" + excp);
			}
		}
		
		if (temp == loginButton)
		{
			String uName = JOptionPane.showInputDialog(chatWindow, "Enter your preferred nick name: ");
			
			if (uName != null)
				clientChat(uName);
		}
		
		if (temp == logoutButton)
		{
			if (s != null)
				logoutSession();
		}
		
		if (temp == exitButton)
		{
			if (s != null)
			{
				logoutSession();
				JOptionPane.showMessageDialog(chatWindow, "You are now logged out!", "Exit",
						JOptionPane.INFORMATION_MESSAGE);
			}
			
			System.exit(0);
		}
	}

	/**
	 * Logout routine
	 */
	public void logoutSession()
	{
		if (s == null)
			return;
		
		try
		{
			dOS.writeUTF(JChatServer.LOGOUT_MESSAGE);
			Thread.sleep(500);
			s = null;
		} catch (Exception e)
		{
			txtBroadcast.append("\n inside logoutSession Method" + e);
		}

		logoutButton.setEnabled(false);
		loginButton.setEnabled(true);
		chatWindow.setTitle("Login for Chat");
	}

	/**
	 * When a new user joins the chat
	 * 
	 * @param uName The nick name
	 */
	public void clientChat(String uName)
	{
		try
		{
			s = new Socket(InetAddress.getLocalHost(), JChatServer.PORT);
			dIS = new DataInputStream(s.getInputStream());
			dOS = new DataOutputStream(s.getOutputStream());
			
			ClientThread ct = new ClientThread(dIS, this);
			Thread t1 = new Thread(ct);
			t1.start();
			
			dOS.writeUTF(uName);
			chatWindow.setTitle(uName + " Chat Window");
		} catch (Exception e)
		{
			txtBroadcast.append("\nClient Constructor " + e);
		}
		
		logoutButton.setEnabled(true);
		loginButton.setEnabled(false);
	}

	/**
	 * The constructor
	 */
	public MyClient() {
		displayGUI();
	}

	/**
	 * Initializes the client
	 * @param args
	 */
	public static void main(String[] args)
	{
		new MyClient();
	}
}

/**
 * The client thread continuously reads the Input stream from the
 * socket and updates the UI accordingly.
 */
class ClientThread implements Runnable
{
	DataInputStream dis;
	MyClient client;

	ClientThread(DataInputStream dis, MyClient client)
	{
		this.dis = dis;
		this.client = client;
	}

	//Looks for messages from cocket on a loop
	public void run()
	{
		String s2 = "";
		
		do {
			try {
				s2 = dis.readUTF();
				
				if (s2.startsWith(JChatServer.UPDATE_USERS))
					updateUserList(s2);
				
				else if (s2.equals(JChatServer.LOGOUT_MESSAGE))
					break;
				
				else
					client.txtBroadcast.append("\n" + s2);
				
				int lineOffset = client.txtBroadcast.getLineStartOffset(client.txtBroadcast.getLineCount() - 1);
				client.txtBroadcast.setCaretPosition(lineOffset);
			} catch (Exception e)
			{
				client.txtBroadcast.append("\nClientThread run : " + e);
			}
		} while (true);
	}

	/**
	 * Updates the user list if new users join the chat room
	 * 
	 * @param ul update message
	 */
	public void updateUserList(String ul)
	{
		Vector<String> ulist = new Vector<String>();

		ul = ul.replace("[", "");
		ul = ul.replace("]", "");
		ul = ul.replace(JChatServer.UPDATE_USERS, "");
		StringTokenizer st = new StringTokenizer(ul, ",");

		while (st.hasMoreTokens())
		{
			String temp = st.nextToken();
			ulist.add(temp);
		}
		
		client.userList.setListData(ulist);
	}
}