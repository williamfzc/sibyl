set -e
basepath=$(cd `dirname $0`; pwd)

cd $basepath
ws=`pwd`
version=0.3.1
echo "current workspace: $ws"
echo "sibyl version: $version"

file_name=sibyl-cli-${version}-jar-with-dependencies.jar
file_path=${ws}/${file_name}
if [ ! -f "$file_path" ]; then
  echo "sibyl cli is not existed"
  wget https://github.com/williamfzc/sibyl/releases/download/v${version}/${file_name}
fi

# ready
output=${ws}/snapshot.json
java -version
java -jar "$file_path" snapshot -i . -o "$output" -t JAVA_8
echo "diff finished: ${output}"
