/*!
* color picker code originated by
* @author: Rachel Baker ( rachel@rachelbaker.me )
*/

(function($) {

    function pickBackgroundColor(color) {
        $("#toolbar-color").val(color);
    }
    function toggle_text() {
        link_color = $("#toolbar-color");
        if ("" === link_color.val().replace("#", "")) {
            link_color.val(default_color);
            pickBackgroundColor(default_color);
        } else pickBackgroundColor(link_color.val());
    }
    var default_color = "fbfbfb";
    $(document).ready(function() {
        var link_color = $("#toolbar-color");
        link_color.wpColorPicker({
            change: function(event, ui) {
                pickBackgroundColor(link_color.wpColorPicker("color"));
            },
            clear: function() {
                pickBackgroundColor("");
            }
        });
        $("#toolbar-color").click(toggle_text);
        toggle_text();
    });


})(jQuery);