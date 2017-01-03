Pantheist serves as a library of different kinds of programming resources, organized by schema.

It operates on basically three levels:

1. Instances. These would be your java objects, documents that your system manages, rows in your
database etc. Generally you don't see these while programming (except when debugging) - the actual code
that you write is at the next level.

2. Types. These might include java classes, java interfaces and json schemas. This is mostly what you're
looking at when you're writing code. Pantheist bundles related types together into what it calls "entities".

3. Kinds. These serve as a schema for types: they specify what they should look like and what they're used
for.



Setting up a new project:

- copy pantheist.conf
- point its dataDir at the common ancestor of your project directory and the pantheist directory
- set up srvDiv to point to pantheist/data/srv
- set up systemDir to something within your own project (it should be in .gitignore though)
- copy kinds from pantheist/data/system/kind to your own systemDir/kind
