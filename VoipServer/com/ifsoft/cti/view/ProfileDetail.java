package com.ifsoft.cti.view;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;

import org.jivesoftware.util.Log;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.CacheFactory;
import org.jivesoftware.openfire.XMPPServer;

import com.ifsoft.cti.*;
import org.red5.server.webapp.voicebridge.Application;

import org.xmpp.packet.JID;


public class ProfileDetail extends HttpServlet
{
	protected Logger Log = Logger.getLogger(getClass().getName());


    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
    }

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");
		response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
		response.addHeader("Cache-Control", "post-check=0, pre-check=0");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Content-Type", "text/html");
		response.setHeader("Connection", "close");

		ServletOutputStream out = response.getOutputStream();

		try {
			String userKey = request.getParameter("user");
			String siteID = request.getParameter("site");
			String siteName = request.getParameter("siteName");
			String action = request.getParameter("action");
			String vMesssage = request.getParameter("vmsg");
			String vmComment = request.getParameter("vmComment");
			String vmName = request.getParameter("vmName");
			String vmLabel = request.getParameter("vmLabel");

			OpenlinkComponent openlinkComponent = (OpenlinkComponent) Application.component;

			out.println("");
			out.println("<html>");
			out.println("<head>");
			out.println("  <title>User Interests</title>");
			out.println("  <meta name=\"pageID\" content=\"OPENLINK-PROFILE-SUMMARY\"/>");
			out.println("  <script>function doCollapse(id){if(document.getElementById(id).style.display == \"none\"){document.getElementById(id).style.display = \"\";}else{document.getElementById(id).style.display = \"none\";}}</script>");
			out.println("</head>");
			out.println("<body>");
			out.println("<br>");

			if (openlinkComponent.openlinkUserTable.containsKey(userKey))
			{
				OpenlinkUser openlinkUser = openlinkComponent.openlinkUserTable.get(userKey);

				if (action != null)
				{
					if ("play".equals(action))
					{

					}

					else

					if ("edit".equals(action))
					{

					}


					else

					if ("create".equals(action))
					{

					}

					else

					if ("delete".equals(action))
					{

					}

					Thread.sleep(1000);
					response.sendRedirect("openlink-profile-detail?user=" + userKey + "&site=" + siteID + "&siteName=" + siteName);
				}


				out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\"><tr valign='top'><td colspan='2'><div id='jive-title'>" + openlinkUser.getProfileName() + "&nbsp;&nbsp;(" + openlinkUser.getUserName() + ")</div></td></tr>");
				out.println("<tr valign='top'><td><div id='jive-title'></div></td></tr>");

/*
				if (openlinkComponent.openlinkVmsService.isTelephonyServerConnected())
				{
					out.println("<tr valign='top'><td colspan='2'>");

					out.println("<div align='left' id='jive-title'>Voice Message Features</div>");
					out.println("<div align='right'><input type='text' size='50' name='msgComment' id='msgComment' /><button onClick=\"location.href='openlink-profile-detail?user=" + userKey + "&site=" + siteID + "&siteName=" + siteName + "&action=create&vmComment=' + document.getElementById(&quot;msgComment&quot;).value;\">Create Message</button></div>");
					out.println("<div class=\"jive-table\">");
					out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
					out.println("<thead>");
					out.println("<tr>");
					out.println("<th nowrap>Id</th>");
					out.println("<th nowrap>Label</th>");
					out.println("<th nowrap>Creation Date</th>");
					out.println("<th nowrap>Modification Date</th>");
					out.println("<th nowrap>Path</th>");
					out.println("<th nowrap>Archived</th>");
					out.println("<th nowrap>Edit</th>");
					out.println("<th nowrap>Delete</th>");
					out.println("</tr>");
					out.println("</thead>");
					out.println("<tbody>");

					Iterator<VMessage> iter4 = openlinkComponent.openlinkVmsService.getVMessages(openlinkUser).iterator();
					int i = 1;

					while( iter4.hasNext() )
					{
						VMessage message = (VMessage)iter4.next();

						try
						{
							if(i % 2 == 1)
								out.println("<tr valign='top' class=\"jive-odd\">");
							else
								out.println("<tr  valign='top' class=\"jive-even\">");

							out.println("<td width=\"1%\">");
							out.println(message.getName().substring(message.getName().indexOf("vm_")+3));
							out.println("</td>");
							out.println("<td width=\"34%\">");
							out.println("<a href='openlink-profile-detail?user=" + userKey + "&site=" + siteID + "&siteName=" + siteName + "&action=play&vmsg=" + message.getId() + "&vmName=" + message.getName() + "'>" + message.getComment() + "</a>");
							out.println("</td>");

							out.println("<td width=\"20%\">");
							out.println(String.valueOf(message.getCreationDate()));
							out.println("</td>");

							out.println("<td width=\"20%\">");
							out.println(String.valueOf(message.getModificationDate()));
							out.println("</td>");

							out.println("<td width=\"10%\">");
							out.println(message.getPath());
							out.println("</td>");

							out.println("<td width=\"5%\">");
							out.println(message.isArchived() ? "<img src=\"images/success-16x16.gif\" border=\"0\">" : "&nbsp;");
							out.println("</td>");


							if (openlinkComponent.openlinkVmsService.hasEditPermissions(openlinkUser.getUserId(), message.getName()))
							{
								out.println("<td width=\"5%\">");
								out.println("<a href=\"javascript:location.href='openlink-profile-detail?user=" + userKey + "&site=" + siteID + "&siteName=" + siteName + "&action=edit&vmsg=" + message.getId() + "&vmComment=" + message.getComment() + "&vmName=" + message.getName() + "&vmLabel=' + document.getElementById(&quot;msgComment&quot;).value;\"><img src=\"images/edit-16x16.gif\" alt=\"Edit Message\" border=\"0\"></a>");
								out.println("</td>");

								out.println("<td width=\"5%\">");
								out.println("<script>function doVMsgDelete" + message.getId() + "(){if(confirm('Do you wish to delete " + message.getComment() + "')){location.href='openlink-profile-detail?user=" + userKey + "&site=" + siteID + "&siteName=" + siteName + "&action=delete&vmsg=" + message.getId() + "&vmName=" + message.getName() + "'}}</script><a href='javascript:doVMsgDelete" + message.getId() + "()'><img src=\"images/delete-16x16.gif\" alt=\"Delete Message\" border=\"0\"</a>");
								out.println("</td>");


							} else {

								out.println("<td width=\"5%\">");
								out.println("&nbsp;");
								out.println("</td>");

								out.println("<td width=\"5%\">");
								out.println("&nbsp;");
								out.println("</td>");
							}

							out.println("</tr>");

							i++;
						}
						catch(Exception e)
						{

						}
					}
					out.println("</tbody>");
					out.println("</table>");
					out.println("</div>");
					out.println("<p></p>");

					out.println("</td></tr><tr><td>&nbsp;<br/></td></tr>");
				}
*/

				out.println("<tr valign='top'><td><div id='jive-title'><center>Direct Lines</center></div>");
				out.println("<div class=\"jive-table\">");
				out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
				out.println("<thead>");
				out.println("<tr>");
				out.println("<th nowrap>Interest Id</th>");
				out.println("<th nowrap>Label</th>");
				//out.println("<th nowrap>Line</th>");
				out.println("<th nowrap>Subs</th>");
				out.println("<th nowrap>Calls</th>");
				out.println("</tr>");
				out.println("</thead>");
				out.println("<tbody>");

				Iterator<OpenlinkInterest> iter = openlinkUser.getInterests().values().iterator();
				int i = 1;

				while( iter.hasNext() )
				{
					OpenlinkInterest openlinkInterest = (OpenlinkInterest)iter.next();

					try
					{
						if ("L".equals(openlinkInterest.getInterestType()))
						{
							if(i % 2 == 1)
								out.println("<tr valign='top' class=\"jive-odd\">");
							else
								out.println("<tr  valign='top' class=\"jive-even\">");

							String openlinkInterestKey = openlinkInterest.getInterestId() + openlinkUser.getUserNo();
							OpenlinkUserInterest openlinkUserInterest = openlinkInterest.getUserInterests().get(openlinkUser.getUserNo());
							int callCount = openlinkUserInterest.getCalls().size();
							int subscriberCount = openlinkUserInterest.getSubscribers().size();

							out.println("<td width=\"10%\">");
							out.println("<a href='openlink-interest-detail?interest=" + openlinkInterestKey + "'>" + openlinkInterest.getInterestId() + "</a>");
							out.println("</td>");
							out.println("<td width=\"60%\">");
							out.println(openlinkInterest.getInterestLabel());
							out.println("</td>");
							//out.println("<td width=\"10%\">");
							//out.println(openlinkInterest.getInterestValue());
							//out.println("</td>");
							out.println("<td width=\"10%\">");
							out.println(subscriberCount == 0 ? "" : String.valueOf(subscriberCount));
							out.println("</td>");

							if (callCount == 0)
							{
								out.println("<td width=\"20\">&nbsp;</td>");

							} else {
								out.println("<td style='background-color:#4dc027;text-align:center' width=\"20\"><font color='#ffffff'>");
								out.println(String.valueOf(callCount));
								out.println("</font></td>");
							}

							out.println("</tr>");

							i++;
						}
					}
					catch(Exception e)
					{

					}
				}
				out.println("</tbody>");
				out.println("</table>");
				out.println("</div>");
				out.println("<p></p>");

				out.println("</td><td>");

				out.println("<div id='jive-title'><center>Directory Numbers</center></div>");
				out.println("<div class=\"jive-table\">");
				out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
				out.println("<thead>");
				out.println("<tr>");
				out.println("<th nowrap>Interest Id</th>");
				out.println("<th nowrap>Label</th>");
				//out.println("<th nowrap>DN</th>");
				out.println("<th nowrap>Callset</th>");
				out.println("<th nowrap>Subs</th>");
				out.println("<th nowrap>Max</th>");
				out.println("<th nowrap>Calls</th>");
				out.println("<th nowrap>Fwd</th>");
				out.println("<th nowrap>Def</th>");
				out.println("</tr>");
				out.println("</thead>");
				out.println("<tbody>");

				iter = openlinkUser.getInterests().values().iterator();
				i = 1;

				while( iter.hasNext() ){
					OpenlinkInterest openlinkInterest = (OpenlinkInterest)iter.next();

					try
					{
						if ("D".equals(openlinkInterest.getInterestType()))
						{
							if(i % 2 == 1)
								out.println("<tr valign='top' class=\"jive-odd\">");
							else
								out.println("<tr valign='top' class=\"jive-even\">");

							String openlinkInterestKey = openlinkInterest.getInterestId()  + openlinkUser.getUserNo();
							OpenlinkUserInterest openlinkUserInterest = openlinkInterest.getUserInterests().get(openlinkUser.getUserNo());
							int callCount = openlinkUserInterest.getCalls().size();
							int max = openlinkUserInterest.getMaxNumCalls();
							int subscriberCount = openlinkUserInterest.getSubscribers().size();

							out.println("<td width=\"10%\">");
							out.println("<a href='openlink-interest-detail?interest=" + openlinkInterestKey + "'>" + openlinkInterest.getInterestId() + "</a>");
							out.println("</td>");
							out.println("<td width=\"30%\">");
							out.println(openlinkInterest.getInterestLabel());
							out.println("</td>");
							//out.println("<td width=\"10%\">");
							//out.println(openlinkInterest.getInterestValue());
							//out.println("</td>");
							out.println("<td width=\"10%\">");
							out.println(openlinkInterest.getCallset() == null ? "" : openlinkInterest.getCallset());
							out.println("</td>");
							out.println("<td width=\"10%\">");
							out.println(subscriberCount == 0 ? "" : String.valueOf(subscriberCount));
							out.println("</td>");
							out.println("<td width=\"10%\">");
							out.println(max == 0 ? "" : String.valueOf(max));
							out.println("</td>");

							if (callCount == 0)
							{
								out.println("<td width=\"10%\">&nbsp;</td>");

							} else {
								out.println("<td style='background-color:#4dc027;text-align:center' width=\"10%\"><font color='#ffffff'>");
								out.println(String.valueOf(callCount));
								out.println("</font></td>");
							}

							out.println("<td width=\"5%\">");
							out.println("true".equals(openlinkUserInterest.getCallFWD()) ? "<img src=\"images/success-16x16.gif\" alt='" + openlinkInterest.getUserInterests().get(openlinkUser.getUserNo()).getCallFWDDigits() + "' border=\"0\">" : "&nbsp;");
							out.println("</td>");

							out.println("<td width=\"5%\">");
							out.println("true".equals(openlinkUserInterest.getDefault()) ? "<img src=\"images/success-16x16.gif\" alt=\"Yes\" border=\"0\">" : "&nbsp;");
							out.println("</td>");

							out.println("</tr>");
							i++;
						}
					}
					catch(Exception e)
					{

					}
				}
				out.println("</tbody>");
				out.println("</table>");
				out.println("</div>");
				out.println("<p></p>");

				out.println("</td></tr><tr valign='top'><td>");

				out.println("<div id='jive-title'><center>Speed Dial Features</center></div>");
				out.println("<div class=\"jive-table\">");
				out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
				out.println("<thead>");
				out.println("<tr>");
				out.println("<th nowrap>Feature Id</th>");
				out.println("<th nowrap>Label</th>");
				out.println("<th nowrap>Dialable Number</th>");
				out.println("</tr>");
				out.println("</thead>");
				out.println("<tbody>");
/*
				Iterator<OpenlinkKeypage> iter2 = openlinkUser.getKeypages().iterator();
				i = 1;

				while( iter2.hasNext() ){
					OpenlinkKeypage openlinkKeypage = (OpenlinkKeypage)iter2.next();

					try
					{
						Iterator it2 = openlinkKeypage.getKeys().iterator();

						while( it2.hasNext() )
						{
							OpenlinkKey openlinkKey = (OpenlinkKey)it2.next();

							if ("3".equals(openlinkKey.getTurretFunction()))
							{
								if(i % 2 == 1)
									out.println("<tr valign='top' class=\"jive-odd\">");
								else
									out.println("<tr valign='top' class=\"jive-even\">");

								String label = openlinkKey.getLongLabel();
								String featureId = openlinkKey.getQualifier();
								String dialableNumber = "";
								String canonicalNumber = "";

								if (openlinkComponent.openlinkLdapService.speedDialTable.containsKey(featureId))
								{
									OpenlinkSpeedDial openlinkSpeedDial = openlinkComponent.openlinkLdapService.speedDialTable.get(featureId);
									dialableNumber = openlinkSpeedDial.getDialableNumber();
									canonicalNumber = openlinkSpeedDial.getCanonicalNumber();
								}

								out.println("<td width=\"10%\">");
								out.println(featureId);
								out.println("</td>");
								out.println("<td width=\"70%\">");
								out.println(label);
								out.println("</td>");
								out.println("<td width=\"20%\">");
								out.println(dialableNumber);
								out.println("</td>");

								out.println("</tr>");
								i++;
							}
						}
					}
					catch(Exception e)
					{

					}
				}
*/
				out.println("</tbody>");
				out.println("</table>");
				out.println("</div>");
				out.println("<p></p>");

				out.println("</td><td>");

				out.println("<div id='jive-title'><center>Intercom Group Features</center></div>");
				out.println("<div class=\"jive-table\">");
				out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
				out.println("<thead>");
				out.println("<tr>");
				out.println("<th nowrap>Feature Id</th>");
				out.println("<th nowrap>Label</th>");
				out.println("</tr>");
				out.println("</thead>");
				out.println("<tbody>");

				Iterator<OpenlinkGroup> iter3 = openlinkUser.getGroups().iterator();
				i = 1;

				while( iter3.hasNext() )
				{
					OpenlinkGroup openlinkGroup = (OpenlinkGroup)iter3.next();

					try
					{
						if(i % 2 == 1)
							out.println("<tr valign='top' class=\"jive-odd\">");
						else
							out.println("<tr  valign='top' class=\"jive-even\">");

						out.println("<td width=\"20%\">");
						out.println(openlinkGroup.getGroupID());
						out.println("</td>");
						out.println("<td width=\"80%\">");
						out.println(openlinkGroup.getName());
						out.println("</td>");;

						out.println("</tr>");

						i++;
					}
					catch(Exception e)
					{

					}
				}
				out.println("</tbody>");
				out.println("</table>");
				out.println("</div>");
				out.println("<p></p>");

				out.println("</td></tr></table>");
			}

			out.println("</body>");
			out.println("</html>");
        }
        catch (Exception e) {
        	Log.error(e);
        }
	}
}

