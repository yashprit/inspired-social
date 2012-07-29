/**
 * Copyright (c) 2009, Brian Showalter
 * All rights reserved.
 * ====================================================================
 * Licensed under the BSD License. Text as follows.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 *   - Neither the name brianshowalter.com nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */

package com.brianshowalter.drupalrewrite;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;


/**
 * Custom match class to handle processing of Drupal clean URLs that match the
 * criteria for rewriting.
 *
 * @author Brian Showalter
 * @version 0.1  2009-10-01
 */
public class DrupalMatch extends RewriteMatch {

	/**
	 * Do the actual rewrite.  Request URI in the form "/node/3" would be rewritten
	 * to "/index.php?q=node/3" and then forwarded.
	 */
	public boolean execute(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String queryString = request.getQueryString();
    	// Do the rewrite

		StringBuilder newURI = new StringBuilder(512);

		if (request.getRequestURI().indexOf("/events/") > -1)
		{
			newURI.append("/index.php?post_type=ep_event&q=").append(request.getRequestURI().substring(1));

		} else {

			newURI.append("/index.php?q=").append(request.getRequestURI().substring(1));
		}

		if (queryString != null) {

			newURI.append("&").append(request.getQueryString());
		}
		System.out.println("changes = " + newURI.toString());

    	RequestDispatcher rd = request.getRequestDispatcher(newURI.toString());
    	rd.forward(request, response);

		return true;
	}
}
