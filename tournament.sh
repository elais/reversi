#!/bin/bash
set -x

baseline_strategies=$(find src/main/java/edu/uab/cis/reversi/strategy/baseline -name "*Strategy.java" | \
                   sed -e 's/\//./g' -e 's/src.main.java.//g' -e 's/.java//g' | \
                   tr '\n' ' ')
group_strategies=$(find src/main/java/edu/uab/cis/reversi/strategy/group2 -name "Group*Strategy.java" | \
                   sed -e 's/\//./g' -e 's/src.main.java.//g' -e 's/.java//g' | \
                   tr '\n' ' ')
mvn exec:java \
-Dexec.mainClass="edu.uab.cis.reversi.Reversi" \
-Dexec.args="--games 5 --timeout 1000 --strategies \
$baseline_strategies \
$group_strategies"
