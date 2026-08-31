#!/bin/bash

# --- Configuration ---
REALM_FILE="./a.interchangedomain.com-realm.json"
EXPORT_DIR="$PWD/export"
VOLUME_NAME="keycloak-db-data"
CONTAINER_NAME="keycloak-dev"

# Ensure the export directory exists on your host
mkdir -p "$EXPORT_DIR"

# 0. Clear old volume to ensure we start fresh from the realm.json file
echo "Cleaning up old data volume..."
docker volume rm $VOLUME_NAME 2>/dev/null || true

# 1. Start Keycloak
# We mount the local realm.json for the initial import
# and a named volume to store the changes you make in the UI
echo "Starting Keycloak..."
docker run -d \
  --name $CONTAINER_NAME \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  -v "$PWD/$REALM_FILE:/opt/keycloak/data/import/realm.json" \
  -v $VOLUME_NAME:/opt/keycloak/data \
  -p 8080:8080 \
  quay.io/keycloak/keycloak:26.5.2 \
  start-dev --import-realm

echo "✓ Running at http://localhost:8080/admin"
echo "Username: admin | Password: admin"
echo ""
read -p "Press Enter when you have finished making changes in the UI..."

# 2. Stop the server 
# This is REQUIRED to release the lock on the H2 database
echo "Stopping server to release DB lock..."
docker stop $CONTAINER_NAME

# 3. Run the export
# We start a temporary container that mounts the same volume and the export folder
echo "Exporting realm to file..."
docker run --rm \
  -v $VOLUME_NAME:/opt/keycloak/data \
  -v "$EXPORT_DIR:/tmp/export" \
  quay.io/keycloak/keycloak:26.5.2 \
  export --users realm_file \
        --dir /tmp/export \
        --realm a.interchangedomain.com

# 4. Overwrite your local config file with the exported one
cp "$EXPORT_DIR/a.interchangedomain.com-realm.json" "$REALM_FILE"
echo "✓ Local $REALM_FILE has been updated."

# Clean up the server container
docker rm $CONTAINER_NAME

# Clean up export 
rm -r "$EXPORT_DIR"
echo "Done!"

