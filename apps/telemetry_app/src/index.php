<?php
$dsn = "pgsql:host={$_ENV['DB_HOST']};dbname={$_ENV['DB_NAME']}";
$user = $_ENV['DB_USER'];
$pass = $_ENV['DB_PASS'];

try {
    $pdo = new PDO($dsn, $user, $pass);

    if ($_SERVER['REQUEST_METHOD'] === 'POST') {
        $input = json_decode(file_get_contents('php://input'), true);
        $stmt = $pdo->prepare("INSERT INTO telemetry (device_id, temperature, humidity) VALUES (?, ?, ?)");
        $stmt->execute([
            $input['device_id'] ?? null,
            $input['temperature'] ?? null,
            $input['humidity'] ?? null
        ]);
        echo json_encode(["status" => "ok"]);
    } else {
        $stmt = $pdo->query("SELECT * FROM telemetry ORDER BY timestamp DESC LIMIT 10");
        echo json_encode($stmt->fetchAll(PDO::FETCH_ASSOC));
    }

} catch (PDOException $e) {
    http_response_code(500);
    echo json_encode(["error" => $e->getMessage()]);
}
?>
