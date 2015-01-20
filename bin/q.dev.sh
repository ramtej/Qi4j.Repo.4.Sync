#!/bin/sh


set -e
[ -z $SUEZCANAL ] && echo "SUEZCANAL is not set" && exit 1

QI4J_GRADLE_HOME=${SUEZCANAL}/Catalogs/Libraries/Qi4j.Fork

usage() {
  echo "c-dev [-p group:project] command [flags]"
  echo
  echo "  -p Select the target project"
  echo "    project is the base name of the targeted submodule"
  echo
  echo "  Available commands"
  echo "    clean            Clean output"
  echo "    build            Build without test"
  echo "    test             Run tests"
  echo "    idea             Create Idea Projects"
  echo "    local            Install atrifacts to maven local"
  echo
  echo "  Available flags"
  echo "    skipTests        Skip tests"
  echo
  echo "  Examples"
  echo "    q.dev clean"
  echo "    q.dev build"
  echo "    q.dev local"
  echo
}

[ -z $1 ] && usage && exit 1

if [ -f "${QI4J_GRADLE_HOME}/gradlew" ]
then
    gradle="./gradlew --daemon"
    echo "Using gradle wrapper found in current directory!"
else
    gradle="gradle"
fi

target=""
if [ "${1}" = "-p" ]
then
  shift
  gradle="${gradle} -a"
  case "${1}" in
    # Special core groupId handling as it is not going plural
    core:*)
      target=`echo $1 | sed -e "s/core:\(.*\)/:org.qi4j.core:org.qi4j.core.\1:/"`;;
    # Special libraries plural groupId handling ...
    library:*)
      target=`echo $1 | sed -e "s/library:\(.*\)/:org.qi4j.libraries:org.qi4j.library.\1:/"`;;
    # Special extensions plural groupId handling ...
    extension:*)
      target=`echo $1 | sed -e "s/extension:\(.*\)/:org.qi4j.extensions:org.qi4j.extension.\1:/"`;;
    *:*)
      target=`echo $1 | sed -e "s/\(.*\):\(.*\)/:org.qi4j.\1s:org.qi4j.\1.\2:/"`;;
    *)
      target="${1}:";;
  esac
  shift
fi

command=$1

shift
while [ "$*" != "" ]
do
  case $1 in
    skipTests)
      gradle="${gradle} -x test";;
    *)
      echo "Unknown flag ${1}"
      echo
      usage
      exit 1;;
  esac
  shift
done

skip_global="-x signArchives"
skip_full="-x javadoc -x javadocJar -x sourceJar"
skip_dist="-x :javadocs -x :tarBinaries -x :tarSources -x :zipBinaries -x :zipSources"
case $command in

  local)
   gradle="${gradle} ${target}-Dversion=2.0.0.1.SNAPSHOT install -x signArchives -x javadoc -x javadocs -x test -x org.qi4j.manual:website"
    ;;
  clean)
    gradle="${gradle} ${target}clean"
    ;;
  build)
    gradle="${gradle} ${target}build -x test"
    ;;    
  test)
    gradle="${gradle} ${target}test"
    ;;
   idea)
    gradle="${gradle} ${target}idea"
    ;;   
  *)
    echo "Unknown command ${command}"
    echo
    usage
    exit 1;;
esac

# here add another generic params
gradle="${gradle}" 

echo "Will run the following gradle command:"
echo
echo "    ${gradle}"
echo

# Execute gradle
(
cd $QI4J_GRADLE_HOME \
&&
$gradle
)