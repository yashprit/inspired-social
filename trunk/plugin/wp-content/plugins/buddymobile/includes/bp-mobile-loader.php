<?php

function bp_mobile_addFooterSwitch($query){

	$container = $_SERVER['HTTP_USER_AGENT'];
	$useragents = array (
		"iPhone",
		"iPad",
		"iPod",
		"Android",
		"blackberry9500",
		"blackberry9530",
		"blackberry9520",
		"blackberry9550",
		"blackberry9800",
		"webOS"
	);
	false;
	foreach ( $useragents as $useragent ) {
		if (eregi($useragent,$container)){
			echo '<div id="footer-switch" style="margin:40px 0">
	    	<p style="text-align: center;"><a href="" style="font-size:150%" id="theme-switch-site">view mobile site</a></p>
		</div><!-- #footer -->';

		}
	}
}
add_action('wp_footer', 'bp_mobile_addFooterSwitch');



function bp_mobile_insert_head() {
?>
 	<script type="text/javascript">
	//<![CDATA[

		var $j = jQuery.noConflict();
		
		$j(document).ready(function(){
		
			$j('#theme-switch').live('click', function(event){
					$j.cookie( 'bpthemeswitch', 'normal', {path: '/'} );			
			}); 		
			
			$j('#theme-switch-site').live('click', function(event){
					$j.cookie( 'bpthemeswitch', 'mobile', {path: '/'} );			
			});   
				
		});

	//]]>
	</script>

<?php
}
add_action('wp_head', 'bp_mobile_insert_head');