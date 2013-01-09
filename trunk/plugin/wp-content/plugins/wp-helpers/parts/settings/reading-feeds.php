<?php
/*
Title: Feeds
Setting: piklist_wp_helpers
Tab: Reading
Order: 310
*/

  piklist('field', array(
    'type' => 'select'
    ,'field' => 'disable_feeds'
    ,'label' => 'All Feeds'
    ,'value' => 'false'
    ,'choices' => array(
      'true' => 'Disable'
      ,'false' => 'Enable'
    )
  ));

  if (current_theme_supports('post-thumbnails'))
  {
    piklist('field', array(
      'type' => 'checkbox'
      ,'field' => 'featured_image_in_feed'
      ,'label' => 'Featured Image'
      ,'choices' => array(
        'true' => 'Add Featured Images to feed.'
      )
      ,'conditions' => array(
        array(
          'field' => 'disable_feeds'
          ,'value' => 'false'
        )
      )
    ));
  }

  
?>