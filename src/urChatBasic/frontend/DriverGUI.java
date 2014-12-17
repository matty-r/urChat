package urChatBasic.frontend;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.UIManager;

public class DriverGUI
{
	public static UserGUI gui = null;
	
	public static void main(String[] args) throws IOException{
		try {
		    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		    //TODO something meaningful
		}
		
		DriverGUI driver = new DriverGUI();	
		driver.startGUI();
	}
	
	
	final public static String getMemoryReport() {
	    final Runtime r = Runtime.getRuntime();
	    final long mb = 1024 * 1024;
	    return "max heap size = " + (r.maxMemory() / (mb)) + " MB; current heap size = " + (r.totalMemory() / (mb)) + " MB; space left in heap = " + (r.freeMemory() / (mb)) + " MB";
	}
	
	public void startGUI(){
		gui = new UserGUI();
		new Thread(gui).start();
		
		
		JFrame frame = new JFrame ("urChat");
		
		frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(gui);
		frame.pack();
		frame.setVisible(true); 
		frame.addWindowListener(new WindowAdapter() {
			  public void windowClosing(WindowEvent e) {
							if(!gui.isCreatedServersEmpty())
								gui.sendGlobalMessage("/quit Goodbye cruel world", "Server");
				  }
				});
					

		
	}
}
