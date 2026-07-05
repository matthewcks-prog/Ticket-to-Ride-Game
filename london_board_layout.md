The UI should have 
Background layer:
- Full London map artwork / image.

Route layer:
- Rounded rectangular route slots.
- Each route has colour: blue, orange, yellow, green, pink, black, or grey.
- Grey routes mean neutral/uncoloured routes.
- Claimed routes can be drawn by overlaying player-coloured bus pieces on top of the slots.

Location layer:
- Circular station markers.
- White outer ring.
- Coloured inner circle.
- Large district number in the centre.
- Text labels near the circles.

Interaction layer:
- Hover over route highlights all slots in that route.
- Click route selects it.
- Click location can show location name and connected routes.

ID	Display name	District no.	Circle colour	Approx position
REGENTS_PARK	Regent's Park	5	red	top-left
BAKER_STREET	Baker Street	5	red	left-upper
HYDE_PARK	Hyde Park	5	red	bottom-left
KINGS_CROSS	King's Cross	5	red	top-centre
BRITISH_MUSEUM	British Museum	1	black	upper-centre-left
COVENT_GARDEN	Covent Garden	1	black	centre
PICCADILLY_CIRCUS	Piccadilly Circus	2	green	left-centre
TRAFALGAR_SQUARE	Trafalgar Square	2	green	centre-lower
BUCKINGHAM_PALACE	Buckingham Palace	2	green	lower-left
BIG_BEN	Big Ben	2	green	lower-centre
WATERLOO	Waterloo	3	blue	lower-centre-right
GLOBE_THEATRE	Globe Theatre	3	blue	right-centre-lower
ELEPHANT_CASTLE	Elephant & Castle	3	blue	bottom-right
THE_CHARTERHOUSE	The Charterhouse	4	orange	upper-right-centre
ST_PAULS	St Paul's	4	orange	centre-right
BRICK_LANE	Brick Lane	4	orange	top-right
TOWER_OF_LONDON	Tower of London	4	orange	right-centre

Approximate normalized coordinates if using a scalable Swing panel:
// x and y are relative values from 0.0 to 1.0
REGENTS_PARK       = (0.22, 0.16)
BAKER_STREET       = (0.09, 0.30)
HYDE_PARK          = (0.13, 0.78)
KINGS_CROSS        = (0.44, 0.16)
BRITISH_MUSEUM     = (0.38, 0.36)
COVENT_GARDEN      = (0.43, 0.50)
PICCADILLY_CIRCUS  = (0.30, 0.55)
TRAFALGAR_SQUARE   = (0.38, 0.62)
BUCKINGHAM_PALACE  = (0.25, 0.77)
BIG_BEN            = (0.42, 0.73)
WATERLOO           = (0.52, 0.66)
GLOBE_THEATRE      = (0.66, 0.59)
ELEPHANT_CASTLE    = (0.65, 0.80)
THE_CHARTERHOUSE   = (0.65, 0.30)
ST_PAULS           = (0.66, 0.46)
BRICK_LANE         = (0.85, 0.32)
TOWER_OF_LONDON    = (0.86, 0.58)

These coordinates are approximate from a photo but if you want the UI to be pixel-perfect, use the image @london_board_layout.md as the background and fine-tune each coordinate by testing in Swing.

Each route should be represented as:
Route(from, to, colour, length, shape, laneOffset)
from/to      = location IDs
colour       = BLUE, ORANGE, YELLOW, GREEN, PINK, BLACK, GREY
length       = number of rectangular bus slots
shape        = STRAIGHT, CURVED, DIAGONAL, HORIZONTAL, VERTICAL
laneOffset   = 0 for normal, -1 / +1 for parallel routes

North-west and top-centre routes
From	To	Colour	Length	Visual shape
Regent's Park	Baker Street	blue	2	diagonal down-left
Regent's Park	King's Cross	green	3	horizontal
Regent's Park	British Museum	yellow	3	diagonal down-right
Baker Street	British Museum	orange	4	long slight diagonal
Baker Street	Hyde Park	black	4	outward curved route down left edge
Baker Street	Piccadilly Circus	grey	4	diagonal down-right
King's Cross	British Museum	black	2	diagonal/vertical down-left
King's Cross	The Charterhouse	pink	3	curved down-right

