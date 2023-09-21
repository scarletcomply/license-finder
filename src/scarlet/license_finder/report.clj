(ns scarlet.license-finder.report
  (:require [babashka.fs :as fs]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(def ^:private csv-headers
  ["Dependency" "Version" "Type"
   "License ID" "License Name" "License URL"])

(defn- csv-row
  "Return a flat vector with dependency's data out of a nested map"
  [{:keys [version type license] dep-name :name}]
  [dep-name
   version
   (name type)
   (:id license)
   (:name license)
   (:url license)])

(defn- write-csv [writer deps _opts]
  (->> deps
       (map csv-row)
       (cons csv-headers)
       (csv/write-csv writer)))

(defn write-report
  [{:keys [project out out-file] :as opts} deps]
  (if (= :stdout out)
    (write-csv *out* deps opts)
    (let [file-path (or out
                        out-file ; for compatibility
                        (fs/normalize (fs/path "./target/licenses"
                                               (str project ".csv"))))]
      (when-let [dir (fs/parent file-path)]
        (fs/create-dirs dir))
      (with-open [writer (io/writer (fs/file file-path))]
        (write-csv writer deps opts))
      (println "Licenses written to" (str file-path)))))
