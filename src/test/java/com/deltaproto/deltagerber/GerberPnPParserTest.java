package com.deltaproto.deltagerber;

import com.deltaproto.deltagerber.model.gerber.ComponentPlacement;
import com.deltaproto.deltagerber.model.gerber.GerberDocument;
import com.deltaproto.deltagerber.parser.GerberParser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Gerber X2 pick-and-place (PnP / component placement) parsing.
 *
 * PnP files use %TO.*% object attributes to attach component metadata to
 * each flash, and %TF.FileFunction,Component,...% to declare the file role.
 * All fixture data is synthetic — no customer board data is included.
 *
 * Format note: FSLAX46Y46 with leading-zero suppression means a raw integer
 * like 500000 encodes 0.500000 mm (padded to 10 digits: 0000500000 → 0000.500000).
 */
public class GerberPnPParserTest {

    private final GerberParser parser = new GerberParser();

    // ── Shared synthetic fixtures ────────────────────────────────────────────

    private static final String HEADER_BOTTOM = """
            %TF.GenerationSoftware,TestEDA,TestTool,1.0*%
            %TF.FileFunction,Component,L4,Bot*%
            %TF.FilePolarity,Positive*%
            %FSLAX46Y46*%
            %MOMM*%
            %LPD*%
            G01*
            """;

    private static final String HEADER_TOP = """
            %TF.GenerationSoftware,TestEDA,TestTool,1.0*%
            %TF.FileFunction,Component,L1,Top*%
            %TF.FilePolarity,Positive*%
            %FSLAX46Y46*%
            %MOMM*%
            %LPD*%
            G01*
            """;

    private static final String APERTURES = """
            G04 APERTURE LIST*
            %TA.AperFunction,ComponentMain*%
            %ADD10C,0.300000*%
            %TD*%
            %TA.AperFunction,ComponentOutline,Courtyard*%
            %ADD11C,0.100000*%
            %TD*%
            %TA.AperFunction,ComponentPin*%
            %ADD12P,0.360000X4X0.000000*%
            %TD*%
            %TA.AperFunction,ComponentPin*%
            %ADD13C,0.100000*%
            %TD*%
            G04 APERTURE END LIST*
            """;

    /** One SMD resistor: centroid, courtyard outline, two pin flashes. */
    private static final String R1_BLOCK = """
            D10*
            %TO.C,R1*%
            %TO.CFtp,R_0402_1005Metric*%
            %TO.CVal,10k*%
            %TO.CLbN,Resistor_SMD*%
            %TO.CMnt,SMD*%
            %TO.CRot,90*%
            X500000Y1000000D03*
            D11*
            X965000Y1925000D02*
            X965000Y75000D01*
            X35000Y75000D01*
            X35000Y1925000D01*
            X965000Y1925000D01*
            D12*
            %TO.P,R1,1*%
            X500000Y1490000D03*
            D13*
            %TO.P,R1,2*%
            X500000Y510000D03*
            %TD*%
            """;

    /** One SMD capacitor with 0° rotation. */
    private static final String C1_BLOCK = """
            D10*
            %TO.C,C1*%
            %TO.CFtp,C_0603_1608Metric*%
            %TO.CVal,100n*%
            %TO.CLbN,Capacitor_SMD*%
            %TO.CMnt,SMD*%
            %TO.CRot,0*%
            X2500000Y3000000D03*
            D11*
            X3225000Y3725000D02*
            X3225000Y2275000D01*
            X1775000Y2275000D01*
            X1775000Y3725000D01*
            X3225000Y3725000D01*
            D12*
            %TO.P,C1,1*%
            X2500000Y3725000D03*
            D13*
            %TO.P,C1,2*%
            X2500000Y2275000D03*
            %TD*%
            """;

    // ── Tests ────────────────────────────────────────────────────────────────

    @Test
    void isComponentFileTrueForPnPFileFunction() {
        GerberDocument doc = parser.parse(HEADER_BOTTOM + APERTURES + R1_BLOCK + "M02*\n");
        assertTrue(doc.isComponentFile());
    }

    @Test
    void isComponentFileFalseForOrdinaryGerber() {
        String gerber = """
                %FSLAX46Y46*%
                %MOMM*%
                %ADD10C,0.5*%
                D10*
                X500000Y500000D03*
                M02*
                """;
        GerberDocument doc = parser.parse(gerber);
        assertFalse(doc.isComponentFile());
    }

    @Test
    void detectsBottomSide() {
        GerberDocument doc = parser.parse(HEADER_BOTTOM + APERTURES + R1_BLOCK + "M02*\n");
        assertEquals("Bottom", doc.getComponentSide());
    }

