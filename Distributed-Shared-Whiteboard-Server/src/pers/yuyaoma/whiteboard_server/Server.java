package pers.yuyaoma.whiteboard_server;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JOptionPane;

/**
 * @author: Yuyao Ma
 * @className: Server
 * @packageName: pers.yuyaoma.whiteboard_client
 * @description: The Server class of Whiteboard app
 * @data: 2021-06-03
 **/

public class Server extends ServerSocket 
{
	// Use Cached Thread Pool to upgrade performance
	private static ExecutorService executorService = Executors.newCachedThreadPool();
	private static int port = 6666; // port number
	private static boolean isPrint = false; //A flag of whether all messages are already printed 
	private static List<String> user_list = new ArrayList<String>(); //A list of the connected users
	private static List<ServerThread> thread_list = new ArrayList<ServerThread>(); //A list of threads that the server created
	private static LinkedList<Message> message_list = new LinkedList<Message>(); //A list of users' messages
	static AtomicInteger sum = new AtomicInteger(); //The account of connected client number
	String WBshapes = "I"; //Store the latest shape of the Whiteboard and send it when a new client connect
	String[] onlineusers; //Store all online users
	static WB_Server wb = new WB_Server(); //Java Swing
	private static ServerSocket server = null; //Server initializing
	static boolean isconnected = true; 
	
	String [] names =new String[] {"Kangaroo","Koala","Eagle", "Wombat", "Quokka", "Elephant", "Tiger", "Fish", "Cat", "Dog"}; //Name
 
	/**
	 * Create a Server Socket, and create a thread to send messages to the client, listen to client requests and process
	 */
	
	public Server() throws IOException 
	{
		server = new ServerSocket(port);
		new PrintOutThread(); //Process all messages sent by the client
		JOptionPane.showMessageDialog(wb, "Server is opened", "Server", JOptionPane.INFORMATION_MESSAGE);
		try 
		{
			while (true) 
			{ 
				//Listen to client requests and start a thread to process
				Socket socket = server.accept();
				executorService.execute(new ServerThread(socket));
			}
		} 
		catch (Exception e) 
		{
		} 
		finally 
		{
			close();
		}
	}
 
	/**
	 * Thread class that listens for messages in the queue and sends messages to clients other than themselves
	 */
 
	class PrintOutThread extends Thread 
	{
 
		public PrintOutThread() 
		{
			start();
		}
 
		@Override
		public void run() 
		{
			while (true) 
			{
				//If there is no message in the message queue, suspend the current thread and give up the CPU fragment to other threads to improve performance
				if (!isPrint) 
				{
					try 
					{
						Thread.sleep(500);
						sleep(100);
					} catch (InterruptedException e) 
					{
						JOptionPane.showMessageDialog(wb, "An error occurs in the Server", "Server Errors", JOptionPane.INFORMATION_MESSAGE);
					}
					continue;
				}
				//Send messages cached in the queue to each client in sequence and clear them from the queue
				Message message = (Message) message_list.getFirst();
				//Iterate over all user threads and broadcast to others if it is not a message sent by yourself
				for (int i = 0; i < thread_list.size(); i++) 
				{
					//Since the adding thread and the user are together, the user corresponding to i is the thread corresponding to i, and it can be judged whether it is its own thread according to
					ServerThread thread = thread_list.get(i);
					if (message.getName() != user_list.get(i)) 
					{
						thread.sendMessage(message);
					}
				}
				message_list.removeFirst();
				isPrint = message_list.size() > 0 ? true : false;
				
			}
		}
	}
 
	/**
	 * Server thread class
	 */
	class ServerThread extends Thread 
	{
		private Socket client;
		private PrintWriter out;
		private BufferedReader in;
		//The name of client
		private String name;
 
