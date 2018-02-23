#!/usr/bin/env sh

print_help() {
  echo "Script to run docker containers for Spring Boot Template API service

  Usage:

  ./run-in-docker.sh [OPTIONS]

  Options:
    --clean, -c                   Clean and install current state of source code
    --install, -i                 Install current state of source code
    --param PARAM=, -p PARAM=     Parse script parameter
    --help, -h                    Print this help block

  Available parameters:
    APPLICATION_INSIGHTS_IKEY     Defaults to '00000000-0000-0000-0000-000000000000'
    FTP_FINGERPRINT               Defaults to 'fingerprint'
    FTP_HOSTNAME                  Defaults to 'hostname'
    FTP_PORT                      Defaults to '22'
    FTP_PRIVATE_KEY               Defaults to 'private'
    FTP_PUBLIC_KEY                Defaults to 'public'
    FTP_REPORTS_FOLDER            Defaults to '/reports/'
    FTP_TARGET_FOLDER             Defaults to '/target/'
    FTP_USER                      Defaults to 'user'
    PDF_SERVICE_URL               Defaults to 'http://localhost:5500'
    S2S_SECRET                    Defaults to 'secret'
    S2S_URL                       Defaults to 'false' - disables health check
    SEND_LETTER_PRODUCER_URL      Defaults to 'http://localhost:8485'
  "
}

# script execution flags
GRADLE_CLEAN=false
GRADLE_INSTALL=false

# environment variables
APPLICATION_INSIGHTS_IKEY="00000000-0000-0000-0000-000000000000"
FTP_FINGERPRINT="fingerprint"
FTP_HOSTNAME="hostname"
FTP_PORT=22
FTP_PRIVATE_KEY="private"
FTP_PUBLIC_KEY="public"
FTP_REPORTS_FOLDER="/reports/"
FTP_TARGET_FOLDER="/target/"
FTP_USER="user"
PDF_SERVICE_URL="http://localhost:5500"
S2S_SECRET="secret"
S2S_URL=false
SEND_LETTER_PRODUCER_URL="http://localhost:8485"

execute_script() {
  cd $(dirname "$0")/..

  if [ ${GRADLE_CLEAN} = true ]
  then
    echo "Clearing previous build.."
    ./gradlew clean
  fi

  if [ ${GRADLE_INSTALL} = true ]
  then
    echo "Installing distribution.."
    ./gradlew installDist
  fi

  echo "Assigning environment variables.."

  export APPLICATION_INSIGHTS_IKEY=${APPLICATION_INSIGHTS_IKEY}
  export FTP_FINGERPRINT=${FTP_FINGERPRINT}
  export FTP_HOSTNAME=${FTP_HOSTNAME}
  export FTP_PORT=${FTP_PORT}
  export FTP_PRIVATE_KEY=${FTP_PRIVATE_KEY}
  export FTP_PUBLIC_KEY=${FTP_PUBLIC_KEY}
  export FTP_REPORTS_FOLDER=${FTP_REPORTS_FOLDER}
  export FTP_TARGET_FOLDER=${FTP_TARGET_FOLDER}
  export FTP_USER=${FTP_USER}
  export PDF_SERVICE_URL=${PDF_SERVICE_URL}
  export S2S_SECRET=${S2S_SECRET}
  export S2S_URL=${S2S_URL}
  export SEND_LETTER_PRODUCER_URL=${SEND_LETTER_PRODUCER_URL}

  echo "Bringing up docker containers.."

  docker-compose up
}

while true ; do
  case "$1" in
    -h|--help) print_help ; shift ; break ;;
    -c|--clean) GRADLE_CLEAN=true ; GRADLE_INSTALL=true ; shift ;;
    -i|--install) GRADLE_INSTALL=true ; shift ;;
    -p|--param)
      case "$2" in
        APPLICATION_INSIGHTS_IKEY=*) APPLICATION_INSIGHTS_IKEY="${2#*=}" ; shift 2 ;;
        FTP_FINGERPRINT=*) FTP_FINGERPRINT="${2#*=}" ; shift 2 ;;
        FTP_HOSTNAME=*) FTP_HOSTNAME="${2#*=}" ; shift 2 ;;
        FTP_PORT=*) FTP_PORT="${2#*=}" ; shift 2 ;;
        FTP_PRIVATE_KEY=*) FTP_PRIVATE_KEY="${2#*=}" ; shift 2 ;;
        FTP_PUBLIC_KEY=*) FTP_PUBLIC_KEY="${2#*=}" ; shift 2 ;;
        FTP_REPORTS_FOLDER=*) FTP_REPORTS_FOLDER="${2#*=}" ; shift 2 ;;
        FTP_TARGET_FOLDER=*) FTP_TARGET_FOLDER="${2#*=}" ; shift 2 ;;
        FTP_USER=*) FTP_USER="${2#*=}" ; shift 2 ;;
        PDF_SERVICE_URL=*) PDF_SERVICE_URL="${2#*=}" ; shift 2 ;;
        S2S_SECRET=*) S2S_SECRET="${2#*=}" ; shift 2 ;;
        S2S_URL=*) S2S_URL="${2#*=}" ; shift 2 ;;
        SEND_LETTER_PRODUCER_URL=*) SEND_LETTER_PRODUCER_URL="${2#*=}" ; shift 2 ;;
        *) shift 2 ;;
      esac ;;
    *) execute_script ; break ;;
  esac
done
