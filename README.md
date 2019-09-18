# AskReddit-video
Tool that creates text to speech videos reading AskReddit threads.
[Here](https://youtu.be/SPimTa8fwl4) is an example of a video created using this program.

# Features
* Creates an MP4 video using a reddit webpage as input
	* Intended for use with r/AskReddit threads, but can work with pages from any subreddit
* Video contains AWS text-to-speech voices.
	* Voice reads question and each response.
	* Alternates between several AWS voices.
* Can include user-made intros, outros, music, and transitions.
	* Must meet requirements listed under "Premade Audio and Video Files" below.
* Response and question data is "plugged into" xhtml templates before being rendered.
	* Default templates for both questions and responses are included.
	* User can edit default templates or create custom templates.
	* User created templates must meet requirements listed in "Custom Templates".

# Build and Run
To build and run, first navigate to the src directory.  
Build:  
`javac -cp ../lib/*;. reddit/*.java` 
Run:  
`java -cp ../lib/*;. reddit/Post [options]` 

# Command Line arguments
```
argument                    description
--page, -p                  Required option. The page to make the video from. Note that the webpage must be saved on the user's computer.
--output, -o                Required option. The file name and path of the resulting video.
--responses, -r             The number of responses to include in the video. If not set, will continue until end of page or "Load more responses" is encountered.
--music, -m                 An audio file which will be played in the background of the video. Must meet audio requirements listed below.
--question-music, -qm       "true" or "false". Whether the music should play over the question, as opposed to starting at the first response. Defaults to true.
--response-template, -rt    The video's response template (see below). Defaults to "../templates/responsetemplate.xhtml".
--question-template, -qt    The video's question template (see below). Defaults to "../templates/questiontemplate.xhtml".
--transition, -t            A video file to play between each response. Must meet video requirements listed below.
--outro, -ot                A video file to play after all the responses have finished. Must meet video requirements listed below.
--intro, -it                A video file to play before the question. Must meet video requirements listed below.

```

# Dependencies
### Java Dependencies
The following are dependencies of this program: 
* [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/)
* [Jsoup](https://jsoup.org/)
* [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer)
* [Humble-Video](https://github.com/artclarke/humble-video)
All required JAR files are included in the lib/ directory. No additional assembly is required.

### FFmpeg
This program uses command line FFmpeg extensively.  
FFmpeg must be installed on your computer and added to the PATH for the program to work correctly.  
Download FFmpeg [here](https://ffmpeg.org/download.html).  

### AWS
This program uses AWS Polly to record text to speech.  
AWS Polly *technically* costs money, **but** you get 5 million characters free each month.  
Even if you make hour-long videos daily, it is unlikely you will hit 1 million characters in a month.  
So it is essentially free.  

AWS Command Line Interface must be installed on your computer and added to the PATH for the program to work correctly.  
You must also have an AWS account with valid credentials, and these credentials must be entered into the CLI's config.  
For more information on setting up the AWS CLI, see [here](https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-welcome.html).  

# Premade Audio and Video Files
The user can include premade audio and video files as intros, outros, transitions, and music.  
These premade files **MUST** meet the following requirements.  

### Audio files
Audio files must have AAC as their codec, have a sample rate of 48000 Hz, and be mono.  
To convert any audio file to these specs, run the following command:  
`ffmpeg -i ORIGINAL_FILE -acodec aac -ar 48000 -ac 1 OUTPUT_FILE `  

### Video files
Audio streams must meet the audio requirements listed above.  
Video streams must have H264 as their codec, yuv420p as their pixel format, and 100 as their timebase.  
To convert any video file to these specs, run the following command:  
`ffmpeg -i ORIGINAL_FILE -acodec aac -ar 48000 -ac 1 -vcodec h264 -pix_fmt yuv420p -enc_time_base 1:100 OUTPUT_FILE`  

# Custom Templates
This program renders video using XHTML templates.  
These templates contain HTML id tags which tell the program where to place relevant data.  
For example, the element with id "userreplace" is where the poster's reddit username goes. All text in that element will be replace with the reddit username.  
If the user wishes to edit the default templates, they can make changes as long as the id tags are not changed. Backing up the original templates is recommended.  
The templates must be in XHTML. If one wishes to *rougly* convert HTML to XHTML, [JTidy](http://jtidy.sourceforge.net/) is useful.  
If the user wishes to make a custom template, they must place the id tags where the relevant information should be put.  
The id tags are as follows. All are required for both question and response templates unless otherwise noted.  
```
id tag               data to be entered
userreplace          The poster's reddit username.
responsereplace      The comment or question.
scorereplace         The comment or question's score.
padreplace           Optional tag. "Pads" the question or comment with invisible text so that the HTML elements don't change size as the text scrolls.
commentreplace       Required for question templates only. The number of comments on the post.
marginreplace        Optional tag. Where to shift the page to vertically center it. Not recommended for custom templates.
```

# AWS Voices
From the command line, there is currently no way to customize which AWS voices read the text.  
However, it can be done relatively easily by editing the source code.  
If one scrolls to the bottom of Readable.java, an enum called "Voice" is declared.  
The values of this enum are the names of the AWS voices the program will use.  
Simply edit the values to customize which voices read the text. Make sure to build again after changing anything.  

# Known issues
It is not recommended to alter the structure of the filesystem. Doing so, especially to the debug and essential folders, may cause unexpected problems.  
This program is untested on Mac OS and Linux.
