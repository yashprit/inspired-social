<div class="wrap">
	<h2><?php _e('Email options', 'wp-better-emails'); ?></h2>

	<form method="post" action="options.php" id="wpbe_options_form">
		<?php settings_fields('wpbe_full_options'); ?>
		<?php $options = get_option('wpbe_options'); ?>
		<div id="wpbe_options_tabs">
			<ul class="wpbe_tabs_titles">
				<li><a href="#tab1"><?php _e('Sender options', 'wp-better-emails'); ?></a></li>
				<li><a href="#tab2"><?php _e('Template options', 'wp-better-emails'); ?></a></li>
				<li><a href="#tab3"><?php _e('Help & support', 'wp-better-emails'); ?></a></li>
			</ul>
			<div id="tab1" class="wpbe_tab_content">
				<p><?php _e('Change the Wordpress default behavior when sending emails to users (i.e. comment notifications, lost password, etc.), set your own sender name and email address.', 'wp-better-emails'); ?></p>
				<table class="form-table">
					<tr valign="top" class="form-field">
						<th scope="row"><label for="wpbe_from_name"><?php _e('From name', 'wp-better-emails'); ?></label></th>
						<td><input type="text" id="wpbe_from_name" class="regular-text" name="wpbe_options[from_name]" value="<?php echo $options['from_name']; ?>" /></td>
					</tr>
					<tr valign="top" class="form-field">
						<th scope="row"><label for="wpbe_from_email"><?php _e('From email address', 'wp-better-emails'); ?></label></th>
						<td><input type="text" id="wpbe_from_email" class="regular-text" name="wpbe_options[from_email]" value="<?php echo $options['from_email']; ?>" /></td>
					</tr>
				</table>
			</div>
			<div id="tab2" class="wpbe_tab_content">
				<table class="form-table">
					<tr valign="top">
						<th role="row"><?php _e('Template variables', 'wp-better-emails'); ?></th>
						<td>
							<p><?php _e('Some dynamic tags can be included in your email template :', 'wp-better-emails'); ?></p>
							<ul>
								<li><?php _e('<strong>%content%</strong> : will be replaced by the message content.', 'wp-better-emails'); ?><br />
								<span class="description"><?php _e('NOTE: The content tag is <strong>required</strong>, WP Better Emails will be automatically desactivated if no content tag is found.', 'wp-better-emails'); ?></span></li>
								<li><?php _e('<strong>%blog_url%</strong> : will be replaced by your blog URL.', 'wp-better-emails'); ?></li>
								<li><?php _e('<strong>%blog_name%</strong> : will be replaced by your blog name.', 'wp-better-emails'); ?></li>
								<li><?php _e('<strong>%blog_description%</strong> : will be replaced by your blog description.', 'wp-better-emails'); ?></li>
								<li><?php _e('<strong>%admin_email%</strong> : will be replaced by admin email.', 'wp-better-emails'); ?></li>
								<li><?php _e('<strong>%date%</strong> : will be replaced by current date, as formatted in <a href="options-general.php">general options</a>.', 'wp-better-emails'); ?></li>
								<li><?php _e('<strong>%time%</strong> : will be replaced by current time, as formatted in <a href="options-general.php">general options</a>.', 'wp-better-emails'); ?></li>
							</ul>
						</td>
					</tr>
					<tr valign="top">
						<th scope="row">
							<label for="wpbe_template"><?php _e('HTML Template', 'wp-better-emails'); ?></label>
						</th>
						<td>
							<textarea id="wpbe_template" name="wpbe_options[template]" cols="80" rows="20"><?php echo $options['template']; ?></textarea>
							<p>
								<label for="wpbe_preview_email"><?php _e('Send an email preview to', 'wp-better-emails'); ?></label>
								<input type="hidden" id="wpbe_nonce_preview" name="_ajax_nonce" value="<? echo wp_create_nonce( 'email_preview' ); ?>" />
								<input type="text" id="wpbe_preview_email" name="wpbe_preview_email" value="<?php echo get_option('admin_email'); ?>" />
								<a href="javascript:void(0);" class="button" id="wpbe_send_preview"><?php _e('Send', 'wp-better-emails'); ?></a><span id="loading"></span>
								<img src="<?php echo get_option('siteurl'); ?>/wp-admin/images/wpspin_light.gif" id="ajax-loading" style="visibility: hidden;" alt="Loading" />
								<br /><span class="description"><?php __('You must save your template before sending an email preview.', 'wp-better-emails'); ?></span>
								<span id="wpbe_preview_message"></span>
							</p>
						</td>
					</tr>
				</table>
			</div>
			<div id="tab3" class="wpbe_tab_content">
				<h3><?php _e('Designing & coding email templates', 'wp-better-emails'); ?></h3>
				<p><?php _e('Here are a few ressources about email formatting :', 'wp-better-emails') ?></p>
				<ul>
					<li><a href="http://www.campaignmonitor.com/resources/" target="_blank">http://www.campaignmonitor.com/resources/</a></li>
					<li><a href="http://articles.sitepoint.com/article/code-html-email-newsletters/" target="_blank">http://articles.sitepoint.com/article/code-html-email-newsletters/</a></li>
				</ul>
				<h3><?php _e('Converting HTML templates', 'wp-better-emails'); ?></h3>
				<p><?php _e('Coding HTML for emails requires CSS styles to be inline. Here\'s a tool powered by MailChimp that will convert your styles placed in the <code>&lt;head&gt;</code> tag to inline : ', 'wp-better-emails'); ?></p>
				<ul>
					<li><?php _e('<a href="http://www.mailchimp.com/labs/inlinecss.php" target="_blank">http://www.mailchimp.com/labs/inlinecss.php</a>', 'wp-better-emails'); ?></li>
				</ul>
				<h3><?php _e('Requests & Bug report', 'wp-better-emails'); ?></h3>
				<p><?php _e('If you have any idea to improve this plugin or any bug to report, please email me at : <a href="mailto:plugins@artyshow-studio.fr?subject=[wp-better-emails]">plugins@artyshow-studio.fr</a>', 'wp-better-emails'); ?></p>
				<h3><?php _e('Credits', 'wp-better-emails'); ?></h3>
				<ul>
					<li><?php _e('MarkItUp! : jQuery HTML editor <a href="http://markitup.jaysalvat.com/home/" target="_blank">http://markitup.jaysalvat.com/home/</a>', 'wp-better-emails'); ?></li>
				</ul>
			</div>
		</div>
		<p class="submit">
			<input type="submit" class="button-primary" value="<?php _e('Save Changes', 'wp-better-emails') ?>" />
		</p>
	</form>
</div>