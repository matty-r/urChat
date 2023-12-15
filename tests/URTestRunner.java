import org.testng.TestNG;
import backend.MessageHandlerTests;
import frontend.DialogTests;
import frontend.UserGUITests;
import java.util.ArrayList;
import java.util.List;
import org.testng.TestListenerAdapter;

public class URTestRunner {

    public static void main(String[] args) {
        TestNG testNG = new TestNG();

        // Define your test classes
        List<Class> classes = new ArrayList<>();
        classes.add(DialogTests.class);
        classes.add(MessageHandlerTests.class);
        classes.add(UserGUITests.class);

        // Set the test classes to TestNG
        testNG.setTestClasses(classes.toArray(new Class[0]));

        // Run TestNG
        TestListenerAdapter listener = new TestListenerAdapter();
        testNG.addListener(listener);

        testNG.run();

        System.exit(0);
    }
}