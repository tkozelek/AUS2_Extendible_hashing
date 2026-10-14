# Extendible hashing on disk

Semester project for Algorithms and Data Structures 2 (FRI UNIZA, 2024). A small database of cars
and their service visits, stored in block files on disk. Cars are found through two extendible hash
indexes, one by car ID and one by licence plate. Written from scratch in Java, with a Swing GUI.

## How it works

- **Heap file**: records are serialised by hand to fixed-length byte arrays and stored in blocks of
  a chosen size. Empty and partly full blocks are kept in chains and reused, and empty blocks at the
  end of the file are trimmed.
- **Extendible hashing**: a directory that doubles when a full block cannot split any further, and
  blocks that merge back with their neighbour after deletes, shrinking the directory again. Local
  depths are kept in memory.
- **Indexes**: the heap file holds the cars, the two hash files point to the block of each car by ID
  and by licence plate, and a third hash file keeps the service visits. A car has at most 5 service
  visits, each with at most 10 lines of notes.
- Insert, find, edit and delete, a random data generator, a block-by-block view of every file, and
  the state of the files is saved, so the data survives a restart.
- Randomised JUnit 5 tests of the heap file and the hash file in `src/main/java/kozelek/test`.

## Run it

A plain IntelliJ IDEA project without a build file. Open it in IntelliJ IDEA and run `kozelek.Main`.
The windows are IntelliJ GUI Designer forms, so the IDE generates their layout code.
