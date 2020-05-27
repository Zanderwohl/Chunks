# com.zanderwohl.chunks.Console

The classes that deal with the external console, both sending and receiving Messages.

## ConsoleConnector

 Console connector is the sole broker between the Message queues and the network.
 * It sends messages from the toConsole queue over the network.
 * It receives messages from the network into the fromConsole queue.

### Send

Private class that sends Messages over the network. Also sends a test message every ten seconds.

### Receive

Private class that receives Messages from the console and places them on the fromConsole queue.