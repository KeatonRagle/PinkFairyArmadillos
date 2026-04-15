#!/bin/bash
# ============================================================
# PFA Scrape Script
# Place in: <repo>/scripts/scrape.sh
# Usage:    ./scripts/scrape.sh
# ============================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

BASE_URL=""

echo -e "${BOLD}${CYAN}"
echo "╔══════════════════════════════════╗"
echo "║        PFA Scrape Tool           ║"
echo "╚══════════════════════════════════╝"
echo -e "${NC}"

# ── Environment Selection ────────────────────────────────────
echo -e "Select environment:"
echo -e "  ${CYAN}1)${NC} dev"
echo -e "  ${CYAN}2)${NC} prod"
read -rp "Choice [1/2]: " env_choice
  case "$env_choice" in
  1)
    BASE_URL="http://localhost:8080/api"
    RUNNING_BACKEND=$(docker compose ps --status running --quiet backend 2>/dev/null)
    RUNNING_DB=$(docker compose ps --status running --quiet db 2>/dev/null)
    if [[ -z "$RUNNING_BACKEND" ]] || [[ -z "$RUNNING_DB" ]]; then
      echo -e "${RED}PFA is not running. Start it with: ./scripts/run.sh${NC}"
      exit 1
    fi
    ;;
  2) BASE_URL="https://api.adoptpetsforall.com/api" ;;
  *) echo -e "${RED}Invalid choice.${NC}"; exit 1 ;;
esac

# ── Credentials ──────────────────────────────────────────────
read -rp  "Email:    " EMAIL
read -rsp "Password: " PASSWORD
echo ""
echo ""

# ── Login ────────────────────────────────────────────────────
echo -e "Logging in as ${CYAN}${EMAIL}${NC}..."

LOGIN_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "${BASE_URL}/users/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${EMAIL}\",\"password\":\"${PASSWORD}\"}")

HTTP_CODE=$(echo "$LOGIN_RESPONSE" | tail -1)
BODY=$(echo "$LOGIN_RESPONSE" | head -1)

if [[ "$HTTP_CODE" != "200" ]]; then
  echo -e "${RED}Login failed (HTTP ${HTTP_CODE}):${NC}"
  echo "$BODY"
  exit 1
fi

TOKEN=$(echo "$BODY" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [[ -z "$TOKEN" ]]; then
  echo -e "${RED}Could not parse token from response.${NC}"
  echo "$BODY"
  exit 1
fi

echo -e "${GREEN}✔ Login successful${NC}"
echo -e "Token: ${CYAN}${TOKEN:0:40}...${NC}\n"

# ── Scrape ───────────────────────────────────────────────────
echo -e "Starting scrape (this may take a while)...\n"

RESPONSE=$(curl -s -w "\n%{http_code}" -X GET "${BASE_URL}/webScraper/scrape" \
  -H "Authorization: Bearer ${TOKEN}")

HTTP_CODE=$(echo "$RESPONSE" | tail -1)
BODY=$(echo "$RESPONSE" | head -1)

echo ""
if [[ "$HTTP_CODE" == "200" ]]; then
  echo -e "${GREEN}${BOLD}✔ ${BODY}${NC}"
else
  echo -e "${RED}Scrape failed (HTTP ${HTTP_CODE}): ${BODY}${NC}"
  exit 1
fi
