import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.plaf.BorderUIResource;


public class ClientFrame extends JFrame implements ActionListener, KeyListener{
	
	JTextField idField;
	JTextField pwField;
	
	JButton loginButton;
	JButton signupButton;
	
	InputStream in;
	OutputStream out;
	
	static NewClient myClient;
	static ClientFrame myClientFrame;
	
	public void setGUI() {
		this.setSize(300,160);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Login");
		this.setLayout(new BorderLayout());
		
		JPanel buttonsJPanel = new JPanel();
		loginButton = new JButton("Login");
		signupButton = new JButton("Signup");
		
		JPanel textsJPanel = new JPanel();
		JPanel idPanel = new JPanel();
		JPanel pwPanel = new JPanel();
		JLabel idLabel = new JLabel("   ID : ");
		JLabel pwLabel = new JLabel("PW : ");
		idField = new JTextField("");
		pwField = new JTextField("");
		idPanel.setLayout(new BoxLayout(idPanel, BoxLayout.X_AXIS));
		pwPanel.setLayout(new BoxLayout(pwPanel, BoxLayout.X_AXIS));
		
		textsJPanel.setLayout(new BoxLayout(textsJPanel, BoxLayout.Y_AXIS));
		idLabel.setSize(100, 20);
		idPanel.add(idLabel);
		idPanel.add(idField);
		pwPanel.add(pwLabel);
		pwPanel.add(pwField);
		textsJPanel.add(idPanel);
		textsJPanel.add(pwPanel);
		
		buttonsJPanel.setLayout(new BorderLayout());
		buttonsJPanel.add(loginButton, BorderLayout.NORTH);
		buttonsJPanel.add(signupButton, BorderLayout.SOUTH);
		
		loginButton.addActionListener(this);
		signupButton.addActionListener(this);
		
		this.add(textsJPanel, BorderLayout.CENTER);
		this.add(buttonsJPanel,BorderLayout.SOUTH);
		this.setVisible(true);
		idField.addKeyListener(this);
		pwField.addKeyListener(this);
	}
	
	public ClientFrame(){
		this.setGUI();
		if(myClient.chkConnected()) {
			System.out.println("Connected!");
		}else {
			System.out.println("Connection Failed!");
		}
	}
	
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource()==loginButton) {
			boolean loggedin = myClient.login(idField.getText(), pwField.getText());
			if(loggedin) {
				ChattingFrame chattingFrame = new ChattingFrame(myClient);
				myClientFrame.dispose();
			}
		}else if(ae.getSource()==signupButton) {
			myClient.signup(idField.getText(), pwField.getText());
		}
	}
	

	public static void main(String[] args) {
		myClient = new NewClient();
		myClientFrame = new ClientFrame();
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			loginButton.doClick();
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
