package urChatBasic.frontend.components;

import javax.swing.JLabel;
import javax.swing.JPanel;
import urChatBasic.base.Constants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class URVersionLabel extends JPanel
{
    // We are using this label as the default foreground and background colours!

    private JLabel versionLabel = new JLabel(Constants.UR_VERSION);

    public URVersionLabel (JPanel parentPanel)
    {
        setVersion();
        setLayout(new BorderLayout());
        versionLabel.setText(Constants.UR_VERSION);
        versionLabel.setToolTipText(Constants.UR_VERSION);
        // Add components to the panel
        add(versionLabel, BorderLayout.SOUTH);
    }

    public static void setVersion ()
    {
        String gitFolderPath = findGitFolder();
        if (gitFolderPath != null)
        {
            try
            {
                String newVersionString = parseVersionString(gitFolderPath);

                if (null != newVersionString)
                {
                    Constants.UR_VERSION = newVersionString;
                }
            } catch (IOException $ex)
            {
                Constants.LOGGER.info( "Unable to determine .git folder. Not setting version string.", $ex);
            }
        }
    }

    public Map<String, Color> getColours ()
    {
        Map<String, Color> colours = new HashMap<>();

        colours.put(Constants.KEY_FONT_FOREGROUND, getForeground());
        colours.put(Constants.KEY_FONT_BACKGROUND, getBackground());

        return colours;
    }

    private static String findGitFolder ()
    {
        // Implement logic to find the .git folder in the project directory or its parents
        // For simplicity, let's assume it's in the current directory
        File gitFolder = new File(".git");
        if (gitFolder.exists() && gitFolder.isDirectory())
        {
            return gitFolder.getAbsolutePath();
        }
        return null; // .git folder not found
    }

    private static String parseVersionString (String gitFolderPath) throws IOException
    {
        String headFilePath = gitFolderPath + File.separator + "HEAD";
        String newVersionString = Constants.UR_VERSION;

        BufferedReader reader = new BufferedReader(new FileReader(headFilePath));
        String line = reader.readLine();

        if (line != null && line.startsWith("ref: "))
        {

            newVersionString = line.split("/")[line.split("/").length - 1];
        } else
        {
            newVersionString = null; // Failed to parse ref path
        }

        String origHeadPath = gitFolderPath + File.separator + "ORIG_HEAD";
        if (Files.exists(Paths.get(origHeadPath)))
        {
            reader = new BufferedReader(new FileReader(origHeadPath));
            line = reader.readLine();
            if (line != null)
            {
                newVersionString += "-" + line.substring(0, 6);
            } else
            {
                newVersionString = null;
            }
        }

        reader.close();
        return newVersionString;
    }
}
