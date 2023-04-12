(ns spdx
  (:require [babashka.fs :as fs]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.pprint :as pprint]
            [clojure.set :as set]
            [scarlet.license-finder.licenses :as licenses]))

(def spdx-key-map
  {:licenseId             :id
   :reference             :ref
   :referenceNumber       :ref-no
   :detailsUrl            :details-url
   :seeAlso               :see-also
   :isDeprecatedLicenseId :deprecated?
   :isOsiApproved         :osi-approved?
   :isFsfLibre            :fsf-libre?})

(defn- transform-spdx-licenses [data]
  (into (sorted-map)
        (comp (map #(set/rename-keys % spdx-key-map))
              (map (juxt :id identity)))
        (:licenses data)))

(defn load-spdx-licenses
  "Loads the SPDX license list (see https://spdx.org/licenses/)
   from its JSON file into a map."
  ([]
   (load-spdx-licenses "https://raw.githubusercontent.com/spdx/license-list-data/master/json/licenses.json"))
  ([url]
   (-> url
       slurp
       (json/read-str :key-fn keyword)
       transform-spdx-licenses)))

(defn update-spdx-resource
  "Updates the bundled SPDX license database."
  []
  (let [data (load-spdx-licenses)]
    (with-open [writer (io/writer (fs/file "./resources/" licenses/spdx-resource-name))]
      (pprint/pprint data writer))))

(comment
  (update-spdx-resource)
  ;;
  )
