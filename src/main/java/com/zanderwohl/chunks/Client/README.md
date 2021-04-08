# com.zanderwohl.chunks.Client
These classes concern the player client, including talking to the server and modelling server state  locally.
Authentication/session stuff goes here too.

## Client
The class that communicates over the network with the server (the SimLoop Class) to synchronize the game state.

##ClientIdentity
Client identity includes username. In the future will include authentication information, skin, and character model.