		public ServerThread(Socket s) throws IOException 
		{
			isconnected = true;
			client = s;
			out = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			int Clientnum = sum.get(); //The number of client
			name = names[Clientnum]; //The assigned name of client
			sum.getAndIncrement(); 
			//The Confirmdialog used to let the administrator decides whether to allow the client to connect
			int i = JOptionPane.showConfirmDialog(wb, name + " want to connect, do you approve？", "Connection request",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(i == JOptionPane.YES_OPTION) //Approve
				{
					String namelist = "N";
					String str = "App" + name;
					out.println(str);
					user_list.add(name);
					thread_list.add(this);
					onlineusers = user_list.toArray(new String[user_list.size()]);
					wb.list.setListData(onlineusers);
					for(int j = 0; j <user_list.size(); j++)
					{	
						namelist += onlineusers[j] + ",";	
					} 
					out.println(namelist);
					
					pushMessage(name, namelist);
					
					//When the latest whiteboard is not null
					if(!"I".equals(WBshapes))
					{
						out.println(WBshapes);
					}
				}
				else //Don't approve
				{
					out.println("Notapproval");
					sum.getAndDecrement();
					isconnected = false;
					in.close();
					out.close();
					client.close();
				}
			start();
		}
 
		@Override
		public void run() 
		{
			try 
			{
				while (isconnected) 
				{
					String line = in.readLine();
					//Client exits
					if(line.equals("Bye")) 
					{
						pushMessage(name, "Exit");
						out.println("Bye");
						thread_list.remove(this);
						user_list.remove(name);
						onlineusers = user_list.toArray(new String[user_list.size()]);
						wb.list.setListData(onlineusers);
						break;
					}
					//First user close
					else if(line.equals("ByeS")) 
					{
						isconnected = false;
						server.close();
						JOptionPane.showMessageDialog(wb, "First user close the client, the server will also be closed", "Server closed", JOptionPane.INFORMATION_MESSAGE);
						System.exit(0);
					}
					//SaveTxt Function
					else if(line.equals("SaveTxt"))
					{
						out.println("ST" + WBshapes);
					}
					//Open function
					else if(line.substring(0,1).equals("I")) 
					{
						WBshapes = line;
						pushMessage(name, line);
					}
					//Kick out function
					else if(line.substring(0,1).equals("K")) 
					{
						String kickoutname = line.substring(1);
						int thread_num = user_list.indexOf(kickoutname);
						ServerThread thread = thread_list.get(thread_num);
						Message message = new Message(name,"K");
						thread.sendMessage(message);
					}
					//Other functions
					else
					{
						//Clear function
						if(line.equals("C")) 
						{
							WBshapes = "I";
						}
						//Store the shape of whiteboard
						else 
						{
							WBshapes += line;
							WBshapes += ";";
						}
						pushMessage(name, line);
					}
				}	
			} 
			catch (Exception e) 
			{
				JOptionPane.showMessageDialog(wb, "An error occurs in the Server", "Server Errors", JOptionPane.INFORMATION_MESSAGE);
			}
			finally //Client exits
			{	
				try 
				{
					in.close();
					out.close();
					client.close();
				} 
				catch (IOException e) 
				{
					JOptionPane.showMessageDialog(wb, "An error occurs in the Server", "Server Errors", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		}
 
		//Put it at the end of the message queue, ready to send to the client
		public void pushMessage(String name, String msg) 
		{
			Message message = new Message(name, msg);
			//Put message
			message_list.addLast(message);
			//Indicates that you can send messages to other users
			isPrint = true;
		}
 
		//Send a message to the client
		public void sendMessage(Message message) 
		{
			out.println(message.getName() + ":" + message.getMessage());
		}
	}
 
	public static void main(String[] args) throws IOException 
	{
		//UI visualization
		wb.setVisible(true);
		wb.setResizable(false); //The interface cannot be resized
		
		//Get and display the IP address and Port number of Server
		try 
		{
			wb.textField.setText(InetAddress.getLocalHost().getHostAddress().toString());
		} 
		catch (UnknownHostException e) 
		{
			String str = "Cannot get the server IP address now! Please try again later.";
			JOptionPane.showMessageDialog(wb, str, "Getting IP address wrong", JOptionPane.INFORMATION_MESSAGE);
		}
		wb.textField_1.setText(Integer.toString(port));		
		
		//Close the server function
		wb.btnNewButton_1.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				int i = JOptionPane.showConfirmDialog(wb, "Are you sure you want to close the server? ", "Comfirmation",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if(i == JOptionPane.YES_OPTION) 
				{
					try 
					{
						isconnected = false;
						server.close();
						JOptionPane.showMessageDialog(wb, "The server is closed", "Server closed", JOptionPane.INFORMATION_MESSAGE);
						System.exit(0);
					} 
					catch (IOException e1) 
					{
						JOptionPane.showMessageDialog(wb, "An error occurs in the Server", "Server Errors", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				else 
				{					
				}
			}
		});
		
		try 
		{
			//Start the server
			new Server();
		} 
		catch (IOException e1) 
		{
			e1.printStackTrace();
			JOptionPane.showMessageDialog(wb, "An error occurs in the Server", "Server Errors", JOptionPane.INFORMATION_MESSAGE);
		}	
	}
}