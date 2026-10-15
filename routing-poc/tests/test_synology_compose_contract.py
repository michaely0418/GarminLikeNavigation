"""Offline safety contract tests for NAS Compose configuration."""
import pathlib
import sys
import unittest

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parents[1] / "scripts"))
from verify_synology_compose import validate


class NASComposeTest(unittest.TestCase):
    def setUp(self):
        self.good = {
            "services": {
                "valhalla": {
                    "ports": [{"host_ip": "127.0.0.1", "target": 8002, "published": "8002"}],
                    "volumes": [{"type": "bind", "source": "/volume1/docker/garmin-routing/valhalla/data", "target": "/custom_files"}],
                    "mem_limit": 6442450944,
                },
            }
        }

    def test_qualified_localhost(self):
        self.assertTrue(validate(self.good, "valhalla", 8002))

    def test_reject_lan_wide_bind(self):
        self.good["services"]["valhalla"]["ports"][0]["host_ip"] = "0.0.0.0"
        with self.assertRaisesRegex(ValueError, "outside NAS localhost"):
            validate(self.good, "valhalla", 8002)

    def test_reject_missing_memory_ceiling(self):
        del self.good["services"]["valhalla"]["mem_limit"]
        with self.assertRaisesRegex(ValueError, "Memory ceiling"):
            validate(self.good, "valhalla", 8002)

    def test_reject_missing_volume(self):
        self.good["services"]["valhalla"]["volumes"] = []
        with self.assertRaisesRegex(ValueError, "bind volume"):
            validate(self.good, "valhalla", 8002)


if __name__ == "__main__":
    unittest.main()
