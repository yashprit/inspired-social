package com.ifsoft.cti;


import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

import org.dom4j.Element;
import org.jivesoftware.util.Log;
import org.w3c.dom.NodeList;


public class QueryFeatures extends OpenlinkCommand {

	public QueryFeatures(OpenlinkComponent openlinkComponent) {
		super(openlinkComponent);
	}

	@Override protected boolean addStageInformation(SessionData data, Element newCommand, Element oldCommand)
	{
		return false;
	}

	@Override public Element execute(SessionData data, Element newCommand, Element oldCommand)
	{
		try {
			String profileID = oldCommand.element("iodata").element("in").element("profile").getText();

			Element iodata = newCommand.addElement("iodata", "urn:xmpp:tmp:io-data");
			iodata.addAttribute("type","output");

			Element features = iodata.addElement("out").addElement("features");

			OpenlinkUser openlinkUser = this.getOpenlinkComponent().getOpenlinkProfile(profileID);

			if (openlinkUser != null)
			{
				if (!validPermissions(data, openlinkUser.getUserId(), newCommand))
				{
					return newCommand;
				}

				String hs1 = "true";
				String hs2 = "false";

				if ("2".equals(openlinkUser.getHandsetNo()))
				{
					hs1 = "false";
					hs2 = "true";
				}

				Element feature1 = features.addElement("feature");
				feature1.addAttribute("id", "hs_1");
				feature1.addAttribute("value1", hs1);

				Element feature2 = features.addElement("feature");
				feature2.addAttribute("id", "hs_2");
				feature2.addAttribute("value1", hs2);

				Element feature3 = features.addElement("feature");
				feature3.addAttribute("id", "priv_1");
				feature3.addAttribute("value1", openlinkUser.getLastPrivacy());

				Element feature4 = features.addElement("feature");
				feature4.addAttribute("id", "fwd_1");
				feature4.addAttribute("value2", openlinkUser.getLastCallForward());
				feature4.addAttribute("value1", openlinkUser.getLastCallForwardInterest());
/*
				if (openlinkUser.getCallback() != null && this.getOpenlinkComponent().openlinkLinkService.isCallbackAvailable())
				{
					Element feature7 = features.addElement("feature");
					feature7.addAttribute("id", "callback_1");
					feature7.addAttribute("value2", openlinkUser.getCallback());
					feature7.addAttribute("value1", openlinkUser.getPhoneCallback() != null ? "true" : "false");
				}


				Element feature5 = features.addElement("feature");
				feature5.addAttribute("id", "hold_1");
				feature5.addAttribute("value1", openlinkUser.autoHold() ? "true" : "false");

				Element feature6 = features.addElement("feature");
				feature6.addAttribute("id", "icom_1");
				feature6.addAttribute("value1", openlinkUser.intercom() ? "true" : "false");
*/
			} else 	{

				Element note = newCommand.addElement("note");
				note.addAttribute("type", "error");
				note.setText("QueryFeature Error - Profile Id not found");
			}

		} catch (Exception e) {
			Log.error("[Openlink] QueryFeatures execute error "	+ e);

			Element note = newCommand.addElement("note");
			note.addAttribute("type", "error");
			note.setText("Query Features Internal error");
		}
		return newCommand;
	}

	@Override protected List<Action> getActions(SessionData data)
	{
		return Arrays.asList(new Action[] { Action.complete });
	}

	@Override public String getCode()
	{
		return "http://xmpp.org/protocol/openlink:01:00:00#query-features";
	}

	@Override public String getDefaultLabel()
	{
		return "Query Features";
	}

	@Override protected Action getExecuteAction(SessionData data)
	{
		return Action.complete;
	}

	@Override public int getMaxStages(SessionData data)
	{
		return 0;
	}

}
