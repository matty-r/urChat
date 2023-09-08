package urChatBasic.frontend;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.net.URL;
// import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.UIManager;
// import javax.swing.UIManager.LookAndFeelInfo;
// import urChatBasic.backend.LookAndFeelLoader;
import urChatBasic.base.Constants;
// import urChatBasic.frontend.utils.UIManagerDefaults;

public class DriverGUI
{
    public static UserGUI gui = null;
    public static JFrame frame = null;
    private static ImageIcon img;

    public static void main(String[] args) throws IOException
    {

        Constants.init();
        URL imgPath = new URL(Constants.RESOURCES_DIR + "urChat Icon.png");

        img = new ImageIcon(imgPath);
        boolean flatLafAvailable = false;

        try
        {
            // // TODO
            // System.out.println("TODO: LookAndFeelLoader not yet implemented completely");
            // LookAndFeelLoader lafLoader = new LookAndFeelLoader(Thread.currentThread().getContextClassLoader());
            // Thread.currentThread().setContextClassLoader(lafLoader.newClassLoader);
            // Properties lafProps = lafLoader.loadedProps;


            // // Load all classes in the JAR?
            // for (Object lafName : lafLoader.lafClasses.keySet()) {
            //     // Whats the best way to load the needed classes correctly?
            //     try{
            //         // ((Class) lafLoader.lafClasses.get(lafName)).getClassLoader().loadClass((String) lafName);
            //         Thread.currentThread().getContextClassLoader().loadClass((String) lafName);
            //         // Thread.currentThread().getContextClassLoader().loadClass((String) lafName);
            //         Class.forName((String) lafName, true, ((Class) lafLoader.lafClasses.get(lafName)).getClassLoader());
            //     } catch (NoClassDefFoundError | Exception classDef)
            //     {
            //         System.out.println(classDef);
            //     }
            // }


            // // 'Construct' and set LAF
            // try{

            //         // UIManager.setLookAndFeel("FlatDarkLaf");
            //         for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            //             System.out.println(info.getName());
            //             if ("FlatMacLightLaf".equals(info.getName())) {
            //                 UIManager.setLookAndFeel(info.getClassName());
            //             }
            //         }
            //         flatLafAvailable = true;
            // } catch(Exception  e) {
            //     throw e;
            // }
            // Figure out choosing and loading others later
            // break;

        } catch (Exception e)
        {
            Constants.LOGGER.log(Level.WARNING, "Failed to set FlatLAF! " + e.getLocalizedMessage());
        } finally {
            if(!flatLafAvailable)
            {
                try
                {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e)
                {
                    Constants.LOGGER.log(Level.WARNING, "Failed to setLookAndFeel! " + e.getLocalizedMessage());
                }
            }
        }

        // UIDefaults defaults = UIManager.getLookAndFeelDefaults();

        // Starts doing some Swing stuff
        // UIManagerDefaults thisng = new UIManagerDefaults();
        // thisng.createAndShowGUI();
        Constants.LOGGER.log(Level.INFO, "Starting up..");
        createGUI();

        startGUI();
    }


    final public static String getMemoryReport()
    {
        final Runtime r = Runtime.getRuntime();
        final long mb = 1024 * 1024;
        return "max heap size = " + (r.maxMemory() / (mb)) + " MB; current heap size = " + (r.totalMemory() / (mb))
                + " MB; space left in heap = " + (r.freeMemory() / (mb)) + " MB";
    }

    public static void createGUI()
    {
        frame = new JFrame("urChat");
        gui = new UserGUI();
    }

    public static void startGUI()
    {

        // ClassLoader mainLoader = Thread.currentThread().getContextClassLoader();

        // mainLoader = new JoinClassLoader(mainLoader, UCAuthTypeComboBox.class.getClassLoader(), DnDTabbedPane.class.getClassLoader());
        // Thread.currentThread().setContextClassLoader(mainLoader);
        // guiThread.setContextClassLoader(threadLoader);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(gui);
        frame.pack();

        if(img != null)
            frame.setIconImage(img.getImage());

        Thread guiThread = new Thread(gui);
        guiThread.start();

        Constants.LOGGER.log(Level.INFO, "Started");

        frame.setVisible(true);

        frame.addWindowFocusListener(new WindowFocusListener() {

            @Override
            public void windowLostFocus(WindowEvent e) {
                gui.lostFocus();
            }

            @Override
            public void windowGainedFocus(WindowEvent e) {
                gui.regainedFocus();
            }
        });

        frame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                gui.setClientSettings();
                gui.cleanUpSettings();
                if (!gui.isCreatedServersEmpty())
                    gui.sendGlobalMessage("/quit Goodbye cruel world", "Server");
                for (Handler tempHandler : Constants.LOGGER.getHandlers())
                    tempHandler.close();
            }


        });
    }
}
