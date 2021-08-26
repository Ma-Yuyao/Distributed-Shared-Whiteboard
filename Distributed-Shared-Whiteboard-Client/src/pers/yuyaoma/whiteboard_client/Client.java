package pers.yuyaoma.whiteboard_client;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author: Yuyao Ma
 * @className: Client
 * @packageName: pers.yuyaoma.whiteboard_client
 * @description: The Client class of the whiteboard app
 * @data: 2021-06-03
 **/

public class Client extends JPanel 
{
	private static final long serialVersionUID = 1L;
	//Shape array definition
	static Shape[] shapeArray = new Shape[1024000];
	static Socket client;
	static PrintStream out;
    static BufferedReader in;
    
    static int index = 0;
    
    static Graphics g;
    
	static JFrame jf = new JFrame();
    
	static AtomicInteger ifapproved = new AtomicInteger(); //0--wait for approval; 1--approval; 2--Disapproval
	
	static String name = "";
	
	private static List<String> user_list = new ArrayList<String>();//The list of online users
	
	static String[] onlinepeer;
	
	static JList<String> list = new JList<String>();
	
	static JTextField operation_name = new JTextField(5);
	
	static boolean isconnected = true;
	
	public static void main(String[] args) 
	{
	    //UI
		Client sd = new Client();
		
		client = ConnectionFunction();
		
		new readLineThread(client);
		
		g = sd.showUI(client);
		
		jf.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    
		//Approval results from Administrator
	    while(ifapproved.get() == 0) //Wait for approval
	    {
	    	JOptionPane.showMessageDialog(jf, "Please wait for manager's approval", "Wait", JOptionPane.INFORMATION_MESSAGE);
	    }
	    if(ifapproved.get() == 1) //Approval
	    {
	    	if(name.equals("Kangaroo"))
	    		JOptionPane.showMessageDialog(jf, "Manager approves your connection, feel free to use! Your username is '" + name + "'\n You are the first user so you can kick out other users and if you exit, the server will close", "Approved", JOptionPane.INFORMATION_MESSAGE);
	    	else
	    		JOptionPane.showMessageDialog(jf, "Manager approves your connection, feel free to use! Your username is '" + name + "'", "Approved", JOptionPane.INFORMATION_MESSAGE);
	    }
	    else if (ifapproved.get() == 2) //Disapproval
	    {
	    	try 
			{
	    		isconnected = false;
			    in.close();
                out.close();
                client.close();
			}
			catch(Exception e)
			{	
			}
	    	JOptionPane.showMessageDialog(jf, "Sorry! Manager does not approve your connection! The applicaiton will close", "Not Approved", JOptionPane.INFORMATION_MESSAGE);
	    	System.exit(0);
	    }
	    
	    //Define first user(whose name is Kangaroo)
	    if(name.equals("Kangaroo"))
	    	jf.setTitle("White Board" + "  "+ "Username: " + name + "  The first user");
	    else
	    	jf.setTitle("White Board" + "  "+ "Username: " + name);
	}
	
	//When the server is opened, pop up the server connection window
	public static Socket ConnectionFunction()
	{
		Socket client = null;
		boolean isConnected2 = false;
		while(!isConnected2)
		{
			String IPandPort = JOptionPane.showInputDialog(null, "Please enter the server IP address and port, separated by Space\nFor example:'127.0.0.1 6666'\nNOTE: if you click Cancel button, the program will close", "Connect to the server", JOptionPane.PLAIN_MESSAGE);
			if (IPandPort == null) 
			{
				System.exit(0);
			}
			String[] IPandPort1 = IPandPort.split(" ");
			try 
			{
				client = new Socket(IPandPort1[0], Integer.parseInt( IPandPort1[1]) );
				isConnected2 = true;
			}
			catch(Exception e)
			{
				String str = "";
				str = "Connection failed!\n Please check the server-address, server-port and server status then try again!\nNote: the client will close and please open it again after the server is running normally.";
				JOptionPane.showMessageDialog(null, str, "Connection failed!", JOptionPane.INFORMATION_MESSAGE);
			}
		}
			return client;
	}
		
