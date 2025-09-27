<?php
require 'db_config.php';
if($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(["success"=>false,"message"=>"POST required"]);
    exit;
}
$email = $conn->real_escape_string($_POST['email'] ?? '');
$password = $_POST['password'] ?? '';

$stmt = $conn->prepare("SELECT id,name,email,password,role FROM users WHERE email=? LIMIT 1");
$stmt->bind_param("s",$email);
$stmt->execute();
$stmt->bind_result($id,$name,$dbemail,$dbpass,$role);
if($stmt->fetch()){
    if(password_verify($password, $dbpass)){
        echo json_encode([
            "success"=>true,
            "message"=>"Login success",
            "user"=>["id"=>$id,"name"=>$name,"email"=>$dbemail,"role"=>$role]
        ]);
    } else {
        echo json_encode(["success"=>false,"message"=>"Wrong password"]);
    }
} else {
    echo json_encode(["success"=>false,"message"=>"User not found"]);
}
$stmt->close();
$conn->close();
?>
