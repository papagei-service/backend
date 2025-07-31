#!/bin/bash

# A script to compare two version strings.
# Exits with status 1 if the first version is greater than the second.
# Exits with status 0 otherwise.

# Check if exactly two arguments were provided.
if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <current_version> <previous_version>"
    exit 1
fi

CURRENT_VERSION="$1"
PREVIOUS_VERSION="$2"

# Use sort -V for robust version comparison.
# This sorts the versions and places the lower one first.
# If the current version is the same as the first element of the sorted list,
# it means the current version is either less than or equal to the previous version.
# If they are different, it means the current version is greater.
if [ "$CURRENT_VERSION" != "$(echo -e "$CURRENT_VERSION\n$PREVIOUS_VERSION" | sort -V | head -n 1)" ]; then
    echo "Success: The current version ($CURRENT_VERSION) is greater than the previous version ($PREVIOUS_VERSION)."
    exit 0
else
    echo "Error: The current version ($CURRENT_VERSION) is not greater than the previous version ($PREVIOUS_VERSION)."
    exit 1
fi

