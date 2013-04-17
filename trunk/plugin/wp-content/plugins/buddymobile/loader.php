<?php

/**
 * Plugin Name: BuddyMobile
 * Plugin URI:  http://buddypress.org
 * Description: Mobile them for optimized mobile experience on BuddyPress sites
 * Author:      modemlooper
 * Author URI:  http://twitter.com/modemlooper
 * Version:     1.6.9
 */

// Exit if accessed directly
if ( !defined( 'ABSPATH' ) ) exit;


function BP_mobile_init() {
    require( dirname( __FILE__ ) . '/includes/bp-mobile-class.php' );
}
add_action( 'plugins_loaded', 'BP_mobile_init' );