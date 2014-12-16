package urChatBasic;

import java.awt.Color;
import java.awt.Font;
import javax.swing.text.*;

public class LineFormatter {
	private String myNick;
	private Font myFont;
	
	public LineFormatter(Font myFont,String myNick){
		this.myNick = myNick;
		this.myFont = myFont;
	}
	
	public SimpleAttributeSet standardStyle(){
		
		SimpleAttributeSet tempStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(tempStyle, Color.BLACK);
		StyleConstants.setFontFamily(tempStyle, myFont.getFamily());
		StyleConstants.setFontSize(tempStyle, myFont.getSize());
		
		return tempStyle;
	}
	
	
	public SimpleAttributeSet lowStyle(){
		
		SimpleAttributeSet tempStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(tempStyle, Color.LIGHT_GRAY);
		StyleConstants.setFontFamily(tempStyle, myFont.getFamily());
		StyleConstants.setFontSize(tempStyle, myFont.getSize());
		
		return tempStyle;
	}
	
	public SimpleAttributeSet mediumStyle(){
		
		SimpleAttributeSet tempStyle = new SimpleAttributeSet();
		StyleConstants.setBackground(tempStyle, Color.YELLOW);
		StyleConstants.setFontFamily(tempStyle, myFont.getFamily());
		StyleConstants.setFontSize(tempStyle, myFont.getSize());
		
		return tempStyle;
	}
	
	public SimpleAttributeSet highStyle(){
		
		SimpleAttributeSet tempStyle = new SimpleAttributeSet();
		StyleConstants.setBackground(tempStyle, Color.RED);
		StyleConstants.setItalic(tempStyle, true);
		StyleConstants.setFontFamily(tempStyle, myFont.getFamily());
		StyleConstants.setFontSize(tempStyle, myFont.getSize());
		
		return tempStyle;
	}
	
	public SimpleAttributeSet myStyle(){
		
		SimpleAttributeSet tempStyle = new SimpleAttributeSet();
		StyleConstants.setBold(tempStyle, true);
		StyleConstants.setFontFamily(tempStyle, myFont.getFamily());
		StyleConstants.setFontSize(tempStyle, myFont.getSize());
		
		return tempStyle;
	}
	
	public void formattedDocument(StyledDocument doc,String timeLine,String fromUser,String line){
		SimpleAttributeSet timeStyle = lowStyle();
		SimpleAttributeSet nameStyle = standardStyle();
		SimpleAttributeSet lineStyle =  standardStyle();
    	

			if(myNick.equals(fromUser)){
					nameStyle = this.myStyle();
			} else {
				if(line.indexOf(myNick) > -1)
					nameStyle = highStyle();
	        } 
	        	if(fromUser.equals(IRCChannel.EVENT_USER)){
	        		nameStyle = lowStyle();
	        		lineStyle = lowStyle();
	        	}
	        
	        	
			
		try {
		doc.insertString(doc.getLength(), timeLine, timeStyle);
		doc.insertString(doc.getLength(), " <", lineStyle);
    	doc.insertString(doc.getLength(), fromUser, nameStyle);
    	doc.insertString(doc.getLength(), "> ", lineStyle);
    	doc.insertString(doc.getLength(), line+"\n", lineStyle);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}