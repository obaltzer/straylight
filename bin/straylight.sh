#!/bin/sh

# $Id: straylight.sh,v 1.4 2002/02/10 18:52:24 racon Exp $


if test -z "$STRAYLIGHT_HOME" ; then
    if test -z "`echo $0 |grep \"^\/\"`" ; then
        STRAYLIGHT_HOME=`echo "\`pwd\`/$0" | sed -e "s/\/[^\/]*$/\/../g"`
    else
        STRAYLIGHT_HOME=`echo $0 | sed -e "s/\/[^\/]*$/\/../g"`
    fi
    echo "Guessing STRAYLIGHT_HOME to ${STRAYLIGHT_HOME}"
fi

# The Jakarta Group style - better for sym-links
# 
# if STRAYLIGHT_HOME is not defined
#if [ "$STRAYLIGHT_HOME" = "" ] ; then
# 
#    # the complete called path of this program
#    PRG=$0
#
#    # this script name
#    progname=`basename $0`
#
#    while [ -h "$PRG" ] ; do
#        ls=`ls -ld "$PRG"`
#        link=`expr "$ls" : '.*-> \(.*\)$'`
#        
#        if expr "$link" : '.*/.*' > /dev/null; then
#            PRG="$link"
#        else
#            PRG="`dirname $PRG`/$link"
#        fi
#    done
#
#    STRAYLIGHT_HOME_1=`dirname "$PRG"`/..
#    echo "Guessing STRAYLIGHT_HOME to ${STRAYLIGHT_HOME_1}"
#        
#    if [ -d ${STRAYLIGHT_HOME_1}/bin ] ; then
#        STRAYLIGHT_HOME=${STRAYLIGHT_HOME_1}
#        echo "Setting STRAYLIGHT_HOME to $STRAYLIGHT_HOME"
#    fi
#fi

if [ "$STRAYLIGHT_HOME" = "" ] ; then
    echo "STRAYLIGHT_HOME not set, please set the environment variable "
    echo "STRAYLIGHT_HOME to the base directory of the Straylight "
    echo "distribution."
    exit 1
fi

if [ -z "$JAVA_HOME" ] ;  then
    JAVA=`which java`
    if [ -z "$JAVA" ] ; then
        echo "Cannot find JAVA. Please set your PATH."
        exit 1
    fi
    JAVA_BINDIR=`dirname $JAVA`
    JAVA_HOME=$JAVA_BINDIR/..
fi

JAVACMD="$JAVA_HOME/bin/java"

oldCP=$CLASSPATH

CLASSPATH=.

for i in ${STRAYLIGHT_HOME}/lib/* ; do
    CLASSPATH=${CLASSPATH}:$i
done

if [ -f ${JAVA_HOME}/lib/tools.jar ] ; then
     CLASSPATH=${CLASSPATH}:${JAVA_HOME}/lib/tools.jar
fi

if [ "$oldCP" != "" ]; then
    CLASSPATH=${CLASSPATH}:${oldCP}
fi

export CLASSPATH

case "$1" in 
    build)
        shift
        echo Using classpath: ${CLASSPATH}

        $JAVACMD -Dstraylight.home=${STRAYLIGHT_HOME} \
                 -Dcp=${CLASSPATH} \
                 org.apache.tools.ant.Main \
                 -f ${STRAYLIGHT_HOME}/build.xml "$@"
        ;;
        
    ant)
        shift
        echo Using classpath: ${CLASSPATH}
        $JAVACMD -Dstraylight.home=${STRAYLIGHT_HOME} \
                 -Dcp=${CLASSPATH} \
                 org.apache.tools.ant.Main "$@"
        ;;
    
    *)
        echo "Usage:"
        echo "${0} (command)"
        echo "      build  - runs Ant with the build file build.xml " \
             "from ${STRAYLIGHT_HOME}"
        echo "      ant    - runs Ant in the current directory"
        exit 0
        ;;
esac

if [ "$oldCP" != "" ]; then
    CLASSPATH=${oldCP}
    export CLASSPATH
else
    unset CLASSPATH
fi
