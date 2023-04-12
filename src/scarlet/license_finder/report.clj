(ns scarlet.license-finder.report
  (:require [babashka.fs :as fs]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(defn- write-csv [file-path deps]
  (with-open [writer (io/writer (fs/file file-path))]
    (->> deps
         (map (fn [{:keys [version type license]
                    dep-name :name}]
                [dep-name
                 version
                 (name type)
                 (:id license)
                 (:name license)
                 (:url license)]))
         (cons ["Dependency" "Version" "Type"
                "License ID" "License Name" "License URL"])
         (csv/write-csv writer))))

(defn write-report [{:keys [project out-file]} deps]
  (let [out-file (or out-file
                     (fs/normalize (fs/path "./target/licenses" (str project ".csv"))))]
    (when-let [dir (fs/parent out-file)]
      (fs/create-dirs dir))
    (write-csv out-file deps)
    (println "Licenses written to" (str out-file))))
