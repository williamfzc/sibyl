set -e
basepath=$(cd `dirname $0`; pwd)

cd $basepath
cd ..
cd ..
echo `pwd`

rm -rf testRes
mkdir testRes
cd testRes
echo "clone test res from github ..."
git clone --depth 1 https://github.com/google/guava.git
git clone --depth 1 https://github.com/google/gson.git
git clone --depth 1 https://github.com/jacoco/jacoco.git
echo "prepare finished"
