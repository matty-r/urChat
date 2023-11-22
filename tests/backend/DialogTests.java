package backend;

import urChatBasic.frontend.dialogs.MessageDialog;
import urChatBasic.frontend.dialogs.YesNoDialog;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import org.testng.Reporter;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

public class DialogTests
{
    protected boolean automateTests = true;

    protected void utilFunction()
    {
        // to test calling another function
    }

    @Test
    public void testMessageDialog()
    {
        // effectively final
        AtomicBoolean passedTest = new AtomicBoolean(false);

        MessageDialog testDialog =
                new MessageDialog("Test dialog message here", "Test dialog title", JOptionPane.ERROR_MESSAGE,
                e -> {
                    passedTest.set(true);
                });

        if (automateTests)
        {
            JButton closeButton = testDialog.getCloseButton();
            closeButton.doClick();
        } else
        {
            testDialog.setVisible(true);
        }


        assertTrue("Didn't press the cancel button", passedTest.get());
    }

    @Test(groups = {"Test #001"})
    public void testYesNoDialogWithBoolean()
    {
        AtomicBoolean cancelClicked = new AtomicBoolean(false);

        // Create an instance of ChoiceDialog with an anonymous function for cancel action
        YesNoDialog testDialog = new YesNoDialog("Click 'No' in this dialog", "Test dialog title",
                JOptionPane.QUESTION_MESSAGE, e -> cancelClicked.set(e.getActionCommand().equalsIgnoreCase("No")));

        if (automateTests)
        {
            // Simulate clicking the No button
            JButton noButton = testDialog.getNoButton();
            noButton.doClick();
        } else
        {
            testDialog.setVisible(true);
        }

        // Assert that the cancel action was executed
        assertTrue("No button wasn't pressed.", cancelClicked.get());

        Reporter.log("Other reporting information for this test");
    }

    @Test
    public void testYesNoDialogWithActionListener()
    {

        Reporter.log("Custom reporting log information here, defined in the method");

        // effectively final
        AtomicBoolean passedTest = new AtomicBoolean(false);

        // Create an instance of ChoiceDialog with an anonymous function for cancel action
        YesNoDialog testDialog = new YesNoDialog("Click any button to execute the function", "Test dialog title",
                JOptionPane.QUESTION_MESSAGE, e -> {
                    passedTest.set(true);
                    assertTrue(passedTest.get());
                });

        if (automateTests)
        {
            // Simulate clicking the cancel button
            JButton cancelButton = testDialog.getNoButton();
            cancelButton.doClick();
        } else
        {
            testDialog.setVisible(true);
        }

        assertTrue("Failed because Yes or No wasn't pressed", passedTest.get());
    }
}
