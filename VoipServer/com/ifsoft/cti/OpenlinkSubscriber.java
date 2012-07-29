package com.ifsoft.cti;


import org.xmpp.packet.*;

public class OpenlinkSubscriber
{
	private JID jid		 				= null;
	private String name		 			= "";
	private String subscription			= "";
	private String subid				= "";
	private boolean online				= false;


	public JID getJID() {
		return jid;
	}

	public void setJID(JID jid) {
		this.jid = jid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSubscription() {
		return subscription;
	}

	public void setSubscription(String subscription) {
		this.subscription = subscription;
	}

	public String getSubID() {
		return subid;
	}

	public void setSubID(String subid) {
		this.subid = subid;
	}

	public boolean getOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}
}