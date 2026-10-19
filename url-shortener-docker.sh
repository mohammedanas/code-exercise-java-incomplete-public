#!/bin/sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
cd "$SCRIPT_DIR"

command=${1:-help}
case "$command" in
    up)          set -- up -d ;;
    down)        set -- down ;;
    restart)     set -- restart ;;
    rebuild)     set -- up -d --build --force-recreate ;;
    logs)        set -- logs --tail 100 ;;
    logs-follow) set -- logs --tail 100 -f ;;
    status)      set -- ps --all ;;
    clean)       set -- down --rmi local --remove-orphans ;;
    help|-h|--help)
        cat <<'EOF'
URL Shortener Docker commands:
  ./url-shortener-docker.sh up           Start the application
  ./url-shortener-docker.sh down         Stop and remove containers
  ./url-shortener-docker.sh restart      Restart running containers
  ./url-shortener-docker.sh rebuild      Rebuild and recreate services
  ./url-shortener-docker.sh logs         Show the last 100 log lines
  ./url-shortener-docker.sh logs-follow  Follow service logs
  ./url-shortener-docker.sh status       Show container status
  ./url-shortener-docker.sh clean        Remove containers and project images

All commands preserve the urlshortener-data database volume.
EOF
        exit 0
        ;;
    *)
        printf 'Unknown command: %s\n' "$command" >&2
        exit 2
        ;;
esac

exec docker compose "$@"