Central routes
From	To	Colour	Length	Visual shape
British Museum	The Charterhouse	blue	4	long curved horizontal
British Museum	Piccadilly Circus	grey	2	diagonal down-left
British Museum	Covent Garden	grey	2	diagonal/vertical down-right
Piccadilly Circus	Covent Garden	green	1	short horizontal parallel route
Piccadilly Circus	Covent Garden	yellow	1	short horizontal parallel route
Piccadilly Circus	Trafalgar Square	blue	1	short diagonal parallel route
Piccadilly Circus	Trafalgar Square	orange	1	short diagonal parallel route
Piccadilly Circus	Hyde Park	grey	2	curved/diagonal down-left, parallel
Piccadilly Circus	Hyde Park	grey	2	curved/diagonal down-left, parallel
Piccadilly Circus	Buckingham Palace	pink	3	vertical diagonal down
Covent Garden	Trafalgar Square	black	1	short vertical parallel route
Covent Garden	Trafalgar Square	pink	1	short vertical parallel route
Covent Garden	St Paul's	grey	4	long horizontal, upper lane
Covent Garden	St Paul's	grey	4	long horizontal, lower lane
Trafalgar Square	Big Ben	grey	2	diagonal down-left/down-centre
Trafalgar Square	Waterloo	grey	2	horizontal/slight diagonal right
Buckingham Palace	Big Ben	green	3	horizontal
Buckingham Palace	Trafalgar Square	grey	2	diagonal up-right
Hyde Park	Buckingham Palace	yellow	1	short parallel route
Hyde Park	Buckingham Palace	orange	1	short parallel route
Big Ben	Waterloo	blue	1	short diagonal
Big Ben	Elephant & Castle	yellow	4	long diagonal down-right

East and south-east routes
From	To	Colour	Length	Visual shape
The Charterhouse	Brick Lane	green	3	curved horizontal
The Charterhouse	St Paul's	black	1	short vertical
St Paul's	Brick Lane	orange	3	diagonal up-right
St Paul's	Tower of London	yellow	3	diagonal down-right, parallel
St Paul's	Tower of London	pink	3	diagonal down-right, parallel
Brick Lane	Tower of London	blue	3	vertical
Waterloo	Globe Theatre	pink	3	curved/horizontal right, bends upward
Waterloo	Elephant & Castle	orange	3	diagonal down-right
St Paul's	Globe Theatre	grey	1	short parallel route
St Paul's	Globe Theatre	grey	1	short parallel route
Globe Theatre	Elephant & Castle	green	3	vertical
Globe Theatre	Tower of London	grey	3	diagonal/curved right, bends downward
Elephant & Castle	Tower of London	black	4	large outward curved route on far right

Route slot style:
- Each bus slot is a rounded rectangle.
- White or off-white border around every slot.
- Small bus icon or faint symbol inside each slot.
- Slots should have small gaps between them.
- Route colours:
  - blue: deep London bus-route blue
  - orange: bright orange
  - yellow: pale yellow
  - green: dark green
  - pink: magenta/pink
  - black: charcoal black
  - grey: neutral grey

Station marker style:
- Circular marker.
- White outer ring.
- Coloured inner circle.
- Large white or black number centred.
- District 1: black circle with white "1".
- District 2: green circle with white "2".
- District 3: blue circle with white "3".
- District 4: orange/yellow circle with dark "4".
- District 5: red circle with white "5".

Text style:
- Use serif font.
- Dark red/brown labels for most landmarks.
- Black/dark labels for central landmarks such as British Museum, Covent Garden, Waterloo, Globe Theatre.
- Labels should be decorative rather than plain UI labels.

Board background:
- Vintage beige London map.
- River Thames shown in blue through the lower-centre/right.
- Score track around the border using ticket-shaped rectangles.
- Multiples of 5 are highlighted in red/orange.
- Small decorative objects: Sherlock Hotel key near Baker Street, coin/stamp near right edge, score card at top-right.

Draw each route as a sequence of rounded rectangles between the two node positions. Parallel routes should be drawn with a perpendicular offset so they sit side-by-side. For curved routes, allow optional control points or use a quadratic curve. The visual style should match the board: white outlines, coloured rounded slots, serif text labels, and circular numbered station markers.

Keep the rendering code data-driven: locations and routes should be defined in lists, not hard-coded directly in paintComponent.

Sprint 3 Ferry routes:

| Route | From | To | Colour | Length | Required Bus symbols | Notes |
|---|---|---|---|---:|---:|---|
| R28 | Big Ben | Waterloo | blue | 1 | 1 | Thames crossing |
| R39 | Globe Theatre | Tower of London | grey | 3 | 1 | Thames-side crossing route |
| R42 | St Paul's | Globe Theatre | grey | 1 | 1 | Double-route ferry, upper lane |
| R43 | St Paul's | Globe Theatre | grey | 1 | 1 | Double-route ferry, lower lane |

Ferry route slots should display a small generated Bus-symbol marker. This is rendering metadata
only; ferry payment validation belongs in the domain `RouteRequirement` strategy.
