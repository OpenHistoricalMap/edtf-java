#!/usr/bin/env bash
# Fetch the upstream edtf.js reference library into a local, gitignored
# directory. Use this when implementing or debugging behavior against the
# upstream JS library. The pinned commit is tracked in REFERENCES.md.

set -euo pipefail

REPO_URL="https://github.com/inukshuk/edtf.js.git"
PINNED_SHA="042e298a2ce15d145a6516ee36187c536d8584de"
TARGET_DIR="reference/edtf.js"

if [[ -d "${TARGET_DIR}/.git" ]]; then
    echo "Reference clone already exists at ${TARGET_DIR}"
    echo "Fetching and checking out pinned SHA ${PINNED_SHA}..."
    git -C "${TARGET_DIR}" fetch --all --tags
    git -C "${TARGET_DIR}" checkout "${PINNED_SHA}"
else
    echo "Cloning ${REPO_URL} into ${TARGET_DIR}..."
    mkdir -p "$(dirname "${TARGET_DIR}")"
    git clone "${REPO_URL}" "${TARGET_DIR}"
    git -C "${TARGET_DIR}" checkout "${PINNED_SHA}"
fi

echo ""
echo "Upstream edtf.js v4.11.0 available at ${TARGET_DIR}"
