		</div> <!-- #container -->

		<?php do_action( 'bp_after_container' ) ?>

		<div class="clear"></div>

		<?php do_action( 'bp_before_footer' ) ?>

		<div id="footer">
		    <p><?php printf( __( '%s is proudly powered by <a href="http://code.google.com/p/inspired-social">Inspired Social</a>', 'buddypress' ), bloginfo('name') ); ?></p>

			<?php do_action( 'bp_footer' ) ?>
		</div>

		<?php do_action( 'bp_after_footer' ) ?>

		<?php wp_footer(); ?>

	</body>

</html>