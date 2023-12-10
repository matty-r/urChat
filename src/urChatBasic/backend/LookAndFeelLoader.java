package urChatBasic.backend;

import java.io.File;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import urChatBasic.base.Constants;

public class LookAndFeelLoader {
    public URLClassLoader cl;

    public LookAndFeelLoader(ClassLoader parentLoader) throws IOException {
        try {
            File[] libFiles = new File(Constants.THEMES_DIR).listFiles();
            if (libFiles != null) {

                // Load JAR file URLs
                URL[] urls = new URL[libFiles.length];

                for (int i = 0; i < libFiles.length; i++) {
                    String pathToJar = libFiles[i].getAbsolutePath();
                    urls[i] = new URL("jar:file:" + pathToJar + "!/");
                }

                cl = URLClassLoader.newInstance(urls, parentLoader);

                UIManager.getLookAndFeelDefaults().put("ClassLoader", cl);

                for (File libFile : libFiles) {
                    // create the file as a jarfile
                    try (JarFile jarFile = new JarFile(libFile.getAbsolutePath())) {

                        Enumeration<JarEntry> e = jarFile.entries();

                        // iterate over each definition within the jarfile
                        while (e.hasMoreElements()) {
                            JarEntry je = e.nextElement();

                            // don't do anything if it's a directory
                            if (je.isDirectory()) {
                                continue;
                            }

                            // Don't do anything that isn't a class
                            if(!je.getName().endsWith(".class"))
                            {
                                continue;
                            }

                            // -6 because of .class
                            String className = je.getName().substring(0, je.getName().length() - 6);

                            String classShortName = className.replaceAll(".*/([^/]+)", "$1");

                            className = className.replace('/', '.');
                            Class c = cl.loadClass(className);

                            // We only want LAF classes
                            Class parentClass = LookAndFeel.class;

                            // Class is a LookAndFeel
                            if (parentClass.isAssignableFrom(c)) {
                                try {
                                    UIManager.installLookAndFeel(classShortName, className);
                                } catch (Exception installEx) {
                                    Constants.LOGGER.log(Level.WARNING, installEx.getMessage());
                                }
                            }
                        }
                    } catch (NoClassDefFoundError | Exception classEx) {
                        Constants.LOGGER.log(Level.WARNING, classEx.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            // Handle URL creation exception
            e.printStackTrace();
        }
        Constants.LOGGER.log(Level.INFO, "Done loading JARs! ");
    }
}
