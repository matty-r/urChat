package urChatBasic.frontend;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.Level;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import urChatBasic.backend.LookAndFeelLoader;
import urChatBasic.base.Constants;

public class DriverGUI
{
    public static UserGUI gui = null;
    public static JFrame frame = null;
    private static ImageIcon img;
    public static ClassLoader contextClassLoader;

    public static void main(String[] args) throws IOException
    {

        Constants.init();
        URL imgPath = new URL(Constants.RESOURCES_DIR + "urChat Icon.png");

        img = new ImageIcon(imgPath);

        Constants.LOGGER.log(Level.INFO, "Starting up..");

        LookAndFeelLoader lafLoader = new LookAndFeelLoader(Thread.currentThread().getContextClassLoader());
        contextClassLoader = lafLoader.cl;
        Thread.currentThread().setContextClassLoader(contextClassLoader);

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
        frame = new JFrame("urChat (" + Constants.UR_VERSION + ")");
        gui = new UserGUI();
    }

    public static void startGUI()
    {

        // ClassLoader mainLoader = Thread.currentThread().getContextClassLoader();

        // mainLoader = new JoinClassLoader(mainLoader, UCAuthTypeComboBox.class.getClassLoader(),
        // DnDTabbedPane.class.getClassLoader());
        // Thread.currentThread().setContextClassLoader(mainLoader);
        // guiThread.setContextClassLoader(threadLoader);

        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(gui);
        frame.pack();


        if (img != null)
            frame.setIconImage(img.getImage());

        // Thread guiThread = new Thread(gui);
        // guiThread.setContextClassLoader(contextClassLoader);
        // guiThread.start();

        SwingUtilities.invokeLater(gui);

        Constants.LOGGER.log(Level.INFO, "Started");

        frame.setVisible(true);

        frame.addWindowFocusListener(new WindowFocusListener()
        {
            static final int REFOCUS_BUFFER_MS = 3000;
            long lostFocusTime;

            @Override
            public void windowLostFocus(WindowEvent e)
            {
                lostFocusTime = System.currentTimeMillis();
                gui.lostFocus();
            }

            @Override
            public void windowGainedFocus(WindowEvent e)
            {
                // Prevent losing focus and triggering the regainedFocus function too quickly
                // pretty much only a problem when debugging the code and we hit a breakpoint
                // which steals focus to the IDE
                if (System.currentTimeMillis() > lostFocusTime + REFOCUS_BUFFER_MS)
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
