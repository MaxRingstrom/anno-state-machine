#!/bin/bash
find . -name "*java" | grep jayway | grep -v "build/" | xargs -I 'FILE' echo 'cat copyright | cat - FILE > temp; mv temp FILE' > instr
