package com.ifsoft.cti;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import java.io.*;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.openfire.SessionManager;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.jivesoftware.openfire.session.Session;

import org.xmpp.packet.JID;

import org.apache.log4j.Logger;

public class OpenlinkUser implements Comparable
{
	private String userType = "Openlink";
	private boolean enabled = true;
	private boolean intercom = false;
	private boolean autoPrivate = false;
	private boolean autoHold = true;
	private String userName;
	private String userId;
	private String userNo;
	private String deviceNo = "0.0.0.0";
	private String handsetNo = "1";
	private String handsetCallId = null;
	private int handsets = 1;
	private long   siteID;
	private String siteName;
	private String personalDDI = null;
	private String callset = null;
	private String defaultUser = "false";
	private OpenlinkInterest defaultInterest = null;
	private OpenlinkUserInterest waitingInterest = null;

	private List<OpenlinkGroup> openlinkGroups 					= new ArrayList();
	private Map<String, OpenlinkInterest> openlinkInterests 	= new HashMap<String, OpenlinkInterest>();
	public Map<String, String> openlinkTrunks 					= new HashMap<String, String>();

	private String lastPrivacy = null;
	private String lastCallForward = "";
	private String lastCallForwardInterest = "";

	private OpenlinkCall currentHS1Call = null;
	private OpenlinkCall currentHS2Call = null;
	private OpenlinkCall currentICMCall = null;

	private String callback = null;
	private OpenlinkCallback phoneCallback = null;
	private boolean callbackActive = false;
	private String vscLine = null;

    protected Logger Log = Logger.getLogger(getClass().getName());

//-------------------------------------------------------
//
//
//
//-------------------------------------------------------



	public String getProfileName()
	{
		return getUserNo();
	}

	public boolean enabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public boolean autoPrivate()
	{
		return autoPrivate;
	}

	public void setAutoPrivate(boolean autoPrivate)
	{
		this.autoPrivate = autoPrivate;
		this.lastPrivacy = autoPrivate ? "true" : "false";
	}

	public boolean autoHold()
	{
		return autoHold;
	}

	public void setAutoHold(boolean autoHold)
	{
		this.autoHold = autoHold;
	}

	public boolean intercom()
	{
		return intercom;
	}

	public void setIntercom(boolean intercom)
	{
		this.intercom = intercom;
	}

	public int getHandsets()
	{
		return handsets;
	}

	public void setHandsets(int handsets)
	{
		this.handsets = handsets;
	}

	public OpenlinkUserInterest getWaitingInterest()
	{
		return waitingInterest;
	}

	public void setWaitingInterest(OpenlinkUserInterest waitingInterest)
	{
		this.waitingInterest = waitingInterest;
	}

	public String getLastPrivacy() {
		return lastPrivacy;
	}

	public void setLastPrivacy(String lastPrivacy)
	{
		this.lastPrivacy = lastPrivacy;
		this.autoPrivate = "true".equals(lastPrivacy);
	}

	public OpenlinkCall getCurrentHS1Call() {
		return currentHS1Call;
	}

	public void setCurrentHS1Call(OpenlinkCall currentHS1Call)
	{
		this.currentHS1Call = currentHS1Call;
	}

	public OpenlinkCall getCurrentHS2Call() {
		return currentHS2Call;
	}

	public void setCurrentHS2Call(OpenlinkCall currentHS2Call)
	{
		this.currentHS2Call = currentHS2Call;
	}

	public OpenlinkCall getCurrentICMCall() {
		return currentICMCall;
	}

	public void setCurrentICMCall(OpenlinkCall currentICMCall)
	{
		this.currentICMCall = currentICMCall;
	}

	public String getLastCallForward()
	{
		return lastCallForward;
	}

	public void setLastCallForward(String lastCallForward)
	{
		this.lastCallForward = lastCallForward;
	}

	public String getLastCallForwardInterest()
	{
		return lastCallForwardInterest;
	}

	public void setLastCallForwardInterest(String lastCallForwardInterest)
	{
		this.lastCallForwardInterest = lastCallForwardInterest;
	}

	public String getUserName()
	{
		return userName;
	}

	public void setUserName(String userName)
	{
		this.userName = userName;
	}

	public String getUserType()
	{
		return userType;
	}

	public void setUserType(String userType)
	{
		this.userType = userType;
	}

	public String getVSCLine()
	{
		return vscLine;
	}

