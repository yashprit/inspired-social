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

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tuckey.web.filters.urlrewrite.extend.RewriteMatch;
import org.tuckey.web.filters.urlrewrite.extend.RewriteRule;


/**
 * Custom rule class to determine if a request URI meets the Drupal
 * criteria for rewriting.
 *
 * @author Brian Showalter
 * @version 0.1  2009-10-01
 */
public class DrupalRule extends RewriteRule {
	private ServletContext sc;

	/**
	 * Initialization method - saves the ServletContext object so that
	 * it can be used later to determine the actual filesystem path
	 * to a requested object.
	 *
	 * @param sc The ServletContext object.
	 * @return true
	 */
	public boolean init(ServletContext sc) {
		this.sc = sc;

		return true;
	}


	/**
	 * Performs the actual testing to determine if the request URL is to be rewritten.
	 *
	 * @param request The HttpServletRequest object.
	 * @param response The HttpServletResponse object.
	 * @return RewriteMatch object which is to perform the actual rewrite.
	 */
	public RewriteMatch matches(HttpServletRequest request, HttpServletResponse response) {
		String requestURI = request.getRequestURI();

		if (requestURI == null) return null;

		if (requestURI.equals("/")) return null;

		if (requestURI.equals("/favicon.ico")) return null;

		if (requestURI.indexOf("screenshare?stream=") > -1) return null;

		if (requestURI.indexOf("/inspired/video/") > -1) return null;

		if (requestURI.indexOf("\\inspired\\video\\") > -1) return null;

		// No rewrite if real path cannot be obtained, or if request URI points to a
		// physical file or directory

		String realPath = sc.getRealPath(requestURI);

		if (realPath == null) return new DrupalMatch();

		int pos = realPath.indexOf("\\inspired\\inspired");

		if (pos > -1)
		{
			realPath = realPath.substring(0, pos) + "\\inspired" + realPath.substring(pos + 18);
		}

		int pos2 = realPath.indexOf("/inspired/inspired");

		if (pos2 > -1)
		{
			realPath = realPath.substring(0, pos2) + "/inspired" + realPath.substring(pos2 + 18);
		}

		File f = new File(realPath);

		System.out.println("matches check " + requestURI + " " + realPath + " " + f.isFile() + " " + f.isDirectory());

		if (f.isFile() || f.isDirectory() || f.isHidden()) return null;

		// Return the RewriteMatch object
		return new DrupalMatch();
	}
}
