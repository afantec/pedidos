#!/usr/bin/env bash
# Funções compartilhadas por deploy.sh e rollback.sh
set -euo pipefail

: "${SERVICE_NAME:?}" "${BASE_DIR:?}" "${HEALTH_URL:?}"
HEALTH_TIMEOUT="${HEALTH_TIMEOUT:-120}"
HEALTH_INTERVAL="${HEALTH_INTERVAL:-5}"
UNIT="${SERVICE_NAME}.service"

log() { echo "[$(date +%H:%M:%S)] $*"; }

# Troca atômica: cria o link temporário e renomeia por cima do atual
switch_link() {  # $1 = atual|anterior  $2 = destino relativo (releases/...)
  ln -sfn "$2" "$BASE_DIR/.$1.tmp"
  mv -Tf "$BASE_DIR/.$1.tmp" "$BASE_DIR/$1"
}

# Zera o contador de StartLimitBurst antes do restart: depois de uma versão que
# caiu em loop, sem isso o systemd recusaria subir até a versão do rollback.
# Retorna erro em vez de abortar, para quem chama decidir o que fazer.
restart_service() {
  sudo -n /usr/bin/systemctl reset-failed "$UNIT" || true
  if ! sudo -n /usr/bin/systemctl restart "$UNIT"; then
    log "falha ao reiniciar $UNIT (sudo ou systemd recusou)"
    return 1
  fi
}

# Espera o serviço responder "status":"UP"; falha rápido se o systemd desistir
health_ok() {
  local deadline=$((SECONDS + HEALTH_TIMEOUT)) body
  while (( SECONDS < deadline )); do
    if systemctl is-failed --quiet "$UNIT"; then
      log "serviço em estado failed no systemd"
      return 1
    fi
    body="$(curl -fsS --max-time 5 "$HEALTH_URL" 2>/dev/null || true)"
    if [[ "$body" == *'"status":"UP"'* ]]; then
      return 0
    fi
    sleep "$HEALTH_INTERVAL"
  done
  log "timeout de ${HEALTH_TIMEOUT}s sem UP"
  return 1
}

dump_logs() {
  journalctl -u "$UNIT" -n 100 --no-pager 2>/dev/null || true
}

# Número do pipeline gravado no arquivo RELEASE de uma release (vazio se não houver)
release_pipeline() {  # $1 = caminho relativo da release
  sed -n 's/^pipeline=//p' "$BASE_DIR/$1/RELEASE" 2>/dev/null || true
}
