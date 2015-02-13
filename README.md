# Inspired Social

An Openfire plugin which combines WordPress and BuddyPress with WebRTC to provide a fully collaborative online social community.

http://inspired-social.googlecode.com/files/Image4.jpg

Please note that Only Openfire and MySQL required. No Apache, No PHP. The Inspired-Social plugin has embedded Quercus PHP Engine and RTMP server for screenshare only. You will also need Openfire Jingle Relay Nodes plugin in order to relay WebRTC audio and video when users are behind restrictive firewalls or home routers

A User Guide can be found at `http://inspired-social.googlecode.com/files/Inspired%20Social%20user%20guide.pdf`

An Admin Guide can be found at `http://inspired-social.googlecode.com/files/Inspired%20Social%20admin%20guide%20_Revised.pdf`

### Inspired Social Software Profile
* WordPress 3.5.1
* BuddyPress 1.7.0
* BuddyPress Docs 1.3.3 by Boone B Gorges
* oEmbed for BuddyPress 0.5.2 by r-a-y
* Private BuddyPress 1.0.4 by Dennis Morhardt
* WP Better Emails 0.2.5 By ArtyShow
* Article2PDF 0.27 By Marc Schieferdecker
* BuddyPress Album+ 0.1.8.14 By The BP-Media Team
* TDLC Birthdays 0.4 by Tom Granger
* BP-Album 0.1.8.14 By The BP-Media Team
* BuddyMobile 1.6.9 By modemlooper
* BuddyPress Hovercards 1.1.3 by Mike Martel
* BuddyShare 1.2.1 by BuddyShare
* Piklist 0.7.2 by Piklist
* Wordpress Helpers 1.4.8 by PikList
* Inspired Communication Toolbar

### XMPP Webclient (http://www.jappix.com)
* WebRTC Video/Audio Conferencing (http://webrtc.org)
* Worgroups (Fastpath live chat) support
* RTMP Screen Share (red5-screenshare)
* Embedded RTMP Server (Milenia Grafter 64K RTMP Server)

### Requirements
* Openfire Server 3.8.0 and above configured with MySQL (Needed by WordPress)
* Openfire Jingle Relay Nodes plugin to relay WebRTC audio/video behind firewalls.
* Openfire WebSockets plugin
* Nothing else required. No Apache, No PHP, No Asterisk and No Red5. The Inspired-Social plugin has embedded servers.

http://inspired-social.googlecode.com/files/inspired-social.jpg

### How to Install
* Unzip `inspired-x.x.x.x.zip` and copy the inspired.war file to the `OPENFIRE_HOME/plugins` directory
* From a browser, go to `http://your_openfire-server:7070/inspired/wp-admin` and complete the installation of WordPress. Login as admin with default password admin. Make sure you change the default password from the WordPress dashboard.
* Go to WordPress Dashboard, activate BuddyPress.
* Do a bulk activate of all other plugins and configure them from dashboard.
* Go to appearance and configure your widgets. Add birthdays and events if required.
* Create a new user with username and password of choice.
* Go to Openfire admin web console and change HTTP bind ports from 7070/7443 to 80/443.
* Configure Openfire email and run the test to make sure it works.
* Go to `http://your_openfire-server/inspired/social` and login with your new user details.
* Openfire is auto-configured as follows
* All Openfire users are WordPress users and Openfire admin console cannot add or modify users
* All Openfire user groups are read from BuddyPress groups Openfire admin console cannot add or modify user groups
* Group Chat rooms are auto-created every time a BuddyPress group is created
* User Rosters are updated every time a BuddyPress friendship is made or broken.

### How to Use Communication Toolbar
Roster of Friendships showing Presence
IM chat with friends or MUC chats with BuddyPress group members
Audio/Video conference with friends
Audio conference only with BuddyPress group members
Live chat (including audio) with anonymous visitors on another web site
Screen share to friends or a group. Requires Java for publisher and Flash for viewer.