    @Test
    void detectsTopSide() {
        GerberDocument doc = parser.parse(HEADER_TOP + APERTURES + R1_BLOCK + "M02*\n");
        assertEquals("Top", doc.getComponentSide());
    }

    @Test
    void extractsOneComponentCentroid() {
        GerberDocument doc = parser.parse(HEADER_BOTTOM + APERTURES + R1_BLOCK + "M02*\n");

        List<ComponentPlacement> comps = doc.getComponents();
        assertEquals(1, comps.size());

        ComponentPlacement r1 = comps.get(0);
        assertEquals("R1",                  r1.getRefdes());
        assertEquals("10k",                 r1.getValue());
        assertEquals("R_0402_1005Metric",   r1.getFootprint());
        assertEquals("SMD",                 r1.getMountType());
        assertEquals(90.0,                  r1.getRotation(), 0.001);
        assertEquals("Bottom",              r1.getSide());
        assertEquals(0.5,                   r1.getX(), 1e-4);  // X500000 → 0.500000 mm
        assertEquals(1.0,                   r1.getY(), 1e-4);  // Y1000000 → 1.000000 mm
    }

    @Test
    void extractsTwoComponentsInSequence() {
        GerberDocument doc = parser.parse(HEADER_BOTTOM + APERTURES + R1_BLOCK + C1_BLOCK + "M02*\n");

        List<ComponentPlacement> comps = doc.getComponents();
        assertEquals(2, comps.size());

        assertEquals("R1", comps.get(0).getRefdes());
        assertEquals("C1", comps.get(1).getRefdes());
    }

    @Test
    void pinFlashesDoNotCreateExtraCentroids() {
        // R1 has 1 centroid flash + 2 pin flashes = 3 D03 total, only 1 centroid expected.
        GerberDocument doc = parser.parse(HEADER_BOTTOM + APERTURES + R1_BLOCK + "M02*\n");
        assertEquals(1, doc.getComponents().size(), "only the centroid flash should be recorded, not pin flashes");
    }

    @Test
    void tdBetweenComponentsResetsState() {
        // Both components must be recorded with correct refdes, not merged or lost.
        GerberDocument doc = parser.parse(HEADER_BOTTOM + APERTURES + R1_BLOCK + C1_BLOCK + "M02*\n");

        List<ComponentPlacement> comps = doc.getComponents();
        assertEquals(2, comps.size());
        assertEquals("R1",   comps.get(0).getRefdes());
        assertEquals("C1",   comps.get(1).getRefdes());
        assertEquals(2.5,    comps.get(1).getX(), 1e-4);  // X2500000 → 2.500000 mm
        assertEquals(3.0,    comps.get(1).getY(), 1e-4);  // Y3000000 → 3.000000 mm
        assertEquals(0.0,    comps.get(1).getRotation(), 0.001);
    }

    @Test
    void negativeRotationIsParsedCorrectly() {
        String block = """
                D10*
                %TO.C,U1*%
                %TO.CFtp,SOT-23*%
                %TO.CVal,LDO*%
                %TO.CMnt,SMD*%
                %TO.CRot,-90*%
                X1000000Y2000000D03*
                %TD*%
                """;
        GerberDocument doc = parser.parse(HEADER_BOTTOM + APERTURES + block + "M02*\n");

        assertEquals(1, doc.getComponents().size());
        assertEquals(-90.0, doc.getComponents().get(0).getRotation(), 0.001);
    }

    @Test
    void throughHoleComponentIsCaptured() {
        String block = """
                D10*
                %TO.C,J1*%
                %TO.CFtp,PinHeader_2x03_P2.54mm*%
                %TO.CVal,Connector*%
                %TO.CLbN,Connector_PinHeader_2.54mm*%
                %TO.CMnt,TH*%
                %TO.CRot,0*%
                X5000000Y5000000D03*
                %TD*%
                """;
        GerberDocument doc = parser.parse(HEADER_TOP + APERTURES + block + "M02*\n");

        List<ComponentPlacement> comps = doc.getComponents();
        assertEquals(1, comps.size());
        assertEquals("J1",   comps.get(0).getRefdes());
        assertEquals("TH",   comps.get(0).getMountType());
        assertEquals("Top",  comps.get(0).getSide());
    }

    @Test
    void emptyPnPFileYieldsZeroComponents() {
        // A file that declares Component/Top function but has no data (like an empty top PnP layer).
        String empty = """
                %TF.FileFunction,Component,L1,Top*%
                %FSLAX46Y46*%
                %MOMM*%
                %LPD*%
                G04 APERTURE LIST*
                G04 APERTURE END LIST*
                M02*
                """;
        GerberDocument doc = parser.parse(empty);
        assertTrue(doc.isComponentFile());
        assertEquals("Top", doc.getComponentSide());
        assertEquals(0, doc.getComponents().size());
    }

