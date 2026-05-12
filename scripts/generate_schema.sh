#!/bin/bash
cd "$(dirname "$0")/.."
python scripts/generate_schema.py notes/database/schema-draft.yaml notes/database/schema.md