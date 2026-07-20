# Contributing to StockPilot AI

Thanks for contributing. This guide keeps changes consistent across the monorepo.

## Before you start

- Read the root `README.md` and relevant docs under `docs/`.
- Do not commit secrets. Use `.env.example` as the template; keep real values in a local `.env`.
- Prefer small, focused pull requests over large mixed changes.

## Repository layout

| Path | Purpose |
|------|---------|
| `backend/` | API and core services |
| `frontend/` | Web client |
| `ai-service/` | AI / ML services |
| `database/` | Schema, migrations, seeds |
| `docker/` | Container configuration |
| `docs/` | Product and architecture docs |
| `scripts/` | Automation utilities |
| `infrastructure/` | Cloud / IaC |
| `tests/` | Cross-cutting tests |
| `.github/workflows/` | CI/CD workflows |

## How to contribute

1. **Create a branch** from `main` using a clear name, for example:
   - `feature/<short-description>`
   - `fix/<short-description>`
   - `docs/<short-description>`
2. **Make your changes** in the appropriate package or docs folder.
3. **Keep the scaffold rules** until implementation starts: do not add unrelated boilerplate or generated app skeletons without agreement.
4. **Update docs** when behavior, APIs, or architecture change (`docs/` mirrors the decision record).
5. **Open a pull request** with:
   - What changed and why
   - How to verify (commands, screenshots, or checklist)
   - Linked issue or sprint item, if any

## Commit messages

Use concise, imperative subjects:

- `add user watchlist API endpoints`
- `fix null handling in portfolio summary`
- `update HLD for AI scoring pipeline`

Avoid vague messages like `update` or `fixes`.

## Code review expectations

- Changes should be readable, scoped, and documented where non-obvious.
- Prefer clarity over cleverness.
- Call out breaking changes, migrations, and new environment variables in the PR description.
- New env vars must be reflected in `.env.example`.

## Documentation contributions

Place materials in the matching `docs/` folder:

- Research → `01-Research`
- Requirements → `02-PRD`
- Personas / stories → `03-User-Personas`, `04-User-Stories`
- Design → `05-Wireframes`, `06-HLD`, `07-LLD`
- Data / API / AI → `08-Database`, `09-API`, `10-AI`
- Delivery → `11-Sprints`, `12-Deployment`
- Media → `Assets/` (`images/`, `diagrams/`, `logos/`, `icons/`)

## Security

- Never commit API keys, tokens, passwords, or private certificates.
- Report suspected vulnerabilities privately to the maintainers; do not open a public issue with exploit details.

## Questions

If something is unclear, open a discussion issue or ask in the team channel before large refactors.
