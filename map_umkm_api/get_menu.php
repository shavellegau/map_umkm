<?php
require 'db_config.php';
$res = $conn->query("SELECT id,name,description,price,image FROM menu_items");
$items = [];
while($row = $res->fetch_assoc()){
    $items[] = $row;
}
echo json_encode(["success"=>true,"menu"=>$items]);
$conn->close();
?>
