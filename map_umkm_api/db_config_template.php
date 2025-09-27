<?php
// db_config_template.php (contoh konfigurasi)
$hostname = "localhost";
$username = "root";
$password = "";
$database = "map_umkm";
$conn = new mysqli($hostname, $username, $password, $database);
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
?>
