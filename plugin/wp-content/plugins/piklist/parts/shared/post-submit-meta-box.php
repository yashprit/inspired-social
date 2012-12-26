<?php
/**
 * Code based on wp-admin/includes/meta-boxes.php
 *
 * TEXT DOMAINS: Do not add text domains to this file so the default WordPress translations will be used.
 */

  global $action, $wp_post_statuses;

  $post_type = $post->post_type;
  $post_type_object = get_post_type_object($post_type);
  $can_publish = current_user_can($post_type_object->cap->publish_posts);

  $statuses = isset($statuses) && !empty($statuses) ? $statuses : $wp_post_statuses;
  $initial_status = array(
    'status' => current(array_keys($statuses))
    ,'data' => current($statuses)
  );
  $action_label = !isset($statuses['publish']) ? 'Save' : 'Publish';
  $status_type = $post->post_status == 'auto-draft' ? $initial_status['status'] : $post->post_status;

?>

<div class="submitbox" id="submitpost">

  <div id="minor-publishing" <?php echo apply_filters('hide_minor_publishing_submit_box', false) ? 'class="hide-all"' : null; ?>>

    <!-- Hide submit button early on so that the browser chooses the right button when form is submitted with Return key -->
    <div class="hide-all">
      <?php submit_button(__('Save'), 'button', 'save'); ?>
    </div>

    <?php if (apply_filters('piklist_post_submit_meta_box', true, 'minor-publishing-actions', $post)): ?>
      
      <div id="minor-publishing-actions">

        <div id="save-action" <?php echo $action_label != 'Publish' ? 'class="hide-all"' : null; ?>>
      
          <input type="submit" name="save" id="save-post" value="<?php esc_attr_e('Save'); ?>" tabindex="4" class="button button-highlighted" />

          <img src="<?php echo esc_url(admin_url('images/wpspin_light.gif')); ?>" class="ajax-loading" id="draft-ajax-loading" alt="" />
      
        </div>

        <div id="preview-action">
      
          <?php
            if ('publish' == $post->post_status) 
            {
              $preview_link = esc_url(get_permalink($post->ID));
              $preview_button = __('Preview Changes');
            } 
            else 
            {
              $preview_link = get_permalink($post->ID);
              if (is_ssl())
              {
                $preview_link = str_replace('http://', 'https://', $preview_link);
              }
              $preview_link = esc_url(apply_filters('preview_post_link', add_query_arg('preview', 'true', $preview_link)));
              $preview_button = __('Preview');
            }
          ?>

          <a class="preview button" href="<?php echo $preview_link; ?>" target="wp-preview" id="post-preview" tabindex="4"><?php echo $preview_button; ?></a>
          <input type="hidden" name="wp-preview" id="wp-preview" value="" />

        </div>

        <div class="clear"></div>
    
      </div>
    
    <?php endif; ?>

    <?php if (apply_filters('piklist_post_submit_meta_box', true, 'misc-publishing-actions', $post)): ?>

      <div id="misc-publishing-actions">
        
        <?php do_action('post_submitbox_misc_actions_status'); ?>
        
        <?php if (apply_filters('piklist_post_submit_meta_box', true, 'misc-publishing-actions-status', $post)): ?>

          <div class="misc-pub-section<?php echo !$can_publish ? ' misc-pub-section-last' : ''; ?>">
    
            <label for="post_status"><?php _e('Status:'); ?></label>
    
            <span id="post-status-display">
              <?php _e(isset($statuses[$status_type]) ? (is_object($statuses[$status_type]) ? $statuses[$status_type]->label : $statuses[$status_type]['label']) : $wp_post_statuses[$status_type]->label); ?>
            </span>

            <?php if ('publish' == $post->post_status || 'private' == $post->post_status || $can_publish): ?>
    
              <a href="#post_status" <?php if ('private' == $post->post_status): ?>style="display:none;" <?php endif; ?>class="edit-post-status hide-if-no-js" tabindex='4'><?php _e('Edit'); ?></a>

              <div id="post-status-select" class="hide-if-js">

                <input type="hidden" name="hidden_post_status" id="hidden_post_status" value="<?php echo esc_attr('auto-draft' == $post->post_status ? $initial_status['status'] : $post->post_status); ?>" />

                <select name="post_status" id="post_status" tabindex="4">
                  <?php foreach ($statuses as $status => $status_data): ?>
                    <option <?php echo $status == $post->post_status ? 'selected="selected"' : ''; ?> value="<?php echo $status == 'auto-draft' ? 'draft' : $status; ?>"><?php _e($status == 'auto-draft' ? 'Draft' : (is_object($status_data) ? $status_data->label : $status_data['label'])); ?></option>
                  <?php endforeach; ?>
                </select>

                <a href="#post_status" class="save-post-status hide-if-no-js button"><?php _e('OK'); ?></a>
                <a href="#post_status" class="cancel-post-status hide-if-no-js"><?php _e('Cancel'); ?></a>

              </div>

            <?php endif; ?>
    
          </div>
      
        <?php endif; ?>
      
        <?php do_action('post_submitbox_misc_actions_visibility'); ?>
        
        <?php if (apply_filters('piklist_post_submit_meta_box', true, 'misc-publishing-actions-visibility', $post)): ?>
        
          <div class="misc-pub-section" id="visibility">
            <?php _e('Visibility:'); ?> <span id="post-visibility-display"><?php
              if ($post->post_status == 'private') 
              {
                $post->post_password = '';
                $visibility = 'private';
                $visibility_trans = __('Private');
              }
              elseif (!empty($post->post_password)) 
              {
                $visibility = 'password';
                $visibility_trans = __('Password protected');
              } 
              elseif ($post_type == 'post' && is_sticky($post->ID)) 
              {
                $visibility = 'public';
                $visibility_trans = __('Public, Sticky');
              } 
              else 
              {
                $visibility = 'public';
                $visibility_trans = __('Public');
              }

              echo esc_html($visibility_trans); ?></span>
  
            <?php if ($can_publish): ?>
      
              <a href="#visibility" class="edit-visibility hide-if-no-js"><?php _e('Edit'); ?></a>

              <div id="post-visibility-select" class="hide-if-js">

                <input type="hidden" name="hidden_post_password" id="hidden-post-password" value="<?php echo esc_attr($post->post_password); ?>" />
                <?php if ($post_type == 'post'): ?>
                  <input type="checkbox" style="display:none" name="hidden_post_sticky" id="hidden-post-sticky" value="sticky" <?php checked(is_sticky($post->ID)); ?> />
                <?php endif; ?>
                <input type="hidden" name="hidden_post_visibility" id="hidden-post-visibility" value="<?php echo esc_attr($visibility); ?>" />


                <input type="radio" name="visibility" id="visibility-radio-public" value="public" <?php checked($visibility, 'public'); ?> /> <label for="visibility-radio-public" class="selectit"><?php _e('Public'); ?></label><br />
                <?php if ($post_type == 'post' && current_user_can('edit_others_posts')) : ?>
                  <span id="sticky-span"><input id="sticky" name="sticky" type="checkbox" value="sticky" <?php checked(is_sticky($post->ID)); ?> tabindex="4" /> <label for="sticky" class="selectit"><?php _e('Stick this post to the front page'); ?></label><br /></span>
                <?php endif; ?>
                <input type="radio" name="visibility" id="visibility-radio-password" value="password" <?php checked($visibility, 'password'); ?> /> <label for="visibility-radio-password" class="selectit"><?php _e('Password protected'); ?></label><br />

                <span id="password-span"><label for="post_password"><?php _e('Password:'); ?></label> <input type="text" name="post_password" id="post_password" value="<?php echo esc_attr($post->post_password); ?>" /><br /></span>
                <input type="radio" name="visibility" id="visibility-radio-private" value="private" <?php checked($visibility, 'private'); ?> /> <label for="visibility-radio-private" class="selectit"><?php _e('Private'); ?></label><br />

                <p>
                 <a href="#visibility" class="save-post-visibility hide-if-no-js button"><?php _e('OK'); ?></a>
                 <a href="#visibility" class="cancel-post-visibility hide-if-no-js"><?php _e('Cancel'); ?></a>
                </p>
        
              </div>

            <?php endif; ?>

          </div>
      
        <?php endif; ?>
        
        <?php do_action('post_submitbox_misc_actions_published'); ?>
        
        <?php if (apply_filters('piklist_post_submit_meta_box', true, 'misc-publishing-actions-published', $post)): ?>
    
          <?php
            // translators: Publish box date format, see http://php.net/date
            $datef = __('M j, Y @ G:i');
            if (0 != $post->ID) 
            {
              if ('future' == $post->post_status) 
              { // scheduled for publishing at a future date
                $stamp = __('Scheduled for: <b>%1$s</b>');
              } 
              else if ('publish' == $post->post_status || 'private' == $post->post_status) 
              { // already published
                $stamp = __('Published on: <b>%1$s</b>');
              } 
              else if ('0000-00-00 00:00:00' == $post->post_date_gmt) 
              { // draft, 1 or more saves, no date specified
                $stamp = __((isset($statuses['publish']) ? 'Publish' : 'Schedule') . ' <b>immediately</b>');
              } 
              else if (time() < strtotime($post->post_date_gmt . ' +0000')) 
              { // draft, 1 or more saves, future date specified
                $stamp = __('Schedule for: <b>%1$s</b>');
              } 
              else 
              { // draft, 1 or more saves, date specified
                $stamp = __((isset($statuses['publish']) ? 'Publish' : 'Schedule') . ' on: <b>%1$s</b>');
              }
              $date = date_i18n($datef, strtotime($post->post_date));
            } 
            else 
            { // draft (no saves, and thus no date specified)
              $stamp = __('Publish <b>immediately</b>');
              $date = date_i18n($datef, strtotime(current_time('mysql')));
            }

            if ($can_publish): // Contributors don't get to choose the date of publish ?>
  
              <div class="misc-pub-section curtime misc-pub-section-last">
  
                <span id="timestamp"><?php printf($stamp, $date); ?></span>
                <a href="#edit_timestamp" class="edit-timestamp hide-if-no-js" tabindex='4'><?php _e('Edit'); ?></a>
  
                <div id="timestampdiv" class="hide-if-js">
                  <?php touch_time(($action == 'edit'), 1, 4); ?>
                </div>
  
              </div>
  
        <?php endif; ?>

      <?php endif; ?>
    
      <?php do_action('post_submitbox_misc_actions'); ?>

    </div>

    <div class="clear"></div>
  
  <?php endif; ?>
    
