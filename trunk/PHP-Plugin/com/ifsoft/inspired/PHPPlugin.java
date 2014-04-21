package com.ifsoft.inspired;

import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.http.HttpBindManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.*;

import java.io.File;

import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.webapp.WebAppContext;

public class PHPPlugin implements Plugin

{
	private static final String NAME 		= "php";
	private static final String DESCRIPTION = "PHP Plugin for Openfire";

	private PluginManager manager;
    private File pluginDirectory;


	public void initializePlugin(PluginManager manager, File pluginDirectory)
	{
		Log.info( "["+ NAME + "] initialize " + NAME + " plugin resources");

		try {

			ContextHandlerCollection contexts = HttpBindManager.getInstance().getContexts();

			try {
				WebAppContext context = new WebAppContext(contexts, pluginDirectory.getPath(), "/" + NAME);
				context.setWelcomeFiles(new String[]{"index.php"});
			}
			catch(Exception e) {

        	}
		}
		catch (Exception e) {
			Log.error("Error initializing " + NAME + " Plugin", e);
		}
	}

	public void destroyPlugin()
	{
		Log.info( "["+ NAME + "] destroy " + NAME + " plugin resources");

		try {

		}
		catch (Exception e) {
			Log.error("["+ NAME + "] destroyPlugin exception " + e);
		}
	}

	public String getName()
	{
		 return NAME;
	}

	public String getDescription()
	{
		return DESCRIPTION;
	}

}