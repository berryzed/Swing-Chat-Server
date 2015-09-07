package kr.berryz.chatserver;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.EmptyBorder;

/**
 * @author Berryzed
 *
 */
public class ChatServer extends JFrame
{
	private static final long serialVersionUID = 1L;

	private static final int SERVER_PORT = 9999;

	Vector<String> users = new Vector<String>();
	Vector<HandleClient> clients = new Vector<HandleClient>();

	private ServerSocket serverSocket;

	private JPanel contentPanel;

	private JTextArea historyTextArea;
	private JScrollPane textScrollPane;
	private JScrollBar textScrollBar;

	/**
	 * Create the frame.
	 */
	public ChatServer()
	{
		setUI();
	}

	public void process() throws Exception
	{
		serverSocket = new ServerSocket(SERVER_PORT);
		appendText("[Server] Server Online...");
		while (true)
		{
			Socket client = serverSocket.accept();
			HandleClient c = new HandleClient(client);
			clients.add(c);
		}
	}

	private void setUI()
	{
		setTitle("ChatServer");
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);

		contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPanel.setLayout(new BorderLayout(0, 0));

		historyTextArea = new JTextArea();
		historyTextArea.setEditable(false);

		textScrollPane = new JScrollPane(historyTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		textScrollBar = textScrollPane.getVerticalScrollBar();

		contentPanel.add("Center", historyTextArea);

		setContentPane(contentPanel);
		setVisible(true);
	}

	public void boradcast(String user, String message)
	{
		for (HandleClient c : clients)
			if (!c.getUserName().equals(user))
				c.sendMessage(user, message);
	}

	public void appendText(String msg)
	{
		historyTextArea.append(msg + "\n");
		textScrollBar.setValue(textScrollBar.getMaximum());
	}

	public static void main(String[] args) throws Exception
	{
		// TODO Auto-generated method stub
		
		try
		{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
			{
				if ("Nimbus".equals(info.getName()))
				{
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		}
		catch (Exception ex)
		{
			System.out.println(ex.getMessage());
		}
		
		new ChatServer().process();
	}

	class HandleClient extends Thread
	{
		String name = "";
		BufferedReader input;
		PrintWriter output;

		public HandleClient(Socket client) throws Exception
		{
			input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			output = new PrintWriter(client.getOutputStream(), true);

			name = input.readLine();
			users.add(name);
			appendText("[Server] " + name + " - Connected!");
			appendText("[Server] Total User: " + users.size());
			start();
			
			boradcast(name, "[Connect]");
		}

		public void sendMessage(String uname, String msg)
		{
			output.println(uname + ": " + msg);
		}

		public String getUserName()
		{
			return name;
		}

		public void run()
		{
			String line;
			try
			{
				while (true)
				{
					line = input.readLine();
					if (line.equals("end"))
					{
						clients.remove(this);
						users.remove(name);
						appendText("[Server] " + name + " - Disconnected!");
						appendText("[Server] Total User: " + users.size());
						break;
					}
					appendText("[Message] " + name + ": " + line);
					boradcast(name, line);
				}
			}
			catch (Exception ex)
			{
				System.out.println(ex.getMessage());
			}
		}
	}

}
