package urChatBasic;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

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

public class IRCServer extends JPanel implements Runnable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4685985875752613136L;
	  ////////////////
	 //GUI ELEMENTS//
	////////////////
	//Icons
	public ImageIcon icon;

	//Server Properties

	//Server Text Area
	private JTextPane serverTextArea = new JTextPane();
	private JScrollPane serverTextScroll = new JScrollPane(serverTextArea);
	public JTextField serverTextBox = new JTextField();
	private String name; 
	
	public IRCServer(String serverName){
		this.setLayout(new BorderLayout());
		this.add(serverTextScroll, BorderLayout.CENTER);
		this.add(serverTextBox, BorderLayout.PAGE_END);
		serverTextBox.addActionListener(new sendServerText());
		this.name = serverName;
		
		Image tempIcon = null;
		try {
			tempIcon = ImageIO.read(new File("Server.png"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		icon = new ImageIcon(tempIcon);
	}
	
	public void setName(String serverName){
		this.name = serverName;
	}
	
	public String getName(){
		return this.name;
	}

	private class sendServerText implements ActionListener
	   {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if(!serverTextBox.getText().trim().isEmpty())
						Connection.sendClientText(serverTextBox.getText(),getName());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				serverTextBox.setText("");
			}
	   }
	
	public void printText(String line){
		StyledDocument doc = (StyledDocument) serverTextArea.getDocument();
		Style style = doc.addStyle("StyleName", null);
	
	    StyleConstants.setItalic(style, true);
	
	    try {
			doc.insertString(doc.getLength(), line+"\n", style);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		serverTextArea.setCaretPosition(serverTextArea.getDocument().getLength());
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}
