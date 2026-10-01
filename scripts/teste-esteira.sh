#!/usr/bin/env bash
# Testa deploy.sh e rollback.sh com systemd, sudo e curl simulados (cenários T1-T11 da análise).
# Roda num container descartável, nunca no servidor:
#   docker run --rm -v "$PWD/scripts:/scripts:ro" --entrypoint bash debian:bookworm-slim /scripts/teste-esteira.sh
set -uo pipefail
S=/scripts
export STATE=/tmp/state SERVICE_NAME=svc BASE_DIR=/opt/svc HEALTH_URL=http://x/health HEALTH_TIMEOUT=3 HEALTH_INTERVAL=1 KEEP_RELEASES=5

# ---- stubs ----
mkdir -p /stub "$STATE"
cat > /stub/sudo <<'EOF'
#!/bin/bash
[[ "$1" == -n ]] && shift
exec "$@"
EOF
cat > /usr/bin/systemctl <<'EOF'
#!/bin/bash
case "$1" in
  reset-failed) rm -f "$STATE/failed"; echo "reset-failed" >> "$STATE/calls" ;;
  is-failed)    [[ -f "$STATE/failed" ]] ;;
  restart)
    echo "restart" >> "$STATE/calls"
    if [[ -f "$STATE/deny_once" ]]; then rm -f "$STATE/deny_once"; echo "sudo: a password is required" >&2; exit 1; fi
    if [[ -f "$STATE/failed" ]]; then echo "start request repeated too quickly" >&2; exit 1; fi
    case "$(cat "$BASE_DIR/atual/app.jar")" in
      ok)  echo '{"status":"UP"}' > "$STATE/health" ;;
      bad) rm -f "$STATE/health"; touch "$STATE/failed" ;;   # entra em loop e estoura o StartLimitBurst
    esac ;;
esac
EOF
cat > /stub/curl <<'EOF'
#!/bin/bash
[[ -f "$STATE/health" ]] && cat "$STATE/health" || exit 7
EOF
printf '#!/bin/bash\necho "(journal)"\n' > /stub/journalctl
chmod +x /stub/* /usr/bin/systemctl
export PATH=/stub:$PATH
echo ok > /tmp/ok.jar; echo bad > /tmp/bad.jar

# ---- helpers ----
FALHAS=0
deploy() { CI_PIPELINE_ID=$1 bash $S/deploy.sh /tmp/$2.jar > /tmp/out 2>&1; echo $?; }
link()   { readlink "$BASE_DIR/$1" 2>/dev/null | sed 's/.*_p//'; }
check()  { if [[ "$2" == "$3" ]]; then echo "  ok    $1"; else echo "  FALHA $1: esperado '$3', obtido '$2'"; FALHAS=$((FALHAS+1)); sed 's/^/        | /' /tmp/out; fi; }
reset()  { rm -rf "$BASE_DIR" "$STATE"; mkdir -p "$BASE_DIR/releases" "$STATE"; }

echo "T1 primeiro deploy";            reset
check "exit 0"            "$(deploy 100 ok)" 0
check "atual=100"         "$(link atual)" 100
check "sem anterior"      "$(link anterior)" ""

echo "T2 deploy normal"
check "exit 0"            "$(deploy 101 ok)" 0
check "atual=101"         "$(link atual)" 101
check "anterior=100"      "$(link anterior)" 100

echo "T4 app não sobe + limite de reinícios (correção 1)"
check "exit 1"            "$(deploy 102 bad)" 1
check "atual volta a 101" "$(link atual)" 101
check "anterior=100"      "$(link anterior)" 100
check "release FALHOU"    "$(ls $BASE_DIR/releases/*_p102/FALHOU >/dev/null 2>&1 && echo s)" s
check "rollback no ar"    "$(grep -c 'rollback concluído' /tmp/out)" 1
check "serviço UP"        "$(cat $STATE/health)" '{"status":"UP"}'

echo "T7 deploy após rollback"
check "exit 0"            "$(deploy 103 ok)" 0
check "atual=103"         "$(link atual)" 103
check "anterior=101"      "$(link anterior)" 101

echo "Restart recusado pelo sudo/systemd (correção 2)"
touch $STATE/deny_once
check "exit 1"            "$(deploy 104 ok)" 1
check "atual volta a 103" "$(link atual)" 103
check "rollback no ar"    "$(grep -c 'rollback concluído' /tmp/out)" 1

echo "Pipeline desatualizado com deploy automático (correção 4)"
antes=$(ls $BASE_DIR/releases | wc -l)
check "exit 1"            "$(DEPLOY_AUTOMATICO=true deploy 99 ok)" 1
check "mensagem"          "$(grep -c 'mais antigo' /tmp/out)" 1
check "atual=103"         "$(link atual)" 103
check "nenhuma release"   "$(ls $BASE_DIR/releases | wc -l)" "$antes"
check "FORCE_DEPLOY"      "$(DEPLOY_AUTOMATICO=true FORCE_DEPLOY=1 deploy 98 ok)" 0
check "atual=98"          "$(link atual)" 98

echo "Versão antiga escolhida no painel (deploy manual)"
check "exit 0"            "$(deploy 99 ok)" 0
check "atual=99"          "$(link atual)" 99
check "anterior=98"       "$(link anterior)" 98

echo "T8 rollback manual (ida e volta)"
bash $S/rollback.sh > /tmp/out 2>&1; check "exit 0" "$?" 0
check "atual=98"          "$(link atual)" 98
check "anterior=99"       "$(link anterior)" 99
bash $S/rollback.sh > /tmp/out 2>&1
check "desfaz: atual=99"  "$(link atual)" 99

echo "T10 limpeza de releases"
for p in 201 202 203 204 205 206 207; do sleep 1; deploy $p ok > /dev/null; done
check "restam 5"          "$(ls $BASE_DIR/releases | wc -l)" 5
check "atual=207"         "$(link atual)" 207
check "anterior existe"   "$(test -d $BASE_DIR/$(readlink $BASE_DIR/anterior) && echo s)" s

echo "T11 primeiro deploy com falha";  reset
check "exit 1"            "$(deploy 300 bad)" 1
check "mensagem"          "$(grep -c 'não há versão anterior' /tmp/out)" 1

echo "Rollback manual sem versão no ar"; reset
bash $S/rollback.sh > /tmp/out 2>&1; check "exit 1" "$?" 1
check "mensagem"          "$(grep -c 'não há versão implantada' /tmp/out)" 1

echo; [[ $FALHAS == 0 ]] && echo "TODOS OS CENÁRIOS PASSARAM" || echo "$FALHAS FALHA(S)"
exit $FALHAS
