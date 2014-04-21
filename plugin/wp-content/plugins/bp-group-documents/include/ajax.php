<?php
// Exit if accessed directly
if (!defined('ABSPATH'))
    exit;

/*
 * bp_group_documents_increment_download_count()
 *
 * instanciates a document object based on the POST id, 
 * then increments the download_count field in the database by 1.
 *
 * This fires in the background when a user clicks on a document link
 */
function bp_group_documents_increment_download_count(){
	$document_id = (string)$_POST['document_id'];
	if( isset( $document_id ) && ctype_digit( $document_id ) ){
		$document = new BP_Group_Documents( $document_id );
		$document->increment_download_count();
	}
}
add_action('wp_ajax_bp_group_documents_increment_downloads','bp_group_documents_increment_download_count');
