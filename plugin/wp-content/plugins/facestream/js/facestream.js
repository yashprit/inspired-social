jQuery('#aw-whats-new-submit').click(function() {

	if(jQuery('#activity_to_facebook').attr('checked') == true)
	{
		var content = jQuery('#whats-new').val();
		content = content.replace(' #FACEBOOK ','');
		content  = content+' #FACEBOOK ';
		jQuery('#whats-new').val(content);
	}
});

jQuery(document).ready(function() {
jQuery('#forum-topic-form').submit(function() {
  
if(jQuery('#topic_to_facebook').attr('checked') == true)
	{
		var content = jQuery('#topic_title').val();
		content = content.replace(' #FACEBOOK ','');
		content  = content+' #FACEBOOK ';
		jQuery('#topic_title').val(content);
	}
	
	if(jQuery('#topicreply_to_facebook').attr('checked') == true)
	{
		var content = jQuery('#reply_text').val();
		content = content.replace(' #FACEBOOK ','');
		content  = content+' #FACEBOOK ';
		jQuery('#reply_text').val(content);
	}
    
});

});