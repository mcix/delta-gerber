package com.deltaproto.deltagerber.renderer.svg;

import com.deltaproto.deltagerber.model.drill.DrillDocument;
import com.deltaproto.deltagerber.model.drill.DrillOperation;
import com.deltaproto.deltagerber.model.gerber.BoundingBox;
import com.deltaproto.deltagerber.model.gerber.GerberDocument;
import com.deltaproto.deltagerber.model.gerber.aperture.Aperture;
import com.deltaproto.deltagerber.model.gerber.operation.GraphicsObject;

import java.util.*;

/**
 * Renders multiple Gerber and drill documents into a single multi-layer SVG.
 *
 * This approach creates a single SVG with all layers sharing:
 * - A common viewBox (calculated from global bounding box)
 * - A shared defs section for apertures
 * - Individual layer groups that can be toggled via display attribute
 *
 * Structure:
 * <pre>
 * &lt;svg viewBox="..."&gt;
 *   &lt;defs&gt;...shared apertures...&lt;/defs&gt;
 *   &lt;g id="viewport" transform="scale(1,-1)"&gt;
 *     &lt;g class="layer" id="file1.GTL" display="inline"&gt;...&lt;/g&gt;
 *     &lt;g class="layer" id="file2.GBL" display="inline"&gt;...&lt;/g&gt;
 *   &lt;/g&gt;
 * &lt;/svg&gt;
 * </pre>
 */
public class MultiLayerSVGRenderer {

    private double margin = 0.5;
    private boolean flipY = true;
    private SvgOptions svgOptions = SvgOptions.exact();

    /**
     * A layer to be rendered, containing either a Gerber or Drill document.
     */
    public static class Layer {
        private final String name;
        private final GerberDocument gerberDoc;
        private final DrillDocument drillDoc;
        private String color = null;
        private double opacity = 0.75;
        private boolean visible = true;

        public Layer(String name, GerberDocument doc) {
            this.name = name;
            this.gerberDoc = doc;
            this.drillDoc = null;
        }

        public Layer(String name, DrillDocument doc) {
            this.name = name;
            this.gerberDoc = null;
            this.drillDoc = doc;
        }

        public String getName() { return name; }
        public GerberDocument getGerberDoc() { return gerberDoc; }
        public DrillDocument getDrillDoc() { return drillDoc; }
        public boolean isGerber() { return gerberDoc != null; }
        public boolean isDrill() { return drillDoc != null; }

        public Layer setColor(String color) {
            this.color = color;
            return this;
        }

        public Layer setOpacity(double opacity) {
            this.opacity = opacity;
            return this;
        }

        public Layer setVisible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public String getColor() { return color; }
        public double getOpacity() { return opacity; }
        public boolean isVisible() { return visible; }

        public BoundingBox getBoundingBox() {
            if (gerberDoc != null) {
                return gerberDoc.getBoundingBox();
            } else if (drillDoc != null) {
                return drillDoc.getBoundingBox();
            }
            return new BoundingBox();
        }
    }

    public MultiLayerSVGRenderer() {
    }

    public MultiLayerSVGRenderer setMargin(double margin) {
        this.margin = margin;
        return this;
    }

    public MultiLayerSVGRenderer setFlipY(boolean flipY) {
        this.flipY = flipY;
        return this;
    }

    public MultiLayerSVGRenderer setSvgOptions(SvgOptions options) {
        this.svgOptions = options;
        return this;
    }

