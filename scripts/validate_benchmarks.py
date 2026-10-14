#!/usr/bin/env python3
"""Validate the shared Stage 0 schema. --allow-draft permits <15 unverified cases."""
import argparse
import json
from pathlib import Path
from jsonschema import Draft202012Validator, FormatChecker

root = Path(__file__).resolve().parent.parent
p = argparse.ArgumentParser()
p.add_argument('--allow-draft', action='store_true')
args = p.parse_args()
schema = json.loads((root / 'tests/routing-cases/schema.json').read_text(encoding='utf-8'))
data = json.loads((root / 'tests/routing-cases/taiwan_vehicle_benchmarks.json').read_text(encoding='utf-8'))
Draft202012Validator.check_schema(schema)
errors = list(Draft202012Validator(schema, format_checker=FormatChecker()).iter_errors(data))
ids = [x['caseId'] for x in data.get('cases', []) if 'caseId' in x]
if len(ids) != len(set(ids)):
    raise SystemExit('FAIL: duplicate caseId')
if errors:
    for err in errors[:10]:
        print('FAIL:', '/'.join(str(x) for x in err.path), err.message)
    raise SystemExit(1)
count = len(data['cases'])
print(f'JSON schema valid, {count} cases')
if count < 15:
    if args.allow_draft:
        print('DRAFT ONLY: Stage 0 acceptance requires >=15 cases')
    else:
        raise SystemExit('FAIL: Stage 0 >=15 reviewable cases missing')
