"""Local-only unit tests for normalized routing metrics, with synthetic payloads."""
import pathlib
import sys
import unittest

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1] / "scripts"))
from benchmark_poc import normalize_graphhopper, normalize_valhalla, percentile


class BenchmarkFixtureTest(unittest.TestCase):
    def test_valhalla_units_and_shape(self):
        km, secs, digest = normalize_valhalla({
            "trip": {
                "summary": {"length": 5.3, "time": 540},
                "legs": [{"shape": "encoded-shape"}],
            }
        })
        self.assertEqual(km, 5.3)
        self.assertEqual(secs, 540)
        self.assertEqual(len(digest), 64)

    def test_graphhopper_units_converted_to_km_and_seconds(self):
        km, secs, digest = normalize_graphhopper({
            "paths": [{
                "distance": 5300,
                "time": 540000,
                "points": {"type": "LineString", "coordinates": [[121.5, 25.0], [121.55, 25.05]]},
            }]
        })
        self.assertEqual(km, 5.3)
        self.assertEqual(secs, 540)
        self.assertEqual(len(digest), 64)

    def test_missing_graphhopper_geometry_rejected(self):
        with self.assertRaisesRegex(ValueError, "geometry"):
            normalize_graphhopper({"paths": [{"distance": 100, "time": 1000}]})

    def test_missing_valhalla_shape_rejected(self):
        with self.assertRaisesRegex(ValueError, "geometry"):
            normalize_valhalla({"trip": {"summary": {"length": 1, "time": 50}, "legs": [{}]}})

    def test_nearest_rank_p95(self):
        self.assertEqual(percentile(list(range(1, 21)), 0.95), 19)

    def test_empty_metrics_rejected(self):
        with self.assertRaises(ValueError):
            percentile([], 0.95)


if __name__ == "__main__":
    unittest.main()
