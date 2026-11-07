// Mocha's own limit of 2 s per test was never chosen here. A test that hashes every seed of every
// frame takes 0.7–0.9 s on a developer's Mac, and on a hosted runner it went over on some runs and
// not on others, with the same tree (B-71). These limits are a watchdog for a test that hangs; they
// compare nothing, and what a test asserts is the same under any value.
//
// A test holds the browser's thread, so Karma hears nothing from it meanwhile. Under Karma's own
// limits (a ping unanswered for 5 s, no message for 30 s) the whole run is dropped without naming
// the test, so both stand above Mocha's: a test too slow fails by name at 30 s, and one that never
// ends turns the run red at 60 s.
config.client = config.client || {};
config.client.mocha = Object.assign(config.client.mocha || {}, { timeout: 30000 });
config.set({ pingTimeout: 60000, browserNoActivityTimeout: 60000 });
