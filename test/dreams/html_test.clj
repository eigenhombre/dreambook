(ns dreams.html-test
  (:require [clojure.test :refer [testing deftest are is]]
            [dreams.html :as html]))

(deftest html
  (testing "basic MD->HTML transformation"
    (are [input expected]
      (= expected (html/md->html input))
      "foo" "<p>foo</p>"
      "foo\nbar" "<p>foo\nbar</p>"
      "foo\n\nbar" "<p>foo</p>\n<p>bar</p>"
      "D&D" "<p>D&amp;D</p>"
      " \"enhuges\" " "<p> &#8220;enhuges&#8221; </p>"
      " *over* " "<p> <em>over</em> </p>"
      "# header" "<h1>header</h1>")))
