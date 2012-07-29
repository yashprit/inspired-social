package com.ifsoft.inspired;

import org.jivesoftware.openfire.XMPPServer;
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

import java.util.*;

import org.xmpp.packet.*;
import java.sql.*;
import java.io.File;
import org.dom4j.Element;

import com.caucho.quercus.module.AbstractQuercusModule;



public class PHP2Java extends AbstractQuercusModule
{
	public String hello_test(String name)
	{
		return "Hello, " + name;
	}

	public void of_logInfo(String text)
	{
		Log.info(text);
	}

	public void of_logError(String text)
	{
		Log.error(text);
	}

	public void of_logWarn(String text)
	{
		Log.warn(text);
	}

	public synchronized void sendEmail(String toAddress, String subject, String body, String htmlBody)
	{
	   try {
		   String domainName = JiveGlobals.getProperty("xmpp.domain", XMPPServer.getInstance().getServerInfo().getHostname());

		   Log.info( "sendEmail " + toAddress + " " + subject + "\n " + body + "\n " + htmlBody);

		   EmailService.getInstance().sendMessage(null, toAddress, "Inspired Social", "no_reply@" + domainName, subject, body, htmlBody);
	   }
	   catch (Exception e) {
		   Log.error(e.toString());
	   }

	}


	public synchronized void createGroupChat(String groupId)
	{
		String roomName = getSQLField("SELECT name FROM wp_bp_groups WHERE id='" + groupId + "'", "name");
		String domainName = JiveGlobals.getProperty("xmpp.domain", XMPPServer.getInstance().getServerInfo().getHostname());

		Log.info( "createGroupChat " + groupId + " " + roomName);

		try
		{
			if (roomName != null)
			{
				createRoom(removeSpaces(roomName).toLowerCase());
			}

		} catch(Exception e) {

			Log.error("createGroupChat exception " + e);
		}
	}

	private String removeSpaces(String Name)
	{
		String NewName = "";
		for ( int i = 0; i < Name.length(); i++)
		{
			if (Name.charAt(i) != ' ' )
			{
				NewName = NewName + Name.charAt(i);
			}
		}
		return NewName;
	}

	public synchronized String getGroupChats(String userId)
	{
		//Log.info( "getGroupChats " + userId);

		String sql = "SELECT name FROM wp_bp_groups INNER JOIN wp_bp_groups_members ON wp_bp_groups.id = wp_bp_groups_members.group_id WHERE wp_bp_groups_members.user_id ='" + userId + "' AND is_confirmed=1";
		return getSQLGroupNames(sql);
	}

	public synchronized void joinLeaveGroup(String fromUserId, String groupId, String action)
	{
		String groupName = getSQLField("SELECT name FROM wp_bp_groups WHERE id='" + groupId + "'", "name");
		String fromUser = getUserIdByID(fromUserId);

		String domainName = JiveGlobals.getProperty("xmpp.domain", XMPPServer.getInstance().getServerInfo().getHostname());

		if (groupName != null)
		{
			try
			{
				Group group = GroupManager.getInstance().getGroup(groupName, true);

				if (group != null)
				{
					if (fromUser != null)
					{
						Log.info( "joinLeaveGroup " + action + " " + fromUser + " " + groupName);

						Map<String, Object> params = new HashMap<String, Object>();
						params.put("member", fromUser+"@"+domainName);

						if ("leave".equals(action))
						{
							GroupEventDispatcher.dispatchEvent(group, GroupEventDispatcher.EventType.member_removed, params);

						} else  {
							GroupEventDispatcher.dispatchEvent(group, GroupEventDispatcher.EventType.member_added, params);
						}
					}
				}
			}
			catch(Exception e)
			{
				Log.error("joinGroup exception " + e);
				e.printStackTrace();
			}
		}
	}


