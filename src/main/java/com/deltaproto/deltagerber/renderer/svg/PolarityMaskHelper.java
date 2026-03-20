package com.deltaproto.deltagerber.renderer.svg;

import com.deltaproto.deltagerber.model.gerber.Polarity;
import com.deltaproto.deltagerber.model.gerber.operation.GraphicsObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Shared helper for rendering Gerber polarity using SVG masks.
 *
 * The Gerber rendering model is a painter's model: dark objects paint,
 * clear objects erase from the entire accumulated image. We implement
 * this by wrapping all accumulated content in a mask for each clear group,
 * producing true SVG transparency rather than painting over with a background color.
 */
class PolarityMaskHelper {

    static class PolarityGroup {
        final Polarity polarity;
        final List<GraphicsObject> objects;

        PolarityGroup(Polarity polarity, List<GraphicsObject> objects) {
            this.polarity = polarity;
            this.objects = objects;
        }
    }

    /**
     * Groups consecutive objects with the same polarity into PolarityGroups.
     */
    static List<PolarityGroup> groupByPolarity(List<GraphicsObject> objects) {
        List<PolarityGroup> groups = new ArrayList<>();
        if (objects.isEmpty()) {
            return groups;
        }

        Polarity current = objects.get(0).getPolarity();
        List<GraphicsObject> currentObjects = new ArrayList<>();

        for (GraphicsObject obj : objects) {
            if (obj.getPolarity() != current) {
                groups.add(new PolarityGroup(current, currentObjects));
                currentObjects = new ArrayList<>();
                current = obj.getPolarity();
            }
            currentObjects.add(obj);
        }
        if (!currentObjects.isEmpty()) {
            groups.add(new PolarityGroup(current, currentObjects));
        }
        return groups;
    }

    /**
     * Generates SVG mask definitions for clear polarity groups.
     *
     * @param svg        target builder
     * @param groups     polarity groups from {@link #groupByPolarity}
     * @param maskPrefix ID prefix for mask elements (e.g., "cm" or "L0_cm")
     * @param maskRect   the white background rect for the mask (covers the viewbox)
     * @param maskOptions SvgOptions with dark/clear colors set to "black" for mask rendering
     */
    static void generateMaskDefs(StringBuilder svg, List<PolarityGroup> groups,
                                  String maskPrefix, String maskRect, SvgOptions maskOptions) {
        int maskId = 0;
        for (PolarityGroup group : groups) {
            if (group.polarity == Polarity.CLEAR) {
                svg.append(String.format("  <mask id=\"%s%d\">\n", maskPrefix, maskId));
                svg.append("    ").append(maskRect).append("\n");
                for (GraphicsObject obj : group.objects) {
                    String objSvg = obj.toSvg(maskOptions);
                    if (objSvg != null && !objSvg.isEmpty()) {
                        svg.append("    ").append(objSvg).append("\n");
                    }
                }
                svg.append("  </mask>\n");
                maskId++;
            }
        }
    }

    /**
     * Renders polarity groups using SVG masks for true transparency.
     *
     * For example, given groups [D0, C0, D1, C1], the SVG structure is:
     * <pre>
     *   &lt;g mask="url(#prefix1)"&gt;        &lt;!-- C1 cuts from everything --&gt;
     *     &lt;g mask="url(#prefix0)"&gt;      &lt;!-- C0 cuts from D0 --&gt;
     *       [D0 objects]
     *     &lt;/g&gt;
     *     [D1 objects]
     *   &lt;/g&gt;
     * </pre>
     *
     * @param svg        target builder
     * @param groups     polarity groups from {@link #groupByPolarity}
     * @param maskPrefix ID prefix matching the one used in {@link #generateMaskDefs}
     * @param options    SvgOptions for rendering dark objects
     */
    static void renderWithMasks(StringBuilder svg, List<PolarityGroup> groups,
                                 String maskPrefix, SvgOptions options) {
        if (groups.isEmpty()) {
            return;
        }

        // Check if there are any clear groups
        boolean hasClear = false;
        for (PolarityGroup group : groups) {
            if (group.polarity == Polarity.CLEAR) {
                hasClear = true;
                break;
            }
        }

        if (!hasClear) {
            // No clear groups — render all objects directly
            for (PolarityGroup group : groups) {
                renderGroup(svg, group, options);
            }
            return;
        }

        // Count clear groups for mask nesting
        int clearCount = 0;
        for (PolarityGroup group : groups) {
            if (group.polarity == Polarity.CLEAR) {
                clearCount++;
            }
        }

        // Open mask groups from the last clear group backward so the outermost
        // mask corresponds to the last clear group (cuts from everything)
        int openMasks = 0;
        for (int ci = clearCount - 1; ci >= 0; ci--) {
            svg.append(String.format("  <g mask=\"url(#%s%d)\">\n", maskPrefix, ci));
            openMasks++;
        }

        // Render dark groups and close masks at clear group positions
        for (PolarityGroup group : groups) {
            if (group.polarity == Polarity.DARK) {
                renderGroup(svg, group, options);
            } else {
                // Clear group — close the innermost open mask group
                svg.append("  </g>\n");
                openMasks--;
            }
        }

        // Close any remaining open mask groups
        while (openMasks > 0) {
            svg.append("  </g>\n");
            openMasks--;
        }
    }

    /**
     * Creates the mask base rect string (white rect covering the viewbox + margin).
     */
    static String createMaskRect(double minX, double minY, double width, double height, double extraMargin) {
        return String.format(Locale.US,
            "<rect x=\"%.6f\" y=\"%.6f\" width=\"%.6f\" height=\"%.6f\" fill=\"white\"/>",
            minX - extraMargin, minY - extraMargin,
            width + 2 * extraMargin, height + 2 * extraMargin);
    }

    private static void renderGroup(StringBuilder svg, PolarityGroup group, SvgOptions options) {
        for (GraphicsObject obj : group.objects) {
            String objSvg = obj.toSvg(options);
            if (objSvg != null && !objSvg.isEmpty()) {
                svg.append("  ").append(objSvg).append("\n");
            }
        }
    }
}
