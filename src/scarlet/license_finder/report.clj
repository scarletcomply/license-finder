(ns scarlet.license-finder.report
  (:require [babashka.fs :as fs]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(def headers ["Dependency" "Version" "Type" "License ID" "License Name" "License URL"])

(defn- dep->vec
  "Return a flat vector with dependency's data out of a nested map"
  [{:keys [version type license] dep-name :name}]
  [dep-name
   version
   (name type)
   (:id license)
   (:name license)
   (:url license)])

(defn- prep-out-path
  [{:keys [project format out-file]
    :or   {format :csv}}]
  (let [filename (str project "." (name format))
        path (or out-file
                 (->> filename
                      (fs/path "./target/licenses")
                      (fs/normalize)))]
    (when-let [dir (fs/parent path)]
      (fs/create-dirs dir))
    (println "Licenses written to" (str path))
    path))

(defn- prep-writer
  "Get a io/writer for a given out-file.

  Create a holding directory if it doesn't exist. The `out-file` value
  `:stdout` is treated as a special value for printing to the standard
  output."
  [{:keys [out-file] :as options}]
  (cond
    (= out-file :stdout) {:writer *out* :close? false}
    :else {:writer (-> options
                       prep-out-path
                       fs/file
                       io/writer)
           :close? true}))

(defn- write-csv [options deps]
  (let [{:keys [writer close?]} (prep-writer options)]
    (->> deps
         (map dep->vec)
         (cons headers)
         (csv/write-csv writer))
    (when close?
      (.close writer))))

;; Support new report formats by adding new defmethods

(defmulti generate-report
  "Generate and write a report for a given format"
  (fn [options _]
    (get options :format)))

(defmethod generate-report :default [options deps]
  (write-csv options deps))
