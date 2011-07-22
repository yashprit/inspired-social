package com.ifsoft.inspired;

import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.admin.AuthCheckFilter;
import org.jivesoftware.openfire.http.HttpBindManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.PrivateStorage;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.roster.Roster;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.roster.*;
import org.jivesoftware.openfire.user.*;
import org.jivesoftware.util.*;
import org.jivesoftware.openfire.muc.*;
import org.jivesoftware.openfire.muc.spi.*;
import org.jivesoftware.openfire.forms.spi.*;
import org.jivesoftware.openfire.forms.*;
import org.jivesoftware.openfire.group.*;
import org.jivesoftware.openfire.event.GroupEventDispatcher;

import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;

import java.util.*;
import java.text.DateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.xmpp.packet.*;
import java.sql.*;
import java.io.File;
import org.dom4j.Element;
import com.milgra.server.Server;
import org.red5.server.webapp.voicebridge.Application;


// uncomment for openfire 3.6.4
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.webapp.WebAppContext;

// uncomment for openfire 3.7.0
//import org.eclipse.jetty.server.handler.ContextHandlerCollection;
//import org.eclipse.jetty.webapp.WebAppContext;

public class InspiredPlugin implements Plugin, InspiredConstants
{
	private static final String NAME 		= "inspired";
	private static final String DESCRIPTION = "Inspired Plugin for Openfire";

	private PluginManager manager;
    private File pluginDirectory;
    private Application application;
    private InspiredComponent component;


	public void initializePlugin(PluginManager manager, File pluginDirectory)
	{
		Log.info( "["+ NAME + "] initialize " + NAME + " plugin resources");

		try {

			ContextHandlerCollection contexts = HttpBindManager.getInstance().getContexts();

			try {
				WebAppContext context = new WebAppContext(contexts, pluginDirectory.getPath(), "/" + NAME);
				context.setWelcomeFiles(new String[]{"index.php"});

				Log.info("starting Inspired Component");
				component = new InspiredComponent(this);
				component.componentEnable();

				Log.info("starting Milenia Grafter RTMP Server");
				new Server();

				Log.info("starting VOIP Server");
				application = new Application();
				application.appStart(component);
				component.setApplication(application);

			}
			catch(Exception e) {

        	}

			Log.info("Setting WordPress as new auth Provider");

			JiveGlobals.setProperty("jdbcAuthProvider.passwordSQL", "SELECT user_pass FROM wp_users WHERE user_login=?");
			JiveGlobals.setProperty("jdbcAuthProvider.setPasswordSQL", "");
			JiveGlobals.setProperty("jdbcAuthProvider.allowUpdate", "false");
			JiveGlobals.setProperty("jdbcAuthProvider.passwordType", "md5");
			JiveGlobals.setProperty("jdbcAuthProvider.useConnectionProvider", "true");
			JiveGlobals.setProperty("provider.auth.className",  "org.jivesoftware.openfire.auth.JDBCAuthProvider");

			Log.info("Setting WordPress as user Provider");

			JiveGlobals.setProperty("jdbcUserProvider.loadUserSQL", "SELECT user_nicename, user_email FROM wp_users WHERE user_login=?");
			JiveGlobals.setProperty("jdbcUserProvider.userCountSQL", "SELECT COUNT(*) FROM wp_users");
			JiveGlobals.setProperty("jdbcUserProvider.allUsersSQL", "SELECT user_login FROM wp_users");
			JiveGlobals.setProperty("jdbcUserProvider.searchSQL", "SELECT user_login FROM wp_users WHERE");
			JiveGlobals.setProperty("jdbcUserProvider.user_loginField", "user_login");
			JiveGlobals.setProperty("jdbcUserProvider.nameField", "user_nicename");
			JiveGlobals.setProperty("jdbcUserProvider.emailField", "user_email");
			JiveGlobals.setProperty("jdbcUserProvider.useConnectionProvider", "true");
			JiveGlobals.setProperty("provider.user.className",  "org.jivesoftware.openfire.user.JDBCUserProvider");

			Log.info("Setting WordPress as group Provider");

			JiveGlobals.setProperty("jdbcGroupProvider.groupCountSQL", "SELECT count(*) FROM wp_bp_groups");
			JiveGlobals.setProperty("jdbcGroupProvider.allGroupsSQL", "SELECT name FROM wp_bp_groups");
			JiveGlobals.setProperty("jdbcGroupProvider.userGroupsSQL", "SELECT name FROM wp_bp_groups INNER JOIN wp_bp_groups_members ON wp_bp_groups.id = wp_bp_groups_members.group_id WHERE wp_bp_groups_members.user_id IN (SELECT ID FROM wp_users WHERE user_login=?) AND is_confirmed=1");
			JiveGlobals.setProperty("jdbcGroupProvider.descriptionSQL", "SELECT description FROM wp_bp_groups WHERE name=?");
			JiveGlobals.setProperty("jdbcGroupProvider.loadMembersSQL", "SELECT user_login FROM wp_users INNER JOIN wp_bp_groups_members ON wp_users.ID = wp_bp_groups_members.user_id WHERE wp_bp_groups_members.group_id IN (SELECT id FROM wp_bp_groups WHERE name=?) AND user_login<>'admin' AND is_confirmed=1");
			JiveGlobals.setProperty("jdbcGroupProvider.loadAdminsSQL", "SELECT user_login FROM wp_users INNER JOIN wp_bp_groups_members ON wp_users.ID = wp_bp_groups_members.user_id WHERE wp_bp_groups_members.group_id IN (SELECT id FROM wp_bp_groups WHERE name=?) AND user_login='admin' AND is_confirmed=1");
			JiveGlobals.setProperty("jdbcGroupProvider.useConnectionProvider", "true");
			JiveGlobals.setProperty("provider.group.className",  "org.jivesoftware.openfire.group.JDBCGroupProvider");

			JiveGlobals.setProperty("cache.groupMeta.maxLifetime", "60000");
			JiveGlobals.setProperty("cache.group.maxLifetime", "60000");
			JiveGlobals.setProperty("cache.userCache.maxLifetime", "60000");
		}
		catch (Exception e) {
			Log.error("Error initializing " + NAME + " Plugin", e);
		}
	}

	public void destroyPlugin()
	{
		Log.info( "["+ NAME + "] destroy " + NAME + " plugin resources");

		try {
				Log.info("stopping Milenia Grafter RTMP Server");
				Server.closeRequest();

				Log.info("stopping VOIP Server");
				application.appStop();

				Log.info("stopping Inspired Component");
				component.componentDestroyed();

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

	public InspiredComponent getComponent()
	{
		return component;
	}

}