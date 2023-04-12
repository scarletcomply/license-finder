(ns scarlet.license-finder.resource
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io])
  (:import [java.io PushbackReader]))

(defn read-edn-resource [resource-name]
  (with-open [reader (PushbackReader. (io/reader (io/resource resource-name)))]
    (edn/read reader)))
