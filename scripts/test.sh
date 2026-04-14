#!/usr/bin/env bash
set -euo pipefail

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
NC='\033[0m'

echo -e "${BOLD}${CYAN}"
echo "╔══════════════════════════════════╗"
echo "║         PFA Test Runner          ║"
echo "╚══════════════════════════════════╝"
echo -e "${NC}"

# ── Preflight ─────────────────────────────────────────────────
docker compose version >/dev/null 2>&1 || {
  echo -e "${RED}Docker Compose v2 required (docker compose)${NC}"
  exit 1
}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$(cd "$SCRIPT_DIR/../backend" && pwd)"

# ── Parse flags ───────────────────────────────────────────────
TEST_CLASS=""
TEST_METHOD=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --class)  TEST_CLASS="$2";  shift 2 ;;
    --method) TEST_METHOD="$2"; shift 2 ;;
    *) echo -e "${RED}Unknown argument: $1${NC}"; exit 1 ;;
  esac
done

# ── Test type selection ───────────────────────────────────────
if [[ -z "$TEST_CLASS" ]]; then
  echo -e "What do you want to run?\n"
  echo -e "  ${CYAN}1)${NC} All tests"
  echo -e "  ${CYAN}2)${NC} Specific class"
  echo -e "  ${CYAN}3)${NC} Specific class + method"
  echo -e "  ${CYAN}4)${NC} Clean build only (skip tests)"
  echo ""
  read -rp "Choice [1-4]: " choice

  case "$choice" in
    1) MODE="all" ;;
    2)
      MODE="class"
      read -rp "Class name (e.g. UserControllerTest): " TEST_CLASS
      ;;
    3)
      MODE="method"
      read -rp "Class name (e.g. UserControllerTest): "  TEST_CLASS
      read -rp "Method name (e.g. testLogin): " TEST_METHOD
      ;;
    4) MODE="skip" ;;
    *) echo -e "${RED}Invalid choice.${NC}"; exit 1 ;;
  esac
else
  MODE=$( [[ -n "$TEST_METHOD" ]] && echo "method" || echo "class" )
fi

# ── Build Maven command ───────────────────────────────────────
cd "$BACKEND_DIR"

case "$MODE" in
  all)
    echo -e "\nRunning ${BOLD}all tests${NC}...\n"
    MVN_CMD="./mvnw clean test -e"
    ;;
  class)
    echo -e "\nRunning class ${CYAN}${TEST_CLASS}${NC}...\n"
    MVN_CMD="./mvnw clean test -e -Dtest=${TEST_CLASS}"
    ;;
  method)
    echo -e "\nRunning ${CYAN}${TEST_CLASS}#${TEST_METHOD}${NC}...\n"
    MVN_CMD="./mvnw clean test -e -Dtest=${TEST_CLASS}#${TEST_METHOD}"
    ;;
  skip)
    echo -e "\nRunning ${BOLD}clean build${NC} (tests skipped)...\n"
    MVN_CMD="./mvnw clean install -DskipTests"
    ;;
esac

echo -e "${YELLOW}Command: ${MVN_CMD}${NC}\n"

if eval "$MVN_CMD"; then
  echo ""
  echo -e "${GREEN}${BOLD}✔ Done!${NC}"
else
  echo ""
  echo -e "${RED}${BOLD}✗ Failed.${NC}"
  exit 1
fi
