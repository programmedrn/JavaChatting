import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.GraphicAttribute;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.AncestorListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.Segment;

import org.w3c.dom.stylesheets.DocumentStyle;


public class ChattingFrame extends JFrame implements ActionListener, KeyListener{
	
	NewClient myClient;
	
	int row=0;
	
	JTextField chatTextField;
	JTextPane chatBoardArea;
	JTextArea userBoardArea;
	JButton sendChatButton;
	JButton emoji;
	JButton logout;
	JButton idChange;
	JScrollPane chatBoardPane;
	
	ScrollablePanel content;
	GridBagConstraints gbc;
	
	public void setGUI() {
		
		content = new ScrollablePanel(new GridBagLayout());
		content.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 10, 10, 5);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		this.setTitle("Chat");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(600,600);

		chatTextField = new JTextField();
		chatBoardArea = new JTextPane();
		userBoardArea = new JTextArea();
		idChange = new JButton("Change ID");
		logout = new JButton("Logout");
		emoji = new JButton("emoji");
		sendChatButton = new JButton("Send");
		
		JPanel chatPanel = new JPanel();
		JPanel usersPanel = new JPanel();
		JPanel buttonsPanel = new JPanel();
		JPanel emojiIDPanel = new JPanel();
		chatBoardPane = new JScrollPane(content);
		
		userBoardArea.setBackground(Color.getHSBColor(210/360f, 0.2f, 0.82f));
		chatBoardArea.setBackground(Color.getHSBColor(20/360f, 0.2f, 0.82f));
		logout.setBackground(Color.getHSBColor(330/360f, 0.5f, 1f));
		
		emojiIDPanel.setLayout(new BoxLayout(emojiIDPanel, BoxLayout.X_AXIS));
		buttonsPanel.setLayout(new BorderLayout());
		chatPanel.setLayout(new BorderLayout());
		this.setLayout(new BorderLayout());
		
		emojiIDPanel.add(emoji);
		emojiIDPanel.add(idChange);
		buttonsPanel.add(logout, BorderLayout.NORTH);
		buttonsPanel.add(emojiIDPanel, BorderLayout.CENTER);
		buttonsPanel.add(sendChatButton, BorderLayout.SOUTH);

		usersPanel.setLayout(new BorderLayout());
		usersPanel.add(userBoardArea,BorderLayout.CENTER);
		usersPanel.add(buttonsPanel,BorderLayout.SOUTH);
		
		chatPanel.add(chatBoardPane, BorderLayout.CENTER);
		chatPanel.add(chatTextField, BorderLayout.SOUTH);

		sendChatButton.addActionListener(this);
		emoji.addActionListener(this);
		logout.addActionListener(this);
		idChange.addActionListener(this);
		
		chatBoardArea.setEditable(false);
		//1209 temp
		chatBoardPane.setPreferredSize(new Dimension(200, chatBoardPane.getPreferredSize().height));
		userBoardArea.setEditable(false);
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Chat");
		this.add(chatPanel,BorderLayout.CENTER);
		this.add(usersPanel,BorderLayout.EAST);
		this.setVisible(true);
	}
	
	public ChattingFrame(NewClient newClient){
		this.setGUI();
		chatTextField.addKeyListener(this);
		myClient = newClient;
		myClient.setChatFrame(this);
		chatBoardPane.setAutoscrolls(true);
	}
	
	public void clearChat() {
//		chatBoardArea.setText("");
		content.removeAll();
	}
	
	public void printOnChat(String message) {

		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(new EmptyBorder(5,5,5,5));
		panel.setBackground(Color.YELLOW);
		JTextArea jTextArea = new JTextArea();
//		Document document = jTextPane.getDocument();
//		try {
//			document.insertString(document.getLength(), message, null);
//		}catch (Exception e) {
//			System.out.println("Chat Printing Failed");
//		}
//		
		jTextArea.append(message);
		jTextArea.setLineWrap(true);
		jTextArea.setBackground(Color.YELLOW);
		jTextArea.setEditable(false);
//		jTextPane.setBackground(new Color(255,0,0,0));
		panel.add(jTextArea);
		
		gbc.gridy = row++;
		content.add(panel, gbc);
		content.revalidate();
		
//		Document document = chatBoardArea.getDocument();
//		try {
//			document.insertString(document.getLength(), message, null);
//			document.insertString(document.getLength(), System.getProperty("line.separator"), null);
//		}catch (Exception e) {
//			System.out.println("Printing chat failed!");
//		}
		
		chatBoardPane.getVerticalScrollBar().setValue(chatBoardPane.getVerticalScrollBar().getMaximum());
	}
	
	public void emojiOnChat(String id, String emoji, String time) {
		
		JTextPane tempPane = new JTextPane();
		Document document = tempPane.getDocument();
		try {
			ImageIcon imgicon = new ImageIcon(emoji);
			imgicon = new ImageIcon(imgicon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
			document.insertString(document.getLength(), id + " : ", null);
			tempPane.setCaretPosition(document.getLength());
			tempPane.insertIcon(imgicon);
			document.insertString(document.getLength(), " / "+time, null);
			document.insertString(document.getLength(), System.getProperty("line.separator"), null);
		}catch (Exception e) {
			System.out.println("Printing chat failed!");
		}
		tempPane.setBackground(Color.YELLOW);
		gbc.gridy = row++;
		content.add(tempPane, gbc);
		content.revalidate();
		
		chatBoardPane.getVerticalScrollBar().setValue(chatBoardPane.getVerticalScrollBar().getMaximum());
	}
	
	public void actionPerformed(ActionEvent ae) {
		String message = chatTextField.getText();
		if(ae.getSource()==logout) {
			//myClient.send some
			myClient.passChatReq(3, message);
		}else if(ae.getSource()==emoji) {
			//open emoji frame
			new emojiFrame();
		}else if (message.strip() == "" || message == null) {
			
		}else if(ae.getSource()==sendChatButton) {
			//myClient.send some
			myClient.passChatReq(1, message);
		}else if(ae.getSource()==idChange) {
			//myClient.send some
			myClient.passChatReq(4, message);
		}
		chatTextField.setText("");
	}
	
	public class emojiFrame extends JFrame implements MouseListener{
		public void setGUI() {
			this.setTitle("Chat");
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			
			ImageIcon icon = new ImageIcon("src/emoji.png");
			JPanel background = new JPanel() {
				public void paintComponent(Graphics g) {
					g.drawImage(icon.getImage(),0,0,null);
					setOpaque(false);
					super.paintComponent(g);
				}
			};
			background.setPreferredSize(new Dimension(700,700));
			setContentPane(background);
			background.addMouseListener(this);
			this.pack();
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			this.setTitle("Emoji");
			this.setVisible(true);
		}
		public emojiFrame() {
			// TODO Auto-generated constructor stub
			this.setGUI();
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			int term = 700/4;
			int x, y;
			for(x=0;x*term < e.getPoint().x;x++) {}
			for(y=0;y*term < e.getPoint().y;y++) {}
			y--;
			int key = x + y * 4;
			
			emojiSend(key);
			this.dispose();
		}
		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
	}//emoji frame
	
	public void emojiSend(int key) {
		String message = "src/"+Integer.toString(key)+".png";
		myClient.passChatReq(2, message);
	}
	
	public JTextPane getChatArea() {
		return this.chatBoardArea;
	}
	
	public JTextArea getUserArea() {
		return this.userBoardArea;
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			sendChatButton.doClick();
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
