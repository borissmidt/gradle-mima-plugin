#!/bin/bash
export SDKMAN_DIR="$HOME/.sdkman"
[[ -s "$HOME/.sdkman/bin/sdkman-init.sh" ]] && source "$HOME/.sdkman/bin/sdkman-init.sh"
versions=(
4.5
4.9
4.10
5.0
5.4
5.6.4
6.0
6.3
)

function runTest(){
  echo ""
  echo "####################################################"
  echo "testing version $1"
  sdk use gradle $1 < /dev/null > /dev/null 2> /dev/null && \
  gradle mimaReportBinaryIssues --stacktrace && \
  echo "version $1 succeeded"|| \
  echo "version $1 failed"
}

for i in "${versions[@]}"; do
    runTest $i
done
