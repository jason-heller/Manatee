Info on how to make UIs:

############################################################################################################
#
#	1. The files
#
############################################################################################################


under the 'ui' directory are two files: "hud.html" and "hud.css". These are the
files that define the structure of the HUD. Note that the HTML tags are 
not entirely the same as in HTML. They are structured to fit JavaFX.

 * "hud.html" is the main file for building a UI, containing the HTML that structures
	the UI. It can additionally contain CSS within <style> tags, much like true HTML, but
	this must be placed ABOVE all other tags.

 * "hud.css" contains the styles for the UI. This is loaded before "hud.html" and can only
	contain CSS.

Additional files required for the UI should be placed in the same directory.


############################################################################################################
#
#	2. Format
#
############################################################################################################


The format is similar to HTML + CSS. However, the tags are changed to fit the components 
within JavaFX. Not all tags / controls found in JavaFX/HTML may be implemented. This is
something sitting on the backburner :(

There may be some bugs with whitespaces, these need to be ironed out. The standard style
of <button text="some text"> should suffice.

The first nestable tag will become the root node for all other components. Any components 
placed outside this node will not appear in-game.


############################################################################################################
#
#	3. Reserved IDs
#
############################################################################################################


Some tag IDs are used by the engine to identify key components of the UI. These tags 
must appear somewhere in the UI for the related component to function properly.

'chat-input' : The input field for the chatbox
'chat-display' : The text area of the chatbox


'worldspace-renderer' : The main worldspace renderer. Set this as the rendering callback of the OpenGLPane


############################################################################################################
#
#	4. Testing
#
############################################################################################################


Enable developer tools in-game and enter the command 'reload_hud' to reload
the hud.html and hud.css files to see changed. I recommend making backups
due to the unfinished nature of this feature.


