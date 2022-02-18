set -e
basepath=$(cd `dirname $0`; pwd)

cd $basepath
ws=`pwd`
version=0.3.0
echo "current workspace: $ws"
echo "sibyl version: $version"

file_name=sibyl-cli-${version}-jar-with-dependencies.jar
file_path=${ws}/${file_name}
if [ ! -f "$file_path" ]; then
  echo "sibyl cli is not existed"
  wget https://github.com/williamfzc/sibyl/releases/download/v${version}/${file_name}
fi

# ready
before=`git rev-parse HEAD~`
after=`git rev-parse HEAD`
echo "before: ${before}"
echo "after: ${after}"

output=${ws}/diff.json
java -version
java -jar "$file_path" diff -i . -o "$output" --before "$before" --after "$after" -t JAVA_8
echo "diff finished: ${output}"
