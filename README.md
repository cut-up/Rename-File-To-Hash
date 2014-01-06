**Synopsis:**

`java -jar RenameFileToHash.jar [-a] [FILE]`

`java -jar RenameFileToHash.jar [OPTION]... [DIRECTORY]`

**Options:**

`-a` algorithm name (SHA-256, SHA-512, etc.). MD5 by default.

`-n` numbering.

**Examples:**

`java -jar RenameFileToHash.jar -a SHA-1 /mnt/flash/random.png`

`java -jar RenameFileToHash.jar -a SHA-1 -n /mnt/flash`