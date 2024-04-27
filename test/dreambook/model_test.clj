(ns dreambook.model-test
  (:require [clojure.test :refer :all]
            [dreambook.model :as m]))

(deftest minimal-parse
  (testing "xxx"
    (let [dr "* 1987

** <1987-01-25 Sun>

Some stuff I don't remember.

*** A subsection

With more stuff.

** <1987-01-26 Mon>

Not much happened.

* 1988

** <1988-01-01 Fri>

Next year.
"]
      (clojure.pprint/pprint (m/parse-dreams dr)))))