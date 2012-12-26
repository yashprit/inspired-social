<?php
/*
Title: Dashboard Widgets
Setting: piklist_wp_helpers
*/

  piklist('field', array(
    'type' => 'select'
    ,'field' => 'screen_layout_columns_dashboard'
    ,'label' => 'Columns'
    ,'value' => 'default'
    ,'attributes' => array(
      'class' => 'small-text'
    )
    ,'choices' => array(
      'default' => 'Default'
      ,'1' => '1'
      ,'2' => '2'
      ,'3' => '3'
      ,'4' => '4'
    )
  ));

  piklist('field', array(
    'type' => 'group'
    ,'field' => 'remove_dashboard_widgets'
    ,'label' => 'Remove Dashboard Widgets'
    ,'fields' => array(
      array(
        'type' => 'checkbox'
        ,'field' => 'dashboard_widgets'
        ,'choices' => array(
          'dashboard_right_now' => 'Right Now'
          ,'dashboard_recent_comments' => 'Recent Comments'
          ,'dashboard_incoming_links' => 'Incoming Links'
          ,'dashboard_quick_press' => 'QuickPress'

        )
        ,'columns' => 4
      )
      ,array(
        'type' => 'checkbox'
        ,'field' => 'dashboard_widgets'
        ,'choices' => array(
          'dashboard_recent_drafts' => 'Recent Drafts'
          ,'dashboard_primary' => 'WordPress Blog'
          ,'dashboard_secondary' => 'WordPress News'
          ,'dashboard_plugins' => 'Plugins'
        )
        ,'columns' => 4
      )
    )
  ));

  
?>