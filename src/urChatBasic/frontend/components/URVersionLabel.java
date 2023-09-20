package urChatBasic.frontend.components;

import javax.swing.JLabel;
import javax.swing.JPanel;
import urChatBasic.base.Constants;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class URVersionLabel extends JPanel
{
    private JLabel versionLabel = new JLabel("Version: " + Constants.UR_VERSION);

    public URVersionLabel(JPanel parentPanel)
    {
        setVersion();
        setBackground(parentPanel.getBackground());
        setLayout(new GridLayout(3, 1));

        versionLabel.setText("Version: " + Constants.UR_VERSION);
        // Add components to the panel
        add(versionLabel);
    }

    public static void setVersion() {
        String gitFolderPath = findGitFolder();
        if (gitFolderPath != null) {
            String headFilePath = gitFolderPath + "/HEAD";
            String refPath = parseRefPath(headFilePath);
            Constants.UR_VERSION = refPath;
        }
    }

    private static String findGitFolder() {
        // Implement logic to find the .git folder in the project directory or its parents
        // For simplicity, let's assume it's in the current directory
        File gitFolder = new File(".git");
        if (gitFolder.exists() && gitFolder.isDirectory()) {
            return gitFolder.getAbsolutePath();
        }
        return null;  // .git folder not found
    }

    private static String parseRefPath(String headFilePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(headFilePath))) {
            String line = reader.readLine();
            if (line != null && line.startsWith("ref: ")) {

                return line.split("/")[line.split("/").length - 1];
            }
        } catch (IOException e) {
            e.printStackTrace();  // Handle the exception according to your requirements
        }
        return null;  // Failed to parse ref path
    }
}