	public void setVSCLine(String vscLine)
	{
		this.vscLine = vscLine;
	}

	public String getCallset()
	{
		return callset;
	}

	public void setCallset(String callset)
	{
		this.callset = callset;
	}

	public String getDefault()
	{
		return defaultUser;
	}

	public void setDefault(String defaultUser)
	{
		this.defaultUser = defaultUser;
	}

	public OpenlinkInterest getDefaultInterest()
	{
		return defaultInterest;
	}

	public void setDefaultInterest(OpenlinkInterest defaultInterest)
	{
		this.defaultInterest = defaultInterest;
	}

	public String getPersonalDDI()
	{
		return personalDDI;
	}

	public void setPersonalDDI(String personalDDI)
	{
		this.personalDDI = personalDDI;
	}

	public String getUserId()
	{
		return userId;
	}

	public void setUserId(String userId)
	{
		this.userId = userId;
	}

	public String getUserNo()
	{
		return userNo;
	}

	public void setUserNo(String userNo) {
		this.userNo = userNo;
	}

	public String getDeviceNo()
	{
		if (getPhoneCallback() != null)
			return getPhoneCallback().getVirtualDeviceId();
		else {

			try {
				Session session = (LocalClientSession) XMPPServer.getInstance().getSessionManager().getSession(new JID(getUserId()));

				if (session == null)
					return "0.0.0.0";
				else
					return session.getHostAddress();
			}

			catch (Exception e) {

				return "0.0.0.0";
			}
		}
	}

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback)
	{
		this.callback = callback;
	}

	public OpenlinkCallback getPhoneCallback()
	{
		return phoneCallback;
	}

	public void setPhoneCallback(OpenlinkCallback phoneCallback)
	{
		this.phoneCallback = phoneCallback;
	}

	public void setCallbackActive(boolean callbackActive)
	{
		this.callbackActive = callbackActive;
	}

	public boolean getCallbackActive()
	{
		return callbackActive;
	}


	public boolean callbackAvailable(OpenlinkComponent component)
	{
		if (getPhoneCallback() != null)
		{
			if (!getCallbackActive())
			{
				//component.openlinkLinkService.activateCallback(getPhoneCallback());
			}

			return true;

		} else return false;
	}

//-------------------------------------------------------
//
//
//
//-------------------------------------------------------

	public String getHandsetNo() {
		return handsetNo;
	}

	public void setHandsetNo(String handsetNo) {
		this.handsetNo = handsetNo;
	}

	public String getHandsetCallId() {
		return handsetCallId;
	}

	public void setHandsetCallId(String handsetCallId) {
		this.handsetCallId = handsetCallId;
	}


	public String getCurretHandsetNo()
	{
		return handsetNo;
	}


	public long getSiteID() {
		return siteID;
	}

	public void setSiteID(long siteID) {
		this.siteID = siteID;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public List<OpenlinkGroup> getGroups() {
		return openlinkGroups;
	}

	public void setGroups(List<OpenlinkGroup> openlinkGroups) {
		this.openlinkGroups = openlinkGroups;
	}

	public Map<String, OpenlinkInterest> getInterests() {
		return openlinkInterests;
	}

	public void addInterest(OpenlinkInterest openlinkInterest)
	{
		if (!openlinkInterests.containsKey(openlinkInterest.getInterestId()))
		{
			this.openlinkInterests.put(openlinkInterest.getInterestId(), openlinkInterest);
		}
	}

//-------------------------------------------------------
//
//
//
//-------------------------------------------------------


	public void selectLine(OpenlinkComponent component, String line, String handset, String privacy, String hold)
	{
		try {


		}
		catch(Exception e) {
			Log.error("OpenlinkUser - selectDDI error: " + e.toString());
		}
	}

	public void selectDDI(OpenlinkComponent component, String ddi, String handset, String privacy, String hold, String dialDigopenlink)
	{
		Log.debug("OpenlinkUser - selectDDI " + ddi + " " + dialDigopenlink + " " + handset + " " + privacy);

		try {


		}
		catch(Exception e) {
			Log.error("OpenlinkUser - selectDDI error: " + e.toString());
		}
	}


    public int compareTo(Object object)
    {
        if (object instanceof OpenlinkUser) {
            return getUserId().compareTo(((OpenlinkUser)object).getUserId());
        }
        return getClass().getName().compareTo(object.getClass().getName());
    }

}
