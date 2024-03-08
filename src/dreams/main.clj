(ns dreams.main
  (:require [clojure.java.shell :as sh]
            [clojure.string :as str]
            [dreams.dates :as d]
            [dreams.parse :as p]))

(def org-dir (str (System/getenv "HOME") "/org"))
(def dreams-path (str org-dir "/dreams.org"))
(def md-path (str org-dir "/dreams.md"))
(def cover-image (str org-dir "/dreams-cover.png"))
(def epub-output (str (System/getenv "HOME") "/Desktop/dreams.epub"))

(defn- org->md [s]
  ;; Convert Org `/s/` to MD `*s*`:
  (-> s
      (str/replace #"/([^/]+?)/" "*$1*")
      (str/replace #"--" "–")
      (str/replace #"<<" "«")
      (str/replace #">>" "»")
      (str/replace #"=([^=]+?)=" "`$1`")))

(defn- year-months [dates]
  (->> dates
       (map (juxt #(.getYear %) (comp d/month-name #(.getMonth %))))
       distinct
       (partition-by first)
       (map (juxt ffirst (partial map second)))))

(defn- dream-dates [dreams]
  (map :date dreams))

(defn- toc-str [dreams]
  (let [ym (year-months (dream-dates dreams))]
    (str
     (str/join
      "\n"
      (for [[yr months] ym]
        (str/join "\n" (concat [(format "- [%s](#%s)" yr yr)]
                               (for [m months]
                                 (format "    - [%s](#%s-%s)"
                                         m yr m)))))))))


(defn- dreams-for-year-month [year month dreams]
  (filter #(and (= year (.getYear (:date %)))
                (= month (d/month-name (.getMonth (:date %)))))
          dreams))


(defn- year-months [dates]
  (->> dates
       (map (juxt #(.getYear %) (comp d/month-name #(.getMonth %))))
       distinct
       (partition-by first)
       (map (juxt ffirst (partial map second)))))

(defn- format-dreams [dreams]
  (let [yrs-mos (year-months (dream-dates dreams))]
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
                           (dreams-for-year-month yr mo dreams)]
                       (format "### %s\n\n%s"
                               (d/format-date-for-section date)
                               (org->md txt))))))))))))

(defn- dreams-as-md [dreams]
  (let [toc (toc-str dreams)]
    (str toc "\n\n" (format-dreams dreams))))

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

(defn- format-single-dream [dream]
  (str/join
   "\n"
   [(format "# %s\n\n" (d/format-date-for-section (:date dream)))
    (org->md (:txt dream))]))

(defn random-dream-str []
  (let [dreams (->> dreams-path
                    slurp
                    p/parse-dreams)]
    (format-single-dream (rand-nth dreams))))

(defn dreamwords []
  (let [dreams (->> dreams-path
                    slurp
                    p/parse-dreams)
        tokens (->> dreams
                    (map :txt)
                    (mapcat #(str/split % #"\s+|\-"))
                    (map normalize))]
    (println (->> tokens
                  frequencies
                  (sort-by second)
                  reverse
                  (map first)
                  (take 300)
                  (str/join " ")
                  (wrap-n-columns 10)))))

(defn md->epub []
  (let [{:keys [exit out err]}
        (sh/sh "ebook-convert"
               md-path
               epub-output
               "--output-profile=tablet"
               (str "--cover=" cover-image)
               "--authors=Eig N. Hombre"
               "--title=eBook of Dreams")]
    (assert (zero? exit) (str "Error: " err))
    (println out)))

(defn install-book []
  (println (sh/sh "open" "-a" "/Applications/calibre.app" epub-output))
  (println (sh/sh "open" epub-output)))

(defn main []
  (let [parsed-dreams (->> dreams-path
                           slurp
                           p/parse-dreams)]
    (spit-md parsed-dreams)
    (print-years parsed-dreams)))
