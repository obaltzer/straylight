#!/bin/sh
#
# $Id: mkhtmlcode.sh,v 1.2 2002/02/10 18:52:24 racon Exp $
#
# converts the source code of all available modules to HTML using the
# java2html program. The HTML pages will be stored in
# MODULENAME/doc/HTMLizedCode.
#
# Copyright (C) 2001,2002 Oliver Baltzer <ob@racon.net>

MODULES="straylight-framework straylight-ssb straylight-fs"

JAVA2HTML="/usr/bin/java2html"

OTHER_PATHES="/opt/bin /usr/local/bin /usr/bin /bin"

if test -z "`echo $0 |grep \"^\/\"`" ; then
    BASE=`echo "\`pwd\`/$0" | sed -e "s/\/[^\/]*$/\/../g"`
else
    BASE=`echo $0 | sed -e "s/\/[^\/]*$/\/../g"`
fi

case "$1" in
    clean)
        for I in $MODULES ; do
            DIR="$BASE/$I/doc/HTMLizedCode"
            if test -d $DIR ; then
                rm -rf $DIR
            fi
        done
        ;;

    create)
        
        if test ! -f "$JAVA2HTML" -o ! -x "$JAVA2HTML" ; then
            JAVA2HTML=`which java2html`
            if test ! -f "$JAVA2HTML" -o ! -x "$JAVA2HTML" ; then
                JAVA2HTML=""
                for I in $OTHER_PATHES ; do
                    if test -f "$I/java2html" -a -x "$I/java2html" ; then
                        JAVA2HTML=$I/java2html
                    fi
                done
                if test -z "$JAVA2HTML" ; then
                    echo "java2html could not be found...exiting"
                    exit 1
                fi
            fi
        fi

        echo "Using java2html...$JAVA2HTML";
        
        for I in $MODULES ; do
            DIR="$BASE/$I"
            if test -d "$DIR" -a -x "$DIR" -a -r "$DIR" \
                    -a -w "$DIR" ; then
                
                echo "Creating HTMLized source code for module $I..."
                
                if test ! -d "$DIR/doc" ; then
                    mkdir $DIR/doc
                fi
                if test ! -d "$DIR/doc/HTMLizedCode" ; then
                    mkdir $DIR/doc/HTMLizedCode
                fi
                 
                for J in `find $DIR/src -name \*.java` ; do
                    K=`echo $J | sed -e 's/.*src\/org\/pr0\///'` 
                    M=`echo $K | sed -e 's/'\`basename $J\`'//'`  
                    
                    if test -z "`echo $DIR | grep \"^\/\"`" ; then
                        DOCDIR="."
                    else
                        DOCDIR=""
                    fi
                    
                    for N in `echo $DIR/doc/HTMLizedCode/$M | \
                              sed -e 's/\// /g' | sed -e 's/^ //'` ; do
                        
                        if test ! -d $DOCDIR/$N ; then
                            mkdir $DOCDIR/$N
                        fi
                        
                        DOCDIR=$DOCDIR/$N
                    
                    done
                    
                    $JAVA2HTML --output-dir=$DIR/doc/HTMLizedCode/$M $J \
                        > /dev/null 2>&1
                done
            fi
        done
        ;;
    *)
        echo "Usage: mkhtmlcode.sh (create|clean)"
        exit 1
        ;;
esac

exit 0
