set -e
basepath=$(cd `dirname $0`; pwd)

cd $basepath
cd ..
echo `pwd`

rm -rf testRes
mkdir testRes
cd testRes
echo "clone test res from github ..."

# --- java ---
# 3000+ files
# too large for github hosted runner?
#git clone --depth 1 https://github.com/google/guava.git
# 200+
git clone --depth 1 https://github.com/google/gson.git
# 600+
git clone --depth 1 https://github.com/jacoco/jacoco.git
echo "prepare finished"
