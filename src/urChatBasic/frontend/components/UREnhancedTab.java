package urChatBasic.frontend.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

public class UREnhancedTab extends JPanel
{
    private final JTabbedPane pane;
    protected JLabel label;
    private Icon tabIcon;
    private TabBadge badge;

    public UREnhancedTab (final JTabbedPane pane, String title, Icon tabIcon)
    {
        super(null); // Set null layout
        if (pane == null)
        {
            throw new NullPointerException("TabbedPane is null");
        }
        this.pane = pane;
        setOpaque(false);

        // make JLabel read titles from JTabbedPane
        this.tabIcon = new BadgeIcon(null);
        label = new JLabel(title, this.tabIcon, 0);

        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

        FontMetrics met = label.getFontMetrics(label.getFont());
        int height = met.getHeight();
        int width = met.stringWidth(label.getText()) + (tabIcon != null ? tabIcon.getIconWidth() : 0);

        setPreferredSize(new Dimension(width + 20, height));

        // badge = new TabBadge();
        // badge.setBounds(0, 0, 20, 20); // Adjust position as needed
        // add(badge);

        label.setBounds(0, 0, width + 20, height); // Set bounds for the label
        add(label);

    }


    private boolean showBadge ()
    {
        return false;
    }

    class BadgeIcon implements Icon
    {
        private Icon originalIcon;

        public BadgeIcon (Icon originalIcon)
        {
            this.originalIcon = originalIcon;
        }

        @Override
        public void paintIcon (Component c, Graphics g, int x, int y)
        {
            Graphics2D g2 = (Graphics2D) g.create();

            // Enable anti-aliasing
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.translate(x, y);
            // Draw notification badge (colored circle and number)
            int badgeDiameter = 16;
            int badgeX = 0;
            int badgeY = 0;
            g2.setColor(Color.RED);
            g2.fillRect(badgeX, badgeY, badgeDiameter, badgeDiameter);
            // g2.fillRect(y, badgeDiameter, badgeX, badgeY);
            g2.setColor(Color.WHITE);
            g2.drawString("99", badgeX, badgeY + 12);
        }

        @Override
        public int getIconWidth ()
        {
            return 16;
        }

        @Override
        public int getIconHeight ()
        {
            return 16;
        }
    }

    private class TabBadge extends JButton implements ActionListener
    {

        public TabBadge ()
        {
            int size = 17;
            setPreferredSize(new Dimension(size, size));
            setToolTipText("close this tab");
            // Make the button looks the same for all Laf's
            setUI(new BasicButtonUI());
            // Make it transparent
            setContentAreaFilled(false);
            // No need to be focusable
            setFocusable(false);
            // setBorder(BorderFactory.createEtchedBorder());
            // setBorderPainted(false);
            // Making nice rollover effect
            // we use the same listener for all buttons
            // addMouseListener(buttonMouseListener);
            // setRolloverEnabled(true);
            // Close the proper tab by clicking the button
            // addActionListener(this);
        }

        public void actionPerformed (ActionEvent e)
        {
            int i = pane.indexOfTabComponent(UREnhancedTab.this);
            if (i != -1)
            {
                pane.remove(i);
            }
        }

        // we don't want to update UI for this button
        public void updateUI ()
        {}

        @Override
        protected void paintComponent (Graphics g)
        {
            super.paintComponent(g);

            // Draw notification badge (colored circle and number)
            int badgeDiameter = 14;
            int badgeX = 0;
            int badgeY = 0;
            g.setColor(Color.RED);
            g.fillOval(badgeX, badgeY, badgeDiameter, badgeDiameter);
            g.setColor(Color.WHITE);
            g.drawString("9+", badgeX + 4, badgeY + 14);
        }

        @Override
        public Dimension getPreferredSize ()
        {
            return new Dimension(20, 20); // Adjust size as needed
        }
    }

    private final static MouseListener buttonMouseListener = new MouseAdapter()
    {
        public void mouseEntered (MouseEvent e)
        {
            Component component = e.getComponent();
            if (component instanceof AbstractButton)
            {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        public void mouseExited (MouseEvent e)
        {
            Component component = e.getComponent();
            if (component instanceof AbstractButton)
            {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };
}
