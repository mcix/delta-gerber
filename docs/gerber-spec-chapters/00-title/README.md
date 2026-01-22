![](_page_0_Picture_0.jpeg)

# **The Gerber Layer Format Specification**

**A format developed by Ucamco.** 

**Named in honor of Joseph Gerber, inventor, entrepreneur, and photo-plotting pioneer.** 

Revision 2024.05

![](_page_0_Picture_5.jpeg)

![](_page_0_Picture_6.jpeg)

![](_page_1_Picture_0.jpeg)

# <span id="page-1-0"></span>Contents

| C  | Contents2                                |    |
|----|------------------------------------------|----|
| Pı | reface                                   | 8  |
| 1  | Introduction                             | 9  |
|    | 1.1 Scope and Target Audience            | 9  |
|    | 1.2 Further Resources                    | 9  |
|    | 1.3 Reference Gerber Viewer              | 9  |
|    | 1.4 Copyright and Intellectual Property  | 10 |
| 2  | Overview                                 | 11 |
|    | 2.1 File Structure                       | 11 |
|    | 2.2 Apertures                            | 11 |
|    | 2.3 Graphical objects                    | 12 |
|    | 2.3.1 Draws and Arcs                     | 13 |
|    | 2.3.2 Polarity                           |    |
|    | 2.4 Blocks                               | 15 |
|    | 2.5 Attributes                           |    |
|    | 2.6 Operations (D01, D02, D03)           |    |
|    | 2.7 Graphics State                       |    |
|    | 2.8 Commands Overview                    |    |
|    | 2.9 Processing a Gerber File             | 20 |
|    | 2.10 Glossary                            | 22 |
|    | 2.11 Annotated Example Files             |    |
|    | 2.11.1 Example: Two Square Boxes         |    |
|    | 2.11.2 Example: Polarities and Apertures |    |
|    | 2.12 Conformance                         | 31 |
| 3  | Syntax                                   | 32 |
|    | 3.1 Character Set                        | 32 |
|    | 3.2 Formal Grammar                       | 32 |
|    | 3.3 Commands                             | 34 |
|    | 3.4 Data Types                           | 36 |
|    | 3.4.1 Integers                           |    |
|    | 3.4.2 Decimals                           | 36 |
|    | 3.4.3 Strings                            | 36 |
|    | 3.4.4 Fields                             |    |
|    | 3.4.5 Names                              | 37 |
|    | 3.5 Grammar of the Gerber Layer Format   | 38 |

![](_page_2_Picture_0.jpeg)

|   | 3.6 File Extension, MIME Type and UTI         | 44 |
|---|-----------------------------------------------|----|
| 4 | Graphics                                      | 45 |
|   | 4.1 Comment (G04)                             | 45 |
|   | 4.2 Coordinate Commands                       | 46 |
|   | 4.2.1 Unit (MO)                               | 47 |
|   | 4.2.2 Format Specification (FS)               | 48 |
|   | 4.3 Aperture Definition (AD)                  | 49 |
|   | 4.3.1 AD Command                              | 49 |
|   | 4.3.2 Examples                                | 50 |
|   | 4.3.3 Zero-size Apertures                     | 50 |
|   | 4.4 Standard Aperture Templates               |    |
|   | 4.4.1 Overview                                | 51 |
|   | 4.4.2 Circle                                  | 51 |
|   | 4.4.3 Rectangle                               | 53 |
|   | 4.4.4 Obround                                 | 54 |
|   | 4.4.5 Polygon                                 | 55 |
|   | 4.4.6 Transparency of Holes                   | 56 |
|   | 4.5 Aperture Macro (AM)                       | 57 |
|   | 4.5.1 Primitives                              | 59 |
|   | 4.5.2 Exposure Parameter                      | 68 |
|   | 4.5.3 Rotation Parameter                      | 69 |
|   | 4.5.4 Macro Variables and Expressions         | 70 |
|   | 4.5.5 Examples                                | 72 |
|   | 4.6 Set Current Aperture (Dnn)                | 75 |
|   | 4.7 Plot State Commands (G01,G02,G03,G75)     | 76 |
|   | 4.7.1 Linear Plotting (G01)                   | 76 |
|   | 4.7.2 Circular Plotting (G02, G03, G75)       | 77 |
|   | 4.8 Operations (D01/D02/D03)                  | 81 |
|   | 4.8.1 Overview                                | 81 |
|   | 4.8.2 Plot (D01)                              | 83 |
|   | 4.8.3 Move (D02)                              | 83 |
|   | 4.8.4 Flash (D03)                             | 83 |
|   | 4.8.5 Example                                 | 84 |
|   | 4.9 Aperture Transformations (LP, LM, LR, LS) | 85 |
|   | 4.9.1 Overview                                | 85 |
|   | 4.9.2 Load Polarity (LP)                      | 87 |
|   | 4.9.3 Load Mirroring (LM)                     | 87 |
|   | 4.9.4 Load Rotation (LR)                      |    |
|   | 4.9.5 Load Scaling (LS)                       |    |
|   | 4.9.6 Example                                 | 88 |
|   | 4.10 Region Statement (G36/G37)               |    |
|   | 4.10.1 Region Overview                        |    |
|   | 4.10.2 Region Statement Syntax                |    |
|   |                                               |    |