    // Thread class for monitoring the server to send messages to the client
	public static class readLineThread extends Thread
	{
        
        private BufferedReader buff;
        public readLineThread(Socket client)
        {
            try 
            {
                buff = new BufferedReader(new InputStreamReader(client.getInputStream()));
                start(); 	    
            } 
            catch (Exception e) 
            {
            	JOptionPane.showMessageDialog(null, "Connection failed!", "Connection failed!", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        
        @Override
        public void run() 
        {
            try 
            {
                while(isconnected)
                {
                	String result = buff.readLine();
                	if("Bye".equals(result))
                	{
                		//The client applies for exit, and the server returns to confirm exit
                		isconnected = false;
                		break;
                	}
                	//Server approves to join
                	else if("App".equals(result.substring(0, 3)))
                    {
                        name = result.substring(3);
                        ifapproved.getAndIncrement();
                    }
                	//Server disapproves to join
                	else if ("Notapproval".equals(result))
                	{
                        ifapproved.getAndIncrement();
                        ifapproved.getAndIncrement();
                        break;
                    }
                	//SaveTxt
                	else if ("ST".equals(result.substring(0,2)))
                	{
                		String WBshapes = result.substring(2);
                		JFileChooser chooser = new JFileChooser();
        				chooser.showSaveDialog(null);
        				File file =chooser.getSelectedFile();
        		        String path = file.getAbsolutePath() + ".txt";
        		        FileWriter fwriter = null;
        				try 
        				{
        					fwriter = new FileWriter(path);
        					fwriter.write(WBshapes);
        					JOptionPane.showMessageDialog(jf, "Whiteboard is saved successfully! The path: " + path + " You can open the whiteboard by using it.", "Whitboard saved", JOptionPane.INFORMATION_MESSAGE);
        				} 
        				catch (Exception e1) 
        				{
        					JOptionPane.showMessageDialog(jf, "An error occurs when save the pictures", "Error", JOptionPane.INFORMATION_MESSAGE);
        				}
        				finally 
        				{
        					try 
        					{
        					   fwriter.flush();
        					   fwriter.close();
        					} 
        					catch (IOException ex) 
        					{
        						JOptionPane.showMessageDialog(jf, "An error occurs when save the pictures", "Error", JOptionPane.INFORMATION_MESSAGE);
        					}
        				}
                	}
                	//The server initializes and sends all shapes on the current whiteboard
                    else if ("I".equals(result.substring(0,1)))
                    {
                        String WBshapesmes = result.substring(1);
                        String WBshapes[] = WBshapesmes.split(";");
                        for(int i = 0; i < WBshapes.length; i++)
                        {
                        	String strshape = WBshapes[i].substring(0,1);
                        	//Text
                            if(strshape.equals("T"))
                            {
                            	String textinformation = WBshapes[i].substring(1);
                            	String textinfor[] = textinformation.split(",");
                            	int x, y;
                            	x = Integer.parseInt(textinfor[0]);
                            	y = Integer.parseInt(textinfor[1]);
                            	String text = textinfor[2];	
                            	String colornum = textinfor[3];
                            	Color currentcolor = g.getColor();
                            	switch(colornum)
                            	{
                            		case "1":
                            			g.setColor(Color.BLACK);
                            			break;
                            		case "2":
                            			g.setColor(Color.RED);
                            			break;
                            		case "3":
                            			g.setColor(Color.GREEN);
                            			break;
                            		case "4":
                            			g.setColor(Color.YELLOW);
                            			break;
                            		case "5":
                            			g.setColor(Color.CYAN);
                            			break;
                            		case "6":
                            			g.setColor(Color.LIGHT_GRAY);
                            			break;
                            		case "7":
                            			g.setColor(Color.PINK);
                            			break;
                            		case "8":
                            			g.setColor(Color.ORANGE);
                            			break;
                            	}
                            	g.drawString(text, x, y);
             		            Shape shape = new Shape(x, y, text, "Text", g.getColor()); //Create a Shape object and save the data of the shape
             		            index = index > Listener.index ? index : Listener.index;
             		            shapeArray[index] = shape; //Save shape objects in an array
             		            index++;
             		            g.setColor(currentcolor);
                            }
                            
                            //Shapes
                            else 
                            {
                            	int x1,x2,y1,y2;
                            	String colornum = "";
                               	String coordinate = WBshapes[i].substring(1);
                            	String coor[] = coordinate.split(",");
                           		x1 = Integer.parseInt(coor[0]);
                           		y1 = Integer.parseInt(coor[1]);
                           		x2 = Integer.parseInt(coor[2]);
                           		y2 = Integer.parseInt(coor[3]);
                            	colornum = coor[4];
                           		switch(colornum)
                           		{
                           			case "1":
                           				g.setColor(Color.BLACK);
                           				break;
                           			case "2":
                           				g.setColor(Color.RED);
                           				break;
                           			case "3":
                           				g.setColor(Color.GREEN);
                           				break;
                           			case "4":
                           				g.setColor(Color.YELLOW);
                           				break;
                           			case "5":
                           				g.setColor(Color.CYAN);
                           				break;
                           			case "6":
                           				g.setColor(Color.LIGHT_GRAY);
                           				break;
                           			case "7":
                           				g.setColor(Color.PINK);
                           				break;
                           			case "8":
                           				g.setColor(Color.ORANGE);                            				
                           				break;
                            	}
                               	//Straignt Line
                               	if(strshape.equals("L"))
                               	{     
                               		//Draw Straight Line
                                	g.drawLine(x1, y1, x2, y2);
                                	Shape shape = new Shape(x1, y1, x2, y2, "Straight Line", g.getColor());
                                	index = index > Listener.index ? index : Listener.index;
                        			shapeArray[index] = shape;
                        			index++;
                               	}
                                //Oval
                               	else if(strshape.equals("O"))
                                {
                                	//Draw Oval
                               		g.drawOval(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
                               		Shape shape = new Shape(x1, y1, x2, y2, "Oval", g.getColor());
                               		index = index > Listener.index ? index : Listener.index;
                        			shapeArray[index] = shape;
                        			index++;
                               	}
                               	//Rectangle
                               	else if(strshape.equals("R"))
                               	{
                               		// Draw Rectangle
                               		g.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2)); 
                               		Shape shape = new Shape(x1, y1, x2, y2, "Rectangle", g.getColor());
                               		index = index > Listener.index ? index : Listener.index;
                        			shapeArray[index] = shape;
                        			index++;
                               	}
                           	}
                       	}
                        g.setColor(Color.BLACK);
                    }
                	
                	//Online peer list
                    else if (result.substring(0, 1).equals("N"))
                    {
                      	user_list.add(name);
                       	String temp = result.substring(1);
                       	String[] namelist = temp.split(",");
                       	for(int i = 0; i < namelist.length; i++)
                       	{
                       		if(!namelist[i].equals(name))
                       		{
                       			user_list.add(namelist[i]);
                       		}
                       	}
                       	onlinepeer = user_list.toArray(new String[user_list.size()]);
                   		list.setListData(onlinepeer); 
                   	}
                	
                	//Synchronize others' operations
                    else
                    {	
                        String str1[] = result.split(":");
                       	String operationname = str1[0];
                       	operation_name.setText(operationname);
                        String operation = str1[1];
                       	String strshape = operation.substring(0,1);
                       	//Update the peer list when a new client joins
                        if(strshape.equals("N"))
                       	{
                       		user_list.clear();
                       		user_list.add(name);
                       		String temp = operation.substring(1);
                           	String[] namelist = temp.split(",");
                           	for(int i = 0; i < namelist.length; i++)
                           	{
                           		if(!namelist[i].equals(name))
                           		{
                           			user_list.add(namelist[i]);	
                           		}
                           	}
                           	onlinepeer = user_list.toArray(new String[user_list.size()]);
                       		list.setListData(onlinepeer);
                       	}
                        //Update the peer list when a current client exits
                        else if(strshape.equals("E"))
                        {
                        	user_list.remove(operationname);
                           	onlinepeer = user_list.toArray(new String[user_list.size()]);
                       		list.setListData(onlinepeer);
                       	}
                        //Clear function
                        else if(strshape.equals("C"))
                        {
                       		Listener.jp.repaint();
                       		index = Listener.index > index ? Listener.index : index;
                   			for(int k=0;k<index;k++)
                   			{
                   				if (shapeArray[k] != null)
                   					shapeArray[k].reset();
                   				else
                   					break;
                   			}
                   			Listener.jp.updateUI();
                   			JOptionPane.showMessageDialog(jf, "The first user create a new Whiteboard", "Create a new whiteboard", JOptionPane.INFORMATION_MESSAGE);
                       	}
                        //Open a previous WB function
                        else if (strshape.equals("I"))
                        {
                        	Listener.jp.repaint();
                       		index = Listener.index > index ? Listener.index : index;
                   			for(int k=0;k<index;k++)
                   			{
                   				if (shapeArray[k] != null)
                   					shapeArray[k].reset();
                   				else
                   					break;
                   			}
                   			Listener.jp.updateUI();
                   			
                            String WBshapesmes2 = operation.substring(1);
                            String WBshapes2[] = WBshapesmes2.split(";");
                            Color currentcolor = g.getColor();
                            for(int i = 0; i < WBshapes2.length; i++)
                            {
                            	String strshape2 = WBshapes2[i].substring(0,1);
                            	//Text
                                if(strshape2.equals("T"))
                                {
                                	String textinformation2 = WBshapes2[i].substring(1);
                                	String textinfor2[] = textinformation2.split(",");
                                	int x, y;
                                	x = Integer.parseInt(textinfor2[0]);
                                	y = Integer.parseInt(textinfor2[1]);
                                	String text = textinfor2[2];	
                                	String colornum2 = textinfor2[3];
                                	switch(colornum2)
                                	{
                                		case "1":
                                			g.setColor(Color.BLACK);
                                			break;
                                		case "2":
                                			g.setColor(Color.RED);
                                			break;
                                		case "3":
                                			g.setColor(Color.GREEN);
                                			break;
                                		case "4":
                                			g.setColor(Color.YELLOW);
                                			break;
                                		case "5":
                                			g.setColor(Color.CYAN);
                                			break;
                                		case "6":
                                			g.setColor(Color.GRAY);
                                			break;
                                		case "7":
                                			g.setColor(Color.LIGHT_GRAY);
                                			break;
                                		case "8":
                                			g.setColor(Color.PINK);
                                			break;
                                		case "9":
                                			g.setColor(Color.ORANGE);
                                			break;
                                	}
                                	g.drawString(text, x, y);
                 		            Shape shape = new Shape(x, y, text, "Text", g.getColor()); //Create a Shape object and save the data of the shape
                 		            index = index > Listener.index ? index : Listener.index;
                 		            shapeArray[index] = shape; //Save shape objects in an array
                 		            index++;
                                }
                                
                                //Shapes
                                else 
                                {
                                	int x1,x2,y1,y2;
                                	String colornum2 = "";
                                   	String coordinate2 = WBshapes2[i].substring(1);
                                	String coor2[] = coordinate2.split(",");
                               		x1 = Integer.parseInt(coor2[0]);
                               		y1 = Integer.parseInt(coor2[1]);
                               		x2 = Integer.parseInt(coor2[2]);
                               		y2 = Integer.parseInt(coor2[3]);
                                	colornum2 = coor2[4];
                               		switch(colornum2)
                               		{
                               			case "1":
                               				g.setColor(Color.BLACK);
                               				break;
                               			case "2":
                               				g.setColor(Color.RED);
                               				break;
                               			case "3":
                               				g.setColor(Color.GREEN);
                               				break;
                               			case "4":
                               				g.setColor(Color.YELLOW);
                               				break;
                               			case "5":
                               				g.setColor(Color.CYAN);
                               				break;
                               			case "6":
                               				g.setColor(Color.GRAY);
                               				break;
                               			case "7":
                               				g.setColor(Color.LIGHT_GRAY);
                               				break;
                               			case "8":
                               				g.setColor(Color.PINK);
                               				break;
                               			case "9":
                               				g.setColor(Color.ORANGE);                            				
                               				break;
                                	}
                                   	//Straignt Line
                                   	if(strshape2.equals("L"))
                                   	{     
                                   		//Draw Straight Line
                                    	g.drawLine(x1, y1, x2, y2);
                                    	Shape shape = new Shape(x1, y1, x2, y2, "Straight Line", g.getColor());
                            			shapeArray[i] = shape;
                            			index++;
                                   	}
                                    //Oval
                                   	else if(strshape2.equals("O"))
                                    {
                                    	//Draw Oval
                                   		g.drawOval(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
                                   		Shape shape = new Shape(x1, y1, x2, y2, "Oval", g.getColor()); 
                            			shapeArray[i] = shape; 
                            			index++;
                                   	}
                                   	//Rectangle
                                   	else if(strshape2.equals("R"))
                                   	{
                                   		// Draw Rectangle
                                   		g.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2)); 
                                   		Shape shape = new Shape(x1, y1, x2, y2, "Rectangle", g.getColor()); 
                            			shapeArray[i] = shape;
                           				index++;
                                   	}
                               	}
                           	}
                            g.setColor(currentcolor);
                            JOptionPane.showMessageDialog(jf, "The first user open a previous Whiteboard", "Open a whiteboard", JOptionPane.INFORMATION_MESSAGE);
                        }
                        
                       	//Text
                        else if(strshape.equals("T"))
                       	{
                       		String textinformation = operation.substring(1);
                       		String textinfor[] = textinformation.split(",");
                       		int x, y;
                       		x = Integer.parseInt(textinfor[0]);
                       		y = Integer.parseInt(textinfor[1]);
                       		String text = textinfor[2];		
                       		String colornum = textinfor[3];
                       		Color currentcolor = g.getColor();
                       		switch(colornum)
                       		{
                       			case "1":
                       				g.setColor(Color.BLACK);
                       				break;
                       			case "2":
                       				g.setColor(Color.RED);
                       				break;
                       			case "3":
                       				g.setColor(Color.GREEN);
                       				break;
                       			case "4":
                       				g.setColor(Color.YELLOW);
                       				break;
                       			case "5":
                       				g.setColor(Color.CYAN);
                        			break;
                       			case "6":
                       				g.setColor(Color.LIGHT_GRAY);
                       				break;
                       			case "7":
                       				g.setColor(Color.PINK);
                       				break;
                       			case "8":
                       				g.setColor(Color.ORANGE);
                       				break;
                       		}
                       		g.drawString(text, x, y);
         	               	Shape shape = new Shape(x, y, text, "Text", g.getColor());
         	               	index = index > Listener.index ? index : Listener.index;
        		            shapeArray[index] = shape;
        		            index++;
         		            g.setColor(currentcolor);
                        }
                        //Kick out Function
                        else if(strshape.equals("K"))
                       	{
                       		out = new PrintStream(client.getOutputStream());
                       		out.println("Bye");
                        	Client.client.close();
                        	JOptionPane.showMessageDialog(jf, "Manager kicked out you", "Kick out", JOptionPane.INFORMATION_MESSAGE);
        					System.exit(0);
                       	}
                        //Shapes
                        else 
                        {
                        	int x1,x2,y1,y2;
                        	String colornum = "";
                           	String coordinate = operation.substring(1);
                        	String coor[] = coordinate.split(",");
                        	x1 = Integer.parseInt(coor[0]);
                       		y1 = Integer.parseInt(coor[1]);
                       		x2 = Integer.parseInt(coor[2]);
                       		y2 = Integer.parseInt(coor[3]);
                       		colornum = coor[4];
                       		Color currentcolor = g.getColor();
                       		switch(colornum)
                       		{
                       			case "1":
                       				g.setColor(Color.BLACK);
                       				break;
                       			case "2":
                       				g.setColor(Color.RED);
                       				break;
                       			case "3":
                       				g.setColor(Color.GREEN);
                       				break;
                       			case "4":
                       				g.setColor(Color.YELLOW);
                       				break;
                       			case "5":
                       				g.setColor(Color.CYAN);
                       				break;
                        		case "6":
                        			g.setColor(Color.LIGHT_GRAY);
                        			break;
                       			case "7":
                       				g.setColor(Color.PINK);
                       				break;
                       			case "8":
                       				g.setColor(Color.ORANGE);
                       				break;
                       		}
                       		//Straight Line
                            if(strshape.equals("L"))
                           	{     
                           		g.drawLine(x1, y1, x2, y2);
                           		Shape shape = new Shape(x1, y1, x2, y2, "Straight Line", g.getColor());
                           		index = Listener.index > index ? Listener.index : index;
                    			shapeArray[index] = shape;
                   				index++;	
                            }
                            //Oval
                            else if(strshape.equals("O"))
                            {
                            	g.drawOval(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
                            	Shape shape = new Shape(x1, y1, x2, y2, "Oval", g.getColor()); 
                           		index = Listener.index > index ? Listener.index : index;
                    			shapeArray[index] = shape; 
                    			index++;
                           	}
                            //Rectangle
                            else if(strshape.equals("R"))
                            {
                            	g.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2)); 
                            	Shape shape = new Shape(x1, y1, x2, y2, "Rectangle", g.getColor());
                           		index = Listener.index > index ? Listener.index : index;
                    			shapeArray[index] = shape;
                    			index++;
                           	}
                            g.setColor(currentcolor);
                        }
                    }
                }
            }
            catch (Exception e) 
            {
            	JOptionPane.showMessageDialog(jf, "Manager closed the application, your application will be closed", "Application closed!", JOptionPane.INFORMATION_MESSAGE);
            	System.exit(0);
            }
        }
    }
    
