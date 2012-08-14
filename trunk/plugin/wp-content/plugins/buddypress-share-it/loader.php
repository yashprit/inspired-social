<?php 
/**
Plugin Name: BuddyShare
Plugin URI: http://buddypress.org
Description: Adds share buttons to your BuddyPress site to let people share content on sites like Twitter and Facebook.
Author: modemlooper
Author URI: http://twitter.com/modemlooper
Version:1.2.1
License:GPL2
**/

function bp_share_it_init() {
	require( dirname( __FILE__ ) . '/bp-share-it.php' );
}
add_action( 'bp_include', 'bp_share_it_init' );

?>