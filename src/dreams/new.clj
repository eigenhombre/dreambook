(ns dreams.new
  (:require
  [clojure.string :as str]
  [clojure.java.shell :as sh]))

(def org-dir (str (System/getenv "HOME") "/org"))
(def dreams-path (str org-dir "/dreams.org"))
(def md-path (str org-dir "/dreams.md"))
(def cover-image (str org-dir "/dreams-cover.png"))
(def epub-output (str (System/getenv "HOME") "/Desktop/dreams.epub"))

(defn main []
  (println "Hello, world!"))
