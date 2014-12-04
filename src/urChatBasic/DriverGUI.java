package urChatBasic;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;

public class DriverGUI
{
	public static Connection chatSession = null;
	public static UserGUI gui = null;
	
	public static void main(String[] args) throws IOException{
		DriverGUI driver = new DriverGUI();	
		driver.startGUI();
	}
	
	public void startGUI(){
		gui = new UserGUI();
		new Thread(gui).start();
 
		JFrame frame = new JFrame ("urChat: Last Updated 04 DEC 14");
		
		
		frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(gui);
		frame.pack();
		frame.setVisible(true); 
		frame.addWindowListener(new WindowAdapter() {
			  public void windowClosing(WindowEvent e) {
							try {
								if(!gui.isCreatedChannelsEmpty())
									Connection.sendClientText("/quit Goodbye cruel world", "Server");
							} catch (IOException x) {
								// TODO Auto-generated catch block
								x.printStackTrace();
							}
				  }
				});
					

		
	}

	public static void startConnection(){
		chatSession =  new Connection();
	}
}
