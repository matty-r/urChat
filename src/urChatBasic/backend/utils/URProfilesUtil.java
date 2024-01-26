package urChatBasic.backend.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javax.swing.event.EventListenerList;
import urChatBasic.base.Constants;
import urChatBasic.base.Constants.EventType;
import urChatBasic.frontend.UserGUI;

public class URProfilesUtil
{
    static String activeProfileName = "";

    // protected EventListenerList createsListenerList = new EventListenerList();
    // protected EventListenerList deletesListenerList = new EventListenerList();
    // protected EventListenerList renamesListenerList = new EventListenerList();
    protected static Map<EventType, EventListenerList> listenerLists = new HashMap<>();
    protected transient static ActionEvent actionEvent = null;

    static final Preferences BASE = Constants.BASE_PREFS;
        /**
     * Retrieves all profiles that have been created and returns a String[] of the names. If no profiles are available, it creates the "Default" profile and sets it as the
     * default.
     * @return
     */
    public static String[] getProfiles ()
    {
        List<String> allProfiles = new ArrayList<String>();
        try
        {
            if (BASE.childrenNames().length == 0)
            {
                BASE.put(Constants.KEY_DEFAULT_PROFILE_NAME, Constants.DEFAULT_PROFILE_NAME);
                allProfiles = Arrays.asList(new String[] {Constants.DEFAULT_PROFILE_NAME});
            } else {
                allProfiles = Arrays.stream(BASE.childrenNames()).collect(Collectors.toList());
            }
        } catch (BackingStoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Use a Set, then convert to Array to drop any duplicates
        String[] profileNames = (new HashSet<>(allProfiles)).toArray(String[]::new);

        return profileNames;
    }


    public static void deleteProfile (String profileName)
    {
        try
        {
            String[] allProfiles = getProfiles();

            if(allProfiles.length > 1)
            {
                Constants.LOGGER.info( "Deleting profile [" + profileName + "].");
                Constants.BASE_PREFS.node(profileName).removeNode();
                fireListeners(EventType.DELETE);
            }
            else
                throw new BackingStoreException("Unable to delete the last profile.");

            if(profileName.equals(getActiveProfileName()))
            {
                if(profileExists(getDefaultProfile()))
                {
                    setActiveProfileName(getDefaultProfile());
                } else {
                    setActiveProfileName(allProfiles[0]);
                }
            }

        } catch (BackingStoreException e)
        {
            Constants.LOGGER.error("Problem deleting profile [" + profileName +"]." + e.getLocalizedMessage());
        }
    }

    /**
     * Deletes the active profile
     */
    public static void deleteProfile ()
    {
        deleteProfile(getActiveProfileName());
    }

    public static String getActiveProfileName ()
    {
        if(activeProfileName.isEmpty())
            return getDefaultProfile();
        else
            return activeProfileName;
    }

    public static void setActiveProfileName (String activeProfileName) // throws ConfigurationException
    {
        if(profileExists(activeProfileName))
        {
            URProfilesUtil.activeProfileName = activeProfileName;
            // UserGUI.fireProfileChangeListeners();
            fireListeners(EventType.CHANGE);
        } else {
            Constants.LOGGER.error("Profile ["+activeProfileName+"] doesn't exist.");
            // throw new ConfigurationException("Profile ["+activeProfileName+"] doesn't exist.");
        }
    }

    public static Preferences getProfilePath (String profileName)
    {
        return Constants.BASE_PREFS.node(profileName);
    }

    public static Preferences getActiveProfilePath ()
    {
        return getProfilePath(getActiveProfileName());
    }

    public static Preferences getFavouritesPath (String profileName)
    {
        return getProfilePath(profileName).node("favourites");
    }

    public static Preferences getActiveFavouritesPath ()
    {
        return getFavouritesPath(getActiveProfileName());
    }

    public static boolean profileExists (String profileName)
    {
        String[] allProfiles = getProfiles();

        for (int i = 0; i < allProfiles.length; i++)
        {
            if (allProfiles[i].toString().equals(profileName))
            {
                return true;
            }
        }

        return false;
    }

    public static void cleanUpSettings ()
    {
        Constants.LOGGER.info( "Cleaning up settings");
        try
        {
            Constants.LOGGER.info( "Remove empty favourites");
            ArrayList<Preferences> allNodes = URPreferencesUtil.getAllNodes(Constants.BASE_PREFS);

            List<Preferences> deletePreferences = new ArrayList<>();

            for (Preferences nodePreferences : allNodes) {
                if(nodePreferences.keys().length == 0 && nodePreferences.childrenNames().length == 0)
                    deletePreferences.add(nodePreferences);
            }

            while(deletePreferences.size() > 0)
            {
                try {
                    deletePreferences.get(0).removeNode();
                    deletePreferences.remove(0);
                } catch (Exception ex)
                {
                    // todo
                }
            }
        } catch (BackingStoreException e)
        {
            Constants.LOGGER.error(e.getLocalizedMessage());
        }
    }

    public static Preferences cloneProfile (String originalProfileName, Optional<String> newProfileName)
    {
        Preferences originalPathRoot = getProfilePath(originalProfileName);
        int cloneNumber = 1;
        String clonedProfileName;
        if(newProfileName.isEmpty())
            clonedProfileName = originalProfileName;
        else
            clonedProfileName = newProfileName.get();

        try
        {
            while(originalPathRoot.parent().nodeExists(clonedProfileName))
            {
                if(newProfileName.isEmpty())
                    clonedProfileName = originalProfileName+" ("+cloneNumber+")";
                else
                    clonedProfileName = newProfileName.get()+" ("+cloneNumber+")";

                cloneNumber++;
            }
        } catch (BackingStoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        final Preferences clonedPathRoot = originalPathRoot.parent().node(clonedProfileName);

        ArrayList<Preferences> originalNodes = URPreferencesUtil.getAllNodes(originalPathRoot);

        for (Preferences originalPrefPath : originalNodes) {
            Preferences clonedPath = clonedPathRoot;

            String[] childNodes = Path.of(originalPrefPath.absolutePath().replace(originalPathRoot.absolutePath(), "")).toString().split("/");

            for (String childName : childNodes) {
                if(!childName.isEmpty())
                    clonedPath = clonedPath.node(childName);
            }

            try
            {
                for (String originalKey : originalPrefPath.keys()) {
                    URPreferencesUtil.putPref(originalKey, Optional.of(URPreferencesUtil.getPref(originalKey, null, originalPrefPath)), clonedPath);
                }
            } catch (BackingStoreException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


        fireListeners(EventType.CREATE);
        return clonedPathRoot;
    }

    public static String getDefaultProfile ()
    {
        try
        {
            if(Arrays.asList(BASE.keys()).contains(Constants.KEY_DEFAULT_PROFILE_NAME))
            {
                return BASE.get(Constants.KEY_DEFAULT_PROFILE_NAME, Constants.DEFAULT_PROFILE_NAME);
            }
        } catch (BackingStoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        setDefaultProfile(Constants.DEFAULT_PROFILE_NAME);

        return Constants.DEFAULT_PROFILE_NAME;
    }

    public static void setDefaultProfile (String profileName)
    {
        Constants.LOGGER.info( "Setting default profile [" + profileName + "]");
        BASE.put(Constants.KEY_DEFAULT_PROFILE_NAME, profileName);
    }

    public static void createProfile (String profileName)
    {
        int newProfileNumber = 0;
        String newProfileName = profileName;

        try
        {
            // If it exists, increment the number and give the profile a new name
            while(Constants.BASE_PREFS.nodeExists(newProfileName))
            {
                newProfileNumber++;
                newProfileName = profileName + " ("+newProfileNumber+")";
            }
        } catch (BackingStoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Constants.LOGGER.info( "Creating new profile [" + newProfileName + "]");
        setDefaultSettings(newProfileName);
        fireListeners(EventType.CREATE);
    }

    private static void setDefaultSettings (String profileName)
    {
        Preferences profileNode = BASE.node(profileName);

        profileNode.put(Constants.KEY_FIRST_CHANNEL, Constants.DEFAULT_FIRST_CHANNEL);
        profileNode.put(Constants.KEY_FIRST_SERVER, Constants.DEFAULT_FIRST_SERVER);
        profileNode.put(Constants.KEY_FIRST_PORT, Constants.DEFAULT_FIRST_PORT);
        profileNode.put(Constants.KEY_AUTH_TYPE, Constants.DEFAULT_AUTH_TYPE);
        profileNode.putBoolean(Constants.KEY_PASSWORD_REMEMBER, Constants.DEFAULT_PASSWORD_REMEMBER);

        profileNode.putBoolean(Constants.KEY_USE_TLS, Constants.DEFAULT_USE_TLS);
        profileNode.put(Constants.KEY_PROXY_HOST, Constants.DEFAULT_PROXY_HOST);
        profileNode.put(Constants.KEY_PROXY_PORT, Constants.DEFAULT_PROXY_PORT);
        profileNode.put(Constants.KEY_PROXY_TYPE, Constants.DEFAULT_PROXY_TYPE);
        profileNode.put(Constants.KEY_NICK_NAME, Constants.DEFAULT_NICK_NAME);
        profileNode.put(Constants.KEY_REAL_NAME, Constants.DEFAULT_REAL_NAME);
        profileNode.putBoolean(Constants.KEY_TIME_STAMPS, Constants.DEFAULT_TIME_STAMPS);
        profileNode.put(Constants.KEY_TIME_STAMP_FORMAT, Constants.DEFAULT_TIME_STAMP_FORMAT);
        profileNode.put(Constants.KEY_LAF_NAME, Constants.DEFAULT_LAF_NAME);
        profileNode.putBoolean(Constants.KEY_EVENT_TICKER_ACTIVE, Constants.DEFAULT_EVENT_TICKER_ACTIVE);
        profileNode.putBoolean(Constants.KEY_USERS_LIST_ACTIVE, Constants.DEFAULT_USERS_LIST_ACTIVE);
        profileNode.putBoolean(Constants.KEY_CLICKABLE_LINKS_ENABLED, Constants.DEFAULT_CLICKABLE_LINKS_ENABLED);
        profileNode.putBoolean(Constants.KEY_EVENT_TICKER_JOINS_QUITS, Constants.DEFAULT_EVENT_TICKER_JOINS_QUITS);
        profileNode.putBoolean(Constants.KEY_MAIN_WINDOW_JOINS_QUITS, Constants.DEFAULT_MAIN_WINDOW_JOINS_QUITS);
        profileNode.putBoolean(Constants.KEY_LOG_CHANNEL_ACTIVITY, Constants.DEFAULT_LOG_CHANNEL_ACTIVITY);
        profileNode.putBoolean(Constants.KEY_LOG_SERVER_ACTIVITY, Constants.DEFAULT_LOG_SERVER_ACTIVITY);
        profileNode.putBoolean(Constants.KEY_LIMIT_CHANNEL_LINES, Constants.DEFAULT_LIMIT_CHANNEL_LINES);
        profileNode.putBoolean(Constants.KEY_AUTO_CONNECT_FAVOURITES, Constants.DEFAULT_AUTO_CONNECT_FAVOURITES);
        profileNode.put(Constants.KEY_LIMIT_CHANNEL_LINES_COUNT, Constants.DEFAULT_LIMIT_SERVER_LINES_COUNT);
        profileNode.putBoolean(Constants.KEY_LIMIT_SERVER_LINES, Constants.DEFAULT_LIMIT_SERVER_LINES);
        profileNode.put(Constants.KEY_LIMIT_SERVER_LINES_COUNT, Constants.DEFAULT_LIMIT_SERVER_LINES_COUNT);
        profileNode.putBoolean(Constants.KEY_LOG_CLIENT_TEXT, Constants.DEFAULT_LOG_CLIENT_TEXT);
        // URPreferencesUtil.saveStyle(defaultStyle, clientFontPanel.getStyle(), profileNode);
        profileNode.putInt(Constants.KEY_EVENT_TICKER_DELAY, Constants.DEFAULT_EVENT_TICKER_DELAY);

        profileNode.putInt(Constants.KEY_WINDOW_X, Constants.DEFAULT_WINDOW_X);
        profileNode.putInt(Constants.KEY_WINDOW_Y, Constants.DEFAULT_WINDOW_Y);
        profileNode.putInt(Constants.KEY_WINDOW_WIDTH, Constants.DEFAULT_WINDOW_WIDTH);
        profileNode.putInt(Constants.KEY_WINDOW_HEIGHT, Constants.DEFAULT_WINDOW_HEIGHT);
    }

    public static void addListener(EventType eventType, ActionListener actionListener)
    {
        if(!listenerLists.containsKey(eventType))
            listenerLists.put(eventType, new EventListenerList());

        listenerLists.get(eventType).add(ActionListener.class, actionListener);
    }

    public static void removeListener(EventType eventType, ActionListener actionListener)
    {
        if(listenerLists.containsKey(eventType))
            listenerLists.get(eventType).remove(ActionListener.class, actionListener);
    }

    public static void fireListeners (EventType eventType)
    {
        if(!listenerLists.containsKey(eventType))
            listenerLists.put(eventType, new EventListenerList());

        Object[] listeners = listenerLists.get(eventType).getListenerList();

        // Reverse order
        for (int i = listeners.length - 2; i >= 0; i -= 2)
        {
            if (listeners[i] == ActionListener.class)
            {
                if (actionEvent == null)
                {
                    actionEvent = new ActionEvent(listeners, i, null);
                }

                ((ActionListener) listeners[i + 1]).actionPerformed(actionEvent);
            }
        }
    }
}