	public synchronized void removeFriendship(String fromUserId, String toUserId)
	{
		String domainName = JiveGlobals.getProperty("xmpp.domain", XMPPServer.getInstance().getServerInfo().getHostname());
		String fromUser = getUserIdByID(fromUserId);
		String toUser = getUserIdByID(toUserId);

		if (fromUser != null && toUser != null)
		{
			Log.info( "removeFriendship " + fromUser + " " + toUser);

			try
			{
				Roster roster = XMPPServer.getInstance().getRosterManager().getRoster(fromUser);

				if (roster != null) {
					RosterItem gwitem = roster.deleteRosterItem(new JID(toUser + "@" + domainName), false);

					if (gwitem != null)
					{
						Presence reply = new Presence();
						reply.setTo(new JID(toUser + "@" + domainName));
						reply.setFrom(new JID(fromUser + "@" + domainName));
						reply.setType(Presence.Type.unavailable);
						XMPPServer.getInstance().getPresenceRouter().route(reply);
					}
				}

				Roster roster2 = XMPPServer.getInstance().getRosterManager().getRoster(toUser);

				if (roster2 != null) {
					RosterItem gwitem = roster2.deleteRosterItem(new JID(fromUser + "@" + domainName), false);

					if (gwitem != null)
					{
						Presence reply = new Presence();
						reply.setTo(new JID(fromUser + "@" + domainName));
						reply.setFrom(new JID(toUser + "@" + domainName));
						reply.setType(Presence.Type.unavailable);
						XMPPServer.getInstance().getPresenceRouter().route(reply);
					}
				}
			}
			catch(Exception e)
			{
				Log.error("removeFriendship exception " + e);
				e.printStackTrace();
			}

		} else Log.warn("cannot delete friendship  " + fromUserId + " " + toUserId);
	}


	public synchronized void createFriendship(String fromUserId, String toUserId, String group)
	{
		String domainName = JiveGlobals.getProperty("xmpp.domain", XMPPServer.getInstance().getServerInfo().getHostname());
		String fromUser = getUserIdByID(fromUserId);
		String toUser = getUserIdByID(toUserId);
		String Nickname = getUserNameByID(toUserId);
		String Nickname2 = getUserNameByID(fromUserId);

		if (fromUser != null && toUser != null)
		{
			try
			{
				Roster roster = XMPPServer.getInstance().getRosterManager().getRoster(fromUser);

				if (roster != null)
				{
					RosterItem gwitem = roster.createRosterItem(new JID(toUser + "@" + domainName), true, true);

					if (gwitem != null)
					{
						Log.info( "createFriendship " + fromUser + " " + toUser + " " + Nickname);

						gwitem.setSubStatus(RosterItem.SUB_BOTH);
						gwitem.setAskStatus(RosterItem.ASK_NONE);
						gwitem.setNickname(Nickname);

						ArrayList<String> groups = new ArrayList<String>();
						groups.add(group);
						gwitem.setGroups((List<String>)groups);
						roster.updateRosterItem(gwitem);
						roster.broadcast(gwitem, true);

						Presence reply = new Presence();
						reply.setTo(new JID(fromUser + "@" + domainName));
						reply.setFrom(new JID(toUser + "@" + domainName));
						XMPPServer.getInstance().getPresenceRouter().route(reply);

					} else Log.warn("cannot create friendship  " + fromUser + " " + toUser + " " + Nickname);
				}

				Roster roster2 = XMPPServer.getInstance().getRosterManager().getRoster(toUser);

				if (roster2 != null)
				{
					RosterItem gwitem = roster2.createRosterItem(new JID(fromUser + "@" + domainName), true, true);

					if (gwitem != null)
					{
						Log.info( "createFriendship " + toUser + " " + fromUser + " " + Nickname2);

						gwitem.setSubStatus(RosterItem.SUB_BOTH);
						gwitem.setAskStatus(RosterItem.ASK_NONE);
						gwitem.setNickname(Nickname2);

						ArrayList<String> groups = new ArrayList<String>();
						groups.add(group);
						gwitem.setGroups((List<String>)groups);
						roster2.updateRosterItem(gwitem);
						roster2.broadcast(gwitem, true);

						Presence reply2 = new Presence();
						reply2.setTo(new JID(toUser + "@" + domainName));
						reply2.setFrom(new JID(fromUser + "@" + domainName));
						XMPPServer.getInstance().getPresenceRouter().route(reply2);

					} else Log.warn("cannot create friendship  " + toUser + " " + fromUser + " " + Nickname2);
				}

			}
			catch(Exception e)
			{
				Log.error("createFriendship exception " + e);
				e.printStackTrace();
			}

		} else Log.warn("cannot create friendship  " + fromUserId + " " + toUserId + " " + group);
	}

