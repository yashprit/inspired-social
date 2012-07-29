package com.ifsoft.cti.view;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import org.jivesoftware.util.Log;
import org.jivesoftware.util.cache.Cache;
import org.jivesoftware.util.cache.CacheFactory;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserNotFoundException;

import com.ifsoft.cti.*;
import org.red5.server.webapp.voicebridge.Application;

public class ProfileSummary extends HttpServlet {

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

		String callback = request.getParameter("callback");
		String remove = request.getParameter("remove");
		String userNo = request.getParameter("userno");

		try {
			OpenlinkComponent openlinkComponent = (OpenlinkComponent) Application.component;

			if (remove != null && userNo != null)
			{
				//openlinkComponent.openlinkLinkService.freeCallback(userNo);
				response.sendRedirect("openlink-profile-summary?");
			}

			out.println("");
			out.println("<html>");
			out.println("    <head>");
			out.println("        <title>User Profiles</title>");
			out.println("        <meta name=\"pageID\" content=\"OPENLINK-PROFILE-SUMMARY\"/>");
			out.println("    </head>");
			out.println("    <body>");
			out.println("");
			out.println("<br>");

			out.println("<table cellpadding=\"2\" cellspacing=\"2\" border=\"0\"><tr><td>Pages:[</td>");

			int linesCount = 20;
			int userCounter = openlinkComponent.getUserCount();
			int pageCounter = (userCounter/linesCount);

			pageCounter = userCounter > (linesCount * pageCounter) ? pageCounter + 1 : pageCounter;

			String start = request.getParameter("start");
			String count = request.getParameter("count");

			int pageStart = start == null ? 0 : Integer.parseInt(start);
			int pageCount = count == null ? linesCount : Integer.parseInt(count);


			for (int i=0; i<pageCounter; i++)
			{
				int iStart = (i * linesCount);
				int iCount = ((i * linesCount) + linesCount) > userCounter ? ((i * linesCount) + linesCount) - userCounter : linesCount;
				int page = i + 1;

				if (pageStart == iStart)
				{
					out.println("<td>" + page + "<td>");

				} else {

					out.println("<td><a href='openlink-profile-summary?start=" + iStart + "&count=" + iCount + "'>" + page + "</a><td>");
				}
			}

			out.println("<td>]</td></tr></table>");
			out.println("<div class=\"jive-table\">");
			out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
			out.println("<thead>");
			out.println("<tr>");
			out.println("<th nowrap></th>");
			out.println("<th nowrap>Id</th>");
			out.println("<th nowrap>User Id</th>");
			out.println("<th nowrap>Device</th>");
			out.println("<th nowrap>HS</th>");
			out.println("<th nowrap>Route</th>");
			out.println("<th nowrap>Name</th>");
			out.println("<th nowrap>Dir No</th>");
			out.println("<th nowrap></th>");
			out.println("<th nowrap>Callback</th>");
			out.println("<th nowrap>Hold</th>");
			out.println("<th nowrap>Priv</th>");
			out.println("<th nowrap>Default</th>");
			out.println("</tr>");
			out.println("</thead>");
			out.println("<tbody>");

			List<OpenlinkUser> sortedProfiles = openlinkComponent.getUsers(pageStart, pageCount);

			Iterator it = sortedProfiles.iterator();

			int i = 0;

			while( it.hasNext() )
			{
				OpenlinkUser openlinkUser = (OpenlinkUser)it.next();

				try
				{
					if (XMPPServer.getInstance().getUserManager().isRegisteredUser(openlinkUser.getUserId()))
					{
						if (callback != null && userNo != null && userNo.equals(openlinkUser.getUserNo()))
						{
							//if (openlinkComponent.openlinkLinkService.getCallback(openlinkUser) == null)
							//	openlinkComponent.openlinkLinkService.allocateCallback(openlinkUser);
							//else
							//	openlinkComponent.openlinkLinkService.freeCallback(openlinkUser.getUserNo());

							Thread.sleep(1000);

							response.sendRedirect("openlink-profile-summary");
						}

						User user = XMPPServer.getInstance().getUserManager().getUser(openlinkUser.getUserId());

						if(i % 2 == 1)
							out.println("<tr valign='top' class=\"jive-odd\">");
						else
							out.println("<tr valign='top' class=\"jive-even\">");

						out.println("<td width=\"1%\">");
						out.println((pageStart + i + 1));
						out.println("</td>");
						out.println("<td width=\"11%\">");
						out.println("<a href='openlink-profile-detail?user=" + openlinkUser.getProfileName() + "'>" + openlinkUser.getProfileName() + "</a>");
						out.println("</td>");
						out.println("<td width=\"6%\">");
						out.println(openlinkUser.getUserId());
						out.println("</td>");

						if (openlinkUser.getDeviceNo() != null && !"0.0.0.0".equals(openlinkUser.getDeviceNo()))
						{
							out.println("<td width=\"6%\">");
							out.println(openlinkUser.getDeviceNo());
							out.println("</td>");

						} else {
							out.println("<td style='background-color:#c04d27' width=\"6%\"><font color='#ffffff'>");
							out.println("offline");
							out.println("</font></td>");
						}

						out.println("<td width=\"4%\">");
						out.println(openlinkUser.getHandsetNo());
						out.println("</td>");
						out.println("<td width=\"6%\">");
						out.println(openlinkUser.getCallset() == null ? "&nbsp;" : openlinkUser.getCallset());
						out.println("</td>");

						out.println("<td width=\"21%\">");
						out.println(openlinkUser.getUserName());
						out.println("</td>");
						out.println("<td width=\"6%\">");
						out.println(openlinkUser.getPersonalDDI() == null ? "&nbsp;" : openlinkUser.getPersonalDDI());
						out.println("</td>");

						String callbackActive = openlinkUser.getPhoneCallback() != null ? "<img src=\"images/success-16x16.gif\" alt=\"Yes\" border=\"0\">" : "&nbsp;";
						String callbackLink =  "<a href='openlink-profile-summary?callback=" + openlinkUser.getCallback() + "&userno=" + openlinkUser.getUserNo() + "'>" + openlinkUser.getCallback() + "</a>";

						out.println("<td width=\"1%\">");
						out.println(callbackActive);
						out.println("</td>");

						out.println("<td width=\"5%\">");
						out.println(openlinkUser.getCallback() == null ? "&nbsp;" : callbackLink);
						out.println("</td>");

						out.println("<td width=\"6%\">");
						out.println(openlinkUser.autoHold() ? "<img src=\"images/success-16x16.gif\" alt=\"Yes\" border=\"0\">" : "&nbsp;");
						out.println("</td>");
						out.println("<td width=\"6%\">");
						out.println(openlinkUser.autoPrivate() ? "<img src=\"images/success-16x16.gif\" alt=\"Yes\" border=\"0\">" : "&nbsp;");
						out.println("</td>");
						out.println("<td width=\"6%\">");
						out.println("true".equals(openlinkUser.getDefault()) ? "<img src=\"images/success-16x16.gif\" alt=\"Yes\" border=\"0\">" : "&nbsp;");
						out.println("</td>");
						out.println("</tr>");

						i++;

					} else Log.warn( "ProfileSummary - ignoring Openlink User " + openlinkUser.getUserId());
				}
				catch(Exception e)
				{
					Log.error( "ProfileSummary " + e);
					e.printStackTrace();
				}
			}
			out.println("<tr>");
			out.println("<td>&nbsp;</td>");
			out.println("</tr>");
			out.println("</tbody>");
			out.println("</table>");
			out.println("</div>");
			out.println("<p>&nbsp;</p>");
/*
			if (openlinkComponent.openlinkLinkService != null && openlinkComponent.openlinkLinkService.isCallbackAvailable())
			{
				out.println("<div id='jive-title'>Virtual Devices (VSC)</div>");
				out.println("<div class=\"jive-table\">");
				out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
				out.println("<thead>");
				out.println("<tr>");
				out.println("<th nowrap>Device</th>");
				out.println("<th nowrap>Local<br>Handset</th>");
				out.println("<th nowrap>Remote<br>Handset</th>");
				out.println("<th nowrap>User<br/>Id</th>");
				out.println("<th nowrap>User Name</th>");
				out.println("<th nowrap>Destination</th>");
				out.println("<th nowrap>Timestamp</th>");
				out.println("<th nowrap>Callback</th>");
				out.println("<th nowrap>Third<br/>Party</th>");
				out.println("<th nowrap>Active</th>");
				out.println("<th nowrap>Remove</th>");
				out.println("</tr>");
				out.println("</thead>");
				out.println("<tbody>");

				it = openlinkComponent.openlinkLinkService.getCallbacks().values().iterator();
				i = 0;

				while( it.hasNext() )
				{
					OpenlinkCallback openlinkCallback = (OpenlinkCallback)it.next();

					if(i % 2 == 1)
						out.println("<tr valign='top' class=\"jive-odd\">");
					else
						out.println("<tr valign='top' class=\"jive-even\">");


					out.println("<td width=\"4%\">");
					out.println(String.valueOf(Long.parseLong(openlinkCallback.getVirtualDeviceId())));
					out.println("</td>");

					out.println("<td width=\"5%\">");
					out.println(openlinkCallback.getLocalHandset());
					out.println("</td>");

					out.println("<td width=\"5%\">");
					out.println(openlinkCallback.getRemoteHandset());
					out.println("</td>");

					out.println("<td width=\5%\">");
					out.println(openlinkCallback.getOpenlinkUser() == null ? "&nbsp;" : openlinkCallback.getOpenlinkUser().getUserId());
					out.println("</td>");

					out.println("<td width=\"20%\">");
					out.println(openlinkCallback.getOpenlinkUser() == null ? "&nbsp;" : openlinkCallback.getOpenlinkUser().getUserName());
					out.println("</td>");

					out.println("<td width=\"10%\">");
					out.println(openlinkCallback.getOpenlinkUser() == null ? "&nbsp;" : openlinkCallback.getOpenlinkUser().getCallback());
					out.println("</td>");

					out.println("<td width=\"33%\">");
					out.println(openlinkCallback.getTimestamp() == null ? "&nbsp;" : String.valueOf(openlinkCallback.getTimestamp()));
					out.println("</td>");

					out.println("<td width=\"2%\">");
					out.println(openlinkCallback.getOpenlinkUser() != null && openlinkCallback.getOpenlinkUser().getUserType().equals("Openlink") ? "<img src=\"images/success-16x16.gif\" alt=\"Yes\" border=\"0\">" : "&nbsp;");
					out.println("</td>");

					out.println("<td width=\"2%\">");
					out.println(openlinkCallback.getOpenlinkUser() != null && openlinkCallback.getOpenlinkUser().getUserType().equals("VSC")  ? "<img src=\"images/success-16x16.gif\" alt=\"Yes\" border=\"0\">" : "&nbsp;");
					out.println("</td>");

					out.println("<td width=\"2%\">");
					out.println(openlinkCallback.getOpenlinkUser() != null && openlinkCallback.getOpenlinkUser().getCallbackActive() ? "<img src=\"images/success-16x16.gif\" alt=\"Yes\" border=\"0\">" : "&nbsp;");
					out.println("</td>");

					out.println("<td width=\"2%\">");

					if (openlinkCallback.getOpenlinkUser() == null)
					{
						out.println("&nbsp;");

					} else {
						out.println("<script>function doVSCRemove" + openlinkCallback.getVirtualDeviceId() + "(){if(confirm('Do you wish to delete device " + openlinkCallback.getVirtualDeviceId() + "')){location.href='openlink-profile-summary?remove=" + openlinkCallback.getOpenlinkUser().getCallback() + "&userno=" + openlinkCallback.getOpenlinkUser().getUserNo() + "&start=" + pageStart + "&count=" + pageCount + "'}}</script><a href='javascript:doVSCRemove" + openlinkCallback.getVirtualDeviceId() + "()'><img src=\"images/delete-16x16.gif\" alt=\"Delete Message\" border=\"0\"</a>");
					}
					out.println("</td>");


					out.println("</tr>");

				}
				out.println("</tbody>");
				out.println("</table>");
				out.println("</div>");
			}
*/
			out.println("<p></p>");
			out.println("</body>");
			out.println("</html>");

        }
        catch (Exception e) {
			Log.error( "ProfileSummary " + e);
			e.printStackTrace();

        }
	}
}

