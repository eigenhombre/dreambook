(ns dreams.html
  (:require [clojure.string :as str]))

(defn md->html
  [s]
  (let [paragraph-sections (str/split s #"\n\n")]
    (str/join
     "\n"
     (for [section paragraph-sections]
       (str "<p>" section "</p>")))))
