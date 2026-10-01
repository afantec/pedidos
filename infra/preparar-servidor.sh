#!/usr/bin/env bash
# Prepara o servidor para o microsserviço pedidos (Fase 1 da análise). Executar como root, uma vez.
set -euo pipefail
SVC=pedidos
DIR="$(cd "$(dirname "$0")" && pwd)"

id "svc-$SVC" &>/dev/null || useradd --system --no-create-home --shell /usr/sbin/nologin "svc-$SVC"
mkdir -p "/opt/microsservicos/$SVC"/{releases,shared,logs}
chown -R "gitlab-runner:svc-$SVC" "/opt/microsservicos/$SVC"
chmod 0750 "/opt/microsservicos/$SVC"

if [[ ! -f "/opt/microsservicos/$SVC/shared/app.env" ]]; then
  install -o root -g "svc-$SVC" -m 0640 "$DIR/app.env.example" "/opt/microsservicos/$SVC/shared/app.env"
  echo "Edite /opt/microsservicos/$SVC/shared/app.env com a senha real do banco."
fi

install -m 0644 "$DIR/$SVC.service" "/etc/systemd/system/$SVC.service"
install -m 0440 "$DIR/sudoers-gitlab-runner-$SVC" "/etc/sudoers.d/gitlab-runner-$SVC"
visudo -cf "/etc/sudoers.d/gitlab-runner-$SVC"
usermod -aG systemd-journal gitlab-runner
systemctl daemon-reload
systemctl enable "$SVC.service"
