<?php
/*
Title: General
Setting: piklist_wp_helpers
Order: 100
*/

global $wp_version;
  
  piklist('field', array(
    'type' => 'checkbox'
    ,'field' => 'show_ids'
    ,'label' => 'Show ID\'s'
    ,'choices' => array(
      'true' => 'Show ID\'s on edit screens for Posts, Pages, Users, etc.'
    )
  ));

  piklist('field', array(
    'type' => 'checkbox'
    ,'field' => 'all_options'
    ,'label' => 'Show ALL Options'
    ,'choices' => array(
      'true' => 'Expose ALL site options, under WordPress Settings tab.'
    )
  ));


  if ( is_multisite() && is_super_admin() )
  {
    piklist('field', array(
      'type' => 'checkbox'
      ,'field' => 'theme_switcher'
      ,'label' => 'Theme Switcher'
      ,'description' => '* Setting shown to Super Administrators only.'
      ,'choices' => array(
        'true' => 'Disable for non-Super Administrators'
      )
    ));
  }

  piklist('field', array(
    'type' => 'checkbox'
    ,'field' => 'remove_screen_options'
    ,'label' => 'Screen Options Tab'
    ,'choices' => array(
      'true' => 'Remove'
    )
  ));

  piklist('field', array(
    'type' => 'checkbox'
    ,'field' => 'disable_uprade_notifications'
    ,'label' => 'Disable Upgrade Notifications (show to Administrators only)'
    ,'description' => 'Do you really want to disable upgrade notifications?<br />Upgrading to the latest versions of WordPress, plugins and themes, is the best way to keep your site secure and performing well.'
    ,'choices' => array(
      'wordpress' => 'WordPress'
      ,'plugins' => 'Plugins'
      ,'themes' => 'Themes'
    )
  ));

  if ($wp_version >= 3.5)
  {

    piklist('field', array(
      'type' => 'checkbox'
      ,'field' => 'link_manager'
      ,'label' => 'Link Manager'
      ,'choices' => array(
        'true' => 'Enable'
      )
    ));

  }


?>