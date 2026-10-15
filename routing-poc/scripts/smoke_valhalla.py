#!/usr/bin/env python3
"""Stage 2 PoC-only Valhalla API smoke test. This does NOT certify route legality.

Uses two public Taipei landmarks, no personal GPS/location or Google Places data.
Outputs a JSON evidence record for manual evaluation of routing API availability.
"""
import argparse
import hashlib
import json
import sys
import time
import urllib.error
import urllib.request


TEST_LOCATIONS = [
    {"lat": 25.0478, "lon": 121.5170},  # Taipei Main Station, approximate
    {"lat": 25.0338, "lon": 121.5645},  # Taipei 101, approximate
]


def validate_trip(data, elapsed_ms):
    """Validate the minimum real response shape without claiming road compliance."""
    trip = data.get("trip")
    if not isinstance(trip, dict):
        raise ValueError("Missing trip in Valhalla response")
    summary = trip.get("summary")
    legs = trip.get("legs")
    if not isinstance(summary, dict) or not isinstance(legs, list) or not legs:
        raise ValueError("Route response missing trip.summary or trip.legs")
    shapes = [leg.get("shape", "") for leg in legs if isinstance(leg, dict)]
    if not all(isinstance(shape, str) and shape for shape in shapes) or len(shapes) != len(legs):
        raise ValueError("At least one route leg lacks an encoded shape")
    if not isinstance(summary.get("length"), (int, float)):
        raise ValueError("Route summary missing numeric length")
    if not isinstance(summary.get("time"), (int, float)):
        raise ValueError("Route summary missing numeric time")
    return {
        "result": "POC_ROUTE_RETURNED_NOT_LEGALLY_VERIFIED",
        "fixture": "taipei_main_station_to_taipei_101_approximate",
        "profile": "auto",
        "elapsed_ms": round(elapsed_ms, 2),
        "length_km": summary["length"],
        "reported_travel_seconds": summary["time"],
        "leg_count": len(legs),
        "encoded_geometry_sha256": hashlib.sha256(
            "|".join(shapes).encode("utf-8")
        ).hexdigest(),
        "legal_routing_verified": False,
        "motorcycle_profiles_verified": False,
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base-url", default="http://127.0.0.1:8002")
    parser.add_argument("--timeout", type=float, default=30)
    parser.add_argument("--output", help="Optional JSON results file; do not commit real user routes")
    args = parser.parse_args()
    payload = json.dumps({
        "locations": TEST_LOCATIONS,
        "costing": "auto",  # Engine sanity check only; NOT a Taiwan legal car profile.
        "units": "kilometers",
    }).encode("utf-8")
    request = urllib.request.Request(
        args.base_url.rstrip("/") + "/route",
        data=payload,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    started = time.monotonic()
    try:
        with urllib.request.urlopen(request, timeout=args.timeout) as response:
            data = json.load(response)
        record = validate_trip(data, (time.monotonic() - started) * 1000)
        print(json.dumps(record, ensure_ascii=False, indent=2))
        if args.output:
            with open(args.output, "w", encoding="utf-8") as fp:
                json.dump(record, fp, ensure_ascii=False, indent=2)
                fp.write("\n")
        return 0
    except (urllib.error.URLError, TimeoutError, ValueError, KeyError, json.JSONDecodeError) as exc:
        print("PoC smoke test FAILED: " + str(exc), file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
