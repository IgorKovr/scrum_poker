#!/usr/bin/env node

/**
 * Frontend-only runner for the Scrum Poker application
 * This allows running just the frontend from Cursor's "Run" button
 */

const { spawn } = require("child_process");
const path = require("path");

console.log("⚛️ Starting Scrum Poker Frontend...\n");

// Run the frontend
const frontendPath = path.join(__dirname, "frontend");
const child = spawn("npm", ["run", "dev"], {
  stdio: "inherit",
  cwd: frontendPath,
});

child.on("error", (error) => {
  console.error(`❌ Error starting frontend: ${error.message}`);
  process.exit(1);
});

child.on("exit", (code) => {
  if (code !== 0) {
    console.log(`\n⚠️  Frontend exited with code ${code}`);
  } else {
    console.log("\n✅ Frontend stopped successfully");
  }
  process.exit(code);
});

// Handle Ctrl+C gracefully
process.on("SIGINT", () => {
  console.log("\n🛑 Stopping frontend...");
  child.kill("SIGINT");
});
