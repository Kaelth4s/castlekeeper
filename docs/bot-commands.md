# Bot Commands

All commands use inline keyboards (5 items per page, ◀ ▶ pagination) for selection-based actions.  
Text lists (no buttons) for browsing commands.

---

## Navigation

| Command | Description | Response |
|---------|-------------|----------|
| `/start` | Welcome screen at castle gates | Medieval narrative: gatekeeper invites you in |
| `/menu` | Main hall — 3 wings | Three buttons: Castles, Authors, Materials + Help |
| `/help` | Foliant of all commands | Full command reference with categories |

---

## Castles (🏰)

### Browse
| Command | Description | Example |
|---------|-------------|---------|
| `/castles` | List all castles (text) | → "📜 You unroll the scroll..." then castle list |
| `Menu → 👁️ Select` | View a castle (inline picker) | → inline keyboard: 5 castles per page → tap → full castle info |

### Create (Wizard)
| Step | Prompt | Input type |
|------|--------|-----------|
| 1 | Enter castle name | Free text |
| 2 | Enter description | Free text (multiline ok) |
| 3 | Select author | **Inline keyboard** (5 authors/page) |
| 4 | Enter year built | Integer (or "skip" button) |
| 5 | Enter year destroyed | Integer (or "skip" button) |
| 6 | Enter height (meters) | Decimal number (or "skip" button) |
| 7 | Select material | **Inline keyboard** (5 materials/page) |
| — | Confirm | `✅ Save` / `❌ Cancel` |

### Edit
| Step | Prompt | Input type |
|------|--------|-----------|
| 1 | Select castle | **Inline keyboard** (5 castles/page) |
| 2 | Select field to edit | **Inline keyboard**: Name, Description, Author, Year Built, Year Destroyed, Height, Material + ✅ Done |
| 3 | Enter new value | Text / number / **FK picker** for Author/Material |
| — | Confirm | Auto-saves on `✅ Done` |

### Delete
| Step | Prompt | Input type |
|------|--------|-----------|
| 1 | Select castle | **Inline keyboard** (5 castles/page) |
| 2 | Confirm deletion | `✅ Yes` / `❌ No` |

### Other
| Command | Description |
|---------|-------------|
| `/random` | Random castle (text output) |

---

## Authors (👥)

### Browse
| Command | Description | Example |
|---------|-------------|---------|
| `/authors` | List all authors (text) | → "📜 Scriptorium: scroll of chroniclers..." |
| `Menu → 👁️ Select` | View author (inline picker) | → tap → name + type + description |

### Create (Wizard)
| Step | Prompt | Input type |
|------|--------|-----------|
| 1 | Enter author name | Free text |
| 2 | Select author type | **Inline keyboard** (5 types/page) |
| — | Confirm | `✅ Save` / `❌ Cancel` |

### Edit
| Step | Prompt | Input type |
|------|--------|-----------|
| 1 | Select author | **Inline keyboard** (5 authors/page) |
| 2 | Select field | **Inline keyboard**: Name, Type + ✅ Done |
| 3 | Enter new value | Text / **FK picker** for Type |

### Delete
| Step | Prompt | Input type |
|------|--------|-----------|
| 1 | Select author | **Inline keyboard** (5 authors/page) |
| 2 | Confirm | `✅ Yes` / `❌ No` |

---

## Author Types (🏷️)

### Browse
| Command | Description |
|---------|-------------|
| `/author_types` | List all types (text) |

### Create (Wizard)
| Step | Prompt | Input type |
|------|--------|-----------|
| 1 | Enter type name | Free text |
| 2 | Enter description (optional) | Free text |
| — | Confirm | `✅ Save` / `❌ Cancel` |

### Edit
| Step | Prompt | Input type |
|------|--------|-----------|
| 1 | Select type | **Inline keyboard** (5 types/page) |
| 2 | Select field | **Inline keyboard**: Name, Description + ✅ Done |
| 3 | Enter new value | Free text |

### Delete
| Step | Prompt | Input type |
|------|--------|-----------|
| 1 | Select type | **Inline keyboard** (5 types/page) |
| 2 | Confirm | `✅ Yes` / `❌ No` |

---

## Materials (🧱)

### Browse
| Command | Description |
|---------|-------------|
| `/materials` | List all materials (text) |

### Create
| Step | Prompt | Input type |
|------|--------|-----------|
| 1 | Enter material name | Free text |
| — | Confirm | `✅ Save` / `❌ Cancel` |

### Edit
| Step | Prompt | Input type |
|------|--------|-----------|
| 1 | Select material | **Inline keyboard** (5 materials/page) |
| 2 | Enter new name | Free text |
| — | Confirm | `✅ Save` / `❌ Cancel` |

### Delete
| Step | Prompt | Input type |
|------|--------|-----------|
| 1 | Select material | **Inline keyboard** (5 materials/page) |
| 2 | Confirm | `✅ Yes` / `❌ No` |

---

## Dialog State Machine

The bot tracks each user's position in multi-step dialogs.  
Example flow for adding a castle:

```
IDLE → ADD_CASTLE_NAME → ADD_CASTLE_DESC → ADD_CASTLE_AUTHOR
  → ADD_CASTLE_BUILT → ADD_CASTLE_DESTROYED → ADD_CASTLE_HEIGHT
  → ADD_CASTLE_MATERIAL → ADD_CASTLE_CONFIRM → IDLE
```

Each step:
- Bot renders the prompt + optional inline keyboard
- User responds with text or taps a button
- Bot validates, stores data in state map, advances to next step
- On cancel or timeout, state resets to IDLE

> 🗺️ Full visual flow: `notes/bot/dialogs.canvas` (open in Obsidian)
