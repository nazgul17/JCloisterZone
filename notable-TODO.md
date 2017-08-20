
* test Mayor scores 0 points on castle

* change resource manager interface - simple load all areas and start
and get it by one one instead of all per tile
+ support area for whole feature? (FarmHintsLayer)

* follower selection & zoom

* SelectPrisoner panel - inactive mode when displayed on remote client

* grid panel - verify left, right, top, bottom fields

* verify clock toggle during bazaar auction

* toggle clock / clearUndo can be derived automatically from state change

* test if builders works properly in extra abbey round (-> means buiders are ignored)

* if tile can be placed only with bridge, player can choose to discard tile

* add ZMG Castle variant - allow any (non-semicircular) cities as casle base
* do not eat meeples by Dragon on castles, do not capture by Tower

* TODO: draw bridge preview on tile preview icon if bridge is mandatory

* TODO <susbtract> -> <subtract>

* save game -> use history of messages
    undo -> send how many messages should be stripped ?

* fix auction
* pass when only placement with bridge exists during auction

* there is still some bug probably related to bridges / or putting road piece inside

* do not challenge yaga hut with shrine - separate feature for yaga's hut? probably not
* + yaga scoring

* move stuff from Board to GameStateHelper
* Location.sides() -> SIDES
* add debug optiom to limit tile pack size

* add function for applying reducer on state (reverse apply)
* rename updateXYZ to mapXYX, add it for everything and move it from mixins to state

* remove GridPanel.getTile


* test if works if barn are contain multiple spots

* implement zoom by single affinetransrom on grid layer