package urChatBasic.frontend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.text.StyledDocument;

import urChatBasic.base.Constants;
import urChatBasic.base.IRCRoomBase;

public class IRCPrivate extends IRCRoomBase
{
    /**
     *
     */
    private static final long serialVersionUID = -7861645386733494089L;
    ////////////////
    // GUI ELEMENTS//
    ////////////////
    // Icons
    public ImageIcon icon;
    // Private Properties

    private JTextPane privateTextArea = new JTextPane();
    private JScrollPane privateTextScroll = new JScrollPane(privateTextArea);
    public JTextField privateTextBox = new JTextField();
    private String name;

    private UserGUI gui = DriverGUI.gui;
    private IRCServer myServer;

    // IRCActions stuff
    private boolean wantsAttention = false;
    private Timer wantsAttentionTimer = new Timer(0, new FlashTab());
    private Color originalColor;


    public IRCPrivate(IRCServer serverName, IRCUser user)
    {
        this.myServer = serverName;
        this.setLayout(new BorderLayout());
        this.add(privateTextScroll, BorderLayout.CENTER);
        this.add(privateTextBox, BorderLayout.PAGE_END);
        privateTextBox.addActionListener(new sendPrivateText());
        privateTextArea.setEditable(false);
        privateTextArea.setFont(gui.getFont());
        setName(user.getName());

        Image tempIcon = null;
        try
        {
            tempIcon = ImageIO.read(new File(Constants.RESOURCES_DIR + "User.png"));
        } catch (IOException e)
        {
            Constants.LOGGER.log(Level.SEVERE, "FAILED to load User.png! " + e.getLocalizedMessage());
        }
        icon = new ImageIcon(tempIcon);
    }

    @Override
    public void setName(String userName)
    {
        this.name = userName;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    private class sendPrivateText implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent arg0)
        {
            if (!privateTextBox.getText().trim().isEmpty())
            {
                String messagePrefix = "";
                if (!privateTextBox.getText().startsWith("/"))
                    messagePrefix = "/msg " + getName() + " ";
                myServer.sendClientText(messagePrefix + privateTextBox.getText(), getName());
            }
            privateTextBox.setText("");
        }
    }

    public void printText(Boolean dateTime, String fromUser, String line)
    {
        StyledDocument doc = (StyledDocument) privateTextArea.getDocument();

        DateFormat chatDateFormat = new SimpleDateFormat("HHmm");
        Date chatDate = new Date();
        String timeLine = "";

        if (dateTime)
            timeLine = "[" + chatDateFormat.format(chatDate) + "]";

        LineFormatter newLine = new LineFormatter(this.getFont(), myServer.getNick());
        newLine.formattedDocument(doc, timeLine, fromUser, line);

        this.callForAttention();

        privateTextArea.setCaretPosition(privateTextArea.getDocument().getLength());
    }


    public String getServer()
    {
        return myServer.getName();
    }

    private class FlashTab implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            Component selectedComponent = gui.tabbedPane.getSelectedComponent();
            int tabIndex = gui.tabbedPane.indexOfComponent(IRCPrivate.this);

            if (IRCPrivate.this.wantsAttention() && selectedComponent != IRCPrivate.this)
            {
                privateTextBox.requestFocus();

                if (gui.tabbedPane.getBackgroundAt(tabIndex) == Color.red)
                {

                    gui.tabbedPane.setBackgroundAt(tabIndex, IRCPrivate.this.originalColor);
                } else
                {
                    gui.tabbedPane.setBackgroundAt(tabIndex, Color.red);
                }

                repaint();
            } else
            {
                gui.tabbedPane.setBackgroundAt(tabIndex, IRCPrivate.this.originalColor);
                wantsAttentionTimer.stop();
            }
        }
    }

    @Override
    public void callForAttention()
    {
        wantsAttentionTimer.setDelay(1000);
        wantsAttention = true;


        for(int i = 0; i < gui.tabbedPane.getTabCount(); i++)
        {
            if(gui.tabbedPane.getComponentAt(i) == IRCPrivate.this)
            {
                IRCPrivate.this.originalColor = gui.tabbedPane.getBackgroundAt(i);
                break;
            }
        }

        if (!(wantsAttentionTimer.isRunning()))
            wantsAttentionTimer.start();
    }

    @Override
    public boolean wantsAttention()
    {
        return wantsAttention;
    }

}
