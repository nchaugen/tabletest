"""Discover untested JUnit releases without changing production dependency versions."""

import argparse
import json
import os
import re
import urllib.request
import xml.etree.ElementTree as ET
from pathlib import Path

METADATA_URL = "https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter/maven-metadata.xml"
CACHE_PREFIX = "junit-release-v1-"
VERSION = re.compile(r"(\d+)\.(\d+)\.(\d+)(?:-(M|RC)([1-9]\d*))?")


def version_key(version):
    match = VERSION.fullmatch(version)
    if not match:
        raise ValueError(f"Unsupported JUnit release version: {version!r}")
    major, minor, patch, qualifier, sequence = match.groups()
    stage = {"M": 0, "RC": 1, None: 2}[qualifier]
    return int(major), int(minor), int(patch), stage, int(sequence or 0)


def select_versions(versions, baseline, tested_keys=(), requested=""):
    minimum = version_key(baseline)
    if requested:
        if version_key(requested)[0] < minimum[0]:
            raise ValueError(f"This workflow supports JUnit {minimum[0]} and newer")
        return [requested]  # Explicit runs always retest, including cached versions.
    candidates = {
        version for version in versions
        if VERSION.fullmatch(version) and version_key(version) > minimum
        and CACHE_PREFIX + version not in tested_keys
    }
    return sorted(candidates, key=version_key)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--pom", required=True, type=Path)
    parser.add_argument("--tested-keys", required=True, type=Path)
    parser.add_argument("--version", default="")
    parser.add_argument("--metadata", type=Path, help="Local Maven metadata for offline checks")
    args = parser.parse_args()
    baseline = ET.parse(args.pom).findtext(
        "./{*}properties/{*}version.junit"
    )
    if not baseline:
        raise ValueError(f"No version.junit property in {args.pom}")
    versions = []
    if not args.version:
        if args.metadata:
            metadata = args.metadata.read_bytes()
        else:
            with urllib.request.urlopen(METADATA_URL, timeout=30) as response:
                metadata = response.read()
        versions = [element.text for element in ET.fromstring(metadata).findall("./versioning/versions/version")]
    selected = select_versions(
        versions, baseline, set(args.tested_keys.read_text().splitlines()), args.version
    )
    matrix = json.dumps(selected)
    print(f"Production JUnit: {baseline}; untested releases: {matrix}")
    if os.environ.get("GITHUB_OUTPUT"):
        with open(os.environ["GITHUB_OUTPUT"], "a", encoding="utf-8") as output:
            output.write(f"versions={matrix}\n")
    if os.environ.get("GITHUB_STEP_SUMMARY"):
        with open(os.environ["GITHUB_STEP_SUMMARY"], "a", encoding="utf-8") as summary:
            summary.write(f"Production JUnit: **{baseline}**. ")
            summary.write(f"Releases to test: {matrix}.\n" if selected else "No untested releases found.\n")


if __name__ == "__main__":
    main()
