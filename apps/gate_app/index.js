require("dotenv").config();
const express = require("express");
const { Pool } = require("pg");

const app = express();
app.use(express.json());

const pool = new Pool({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASS,
  database: process.env.DB_NAME,
});

app.post("/gate", async (req, res) => {
  const { action, user_id } = req.body;
  if (!["open", "close"].includes(action)) {
    return res.status(400).json({ error: "Invalid action. Use 'open' or 'close'." });
  }

  try {
    // логируем действие
    await pool.query("INSERT INTO gate_log (action, user_id) VALUES ($1, $2)", [action, user_id]);

    // здесь может быть управляющая логика: реле, GPIO, MQTT и т.д.
    console.log(`Gate ${action} command received from user: ${user_id}`);

    res.json({ status: `Gate ${action} command accepted.` });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get("/gate/logs", async (req, res) => {
  try {
    const result = await pool.query("SELECT * FROM gate_log ORDER BY timestamp DESC LIMIT 20");
    res.json(result.rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.listen(4000, () => {
  console.log("Gate service running on port 4000");
});
