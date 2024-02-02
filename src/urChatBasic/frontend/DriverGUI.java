package urChatBasic.frontend;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import urChatBasic.backend.LookAndFeelLoader;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.backend.utils.URUncaughtExceptionHandler;
import urChatBasic.base.Constants;
import static urChatBasic.base.Constants.LOGGER;

public class DriverGUI
{
    public static UserGUI gui = null;
    public static JFrame frame = null;
    private static ImageIcon img;
    public static ClassLoader contextClassLoader;

    public static void main(String[] args) throws IOException
    {
        Constants.init();
        try
        {
            URL imgPath = new URL(Constants.IMAGES_DIR + "urChat Icon.png");

            img = new ImageIcon(imgPath);
        } catch (Exception e)
        {
            Constants.LOGGER.info( "No Icon found.");
        }

        Constants.LOGGER.info( "Starting up..");

        initLAFLoader();

        // Fixes #92
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                frame = new JFrame("urChat");

                // This will load the default profile
                gui = createGUI(Optional.empty());

                startGUI();
            }
        });
    }

    public static void initLAFLoader() throws IOException
    {
        LookAndFeelLoader lafLoader = new LookAndFeelLoader(Thread.currentThread().getContextClassLoader());
        contextClassLoader = lafLoader.cl;
        Thread.currentThread().setContextClassLoader(contextClassLoader);
        Thread.currentThread().setUncaughtExceptionHandler(new URUncaughtExceptionHandler());
    }

    final public static String getMemoryReport()
    {
        final Runtime r = Runtime.getRuntime();
        final long mb = 1024 * 1024;
        return "max heap size = " + (r.maxMemory() / (mb)) + " MB; current heap size = " + (r.totalMemory() / (mb))
                + " MB; space left in heap = " + (r.freeMemory() / (mb)) + " MB";
    }

    public static UserGUI createGUI(Optional<String> profileName)
    {
        return new UserGUI(profileName);
    }

    public static void startGUI()
    {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(gui);
        frame.pack();


        if (img != null)
            frame.setIconImage(img.getImage());

        SwingUtilities.invokeLater(gui);

        LOGGER.info( "Started");

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
                URProfilesUtil.cleanUpSettings();
                if (!gui.isCreatedServersEmpty())
                    gui.sendGlobalMessage("/quit Goodbye cruel world", "Server");
            }


        });
    }
}
