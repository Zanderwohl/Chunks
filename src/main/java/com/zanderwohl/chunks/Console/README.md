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

### addCommand(Command)
Adds the command to the Command Manager. It will automatically add the command to the help text in alphabetical order.

### processCommands()
The method to call when the CommandManager should consume all commands on the queue and take the relevant actions.

### doCommands()
The method called to execute all commands.

## UserCommand
An object that contains a console userCommand to be sent between different objects. Takes in a single string in the
constructor, and internally will parse out the different pieces of the userCommand. Handles both required and optional
commands. Also automatically formats help text.

### Future
Will later on be able to take in a json string as final argument and parse out optional/unordered arguments that will be
internally keyed by string. Each userCommand type will have a lambda to pass in that will allow ordered arguments to
also be arranged with string keys.

## Command
How a command is added to the engine. Each one represents an action that can be taken.

### Building a Command
A command is a class that implements BiConsumer<HashMap<String, String>, ConcurrentLinkedQueue<Message>>.
The BiConsumer is a higher-class function that can be passed as an argument and is called as scheduled by the Command
Manager.

#### The Two Arguments
Because a Command must implement the method accept(arguments, toConsole), all the action should take place
inside that function, or be called from that function.

##### TODO: MORE ABOUT THAT.

## Default Commands
Constructs the normal commands for the engine, and adds them to the Command Manager.

