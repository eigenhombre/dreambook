(ns dreams.main
  (:require [clojure.java.shell :as sh]
            [clojure.string :as str]
            [dreams.dates :as d]
            [dreams.epub :as epub]
            [dreams.md :refer [org->md]]
            [dreams.model :as m]
            [dreams.util :refer [wrap-n-columns nopunct normalize]]))

(def ^:private org-dir (str (System/getenv "HOME") "/org"))
(def ^:private dreams-path (str org-dir "/dreams.org"))
(def ^:private md-path (str org-dir "/dreams.md"))
(def ^:private cover-image-path (str org-dir "/dreams-cover.png"))
(def ^:private epub-output (str (System/getenv "HOME") "/Desktop/dreams.epub"))

(defn- toc-str [dreams]
  (let [ym (d/year-months (m/dream-dates dreams))]
    (str
     (str/join
      "\n"
      (for [[yr months] ym]
        (str/join "\n" (concat [(format "- [%s](#%s)" yr yr)]
                               (for [m months]
                                 (format "    - [%s](#%s-%s)"
                                         m yr m)))))))))

(defn- format-dreams-md [dreams]
  (let [yrs-mos (d/year-months (m/dream-dates dreams))]
    (str/join
     "\n\n"
     (for [[yr mos] yrs-mos]
       (str (format "# %s\n\n" yr)
            (str/join
             "\n"
             (for [mo mos]
               (str (format "## <a name=\"%s-%s\"></a>\n\n"
                            yr mo)
                    (str/join
                     "\n"
                     (for [{:keys [date id txt]}
                           (m/dreams-for-year-month yr mo dreams)]
                       (format "### %s\n\n%s"
                               (d/format-date-for-section date)
                               (org->md txt))))))))))))

(defn- dreams-as-md [dreams]
  (let [toc (toc-str dreams)]
    (str toc "\n\n" (format-dreams-md dreams))))

(defn- spit-md [parsed-dreams]
  (spit md-path (dreams-as-md parsed-dreams)))

(defn- print-years [parsed-dreams]
  (let [year-counts (->> parsed-dreams
                         (group-by :year)
                         (sort-by first)
                         (map (juxt first (comp count second))))]
    (doseq [[yr cnt] year-counts]
      (println (format "%d: %4d %s"
                       yr
                       cnt
                       (str/join "" (repeat cnt ".")))))
    (println (format "TOTAL: %d" (count parsed-dreams)))))

(defn- format-single-dream [dream]
  (str/join
   "\n"
   [(format "# %s\n\n" (d/format-date-for-section (:date dream)))
    (org->md (:txt dream))]))

(defn random-dream-str []
  (let [dreams (->> dreams-path
                    slurp
                    m/parse-dreams)]
    (format-single-dream (rand-nth dreams))))

(defn dreamwords-str []
  (let [dreams (->> dreams-path
                    slurp
                    m/parse-dreams)
        tokens (->> dreams
                    (map :txt)
                    (mapcat #(str/split % #"\s+|\-"))
                    (map normalize))]
    (->> tokens
         frequencies
         (sort-by second)
         reverse
         (map first)
         (take 300)
         (str/join " ")
         (wrap-n-columns 10))))

(defn epub []
  (let [parsed-dreams (->> dreams-path
                           slurp
                           m/parse-dreams)]
    (spit-md parsed-dreams)
    (epub/mk-epub cover-image-path parsed-dreams)
    (print-years parsed-dreams)))
