(ns dreambook.util
  (:require [clojure.string :as str]))

(defn- wrap-n-columns [n s]
  (str/join
   "\n"
   (map #(str/join " " %)
        (partition n (str/split s #"\s+")))))

(defn- nopunct [s]
  (str/replace s #"[,-\.\?\\\[\]\(\)\-–\"“”$’]*" ""))

(defn- normalize [w]
  (->> w
       str/lower-case
       nopunct))