    /**
     * Render multiple layers into a single SVG document.
     */
    public String render(List<Layer> layers) {
        if (layers == null || layers.isEmpty()) {
            return createEmptySvg();
        }

        // Calculate global bounding box across all layers
        BoundingBox globalBounds = new BoundingBox();
        for (Layer layer : layers) {
            BoundingBox layerBounds = layer.getBoundingBox();
            if (layerBounds.isValid()) {
                globalBounds.extend(layerBounds);
            }
        }

        if (!globalBounds.isValid()) {
            return createEmptySvg();
        }

        // Add margin
        double minX = globalBounds.getMinX() - margin;
        double minY = globalBounds.getMinY() - margin;
        double width = globalBounds.getWidth() + 2 * margin;
        double height = globalBounds.getHeight() + 2 * margin;

        StringBuilder svg = new StringBuilder();

        // SVG header with shared viewBox
        svg.append(String.format(Locale.US,
            "<svg xmlns=\"http://www.w3.org/2000/svg\" " +
            "viewBox=\"%.6f %.6f %.6f %.6f\" " +
            "preserveAspectRatio=\"xMidYMid meet\" " +
            "stroke-linecap=\"round\" stroke-linejoin=\"round\" " +
            "fill-rule=\"nonzero\">\n",
            minX, minY, width, height));

        // Collect all apertures from all Gerber layers with unique prefixes
        // Use "currentColor" so apertures pick up the layer group's color property
        svg.append("<defs>\n");

        // Mask base rect for clear polarity masks
        String maskRect = PolarityMaskHelper.createMaskRect(minX, minY, width, height, 1);

        int layerIndex = 0;
        // Pre-compute polarity groups per layer (needed for both mask defs and rendering)
        List<List<PolarityMaskHelper.PolarityGroup>> allLayerGroups = new ArrayList<>();

        for (Layer layer : layers) {
            if (layer.isGerber() && layer.getGerberDoc() != null) {
                String aperturePrefix = "L" + layerIndex + "_ap";
                // Aperture defs don't include fill — fill is set on <use> elements
                svgOptions.setDarkColor("currentColor").setClearColor("currentColor").setFlipY(flipY);
                for (Aperture aperture : layer.getGerberDoc().getApertures().values()) {
                    String def = aperture.toSvgDef(aperturePrefix + aperture.getDCode(), svgOptions);
                    svg.append("  ").append(def).append("\n");
                }

                // Group objects by polarity and generate mask defs
                List<PolarityMaskHelper.PolarityGroup> groups =
                    PolarityMaskHelper.groupByPolarity(layer.getGerberDoc().getObjects());
                allLayerGroups.add(groups);

                // Generate masks for clear polarity groups (black = hidden in mask)
                String maskPrefix = "L" + layerIndex + "_cm";
                SvgOptions maskOptions = svgOptions.copy();
                maskOptions.setApertureIdPrefix(aperturePrefix);
                maskOptions.setDarkColor("black").setClearColor("black");
                PolarityMaskHelper.generateMaskDefs(svg, groups, maskPrefix, maskRect, maskOptions);
            } else {
                allLayerGroups.add(Collections.emptyList());
            }
            layerIndex++;
        }
        svg.append("</defs>\n");

        // Viewport group with Y-flip transform and stroke-width="0" to prevent inherited strokes
        if (flipY) {
            svg.append(String.format(Locale.US,
                "<g id=\"viewport\" transform=\"translate(0, %.6f) scale(1,-1)\" stroke-width=\"0\">\n",
                minY + height + minY));
        } else {
            svg.append("<g id=\"viewport\" stroke-width=\"0\">\n");
        }

        // Render each layer as a group
        layerIndex = 0;
        for (Layer layer : layers) {
            String layerId = sanitizeId(layer.getName());
            String display = layer.isVisible() ? "inline" : "none";
            String fillColor = layer.getColor() != null ? layer.getColor() : "#000000";

            svg.append(String.format(Locale.US,
                "  <g class=\"layer\" id=\"%s\" display=\"%s\" " +
                "color=\"%s\" fill=\"currentColor\" stroke=\"none\" stroke-width=\"0\" opacity=\"%.2f\">\n",
                layerId, display, fillColor, layer.getOpacity()));

            // Render layer content
            if (layer.isGerber()) {
                String aperturePrefix = "L" + layerIndex + "_ap";
                String maskPrefix = "L" + layerIndex + "_cm";
                List<PolarityMaskHelper.PolarityGroup> groups = allLayerGroups.get(layerIndex);

                SvgOptions layerOptions = svgOptions.copy();
                layerOptions.setApertureIdPrefix(aperturePrefix);
                layerOptions.setDarkColor("currentColor").setClearColor("currentColor");

                PolarityMaskHelper.renderWithMasks(svg, groups, maskPrefix, layerOptions);
            } else if (layer.isDrill()) {
                renderDrillContent(svg, layer.getDrillDoc());
            }

            svg.append("  </g>\n");
            layerIndex++;
        }

        svg.append("</g>\n");
        svg.append("</svg>");

        return svg.toString();
    }

    private void renderDrillContent(StringBuilder svg, DrillDocument doc) {
        if (doc == null) return;

        for (DrillOperation op : doc.getOperations()) {
            String opSvg = op.toSvg();
            if (opSvg != null && !opSvg.isEmpty()) {
                svg.append("    ").append(opSvg).append("\n");
            }
        }
    }

    /**
     * Sanitize a filename to be used as an SVG element ID.
     */
    private String sanitizeId(String name) {
        // Replace spaces and special characters that are invalid in SVG IDs
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private String createEmptySvg() {
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 1 1\"></svg>";
    }
}
