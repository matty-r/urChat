package utils;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Handler;
import java.util.logging.Level;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.testng.Reporter;
import urChatBasic.backend.utils.URProfilesUtil;
import urChatBasic.base.Constants;
import urChatBasic.frontend.DriverGUI;
import urChatBasic.frontend.UserGUI;

public class TestDriverGUI extends DriverGUI
{
    final String testProfileName = "testingprofile" + (new SimpleDateFormat("yyMMddss")).format(new Date());
    final UserGUI testGUI;

    public String getTestProfileName ()
    {
        return testProfileName;
    }

    public TestDriverGUI () throws IOException
    {
        super();
        Reporter.log("Creating test gui", true);
        // TODO: We should just create a TestDriverGUI instead.
        DriverGUI.initLAFLoader();
        URProfilesUtil.createProfile(testProfileName);
        DriverGUI.createGUI(Optional.of(testProfileName));
        testGUI = DriverGUI.gui;
        testGUI.setupUserGUI();
        UserGUI.setTimeLineString(Constants.DEFAULT_TIME_STAMP_FORMAT);
        UserGUI.setNickFormatString(Constants.DEFAULT_NICK_FORMAT);
    }

    public static void startTestGUI()
    {
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(gui);
        frame.pack();

        SwingUtilities.invokeLater(gui);

        Constants.LOGGER.log(Level.INFO, "Started");

        frame.setVisible(false);

        frame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent e)
            {
                gui.setClientSettings();
                URProfilesUtil.cleanUpSettings();
                if (!gui.isCreatedServersEmpty())
                    gui.sendGlobalMessage("/quit Goodbye cruel world", "Server");
                for (Handler tempHandler : Constants.LOGGER.getHandlers())
                    tempHandler.close();
            }
        });
    }
}
