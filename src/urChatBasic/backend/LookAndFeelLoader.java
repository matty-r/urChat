package urChatBasic.backend;

import java.io.File;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import urChatBasic.base.Constants;

public class LookAndFeelLoader<C extends LookAndFeel> {
    public HashMap<String, Constructor> foundLAFs = new HashMap<>();
    public HashMap<String, Class> lafClasses = new HashMap<>();
    public HashMap<String, String> foundProps = new HashMap<>();
    public final Properties loadedProps = new Properties();
    public ClassLoader newClassLoader;

    public LookAndFeelLoader(ClassLoader parentLoader) throws IOException {
        URLClassLoader cl = null;
        try {
            File[] libFiles = new File(Constants.THEMES_DIR).listFiles();
            if (libFiles != null) {
                URL[] urls = new URL[libFiles.length];
                for (int i = 0; i < libFiles.length; i++) {
                    String pathToJar = libFiles[i].getAbsolutePath();
                    urls[i] = new URL("jar:file:" + pathToJar + "!/");
                }
                cl = URLClassLoader.newInstance(urls, parentLoader);
                // Thread.currentThread().setContextClassLoader(cl);
                UIManager.getLookAndFeelDefaults().put("ClassLoader", cl);
                for (File libFile : libFiles) {
                    try (JarFile jarFile = new JarFile(libFile.getAbsolutePath())) {
                        Enumeration<JarEntry> e = jarFile.entries();
                        while (e.hasMoreElements()) {
                            JarEntry je = e.nextElement();
                            // System.out.println(je.getName());
                            if (je.isDirectory()) {
                                continue;
                            }

                            if(je.getName().toLowerCase().endsWith(".properties")) {
                                foundProps.put(je.getName().replaceAll(".*/([^/]+)\\.properties", "$1"),je.getName());
                                continue;
                            } else if(!je.getName().endsWith(".class"))
                            {
                                continue;
                            }

                            // -6 because of .class
                            String className = je.getName().substring(0, je.getName().length() - 6);
                            String classShortName = className.replaceAll(".*/([^/]+)", "$1");
                            className = className.replace('/', '.');
                            Class c = cl.loadClass(className);


                            for (String propName : foundProps.keySet()) {
                                if(propName.equals(classShortName))
                                {
                                    // FileInputStream in = new FileInputStream( "." + File.separator + "themes" + File.separator + foundProps.get(propName) );
                                    InputStream resourceStream = cl.getResourceAsStream(foundProps.get(propName));
                                    // in.close( );
                                    loadedProps.load(resourceStream);
                                    // resourceStream.close();
                                }
                            }
                            // Put all of the available classes in the HashMap for loading in DriverGUI
                            lafClasses.put(className, c);
                            // We only want LAF classes
                            Class parentClass = LookAndFeel.class;

                            // Class is a LookAndFeel
                            if (parentClass.isAssignableFrom(c)) {
                                Class<?> clazz = Class.forName(className, true, c.getClassLoader());
                                Class<? extends C> newClass = clazz.asSubclass(parentClass);
                                try {
                                    Constructor<? extends C> constructor = newClass.getConstructor();
                                    foundLAFs.put(className, constructor);
                                    UIManager.installLookAndFeel(classShortName, className);
                                } catch (NoClassDefFoundError | NoSuchMethodException constructorEx) {
                                    // Handle constructor not found
                                }
                            }
                        }
                    } catch (NoClassDefFoundError | Exception classEx) {
                        System.out.println(classEx.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            // Handle URL creation exception
            e.printStackTrace();
        }
        Constants.LOGGER.log(Level.INFO, "Done loading JARs! ");

        this.newClassLoader = cl;
    }
}
