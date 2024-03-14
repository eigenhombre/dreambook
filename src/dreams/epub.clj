(ns dreams.epub
  (:require [babashka.fs :as fs]
            [clojure.data.xml :as xml]
            [clojure.string :as str]
            [dreams.dates :as d]
            [dreams.model :as m]))

(defn- strip-odd-c-or-d [s]
  (str/replace s #"xmlns:.=" "xmlns="))

(defn- normalize-xml [s]
  (-> s
      strip-odd-c-or-d
      (str/replace "  " "\t")))

(defn- uuid []
  (java.util.UUID/randomUUID))

(defn- chaplink [chapname]
  (-> chapname
      (clojure.string/replace #" " "")
      str/lower-case))

(defn- opf [title author uuid-str chapters]
  (let [modified-date "2024-02-28T12:00:00Z"
        content-opf
        `[:package {"xmlns" "http://www.idpf.org/2007/opf"
                    :dir "ltr"
                    :unique-identifier "uid"
                    :version "3.0"
                    :xml:lang "en-US"}
          [:metadata {:xmlns:dc "http://purl.org/dc/elements/1.1/"}
           [:dc:identifier {:id "uid"} ~uuid-str]
           [:dc:date ~modified-date]
           [:link {:href "onix.xml"
                   :media-type "application/xml"
                   :properties "onix"
                   :rel "record"}]
           [:dc:title {:id "title"} ~title]
           [:dc:creator {:id "author"} ~author]
           [:meta {:name "cover" :content "cover.png"}]]
          [:manifest
           [:item {:href "images/cover.png"
                   :id "cover.png"
                   :media-type "image/jpeg"
                   :properties "cover-image"}]
           ~@(for [[chapname _] chapters]
               [:item {:href (str "text/" (chaplink chapname) ".xhtml")
                       :id (chaplink chapname)
                       :media-type "application/xhtml+xml"}])
           [:item {:href "toc.xhtml"
                   :id "toc.xhtml"
                   :media-type "application/xhtml+xml"
                   :properties "nav"}]
           [:item {:href "toc.ncx"
                   :id "ncx"
                   :media-type "application/x-dtbncx+xml"}]]
          [:spine {:toc "ncx"}
           [:itemref {:idref "toc.xhtml"}]
           ~@(for [[chapname _] chapters]
               [:itemref {:idref (chaplink chapname)}])]]]
    (normalize-xml (xml/indent-str
                    (xml/sexp-as-element
                     content-opf)))))

(defn- chapter [title content]
  (let [doc `[:html {:xmlns "http://www.w3.org/1999/xhtml"
                     :xmlns:epub "http://www.idpf.org/2007/ops"
                     :xml:lang "en"
                     :lang "en"}
              [:head
               [:title ~title]]
              [:body
               [:h1 ~title]
               [:p ~content]]]]
    (normalize-xml (xml/indent-str (xml/sexp-as-element doc)))))

(defn- mkdirp [& paths]
  (doseq [path paths]
    (clojure.java.shell/sh "mkdir" "-p" path)))

(defn- rmrf [& paths]
  (doseq [path paths]
    (when-not (.startsWith (str path) "/")
      (clojure.java.shell/sh "rm" "-rf" path))))

(defn- container-xml [cdir]
  (normalize-xml (xml/indent-str
                  (xml/sexp-as-element
                   [:container
                    {:version "1.0"
                     :xmlns "urn:oasis:names:tc:opendocument:xmlns:container"}
                    [:rootfiles
                     [:rootfile {:full-path (str cdir "/content.opf")
                                 :media-type "application/oebps-package+xml"}]]]))))

(defn- toc-ncx [title uid chapters]
  (normalize-xml
   (xml/indent-str
    (xml/sexp-as-element
     `[:ncx {:xmlns "http://www.daisy.org/z3986/2005/ncx/"
             :version "2005-1"
             :xml:lang "en-US"}
       [:head
        [:meta {:content ~uid :name "dtb:uid"}]]
       [:docTitle [:text "Table of Contents"]]
       [:navMap {:id "navmap"}
        ~@(for [[i [chapname]] (map-indexed vector chapters)]
            [:navPoint {:id (str "navpoint-" (inc i))
                        :playOrder (inc i)}
             [:navLabel [:text chapname]]
             [:content
              {:src (str "text/" (chaplink chapname) ".xhtml")}]])]]))))

(defn- toc-xhtml [title chapters]
  (normalize-xml
   (xml/indent-str
    (xml/sexp-as-element
     `[:html {:xmlns "http://www.w3.org/1999/xhtml"
              :xmlns:epub "http://www.idpf.org/2007/ops"
              :lang "en-US"
              :epub:prefix
              (str "z3998: "
                   "http://www.daisy.org/z3998/2012/vocab/structure/, "
                   "se: https://standardebooks.org/vocab/1.0")
              :xml:lang "en-US"}
       [:head
        [:title ~title]]
       [:body {:epub:type "frontmatter"}
        [:nav {:id "toc" :role "doc-toc" :epub:type "toc"}
         [:h2 {:epub:type "title"} "Table of Contents"]
         [:ol
          ~@(for [[chapname _] chapters]
              [:li [:a {:href
                        (str "text/"
                             (chaplink chapname)
                             ".xhtml")}
                    chapname]])]]]]))))

(defn- files-to-zip [cdir]
  (map str (concat (file-seq (fs/file "META-INF"))
                   (file-seq (fs/file cdir))
                   ["mimetype"])))


(defn generate-epub [bookname title author image-file chapters]
  (let [cdir "epub"
        uid (uuid)
        cspit (fn [f c] (spit (str cdir "/" f) c))]
    (rmrf cdir "META-INF" "mimetype")
    (mkdirp "META-INF")
    (spit "mimetype" "application/epub+zip")
    (mkdirp cdir (str cdir "/text") (str cdir "/images"))
    (spit "META-INF/container.xml" (container-xml cdir))
    (cspit "content.opf" (opf title author uid chapters))
    (fs/copy image-file (str cdir "/images/cover.png"))
    (doseq [[chapname content] chapters]
      (cspit (str "text/" (chaplink chapname) ".xhtml")
             (chapter chapname content)))
    (cspit "toc.ncx" (toc-ncx title uid chapters))
    (cspit "toc.xhtml" (toc-xhtml title chapters))
    (println (->> cdir
                  files-to-zip
                  (cons "book.epub")
                  (cons "-q")
                  (cons "zip")
                  (apply clojure.java.shell/sh))))
  (println (format "EPUB '%s' generated successfully."
                   bookname)))


;; (generate-epub "book.epub"
;;                "From Bohm to Loess"
;;                "Eig N. Hombre")

(def ^:private chapters
  [["Introduction"
    "This is an intro.  There are many like it, but this one is mine."]
   ["Chapter 1" "This is the first sentence of Chapter 1."]
   ["Chapter 2" "This is the another sentence, in Chapter 2."]
   ["Bonus Chapter" "This is a bonus chapter."]])

(defn- make-chapters [dreams]
  (let [dates (m/dream-dates dreams)
        year-months (d/year-months dates)]
    (for [[year months] year-months
          month months]
      (let [dreams (m/dreams-for-year-month year month dreams)]
        [(str month ", " year)
         (str/join "\n\n"
                   (for [{:keys [date id txt]} dreams]
                     (format "<h2>%s</h2>\n%s"
                             (d/format-date-for-section date)
                             txt)))]))))

(defn mk-epub [dreams]
  (let [#_#_chapters (->> dreams
                          (take 1000)
                          (map (juxt :id :txt)))
        dates (m/dream-dates dreams)
        year-months (d/year-months dates)
        chapters (make-chapters dreams)
        org-dir (str (System/getenv "HOME") "/org")
        cover-image (str org-dir "/dreams-cover.png")]
    ;; (rmrf cdir "META-INF" "mimetype")
    ;; (mkdirp "META-INF")
    ;; (spit "mimetype" "application/epub+zip")
    ;;(clojure.pprint/pprint chapters)
    ;;(prn (d/year-months (m/dream-dates dreams)))
    (generate-epub "dreams.epub"
                   "Dreams"
                   "Eig N. Hombre"
                   cover-image
                   chapters))
  (println "mk-epub"))
