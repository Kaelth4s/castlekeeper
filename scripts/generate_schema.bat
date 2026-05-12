@echo off
cd /d "%~dp0.."
python scripts\generate_schema.py notes\database\schema-draft.yaml notes\database\schema.md