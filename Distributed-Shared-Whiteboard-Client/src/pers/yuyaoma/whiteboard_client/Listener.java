package pers.yuyaoma.whiteboard_client;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author: Yuyao Ma
 * @className: Listener
 * @packageName: pers.yuyaoma.whiteboard_client
 * @description: The Listener class of the whiteboard app, used as the listener
 * @data: 2021-06-03
 **/

public class Listener implements MouseListener, ActionListener, MouseMotionListener 
{

	private int a1, b1, a2, b2, x1, y1, x2, y2, x, y; //Record the coordinates of two mouse clicks
	private Graphics g; //Canvas object obtained from the interface object
	private int flag = 0;
	private Color color = Color.BLACK;
	static Shape[] shapeArray;
	
	static int index = 0;
	static JPanel jp; 
	private JTextField tf;
	String message = "";
	static int sum = 1;
	static boolean isTextfinished = true;

	Socket client = null;

	public Listener(Socket client)
	{
		this.client = client;
	}
	
	/**
	 * Constructor, initialize canvas object
	 * 
	 * @param g
	 */
	public void setg(Graphics f) 
	{
		g = f;
	}
	
	public void setthis(JPanel j)
	{
		jp=j;
	}

	public void setShapeArray(Shape[] shapeArray) 
	{
		this.shapeArray = shapeArray;
	}
	
	//MouseMotionListener
	public void mouseDragged(MouseEvent e) 
	{
	}
	
	//MouseMotionListener
	public void mouseMoved(MouseEvent e) 
	{
	}

