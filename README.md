# AskReddit-video
Tool that creates text to speech videos reading AskReddit threads.  
This tool uses AWS polly and the AWS Command Line Interface for text to speech. To use it, you must have an AWS account.

# Features

# Command Line arguments
```
argument                    description
--page, -p                  Required option. The page to make the video from.
--output, -o                Required option. The file name and path of the resulting video.
--responses, -r             The number of responses to include in the video. If not set, will continue until end of page or "Load more responses" is encountered.
--music, -m                 An audio file which will be played in the background of the video. Must meet audio requirements listed below.
--question-music, -qm       "true" or "false". Whether the music should play over the question, as opposed to starting at the first response. Defaults to true.
--response-template, -rt    The video's response template (see below). Defaults to "../templates/responsetemplate.xhtml".
--question-template, -qt    The video's question template (see below). Defaults to "../templates/questiontemplate.xhtml".
--transition, -t            A video file to play between each response. Must meet video requirements listed below.
--outro, -ot                A video file to play after all the responses have finished. Must meet video requirements listed below.
--intro, -it                A video file to play before the question. Must meet video requirements listed below.
--framerate, -f             Video framrate. Defaults to 25. Values that evenly divide 1000 are recommended.

```

# Dependencies

# Premade Music, Transitions, and Outros

# Known issues
