use scripting additions
use framework "Foundation"

on open location (newUrl)
	my play_track(newUrl)
end open location

on play_track(newUrl)
	set pos to the offset of "?" in the newUrl
	set AppleScript's text item delimiters to "&"
	set arguments to every text item of the text from (pos + 1) to -1 of the newUrl
	set artistToPlay to ""
	set trackToPlay to ""
	set AppleScript's text item delimiters to "="
	repeat with i from 1 to the count of arguments
		set argument to text items of (item i of arguments)
		if (item 1 of argument is equal to "artist") then
			set artistToPlay to decodeText(item 2 of argument)
		end if
		if (item 1 of argument is equal to "name") then
			set trackToPlay to decodeText(item 2 of argument)
		end if
	end repeat
	
	tell application "Music"
		set selectedTracks to every track of library playlist 1 whose artist is artistToPlay and name is trackToPlay
		set playedTheMost to null
		repeat with selectedTrack in selectedTracks
			if (playedTheMost is null or played count of the selectedTrack > played count of playedTheMost) then
				set playedTheMost to selectedTrack
			end if
		end repeat
		if (playedTheMost is not null) then
			play playedTheMost with once
		end if
	end tell
end play_tracks

on decodeText(encodedText)
	set encodedText to stringWithString_(encodedText) of NSString of current application
	set encodedText to stringByReplacingOccurrencesOfString_withString_("+", " ") of the encodedText
	return stringByRemovingPercentEncoding() of the encodedText as string
end decodeText
