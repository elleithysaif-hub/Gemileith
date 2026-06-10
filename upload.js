const https = require("https");
const fs = require("fs");
const path = require("path");

const filePath = path.join(__dirname, "app/build/outputs/apk/debug/app-debug.apk");
const fileSize = fs.statSync(filePath).size;
const fileStream = fs.createReadStream(filePath);

const options = {
  hostname: "transfer.sh",
  port: 443,
  path: "/Gemileith_OS.apk",
  method: "PUT",
  headers: {
    "Content-Length": fileSize,
  },
};

console.log("Uploading " + filePath + " (" + fileSize + " bytes)...");

const req = https.request(options, (res) => {
  let responseBody = "";
  res.on("data", (chunk) => {
    responseBody += chunk.toString();
  });
  res.on("end", () => console.log("File Link:", responseBody.trim()));
});

req.on("error", (e) => console.error("Error:", e));
fileStream.pipe(req);
