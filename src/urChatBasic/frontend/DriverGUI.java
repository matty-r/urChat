package urChatBasic.frontend;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;

import urChatBasic.base.Constants;

public class DriverGUI
{
	public static UserGUI gui = null;
	public static JFrame frame = null;
	ImageIcon img = new ImageIcon(Constants.RESOURCES_DIR+"urChat Icon.png");
	
	public static void main(String[] args) throws IOException{
		
		Constants.init();
		try {
		    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			Constants.LOGGER.log(Level.WARNING, "Failed to setLookAndFeel! " + e.getLocalizedMessage());
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
		frame = new JFrame ("urChat");
		
		gui = new UserGUI();
		new Thread(gui).start();
		
		
		
		frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(gui);
		frame.pack();
		frame.setIconImage(img.getImage());
		frame.setVisible(true); 
		frame.addWindowListener(new WindowAdapter() {
			  public void windowClosing(WindowEvent e) {
				  			gui.setClientSettings();
							if(!gui.isCreatedServersEmpty())
								gui.sendGlobalMessage("/quit Goodbye cruel world", "Server");
							for(Handler tempHandler:Constants.LOGGER.getHandlers())
								tempHandler.close();
				  }
				});
					

		
	}
}
