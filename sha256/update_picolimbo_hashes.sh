#!/bin/bash
set -e

REPO="Quozul/PicoLimbo"
API_URL="https://api.github.com/repos/$REPO/releases"

# Get latest 2 releases
RELEASES=$(curl -s $API_URL | jq -c '.[0:2]')

get_hashes() {
    local release_json=$1
    local hashes=()
    
    # Order: linux-aarch64, linux-x86_64, macos-aarch64, windows-x86_64
    local patterns=("linux-aarch64" "linux-x86_64" "macos-aarch64" "windows-x86_64")
    
    for pattern in "${patterns[@]}"; do
        local asset=$(echo "$release_json" | jq -r ".assets[] | select(.name | contains(\"$pattern\"))")
        local asset_url=$(echo "$asset" | jq -r ".browser_download_url")
        local asset_name=$(echo "$asset" | jq -r ".name")
        
        if [ -n "$asset_url" ] && [ "$asset_url" != "null" ]; then
            echo "Downloading $asset_name..." >&2
            curl -L -s -o "$asset_name" "$asset_url"
            
            if [[ "$asset_name" == *.tar.gz ]]; then
                tar -xzf "$asset_name"
                if [ -f "pico_limbo" ]; then
                    hashes+=("${pattern}: $(sha256sum pico_limbo | cut -d ' ' -f 1)")
                    rm pico_limbo
                fi
            elif [[ "$asset_name" == *.zip ]]; then
                unzip -o -q "$asset_name"
                if [ -f "pico_limbo.exe" ]; then
                    hashes+=("${pattern}: $(sha256sum pico_limbo.exe | cut -d ' ' -f 1)")
                    rm pico_limbo.exe
                fi
            fi
            rm "$asset_name"
        else
            echo "Asset for $pattern not found." >&2
            hashes+=("${pattern}: 0000000000000000000000000000000000000000000000000000000000000000")
        fi
    done
    printf "%s\n" "${hashes[@]}"
}

echo "Fetching hashes for version 1..." >&2
# Get version tags
TAG1=$(echo "$RELEASES" | jq -r '.[0].tag_name')
TAG2=$(echo "$RELEASES" | jq -r '.[1].tag_name')

{
    echo "LATEST VERSION ($TAG1)"
    get_hashes "$(echo "$RELEASES" | jq -c '.[0]') "
    echo ""
    echo "-------------------- PREVIOUS VERSION ($TAG2) --------------------"
    echo ""
    echo "PREVIOUS VERSION ($TAG2)"
    get_hashes "$(echo "$RELEASES" | jq -c '.[1]') "
} > latest.txt

echo "Updated latest.txt" >&2
