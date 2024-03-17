(ns ^{:doc "
Adapted from
https://github.com/typedclojure/typedclojure/blob/main/script-test/test_runner.clj
"}
  dreams.test-runner
  (:require [clojure.set :as set]
            [clojure.test :as t])
  (:import [java.io File]))

(def test-nses
  '{"test/dreams/dates_test.clj" dreams.dates-test
    "test/dreams/md_test.clj" dreams.md-test
    "test/dreams/html_test.clj" dreams.html-test})

(defn check-missing! []
  (let [test-files
        (->> (File. "test")
             file-seq
             (filter #(.isFile %))
             (map #(.getPath %))
             (filter #(clojure.string/ends-with? % ".clj"))
             set)
        exclusions #{"test/dreams/test_runner.clj"}
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
