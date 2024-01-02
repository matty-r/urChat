urChat
======

urChat is a Java based IRC Client designed around simplicity and minimal resource impact on your system.

Contributions
======
**Currently targeting ![Milestone v0.5.1](https://github.com/matty-r/urChat/milestone/9)**

If you would like to assist in the development of urChat take a look at the Issues associated with the project. Please let me know if you wish to tackle a certain issue.

A Maven pom.xml is included to assist in getting your environment up and running sooner. 

Usage
======
Ensure you've got Java 17 available on your system, download and run the latest JAR release (https://github.com/matty-r/urChat/releases). If you'd like to try out the Theme functionality, create a 'themes' directory next to the urChat.jar and download the FlatLAF.jar release and place within that directory. The theme can be selected under the client options page.

Themes
======
Tested with FlatLaf + IntelliJ Themes, download the below jar files and place them in a themes directory alongside the urchat.jar.

* [Flatlaf 3.2.5](https://search.maven.org/artifact/com.formdev/flatlaf/3.2.5/jar?eh=)
* [Flatlaf IntelliJ themes](https://search.maven.org/artifact/com.formdev/flatlaf-intellij-themes/3.2.5/jar?eh=)

Screenshots
======

![image](https://github.com/matty-r/urChat/assets/9965448/581ea2c9-280a-45c1-b254-d146465f0d21)

Test/Code Coverage Dependencies
======
Dependencies required only for running the tests:

If you're not using Maven use the following instructions, otherwise just download the dependencies via the pom.xml.

Create a **lib/test** directory with the following files:

* [Junit 4](https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar)
* [Hamcrest Core](https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar)
* [TestNG 7.8.0](https://repo1.maven.org/maven2/org/testng/testng/7.8.0/testng-7.8.0.jar)
* [JCommander](https://repo1.maven.org/maven2/com/beust/jcommander/1.82/jcommander-1.82.jar)
* [SLF4J](https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar)

Extract jacocoagent.jar and jacococli.jar into the **lib/coverage** directory

* [Jacoco](https://search.maven.org/remotecontent?filepath=org/jacoco/jacoco/0.8.11/jacoco-0.8.11.zip)

Test Usage
======

Using the testng.xml - must be in the same directory as urchat.jar
*	java -cp "urTestRunner.jar" org.testng.TestNG testng.xml

Without testng.xml
*	java -jar urTestRunner.jar

### Update - v0.4.0 (12 DEC 23)
* Added custom styling support for the different styles used in messages
* Added custom Timestamp format
* UI Improvements
* New appearance tab, and formatting preview area
* Clickable channel names in messages (Can be styled separately from URLs)
* Various bug fixes
* Performance improvements, particularly around the styling of messages

### Update - v0.3.0 (22 SEP 23)
* Bug fixes and performance improvements
* UI tweaks - reduce repaints, focus handling, new tabs, layout etc..
* Added support for Profiles, create a Profile to save individual settings/favourites and Theme
* Refactoring for better handling and avoiding race conditions
* Adds support for Themeing through LookAndFeel plugins - just drop the LAF jar in a themes directory alongside the urchat.jar (tested with FlatLAF)
* Improved color handling for theme support.
* Font changes are now also applied to all previously sent text
* Added support for 401 error handling.
* Added option to save passwords (currently is plain-text).

### Update (22 AUG 23)
* Moved 'Resources' to 'images'
* Created an initial release on the GitHub page (https://github.com/matty-r/urChat/releases)
* Updated the tab component
* Fixed all rooms from showing under favourites

### Update (19 AUG 23)
* Right-click user names in the text area to show the IRCUser popup menu
* Font picker is now a dialog not a panel
* Save fonts per room
* Adds support for 328 (RPL_CHANNEL_URL) - just shows it as part of the notice message when joining a channel
* Fixed QUIT handling, properly captures QUIT events instead of just PART events
* Started adding some unit tests

### Update (13 AUG 23)

* Add support for SASL (PLAIN) Authentication
* Add auto-Nickserv authentication
* Added a MessageDialog for showing alerts,warnings.. etc
* Clickable links have been implemented, and can be toggled
* Tab now highlights/flashes if you're mentioned
* Disconnecting/Reconnecting to the server is now done correctly
* TLS/SSL has been implemented
* Adds support for using a proxy
* Users list is now resizeable

### Update (23 DEC 14)

* Added check to make sure a user doesn't already exist in the users list before attempting to add them.

### Update (22 DEC 14)

* Added support for 403(NoSuchChannel) and 461(NotEnoughParameters) - Goofybud16

### Update (21 DEC 14)

* Improved MessageHandler
* Added tab Drag and Drop functionality - thanks jnorgan
* Fixed bug where quitting from the server would result in an exception - it was trying to send information to the server that had already been removed.
* Added saving of Window Bounds (X,Y,Width,Height)
* Added save settings on exit
* Added scrollpanes to Server options and client Options - also moved the Save Settings button
* Added an icon :)
* Changed how MessageHandler is instantiated through Connection.

### Update (20 DEC 14)

* Added MessageHandler - It's now much more flexible and allows it to be expanded to support many different types of messages, each with it's own accompanying method. Not 100% tested at this stage, but we no longer need to rely on Connection to do the message parsing - Connection should only be worried about receiving the text.

### Update (19 DEC 14)

* Fixed usersList not selecting an item when you right click a user
* Fixed LOGGER not deleting lck files on exit.
* Added MessageHandler placeholder class for future message handling.

### Update (18 DEC 14)

* Changed logging function to use inbuilt logger - thanks Goofybud16
* Fixed lock-up when change to/from Private tab - required changing how selecting a tab is handled, now uses the same method of getting what type of tab is selected as TabbedMouseListener.
* Changed some methods to use an iterator instead of an Enhanced For loop, this should help with ConcurrentModificationExceptions.
* Fixed IndexOutOfBounds error when parsing the 'message' out of receivedText by checking to make sure there are two ':' within received text. See countOfOccurences within Connection.
* Improved the layout of the option panels
* Removed the favourites panel and placed it onto the same panel as the Server options - this makes more sense as you'll be able to see what you're connecting to when you press connect.
* Added Port to the server options, defaults to port 6667
* Added Port requirement when creating an IRCServer&Connection.
* Added Real name to the server options
* Added settings as constants.

### Update (17 DEC 14)

* Package split into Frontend, Base and Backend - thanks Goofybud16

### Update (16 DEC 14)

* Changed how user status is set, it's now set through IRCUser in the constructor
* Changed how the JOIN command is handled, channels will always start with # so use this to find the channel name to join to. Also use the extractNick() method to get the appropriate username if it isn't me.
* Added check to make sure the Logs directory exists, if not then create it - thanks Goofybud16
* TODO list is now maintained on GITHUB https://github.com/matty-r/urChat/issues
* Fixed communication with multiple servers - BufferedReader and BufferedWriter of the connection class were static methods. This was a piece that hadn't been changed over during the transition to enable multiple server connections.
* Improved the handling of the TabbedMouseListener, it finds out the appropriate class of the selected tab and carries out the correct method. This will stop things with the same name closing incorrectly.
* Removed unused imports.

### Update (15 DEC 14)

* Java 7 and *nix compatibility - thanks Goofybud16
* Added change font in the channel right click menu
* Added change font globally (A bit messy right now)
* Changed handling of channel messages - if a message is received by someone not in the user list they are added to the list and then the message is displayed.

### Update (14 DEC 14)

* Added LineFormatter, does all the formatting and inserting of the strings into the document

### Update (13 DEC 14)

* Added Save channels as favourites
* Added automatically connect to server/channels when you connect for the first time
* Added save as favourite in the channel right click menu
* Added automatically attempt to reconnect to favourites when you are identified.

### Update (12 DEC 14)

* Changed how connections are handled in order to prep for handling multiple servers. IRCServer now controls it's own Connection and its contained within it's own thread.
* Tested connecting to multiple servers, so far it appears to be working fine.

### Update (11 DEC 14)

* Added mute to right click menu for user, stop receiving message in channel or private messages
* Added memory usage method. Will integrate this into the client tab.

### Update (10 DEC 14)

* Added right click menu to channel tab
* Added Quit to tab right click menu
* Added Show/hide users list to tab right click menu
* Added Show/hide event ticker
* Changed Ticker height to 35
* Fixed removal of ticker labels from the ticker panel. It was only removing it from the eventLabels array, and not removing it from the actual panel.
* Added Hiding the Event Ticker will remove all labels and stop the timer.
* Added global control of show or hide
* Added setting the Hide event ticker/users list overrides the global control.

### Update (9 DEC 14)

* Added showing events (Joins/Quits) in the main text window, I forgot that I removed it ages ago
* Changed all ArrayLists in IRCChannel to List<> and instead I instantiate it as an ArrayList<>
* Added toggle for the saving of the entire server history
* Added a Resources folder so that nothing was stored in the root path of the program
* Changed the Look and Feel to systemLookandFeel()
* Added whois command to right-click user menu, sends reponse to private window
* Fixed Boolean methods to say isMethodEnabled instead of getMethod.
* Added universalFont, controls the font of all textareas
* Added option to Limit the number of lines in server and channel (Still needs some improving)

### Update (7 DEC 14)

* Added a right click menu to users - controlled within IRCUser
* Added NickServ response gets it's own private room (As per a user)
* Fixed hangs on exit bug (Hopefully)
* Added right click on tab to quit channel/room
* Added open private room when right click on someones name and select Private Message

### Update (6 DEC 14)

* Fixed sorting of user list

### Update (5 DEC 14)

* Added server message logging, saves messages to a text file and has an alert to show if a message
	was not handled.
* Fixed handling of NICK, some how I must have removed it.. whoops :)
* Fixed /quit command, now correctly quits all channels and servers, also removing their appropriate
	tab
* Added focus automatically goes to text box in server tab
* Added colourful icons per tab so that you know what type of tab it is
* Added Create a private message channel tab when I receive a private message
* Added Save history in real time
* Improved handling of private messages

### Update (4 DEC 14)

* Fixed words not wrapping correctly if there is no space.
* Fixed the alert of someone changing their name displaying in all Channels. Now only shows in the Channel where the user was located.
* Added a Server class
* Improved the parsing of server messages - won't lose any detail from splitting by a blank space.
* Added text box to server window so that it's possible to send messages through the server console	instead of in a channel. This allows you to change nick or identify without worry about sending	text to server.

### Update (30 NOV 14)

* Added ticker slows down on mouse over
* Changed text area to a JTextPane
* Added text formatting based on who sends it. Need to adjust formatting later
* Fixed receiving text from a channel mod with @ in front of their name
* Added IRCUser now has a character modifier to identify if they are a mod or not

### Update (26 NOV 14)

* Added icons on the Server Tab- Go, Wait and Stop. Changes icon depending on server status
* Fixed Server tab status to only show disconnected when the server responds to the Quit command.
* Fixed list sorting ... People that join the channel are now an IRCUser object and users exist	in a UsersListModel which implements AbstractListModel. Collections.sort();
* Fixed Renaming users to work with the new IRCUser Object.

### Update (24 NOV 14)

* Added event ticker delay slider
* Added saving of settings using the preferences api
* Fixed tab complete - it had lost ability to tab through names
* Changed all static methods to non-static (This is better right?)
* Fixed PART messages not correctly removing a user
* Fixed Event Ticker alerts not showing up fast enough, remove super.width as buffer, alerts can now
	chain together.

### Update (23 NOV 14)

* Fixed Options panel layouts
* Removed Show Joins/Quits in the chat window checkbox, I want ticker only at the moment
* Added Disconnect also, it closes all open channels/tabs
* Fixed the creation of Channel tabs, only creates the tab on successful connect
* Added sets tab index to new channel tab on join
* Added Connected/Disconnected prompt onto Server tab
* Added functionality to Show Joins/Quits checkbox
* Added Client and Channel history check box
* Added timestamp checkbox
* Added save chat history to working directory on disconnect.

### Update (22 NOV 14)

* Fixed tab complete for user names, works mid sentence also, and will rotate through all available matches
* Added focus in the chat text box when you change the tab to a channel
* Added playing a noise when your nick is mentioned in a message or you receive a private message
* Added press up or down to repeat previous things you've said per channel
* Added Connect button to options panel, only connects to server when you press connect.
* Added press escape to clear the client text box

### Update (21 NOV 14)

* Added Tabs for each channel that you join, tabs are automatically created
* Added channel specific alerts to only display in that tab i.e Join/Quit
* Fixed the UserGUI to redirect channel specific calls to the IRCChannel correctly
* Added news ticker style events of people joining a channel

### Update (20 NOV 14)

* Added alert when user quits channel, removes user from user list
* Added sorting of names alphabetical in users list (Took me ages to get this working correctly)
* Broke everything when trying to get tabs to work.
* Fixed the tab layout by using BorderLayout.
* Added tab complete to the client text box (Currently only works if the name is the first word in the text box)

### Update (18 NOV 14)

* Added List for clients connected to channel (Only when you join the channel though)
* Added when a user joins the channel user list will now also update
* Changed size of Clients list to only 100 pixels
* Changed font of channel text area, it's now a bit more readable
* Fixed the invokeLater for the GUI and connection threads

### Update (16 NOV 14)

* Added a text box to send messages
* Added main text area to show server messages
* Added parse /join,/msg and /nick commands from client
* Fixed auto-response to ping server messages

### Update (7 NOV 14)

* Connect to IRC server and receive server text via console