	//ActionListener
	public void actionPerformed(ActionEvent e) 
	{
		String selected = e.getActionCommand();
		
		//Set the current drawing color
		g.setColor(color);

		if(isTextfinished == false)
		{
			jp.removeAll();
			jp.updateUI();
		}
		
		switch (selected) 
		{
			//Click "Straight Line"
			case " S":
				flag = 1;
				break;
			//Click "Oval"
			case " O":
				flag = 2;
				break;
			//Click "Rectangle"
			case " R":
				flag = 3;
				break;
			//Click "Text"
			case " T":
				flag = 4;
				break;
			//Click "Clear screen"
			case "N":
			{
				//Only the first user can Create a New Whiteboard
				if (Client.name.equals("Kangaroo"))
				{
					int i = JOptionPane.showConfirmDialog(jp, "Are you sure you want to create a new whiteboard? If you do so, all the shapes will be cleared", "Comfirmation",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(i == JOptionPane.YES_OPTION) 
					{
						PrintStream out;
						try 
						{
							out = new PrintStream(client.getOutputStream());
							out.println("C");
						} 
						catch (IOException e1) 
						{
							JOptionPane.showMessageDialog(jp, "An error occurs in the Client", "Client Errors", JOptionPane.INFORMATION_MESSAGE);
						}
						jp.repaint();
						index = Client.index > index ? Client.index : index;
						for(int j=0;j<index;j++)
						{
							if (shapeArray[j] != null)
								shapeArray[j].reset();
							else
								break;
						}
					}
					else
					{
					}
				}
				else
				{
					JOptionPane.showMessageDialog(jp, "Sorry, only the first user can Create a new whiteboard", "New", JOptionPane.INFORMATION_MESSAGE);
				}
				break;
			}
			//Click "Exit"
			case "E":
				{
					//First user(Kangaroo) exits
					if (Client.name.equals("Kangaroo"))
					{
						int i = JOptionPane.showConfirmDialog(jp, "Are you sure you want to exit? Note: Because you are the first user, if you exit, the server will close", "Comfirmation",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if(i == JOptionPane.YES_OPTION) 
						{
							PrintStream out;
							try 
							{
								out = new PrintStream(client.getOutputStream());
								out.println("ByeS");
								JOptionPane.showMessageDialog(jp, "Thanks you for using", "Thanks", JOptionPane.INFORMATION_MESSAGE);
								System.exit(0);
							} 
							catch (IOException e1) 
							{
								JOptionPane.showMessageDialog(jp, "An error occurs in the Client", "Client Errors", JOptionPane.INFORMATION_MESSAGE);
							}
						}
						else
						{
						}
					}
					//Other user exits
					else
					{
						int i = JOptionPane.showConfirmDialog(jp, "Are you sure you want to exit?", "Comfirmation",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						if(i == JOptionPane.YES_OPTION) 
						{
							PrintStream out;
							try 
							{
								out = new PrintStream(client.getOutputStream());
								out.println("Bye");
								JOptionPane.showMessageDialog(jp, "Thanks you for using", "Thanks", JOptionPane.INFORMATION_MESSAGE);
								System.exit(0);
							} 
							catch (IOException e1) 
							{
								JOptionPane.showMessageDialog(jp, "An error occurs in the Client", "Client Errors", JOptionPane.INFORMATION_MESSAGE);
							}
						}
						else
						{
						}
					}
					break;
				}
			//Click "KickOut"
			case "K":
			{
				//Only the first user("Kangaroo") can use kick out function
				if(Client.name.equals("Kangaroo"))
				{
					if(Client.list.getSelectedValue() != null)
					{
						String kickoutname = Client.list.getSelectedValue();
						
						if(kickoutname.equals("Kangaroo"))
						{
							JOptionPane.showMessageDialog(jp, "Can not select yourself to kick out", "Kick out", JOptionPane.INFORMATION_MESSAGE);
						}
						else
						{
							int i = JOptionPane.showConfirmDialog(jp, "Are you sure you want to kick out this user?", "Comfirmation",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
							if(i == JOptionPane.YES_OPTION) 
							{
								try 
								{
									PrintStream out = new PrintStream(client.getOutputStream());;
									out.println("K" + kickoutname);
									JOptionPane.showMessageDialog(jp, "Kick out successfully", "Kick out", JOptionPane.INFORMATION_MESSAGE);
								} 
								catch (IOException e1) 
								{
									JOptionPane.showMessageDialog(jp, "An error occurs in the Client", "Client Errors", JOptionPane.INFORMATION_MESSAGE);
								}
							}
							else
							{
							}
						}	
					}
					else
					{
						JOptionPane.showMessageDialog(jp, "Please select the user you want to kick out", "Kick out", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				else
				{
					JOptionPane.showMessageDialog(jp, "Sorry, only the first user have the authority to kick out", "Kick out", JOptionPane.INFORMATION_MESSAGE);
				}
	
				break;
			}
			//Click "Save Picture"
			case "P":
			{
				try 
				{				
					int imageWidth = 600; //Width of image
					int imageHeight = 600;//Height of image
					BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
					Graphics savedgraphics = image.getGraphics();
					savedgraphics.fillRect(0, 0, imageWidth, imageHeight);
					index = Client.index > index ? Client.index : index;
					for (int k = 0; k < index; k++) 
					{
						Shape shape = shapeArray[k];
						if (shape != null) 
						{
							shape.drawShape(savedgraphics);
						} else 
						{
							break;
						}
					}
					ImageIO.write(image, "PNG", new File("D:\\" + Client.name + "-" + sum +".png"));
					JOptionPane.showMessageDialog(jp, "Picture is saved successfully!" + "D:\\" + Client.name + "-" + sum +".png", "Picture saved", JOptionPane.INFORMATION_MESSAGE);
					sum++;
				} 
				catch (IOException e1) 
				{
					JOptionPane.showMessageDialog(jp, "An error occurs when save the pictures", "Error", JOptionPane.INFORMATION_MESSAGE);
				}
				break;
			}
			//Click "SaveAs"
			case "A":
			{
				JFileChooser chooser = new JFileChooser();
				chooser.showSaveDialog(null);
				File file =chooser.getSelectedFile();
		        String path = file.getAbsolutePath() + ".png";
				try 
				{
					int imageWidth = 600;
					int imageHeight = 600;
					BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
					Graphics savedgraphics = image.getGraphics();
					savedgraphics.fillRect(0, 0, imageWidth, imageHeight);
					index = Client.index > index ? Client.index : index;
					for (int k = 0; k < index; k++) 
					{
						Shape shape = shapeArray[k];
						if (shape != null) 
						{
							shape.drawShape(savedgraphics);
						} 
						else 
						{
							break;
						}
					}
						
					ImageIO.write(image, "PNG", new File(path));
					JOptionPane.showMessageDialog(jp, "Picture is saved successfully! " + path, "Picture saved", JOptionPane.INFORMATION_MESSAGE);
				} 
				catch (Exception e1) 
				{
					JOptionPane.showMessageDialog(jp, "An error occurs when save the pictures", "Error", JOptionPane.INFORMATION_MESSAGE);
				}
				break;
			}
			
			//Save as text
			case "S":
			{
				if(Client.name.equals("Kangaroo"))
				{
					PrintStream out;
					try 
					{
						out = new PrintStream(client.getOutputStream());
						out.println("SaveTxt");
					} 
					catch (IOException e1) 
					{
						JOptionPane.showMessageDialog(jp, "An error occurs in the Client", "Client Errors", JOptionPane.INFORMATION_MESSAGE);
					}
				}
				else
				{
					JOptionPane.showMessageDialog(jp, "Sorry, only the first user have the authority to Save the current WhiteBoard", "Save", JOptionPane.INFORMATION_MESSAGE);
				}
				break;
			}
			
			//"Open"
			case "O":
			{
				if(Client.name.equals("Kangaroo"))
				{
					int i = JOptionPane.showConfirmDialog(jp, "Are you sure you want to open a previous Whitboard? If you do so, the current Whiteboard will be clear", "Comfirmation",JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
					if(i == JOptionPane.YES_OPTION) 
					{
						JFileChooser chooser = new JFileChooser();
						chooser.showOpenDialog(null);
						File file =chooser.getSelectedFile();
				        String path = file.getAbsolutePath();
				        String WBShapes = "";
						try 
						{
							String encoding = "UTF-8";
						    File file2 =new File(path);
							if (file2.isFile() && file2.exists()) 
							{ 
								InputStreamReader read = new InputStreamReader(new FileInputStream(file2), encoding);// 考虑到编码格式
								BufferedReader bufferedReader = new BufferedReader(read);
								String lineTxt = null;
								while ((lineTxt = bufferedReader.readLine()) != null) 
								{
									WBShapes = lineTxt;
								}
								read.close();
							}
						} 
						catch (Exception e1) 
						{
							JOptionPane.showMessageDialog(jp, "Sorry, read the wrong file, please make sure your file is saved by Yuyao's Whiteboard app", "SaveTxt", JOptionPane.INFORMATION_MESSAGE);
						}
						
						PrintStream out;
						try 
						{
							out = new PrintStream(client.getOutputStream());
							out.println(WBShapes);
						} 
						catch (IOException e1) 
						{
							JOptionPane.showMessageDialog(jp, "An error occurs in the Client", "Client Errors", JOptionPane.INFORMATION_MESSAGE);
						}
						
						jp.repaint();
						index = Client.index > index ? Client.index : index;
						for(int j=0;j<index;j++)
						{
							if (shapeArray[j] != null)
								shapeArray[j].reset();
							else
								break;
						}
						
						String WBshapesmes = WBShapes.substring(1);
                        String WBshapes[] = WBshapesmes.split(";");
                        for(int i1 = 0; i1 < WBshapes.length; i1++)
                        {
                        	String strshape = WBshapes[i1].substring(0,1);
                        	//Text
                            if(strshape.equals("T"))
                            {
                            	String textinformation = WBshapes[i1].substring(1);
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
                               	String coordinate = WBshapes[i1].substring(1);
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
                 		            shapeArray[index] = shape; //Save shape objects in an array
                 		            index++;
                               	}
                                //Oval
                               	else if(strshape.equals("O"))
                                {
                                	//Draw Oval
                               		g.drawOval(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
                               		Shape shape = new Shape(x1, y1, x2, y2, "Oval", g.getColor()); // 创建Shape对象，保存该图形的数据
                 		            index = index > Listener.index ? index : Listener.index;
                 		            shapeArray[index] = shape; //Save shape objects in an array
                 		            index++;
                               	}
                               	//Rectangle
                               	else if(strshape.equals("R"))
                               	{
                               		// Draw Rectangle
                               		g.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2)); 
                               		Shape shape = new Shape(x1, y1, x2, y2, "Rectangle", g.getColor()); // 创建Shape对象，保存该图形的数据
                 		            index = index > Listener.index ? index : Listener.index;
                 		            shapeArray[index] = shape; //Save shape objects in an array
                 		            index++;
                               	}
                           	}
                       	}
                        g.setColor(Color.BLACK);
                        
						JOptionPane.showMessageDialog(jp, "The whiteboard is opened successfully", "Open", JOptionPane.INFORMATION_MESSAGE);
					}
					else
					{
					}
				}
				else
				{
					JOptionPane.showMessageDialog(jp, "Sorry, only the first user have the authority to Open a previous Whiteboard", "Open", JOptionPane.INFORMATION_MESSAGE);
				}
				
				break;
			}
				
			//Click "Colors"
			case "":
				JButton jb = (JButton) e.getSource();
				color = jb.getBackground();
				g.setColor(color);
				break;
		}
	}

	//The coordinates of the point when the mouse is pressed
	public void mousePressed(MouseEvent e) 
	{
		a1 = e.getX();
		b1 = e.getY();
	}

	//The coordinates of the point when the mouse is released
	public void mouseReleased(MouseEvent e) 
	{
		Client.operation_name.setText(Client.name);
		a2 = e.getX();
		b2 = e.getY();

		if (a1 != a2) 
		{
			x1 = a1;
			y1 = b1;
			x2 = a2;
			y2 = b2;
		}
		
		try
		{
			String colornum = "";
			if(color == Color.BLACK)
				colornum = "1";
			else if(color == Color.RED)
				colornum = "2";
			else if(color == Color.GREEN)
				colornum = "3";
			else if(color == Color.YELLOW)
				colornum = "4";
			else if(color == Color.CYAN)
				colornum = "5";
			else if(color == Color.LIGHT_GRAY)
				colornum = "6";
			else if(color == Color.PINK)
				colornum = "7";
			else if(color == Color.ORANGE)
				colornum = "8";
			
			//Get Socket output stream, used to send data to the server
			PrintStream out = new PrintStream(client.getOutputStream());

			//StraightLine
			if (flag == 1) 
			{
				g.drawLine(x1, y1, x2, y2);
				Shape shape = new Shape(x1, y1, x2, y2, "Straight Line", color);
				index = Client.index > index ? Client.index : index;
				shapeArray[index] = shape;
				index++;
				message = "L" + Integer.toString(x1) + "," + Integer.toString(y1) + "," + Integer.toString(x2) + "," + Integer.toString(y2) + ","  + colornum;
				out.println(message);
			}
			//Oval
			if (flag == 2) 
			{
				g.drawOval(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
				Shape shape = new Shape(x1, y1, x2, y2, "Oval", color);
				index = Client.index > index ? Client.index : index;
				shapeArray[index] = shape;
				index++;
				message = "O" + Integer.toString(x1) + "," + Integer.toString(y1) + "," + Integer.toString(x2) + "," + Integer.toString(y2) + ","  + colornum;
				out.println(message);
			}
			//Rectangle
			if (flag == 3) 
			{
				g.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
				Shape shape = new Shape(x1, y1, x2, y2, "Rectangle", color);
				index = Client.index > index ? Client.index : index;
				shapeArray[index] = shape;
				index++;
				message = "R" + Integer.toString(x1) + "," + Integer.toString(y1) + "," + Integer.toString(x2) + "," + Integer.toString(y2) + "," + colornum;
				out.println(message);
			}
			//Text
			if(flag == 4)
			{	
				jp.removeAll();
				jp.updateUI();
				isTextfinished = false;
				tf = new JTextField();
				jp.add(tf);
				tf.setBounds(a1,b1, 60, 30);
				tf.addFocusListener(new FocusListener()
				{
		            @Override
		            //When focus is lost
		            public void focusLost(FocusEvent e) 
		            {  
		               isTextfinished = true;
		               String text = tf.getText();
		               jp.remove(tf);
		               jp.updateUI();
		               x = a1 + 2;
		               y = b1 + 17;
		               g.drawString(text, x , y);
		               Shape shape = new Shape(x, y, text, "Text", color); 
		               index = Client.index > index ? Client.index : index;
		               shapeArray[index] = shape;
		               index++;
		               String colornum = "";
		   				if(color == Color.BLACK)
		   					colornum = "1";
		   				else if(color == Color.RED)
		   					colornum = "2";
		   				else if(color == Color.GREEN)
		   					colornum = "3";
		   				else if(color == Color.YELLOW)
		   					colornum = "4";
		   				else if(color == Color.CYAN)
		   					colornum = "5";
		   				else if(color == Color.LIGHT_GRAY)
		   					colornum = "6";
		   				else if(color == Color.PINK)
		   					colornum = "7";
		   				else if(color == Color.ORANGE)
		   					colornum = "8";
		               message = "T" + Integer.toString(x) + "," + Integer.toString(y) + "," + text + "," + colornum;
		               out.println(message);
		            }
		            @Override
		             public void focusGained(FocusEvent e) 
		            {
		            	//When focus is got
		            }
		            
		        });			
			}
		}
		catch(Exception e1)
		{
			JOptionPane.showMessageDialog(jp, "An error occurs in the Client", "Client Errors", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	//mouseClicked
	public void mouseClicked(MouseEvent e) 
	{
	}

	//mouseEntered
	public void mouseEntered(MouseEvent e) 
	{
	}
	//mouseExited
	public void mouseExited(MouseEvent e) 
	{
	}
}