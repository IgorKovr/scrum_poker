#!/usr/bin/env node

/**
 * Backend-only runner for the Scrum Poker application
 * This allows running just the backend from Cursor's "Run" button
 */

const { spawn } = require("child_process");
const path = require("path");

console.log("☕ Starting Scrum Poker Backend...\n");

// Run the backend
const backendPath = path.join(__dirname, "backend");
const child = spawn("./gradlew", ["bootRun"], {
  stdio: "inherit",
  cwd: backendPath,
});

child.on("error", (error) => {
  console.error(`❌ Error starting backend: ${error.message}`);
  process.exit(1);
});

child.on("exit", (code) => {
  if (code !== 0) {
    console.log(`\n⚠️  Backend exited with code ${code}`);
  } else {
    console.log("\n✅ Backend stopped successfully");
  }
  process.exit(code);
});

// Handle Ctrl+C gracefully
process.on("SIGINT", () => {
  console.log("\n🛑 Stopping backend...");
  child.kill("SIGINT");
});
