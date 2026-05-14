# Bot Commands

**Reply Keyboard** for navigation. **Inline Keyboard** for CRUD pickers (5/page with ◀▶).

## Navigation

| Command | Keyboard |
|---------|----------|
| `/start` | Reply: [🚪 Войти] [📜 Список замков] |
| `/menu` | Reply: [🏰 Замки] [👥 Авторы] [🏷️ Титулы] [🧱 Материалы] [🔨 Реконстр.] [🚪 Выйти] |
| `/cancel` | Exit any wizard dialog |

## CRUD — all 5 entities

Each wing submenu (Reply): 📜 All · 👁 Select · 🏗 Add · ✒ Edit · 💥 Delete · ↩️ Back.

| Entity | Add Wizard Steps | FK Pickers (inline 5/page) |
|--------|-----------------|---------------------------|
| 🏰 Castle | 7: name→desc→author→built→destroyed→height→material | Author, Material |
| 👥 Author | 2: name→type | Author Type |
| 🏷️ Author Type | 2: name→description | — |
| 🧱 Material | 1: name | — |
| 🔨 Reconstruction | 3: castle→author→year | Castle, Author |

**Select / Edit / Delete:** inline keyboard → pick entity → view details / choose field / confirm ✅/❌.

## UX details

- Inline keyboards **removed** after callback (prevents double-click)
- Reply keyboard **restored** after wizard/CRUD completion
- **Cancel** returns to current submenu (not main menu)
- Invalid input in wizard → bot asks again on same step
- State auto-expires after 30 min of inactivity

## Dialog State Machine

```
IDLE → STEP_1 → STEP_2 → ... → CONFIRM → IDLE
```

Wizard steps stored per chat in `DialogStateMachine`. Server API called only on confirm. State cleared on cancel, completion, or timeout.
