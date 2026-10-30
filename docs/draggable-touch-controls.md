# Draggable touch control groups

Open **Edit controls** and drag either outlined group. Collapse the panel to
reach controls behind it. **Done** or Back returns to the game.

Groups stay within the viewport. Positions are stored per controller layout and
orientation after a completed gesture; cancelled gestures restore the saved
position. Editing does not send button presses to the game. Reset restores the
original positions.

This builds on the draggable-group proposal in
[#1183](https://github.com/Swordfish90/Lemuroid/pull/1183) by nkhanhtrn, keeping its
control editor separate from the 3DS and foldable screen changes.
