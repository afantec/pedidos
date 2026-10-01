#!/usr/bin/env bash
# Prepara o servidor para o microsserviço pedidos (Fase 1 da análise). Executar como root, uma vez.
set -euo pipefail
SVC=pedidos
# Usuário do runner: gitlab-runner (GitLab) ou o usuário do runner self-hosted do GitHub Actions
RUNNER_USER="${RUNNER_USER:-gitlab-runner}"
DIR="$(cd "$(dirname "$0")" && pwd)"

id "svc-$SVC" &>/dev/null || useradd --system --no-create-home --shell /usr/sbin/nologin "svc-$SVC"
mkdir -p "/opt/microsservicos/$SVC"/{releases,shared,logs}
chown -R "$RUNNER_USER:svc-$SVC" "/opt/microsservicos/$SVC"
chmod 0750 "/opt/microsservicos/$SVC"

if [[ ! -f "/opt/microsservicos/$SVC/shared/app.env" ]]; then
  install -o root -g "svc-$SVC" -m 0640 "$DIR/app.env.example" "/opt/microsservicos/$SVC/shared/app.env"
  echo "Edite /opt/microsservicos/$SVC/shared/app.env com a senha real do banco."
fi

install -m 0644 "$DIR/$SVC.service" "/etc/systemd/system/$SVC.service"
sed "s/^gitlab-runner /$RUNNER_USER /" "$DIR/sudoers-gitlab-runner-$SVC" > "/etc/sudoers.d/$RUNNER_USER-$SVC"
chmod 0440 "/etc/sudoers.d/$RUNNER_USER-$SVC"
visudo -cf "/etc/sudoers.d/$RUNNER_USER-$SVC"
usermod -aG systemd-journal "$RUNNER_USER"
systemctl daemon-reload
systemctl enable "$SVC.service"
