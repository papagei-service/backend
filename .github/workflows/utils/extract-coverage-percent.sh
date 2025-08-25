#!/bin/bash

# Extract a numeric value from a specific line in a text file.

# --- Functions ---

# Get the specific line from the file.
# Returns:
#   The specific line from the file if found
#   1 if the file or the specific line not found
get_coverage_line() {

  # Check if the file exists and is readable.
  local filename="$1"
  if [[ ! -f "$filename" ]]; then
    echo "Error: The file '$filename' not found." >&2
    return 1
  fi

  # Search the specific line.
  local result
  result=$(grep -E 'application line coverage:' "$filename" | head -n 1)

  # Check if a line was found.
  if [[ -z "$result" ]]; then
    echo "Error: The corresponding line in the '$filename' file was not found." >&2
    return 1
  else
    echo "$result"
  fi
}

# Extract the number from a given line
# This function takes a string (a line) as an argument and extracts the numeric value.
# Returns:
#   The specific numeric value from the line if found
#   1 if the specific numeric value not found
extract_number_from_line() {

  # Extract the specific numeric value.
  # 'awk {print $4}' prints the 4th field (which is "86.1972%").
  # 'sed 's/%//'' removes the '%' symbol.
  local line="$1"
  local numeric_value
  numeric_value=$(echo "$line" | awk '{print $4}' | sed 's/%//')
  echo "$line" | awk '{print $4}' | sed 's/%//'

  # Check if a line was found.
  if [[ "$numeric_value" =~ ^[0-9]+\.?[0-9]*$ ]]; then
    echo "Error: The corresponding value in the '$line' line was not found." >&2
    return 1
  else
    echo "$numeric_value"
  fi
}

# --- Main script logic ---

# Check if exactly one argument was provided to the function.
if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <filename>" >&2
  return 2
fi

# Get the line that contains the coverage percent
coverage_line=$(get_coverage_line "$1")
# Get the specific numeric value
numeric_value=$(extract_number_from_line "$coverage_line")

# Print the final result.
echo "$numeric_value"
