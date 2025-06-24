#!/bin/bash

# Colors for output
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test results tracking
PASSED=0
FAILED=0
FAILED_MODULES=()

echo "Running compatibility tests across all configurations..."
echo

# Function to extract build tool from directory name
get_build_tool() {
    local dir_name
    dir_name=$(basename "$1")
    if [[ "$dir_name" =~ ^gradle- ]]; then
        echo "gradle"
    elif [[ "$dir_name" =~ ^maven- ]]; then
        echo "maven"
    else
        echo "unknown"
    fi
}

# Function to run tests in a directory
run_tests() {
    local dir
    local build_tool
    local relative_path
    dir=$1
    build_tool=$(get_build_tool "$dir")
    relative_path=${dir#compatibility-tests/}

    echo -e "${YELLOW}Testing $relative_path ($build_tool)...${NC}"

    if [ ! -d "$dir" ]; then
        echo "❌ Directory $dir not found"
        ((FAILED++))
        FAILED_MODULES+=("$relative_path")
        return 1
    fi

    cd "$dir" || return 1

    case $build_tool in
        "maven")
            mvn clean test -q
            ;;
        "gradle")
            if [ -f "./gradlew" ]; then
                ./gradlew clean test -q
            else
                gradle clean test -q
            fi
            ;;
        *)
            echo "❌ Unknown build tool for $dir"
            ((FAILED++))
            FAILED_MODULES+=("$relative_path")
            cd - > /dev/null
            return 1
            ;;
    esac

    if [ $? -eq 0 ]; then
        echo "✅ $relative_path PASSED"
        ((PASSED++))
    else
        echo "❌ $relative_path FAILED"
        ((FAILED++))
        FAILED_MODULES+=("$relative_path")
    fi

    cd - > /dev/null
    echo
}

# Find and run tests for all configurations
echo "Scanning compatibility-tests directory..."

for group_dir in compatibility-tests/*/; do
    if [ -d "$group_dir" ]; then
        group_name=$(basename "$group_dir")
        echo "Found test group: $group_name"

        for config_dir in "$group_dir"*/; do
            if [ -d "$config_dir" ]; then
                run_tests "$config_dir"
            fi
        done
    fi
done

# Summary
echo "Test Summary:"
echo "✅ Passed: $PASSED"
echo "❌ Failed: $FAILED"

if [ $FAILED -gt 0 ]; then
    echo
    echo "Failed configurations:"
    printf '%s\n' "${FAILED_MODULES[@]}"
    exit 1
else
    echo
    echo "All compatibility tests passed!"
fi
