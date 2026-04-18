#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TOOLS_DIR="${ROOT_DIR}/tools"
JDK_DIR="${TOOLS_DIR}/jdk-21"
ARCHIVE_PATH="${TOOLS_DIR}/jdk-21.tar.gz"
DOWNLOAD_URL="${JDK21_DOWNLOAD_URL:-https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jdk/hotspot/normal/eclipse}"

if [ -x "${JDK_DIR}/bin/javac" ]; then
  echo "Java 21 JDK already available at ${JDK_DIR}"
  exit 0
fi

mkdir -p "${TOOLS_DIR}"

echo "Downloading Java 21 JDK from ${DOWNLOAD_URL}"
curl -fsSL "${DOWNLOAD_URL}" -o "${ARCHIVE_PATH}"

rm -rf "${JDK_DIR}"
mkdir -p "${JDK_DIR}"
tar -xzf "${ARCHIVE_PATH}" -C "${TOOLS_DIR}"

EXTRACTED_DIR="$(find "${TOOLS_DIR}" -maxdepth 1 -mindepth 1 -type d -name 'jdk-*' ! -name 'jdk-21' | head -n 1)"
if [ -z "${EXTRACTED_DIR}" ]; then
  echo "Could not locate extracted JDK directory." >&2
  exit 1
fi

rm -rf "${JDK_DIR}"
mv "${EXTRACTED_DIR}" "${JDK_DIR}"

echo "Java 21 JDK installed at ${JDK_DIR}"
