/*

Jappix - An open social platform
These are the links JS script for Jappix

-------------------------------------------------

License: AGPL
Authors: Valérian Saliou, Maranda
Last revision: 24/06/11

*/

// Apply links in a string
function applyLinks(string, mode, style) {
	// Special stuffs
	var style, target;
	
	// Links style
	if(!style)
		style = '';
	else
		style = ' style="' + style + '"';
	
	// Open in new tabs
	if(mode != 'xhtml-im')
		target = ' target="_blank"';
	else
		target = '';
	
	if (string.indexOf('/video/') > -1)
	{
		var type = "video-chat";
		var prompt = "Open video confence";
		
		if (string.indexOf('redfire_2way.html') > -1)
		{
			type = "video-chat";
		}
		
		if (string.indexOf('redfire_video.html') > -1)
		{
			type = "video-groupchat";
			prompt = "Open group video confence";
		}

		if (string.indexOf('screenviewer.html') > -1)
		{
			type = "screen-share-viewer";
			prompt = "Open screen share";
		}
		
		string = '<a href="javascript:top.openURL(&quot;' + string + '&quot;,&quot;' +  type + '&quot;)"' + style + '><img src="' + JAPPIX_STATIC + 'php/get.php?t=img&amp;f=others/conference.png"/>&nbsp;' + prompt + '</a>';
	
	} else {
		// XMPP address
		string = string.replace(/(\s|<br \/>|^)(([a-zA-Z0-9\._-]+)@([a-zA-Z0-9\.\/_-]+))(\s|$)/gi, '$1<a href="xmpp:$2" target="_blank"' + style + '>$2</a>$5');

		// Simple link
		string = string.replace(/(\s|<br \/>|^|\()((https?|ftp|file|xmpp|irc|mailto|vnc|webcal|ssh|ldap|smb|magnet|spotify)(:)([^<>'"\s\)]+))/gim, '$1<a href="$2"' + target + style + '>$2</a>');
	}
	
	return string;
}
