# com.zanderwohl.chunks
The root directory for the Chunks program.

## Main
The main class. Main.main(String[] args) will start the program with the SuperConsole.
What a mess of a file. It will be rewritten as the program takes shape.

## List of Shared Data Structures

These data structures are shared by multiple threads, and may cause issues if accidentally shared incorrectly. Thus,
they are all listed here and include lists of what classes read and write from them.

### toConsole

ConcurrentLinkedList&lt;Messages&gt; of Messages meant to be sent to the console to be displayed to the user.

#### Read:

Console

#### Write:

CommandManager

### fromConsole

ConcurrentLinkedList&lt;Messages&gt; of Messages typed in by the user to be consumed by the game logic.

#### Read:

CommandManager

#### Write:

Console

## Included Libraries

### OpenSimplex

OpenSimplex Noise in Java by Kurt Spencer.

Found at KdotJPG's [GitHub Repository](https://github.com/KdotJPG/OpenSimplex2).

### SuperConsole

A console tool written by me.

### org.json

The reference Java Json implementation. License and information available in the org.json package README.