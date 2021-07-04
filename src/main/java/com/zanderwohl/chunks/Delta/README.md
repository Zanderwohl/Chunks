# Delta

The delta object represents some kind of change that the server sends the client or the client sends the
server. The client "trusts" the server to give accurate information in deltas, but the server does not "trust"
the client and verifies that deltas are reasonable.

UID: 32112000000L

## Chat

A chat message. When a client wants to chat other players, this is sent to the server then the server
broadcasts it to other players appropriately. When received from a server, the message is displayed to
the player.

UID: 32112000001L

## Disconnect

A message from the client to the server that initiates the "clean" and graceful disconnect process.

UID: 32112000002L

## Kick

A notice from the server that a client is being disconnected. When sent to a server, this is ignored
as nonsense.

UID: 32112000003L

## PPos

A player position and character pose information. Sent both ways to inform changes in character positioning
for both player characters and non-player characters.

UID: 32112000004L

## ServerClose

A broadcast from the server that the server is closing. Usually includes a reason. Sent to the server,
it is ignored as nonsense.

UID: 32112000005L

## StartingVolumesRequest

An initial request by the player to send the starting volumes near the player. Sent to the player, it is
ignored as nonsense.

UID: 32112000006L

## VolumeAge

This is kind of complicated so I'm not explaining it rn.

UID: 32112000007L

## VolumeRequest

A request from a player to send a current version of a volume. Sent from the server, the client interprets
this as nonsense.

UID: 32112000008L

## WorldRequest

A request from a player to send metadata about a world. Sent from the server, the client interprets this
as nonsense.

UID: 32112000009L

## ClientIdentity

Not a delta.

UID: 32112000010L