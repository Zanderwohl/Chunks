# com.zanderwohl.World
Classes for representing, simulating, and dealing with a world and its component pieces.

## Space
Functions for converting world coordinates to Volume coordinates and vice-versa. World coordinates are the tuples
representing a point in the world relative to that World's origin, and Volume coordinates are tuples that describe a
location relative to a smaller Volume within the World.

## Volume

A unit of blocks in three dimensions that has a height, width and depth. Is stored as a single file, and is given a
generator in order to generate and populate its own contents when called to do so.

## World

A collection of Volumes and their relations to each other, to allow for dynamic generation of the world in small pieces
at a time. Contains other information.