    @Test
    void polygonApertureForPinsDoesNotCrash() {
        // D12 is a polygon aperture (%ADD12P,...%) — must parse without exception,
        // and pin flashes using it must not be counted as centroids.
        String block = """
                D10*
                %TO.C,R2*%
                %TO.CFtp,R_0402_1005Metric*%
                %TO.CVal,1k*%
                %TO.CMnt,SMD*%
                %TO.CRot,180*%
                X3000000Y4000000D03*
                D12*
                %TO.P,R2,1*%
                X3500000Y4000000D03*
                %TO.P,R2,2*%
                X2500000Y4000000D03*
                %TD*%
                """;
        GerberDocument doc = assertDoesNotThrow(
            () -> parser.parse(HEADER_BOTTOM + APERTURES + block + "M02*\n"));

        assertEquals(1, doc.getComponents().size());
        assertEquals("R2",  doc.getComponents().get(0).getRefdes());
        assertEquals(180.0, doc.getComponents().get(0).getRotation(), 0.001);
    }

    @Test
    void rotatedCourtyardOutlineDoesNotProduceCentroid() {
        // Z-rotated components (e.g. 30° or 120°) draw courtyard with diagonal D01
        // lines. Those are draws, not flashes — so the centroid count must still be 1.
        String block = """
                D10*
                %TO.C,Z1*%
                %TO.CFtp,R_0402_1005Metric*%
                %TO.CVal,0R*%
                %TO.CMnt,SMD*%
                %TO.CRot,30*%
                X3000000Y5000000D03*
                D11*
                X4033574Y5059799D02*
                X2431427Y4134799D01*
                X1966425Y4940202D01*
                X3568572Y5865202D01*
                X4033574Y5059799D01*
                %TD*%
                """;
        GerberDocument doc = parser.parse(HEADER_BOTTOM + APERTURES + block + "M02*\n");
        assertEquals(1, doc.getComponents().size());
        assertEquals(30.0, doc.getComponents().get(0).getRotation(), 0.001);
    }

    @Test
    void centroidCoordinatesAreInMillimeters() {
        // FSLAX46Y46: raw integer 6250000 → 0006.250000 → 6.25 mm.
        // FSLAX46Y46: raw integer 26500000 → 0026.500000 → 26.5 mm.
        String block = """
                D10*
                %TO.C,C9*%
                %TO.CFtp,C_0603_1608Metric*%
                %TO.CVal,4u7*%
                %TO.CMnt,SMD*%
                %TO.CRot,180*%
                X6250000Y26500000D03*
                %TD*%
                """;
        GerberDocument doc = parser.parse(HEADER_BOTTOM + APERTURES + block + "M02*\n");

        assertEquals(1, doc.getComponents().size());
        assertEquals(6.25,  doc.getComponents().get(0).getX(), 1e-4);
        assertEquals(26.5,  doc.getComponents().get(0).getY(), 1e-4);
    }

    @Test
    void manyComponentsAllCaptured() {
        // Five consecutive components; verifies that %TD% + new %TO.C% correctly
        // alternates state for each one without double-counting or skipping.
        StringBuilder body = new StringBuilder(APERTURES);
        String[] refs    = {"R1","R2","C1","C2","U1"};
        String[] vals    = {"10k","22k","100n","10u","MCU"};
        int[]    xRaw    = {500000, 1000000, 1500000, 2000000, 5000000};
        int[]    yRaw    = {1000000, 2000000, 3000000, 4000000, 10000000};
        double[] rotDeg  = {0, 90, 180, -90, 270};

        for (int i = 0; i < refs.length; i++) {
            body.append("D10*\n")
                .append("%TO.C,").append(refs[i]).append("*%\n")
                .append("%TO.CVal,").append(vals[i]).append("*%\n")
                .append("%TO.CMnt,SMD*%\n")
                .append("%TO.CRot,").append((int) rotDeg[i]).append("*%\n")
                .append("X").append(xRaw[i]).append("Y").append(yRaw[i]).append("D03*\n")
                .append("%TD*%\n");
        }
        body.append("M02*\n");

        GerberDocument doc = parser.parse(HEADER_BOTTOM + body);
        List<ComponentPlacement> comps = doc.getComponents();
        assertEquals(5, comps.size());

        for (int i = 0; i < refs.length; i++) {
            assertEquals(refs[i], comps.get(i).getRefdes(), "refdes at index " + i);
            assertEquals(rotDeg[i], comps.get(i).getRotation(), 0.001, "rotation at index " + i);
        }
    }
}