	public String getUserIdByID(String id)
	{
		return getUserByID(id, "user_login");
	}

	public String getUserNameByID(String id)
	{
		return getUserByID(id, "user_nicename");
	}

	private String getUserByID(String id, String field)
	{
		return getSQLField("SELECT " + field + " FROM wp_users WHERE ID='" + id + "'", field);
	}

	private String getSQLField(String sql, String field)
	{
		Connection con = null;
		PreparedStatement psmt = null;
		ResultSet rs = null;
		String fieldValue = null;

		try {
			con = DbConnectionManager.getConnection();
			psmt = con.prepareStatement(sql);
			rs = psmt.executeQuery();

			if (rs.next()) {
				fieldValue = rs.getString(field);
			}

		} catch (SQLException e) {
			Log.error("getSQLField exception " + e);

		} finally {
			DbConnectionManager.closeConnection(rs, psmt, con);
		}

		return fieldValue;
	}

	private String getSQLGroupNames(String sql)
	{
		String field = "name";
		Connection con = null;
		PreparedStatement psmt = null;
		ResultSet rs = null;
		String listValue = "";

		try {
			con = DbConnectionManager.getConnection();
			psmt = con.prepareStatement(sql);
			rs = psmt.executeQuery();
			boolean first = true;

			while (rs.next()) {

				String fieldValue = removeSpaces(rs.getString(field)).toLowerCase();
				createRoom(fieldValue);

				if (first)
				{
					listValue = "\"" + fieldValue + "\"";
					first = false;

				} else listValue = listValue + ", \"" + fieldValue + "\"";
			}

		} catch (Exception e) {
			Log.error("getSQLList exception " + e);

		} finally {
			DbConnectionManager.closeConnection(rs, psmt, con);
		}

		return listValue;
	}

	public String getOpenfireUsers()
	{
		String sql = "SELECT * FROM ofuser;";
		Connection con = null;
		PreparedStatement psmt = null;
		ResultSet rs = null;
		String listValue = "";

		try {
			con = DbConnectionManager.getConnection();
			psmt = con.prepareStatement(sql);
			rs = psmt.executeQuery();
			boolean first = true;

			while (rs.next()) {

				String username = rs.getString("username");
				String name = rs.getString("name");
				String email = rs.getString("email");

				if (first)
				{
					listValue = username + "," + name + "," + email;
					first = false;

				} else listValue = listValue + "|" + username + "," + name + "," + email;
			}

		} catch (Exception e) {
			Log.error("getSQLList exception " + e);

		} finally {
			DbConnectionManager.closeConnection(rs, psmt, con);
		}

		return listValue;
	}

	public void messageOtherRoomMembers(String myName, String roomJID, String msg)
	{
		Log.info( "messageOtherRoomMembers " + roomJID);

		try
		{
			String domainName = JiveGlobals.getProperty("xmpp.domain", XMPPServer.getInstance().getServerInfo().getHostname());
			String roomName = (new JID(roomJID)).getNode();

			if (XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService("conference").hasChatRoom(roomName))
			{
				MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService("conference").getChatRoom(roomName);

				for (String jid : room.getMembers())
				{
					Log.info( "messageOtherRoomMembers memember " + jid);

					String hisName = (new JID(jid)).getNode();

					if (hisName.equals(myName) == false)
					{

					}
				}

			}

		} catch (Exception e) {
			Log.error("messageOtherRoomMembers exception " + e);
		}
	}


