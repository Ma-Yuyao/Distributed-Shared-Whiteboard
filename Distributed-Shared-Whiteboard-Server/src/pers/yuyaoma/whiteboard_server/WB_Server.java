package pers.yuyaoma.whiteboard_server;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JList;

/**
 * @author: Yuyao Ma
 * @className: WB_Server
 * @packageName: pers.yuyaoma.whiteboard_client
 * @description: The Java Swing class of Server
 * @data: 2021-06-03
 **/

public class WB_Server extends JFrame {

	private JPanel contentPane;
	JPanel contentPane_1;
	public JTextField textField;
	public JTextField textField_1;
	public JButton btnNewButton_1;
	public JList<String> list;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
	{
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					WB_Server frame = new WB_Server();
					frame.setVisible(true);
				} catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public WB_Server() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 500, 500);
		contentPane = new JPanel();
//		setBounds(100, 100, 562, 535);
		contentPane_1 = new JPanel();
		contentPane_1.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane_1);
		contentPane_1.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Server-address");
		lblNewLabel.setFont(new Font("Times New Roman", Font.PLAIN, 16));
		lblNewLabel.setBounds(68, 19, 99, 20);
		contentPane_1.add(lblNewLabel);
		
		textField = new JTextField();
		textField.setFont(new Font("Times New Roman", Font.PLAIN, 16));
		textField.setHorizontalAlignment(SwingConstants.CENTER);
		textField.setBounds(217, 19, 99, 21);
		textField.setEditable(false);
		contentPane_1.add(textField);
		textField.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Server-port");
		lblNewLabel_1.setFont(new Font("Times New Roman", Font.PLAIN, 16));
		lblNewLabel_1.setBounds(68, 59, 83, 15);
		contentPane_1.add(lblNewLabel_1);
		
		textField_1 = new JTextField();
		textField_1.setEditable(false);
		textField_1.setFont(new Font("Times New Roman", Font.PLAIN, 16));
		textField_1.setHorizontalAlignment(SwingConstants.CENTER);
		textField_1.setBounds(217, 59, 99, 21);
		contentPane_1.add(textField_1);
		textField_1.setColumns(10);
		
		btnNewButton_1 = new JButton("Close server");
		btnNewButton_1.setFont(new Font("Times New Roman", Font.PLAIN, 16));
		btnNewButton_1.setBounds(338, 33, 119, 30);
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		contentPane_1.add(btnNewButton_1);
		
		list = new JList<String>();
		list.setBounds(68, 119, 348, 310);
		contentPane_1.add(list);
		
		JLabel lblNewLabel_1_1 = new JLabel("Online peer list");
		lblNewLabel_1_1.setFont(new Font("Times New Roman", Font.PLAIN, 16));
		lblNewLabel_1_1.setBounds(68, 94, 99, 15);
		contentPane_1.add(lblNewLabel_1_1);
	}
}
