#!/bin/bash

# Extract a numeric value from a specific line in a text file.

# --- Functions ---

# Get the specific line from the file.
# Returns:
#   The specific line from the file on stdout.
#   Returns 0 on success, 1 on failure.
get_coverage_line() {
  local filename="$1"

  # Check if the file exists and is readable.
  if [[ ! -f "$filename" ]]; then
    echo "Error: Source coverage report file not found at the following path:" >&2
    echo "=> $coverage_report_file" >&2
    return 2
  fi

  # Search the specific line.
  local result
  result=$(grep -E 'application line coverage:' "$filename" | head -n 1)

  # Check if a line was found.
  if [[ -z "$result" ]]; then
    echo "Error: Cannot get the line that contains the coverage percentage." >&2
    return 1
  else
    echo "$result"
    return 0
  fi
}

# Extract the number from a given line.
# Returns:
#   The specific numeric value from the line on stdout.
#   Returns 0 on success, 1 on failure.
extract_number_from_line() {
  local line="$1"
  local numeric_value

  # Extract the specific numeric value.
  numeric_value=$(echo "$line" | awk '{print $4}' | sed 's/%//')

  # Check if a valid numeric value was extracted.
  if [[ "$numeric_value" =~ ^[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)$ ]]; then
    echo "$numeric_value"
    return 0
  else
    echo "Cannot extract numeric coverage percentage from the following line:" >&2
    echo "=> $line" >&2
    return 1
  fi
}

# Check if current code coverage is greater than required.
check_coverage_percentage() {
  local current="$1"
  local minimal="$2"

  if (( $(echo "$current > $minimal" | bc -l) )); then
    echo "Success: Current code test coverage ($current%) is greater than the minimal required ($minimal%)."
    return 0
  elif (( $(echo "$current < $minimal" | bc -l) )); then
    echo "Error: Current code test coverage ($current%) is lesser than the minimal required ($minimal%)." >&2
    return 1
  else
    echo "Success: Current code test coverage ($current%) is equals to the minimal required ($minimal%)." >&2
    return 0
  fi
}

# --- Main script logic ---

# Check if exactly one argument was provided.
if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <coverage_report_file>" >&2
  exit 1
fi

# Get the line with coverage percent.
echo "Extracting the line that contains coverage percentage..."
coverage_report_file="$1"
coverage_line=$(get_coverage_line "$coverage_report_file")
get_coverage_line_status=$?

if [[ $get_coverage_line_status -ne 0 ]]; then
  exit 1
fi

# Extract the coverage value from the line.
echo "Extracting the coverage percentage from the corresponding line..."
current_coverage=$(extract_number_from_line "$coverage_line")
extract_number_status=$?

if [[ $extract_number_status -ne 0 ]]; then
  exit 1
fi

# Check if current code coverage is greater than required.
minimal_coverage=80
check_coverage_percentage "$current_coverage" "$minimal_coverage"
check_coverage_status=$?

if [[ $check_coverage_status -ne 0 ]]; then
  exit 1
fi
