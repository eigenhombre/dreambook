(ns dreambook.md-test
  (:require [clojure.test :refer :all]
            [dreambook.md :as md]))

(deftest test-md
  (testing "org->md"
    (are [input expected]
      (= expected (md/org->md input))
      "foo" "foo"
      "" ""
      "/hello/" "*hello*"
      " /hello/ /there/ " " *hello* *there* "
      " /hello there/ " " *hello there* "
      " /City/. " " *City*. "
      " /dead/.\" " " *dead*.\" "
      "a/b" "a/b"
      " a/b/c " " a/b/c "
      "* a\n" "# a\n"
      "* a\nb\n" "# a\nb\n"
      "* a\nb d ee f\n" "# a\nb d ee f\n"
      "* a\n* b\n" "# a\n# b\n"
      "* a\n** b\n" "# a\n## b\n"
      "* a\n** b\n* c\n" "# a\n## b\n# c\n"
      "* a\n** b\n* c\n** d\n" "# a\n## b\n# c\n## d\n"
      "* a\n** b\n* c\n** d\n* e\n" "# a\n## b\n# c\n## d\n# e\n"
      "*** a\n" "### a\n"
      "=code=" "`code`"
      " =code= " " `code` "
      ;; Stripping frontmatter:
      "#+TITLE: Dreams
#+DATE: <2017-08-29 Tue>
#+OPTIONS: toc:nil num:nil

# Introduction" "# Introduction"
      ;; Parsing dates:
      "* <1987-01-12 Mon>\n" "# Monday, January 12\n")))
