const express = require("express");
const http = require("http");
const WebSocket = require("ws");

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

const PORT = process.env.PORT || 3000;

// Connected live-location rooms
const rooms = new Map();

app.get("/", (req, res) => {
  res.send("NOVA Live Location Server is running");
});

wss.on("connection", (ws) => {
  let roomId = null;

  ws.on("message", (message) => {
    try {
      const data = JSON.parse(message.toString());

      // Join a live-location room
      if (data.type === "join") {
        roomId = String(data.roomId || "").trim();

        if (!roomId) {
          ws.send(JSON.stringify({
            type: "error",
            message: "Room ID required"
          }));
          return;
        }

        if (!rooms.has(roomId)) {
          rooms.set(roomId, new Set());
        }

        rooms.get(roomId).add(ws);

        ws.send(JSON.stringify({
          type: "joined",
          roomId: roomId
        }));

        return;
      }

      // Send location to everyone else in the same room
      if (data.type === "location") {
        if (!roomId) {
          ws.send(JSON.stringify({
            type: "error",
            message: "Join a room first"
          }));
          return;
        }

        const latitude = Number(data.latitude);
        const longitude = Number(data.longitude);

        if (
          !Number.isFinite(latitude) ||
          !Number.isFinite(longitude)
        ) {
          return;
        }

        const locationMessage = JSON.stringify({
          type: "location",
          latitude: latitude,
          longitude: longitude,
          timestamp: Date.now()
        });

        const clients = rooms.get(roomId);

        if (clients) {
          for (const client of clients) {
            if (client !== ws && client.readyState === WebSocket.OPEN) {
              client.send(locationMessage);
            }
          }
        }
      }
    } catch (error) {
      ws.send(JSON.stringify({
        type: "error",
        message: "Invalid message"
      }));
    }
  });

  ws.on("close", () => {
    if (roomId && rooms.has(roomId)) {
      const clients = rooms.get(roomId);
      clients.delete(ws);

      if (clients.size === 0) {
        rooms.delete(roomId);
      }
    }
  });
});

server.listen(PORT, () => {
  console.log(`NOVA Live Location Server running on port ${PORT}`);
});
