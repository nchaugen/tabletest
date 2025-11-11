#!/bin/bash

# Compatibility matrix runner for tabletest samples
# Env vars (space-separated lists) to control matrices; defaults provided if unset:
#   JUNIT_VERSIONS="5.13.4 5.13.2 5.12.2 5.11.0"
#   QUARKUS_VERSIONS="3.25.3 3.24.0 3.23.4 3.21.2"
#   SPRINGBOOT_VERSIONS="3.5.4 3.4.8 3.4.0"
#   TABLETEST_VERSION="0.5.6-SNAPSHOT"  # default TableTest version (can be overridden via env)
# Usage examples:
#   ./test-compatibility.sh                    # run all with defaults
#   JUNIT_VERSIONS="5.13.4 5.11.0" ./test-compatibility.sh
#   QUARKUS_VERSIONS="3.25.3" SPRINGBOOT_VERSIONS="3.5.4" ./test-compatibility.sh
#   TABLETEST_VERSION="0.5.6-SNAPSHOT" ./test-compatibility.sh

# Colors for output
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Set defaults if env not provided
if [ -z "$JUNIT_VERSIONS" ]; then JUNIT_VERSIONS="6.0.1 5.14.1 5.13.4 5.12.2 5.11.0"; fi
if [ -z "$QUARKUS_VERSIONS" ]; then QUARKUS_VERSIONS="3.29.2 3.24.0 3.23.4 3.21.2"; fi
if [ -z "$SPRINGBOOT_VERSIONS" ]; then SPRINGBOOT_VERSIONS="4.0.0-RC2 3.5.7  3.4.8 3.4.0"; fi
if [ -z "$TABLETEST_VERSION" ]; then TABLETEST_VERSION="0.5.6-SNAPSHOT"; fi

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
# Args: <dir> <extra_args> <label>
run_tests() {
    local dir="$1"
    local extra_args="$2"
    local label="$3"
    local build_tool
    local relative_path

    build_tool=$(get_build_tool "$dir")
    relative_path=${dir#compatibility-tests/}

    echo -e "${YELLOW}Testing $relative_path ($build_tool) ${label}...${NC}"

    if [ ! -d "$dir" ]; then
        echo "❌ Directory $dir not found"
        ((FAILED++))
        FAILED_MODULES+=("$relative_path ${label}")
        return 1
    fi

    cd "$dir" || return 1

    case $build_tool in
        "maven")
            tt_arg="-Dtabletest.version=$TABLETEST_VERSION"
            mvn clean test -q $extra_args $tt_arg
            ;;
        "gradle")
            tt_arg="-Ptabletest.version=$TABLETEST_VERSION"
            if [ -f "./gradlew" ]; then
                ./gradlew clean test -q $extra_args $tt_arg
            else
                gradle clean test -q $extra_args $tt_arg
            fi
            ;;
        *)
            echo "❌ Unknown build tool for $dir"
            ((FAILED++))
            FAILED_MODULES+=("$relative_path ${label}")
            cd - > /dev/null
            return 1
            ;;
    esac

    if [ $? -eq 0 ]; then
        echo "✅ $relative_path ${label} PASSED"
        ((PASSED++))
    else
        echo "❌ $relative_path ${label} FAILED"
        ((FAILED++))
        FAILED_MODULES+=("$relative_path ${label}")
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
                tool=$(get_build_tool "$config_dir")
                case "$group_name" in
                  basic)
                    for v in $JUNIT_VERSIONS; do
                      if [ "$tool" = "maven" ]; then
                        run_tests "$config_dir" "-Djunit.version=$v" "[junit=$v]"
                      elif [ "$tool" = "gradle" ]; then
                        run_tests "$config_dir" "-Pjunit.version=$v" "[junit=$v]"
                      else
                        run_tests "$config_dir" "" "[junit=$v]"
                      fi
                    done
                    ;;
                  frameworks)
                    if [[ "$config_dir" == *quarkus* ]]; then
                      for v in $QUARKUS_VERSIONS; do
                        if [ "$tool" = "maven" ]; then
                          run_tests "$config_dir" "-Dquarkus.version=$v" "[quarkus=$v]"
                        elif [ "$tool" = "gradle" ]; then
                          run_tests "$config_dir" "-Pquarkus.version=$v" "[quarkus=$v]"
                        else
                          run_tests "$config_dir" "" "[quarkus=$v]"
                        fi
                      done
                    elif [[ "$config_dir" == *springboot* ]]; then
                      for v in $SPRINGBOOT_VERSIONS; do
                        if [ "$tool" = "maven" ]; then
                          run_tests "$config_dir" "-Dspringboot.version=$v" "[springboot=$v]"
                        elif [ "$tool" = "gradle" ]; then
                          run_tests "$config_dir" "-Pspringboot.version=$v" "[springboot=$v]"
                        else
                          run_tests "$config_dir" "" "[springboot=$v]"
                        fi
                      done
                    else
                      run_tests "$config_dir" "" ""
                    fi
                    ;;
                  *)
                    run_tests "$config_dir" "" ""
                    ;;
                esac
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
