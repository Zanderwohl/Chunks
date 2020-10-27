# com.zanderwohl.chunks.Console
The classes that deal with the external console, both sending and receiving Messages.

The console uses the SuperConsole library, which I wrote. This is a console meant to be slightly easier to use than a
normal userCommand-line console. It has a history you can scroll through.

Planned features include detail dialogues similar to how QLab handles editing details per-cue and the saving and
loading of past sessions.

In the final game, the console will not appear by default but on a keypress or setting, like the does ~ in Source.

## ConsoleConnector
 Console connector is the sole broker between the Message queues and the network.
 * It sends messages from the toConsole queue over the network.
 * It receives messages from the network into the fromConsole queue.

### Send
Private class that sends Messages over the network. Also sends a test message every ten seconds.

### Receive
Private class that receives Messages from the console and places them on the fromConsole queue.

## CommandManager
Consumes Messages from the fromConsole queue and notifies the appropriate objects of any relevant commands.

### processCommands()
The method to call when the CommandManager should consume all commands on the queue and take the relevant actions.

## Command
An object that contains a console userCommand to be sent between different objects. Takes in a single string in the
constructor, and internally will parse out the different pieces of the userCommand.

### Future
Will later on be able to take in a json string as final argument and parse out optional/unordered arguments that will be
internally keyed by string. Each userCommand type will have a lambda to pass in that will allow ordered arguments to also
be arranged with string keys.