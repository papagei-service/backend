#!/bin/bash

# Get the code coverage percentage and compare it with the minimum required.

# --- Functions ---

# Get the specific line from the file.
# Returns:
#   The specific line from the file on stdout.
#   Returns 0 on success, 1 on failure.
extract_coverage_line() {
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
# Returns:
#   0 if the current code coverage percentage is greater than or equal to the minimum required.
#   1 if the current code coverage percentage is less than or equal to the minimum required.
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

# Navigate to the script directory and define essential paths.
script_dir_path="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
project_dir_path="$script_dir_path/.."
coverage_report_file_path="$project_dir_path/kover-coverage-report.out"

# Generate the coverage report file.
gradle :koverPrintCoverage --quiet >> "$coverage_report_file_path"

# Extract the line with coverage percent from the file.
coverage_line=$(extract_coverage_line "$coverage_report_file_path")
extract_coverage_line_status=$?
if [[ $extract_coverage_line_status -ne 0 ]]; then
  exit 1
fi

# Extract the coverage value from the line.
echo "Extracting the coverage percentage from the line..."
current_coverage=$(extract_number_from_line "$coverage_line")
extract_number_from_line_status=$?
if [[ $extract_number_from_line_status -ne 0 ]]; then
  exit 1
fi

# Check if current code coverage is greater than required.
minimal_coverage="$MINIMAL_CODE_COVERAGE"
check_coverage_percentage "$current_coverage" "$minimal_coverage"
check_coverage_status=$?
if [[ $check_coverage_status -ne 0 ]]; then
  exit 1
fi

# Clear the cache.
rm "$coverage_report_file_path"