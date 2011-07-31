jQuery(document).ready(function(){	
	// Tabs
	jQuery('#wpbe_options_tabs').tabs( { cookie: { expires: 1 } } );

	jQuery('#wpbe_template').markItUp(mySettings);

	// AJAX
	jQuery('#wpbe_send_preview').click(function(e) {
		var email = jQuery('#wpbe_preview_email').val();
		var preview_nonce = jQuery('#wpbe_nonce_preview').val();
		jQuery.ajax({
			type: 'post',
			url: 'admin-ajax.php',
			data: {
				action: 'send_preview',
				preview_email: email,
				_ajax_nonce: preview_nonce
			},
			beforeSend: function() { jQuery('#ajax-loading').css('visibility', 'visible'); },
			complete: function() { jQuery('#ajax-loading').css('visibility', 'hidden');},
			success: function(html){
				jQuery('#wpbe_preview_message').html(jQuery(html).fadeIn());
			}
		});
		e.preventDefault();
	});
});
