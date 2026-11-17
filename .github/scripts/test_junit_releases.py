"""Guard release selection, including milestones, release candidates and reruns."""

import unittest

from junit_releases import CACHE_PREFIX, select_versions


class ReleaseSelectionTest(unittest.TestCase):
    def test_orders_prereleases_before_the_final_release(self):
        versions = ["6.2.0", "6.2.0-RC2", "6.2.0-M10", "6.2.0-RC1", "6.2.0-M2"]
        self.assertEqual(
            select_versions(versions, "6.1.3"),
            ["6.2.0-M2", "6.2.0-M10", "6.2.0-RC1", "6.2.0-RC2", "6.2.0"],
        )

    def test_keeps_all_unseen_new_releases_across_minor_and_major_lines(self):
        self.assertEqual(
            select_versions(["6.1.4", "6.2.0-M1", "7.0.0-M1", "6.1.4"], "6.1.3"),
            ["6.1.4", "6.2.0-M1", "7.0.0-M1"],
        )

    def test_ignores_old_unsupported_and_successfully_tested_versions(self):
        self.assertEqual(
            select_versions(
                ["5.14.0", "6.1.2", "6.1.3", "6.2.0-SNAPSHOT", "6.2.0-beta1", "6.2.0-M1", "6.2.0-M2"],
                "6.1.3", {CACHE_PREFIX + "6.2.0-M1"},
            ),
            ["6.2.0-M2"],
        )

    def test_manual_run_retests_a_known_version_even_at_the_baseline(self):
        self.assertEqual(
            select_versions([], "6.1.3", {CACHE_PREFIX + "6.1.3"}, requested="6.1.3"),
            ["6.1.3"],
        )

    def test_rejects_invalid_manual_versions(self):
        for version in ["6.2.0-SNAPSHOT", "6.2.0-M0", "6.2.0; echo injected", "../6.2.0"]:
            with self.subTest(version=version), self.assertRaises(ValueError):
                select_versions([], "6.1.3", requested=version)

    def test_rejects_older_major_versions_for_this_test_suite(self):
        with self.assertRaises(ValueError):
            select_versions([], "6.1.3", requested="5.12.0")

    def test_final_release_is_newer_than_its_milestone_baseline(self):
        self.assertEqual(select_versions(["6.2.0-M1", "6.2.0"], "6.2.0-M1"), ["6.2.0"])


if __name__ == "__main__":
    unittest.main()
