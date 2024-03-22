(ns dreams.main
  (:require [clojure.java.shell :as sh]
            [clojure.string :as str]
            [dreams.dates :as d]
            [dreams.epub :as epub]
            [dreams.md :refer [org->md]]
            [dreams.model :as m]
            [dreams.org :as org]
            [dreams.util :refer [wrap-n-columns nopunct normalize]]))

(def ^:private org-dir (str (System/getenv "HOME") "/org"))
(def ^:private dreams-path (str org-dir "/dreams.org"))
(def ^:private dreams-intro-path (str org-dir "/dreams-intro.org"))
(def ^:private dreams-collophon-path (str org-dir "/dreams-collophon.org"))
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
  (format-dreams-md dreams))

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

(def ^:private english-words-top-100
  ;; Source: https://en.wikipedia.org/wiki/Most_common_words_in_English
  (-> "the be to of and a in that have i it for not on with he as you do at
this but his by from they we say her she or an will my one all would
there their what so up out if about who get which go me when make can
like time no just him know take people into year your good some could
them see other than then now look only come its over think also back
after use two how our work first well way even new want because any
these give day most us"
      (str/split #"\s+")
      set))

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
         (remove english-words-top-100)
         (take 300)
         sort
         (str/join " ")
         (wrap-n-columns 10))))

(defn find-frontmatter [s]
  (->> s
       org/strip-frontmatter
       org/raw-sections-before-date))

(defn epub []
  (let [raw-dreams (slurp dreams-path)
        parsed-dreams (->> raw-dreams
                           m/parse-dreams)
        starting-md (->> dreams-intro-path
                         slurp
                         org/strip-frontmatter
                         org->md)
        ending-md (->> dreams-collophon-path
                       slurp
                       org/strip-frontmatter
                       org->md)]
    (epub/mk-epub cover-image-path starting-md parsed-dreams ending-md)
    (print-years parsed-dreams)))
