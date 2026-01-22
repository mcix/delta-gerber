#!/bin/bash
# Generate reference SVGs for all Gerber test files using gerbv
# This creates a baseline for integration testing

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_DIR="$SCRIPT_DIR/reference-svg"

# Create output directory if it doesn't exist
mkdir -p "$OUTPUT_DIR"

# Define colors (copper gold color)
COPPER_COLOR="#C87533"
BG_COLOR="#FFFFFF"

# Function to generate SVG for a single gerber file
generate_svg() {
    local input_file="$1"
    local category="$2"
    local basename=$(basename "$input_file" .gbr)
    basename=$(basename "$basename" .drl)
    local output_file="$OUTPUT_DIR/${category}_${basename}.svg"

    echo "Generating: $output_file"

    # Use gerbv to export to SVG
    gerbv -x svg -o "$output_file" -b "$BG_COLOR" -f "$COPPER_COLOR" "$input_file" 2>/dev/null

    if [ $? -eq 0 ] && [ -f "$output_file" ]; then
        echo "  ✓ Success ($(stat -f%z "$output_file") bytes)"
    else
        echo "  ✗ Failed"
    fi
}

echo "=== Generating Reference SVGs ==="
echo "Output directory: $OUTPUT_DIR"
echo ""

# Process all Gerber files by category
echo "--- Standard Apertures ---"
for f in "$SCRIPT_DIR"/apertures/*.gbr; do
    [ -f "$f" ] && generate_svg "$f" "apertures"
done

echo ""
echo "--- Macro Primitives ---"
for f in "$SCRIPT_DIR"/macros/*.gbr; do
    [ -f "$f" ] && generate_svg "$f" "macros"
done

echo ""
echo "--- Plotting ---"
for f in "$SCRIPT_DIR"/plotting/*.gbr; do
    [ -f "$f" ] && generate_svg "$f" "plotting"
done

echo ""
echo "--- Regions ---"
for f in "$SCRIPT_DIR"/regions/*.gbr; do
    [ -f "$f" ] && generate_svg "$f" "regions"
done

echo ""
echo "--- Polarity ---"
for f in "$SCRIPT_DIR"/polarity/*.gbr; do
    [ -f "$f" ] && generate_svg "$f" "polarity"
done

echo ""
echo "--- Transforms ---"
for f in "$SCRIPT_DIR"/transforms/*.gbr; do
    [ -f "$f" ] && generate_svg "$f" "transforms"
done

echo ""
echo "--- Block Apertures ---"
for f in "$SCRIPT_DIR"/blocks/*.gbr; do
    [ -f "$f" ] && generate_svg "$f" "blocks"
done

echo ""
echo "--- Step and Repeat ---"
for f in "$SCRIPT_DIR"/step-repeat/*.gbr; do
    [ -f "$f" ] && generate_svg "$f" "step-repeat"
done

echo ""
echo "--- Attributes ---"
for f in "$SCRIPT_DIR"/attributes/*.gbr; do
    [ -f "$f" ] && generate_svg "$f" "attributes"
done

echo ""
echo "--- Inch Units ---"
for f in "$SCRIPT_DIR"/inch/*.gbr; do
    [ -f "$f" ] && generate_svg "$f" "inch"
done

echo ""
echo "--- Combined Tests ---"
for f in "$SCRIPT_DIR"/combined/*.gbr; do
    [ -f "$f" ] && generate_svg "$f" "combined"
done

echo ""
echo "--- Drill Files ---"
for f in "$SCRIPT_DIR"/drill/*.drl; do
    [ -f "$f" ] && generate_svg "$f" "drill"
done

echo ""
echo "=== Generation Complete ==="
echo ""
echo "SVG files generated:"
ls -la "$OUTPUT_DIR"/*.svg 2>/dev/null | wc -l
echo ""
echo "Total size:"
du -sh "$OUTPUT_DIR"
