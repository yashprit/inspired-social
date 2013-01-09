<?php

/**
 * User blog posts
 *
 * @package bbPress
 * @subpackage Theme
 */

?>

	<div id="bbp-author-blog-posts" class="bbp-author-blog-posts">
		<hr />
		<h2 class="entry-title"><?php _e( 'Blog Posts', 'bbpress' ); ?></h2>

		<div class="entry-content">

		<?php rewind_posts(); ?>

		<?php bbp_get_template_part( 'bbpress/loop', 'author' ); ?>

		</div>
	</div><!-- #bbp-author-blog-posts -->
