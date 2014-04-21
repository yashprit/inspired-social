<?php
/*
Title: Visitors
Setting: piklist_wp_helpers
Tab: Users
Order: 610
*/

?>

  <p><strong>Interact with your visitors</strong></p>

<?php

  piklist('field', array(
    'type' => 'checkbox'
    ,'field' => 'maintenance_mode'
    ,'label' => 'Maintenance Mode'
    ,'description' => 'Disable site and show message'
    ,'choices' => array(
      'true' => 'Message:[field=maintenance_mode_message]'
    )
    ,'fields' => array(
      array(
        'type' => 'textarea'
        ,'field' => 'maintenance_mode_message'
        ,'value' => 'We are currently down for Maintenance.'
        ,'embed' => true
        ,'attributes' => array(
          'class' => 'regular-text'
          ,'style' => 'vertical-align: top'
          ,'cols' => 55
          ,'rows' => 3
        )
      )
    )
  ));

  piklist('field', array(
    'type' => 'checkbox'
    ,'label' => 'Private Website'
    ,'field' => 'private_site'
    ,'choices' => array(
      'true' => 'Only logged in users can see Website.'
    )
  ));

  piklist('field', array(
    'type' => 'checkbox'
    ,'label' => 'Redirect Home'
    ,'field' => 'redirect_to_home'
    ,'choices' => array(
      'true' => 'Redirect users to Home Page after login.'
    )
  ));
  
  piklist('field', array(
    'type' => 'checkbox'
    ,'field' => 'notice_admin'
    ,'label' => 'In Admin Message'
    ,'description' => 'Displays in WordPress admin.'
    ,'choices' => array(
      'true' => 'Message:[field=admin_message]'
    )
    ,'fields' => array(
      array(
        'type' => 'textarea'
        ,'field' => 'admin_message'
        ,'value' => 'This site will be down for Maintenance tomorrow.'
        ,'embed' => true
        ,'attributes' => array(
          'class' => 'regular-text'
          ,'style' => 'vertical-align: top'
          ,'cols' => 55
          ,'rows' => 3
        )
      )
    )
  ));

  piklist('field', array(
    'type' => 'checkbox'
    ,'field' => 'notice_front'
    ,'label' => 'Frontend Message'
    ,'description' => 'Displays on front of website.'
    ,'choices' => array(
      'true' => 'Message:[field=logged_in_front_message][field=notice_user_type][field=notice_browser_type][field=notice_color]'
    )
    ,'fields' => array(
      array(
        'type' => 'textarea'
        ,'field' => 'logged_in_front_message'
        ,'value' => 'This site will be down for Maintenance tomorrow.'
        ,'embed' => true
        ,'attributes' => array(
          'class' => 'regular-text'
          ,'style' => 'vertical-align: top'
          ,'cols' => 55
          ,'rows' => 3
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
        ,'field' => 'notice_browser_type'
        ,'value' => 'all'
        ,'attributes' => array(
          'class' => 'small-text'
        )
        ,'choices' => array(
          'all' => 'All Browsers'
          ,'is_chrome' => 'Chrome'
          ,'is_gecko' => 'Gecko'
          , 'is_IE' => 'IE'
          , 'is_macIE' => 'IE: MAC'
          , 'is_winIE' => 'IE: Windows'
          ,'is_lynx' => 'Lynx'
          , 'is_opera' => 'Opera'
          , 'is_NS4' => 'NS4'
          , 'is_safari' => 'Safari'
          , 'is_iphone' => 'Safari: mobile'
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