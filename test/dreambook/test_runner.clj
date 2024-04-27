(ns ^{:doc "
Adapted from
https://github.com/typedclojure/typedclojure/blob/main/script-test/test_runner.clj
"}
  dreambook.test-runner
  (:require [clojure.set :as set]
            [clojure.test :as t])
  (:import [java.io File]))

(def ^:private test-nses
  '{"test/dreambook/dates_test.clj" dreambook.dates-test
    "test/dreambook/md_test.clj" dreambook.md-test
    "test/dreambook/model_test.clj" dreambook.model-test
    "test/dreambook/html_test.clj" dreambook.html-test})

(defn- check-missing! []
  (let [test-files
        (->> (File. "test")
             file-seq
             (filter #(.isFile %))
             (map #(.getPath %))
             (filter #(clojure.string/ends-with? % ".clj"))
             set)
        exclusions #{"test/dreambook/test_runner.clj"}
        missing (set/difference
                 test-files
                 (into exclusions (keys test-nses)))]
    (assert
     (empty? missing)
     (str "Don't forget to add test files to test_runner.clj:"
          (clojure.string/join ", " missing)))))

(defn run-tests []
  (check-missing!)
  (apply require (vals test-nses))
  (let [{:keys [fail error]}
        (apply t/run-tests
               (vals test-nses))]
    (System/exit (min (+ fail error)))))
