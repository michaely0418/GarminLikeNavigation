#!/usr/bin/env python3
"""Repeatable local-only route API comparison; no Taiwan road-law certification.

Use with SSH tunnels to 127.0.0.1:8002 (Valhalla) and :8989 (GraphHopper).
Only publicly known landmarks are used; never submit personal GPS coordinates.
"""
import argparse
import hashlib
import json
import math
import statistics
import sys
import time
import urllib.parse
import urllib.request


# Public fixed reference landmarks (approximate). Do not use Google Places content.
PAIRS = {
    "taipei_station_to_101": ((25.0478, 121.5170), (25.0338, 121.5645)),
    "taipei_101_to_station": ((25.0338, 121.5645), (25.0478, 121.5170)),
}


def percentile(samples, pct):
    if not samples:
        raise ValueError("No samples")
    seq = sorted(samples)
    return seq[max(0, min(len(seq) - 1, math.ceil(pct * len(seq)) - 1))]


def normalize_valhalla(payload):
    trip = payload.get("trip")
    if not isinstance(trip, dict) or not isinstance(trip.get("legs"), list):
        raise ValueError("Valhalla trip/legs missing")
    summary = trip.get("summary", {})
    shapes = [leg.get("shape", "") for leg in trip["legs"]]
    if not shapes or not all(isinstance(sh, str) and sh for sh in shapes):
        raise ValueError("Valhalla route geometry missing")
    length = summary.get("length")
    seconds = summary.get("time")
    if not isinstance(length, (int, float)) or not isinstance(seconds, (int, float)):
        raise ValueError("Valhalla summary length/time missing")
    return float(length), float(seconds), hashlib.sha256("|".join(shapes).encode()).hexdigest()


def normalize_graphhopper(payload):
    paths = payload.get("paths")
    if not isinstance(paths, list) or not paths:
        raise ValueError("GraphHopper paths missing")
    path = paths[0]
    distance, duration = path.get("distance"), path.get("time")
    shape = path.get("points")
    if not isinstance(distance, (int, float)) or not isinstance(duration, (int, float)):
        raise ValueError("GraphHopper distance/time missing")
    if not shape:
        raise ValueError("GraphHopper route geometry missing")
    # GraphHopper defaults distance meters / time milliseconds.
    raw = json.dumps(shape, sort_keys=True, ensure_ascii=False, separators=(",", ":"))
    return float(distance) / 1000.0, float(duration) / 1000.0, hashlib.sha256(raw.encode()).hexdigest()


def request_route(engine, url, points, timeout):
    (lat1, lon1), (lat2, lon2) = points
    if engine == "valhalla":
        body = json.dumps({
            "locations": [{"lat": lat1, "lon": lon1}, {"lat": lat2, "lon": lon2}],
            "costing": "auto",
            "units": "kilometers",
        }).encode("utf-8")
        req = urllib.request.Request(
            url.rstrip("/") + "/route", data=body,
            headers={"Content-Type": "application/json"}, method="POST"
        )
    else:
        args = [
            ("point", f"{lat1},{lon1}"), ("point", f"{lat2},{lon2}"),
            ("profile", "car"), ("points_encoded", "false"), ("instructions", "true"),
        ]
        req = urllib.request.Request(url.rstrip("/") + "/route?" + urllib.parse.urlencode(args))
    started = time.perf_counter()
    with urllib.request.urlopen(req, timeout=timeout) as response:
        payload = json.load(response)
    milliseconds = (time.perf_counter() - started) * 1000.0
    km, seconds, geometry_hash = (
        normalize_valhalla(payload) if engine == "valhalla"
        else normalize_graphhopper(payload)
    )
    if km <= 0 or seconds < 0:
        raise ValueError("Unreasonable route length/duration")
    return milliseconds, km, seconds, geometry_hash


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--engine", choices=["valhalla", "graphhopper"], required=True)
    parser.add_argument("--base-url", required=True)
    parser.add_argument("--dataset-sha256", required=True, help="SHA256 of identical input OSM PBF")
    parser.add_argument("--engine-image", required=True, help="Actual image ID/digest from NAS")
    parser.add_argument("--warmup", type=int, default=3)
    parser.add_argument("--samples", type=int, default=20)
    parser.add_argument("--timeout", type=float, default=30)
    parser.add_argument("--output", required=True)
    args = parser.parse_args()
    if len(args.dataset_sha256) != 64 or any(c not in "0123456789abcdefABCDEF" for c in args.dataset_sha256):
        parser.error("--dataset-sha256 must be 64 hex characters")
    if args.warmup < 0 or args.samples < 5 or args.samples > 100:
        parser.error("--warmup >=0 and 5 <= --samples <=100 required")
    results = {
        "status": "ENGINE_POC_NOT_ROAD_LAW_VERIFIED",
        "engine": args.engine,
        "profile": "auto" if args.engine == "valhalla" else "car",
        "engine_image": args.engine_image,
        "dataset_sha256": args.dataset_sha256.lower(),
        "legal_routing_verified": False,
        "motorcycle_routing_verified": False,
        "cases": {},
    }
    for key, waypoints in PAIRS.items():
        obs = []
        last = None
        for _ in range(args.warmup + args.samples):
            datum = request_route(args.engine, args.base_url, waypoints, args.timeout)
            obs.append(datum)
        timings = [datum[0] for datum in obs[args.warmup:]]
        last = obs[-1]
        results["cases"][key] = {
            "successful_samples": len(timings),
            "warmup_excluded": args.warmup,
            "latency_p50_ms": round(statistics.median(timings), 2),
            "latency_p95_ms": round(percentile(timings, 0.95), 2),
            "reported_distance_km": round(last[1], 3),
            "reported_duration_seconds": round(last[2], 2),
            "geometry_sha256": last[3],
        }
    with open(args.output, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
        f.write("\n")
    print(json.dumps(results, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    try:
        main()
    except (OSError, ValueError, KeyError, json.JSONDecodeError) as exc:
        sys.exit("PoC benchmark failed (no PASSED legality assertion): " + str(exc))
