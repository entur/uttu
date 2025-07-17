#!/bin/bash  -e

SCRIPT_PATH="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

find "$SCRIPT_PATH/db/migration" -type f -name 'V*__*.sql' | sort -V | while read -r f; do
  echo "Running migration for $f"
  PGPASSWORD=uttu psql -U uttu -h localhost -p 5432 -f "$f"
done
