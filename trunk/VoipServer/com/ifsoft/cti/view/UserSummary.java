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
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.vcard.*;

import com.ifsoft.cti.*;

import org.dom4j.*;


public class UserSummary extends HttpServlet {

	private UserManager userManager;
	protected Logger Log = Logger.getLogger(getClass().getName());


    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
		userManager = XMPPServer.getInstance().getUserManager();
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
			String userName = request.getParameter("user");
			String action = request.getParameter("action");

			String resetMessage = null;

			if ("reset".equals(action))
			{
				//resetUser(siteID, userName);
				resetMessage = "Cache reset for " + userName;
			}

			out.println("");
			out.println("<html>");
			out.println("<head>");
			out.println("<title>Users</title>");
			out.println("<meta name=\"pageID\" content=\"OPENLINK-USER-SUMMARY\"/>");
			out.println("</head>");
			out.println("<body>");

			if (resetMessage != null)
			{
				out.println("<div class='jive-success'>");
				out.println("<table cellpadding='0' cellspacing='0' border='0'>");
				out.println("<tbody>");
				out.println("<tr><td class='jive-icon'><img src='images/success-16x16.gif' width='16' height='16' border='0' alt=''></td>");
				out.println("<td class='jive-icon-label'>");
				out.println(resetMessage);
				out.println("</td></tr>");
				out.println("</tbody>");
				out.println("</table>");
				out.println("</div>");
			}

			out.println("");
			out.println("<br>");

			out.println("<table cellpadding=\"2\" cellspacing=\"2\" border=\"0\"><tr><td>Pages:[</td>");

			int linesCount = 20;
			int userCounter = userManager.getUserCount();
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

					out.println("<td><a href='openlink-user-summary?start=" + iStart + "&count=" + iCount + "'>" + page + "</a><td>");
				}
			}

			out.println("<td>]</td></tr></table>");
			out.println("<div class=\"jive-table\">");
			out.println("<table cellpadding=\"0\" cellspacing=\"0\" border=\"0\" width=\"100%\">");
			out.println("<thead>");
			out.println("<tr>");
			out.println("<th nowrap></th>");
			out.println("<th nowrap>User Name</th>");
			out.println("<th nowrap>Full Name</th>");
			out.println("<th nowrap>Office Phone</th>");
			out.println("<th nowrap>Office Mobile</th>");
			out.println("<th nowrap>Home Phone</th>");
			out.println("<th nowrap>Home Mobile</th>");
			out.println("<th nowrap>Reset Cache</th>");
			out.println("</tr>");
			out.println("</thead>");
			out.println("<tbody>");

			Collection<User> users = userManager.getUsers(pageStart, pageCount);

            Iterator it = users.iterator();
            int i = 0;

			while( it.hasNext() )
			{
				try
				{
               		User user = (User)it.next();

               		String prefix = user.getUsername().substring(0, 4);

               		if("tlp.".equals(prefix) || "tli.".equals(prefix))
               		{

					} else {

						Element vCard = VCardManager.getInstance().getVCard(user.getUsername());

						String officePhone = "&nbsp;";
						String officeMobile = "&nbsp;";
						String homePhone = "&nbsp;";
						String homeMobile = "&nbsp;";

						if (vCard != null)
						{
							officePhone = getTelVoiceNumber(vCard, "WORK", "VOICE");
							officeMobile = getTelVoiceNumber(vCard, "WORK", "CELL");
							homePhone = getTelVoiceNumber(vCard, "HOME", "VOICE");
							homeMobile = getTelVoiceNumber(vCard, "HOME", "CELL");
						}

						if(i % 2 == 1)
							out.println("<tr class=\"jive-odd\">");
						else
							out.println("<tr class=\"jive-even\">");

						out.println("<td width=\"1%\">");
						out.println((pageStart + i + 1));
						out.println("</td>");
						out.println("<td width=\"9%\">");
						out.println(user.getUsername());
						out.println("</td>");
						out.println("<td width=\"20%\">");
						out.println(user.getName());
						out.println("</td>");
						out.println("<td width=\"15%\">");
						out.println(officePhone == null ? "&nbsp;" : officePhone);
						out.println("</td>");
						out.println("<td width=\"15%\">");
						out.println(officeMobile == null ? "&nbsp;" : officeMobile);
						out.println("</td>");
						out.println("<td width=\"15%\">");
						out.println(homePhone == null ? "&nbsp;" : homePhone);
						out.println("</td>");
						out.println("<td width=\"15%\">");
						out.println(homeMobile == null ? "&nbsp;" : homeMobile);
						out.println("</td>");
						out.println("<td width=\"10%\">");
						out.println("<a href='openlink-user-summary?action=reset&user=" + user.getUsername() + "&start=" + pageStart + "&count=" + pageCount + "'><img src=\"images/refresh-16x16.gif\" alt=\"Reset User Cache\" border=\"0\"></a>");
						out.println("</td>");
						out.println("</tr>");

						i++;
					}
				}
				catch(Exception e)
				{
					Log.error("UserSummary " + e);
					e.printStackTrace();
				}
			}
			out.println("</tbody>");
			out.println("</table>");
			out.println("</div>");
			out.println("</body>");
			out.println("</html>");

        }
        catch (Exception e) {
        	Log.error("UserSummary " + e);
        	e.printStackTrace();
        }
	}

	private String getTelVoiceNumber(Element vCard, String work, String voice)
	{
		String telVoiceNumber = null;

		for ( Iterator i = vCard.elementIterator( "TEL" ); i.hasNext(); )
		{
			Element tel = (Element) i.next();
			//Log.debug( "["+ site.getName() + "] getTelVoiceNumber - tel " + tel.asXML());

			if (tel.element(work) != null && tel.element(voice) != null)
			{
				Element number = tel.element("NUMBER");

				if (number != null)
				{
					//Log.debug( "["+ site.getName() + "] getTelVoiceNumber - number " + number.getText());
					telVoiceNumber = number.getText();
					break;
				}
			}
		}

		return telVoiceNumber;
	}

}