![](_page_3_Picture_0.jpeg)

|   | 4.10.3 Valid Contours                        | 91  |
|---|----------------------------------------------|-----|
|   | 4.10.4 Examples                              | 93  |
|   | 4.10.5 Copper Pours, Power and Ground Planes | 108 |
|   | 4.11 Block Aperture (AB)                     | 111 |
|   | 4.11.1 Overview of block apertures           | 111 |
|   | 4.11.2 AB Statement Syntax                   | 111 |
|   | 4.11.3 Usage of Block Apertures              | 113 |
|   | 4.11.4 Example                               | 114 |
|   | 4.12 Step and Repeat (SR)                    | 116 |
|   | 4.13 End-of-file (M02)                       | 119 |
|   | 4.14 Numerical Accuracy                      | 120 |
|   | 4.14.1 Visualization                         | 120 |
|   | 4.14.2 Image Processing                      | 120 |
| 5 | Attributes                                   | 122 |
|   | 5.1 Attributes Overview                      |     |
|   | 5.2 File Attributes (TF)                     |     |
|   | 5.3 Aperture Attributes (TA)                 |     |
|   | 5.3.1 Aperture Attributes on Regions         |     |
|   | 5.4 Object Attributes (TO)                   |     |
|   | 5.5 Delete Attribute (TD)                    |     |
|   | 5.6 Standard Attributes                      |     |
|   | 5.6.1 Overview                               |     |
|   | 5.6.2 .Part                                  |     |
|   | 5.6.3 .FileFunction                          |     |
|   | 5.6.4 .FilePolarity                          |     |
|   | 5.6.5 .SameCoordinates                       |     |
|   | 5.6.6 .CreationDate                          |     |
|   | 5.6.7 .GenerationSoftware                    |     |
|   | 5.6.8 .ProjectId                             |     |
|   | 5.6.9 .MD5                                   |     |
|   | 5.6.10 .AperFunction                         |     |
|   | 5.6.11 .DrillTolerance                       |     |
|   | 5.6.12 .FlashText                            |     |
|   | 5.6.13 .N (Net)                              |     |
|   | 5.6.14 .P (Pin)                              |     |
|   | 5.6.15 .C (Component Refdes)                 |     |
|   | 5.6.16 . Cxxx (Component Characteristics)    |     |
|   | 5.7 Text in the Image                        |     |
|   | 5.8 Examples                                 |     |
|   |                                              |     |
| 6 | PCB Fabrication and Assembly Data            |     |
|   | 6.1 Structure                                | 159 |

![](_page_4_Picture_0.jpeg)

|   | 6.2 Mandatory Attributes                                | 159 |
|---|---------------------------------------------------------|-----|
|   | 6.3 Alignment                                           | 159 |
|   | 6.4 Pads                                                | 159 |
|   | 6.5 The Profile                                         | 159 |
|   | 6.6 Drill/rout files                                    | 160 |
|   | 6.6.1 Backdrilling                                      |     |
|   | 6.6.2 Example Drill File                                | 161 |
|   | 6.7 Drawings and Data                                   |     |
|   | 6.8 The CAD Netlist                                     | 165 |
|   | 6.8.1 Benefits of Including the CAD Netlist             |     |
|   | 6.8.2 IP Considerations                                 |     |
|   | 6.9 Component Data                                      |     |
|   | 6.9.1 Overview                                          |     |
|   | 6.9.2 Assembly Data Set                                 |     |
|   | 6.9.3 Annotated Example Component Layer                 | 168 |
| 7 | Errors and Bad Practices                                | 170 |
|   | 7.1 Errors                                              | 170 |
|   | 7.2 Bad Practices                                       | 172 |
| 8 | Deprecated Format Elements                              | 174 |
|   | 8.1 Deprecated Commands                                 |     |
|   | 8.1.1 Overview                                          |     |
|   | 8.1.2 Axis Select (AS)                                  |     |
|   | 8.1.3 Image Name (IN)                                   |     |
|   | 8.1.4 Image Polarity (IP)                               |     |
|   | 8.1.5 Image Rotation (IR)                               |     |
|   | 8.1.6 Load Name (LN)                                    |     |
|   | 8.1.7 Mirror Image (MI)                                 |     |
|   | 8.1.8 Offset (OF)                                       |     |
|   | 8.1.9 Scale Factor (SF)                                 |     |
|   | 8.1.10 Single-quadrant arc mode (G74)                   |     |
|   | 8.2 Deprecated Command Options                          |     |
|   | 8.2.1 Format Specification (FS) Options                 |     |
|   | 8.2.2 Rectangular Hole in Standard Apertures            |     |
|   | 8.2.3 Draws and Arcs with Rectangular Apertures         |     |
|   | 8.2.4 Macro Primitive Code 2, Vector Line               |     |
|   | 8.2.5 Macro Primitive Code 22, Lower Left Line          |     |
|   | 8.2.6 Macro Primitive Code 6, Moiré                     |     |
|   | 8.3 Deprecated Syntax Variations                        |     |
|   | 8.3.1 Combining G01/G02/G03 and D01 in a single command |     |
|   | 8.3.2 Coordinate Data without Operation Code            |     |
|   | 8.3.3 Style Variations in Command Codes                 |     |
|   |                                                         |     |