	private void createRoom(String roomName)
	{
		//Log.info( "createRoom " + roomName);

		try
		{
			String domainName = JiveGlobals.getProperty("xmpp.domain", XMPPServer.getInstance().getServerInfo().getHostname());

			if (XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService("conference").hasChatRoom(roomName) == false)
			{
				MUCRoom room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService("conference").getChatRoom(roomName);

				if (room == null)
				{
					room = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService("conference").getChatRoom(roomName, new JID("admin@"+domainName));

					if (room != null)
					{
						configureRoom(room);
					}
				}
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
	}


	private void configureRoom(MUCRoom room )
	{
		Log.info( "configureRoom " + room.getID());

		FormField field;
		XDataFormImpl dataForm = new XDataFormImpl(DataForm.TYPE_SUBMIT);

        field = new XFormFieldImpl("muc#roomconfig_roomdesc");
        field.setType(FormField.TYPE_TEXT_SINGLE);

		String desc = room.getDescription();
		desc = desc == null ? "" : desc;

		//int pos = desc.indexOf(":");

		//if (pos > 0)
		//	desc = desc.substring(pos + 1);


        //field.addValue(String.valueOf(room.getID() + 1000) + ":" + desc);

        field.addValue(desc);
        dataForm.addField(field);

        field = new XFormFieldImpl("muc#roomconfig_roomname");
        field.setType(FormField.TYPE_TEXT_SINGLE);
        field.addValue(room.getName());
        dataForm.addField(field);

		field = new XFormFieldImpl("FORM_TYPE");
		field.setType(FormField.TYPE_HIDDEN);
		field.addValue("http://jabber.org/protocol/muc#roomconfig");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_changesubject");
		field.addValue("1");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_maxusers");
		field.addValue("30");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_presencebroadcast");
		field.addValue("moderator");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_publicroom");
		field.addValue("1");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_persistentroom");
		field.addValue("1");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_moderatedroom");
		field.addValue("0");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_membersonly");
		field.addValue("0");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_allowinvites");
		field.addValue("1");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_passwordprotectedroom");
		field.addValue("0");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_whois");
		field.addValue("moderator");
		dataForm.addField(field);

		field = new XFormFieldImpl("muc#roomconfig_enablelogging");
		field.addValue("1");
		dataForm.addField(field);

		field = new XFormFieldImpl("x-muc#roomconfig_canchangenick");
		field.addValue("1");
		dataForm.addField(field);

		field = new XFormFieldImpl("x-muc#roomconfig_registration");
		field.addValue("1");
		dataForm.addField(field);

		// Keep the existing list of admins
		field = new XFormFieldImpl("muc#roomconfig_roomadmins");
		for (String jid : room.getAdmins()) {
			field.addValue(jid);
		}
		dataForm.addField(field);

		String domainName = JiveGlobals.getProperty("xmpp.domain", XMPPServer.getInstance().getServerInfo().getHostname());
		field = new XFormFieldImpl("muc#roomconfig_roomowners");
		field.addValue("admin@"+domainName);
		dataForm.addField(field);

		// Create an IQ packet and set the dataform as the main fragment
		IQ iq = new IQ(IQ.Type.set);
		Element element = iq.setChildElement("query", "http://jabber.org/protocol/muc#owner");
		element.add(dataForm.asXMLElement());

		try
		{
			// Send the IQ packet that will modify the room's configuration
			room.getIQOwnerHandler().handleIQ(iq, room.getRole());

		} catch (Exception e) {
			Log.error("configureRoom exception " + e);
		}
	}
}