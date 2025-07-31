#!/usr/bin/env node

/**
 * Simple Node.js runner for the Scrum Poker application
 * This allows running the app from Cursor's "Run" button
 */

const { spawn } = require('child_process');
const path = require('path');

console.log('🚀 Starting Scrum Poker Application...\n');

// Run the bash script
const scriptPath = path.join(__dirname, 'run-local.sh');
const child = spawn('bash', [scriptPath], {
  stdio: 'inherit',
  cwd: __dirname
});

child.on('error', (error) => {
  console.error(`❌ Error starting application: ${error.message}`);
  process.exit(1);
});

child.on('exit', (code) => {
  if (code !== 0) {
    console.log(`\n⚠️  Application exited with code ${code}`);
  } else {
    console.log('\n✅ Application stopped successfully');
  }
  process.exit(code);
});

// Handle Ctrl+C gracefully
process.on('SIGINT', () => {
  console.log('\n🛑 Stopping application...');
  child.kill('SIGINT');
}); 