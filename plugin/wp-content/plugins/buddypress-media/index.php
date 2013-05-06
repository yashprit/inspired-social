<?php
/*
  Plugin Name: BuddyPress Media
  Plugin URI: http://rtcamp.com/buddypress-media/?utm_source=dashboard&utm_medium=plugin&utm_campaign=buddypress-media
  Description: This plugin adds missing media rich features like photos, videos and audio uploading to BuddyPress which are essential if you are building social network, seriously!
  Version: 2.11
  Author: rtCamp
  Text Domain: buddypress-media
  Author URI: http://rtcamp.com/?utm_source=dashboard&utm_medium=plugin&utm_campaign=buddypress-media
 */

/**
 * Main file, contains the plugin metadata and activation processes
 *
 * @package BuddyPressMedia
 * @subpackage Main
 */

if ( ! defined( 'BP_MEDIA_PATH' ) ){

	/**
	 *  The server file system path to the plugin directory
	 *
	 */
	define( 'BP_MEDIA_PATH', plugin_dir_path( __FILE__ ) );
}

if ( ! defined( 'BP_MEDIA_URL' ) ){

	/**
	 * The url to the plugin directory
	 *
	 */
	define( 'BP_MEDIA_URL', plugin_dir_url( __FILE__ ) );
}

/**
 * Auto Loader Function
 *
 * Autoloads classes on instantiation. Used by spl_autoload_register.
 *
 * @param string $class_name The name of the class to autoload
 */
function buddypress_media_autoloader( $class_name ) {
	$rtlibpath = array(
		'app/helper/' . $class_name . '.php',
		'app/admin/' . $class_name . '.php',
		'app/main/' . $class_name . '.php',
		'app/main/activity/' . $class_name . '.php',
		'app/main/profile/' . $class_name . '.php',
		'app/main/group/' . $class_name . '.php',
		'app/main/query/' . $class_name . '.php',
                'app/main/privacy/' . $class_name . '.php',
		'app/main/group/dummy/' . $class_name . '.php',
		'app/main/includes/' . $class_name . '.php',
		'app/main/widgets/' . $class_name . '.php',
		'app/log/' . $class_name . '.php',
		'app/importers/' . $class_name . '.php',
	);
	foreach ( $rtlibpath as $i => $path ) {
		$path = BP_MEDIA_PATH . $path;
		if ( file_exists( $path ) ) {
			include $path;
			break;
		}
	}
}

/**
 * Register the autoloader function into spl_autoload
 */
spl_autoload_register( 'buddypress_media_autoloader' );

/**
 * Instantiate the BuddyPressMedia class.
 */
global $bp_media;
$bp_media = new BuddyPressMedia();

/*
 * Look Ma! Very few includes! Next File: /app/main/BuddyPressMedia.php
 */
?>
