"""Offline tests for Stage 2 response parser; no Docker/network required."""
import pathlib
import sys
import unittest

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1] / "scripts"))
import smoke_valhalla


class SmokeValhallaValidationTest(unittest.TestCase):
    def test_valid_route_response_is_unverified(self):
        fixture = {
            "trip": {
                "summary": {"length": 6.25, "time": 850},
                "legs": [{"shape": "encoded-polyline-example"}],
            }
        }
        result = smoke_valhalla.validate_trip(fixture, 17.5)
        self.assertEqual(result["result"], "POC_ROUTE_RETURNED_NOT_LEGALLY_VERIFIED")
        self.assertEqual(result["profile"], "auto")
        self.assertEqual(result["length_km"], 6.25)
        self.assertEqual(result["reported_travel_seconds"], 850)
        self.assertFalse(result["legal_routing_verified"])
        self.assertFalse(result["motorcycle_profiles_verified"])
        self.assertEqual(result["leg_count"], 1)
        self.assertEqual(len(result["encoded_geometry_sha256"]), 64)

    def test_missing_trip_raises(self):
        with self.assertRaisesRegex(ValueError, "Missing trip"):
            smoke_valhalla.validate_trip({"status": "error"}, 12)

    def test_missing_legs_raises(self):
        with self.assertRaisesRegex(ValueError, "trip.summary or trip.legs"):
            smoke_valhalla.validate_trip({"trip": {"summary": {"length": 1, "time": 90}}}, 4)

    def test_missing_shape_raises(self):
        with self.assertRaisesRegex(ValueError, "shape"):
            smoke_valhalla.validate_trip(
                {"trip": {"summary": {"length": 1, "time": 90}, "legs": [{}]}}, 4
            )

    def test_missing_travel_time_raises(self):
        with self.assertRaisesRegex(ValueError, "time"):
            smoke_valhalla.validate_trip(
                {"trip": {"summary": {"length": 1}, "legs": [{"shape": "abc"}]}}, 4
            )


if __name__ == "__main__":
    unittest.main()
