#!/usr/bin/env bash
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

echo -e "${BOLD}${CYAN}"
echo "╔══════════════════════════════════╗"
echo "║         PFA Startup Tool         ║"
echo "╚══════════════════════════════════╝"
echo -e "${NC}"

# ── Preflight ─────────────────────────────────────────────────
docker compose version >/dev/null 2>&1 || {
  echo -e "${RED}Docker Compose v2 required (docker compose)${NC}"
  exit 1
}

# ── Resolve Paths ─────────────────────────────────────────────
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$REPO_ROOT"

# ── Start ─────────────────────────────────────────────────────
echo -e "${CYAN}Building and starting PFA stack...${NC}\n"

docker compose up -d --build

echo ""
echo -e "${GREEN}${BOLD}✔ PFA is up!${NC}"
echo -e "  Frontend:  ${CYAN}http://localhost:3000${NC}"
echo -e "  Backend:   ${CYAN}http://localhost:8080${NC}"