![](_page_5_Picture_0.jpeg)

| 8.3.4 Deprecated usage of SR                   | 194 |
|------------------------------------------------|-----|
| 8.4 Deprecated Attribute Values                | 194 |
| 8.5 Standard Gerber (RS-274-D)                 | 195 |
| 9 References                                   | 196 |
|                                                |     |
| 10 History                                     | 197 |
| 11 Revisions                                   | 199 |
| 11.1 Revision xxxx.xx                          | 199 |
| 11.2 Revision 2023.08                          | 199 |
| 11.3 Revision 2023.03                          | 199 |
| 11.4 Revision 2022.02                          | 199 |
| 11.5 Revision 2021.11                          | 199 |
| 11.6 Revision 2021.04                          | 200 |
| 11.7 Revision 2021.02 – Formal grammar         | 200 |
| 11.8 Revision 2020.09 – X3                     | 200 |
| 11.9 Revision 2019.09                          | 200 |
| 11.10 Revision 2019.06                         | 201 |
| 11.11Revision 2018.11                          | 201 |
| 11.12Revision 2018.09                          | 201 |
| 11.13Revision 2018.06                          | 201 |
| 11.14Revision 2018.05                          | 201 |
| 11.15Revision 2017.11                          | 202 |
| 11.16Revision 2017.05                          | 202 |
| 11.17 Revision 2017.03                         | 202 |
| 11.18Revision 2016.12 – Nested step and repeat | 202 |
| 11.19Revision 2016.11                          | 203 |
| 11.20 Revision 2016.09                         | 203 |
| 11.21Revision 2016.06                          | 203 |
| 11.22Revision 2016.04                          | 203 |
| 11.23Revision 2016.01                          | 204 |
| 11.24Revision 2015.10                          | 204 |
| 11.25Revision 2015.07                          | 204 |
| 11.26Revision 2015.06                          | 204 |
| 11.27 Revision J4 (2015 02)                    | 204 |
| 11.28Revision J3 (2014 10)                     | 205 |
| 11.29Revision J2 (2014 07)                     | 205 |
| 11.30 Revision J1 (2014 02) – X2               |     |
| 11.31Revision I4 (2013 10)                     | 205 |
| 11.32Revision I3 (2013 06)                     | 205 |
| 11.33Revision I2 (2013 04)                     | 205 |
| 11.34Revision I1 (2012 12)                     | 205 |

![](_page_6_Picture_0.jpeg)

![](_page_7_Picture_0.jpeg)

# <span id="page-7-0"></span>**Preface**

The Gerber format is the de facto open standard for printed circuit board (PCB) design data transfer. As a UTF-8 human-readable format Gerber is portable and easy to debug. It is compact and unequivocal, and simple - there are just 27 commands. Every PCB design system outputs Gerber files and every PCB fabrication software inputs them. Implementations are thoroughly field-tested. Gerber's widespread availability allows PCB professionals to exchange PCB design data securely and efficiently. It has been called "the backbone of the electronics fabrication industry".

Gerber is at its core is an open vector format for 2D binary images specifying PCB copper layers, solder mask, legend, etc. Attributes transfer meta-information with the images. Attributes are akin to labels expressing, for example, that an image represents the top solder mask, or that a pad stack is a via or component stack, and which components are positioned where. Attributes transfer the meta-information necessary for fabrication and assembly. A set of wellconstructed Gerber files reliably and productively transfers PCB fabrication from design to fabrication, and component information to assembly.

Although other data formats have appeared, they have not displaced Gerber. The reason is simple. Any problems in PCB fabrication data transfer are not due to limitations in the Gerber format but are due to poor practices and poor implementations. The new formats are more complicated and less transparent. To quote Günther Schindler: *"There are no superfluous, production-specific attributes like in other CAM formats. Gerber X2 is simple and tidy."* Poor practices and poor implementations in unfamiliar, new and complicated formats are more damaging than in a well-known, well-tested and simple format. The industry has not adopted new formats. Gerber remains the standard.

Ucamco continuously clarifies this document and adapts it to current needs based on input from the user community. Ucamco thanks the individuals that help us with comments, criticism, and suggestions.

The Gerber format was named after Joseph Gerber, who fled his native Austria from the Nazis, and arrived in the USA with his mother, penniless. With his technical genius and hard work he became a very successful inventor and technical entrepreneur. The American dream come true. Mr. Gerber pioneered photoplotting in PCB fabrication, which is the link with the format. Ucamco is honored to look after a format called after this brilliant man.

The emergence of Gerber as a standard for PCB fabrication data is the result of efforts by many individuals who developed outstanding software for Gerber files. Without their dedication there would be no standard format in the electronics fabrication industry. Ucamco thanks these dedicated individuals.

Karel Tavernier Managing Director, Ucamco

![](_page_8_Picture_0.jpeg)

