#!/usr/bin/env bash
# B-06: the frame benchmark on the attached phone, by the protocol in docs/backlog/B-06-frame-cost-measurement.md.
#
#   scripts/device-bench.sh install                      build the release, install it, compile speed-profile
#   scripts/device-bench.sh run <out dir> [rounds] [s]   the benchmark, its logs pulled into <out dir>
#   scripts/device-bench.sh projection <out dir>         the projection's first frames, likewise
#   scripts/device-bench.sh show <mode> [extra am args]  an arm, a form or the kit on screen, for a look
#   scripts/device-bench.sh stop                         takes the activity down
set -euo pipefail

PKG=io.github.youndie.plakat.bench
ACT="$PKG/io.github.youndie.plakat.bench.android.BenchActivity"
ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"
FILES="/sdcard/Android/data/$PKG/files"

wait_done() {
  local deadline=$(($(date +%s) + $1))
  while [ "$(date +%s)" -lt "$deadline" ]; do
    if "$ADB" logcat -d -s PlakatBench:I | grep -q DONE; then return 0; fi
    sleep 5
  done
  echo "no DONE within $1 s" >&2
  return 1
}

case "${1:-}" in
  install)
    ./gradlew :bench-android:assembleRelease -q
    "$ADB" install -r bench-android/build/outputs/apk/release/bench-android-release.apk
    "$ADB" shell cmd package compile -m speed-profile -f "$PKG"
    ;;
  run | projection)
    out="$2"
    mkdir -p "$out"
    commit="$(git rev-parse --short HEAD)"
    [ -z "$(git status --short)" ] || commit="$commit-dirty"
    "$ADB" logcat -c
    "$ADB" shell input keyevent KEYCODE_WAKEUP
    "$ADB" shell am force-stop "$PKG"
    if [ "$1" = run ]; then
      rounds="${3:-6}"
      seconds="${4:-4}"
      "$ADB" shell am start -n "$ACT" --es mode bench --es commit "$commit" --ei rounds "$rounds" --ei seconds "$seconds"
      wait_done $((rounds * 13 * seconds + 120))
      "$ADB" pull "$FILES/frames.csv" "$out/frames.csv"
      "$ADB" pull "$FILES/metrics.csv" "$out/metrics.csv"
    else
      "$ADB" shell am start -n "$ACT" --es mode projection --es commit "$commit"
      wait_done 180
      "$ADB" pull "$FILES/projection.csv" "$out/projection.csv"
    fi
    ;;
  show)
    "$ADB" shell input keyevent KEYCODE_WAKEUP
    "$ADB" shell am force-stop "$PKG"
    mode="$2"
    shift 2
    "$ADB" shell am start -n "$ACT" --es mode "$mode" "$@"
    ;;
  stop)
    "$ADB" shell am force-stop "$PKG"
    ;;
  *)
    sed -n 2,9p "$0"
    exit 2
    ;;
esac
