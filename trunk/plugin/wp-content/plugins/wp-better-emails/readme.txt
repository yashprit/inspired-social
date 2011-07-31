=== WP Better Emails ===
Tags: email, emails, templates, notification, html emails, html, wp_mail, wpmu, multisite
Requires at least: 2.8
Tested up to: 3.1
Stable tag: 0.1.3

Adds a customizable good looking HTML template to all WP default plain/text emails and lets you set
 a custom sender name and email address.

== Description ==

All emails from Wordpress (lost password, notifications, etc.) are sent by default in text/plain format. WP Better
Emails wraps them with a much better looking customizable **HTML template** and lets you also set your own **sender name** and **email address**.

* WP Better Emails comes with a default simple and clean template that has been tested on various and popular email clients
 like Gmail, Yahoo Mail, Hotmail/Live, AOL, Outlook, Apple Mail and many more. This to ensure your emails will always display
nicely in your recipient mailbox. But you can of course design your own.
* WP Better Emails lets you send sample emails to test and preview your own custom template.
* All emails sent by this plugin are as 'multipart' so email clients that don't support HTML can read them.
* You can include some variables in your template such as your blog URL, blog name, blog description, admin email or date and time. They will all be
replaced when sending the email.
* The default template is included as an HTML file in the plugin folder (wpbe_template.html), feel free to edit it with your favorite editor.
* Clean uninstall process, doesn't leave some useless data in your database when deleted, so try it !

= Examples usages : =

* Add some ads/sponsored links to every email sent with wordpress
* Include some banners to promote a special event or feature of your website
* Brand the emails of your website or client website

= Internationalization =

WP Better Emails is currently available in :

* English
* French
* Dutch by [Glenn Mulleners](http://wp-expert.nl "Glenn Mulleners")

I'm looking for translators to extend to other languages. If you have translated the plugin in your language or want to,
please let me know : plugins [ at ] artyshow-studio.fr

== Installation ==

1. Extract and upload the `wp-better-emails` folder to the `/wp-content/plugins/` directory
2. Activate the plugin through the 'Plugins' menu in WordPress
3. Set a sender email and name, defaults are respectively 'wordpress@yourdomain.com' and 'Your Blog Title'
4. (Optional) Edit your own email template. See the screenshot tab to have a look at the default template
5. Every emails going out of your Wordpress Blog (notifications, lost password, etc.) looks better now !

== Frequently Asked Questions ==

= What if recipient can't read HTML emails ? =

WP Better Emails sends all emails in both formats ('multipart', i.e. HTML and plain text) so that email can be displayed in every email client.

= Why are the emails still sent in plain text format ? =

Be sure to include the **%content%** tag in your template. WP Better Emails wrap the content with the template, if no tag
is found, sending HTML emails is automatically desactivated.

= My custom email template doesn't look the same in Gmail and other email clients ? =

For example, Gmail strips everything before the `<body>` tag so if you included styles there, they won't be applied.
I included a few helpful links in the 'Help & support' tab, you will find complete information about coding for emails.

== Screenshots ==

1. The default template that comes with WP Better Emails. Tested on many email clients like Gmail, Yahoo!, Live/Hotmail, etc.
2. Sender option screen.
3. Edit template screen.

== Changelog ==

= 0.1.3 =
 * Sender email and name are now optional
 * Fixes replacing URLs of plain text content to handle https protocol

= 0.1.2 =
 * Added 3.1 compatibility
 * Dutch translation

= 0.1.1 =
 * French translation added

= 0.1 =
 * WP Better Emails first release