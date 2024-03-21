(ns dreams.dates-test
  (:require [clojure.test :refer :all]
            [dreams.dates :as d]))

(deftest test-date-parse
  (testing "parse and unparse"
    (are [input expected]
      (is (= expected (d/format-date-for-section
                       (d/parse-date input))))
      "<1998-11-01 Sun>" "Sunday, November 1")))
