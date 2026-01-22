# Gerber/Drill Test Suite

A comprehensive test suite for validating Gerber RS-274X and Excellon drill parsers and SVG renderers.

## Purpose

This test suite provides reference Gerber and drill files that can be used to:
1. Validate parser implementations against known inputs
2. Compare SVG output against battle-tested reference renderers
3. Visually verify rendering correctness

## Directory Structure

```
test-gerber-suite/
├── apertures/              # Standard aperture templates (Section 4.4)
│   ├── 01_circle_basic.gbr
│   ├── 02_circle_with_hole.gbr
│   ├── 03_rectangle_basic.gbr
│   ├── 04_rectangle_with_hole.gbr
│   ├── 05_obround_basic.gbr
│   ├── 06_polygon_vertices.gbr
│   ├── 07_polygon_rotation.gbr
│   └── 08_zero_size_aperture.gbr     # Zero-diameter circle (for outlines)
├── macros/                 # Macro aperture primitives (Section 4.5)
│   ├── 01_circle_primitive.gbr       # Code 1
│   ├── 02_vector_line_primitive.gbr  # Code 20
│   ├── 03_center_line_primitive.gbr  # Code 21
│   ├── 04_outline_primitive.gbr      # Code 4
│   ├── 05_polygon_primitive.gbr      # Code 5
│   ├── 06_thermal_primitive.gbr      # Code 7
│   └── 07_macro_variables.gbr        # Variables and expressions
├── plotting/               # Plot operations (Sections 4.7-4.8)
│   ├── 01_linear_interpolation.gbr   # G01 + D01
│   ├── 02_circular_cw.gbr            # G02 + D01
│   ├── 03_circular_ccw.gbr           # G03 + D01
│   ├── 04_full_circles.gbr           # 360° arcs (start == end)
│   └── 05_modal_coordinates.gbr      # Omitting X/Y when unchanged
├── regions/                # Region statements (Section 4.10)
│   ├── 01_simple_regions.gbr         # G36/G37 with linear segments
│   ├── 02_regions_with_arcs.gbr      # G36/G37 with circular segments
│   ├── 03_regions_with_holes_polarity.gbr  # Holes via LPC
│   ├── 04_regions_with_cutins.gbr    # Holes via cut-in contours
│   └── 05_multiple_contours.gbr      # D02 to start multiple contours
├── polarity/               # Polarity commands (Section 4.9.2)
│   └── 01_polarity_basic.gbr         # LPD/LPC
├── transforms/             # Aperture transformations (Section 4.9)
│   ├── 01_rotation.gbr               # LR
│   ├── 02_scaling.gbr                # LS
│   └── 03_mirroring.gbr              # LM
├── blocks/                 # Block apertures (Section 4.11)
│   ├── 01_block_aperture_basic.gbr   # AB statement basics
│   ├── 02_block_aperture_transforms.gbr  # AB with LM, LR, LS
│   └── 03_block_aperture_polarity.gbr    # AB with LPD/LPC
├── step-repeat/            # Step and repeat (Section 4.11)
│   ├── 01_step_repeat_basic.gbr      # SR statement basics
│   └── 02_step_repeat_complex.gbr    # SR with regions, arcs
├── attributes/             # Attributes (Chapter 5)
│   ├── 01_file_attributes.gbr        # TF commands
│   └── 02_aperture_object_attributes.gbr  # TA, TO, TD commands
├── inch/                   # Inch unit tests
│   ├── 01_inch_apertures.gbr         # Standard apertures in inches
│   └── 02_inch_plotting.gbr          # Plotting in inches
├── drill/                  # Excellon drill files
│   ├── 01_drill_basic.drl            # Basic drill holes
│   └── 02_drill_slots.drl            # Routed slots (G85)
├── combined/               # Combined/comprehensive tests
│   ├── board_outline.gbr             # Typical PCB outline layer
│   ├── comprehensive_test.gbr        # Original comprehensive test
│   └── all_features_comprehensive.gbr # Full feature coverage
└── reference-svg/          # Reference SVG outputs
```

## Test Coverage

### Units
- **Millimeters (MOMM)**: All tests in main directories
- **Inches (MOIN)**: Tests in `inch/` directory

### Standard Apertures (C, R, O, P)
- Circle: Various diameters, with/without center holes, zero-size
- Rectangle: Various aspect ratios, with/without holes
- Obround: Horizontal/vertical orientations
- Polygon: 3-12 vertices, rotation parameter

### Macro Primitives (AM command)
- Circle (1): Basic, offset, rotated, multiple, donut (exposure off)
- Vector Line (20): Horizontal, vertical, diagonal, rotated
- Center Line (21): Various orientations and rotations
- Outline (4): Triangle, square, pentagon, arrow, L-shape
- Polygon (5): Regular polygons, rotated, offset
- Thermal (7): Various gap widths, rotations
- Variables and Expressions: $n parameters, arithmetic (+, -, x, /)

### Operations
- Flash (D03): All aperture types
- Move (D02): Path starting, new contours in regions
- Linear Plot (G01 + D01): Lines, rectangles, multi-segment paths
- Circular CW (G02 + D01): Arcs 45°-360°, S-curves
- Circular CCW (G03 + D01): Arcs, combined paths
- Full Circles (360°): Start point equals end point
- Modal Coordinates: Omitting X or Y when unchanged

### Regions (G36/G37)
- Simple linear contours
- Contours with circular segments
- Rounded rectangles, circles, semicircles, stadium shapes
- Multiple contours in single region statement
- Overlapping and touching contours
- Holes using clear polarity (LPC)
- Holes using cut-in (fully-coincident segments)

