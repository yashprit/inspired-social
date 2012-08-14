<?php
if (isset($_GET['title'])) {
$title = $_GET['title'];
}

if (isset($_GET['url'])) {
$url = $_GET['url'];
}

if (isset($_GET['content'])) {
$content = '<img style="width:150px;float:left;" src=' . $_GET['content'] . '>';
}

echo $content . '<a href="'.$url.'">' .$title . '</a><br/>';
?>