</div>


<div id="major-publishing-actions">

  <?php do_action('post_submitbox_start'); ?>

  <div id="delete-action">
    <?php
      if (current_user_can("delete_post", $post->ID)):
        if (!EMPTY_TRASH_DAYS)
        {
          $delete_text = __('Delete Permanently');
        }
        else
        {
          $delete_text = __('Move to Trash');
        }
    ?>

      <a class="submitdelete deletion" href="<?php echo get_delete_post_link($post->ID); ?>"><?php echo $delete_text; ?></a>

    <?php endif; ?>

  </div>

  <div id="publishing-action">

    <img src="<?php echo esc_url(admin_url('images/wpspin_light.gif')); ?>" class="ajax-loading" id="ajax-loading" alt="" />

    <?php
      if ((!in_array($post->post_status, array('publish', 'future', 'private')) || 0 == $post->ID)):
        if ($can_publish):
          if (!empty($post->post_date_gmt) && time() < strtotime($post->post_date_gmt . ' +0000')): ?>
      
            <input name="original_publish" type="hidden" id="original_publish" value="<?php esc_attr_e('Schedule'); ?>" />
            <?php submit_button(__('Schedule'), 'primary', 'publish', false, array('tabindex' => '5', 'accesskey' => 'p')); ?>
        
          <?php else: ?>
        
            <input name="original_publish" type="hidden" id="original_publish" value="<?php esc_attr_e($action_label); ?>" />
            <input name="<?php echo $action_label == 'Publish' ? 'publish' : 'save'; ?>" type="submit" class="button-primary" id="publish" tabindex="5" accesskey="p" value="<?php esc_attr_e($action_label); ?>" />
            
        <?php endif; ?>
      
      <?php else: ?>
    
        <input name="original_publish" type="hidden" id="original_publish" value="<?php esc_attr_e('Submit for Review'); ?>" />
        <?php submit_button(__('Submit for Review'), 'primary', 'publish', false, array('tabindex' => '5', 'accesskey' => 'p')); ?>
  
      <?php endif; ?>

    <?php else: ?>
    
      <input name="original_publish" type="hidden" id="original_publish" value="<?php echo esc_attr('auto-draft' == $post->post_status ? $initial_status['status'] : $post->post_status); ?>" />
      <input name="save" type="submit" class="button-primary" id="publish" tabindex="5" accesskey="p" value="<?php esc_attr_e('Update'); ?>" />
  
    <?php endif; ?>
  
  </div>

  <div class="clear"></div>

  </div>
</div>
