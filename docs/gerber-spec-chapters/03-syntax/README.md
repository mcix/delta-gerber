# <span id="page-31-0"></span>3 **Syntax**

### <span id="page-31-1"></span>**3.1 Character Set**

A Gerber file is expressed in UTF-8 Unicode. The new line characters CR and LF are allowed at the end of words – after '\*' – or commands – after '%'. Their use is encouraged to increase human readability. The Gerber file is therefore human-readable and transferrable between systems.

The characters '\*' and '%' are delimiters and can only be used as prescribed in the syntax. Space characters are *only* allowed inside strings and fields (see [3.4.3](#page-35-3) and [3.4.4\)](#page-36-0).

Gerber files are case-sensitive. Command codes must be in upper case.

Actually, *except for user defined attribute values,* all characters in a Gerber file are restricted to the readable ASCII characters, with codes 32 to 126, and new line characters. User defined meta-data may require special characters such as μ or may be in other languages than English; hence UTF-8 Unicode is allowed.

### <span id="page-31-2"></span>**3.2 Formal Grammar**

The formal grammar used in this specification is the parsing expression grammar (PEG), similar in formalism to context-free grammars, with a somewhat different interpretation. See [https://en.wikipedia.org/wiki/Parsing\\_expression\\_grammar](https://en.wikipedia.org/wiki/Parsing_expression_grammar) for more information. The grammar of is expressed in the variant of the Extended Backus-Naur Form used by the TatSu PEG parser generator. <https://tatsu.readthedocs.io/en/stable/> for more information. Only a subset of the rules in the very powerful TatSu grammar is needed – after all Gerber is a simple format. Below is a description of that subset, taken from the TatSu documentation.

A grammar consists of a sequence of one or more rules of the form:

```
name = <expression> ;
```

The expressions are constructed from the following operators, in reverse order of precedence.

![](_page_32_Picture_0.jpeg)

| Grammar Syntax Rules |                    |                                                                                                                                                                                                                                                                                       |
|----------------------|--------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Rule                 | Name               | Description                                                                                                                                                                                                                                                                           |
| #                    | Comment            | Comments have no effect on the grammar.                                                                                                                                                                                                                                               |
| e1   e2              | Choice             | Match either e1<br>or e2                                                                                                                                                                                                                                                              |
|                      |                    | A   can be used before the first option as a layout aid:                                                                                                                                                                                                                              |
|                      |                    | rule =                                                                                                                                                                                                                                                                                |
|                      |                    | option1<br>  option2                                                                                                                                                                                                                                                                  |
|                      |                    | option3                                                                                                                                                                                                                                                                               |
|                      |                    | ;                                                                                                                                                                                                                                                                                     |
| (e)                  | Grouping           | Match e, making it a node in the syntax tree                                                                                                                                                                                                                                          |
| [e]                  | Option             | Optionally match e                                                                                                                                                                                                                                                                    |
| {e}<br>or<br>{e}*    | Closure            | Match e<br>zero or more times.                                                                                                                                                                                                                                                        |
| {e}+                 | Positive closure   | Match e<br>one or more times.                                                                                                                                                                                                                                                         |
| &e                   | Positive lookahead | Succeeds if e<br>can be parsed. Does not consume input                                                                                                                                                                                                                                |
| !e                   | Negative lookahead | Fails if e<br>can be parsed. Does not consume input                                                                                                                                                                                                                                   |
| 'text'               | Token              | Match the token text.                                                                                                                                                                                                                                                                 |
|                      |                    | If text is alphanumeric it will only parse if the character<br>following the token is not alphanumeric. This is done to<br>prevent tokens like IN matching when the text ahead<br>is INITIALIZE. This feature can be turned off by setting<br>the grammar directive @@nameguard=False |
| /regexp/             | Regex              | The pattern expression.                                                                                                                                                                                                                                                               |
|                      |                    | Match the regular expression regexp at the current text<br>position.                                                                                                                                                                                                                  |
|                      |                    | Python style regex is used, and it is interpreted as a<br>Python raw string.                                                                                                                                                                                                          |
| \$                   | End-of-text        | Verify that the end of the input text has been reached.                                                                                                                                                                                                                               |

![](_page_33_Picture_0.jpeg)

| Grammar Directives                                                            |                                                                                                                                                                                                       |  |
|-------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--|
| Directive                                                                     | Description                                                                                                                                                                                           |  |
| @@grammar :: <word< th=""><th>Specifies the name of the grammar.</th></word<> | Specifies the name of the grammar.                                                                                                                                                                    |  |
| @@nameguard :: <bool></bool>                                                  | When set to True, avoids matching tokens when the next<br>character in the input sequence is alphanumeric. Defaults<br>to True. See the 'text' expression for an explanation.<br>@@nameguard :: False |  |
|                                                                               | @@whitespace :: <regexp> Provides a regular expression for the whitespace to be<br/>ignored by the parser. It defaults to /(?s)\s+/<br/>@@whitespace :: /[\t ]+</regexp>                              |  |

### <span id="page-33-0"></span>**3.3 Commands**

Commands are the core syntactic element of the Gerber format. A Gerber file is a stream of commands. Commands define the graphics state, create graphical objects, defines apertures, manage attributes and so on.

Commands are built with *words*, the basic syntactic building block of a Gerber file. A word is a non-empty character string, excluding the reserved characters '\*' and '%', terminated with an '\*'

```
free_character = /[^%*]/; # All characters but * and % 
word = {free_character}+ '*';
```

For historic reasons, there are two command syntax styles: word commands and extended commands.

```
command = 
 | extended_command 
 | word_command 
 ; 
word_command = word; 
extended_command = '%' {word}+ '%';
```

Word commands are identified by a command code, the letter G, D or M followed by a positive integer, e.g. G02. Most word commands only consist of the command code, some also contain coordinates.

Extended commands are identified by a two-character command code that is followed by parameters specific to the code. An extended command is enclosed by a pair of '%' delimiters.

An overview of all commands is in section [2.8,](#page-18-0) a full description in chapters [3.5](#page-37-0) and [5.](#page-121-0)

The example below shows a stream of Gerber commands. Word commands are in yellow, extended commands in green.

## **Example:**

```
G04 Different command styles* 
%FSLAX26Y26*%
%MOMM*%
%AMDonut*
1,1,$1,$2,$3*
$4=$1x0.75*
1,0,$4,$2,$3*
```

![](_page_34_Picture_0.jpeg)

% %ADD11Donut,0.30X0X0\*% %ADD10C,0.1\*% G75\* G02\* D10\* X0Y0D02\* X2000000Y0I1000000J0D01\* D11\* X0Y2000000D03\* M02\*

One of the strengths of the Gerber format is its human readability. Use line breaks to enhance readability; put one word or command per line.

![](_page_35_Picture_0.jpeg)

### <span id="page-35-0"></span>**3.4 Data Types**

<span id="page-35-1"></span>All data types are tokens in the Gerber syntax, expressed a regex.

### **Integers**

Integers must fit in a 32-bit signed integer. Examples:

```
0
  -1024
  +16
unsigned_integer = /[0-9]+/; 
positive_integer = /[0-9]*[1-9][0-9]*/; 
integer = /[+-]?[0-9]+/;
```

#### <span id="page-35-2"></span>**Decimals**

Decimals must fit in an IEEE double. Examples:

```
-200
5000
1234.56
.123
-0.128
0
```

```
unsigned_decimal = /((([0-9]+)(\.[0-9]*)?)|(\.[0-9]+))/; 
decimal = /[+-]?((([0-9]+)(\.[0-9]*)?)|(\.[0-9]+))/;
```

<span id="page-35-3"></span>Note that a standalone comma ',' is *not* a valid decimal point.

#### **Strings**

Strings are used for communication between humans and can contain other characters than ASCII. These are expressed by UTF-8 literal characters, or by a Unicode escape. Note that reserved characters *must* be escaped.

```
string = /[^%*]*/; # All characters except *%
```

Note that UTF-8 is identical to ASCII for any character that can be represented by ASCII.

The Unicode escape for the copyright symbol '©' is as follows:

- With lower case u for a 16-bit hex value: \u00A9
- With upper case U for a 32-bit hex value: \U000000A9

Zero-fill if needed to reach the required 4 or 8 hex digits

- The reserved characters '%' (\u0025 ) and '\*' (\u002A) must *always* be escaped as they are the delimiters of the Gerber syntax.
- '\' (\u005C) must be escaped as it is the escape character.
- ',' (\u002C) separates fields, and therefore must be escaped in any string that does not end the word.

![](_page_36_Picture_0.jpeg)

Any character may be escaped. Escape every non-ASCII character if you need to keep a file ASCII-only. This may increase compatibility with legacy software but defeats human readability of the meta-data.

If you want to keep the Gerber file printable ASCII-only use escape sequences for any character in strings or fields. This may increase compatibility with legacy software but defeats human readability of the meta-data.

#### <span id="page-36-0"></span>**Fields**

The fields follow the string syntax in section [3.4.3](#page-35-3) with the additional restriction that a field must not contain commas. Fields are intended to represent comma-separated items in strings. A comma can be escaped with \u002C.

<span id="page-36-1"></span>field = /[^%\*,]\*/; # All characters except \*%,

### **Names**

Names identify something, such as an attribute. They are for use only within the Gerber format and are therefore limited to printable ASCII.

Names consist of upper- or lower-case ASCII letters, underscores ('\_'), dots ('.'), a dollar sign ('\$') and digits. The first character *cannot* be a digit. Names are from 1 to 127 characters long. Names beginning with a dot '.' are reserved for *standard names* defined in the specification. User defined names *cannot* begin with a dot.

```
Name = [._$a-zA-Z][._$a-zA-Z0-9]{0,126} 
StandardName = \.[._$a-zA-Z][._$a-zA-Z0-9]{0,125} 
UserDefinedName = [_$a-zA-Z][_.$a-zA-Z0-9]{0,126}
```

The scope of a name is from its definition till the end of the file. Names are case-sensitive.

Names for macro variables used in AM commands are more restrictive. They are of the form \$n, with n a positive integer, for example \$3.

![](_page_37_Picture_0.jpeg)

### <span id="page-37-0"></span>**3.5 Grammar of the Gerber Layer Format**

```
@@grammar :: Gerber_2022.02 
@@nameguard :: False 
@@whitespace :: /\n/ 
start = 
 { 
 | G04 
 | MO 
 | FS 
 | AD 
 | AM 
 | Dnn 
 | G75 
 | G01 
 | G02 
 | G03 
 | D01 
 | D02 
 | D03 
 | LP 
 | LM 
 | LR 
 | LS 
 | region_statement 
 | AB_statement 
 | SR_statement 
 | TF 
 | TA 
 | TO 
 | TD 
 }* 
 M02 
 $;
```

![](_page_38_Picture_0.jpeg)

```
# Graphics commands 
#------------------ 
G04 = 'G04' string '*'; 
MO = '%MO' ('MM'|'IN') '*%'; 
FS = '%FS' 'LA' 'X' coordinate_digits 'Y' coordinate_digits '*%'; 
coordinate_digits = /[1-6][6]/; 
G01 = 'G01*'; 
G02 = 'G02*'; 
G03 = 'G03*'; 
G75 = 'G75*'; 
AD = '%AD' 
 aperture_identifier 
 ( 
 | 'C' ',' ~ decimal ['X' decimal] 
 | 'R' ',' ~ decimal 'X' decimal ['X' decimal] 
 | 'O' ',' ~ decimal 'X' decimal ['X' decimal] 
 | 'P' ',' ~ decimal 'X' decimal ['X' decimal ['X' decimal]] 
 | name [',' decimal {'X' decimal}*] 
 ) 
 '*%'; 
AM = '%AM' name '*' macro_body '%'; 
macro_body = { primitive | variable_definition }+; 
variable_definition = macro_variable '=' expr '*'; 
primitive = 
 | '0' string '*' 
 | '1' ',' expr ',' expr ',' expr ',' expr [',' expr] '*' 
 | '20' ',' expr ',' expr ',' expr ',' expr ',' expr ',' expr ',' expr '*' 
 | '21' ',' expr ',' expr ',' expr ',' expr ',' expr ',' expr '*' 
 | '4' ',' expr ',' expr ',' expr ',' expr {',' expr ',' expr}+ ',' 
expr'*' 
 | '5' ',' expr ',' expr ',' expr ',' expr ',' expr ',' expr '*' 
 | '7' ',' expr ',' expr ',' expr ',' expr ',' expr ',' expr '*' 
 ; 
macro_variable = /\$[0-9]*[1-9][0-9]*/; 
expr = 
 |{/[+-]/ term}+ 
 |expr /[+-]/ term 
 |term 
 ; 
term = 
 |term /[x\/]/ factor 
 |factor 
 ; 
factor = 
 | '(' ~ expr ')'
```

![](_page_39_Picture_0.jpeg)

```
 |macro_variable 
 |unsigned_decimal 
 ; 
Dnn = aperture_identifier '*'; 
D01 = ['X' integer] ['Y' integer] ['I' integer 'J' integer] 'D01*'; 
D02 = ['X' integer] ['Y' integer] 'D02*'; 
D03 = ['X' integer] ['Y' integer] 'D03*'; 
LP = '%LP' ('C'|'D') '*%'; 
LM = '%LM' ('N'|'XY'|'Y'|'X') '*%'; 
LR = '%LR' decimal '*%'; 
LS = '%LS' decimal '*%'; 
M02 = 'M02*';
```

![](_page_40_Picture_0.jpeg)

```
region_statement = G36 {contour}+ G37; 
contour = D02 {D01|G01|G02|G03}*; 
G36 = 'G36*'; 
G37 = 'G37*'; 
AB_statement = AB_open block AB_close; 
AB_open = '%AB' aperture_identifier '*%'; 
AB_close = '%AB' '*%'; 
SR_statement = SR_open block SR_close; 
SR_open = '%SR' 'X' positive_integer 'Y' positive_integer 
 'I' decimal 'J' decimal '*%'; 
SR_close = '%SR' '*%'; 
block = 
 { 
 | G04 
 | MO 
 | FS 
 | AD 
 | AM 
 | Dnn 
 | D01 
 | D02 
 | D03 
 | G01 
 | G02 
 | G03 
 | G75 
 | LP 
 | LM 
 | LR 
 | LS 
 | region_statement 
 | AB_statement 
 | TF 
 | TA 
 | TO 
 | TD 
 }* 
 ;
```

![](_page_41_Picture_0.jpeg)

```
# Attribute commands 
#------------------- 
TF = '%TF' file_attribute_name {',' field}* '*%'; 
TA = '%TA' aperture_attribute_name {',' field}* '*%'; 
TO = '%TO' object_attribute_name {',' field}* '*%'; 
TD = '%TD' 
 [ 
 | file_attribute_name 
 | aperture_attribute_name 
 | object_attribute_name 
 | user_name 
 ] '*%'; 
file_attribute_name = 
 | '.Part' 
 | '.FileFunction' 
 | '.FilePolarity' 
 | '.SameCoordinates' 
 | '.CreationDate' 
 | '.GenerationSoftware' 
 | '.ProjectId' 
 | '.MD5' 
 | user_name 
 ; 
aperture_attribute_name = 
 | '.AperFunction' 
 | '.DrillTolerance' 
 | '.FlashText' 
 | user_name 
 ; 
object_attribute_name = 
 | '.N' 
 | '.P' 
 | '.C' &',' # To avoid this rule also parses .CRot etc 
 | '.CRot' 
 | '.CMfr' 
 | '.CMPN' 
 | '.CVal' 
 | '.CMnt' 
 | '.CFtp' 
 | '.CPgN' 
 | '.CPgD' 
 | '.CHgt' 
 | '.CLbN' 
 | '.CLbD' 
 | '.CSup' 
 | user_name 
 ;
```

![](_page_42_Picture_0.jpeg)

```
# Tokens, by regex 
#----------------- 
unsigned_integer = /[0-9]+/; 
positive_integer = /[0-9]*[1-9][0-9]*/; 
integer = /[+-]?[0-9]+/; 
unsigned_decimal = /((([0-9]+)(\.[0-9]*)?)|(\.[0-9]+))/; 
decimal = /[+-]?((([0-9]+)(\.[0-9]*)?)|(\.[0-9]+))/; 
aperture_identifier = /D[0]*[1-9][0-9]+/; 
name = /[._a-zA-Z$][._a-zA-Z0-9]*/; 
user_name = /[_a-zA-Z$][._a-zA-Z0-9]*/; # Cannot start with a dot 
string = /[^%*]*/; # All characters except * % 
field = /[^%*,]*/; # All characters except * % ,
```

![](_page_43_Picture_0.jpeg)

### <span id="page-43-0"></span>**3.6 File Extension, MIME Type and UTI**

The Gerber Format has a standard file name extension, a registered mime type and a UTI definition.

**Standard file extension:** .gbr or .GBR

**Mime type:** application/vnd.gerber

(see http://www.iana.org/assignments/media-types/application/vnd.gerber)

#### **Mac OS X UTI:**

```
<key>UTExportedTypeDeclarations</key>
<array>
 <dict>
 <key>UTTypeIdentifier</key>
 <string>com.ucamco.gerber.image</string>
 <key>UTTypeReferenceURL</key>
 <string>http://www.ucamco.com/gerber</string>
 <key>UTTypeDescription</key>
 <string>Gerber image</string>
 <key>UTTypeConformsTo</key>
 <array>
 <string>public.plain-text</string>
 <string>public.image</string>
 </array>
 <key>UTTypeTagSpecification</key>
 <dict>
 <key>public.filename-extension</key>
 <array>
 <string>gbr</string>
 </array>
 <key>public.mime-type</key>
 <string>application/vnd.gerber</string>
 </dict>
 </dict>
</array>
```

![](_page_44_Picture_0.jpeg)

