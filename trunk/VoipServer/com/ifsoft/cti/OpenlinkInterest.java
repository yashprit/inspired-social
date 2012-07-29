package com.ifsoft.cti;


import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collection;
import java.io.*;

import org.jivesoftware.util.Log;


public class OpenlinkInterest {

	private String interestType 			= "";
	private String interestValue			= "";
	private String callset 					= null;
	private String interestLabel			= "";
	private String siteName					= "";
	private String defaultInterest 			= "false";

	private Map<String, OpenlinkUserInterest> openlinkUserInterests 	= new HashMap();


//-------------------------------------------------------
//
//
//
//-------------------------------------------------------

	public String getDefault() {
		return defaultInterest;
	}

	public void setDefault(String defaultInterest) {
		this.defaultInterest = defaultInterest;
	}

	public String getInterestId() {
		return interestValue;
	}

	public String getInterestType() {
		return interestType;
	}

	public void setInterestType(String interestType) {
		this.interestType = interestType;
	}

	public String getInterestLabel() {
		return interestLabel;
	}

	public void setInterestLabel(String interestLabel) {
		this.interestLabel = interestLabel;
	}

	public String getInterestValue() {
		return interestValue;
	}

	public void setInterestValue(String interestValue) {
		this.interestValue = interestValue;
	}

	public String getCallset() {
		return callset;
	}

	public void setCallset(String callset) {
		this.callset = callset;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public OpenlinkUserInterest addUserInterest(OpenlinkUser openlinkUser, String defaultInterest)
	{
		OpenlinkUserInterest openlinkUserInterest = null;

		if (!openlinkUserInterests.containsKey(openlinkUser.getUserNo()))
		{
			openlinkUserInterest = new OpenlinkUserInterest();
			openlinkUserInterest.setUser(openlinkUser);
			openlinkUserInterest.setInterest(this);
			openlinkUserInterest.setDefault(defaultInterest);

			this.openlinkUserInterests.put(openlinkUser.getUserNo(), openlinkUserInterest);

		} else {

			openlinkUserInterest = openlinkUserInterests.get(openlinkUser.getUserNo());
		}

		return openlinkUserInterest;
	}

	public Map<String, OpenlinkUserInterest> getUserInterests()
	{
		return openlinkUserInterests;
	}

	public void setInterests(Map<String, OpenlinkUserInterest> openlinkUserInterests)
	{
		this.openlinkUserInterests = openlinkUserInterests;
	}
}
