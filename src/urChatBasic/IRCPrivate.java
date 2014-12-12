package urChatBasic;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class IRCPrivate extends JPanel{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7861645386733494089L;
	  ////////////////
	 //GUI ELEMENTS//
	////////////////
	//Icons
	public ImageIcon icon;
	//Private Properties

	//Private Text Area
	private JTextPane privateTextArea = new JTextPane();
	private JScrollPane privateTextScroll = new JScrollPane(privateTextArea);
	public JTextField privateTextBox = new JTextField();
	private String name; 
	
	private UserGUI gui = DriverGUI.gui;
	private IRCServer myServer;
	
	
	public IRCPrivate(IRCServer serverName,IRCUser user){
		this.myServer = serverName;
		this.setLayout(new BorderLayout());
		this.add(privateTextScroll, BorderLayout.CENTER);
		this.add(privateTextBox, BorderLayout.PAGE_END);
		privateTextBox.addActionListener(new sendPrivateText());
		privateTextArea.setFont(gui.getFont());
		setName(user.getName());
		
		Image tempIcon = null;
		try {
			tempIcon = ImageIO.read(new File("Resources\\User.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		icon = new ImageIcon(tempIcon);
	}
	@Override
	public void setName(String userName){	
		this.name = userName;
	}
	
	@Override
	public String getName(){
		return this.name;
	}

	private class sendPrivateText implements ActionListener
	   {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(!privateTextBox.getText().trim().isEmpty()){
					 String messagePrefix = "";
					if(!privateTextBox.getText().startsWith("/"))
						messagePrefix = "/msg "+getName()+" ";
				myServer.sendClientText(messagePrefix+privateTextBox.getText(),getName());
				}
				privateTextBox.setText("");
			}
	   }
	
	public void printText(Boolean dateTime, String line){
		StyledDocument doc = (StyledDocument) privateTextArea.getDocument();
		Style style = doc.addStyle("StyleName", null);
	
	    //StyleConstants.setItalic(style, true);
		DateFormat chatDateFormat = new SimpleDateFormat("HHmm");
		Date chatDate = new Date();
		
		if(dateTime)
			line = "["+chatDateFormat.format(chatDate)+"] " + line;
	    try {
			doc.insertString(doc.getLength(), line+"\n", style);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	    privateTextArea.setCaretPosition(privateTextArea.getDocument().getLength());
	}
	

	public String getServer() {
		return myServer.getName();
	}
	
}
