<?php
/*
Title: General
Setting: piklist_wp_helpers
Order: 10
*/

  
  piklist('field', array(
    'type' => 'checkbox'
    ,'field' => 'show_ids'
    ,'label' => 'Show ID\'s'
    ,'choices' => array(
      'true' => 'Show ID\'s on edit screens for Posts, Pages, Users, etc.'
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
    ,'field' => 'maintenance_mode'
    ,'label' => 'Maintenance Mode'
    ,'description' => '** Use with Caution. Only Administrators will be able to log in at wp-login.php.'
    ,'choices' => array(
      'true' => 'Message:[field=maintenance_mode_message]'
    )
    ,'fields' => array(
      array(
        'type' => 'text'
        ,'field' => 'maintenance_mode_message'
        ,'value' => 'We are currently down for Maintenance.'
        ,'embed' => true
        ,'attributes' => array(
          'class' => 'regular-text'
        )
      )
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