<?php
/*
Plugin Name: Facestream
Plugin URI:
Description: Synchronises facebook with activity stream and back.
Version: 1.1
Author: Peter Hofman
Author URI: http://www.faboo.nl
*/

// Copyright (c) 2010 Faboo.nl. All rights reserved.
//
// Released under the GPL license
// http://www.opensource.org/licenses/gpl-license.php
//
// This is an add-on for Buddypress
// http://buddypress.org/
//
// **********************************************************************
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// **********************************************************************
function facestream_init() {
    require'facestream-functions.php';   
}

add_action( 'bp_init', 'facestream_init' );

?>