### Block Apertures (AB statement)
- Basic block definition and flashing
- Blocks with draws, arcs, flashes
- Blocks with polarity switching (LPD/LPC)
- Blocks with transformations (LM, LR, LS)

### Step and Repeat (SR statement)
- Basic XY arrays with spacing
- Step and repeat with draws
- Step and repeat with regions
- Step and repeat with polarity

### Polarity & Transformations
- Dark (LPD) and Clear (LPC) polarity
- Nested polarity (rings, holes)
- Rotation (LR): 0°, 30°, 45°, 60°, 90°, 120°, 180°, 270°
- Scaling (LS): 0.5x, 0.75x, 1.0x, 1.25x, 1.5x, 2.0x
- Mirroring (LM): N, X, Y, XY
- Combined transformations

### Attributes (TF, TA, TO, TD)
- File attributes: .FileFunction, .FilePolarity, .Part, etc.
- Aperture attributes: .AperFunction (SMDPad, ViaPad, etc.)
- Object attributes: .C, .P, .N (component, pin, net)
- Attribute deletion: TD command

### Drill Files
- Multiple tool definitions
- Various hole sizes
- Routed slots (G85)

## Validation Workflow

1. **Generate Reference SVGs**: Use a trusted renderer (gerbv, KiCad, etc.) to generate reference SVGs
2. **Generate Test SVGs**: Use your implementation to generate SVGs
3. **Compare**: Visual comparison or automated diff

```bash
# Example using gerbv
gerbv -x svg -o reference.svg test.gbr

# Example using your implementation
java -jar gerber-parser.jar test.gbr -o test.svg

# Compare
diff reference.svg test.svg
```

## Feature Matrix

| Feature | Test File(s) | Spec Section |
|---------|-------------|--------------|
| Units (mm) | Most files | 4.2.1 |
| Units (inch) | `inch/01_*.gbr`, `inch/02_*.gbr` | 4.2.1 |
| Format Spec | All files use `%FSLAX26Y26*%` | 4.2.2 |
| Circle aperture | `apertures/01_*.gbr`, `apertures/02_*.gbr` | 4.4.2 |
| Rectangle aperture | `apertures/03_*.gbr`, `apertures/04_*.gbr` | 4.4.3 |
| Obround aperture | `apertures/05_*.gbr` | 4.4.4 |
| Polygon aperture | `apertures/06_*.gbr`, `apertures/07_*.gbr` | 4.4.5 |
| Zero-size aperture | `apertures/08_*.gbr` | 4.3.2 |
| Macro circle | `macros/01_*.gbr` | 4.5.1.3 |
| Macro vector line | `macros/02_*.gbr` | 4.5.1.4 |
| Macro center line | `macros/03_*.gbr` | 4.5.1.5 |
| Macro outline | `macros/04_*.gbr` | 4.5.1.6 |
| Macro polygon | `macros/05_*.gbr` | 4.5.1.7 |
| Macro thermal | `macros/06_*.gbr` | 4.5.1.8 |
| Macro variables | `macros/07_*.gbr` | 4.5.4 |
| Linear plot (G01) | `plotting/01_*.gbr` | 4.7.1 |
| Circular CW (G02) | `plotting/02_*.gbr` | 4.7.2 |
| Circular CCW (G03) | `plotting/03_*.gbr` | 4.7.2 |
| Full circles | `plotting/04_*.gbr` | 4.7.2 |
| Modal coords | `plotting/05_*.gbr` | 4.8 |
| Regions simple | `regions/01_*.gbr` | 4.10 |
| Regions with arcs | `regions/02_*.gbr` | 4.10 |
| Regions with holes | `regions/03_*.gbr`, `regions/04_*.gbr` | 4.10.3 |
| Multiple contours | `regions/05_*.gbr` | 4.10.4 |
| Polarity | `polarity/01_*.gbr` | 4.9.2 |
| Rotation (LR) | `transforms/01_*.gbr` | 4.9.4 |
| Scaling (LS) | `transforms/02_*.gbr` | 4.9.5 |
| Mirroring (LM) | `transforms/03_*.gbr` | 4.9.3 |
| Block apertures | `blocks/01_*.gbr`, `blocks/02_*.gbr`, `blocks/03_*.gbr` | 4.11 |
| Step and repeat | `step-repeat/01_*.gbr`, `step-repeat/02_*.gbr` | 4.11 |
| File attributes | `attributes/01_*.gbr` | 5.2 |
| Aperture attributes | `attributes/02_*.gbr` | 5.3 |
| Object attributes | `attributes/02_*.gbr` | 5.4 |

## Gerber Format Reference

Based on Gerber Layer Format Specification Revision 2022.02

- Section 4.1: Comment (G04)
- Section 4.2: Coordinate Commands (MO, FS)
- Section 4.3: Aperture Definition (AD)
- Section 4.4: Standard Aperture Templates (C, R, O, P)
- Section 4.5: Aperture Macro (AM)
- Section 4.6: Set Current Aperture (Dnn)
- Section 4.7: Plot State Commands (G01, G02, G03, G75)
- Section 4.8: Operations (D01, D02, D03)
- Section 4.9: Aperture Transformations (LP, LM, LR, LS)
- Section 4.10: Region Statement (G36/G37)
- Section 4.11: Block Aperture (AB) and Step & Repeat (SR)
- Section 4.13: End of File (M02)
- Chapter 5: Attributes (TF, TA, TO, TD)
