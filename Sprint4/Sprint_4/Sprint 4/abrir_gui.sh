#!/bin/sh
set -eu
cd "$(dirname "$0")"
sh compilar.sh
java -cp build TelaFarmacia
