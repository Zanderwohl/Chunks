# com.zanderwohl.chunks.World
Classes for representing, simulating, and dealing with a world and its component pieces.

### World coordinates vs. Volume coordinates

A World coordinate is the ordered triple (x, y, z) that describes the location of a block within the world, relative to
the world's origin.

A Volume coordinate is the ordered triple (x, y, z) that describes the location of a Volume at the scale of Volumes.
For example, the volume starting at the origin has the coordinates (0, 0, 0) and its south-west-bottom corner is at the
World coordinate (0, 0, 0). The volume just above it has the Volume coordinates (0, 1, 0) but the World coordinates of
its south-west-bottom corner are (0, 128, 0), given that a volume height is 128 blocks.

## Space
Functions for converting world coordinates to Volume coordinates and vice-versa. World coordinates are the tuples
representing a point in the world relative to that World's origin, and Volume coordinates are tuples that describe a
location relative to a smaller Volume within the World.

## Coord
A data structure that contains an x, y, and z integer, with useful methods. Immutable.

## Volume

A unit of blocks in three dimensions that has a height, width and depth. Is stored as a single file, and is given a
generator in order to generate and populate its own contents when called to do so.

## World

A collection of Volumes and their relations to each other, to allow for dynamic generation of the world in small pieces
at a time. Contains other information.

## WorldManager
The object that controls all world objects and keeps track of them.