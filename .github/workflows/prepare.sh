set -e
basepath=$(cd `dirname $0`; pwd)
echo $basepath

cd ..
cd ..

rm -rf testRes
mkdir testRes
cd testRes
git clone --depth 1 https://github.com/jacoco/jacoco.git