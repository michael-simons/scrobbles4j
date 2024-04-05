use scripting additions
use framework "Foundation"

on open location (newUrl)
	my handle_url(newUrl)
end open location

on handle_url(newUrl)
	set pos to the offset of "?" in the newUrl
	set AppleScript's text item delimiters to "&"
	set arguments to every text item of the text from (pos + 1) to -1 of the newUrl
	set artistToPlay to ""
	set trackToPlay to ""
	set queue to false
	set AppleScript's text item delimiters to "="
	repeat with i from 1 to the count of arguments
		set argument to text items of (item i of arguments)
		if (item 1 of argument is equal to "artist") then
			set artistToPlay to decodeText(item 2 of argument)
		end if
		if (item 1 of argument is equal to "name") then
			set trackToPlay to decodeText(item 2 of argument)
		end if
		if (item 1 of argument is equal to "queue") then
			set queue to true
		end if
	end repeat
	
	if (queue) then
		set uniqueIdentifier to current application's NSUUID's UUID()'s UUIDString as text
		set posixtmpfile to POSIX path of (path to temporary items folder) & uniqueIdentifier
		try
			set fhandle to open for access posixtmpfile with write permission
			set AppleScript's text item delimiters to linefeed
			write ({artistToPlay, trackToPlay} as text) to fhandle as Çclass utf8È
			close access fhandle
			do shell script ("more " & posixtmpfile & " | shortcuts run \"Queue track\"")
		on error
			try
				close access posixtmpfile
			end try
		end try
	else
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
	end if
end handle_url

on decodeText(encodedText)
	set encodedText to stringWithString_(encodedText) of NSString of current application
	set encodedText to stringByReplacingOccurrencesOfString_withString_("+", " ") of the encodedText
	return stringByRemovingPercentEncoding() of the encodedText as string
end decodeText
