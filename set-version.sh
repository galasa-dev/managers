#! /usr/bin/env bash 

#-----------------------------------------------------------------------------------------                   
#
# Objectives: Sets the version number, and/or re-build the release.yaml
#
# Environment variable over-rides:
# None
# 
#-----------------------------------------------------------------------------------------                   

# Where is this script executing from ?
BASEDIR=$(dirname "$0");pushd $BASEDIR 2>&1 >> /dev/null ;BASEDIR=$(pwd);popd 2>&1 >> /dev/null
# echo "Running from directory ${BASEDIR}"
export ORIGINAL_DIR=$(pwd)
# cd "${BASEDIR}"

cd "${BASEDIR}/.."
WORKSPACE_DIR=$(pwd)


#-----------------------------------------------------------------------------------------                   
#
# Set Colors
#
#-----------------------------------------------------------------------------------------                   
bold=$(tput bold)
underline=$(tput sgr 0 1)
reset=$(tput sgr0)
red=$(tput setaf 1)
green=$(tput setaf 76)
white=$(tput setaf 7)
tan=$(tput setaf 202)
blue=$(tput setaf 25)

#-----------------------------------------------------------------------------------------                   
#
# Headers and Logging
#
#-----------------------------------------------------------------------------------------                   
underline() { printf "${underline}${bold}%s${reset}\n" "$@"
}
h1() { printf "\n${underline}${bold}${blue}%s${reset}\n" "$@"
}
h2() { printf "\n${underline}${bold}${white}%s${reset}\n" "$@"
}
debug() { printf "${white}%s${reset}\n" "$@"
}
info() { printf "${white}➜ %s${reset}\n" "$@"
}
success() { printf "${green}✔ %s${reset}\n" "$@"
}
error() { printf "${red}✖ %s${reset}\n" "$@"
}
warn() { printf "${tan}➜ %s${reset}\n" "$@"
}
bold() { printf "${bold}%s${reset}\n" "$@"
}
note() { printf "\n${underline}${bold}${blue}Note:${reset} ${blue}%s${reset}\n" "$@"
}

#-----------------------------------------------------------------------------------------                   
# Functions
#-----------------------------------------------------------------------------------------                   
function usage {
    h1 "Syntax"
    cat << EOF
set-version.sh [OPTIONS]
Options are:
-v | --version xxx : Mandatory. Set the version number to something explicitly. 
    Re-builds the release.yaml based on the contents of sub-projects.
    For example '--version 0.29.0'
EOF
}

#-----------------------------------------------------------------------------------------                   
# Process parameters
#-----------------------------------------------------------------------------------------                   
overall_version=""

while [ "$1" != "" ]; do
    case $1 in
        -v | --version )        export overall_version=$1
                                shift
                                ;;
        -h | --help )           usage
                                exit
                                ;;
        * )                     error "Unexpected argument $1"
                                usage
                                exit 1
    esac
    shift
done

if [[ -z $overall_version ]]; then 
    error "Missing mandatory '--version' argument."
    usage
    exit 1
fi

function build_release_yaml {
    target_file=$1


    cd $BASEDIR

    manager_folders_list="$target_file-temp-managers-folders.txt"
    find . | grep "build.gradle" | sed "s/\/build.gradle//g" | sort > $manager_folders_list

    cat << EOF > $target_file
#
# Copyright contributors to the Galasa project 
#

# -----------------------------------------------------------
#
#                         WARNING
#
# This file is periodically re-generated from the contents of 
# the repository, so don't make changes here manually please.
# -----------------------------------------------------------


apiVersion: galasa.dev/v1alpha
kind: Release
metadata:
  name: galasa-release
    
managers:
  bundles:

#
# Manager 
#  
EOF


    # Results in a file containing things like this:
    # ./galasa-managers-parent/galasa-managers-cicsts-parent/dev.galasa.cicsts.ceci.manager.ivt
    # ./galasa-managers-parent/galasa-managers-cicsts-parent/dev.galasa.cicsts.manager
    # ./galasa-managers-parent/galasa-managers-cicsts-parent/dev.galasa.cicsts.ceci.manager

    while IFS= read -r manager_folder
    do 
        info "Processing folder $manager_folder..."
        manager_version=$(cat $manager_folder/build.gradle | grep "version =" | cut -f2 -d"'")

        manager_name=$(cat $manager_folder/settings.gradle | grep "rootProject.name =" | cut -f2 -d"'")


        if [[ "${manager_name}" != "" ]]; then 

            # The settings.gradle file also contains something like this:
            # include_in_obr = true
            # include_in_javadoc = true
            # include_in_bom = true
            # include_in_mvp = true
            # include_in_isolated = true
            # include_in_code_coverage =  true
            # ... which we need to read-in.
            include_in_obr=$(cat $manager_folder/settings.gradle | grep -v "\/\/" | grep "include_in_obr" | sed "s/=/ /g" | xargs | cut -f2 -d' ')
            include_in_javadoc=$(cat $manager_folder/settings.gradle | grep -v "\/\/" | grep "include_in_javadoc" | sed "s/=/ /g" | xargs | cut -f2 -d' ')
            include_in_bom=$(cat $manager_folder/settings.gradle | grep -v "\/\/" | grep "include_in_bom" | sed "s/=/ /g" | xargs | cut -f2 -d' ')
            include_in_mvp=$(cat $manager_folder/settings.gradle | grep -v "\/\/" | grep "include_in_mvp" | sed "s/=/ /g" | xargs | cut -f2 -d' ')
            include_in_isolated=$(cat $manager_folder/settings.gradle | grep -v "\/\/" | grep "include_in_isolated" | sed "s/=/ /g" | xargs | cut -f2 -d' ')
            include_in_code_coverage=$(cat $manager_folder/settings.gradle | grep -v "\/\/" | grep "include_in_code_coverage" | sed "s/=/ /g" | xargs | cut -f2 -d' ')

            info "Manager '$manager_name' is at version '$manager_version'"

            echo "" >> $target_file
            echo "  - artifact: $manager_name" >> $target_file
            echo "    version: $manager_version" >> $target_file
            
            if [[ "${include_in_obr}" != "" ]]; then 
                echo "    obr: $include_in_obr" >> $target_file
            fi

            if [[ "${include_in_javadoc}" != "" ]]; then 
                echo "    javadoc: $include_in_javadoc" >> $target_file
            fi

            if [[ "${include_in_bom}" != "" ]]; then 
                echo "    bom: $include_in_bom" >> $target_file
            fi

            if [[ "${include_in_mvp}" != "" ]]; then 
                echo "    mvp: $include_in_mvp" >> $target_file
            fi

            if [[ "${include_in_isolated}" != "" ]]; then 
                echo "    isolated: $include_in_isolated" >> $target_file
            fi

            if [[ "${include_in_code_coverage}" != "" ]]; then 
                echo "    codecoverage: $include_in_code_coverage" >> $target_file
            fi

    fi
    
    done < $manager_folders_list

    rm $manager_folders_list

}

rm -fr $BASEDIR/temp
mkdir -p $BASEDIR/temp

build_release_yaml $BASEDIR/temp/release.yaml

diff ${BASEDIR}/release.yaml $BASEDIR/temp/release.yaml > /dev/null
is_different=$?
if [[ "${is_different}" == "0" ]]; then
    info "The generated yaml and the release.yaml are the same, so not replacing it."
else
    info "Replacing the generated yaml with the release.yaml file..."
    cp $BASEDIR/temp/release.yaml ${BASEDIR}/release.yaml
fi

success "OK"