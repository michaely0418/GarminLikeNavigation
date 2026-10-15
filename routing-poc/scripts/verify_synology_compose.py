#!/usr/bin/env python3
"""Validate Synology Compose JSON. No Docker container execution is performed."""
import json
import pathlib
import sys


def validate(data, service_name, expected_port):
    services = data.get("services")
    if not isinstance(services, dict) or service_name not in services:
        raise ValueError("Missing expected service: " + service_name)
    service = services[service_name]
    ports = service.get("ports") or []
    if not ports:
        raise ValueError("No explicit loopback-only port mapping")
    for port in ports:
        if not isinstance(port, dict) or port.get("host_ip") != "127.0.0.1":
            raise ValueError("A container port is bound outside NAS localhost")
    if not any(int(p.get("target", -1)) == expected_port for p in ports):
        raise ValueError("Expected application port not found")
    if not service.get("volumes"):
        raise ValueError("Persistent host bind volume absent")
    if not service.get("mem_limit"):
        raise ValueError("Memory ceiling absent on shared NAS")
    return True


def main(args):
    if len(args) != 4:
        raise ValueError("Usage: verify_synology_compose.py <compose-json> <service> <port>")
    contents = json.loads(pathlib.Path(args[1]).read_text(encoding="utf-8"))
    validate(contents, args[2], int(args[3]))
    print(f"Synology Compose local-only safety contract OK: {args[2]}")


if __name__ == "__main__":
    try:
        main(sys.argv)
    except (ValueError, KeyError, OSError) as exc:
        sys.exit("Synology configuration gate FAILED: " + str(exc))
