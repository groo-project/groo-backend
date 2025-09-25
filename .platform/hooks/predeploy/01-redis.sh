#!/bin/bash
set -euxo pipefail

REDIS_VERSION="7.2.5"
SRC_DIR="/usr/local/src"
TARBALL="redis-${REDIS_VERSION}.tar.gz"

# 0) 빌드 도구/라이브러리 (AL2023은 dnf 사용)
dnf -y groupinstall "Development Tools" || true
dnf -y install gcc make jemalloc-devel tcl curl tar which || true

# 0-1) 커널 튜닝 (매 배포 시 보장)
sysctl -w vm.overcommit_memory=1
echo "vm.overcommit_memory=1" > /etc/sysctl.d/99-redis.conf || true
# THP off (없어도 실패하지 않게)
echo never > /sys/kernel/mm/transparent_hugepage/enabled || true

# 1) Redis 소스 다운로드 & 빌드 (최초 1회만)
if ! command -v /usr/local/bin/redis-server >/dev/null 2>&1; then
  mkdir -p "$SRC_DIR"
  cd "$SRC_DIR"
  curl -LO "http://download.redis.io/releases/${TARBALL}"
  rm -rf "redis-${REDIS_VERSION}"
  tar xzf "${TARBALL}"
  cd "redis-${REDIS_VERSION}"
  make
  make install     # /usr/local/bin/redis-server, redis-cli 설치
fi

# 2) 사용자/디렉토리/설정
id -u redis &>/dev/null || useradd -r -s /bin/false redis
mkdir -p /etc/redis /var/lib/redis /var/log/redis
chown -R redis:redis /var/lib/redis /var/log/redis

# 3) 기본 설정(최초 1회 생성). 이후엔 외부에서 관리해도 됨.
if [[ ! -f /etc/redis/redis.conf ]]; then
  cat >/etc/redis/redis.conf <<'CONF'
bind 127.0.0.1
protected-mode yes
port 6379

dir /var/lib/redis
logfile /var/log/redis/redis.log

appendonly yes
appendfsync everysec

# 인스턴스 RAM에 맞춰 조정(예: 2GB면 1~1.2GB 권장)
maxmemory 1gb
maxmemory-policy allkeys-lru

tcp-keepalive 60
daemonize no
supervised systemd
CONF
fi

# 4) systemd 서비스 파일(없으면 생성)
if [[ ! -f /etc/systemd/system/redis.service ]]; then
  cat >/etc/systemd/system/redis.service <<'SERVICE'
[Unit]
Description=Redis In-Memory Data Store
After=network.target

[Service]
ExecStart=/usr/local/bin/redis-server /etc/redis/redis.conf
ExecStop=/usr/local/bin/redis-cli shutdown
User=redis
Group=redis
Restart=always
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
SERVICE
fi

# 유닛 내용이 바뀌었을 수도 있으니 매번 반영
systemctl daemon-reload

# 5) 기동/재기동
systemctl enable redis
systemctl restart redis
