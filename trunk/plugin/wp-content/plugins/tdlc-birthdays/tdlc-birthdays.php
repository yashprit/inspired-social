<?php
/*
Plugin Name: TDLC Birthdays
Plugin URI: http://www.t0m.fr/2009/wordpress/plugin-buddypress-tdlc-birthdays.html
Description: This simple Buddypress Widget uses a Birthday field from Buddypress extended profile to display a list of upcoming birthdays of the user's friends. English, French, German, Hungarian, Italian, Japanese, Polish, Russian and Spanish languages available.
Author: Tom Granger
Version: 0.4
Author URI: http://www.t0m.fr/
License: Licensed under the The GNU General Public License 3.0 (GPL) http://www.gnu.org/licenses/gpl.html
*/

/* WP 2.8 is required for the multi-instance widget */
global $wp_version;
if((float)$wp_version >= 2.8){ 

	/* Only load code that needs BuddyPress to run once BP is loaded and initialized. */
	function load_tdlcbirthdays() {
		require( dirname( __FILE__ ) . '/core.php' );
	}
	add_action( 'bp_include', 'load_tdlcbirthdays' );
	 
}
?>