	public Graphics showUI(Socket client) 
	{
		JPanel jp1 = new JPanel(); //West areas
		JPanel jp2 = new JPanel(); //East areas
		java.awt.FlowLayout flow = new java.awt.FlowLayout();

		this.setBackground(Color.WHITE);
		this.setLayout(null);
		
		jp1.setBackground(Color.GRAY);
		jp1.setPreferredSize(new Dimension(80, 600));
		jp1.setLayout(flow);

		jp2.setBackground(Color.GRAY);
		jp2.setPreferredSize(new Dimension(80, 600));
		jp2.setLayout(flow);
		

		jf.setTitle("Yuyao's White Board"); 
		jf.setSize(760, 600); 
		jf.getContentPane().setBackground(Color.WHITE); 
		jf.setResizable(false); 
		jf.setDefaultCloseOperation(3); 
		jf.setLocationRelativeTo(null); 

		jf.add(jp1, BorderLayout.WEST);
		jf.add(jp2, BorderLayout.EAST);
		jf.add(this, BorderLayout.CENTER);

		//Create a mouse listener object
		Listener dlis = new Listener(client);
		//Add the mouse listener to the panel
		this.addMouseListener(dlis);
		this.addMouseMotionListener(dlis);
		String rootpath =System.getProperty("user.dir");
		//Create a shape button and add the action listener to the shape button
		String[] shape = { " S", " O", " R", " T"};
		for (int i = 0; i < shape.length; i++) 
		{
			String path= rootpath + "/src/Icons/" + i + ".png";
			Icon icon=new ImageIcon(path);
			JButton jbuName = new JButton(shape[i],icon);
			jbuName.setContentAreaFilled(false);
			jp1.add(jbuName);
			jbuName.addActionListener(dlis);
		}
		
		//Add the online peer list
		list.setBorder(BorderFactory.createTitledBorder("Online"));
		list.setBounds(5, 200, 70, 400);
		jp1.add(list);
		
		//Add the kick_out button
		String path = rootpath + "/src/Icons/" + "KickOut.png";
		Icon icon = new ImageIcon(path);
		JButton Kickout = new JButton("K", icon);
		Kickout.setContentAreaFilled(false);
		jp1.add(Kickout);
		Kickout.addActionListener(dlis);
		
		//Add the "Recently modified by:" function
		JLabel jl1 = new JLabel("Recently");
		JLabel jl2 = new JLabel("modified by:");
		jp1.add(jl1);
		jp1.add(jl2);
		operation_name.setEditable(false);
		jp1.add(operation_name);
		
		//Create a color button and add an action listener to the color button
		Color[] color = { Color.BLACK, Color.RED, Color.GREEN, Color.YELLOW, Color.CYAN, Color.LIGHT_GRAY,
				Color.PINK, Color.ORANGE };
		Dimension dm = new Dimension(30, 30);
		for (int i = 0; i < color.length; i++) {
			JButton jbuColor = new JButton();
			jbuColor.setPreferredSize(dm);
			jbuColor.setBackground(color[i]);
			jp2.add(jbuColor);
			jbuColor.addActionListener(dlis);
		}

		//Add "New" function
		String pathNew = rootpath + "/src/Icons/" + "New.png";
		Icon iconNew = new ImageIcon(pathNew);
		JButton jNew = new JButton("N",iconNew);
		jNew.setContentAreaFilled(false);
		jp2.add(jNew);
		jNew.addActionListener(dlis);		
		
		//Add "Open" function
		String pathopen = rootpath + "/src/Icons/" + "Open.png";
		Icon iconopen = new ImageIcon(pathopen);
		JButton jOpen = new JButton("O",iconopen);
		jOpen.setContentAreaFilled(false);
		jp2.add(jOpen);
		jOpen.addActionListener(dlis);
		
		//Add "Save Picture" function
		String path2 = rootpath + "/src/Icons/" + "Save.png";
		Icon icon2 = new ImageIcon(path2);
		JButton jSave = new JButton("P",icon2);
		jSave.setContentAreaFilled(false);
		jp2.add(jSave);
		jSave.addActionListener(dlis);		
		
		//Add "Save Txt" function
		String pathSavetxt = rootpath + "/src/Icons/" + "SaveTxt.png";
		Icon icontxt = new ImageIcon(pathSavetxt);
		JButton jSavetxt = new JButton("S",icontxt);
		jSavetxt.setContentAreaFilled(false);
		jp2.add(jSavetxt);
		jSavetxt.addActionListener(dlis);	
		
		//Add "Save as" function
		String path3 = rootpath + "/src/Icons/" + "SaveAs.png";
		Icon icon3 = new ImageIcon(path3);
		JButton jSaveAs = new JButton("A", icon3);
		jSaveAs.setContentAreaFilled(false);
		jp2.add(jSaveAs);
		jSaveAs.addActionListener(dlis);
		
		//Add "Exit" function
		String path4 = rootpath + "/src/Icons/" + "Exit.png";
		Icon icon4 = new ImageIcon(path4);
		JButton jExist = new JButton("E", icon4);
		jExist.setContentAreaFilled(false);
		jp2.add(jExist);
		jExist.addActionListener(dlis);
		
		dlis.setthis(this);
		
		jf.setVisible(true);
		//Get canvas object from form, That is to obtain the area occupied by the form on the screen, this area can change color
		java.awt.Graphics g = this.getGraphics();
		dlis.setg(g);
		dlis.setShapeArray(shapeArray);
		return g;
	}

	/*
	 * Override the method of drawing components
	 */
	public void paint(Graphics g) 
	{
		super.paint(g);
		//Take out the graphic objects saved in the shapeArray array and draw
		for (int i = 0; i < shapeArray.length; i++) 
		{
			Shape shape = shapeArray[i];
			if (shape != null) 
			{
				shape.drawShape(g);
			} else 
			{
				break;
			}
		}
	}
}