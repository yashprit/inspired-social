<?php
/*
Title: Notices
Setting: piklist_wp_helpers
Order: 10
*/

?>

  <p><strong>Use notices to communicate with your users.</strong></p>

<?php
  
  piklist('field', array(
    'type' => 'checkbox'
    ,'field' => 'notice_admin'
    ,'label' => 'In Admin'
    ,'description' => 'Displays in WordPress admin.'
    ,'choices' => array(
      'true' => 'Message:[field=admin_message]'
    )
    ,'fields' => array(
      array(
        'type' => 'text'
        ,'field' => 'admin_message'
        ,'value' => 'This site will be down for Maintenance tomorrow.'
        ,'embed' => true
        ,'attributes' => array(
          'class' => 'regular-text'
        )
      )
    )
  ));

  piklist('field', array(
    'type' => 'checkbox'
    ,'field' => 'notice_front'
    ,'label' => 'Website'
    ,'description' => 'Displays on front of website.'
    ,'choices' => array(
      'true' => 'Message:[field=logged_in_front_message][field=notice_user_type][field=notice_color]'
    )
    ,'fields' => array(
      array(
        'type' => 'text'
        ,'field' => 'logged_in_front_message'
        ,'value' => 'This site will be down for Maintenance tomorrow.'
        ,'embed' => true
        ,'attributes' => array(
          'class' => 'regular-text'
        )
      ),
      array(
        'type' => 'select'
        ,'field' => 'notice_user_type'
        ,'value' => 'all'
        ,'attributes' => array(
          'class' => 'small-text'
        )
        ,'choices' => array(
          'all' => 'All Users'
          ,'logged_in' => 'Logged in Users'
        )
      ),
      array(
        'type' => 'select'
        ,'field' => 'notice_color'
        ,'attributes' => array(
          'class' => 'small-text'
        )
        ,'choices' => array(
          'danger' => 'Red'
          ,'success' => 'Green'
          ,'info' => 'Blue'
        )
      )
    )
  ));


?>