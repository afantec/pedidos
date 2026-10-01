#!/usr/bin/env bash
# Deploy atômico com health check e rollback automático.
# Uso: deploy.sh <caminho-do-jar>
source "$(dirname "$0")/lib.sh"

JAR_SRC="${1:?informe o caminho do jar}"
KEEP_RELEASES="${KEEP_RELEASES:-5}"
RELEASE_NAME="$(date +%Y%m%d-%H%M%S)_p${CI_PIPELINE_ID:-manual}"
RELEASE_REL="releases/$RELEASE_NAME"
RELEASE_DIR="$BASE_DIR/$RELEASE_REL"

[[ -f "$JAR_SRC" ]] || { log "jar não encontrado: $JAR_SRC"; exit 1; }

# 1. Guarda o estado atual para poder restaurar
CURRENT="$(readlink "$BASE_DIR/atual" 2>/dev/null || true)"
OLD_PREVIOUS="$(readlink "$BASE_DIR/anterior" 2>/dev/null || true)"

# 2. Com deploy automático (DEPLOY_AUTOMATICO=true), recusa pipeline mais antigo que o
#    que está no ar: o resource_group só serializa, não ordena. No deploy manual, feito
#    pelo painel da esteira, quem escolhe a versão é a pessoa, e voltar versão é permitido.
CURRENT_PIPELINE="$(release_pipeline "$CURRENT")"
if [[ "${DEPLOY_AUTOMATICO:-false}" == true && "${FORCE_DEPLOY:-0}" != 1 \
      && "${CI_PIPELINE_ID:-}" =~ ^[0-9]+$ && "$CURRENT_PIPELINE" =~ ^[0-9]+$ ]] \
   && (( CI_PIPELINE_ID < CURRENT_PIPELINE )); then
  log "pipeline $CI_PIPELINE_ID é mais antigo que o que está no ar ($CURRENT_PIPELINE): deploy recusado"
  exit 1
fi

# 3. Nova release com metadados
mkdir -p "$RELEASE_DIR"
install -m 0644 "$JAR_SRC" "$RELEASE_DIR/app.jar"
cat > "$RELEASE_DIR/RELEASE" <<EOF
commit=${CI_COMMIT_SHA:-desconhecido}
pipeline=${CI_PIPELINE_ID:-manual}
autor=${GITLAB_USER_LOGIN:-desconhecido}
data=$(date -Iseconds)
EOF

# 4. anterior <- atual ; atual <- nova release
[[ -n "$CURRENT" ]] && switch_link anterior "$CURRENT"
switch_link atual "$RELEASE_REL"

# 5. Reinicia (Spring Boot executa as migrações Flyway na subida) e verifica a saúde.
#    Falha no restart segue o mesmo caminho do health check: rollback.
log "reiniciando $UNIT com $RELEASE_NAME"
if restart_service && health_ok; then
  log "deploy concluído: $RELEASE_NAME"
  # 6. Limpeza: mantém as N mais recentes, nunca atual/anterior
  keep_a="$(readlink "$BASE_DIR/atual")"
  keep_b="$(readlink "$BASE_DIR/anterior" 2>/dev/null || true)"
  find "$BASE_DIR/releases" -mindepth 1 -maxdepth 1 -type d -printf '%f\n' \
    | sort -r | tail -n +$((KEEP_RELEASES + 1)) \
    | while read -r name; do
        [[ "releases/$name" == "$keep_a" || "releases/$name" == "$keep_b" ]] && continue
        rm -rf -- "$BASE_DIR/releases/$name"
      done
  exit 0
fi

# 7. Rollback automático
log "ALERTA: $RELEASE_NAME não subiu"
dump_logs
touch "$RELEASE_DIR/FALHOU"

if [[ -z "$CURRENT" ]]; then
  log "primeiro deploy do serviço: não há versão anterior para restaurar"
  exit 1
fi

switch_link atual "$CURRENT"
if [[ -n "$OLD_PREVIOUS" ]]; then switch_link anterior "$OLD_PREVIOUS"; fi

if restart_service && health_ok; then
  log "rollback concluído: de volta a $CURRENT"
else
  log "CRÍTICO: a versão restaurada ($CURRENT) também não respondeu UP; intervenção manual necessária"
  dump_logs
fi
exit 1   # o pipeline sempre falha quando houve rollback
