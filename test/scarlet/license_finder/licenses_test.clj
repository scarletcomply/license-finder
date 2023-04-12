(ns scarlet.license-finder.licenses-test
  (:require [clojure.test :refer [deftest is]]
            [scarlet.license-finder.licenses :as licenses]))

(deftest common-license-names-test
  (let [licenses (licenses/common-license-names)]
    (is (map? licenses))
    (doseq [[name id] licenses]
      (is (= id (licenses/find-license-id {:name name}))))))
