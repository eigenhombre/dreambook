(ns dreams.md
  (:require [clojure.string :as str]))

(defn org->md [s]
  ;; Convert Org `/s/` to MD `*s*`:
  (-> s
      (str/replace #"/([^/]+?)/" "*$1*")
      (str/replace #"--" "–")
      (str/replace #"<<" "«")
      (str/replace #">>" "»")
      (str/replace #"=([^=]+?)=" "`$1`")))
