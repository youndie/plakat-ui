// Mocha's own limit of 2 s per test was never chosen here. A test that hashes every seed of every
// frame takes 0.7–0.9 s on a developer's Mac, and on a hosted runner it went over on some runs and
// not on others, with the same tree (B-71). This limit is a watchdog for a test that hangs; it
// compares nothing, and what a test asserts is the same under any value.
config.client = config.client || {};
config.client.mocha = Object.assign(config.client.mocha || {}, { timeout: 30000 });
