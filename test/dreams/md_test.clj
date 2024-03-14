(ns dreams.md-test
  (:require [clojure.test :refer :all]
            [dreams.md :as md]))

(deftest test-md
  (testing "md->html"
    (are [input expected]
      (= expected (md/org->md input))
      "foo" "foo"
      "" ""
      "/hello/" "*hello*"
      " /hello/ " " *hello* "
      "a/b" "a/b")))