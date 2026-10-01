#!/usr/bin/env bash
# Troca atual e anterior. Rodar de novo desfaz o rollback.
source "$(dirname "$0")/lib.sh"

CURRENT="$(readlink "$BASE_DIR/atual" 2>/dev/null || true)"
PREVIOUS="$(readlink "$BASE_DIR/anterior" 2>/dev/null || true)"
[[ -n "$CURRENT" ]] || { log "não há versão implantada em $BASE_DIR/atual"; exit 1; }
[[ -n "$PREVIOUS" ]] || { log "não há versão anterior"; exit 1; }

switch_link atual "$PREVIOUS"
switch_link anterior "$CURRENT"
if ! { restart_service && health_ok; }; then
  log "CRÍTICO: $PREVIOUS não respondeu UP; rode o rollback_manual de novo para voltar a $CURRENT"
  dump_logs
  exit 1
fi
log "rollback manual concluído: $CURRENT -> $PREVIOUS"
