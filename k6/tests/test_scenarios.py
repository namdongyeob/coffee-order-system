"""Issue #13 k6 주문 시나리오의 정적·inspect 계약을 검증합니다."""

import json
import subprocess
import unittest
from pathlib import Path


K6_DIR = Path(__file__).resolve().parents[1]
SCENARIOS = ("order-load.js", "order-stress.js", "order-spike.js")


class K6ScenarioContractTest(unittest.TestCase):

    def test_all_scenarios_are_inspectable_and_bounded_by_default(self):
        for scenario in SCENARIOS:
            with self.subTest(scenario=scenario):
                result = subprocess.run(
                    ["k6", "inspect", str(K6_DIR / scenario)],
                    check=True,
                    capture_output=True,
                    text=True,
                    encoding="utf-8",
                )
                inspected = json.loads(result.stdout)
                scenario_options = next(iter(inspected["scenarios"].values()))
                max_vus = max(stage["target"] for stage in scenario_options["stages"])
                self.assertLessEqual(max_vus, 10)
                self.assertIn("http_req_failed", inspected["thresholds"])
                self.assertIn("order_error_rate", inspected["thresholds"])

    def test_scenarios_share_order_contract_and_expose_named_profiles(self):
        common = (K6_DIR / "lib" / "order-scenario.js").read_text(encoding="utf-8")
        self.assertIn("http.post", common)
        self.assertIn("/api/points/charge", common)
        self.assertIn("/api/orders", common)
        self.assertIn("order_success_rate", common)
        self.assertIn("order_error_rate", common)
        self.assertIn("synthetic", common)
        self.assertIn("K6_PROFILE", common)

        expected_names = {
            "order-load.js": "order_load",
            "order-stress.js": "order_stress",
            "order-spike.js": "order_spike",
        }
        for scenario, name in expected_names.items():
            with self.subTest(scenario=scenario):
                source = (K6_DIR / scenario).read_text(encoding="utf-8")
                self.assertIn(name, source)
                self.assertIn("safe", source)
                self.assertIn("heavy", source)
                self.assertIn("chargeRounds", source)
                self.assertIn("prepareSyntheticUsers(selected.maxVUs, selected.chargeRounds)", source)


if __name__ == "__main__":
    unittest.main()
