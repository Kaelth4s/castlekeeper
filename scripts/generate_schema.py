import yaml
import sys

def generate_mermaid(data):
    lines = ["```mermaid", "%%{init: {'theme': 'neutral'}}%%", "erDiagram"]
    for t in data["tables"]:
        for rel in t.get("relationships", []):
            label = rel["fk_local"] if "fk_local" in rel else ""
            if rel["type"] == "one_to_many":
                lines.append(f"    {t['name']} ||--o{{ {rel['target']} : {label}")
            elif rel["type"] == "many_to_one":
                lines.append(f"    {t['name']} }}o--|| {rel['target']} : {label}")
            elif rel["type"] == "one_to_one":
                lines.append(f"    {t['name']} ||--|| {rel['target']} : {label}")
    for t in data["tables"]:
        lines.append(f"    {t['name']} {{")
        for col in t["columns"]:
            modifiers = ""
            if col.get("pk"):
                modifiers += " PK"
            if col.get("fk"):
                modifiers += " FK"
            if col.get("unique"):
                modifiers += " UK"
            nullable = " nullable" if col.get("nullable") else ""
            lines.append(f"        {col['type']} {col['name']}{modifiers}{nullable}")
        lines.append("    }")
    lines.append("```")
    return "\n".join(lines)

def main():
    with open(sys.argv[1], 'r', encoding='utf-8') as f:
        data = yaml.safe_load(f)
    md = generate_mermaid(data)
    with open(sys.argv[2], 'w', encoding='utf-8') as f:
        f.write("# Database Schema (Draft)\n\n")
        f.write(md)
        f.write("\n\n*Generated from schema-draft.yaml.*\n")

if __name__ == "__main__":
    main()