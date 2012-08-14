=== TDLC Birthdays ===

Contributors: ptitneo
Tags: buddypress, birthdays, widget
Requires at least: WPMU 2.9.1, Buddypress 1.2
Tested up to: WP 3.3.1, Buddypress 1.5.4
Stable tag: 0.4

A simple BuddyPress plugin displaying the birthday of members in a sidebar Widget. 9 languages, many options available. Check out the description :)

== Description ==

*Important: v0.4 introduces support for multiple widget instances. Because of this, upgrading from an earlier version will remove the old widget (and its settings)*

This is a Plugin that creates a multi-instance 'Birthdays' Widget that you can use in your Buddypress pages. Once you've created a datebox field in the Buddypress profiles and referenced it in the TLDC Birthdays widget settings, the widget will display the list of either all BP users or just the connected user's friends whose birthday is today and also, if you like, the list of upcoming birthdays within the next n days. Plugin now supports Andy Peatling's Buddypress Followers plugin when installed, so that you can also track the birthdays of people you follow.

So to sum up, in order to get things to work, you'll need :

* A birthday field in the Buddypress extended profile settings
* Users to fill it out in their profiles
* The TDLC Birthdays widget properly installed and set up (see *Installation*)

Features :

* Display only friends, or followed people (if BuddyPress Followers plugin is installed), or everybody's birthday
* Display upcoming birthdays within a range of your choice, or just today's birthdays
* Hide or show ages
* Optionally suggest your users to fill out their birth date if not already done
* Multiple widget instances now supported !

Remarks :

* Localized! Currently English, French, German, Hungarian, Italian, Japanese, Polish, Russian and Spanish languages are included. Many thanks to the translators!
* Sounds obvious but in "display friends only" mode, as you only see your friend's birthdays, you wont see your own birthday in the widget. This does not mean that your friends don't see it either! Same thing goes with the Followers option.

== Installation ==

This section describes how to install the plugin and get it working.

1. Download the archive and expand it.
2. Upload the *tdlc-birthdays* folder into your *wp-content/plugins/* directory
3. In your *WordPress Administration Area*, go to the *Plugins* page and click *Activate* for *TDLC Birthdays*
4. In the *Widgets* page, you can now add the Birthday plugin wherver you like. Please note that the widget requires compulsory settings.

== Changelog ==

*0.4*

1. [ENH] You can now add multiple instances of the widget to your sidebars (Note : *old settings will be lost* during upgrade from earlier version!)

*0.3.2*

1. [ENH] Now compatible with BP1.5 - older versions of the plugin could not read the user metadata (birthdate) properly anymore due to a change in BP1.5. Thanks to Jeff for finding out about that.
2. [ENH] Added an option to display a link offering to edit user's profile if their birthdate is missing (merci NoahY). Warning : translation only available in English and French for now.

*0.3.1*

1. [BUG][Regression] Ids for the birthdate field were not allowed anymore in v0.3 (only field name). Now fixed.
2. [BUG] "Birthday within N days" results list did not include the Nth day, thanks to Chestnut.
3. [ENH] Code improvement in the UTC offset time management, thanks to Chestnut again and Steve for testing.

*0.3*

1. [ENH] Code optimization, rewrote database queries. The performance should now be WAY better in buddypress setups with a large number of users. Feedback welcome !
2. [ENH] Japanse translation now available thanks to Chestnut. Yataa !
3. [BUG] Rewrote the GMT offset fix of 0.2.8 for better efficiency. It should cover all cases now. Thanks to Steve for helping me with the troubleshooting.

*0.2.8*

1. [BUG] Fixed a date issue occuring when the main blog uses a GMT offset, causing the birth date to be displayed one day before or after the actual date. This issue was due to change http://buddypress.trac.wordpress.org/changeset/3651 in Buddypress. Thanks to Pisanojm and seballero.

*0.2.7*

1. Repost of 0.2.6 with missing language files (cvs error)

*0.2.6*

1. [BUG] Fixed a PHP warning due to array_merge() function in PHP5 (thanks Wally)
2. [BUG] Fixed another PHP warning regarding spam users, should complement the 0.2.3 fix (merci Emmanuel)

*0.2.5*

1. [ENH] Now supports the BuddyPress Followers plugin! http://wordpress.org/extend/plugins/buddypress-followers/ Check the new options in the widget settings. Thanks to Michael Lovelock for suggesting this feature. *Message to the plugin translators* : a few new strings have been added, you may want to update your language files :)
2. [ENH] Hungarian translation brought to us by Boris Yakshov, thanks!
3. [ENH] Russian translation updated by Mac, thanks!
4. [BUG] When a user had no birth year, the displayed age was "0 year".
5. [BUG] Some HTML code was wrong in the widget Settings, causing weird behaviour of the labels (only in IE). Thanks to supervinnie40 for noticing.

*0.2.4*

1. [ENH] Spanish translation provided by Jorge Epuñan, thanks!

*0.2.3*

1. [BUG] The change introduced in v0.2.2 regarding spam users caused a PHP warning on some installations. This should be fixed now.
2. [ENH] Polish language added, thanks to Sousuke.

*0.2.2*

1. [BUG] The plugin does not display spam users anymore. Thanks Sam for noticing.
2. [ENH] Small changes in the layout (does not mess up anymore when 2 or more people have the same birthday).

*0.2.1*

1. [BUG] Small typos fixed in the English version.

*0.2*

1. [ENH] Now compatible with Buddypress 1.2, slight layout alterations to fit the new default theme.

*0.1.7*

1. [ENH] Russian language added, thanks to Valairus.

*0.1.6*

1. [ENH] German language added, thanks to Uwe.

*0.1.5*

1. [ENH] Two modes are now available in the settings: either display the birthdays of the user's friend (that's the legacy mode, which requires a registered, logged in user with some friends), or display the birthdays of any and all users of the BP install (works for anonymous visitors, suitable for small communities). Thanks to Arend from the Netherlands for the idea ;)
2. [ENH] A bit of code refactoring, also languages files are now located in a dedicated directory.

*0.1.4*

1. [ENH] Italian translation included, thanks to Daniele Argiolas.

*0.1.3*

1. [ENH] New option : hide people's age in the widget.
2. [ENH] Should be PHP4 compatible now.

*0.1.2*

1. [BUG] User with no friends or not connected are now properly handled with dedicated error messages (thanks Zuegmut).
2. [ENH] Now displays avatars (small one for upcoming birthdays, big one for today's birthdays).

*0.1.1*

1. [BUG] Small bug fixed in the age calculation function (was wrong if the birthday was after Dec 31).

*0.1*

1. Initial release.

== Settings ==

* In order to get the widget working, you need to fill out the *Birthday field Name* (or ID). The Birthday field must have been previously created in the *Buddypress profile page* (and it should be a *datebox* type field). Of course, the users must also fill it out on their profile pages...

* The other settings are self-explanatory (or at least I hope so).

== Known Issues ==

* Some translations are incomplete. The new strings related to the 'Followers' plugin support added in v0.2.5 is not translated yet except for French and English.

If you find any other bugs or want to request some additional features for future releases (which I may consider if I've got time), please post a comment on the plugin homepage.
